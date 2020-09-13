/*******************************************************************************
 * Copyright Julio Leite
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package leitej.net.csl.secure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import leitej.Constant;
import leitej.crypto.Cryptography;
import leitej.crypto.asymmetric.CipherRSA;
import leitej.crypto.asymmetric.certificate.CertificateIoUtil;
import leitej.crypto.hash.HMacEnum;
import leitej.crypto.hash.stream.HMacInputStream;
import leitej.crypto.hash.stream.HMacOutputStream;
import leitej.crypto.symmetric.CipherEnum;
import leitej.crypto.symmetric.stream.CircInputStream;
import leitej.crypto.symmetric.stream.CircOutputStream;
import leitej.exception.CertificateChainLtException;
import leitej.exception.CertificateLtException;
import leitej.exception.ClosedLtRtException;
import leitej.exception.ConnectionLtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.KeyStoreLtException;
import leitej.exception.LtmLtRtException;
import leitej.log.Logger;
import leitej.net.csl.AbstractCommunicationSession;
import leitej.net.csl.secure.rooter.OffRoot;

/**
 *
 * @author Julio Leite
 */
abstract class AbstractCommunicationSecureSession extends
		AbstractCommunicationSession<CommunicationSecureFactory, CommunicationSecureListener, CommunicationSecureHost, CommunicationSecureGuest> {

	// <initiate communication>
	// |> 1Byte to define the version of SecureCommunicationSession
	// |< 1Byte 0x00 confirming the version (else value close connection)

	// |> nBytes of client certificate in DER format (utf8)
	// |< 1Byte 0x00 confirming the client certificate (else value certificate
	// client unknown)
	// // |> nBytes with client chain without the end-point certificate (already
	// sent)
	// // |< 1Byte 0x00 confirming validation of client chain (else value close
	// connection)

	// |< nBytes of host certificate in DER format (utf8)
	// |> 1Byte 0x00 confirming validation of host certificate (else value
	// certificate host unknown)
	// // |< nBytes with host chain without the end-point certificate (already sent)
	// // |> 1Byte 0x00 confirming validation of host chain (else value close
	// connection)

	// |> 2Bytes client defining the size of half-state-key block encrypted with RSA
	// |> nBytes client half-state-key block encrypted with RSA
	// |< 2Bytes host defining the size of half-state-key block encrypted with RSA
	// |< nBytes host half-state-key block encrypted with RSA

	// |> 80Bytes client key block to start a secure half-channel encrypted with
	// Twofish-CIRC
	// |< 80Bytes host key block to start a secure half-channel encrypted with
	// Twofish-CIRC
	// <end communication>

	// <stream wrapped>
	// |-- Twofish-CIRC with HMacSHA512
	// <end stream wrapped>

	// <initiate wrapped communication>
	// |> 64Bytes client random data test
	// |< 64Bytes host random data test
	// <end wrapped communication>

	private static final Logger LOG = Logger.getInstance();

	private byte[] oKeyBytes;
	private byte[] oIvBytes;
	private byte[] oHMacKeyBytes;
	private byte[] iKeyBytes;
	private byte[] iIvBytes;
	private byte[] iHMacKeyBytes;

	/**
	 * Connects and initiates session from guest side.
	 *
	 * @param factory     with settings to apply
	 * @param endpoint    the SocketAddress
	 * @param charsetName the name of the requested charset; may be either a
	 *                    canonical name or an alias
	 * @throws SocketException              if there is an error in the underlying
	 *                                      protocol, such as a TCP error
	 * @throws IllegalArgumentException     if endpoint is null or is a
	 *                                      SocketAddress subclass not supported by
	 *                                      this socket
	 * @throws IllegalArgumentLtRtException if the charset name is not defined
	 * @throws ConnectionLtException        <br/>
	 *                                      +Cause IOException if an error occurs
	 *                                      during the connection
	 */
	protected AbstractCommunicationSecureSession(final CommunicationSecureFactory factory, final SocketAddress endpoint,
			final String charsetName)
			throws SocketException, IllegalArgumentException, IllegalArgumentLtRtException, ConnectionLtException {
		super(factory, endpoint, charsetName);
	}

	/**
	 * initiates session from host side.
	 *
	 * @param factory with settings to apply
	 * @param socket  connected to the guest
	 * @throws SocketException       if there is an error in the underlying
	 *                               protocol, such as a TCP error
	 * @throws ConnectionLtException <br/>
	 *                               +Cause IOException if an error occurs during
	 *                               the connection <br/>
	 *                               +Cause IllegalArgumentLtRtException if the
	 *                               charset name read from socket is not defined on
	 *                               host
	 */
	protected AbstractCommunicationSecureSession(final CommunicationSecureFactory factory, final Socket socket)
			throws SocketException, ConnectionLtException {
		super(factory, socket);
	}

	@Override
	protected final OutputStream getOutputStreamWrapped(final OutputStream out) throws ConnectionLtException {
		try {
			final CircOutputStream circOutputStream = new CircOutputStream(out, CipherEnum.Twofish);
			circOutputStream.cipherInit(true, Cryptography.keyProduce(CipherEnum.Twofish, this.oKeyBytes),
					Cryptography.ivProduce(this.oIvBytes));
			return new HMacOutputStream(circOutputStream, HMacEnum.HMacSHA512,
					Cryptography.hmacKeyProduce(HMacEnum.HMacSHA512, this.oHMacKeyBytes), Constant.MEGA);
		} catch (final InvalidKeyException e) {
			throw new ConnectionLtException(e);
		}
	}

	@Override
	protected final InputStream getInputStreamWrapped(final InputStream in) throws ConnectionLtException {
		try {
			final CircInputStream circInputStream = new CircInputStream(in, CipherEnum.Twofish);
			circInputStream.cipherInit(false, Cryptography.keyProduce(CipherEnum.Twofish, this.iKeyBytes),
					Cryptography.ivProduce(this.iIvBytes));
			return new HMacInputStream(circInputStream, HMacEnum.HMacSHA512,
					Cryptography.hmacKeyProduce(HMacEnum.HMacSHA512, this.iHMacKeyBytes), Constant.MEGA);
		} catch (final InvalidKeyException e) {
			throw new ConnectionLtException(e);
		}
	}

	protected final void sendConfirmationByte(final OutputStream out) throws IOException {
		out.write(0x00);
		out.flush();
	}

	protected final void sendDenyByte(final OutputStream out) throws IOException {
		out.write(0x01);
		out.flush();
	}

	protected final int readByte(final InputStream in) throws IOException {
		int tmp;
		tmp = in.read();
		if (tmp == -1) {
			throw new IOException(new ClosedLtRtException("Unexpected end of stream"));
		}
		return tmp;
	}

	protected final void sendMyIdentification(final InputStream in, final OutputStream out)
			throws IOException, KeyStoreLtException {
		// |< nBytes of host certificate in DER format (utf8)
		final X509Certificate certificate = getFactory().getCslVault().getCslCertificate();
		if (certificate == null) {
			throw new IllegalStateLtRtException();
		}
		LOG.debug("Sending my end-certificate");
		try {
			CertificateIoUtil.writeX509Certificates(out, certificate);
		} catch (final CertificateLtException e) {
			throw new IOException(e);
		}
		out.flush();
		// |> 1Byte 0x00 confirming validation of host certificate (else value
		// certificate host unknown)
		int tmp = readByte(in);
		if (tmp != 0) {
			LOG.debug("Sending my chain certificate");
			// |< nBytes with host chain without the end-point certificate (already sent)
			final X509Certificate[] hostChain = getFactory().getCslVault().getCslChainCertificate();
			try {
				for (int i = 1; i < hostChain.length; i++) {
					CertificateIoUtil.writeX509Certificates(out, hostChain[i]);
				}
			} catch (final CertificateLtException e) {
				throw new IOException(e);
			}
			out.flush();
			// |> 1Byte 0x00 confirming validation of host chain (else value close
			// connection)
			tmp = readByte(in);
			if (tmp != 0) {
				throw new IOException(new IllegalStateLtRtException("Endpoint held my chain invalid"));
			}
		}
	}

	protected final X509Certificate readRemoteIdentification(final InputStream in, final OutputStream out)
			throws IOException {
		// |> nBytes of client certificate in DER format (utf8)
		// |< 1Byte 0x00 confirming the client certificate (else value certificate
		// client unknown)
		X509Certificate clientCertificate = null;
		try {
			clientCertificate = CertificateIoUtil.readX509Certificate(in);
			LOG.debug("Receiving remote end-certificate");
			getFactory().getCslVault().verifyEndPointCertificate(clientCertificate);
			sendConfirmationByte(out);
		} catch (final CertificateLtException e) {
			sendDenyByte(out);
			LOG.debug("Receiving remote chain certificate");
			if (clientCertificate != null) {
				// |> nBytes with client chain without the end-point certificate (already sent)
				// |< 1Byte 0x00 confirming validation of client chain (else value close
				// connection)
				final X509Certificate[] clientChain = new X509Certificate[OffRoot.COMMUNICATION_PATH_LENGTH];
				clientChain[0] = clientCertificate;
				try {
					for (int i = 1; i < clientChain.length; i++) {
						clientChain[i] = CertificateIoUtil.readX509Certificate(in);
					}
					getFactory().getCslVault().addEndPointChain(clientChain);
					sendConfirmationByte(out);
				} catch (final CertificateLtException e1) {
					sendDenyByte(out);
					throw new IOException(new IllegalStateLtRtException(e1, "Endpoint certificate is invalid"));
				} catch (final CertificateChainLtException e1) {
					sendDenyByte(out);
					throw new IOException(new IllegalStateLtRtException(e1, "Endpoint certificate is invalid"));
				} catch (final LtmLtRtException e1) {
					sendDenyByte(out);
					throw new IOException(e1);
				} catch (final KeyStoreLtException e1) {
					sendDenyByte(out);
					throw new IOException(e1);
				}
			} else {
				// some problem with client certificate
				sendDenyByte(out);
				throw new IOException(e);
			}
		}
		return clientCertificate;
	}

	protected final byte[] sendMyHalfStateKeyBlock(final OutputStream out, final CipherRSA cipherRSA)
			throws IOException {
		final byte[] key = new byte[48];
		getFactory().secureRandomFill(key);
		final byte[] block = cipherRSA.encript(key);
		// |< 2Bytes host defining the size of half-state-key block encrypted with RSA
		LOG.debug("#0", block.length);
		out.write((block.length >>> 8) & 0xff);
		// |< nBytes host half-state-key block encrypted with RSA
		out.write(block.length & 0xff);
		out.write(block);
		out.flush();
		return key;
	}

	protected final byte[] readRemoteHalfStateKeyBlock(final InputStream in, final CipherRSA cipherRSA)
			throws IOException {
		// |> 2Bytes client defining the size of half-state-key block encrypted with RSA
		final int blockSize = readByte(in) << 8 + readByte(in);
		LOG.debug("#0", blockSize);
		// |> nBytes client half-state-key block encrypted with RSA
		final byte[] block = new byte[blockSize];
		int off = 0;
		while (off != blockSize) {
			off += in.read(block, off, blockSize - off);
		}
		return cipherRSA.decript(block);
	}

	protected final void readRemoteKeyBlock(final InputStream in, final byte[] remoteKeyBlock)
			throws IOException, InvalidKeyException {
		// |> 80Bytes client key block to start a secure half-channel encrypted with
		// Twofish-CIRC
		final byte[] block = new byte[80];
		int off = 0;
		while (off != block.length) {
			off += in.read(block, off, block.length - off);
		}
		final ByteArrayInputStream bis = new ByteArrayInputStream(block);
		final CircInputStream circInputStream = new CircInputStream(bis, CipherEnum.Twofish);
		circInputStream.cipherInit(false,
				Cryptography.keyProduce(CipherEnum.Twofish, Arrays.copyOfRange(remoteKeyBlock, 0, 32)),
				Cryptography.ivProduce(Arrays.copyOfRange(remoteKeyBlock, 32, 48)));
		final byte[] keys = new byte[80];
		off = 0;
		while (off != keys.length) {
			off += circInputStream.read(keys, off, keys.length - off);
		}
		circInputStream.close();
		setKeys(keys, false);
	}

	protected final void sendMyKeyBlock(final OutputStream out, final byte[] myKeyBlock)
			throws IOException, InvalidKeyException {
		// |< 80Bytes host key block to start a secure half-channel encrypted with
		// Twofish-CIRC
		final byte[] keys = new byte[80];
		getFactory().secureRandomFill(keys);
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(keys.length);
		final CircOutputStream circOutputStream = new CircOutputStream(bos, CipherEnum.Twofish);
		circOutputStream.cipherInit(true,
				Cryptography.keyProduce(CipherEnum.Twofish, Arrays.copyOfRange(myKeyBlock, 0, 32)),
				Cryptography.ivProduce(Arrays.copyOfRange(myKeyBlock, 32, 48)));
		circOutputStream.write(keys);
		circOutputStream.flush();
		circOutputStream.close();
		bos.writeTo(out);
		out.flush();
		setKeys(keys, true);
	}

	private void setKeys(final byte[] b, final boolean mine) {
		LOG.trace("#0", b);
		if (mine) {
			// TODO: procurar onde utilizado 'Arrays.' e para aumentar a seguranca criar um
			// arraysutil com metodos proprios para invocar copia de arrays de keys dentro
			// da jvm
			this.oKeyBytes = Arrays.copyOfRange(b, 0, 32);
			this.oIvBytes = Arrays.copyOfRange(b, 32, 48);
			this.iHMacKeyBytes = Arrays.copyOfRange(b, 48, 80);
		} else {
			this.iKeyBytes = Arrays.copyOfRange(b, 0, 32);
			this.iIvBytes = Arrays.copyOfRange(b, 32, 48);
			this.oHMacKeyBytes = Arrays.copyOfRange(b, 48, 80);
		}
	}

}

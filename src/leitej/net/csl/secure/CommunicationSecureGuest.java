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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.cert.X509Certificate;

import leitej.crypto.asymmetric.CipherRSA;
import leitej.crypto.asymmetric.PaddingAsymEnum;
import leitej.crypto.exception.KeyStoreLtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.log.Logger;
import leitej.net.exception.ConnectionLtException;

/**
 *
 * @author Julio Leite
 */
public final class CommunicationSecureGuest extends AbstractCommunicationSecureSession {

	private static final Logger LOG = Logger.getInstance();

	private byte[] remoteKeyBlock;
	private byte[] myKeyBlock;

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
	protected CommunicationSecureGuest(final CommunicationSecureFactory factory, final SocketAddress endpoint,
			final String charsetName)
			throws SocketException, IllegalArgumentLtRtException, IllegalArgumentException, ConnectionLtException {
		super(factory, endpoint, charsetName);
	}

	@Override
	protected final void initiateCommunication(final InputStream in, final OutputStream out)
			throws ConnectionLtException {
		try {
			int tmp;
			// |> 1Byte to define the version of SecureCommunicationSession
			out.write(CommunicationSecureFactory.VERSION);
			out.flush();
			// |< 1Byte 0x00 confirming the version (else value close connection)
			tmp = readByte(in);
			if (tmp != 0) {
				throw new IOException(new IllegalStateLtRtException("Endpoint with diferent version"));
			}

			sendMyIdentification(in, out);
			final X509Certificate hostCertificate = readRemoteIdentification(in, out);

			final CipherRSA cipherRSA = new CipherRSA(hostCertificate.getPublicKey(),
					getFactory().getCslVault().getCslPrivateKey(), PaddingAsymEnum.OAEPWithSHA512AndMGF1Padding);
			this.myKeyBlock = sendMyHalfStateKeyBlock(out, cipherRSA);
			out.flush();
			getFactory().getCslVault().saltOut(this.myKeyBlock, hostCertificate);
			LOG.trace("myKeyBlock: #0", this.myKeyBlock);
			this.remoteKeyBlock = readRemoteHalfStateKeyBlock(in, cipherRSA);
			getFactory().getCslVault().saltIn(this.remoteKeyBlock, hostCertificate);
			LOG.trace("remoteKeyBlock: #0", this.remoteKeyBlock);

			sendMyKeyBlock(out, this.myKeyBlock);
			readRemoteKeyBlock(in, this.remoteKeyBlock);
		} catch (final IOException e) {
			throw new ConnectionLtException(e);
		} catch (final KeyStoreLtException e) {
			throw new ConnectionLtException(e);
		} catch (final InvalidKeyException e) {
			throw new ConnectionLtException(e);
		}
	}

	@Override
	protected final void initiateWrappedCommunication(final InputStream in, final OutputStream out)
			throws ConnectionLtException {
		// TODO: |> 64Bytes client random data test
		// TODO: |< 64Bytes host random data test
		// FIXME:
	}

}

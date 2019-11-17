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
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.cert.X509Certificate;

import leitej.crypto.asymmetric.CipherRSA;
import leitej.crypto.asymmetric.PaddingAsymEnum;
import leitej.crypto.exception.KeyStoreLtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.log.Logger;
import leitej.net.exception.ConnectionLtException;

/**
 *
 * @author Julio Leite
 */
public final class CommunicationSecureHost extends AbstractCommunicationSecureSession {

	private static final Logger LOG = Logger.getInstance();

	private byte[] remoteKeyBlock;
	private byte[] myKeyBlock;

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
	protected CommunicationSecureHost(final CommunicationSecureFactory factory, final Socket socket)
			throws SocketException, ConnectionLtException {
		super(factory, socket);
	}

	@Override
	protected final void initiateCommunication(final InputStream in, final OutputStream out)
			throws ConnectionLtException {
		try {
			int tmp;
			// |> 1Byte to define the version of SecureCommunicationSession
			tmp = readByte(in);
			// |< 1Byte 0x00 confirming the version (else value close connection)
			if (CommunicationSecureFactory.VERSION != tmp) {
				sendDenyByte(out);
				throw new IOException(new IllegalStateLtRtException("lt.CSLWrongVersion"));
			} else {
				sendConfirmationByte(out);
			}

			final X509Certificate clientCertificate = readRemoteIdentification(in, out);
			sendMyIdentification(in, out);

			final CipherRSA cipherRSA = new CipherRSA(clientCertificate.getPublicKey(),
					getFactory().getCslVault().getCslPrivateKey(), PaddingAsymEnum.OAEPWithSHA512AndMGF1Padding);
			this.remoteKeyBlock = readRemoteHalfStateKeyBlock(in, cipherRSA);
			getFactory().getCslVault().saltIn(this.remoteKeyBlock, clientCertificate);
			LOG.trace("remoteKeyBlock: #0", this.remoteKeyBlock);
			this.myKeyBlock = sendMyHalfStateKeyBlock(out, cipherRSA);
			out.flush();
			getFactory().getCslVault().saltOut(this.myKeyBlock, clientCertificate);
			LOG.trace("myKeyBlock: #0", this.myKeyBlock);

			readRemoteKeyBlock(in, this.remoteKeyBlock);
			sendMyKeyBlock(out, this.myKeyBlock);
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

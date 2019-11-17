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
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.security.SecureRandom;

import leitej.crypto.exception.KeyStoreLtException;
import leitej.crypto.keyStore.Password;
import leitej.exception.ExpiredDataLtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.net.ConstantNet;
import leitej.net.csl.AbstractCommunicationFactory;
import leitej.net.csl.secure.rooter.OffRoot;
import leitej.net.csl.secure.vault.CslVaultItf;
import leitej.net.exception.ConnectionLtException;

/**
 *
 * @author Julio Leite
 */
public final class CommunicationSecureFactory extends
		AbstractCommunicationFactory<CommunicationSecureFactory, CommunicationSecureListener, CommunicationSecureHost, CommunicationSecureGuest> {

	static final byte VERSION = 0x01;
	static final int SECURE_CHAIN_LENGTH = OffRoot.COMMUNICATION_PATH_LENGTH;

	private final SecureRandom secureRandom = new SecureRandom();
	private final CslVaultItf cslVault;

	/**
	 *
	 * @param password used to load the CSL vault
	 * @throws IOException
	 * @throws KeyStoreLtException
	 * @throws ExpiredDataLtException if at the first load of the vault, the primary
	 *                                application trusted anchor does not pass
	 *                                verification procedure
	 */
	public CommunicationSecureFactory(final Password password)
			throws KeyStoreLtException, IOException, ExpiredDataLtException {
		this((new CslVault(password)), ConstantNet.DEFAULT_VELOCITY, ConstantNet.DEFAULT_SIZE_PER_SENTENCE,
				ConstantNet.DEFAULT_TIMEOUT_MS);
	}

	/**
	 *
	 * @param cslVault with key and certificate
	 */
	public CommunicationSecureFactory(final CslVaultItf cslVault) {
		this(cslVault, ConstantNet.DEFAULT_VELOCITY, ConstantNet.DEFAULT_SIZE_PER_SENTENCE,
				ConstantNet.DEFAULT_TIMEOUT_MS);
	}

	/**
	 *
	 * @param cslVault        with key and certificate
	 * @param velocity        byte per second (0 infinite)
	 * @param sizePerSentence number of bytes per read step (0 infinite)
	 * @param msTimeOut       the specified timeout, in milliseconds (0 infinite)
	 */
	public CommunicationSecureFactory(final CslVaultItf cslVault, final int velocity, final int sizePerSentence,
			final int msTimeOut) {
		super(velocity, sizePerSentence, msTimeOut);
		this.cslVault = cslVault;
	}

	protected final void secureRandomFill(final byte[] bytes) {
		synchronized (this.secureRandom) {
			this.secureRandom.nextBytes(bytes);
		}
	}

	protected final CslVaultItf getCslVault() {
		return this.cslVault;
	}

	@Override
	public final CommunicationSecureGuest clientInstanciation(final SocketAddress endpoint, final String charsetName)
			throws ConnectionLtException {
		try {
			return new CommunicationSecureGuest(this, endpoint, charsetName);
		} catch (final IllegalArgumentLtRtException e) {
			throw new ConnectionLtException(e);
		} catch (final SocketException e) {
			throw new ConnectionLtException(e);
		} catch (final IllegalArgumentException e) {
			throw new ConnectionLtException(e);
		}
	}

	@Override
	public final CommunicationSecureListener serverInstanciation(final int port, final int backlog,
			final InetAddress bindAddr) throws ConnectionLtException {
		try {
			return new CommunicationSecureListener(this, port, backlog, bindAddr);
		} catch (final SecurityException e) {
			throw new ConnectionLtException(e);
		} catch (final SocketException e) {
			throw new ConnectionLtException(e);
		} catch (final IOException e) {
			throw new ConnectionLtException(e);
		}
	}

}

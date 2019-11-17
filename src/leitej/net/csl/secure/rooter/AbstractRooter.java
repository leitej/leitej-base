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

package leitej.net.csl.secure.rooter;

import java.io.IOException;

import leitej.crypto.exception.KeyStoreLtException;
import leitej.crypto.keyStore.Password;
import leitej.exception.ExpiredDataLtException;
import leitej.exception.SeppukuLtRtException;
import leitej.log.Logger;

/*
 * Level 5 - trusted root - self-signed - off-line - CA		| 2 trusted roots (1 inactive - contingency)	|8192b - 32y16
 * Level 4 - root# link - on-line - CA						| 5 root links (continents)						|8192b - 16y 8
 * Level 3 - country - CA																					|4096b -  8y 4
 * Level 2 - region - CA																					|4096b -  4y 2
 * Level 1 - city - CA																						|4096b -  2y 1
 * Level 0 - end-point - communication-level																|2048b -  1y 1
 */
/**
 *
 * @author Julio Leite
 */
abstract class AbstractRooter {

	private static final Logger LOG = Logger.getInstance();

	private static final String REPOSITORY_FILE_DIR = OffRoot.REPOSITORY_FILE_DIR;
	private static final int COMMUNICATION_PATH_LENGTH = OffRoot.COMMUNICATION_PATH_LENGTH;
	private static final int RSA_KEY_BIT_SIZE = 8192;

	private final Vault vault;

	/**
	 *
	 * @param password
	 * @throws KeyStoreLtException    if the keystore cannot be loaded
	 * @throws IOException            if there is an I/O or format problem with the
	 *                                keystore data, if a password is required but
	 *                                not given, or if the given password was
	 *                                incorrect. If the error is due to a wrong
	 *                                password, the {@link Throwable#getCause cause}
	 *                                of the <code>IOException</code> should be an
	 *                                <code>UnrecoverableKeyException</code>
	 * @throws ExpiredDataLtException if at the first load of the vault, the primary
	 *                                application trusted anchor does not pass
	 *                                verification procedure
	 */
	protected AbstractRooter(final Password password) throws KeyStoreLtException, IOException, ExpiredDataLtException {
		this.vault = new Vault(password);
		if (!this.vault.hasRooterKey()) {
			// TODO:
			// KeyPair keys = Cryptography.RSA.keyPairGenerate(RSA_KEY_BIT_SIZE);
			throw new SeppukuLtRtException(1, null, "");
		}
		if (!this.vault.hasCslKey()) {
			// TODO:

		}
	}

}

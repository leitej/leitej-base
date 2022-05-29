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
import java.security.PrivateKey;

import org.bouncycastle.cert.X509CertificateHolder;

import leitej.crypto.keyStore.Password;
import leitej.exception.CertificateLtException;
import leitej.exception.ExpiredDataLtException;
import leitej.exception.KeyStoreLtException;
import leitej.net.csl.secure.vault.AbstractCslVault;

/**
 *
 * @author Julio Leite
 */
final class Vault extends AbstractCslVault {

	private static final String ROOTER_PRIVATE_KEY_ENTRY_ALIAS = "__ROOTER_PRIVATE_KEY_ENTRY_ALIAS__";
	private static final String ROOTER_PRIVATE_KEY_ENTRY_OLD_ALIAS = "__ROOTER_PRIVATE_KEY_ENTRY_OLD_ALIAS__";

	/**
	 * @param password
	 * @throws KeyStoreLtException
	 * @throws IOException
	 * @throws ExpiredDataLtException if at the first load of the vault, the primary
	 *                                application trusted anchor does not pass
	 *                                verification procedure
	 * @throws CertificateLtException
	 */
	Vault(final Password password)
			throws KeyStoreLtException, IOException, ExpiredDataLtException, CertificateLtException {
		super(password);
	}

	/**
	 *
	 * @return true only if has my key, otherwise false
	 * @throws KeyStoreLtException if the key cannot be recovered
	 */
	final boolean hasRooterKey() throws KeyStoreLtException {
		return this.vault.getPrivateKey(ROOTER_PRIVATE_KEY_ENTRY_ALIAS) != null;
	}

	/**
	 *
	 * @param key
	 * @param certificates
	 * @throws KeyStoreLtException    if the given key cannot be protected, or this
	 *                                operation fails for some other reason
	 * @throws IOException            if there was an I/O problem with data
	 * @throws CertificateLtException
	 */
	final synchronized void setRooterKey(final PrivateKey key, final X509CertificateHolder[] certificates)
			throws KeyStoreLtException, IOException, CertificateLtException {
		this.vault.setPrivateKeyEntry(ROOTER_PRIVATE_KEY_ENTRY_ALIAS, key, certificates);
		this.vault.persist();
	}

	final PrivateKey getRooterPrivateKey() throws KeyStoreLtException {
		return this.vault.getPrivateKey(ROOTER_PRIVATE_KEY_ENTRY_ALIAS);
	}

	final X509CertificateHolder[] getRooterChainCertificate() throws KeyStoreLtException, CertificateLtException {
		return this.vault.getKeyCertificateChain(ROOTER_PRIVATE_KEY_ENTRY_ALIAS);
	}

	final X509CertificateHolder getRooterCertificate() throws KeyStoreLtException, CertificateLtException {
		return this.vault.getKeyCertificate(ROOTER_PRIVATE_KEY_ENTRY_ALIAS);
	}

}

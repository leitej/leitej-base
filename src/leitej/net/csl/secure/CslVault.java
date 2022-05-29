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

import leitej.crypto.keyStore.Password;
import leitej.exception.CertificateLtException;
import leitej.exception.ExpiredDataLtException;
import leitej.exception.KeyStoreLtException;
import leitej.net.csl.secure.vault.AbstractCslVault;

/**
 *
 * @author Julio Leite
 */
final class CslVault extends AbstractCslVault {

	/**
	 * @param password
	 * @throws KeyStoreLtException
	 * @throws IOException
	 * @throws ExpiredDataLtException if at the first load of the vault, the primary
	 *                                application trusted anchor does not pass
	 *                                verification procedure
	 * @throws CertificateLtException
	 */
	public CslVault(final Password password)
			throws KeyStoreLtException, IOException, ExpiredDataLtException, CertificateLtException {
		super(password);
		if (!hasCslKey()) {
			// TODO:

		}
	}

}

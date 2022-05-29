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

package leitej.crypto.vault;

import leitej.ltm.Index;
import leitej.ltm.LtmObjectModelling;
import leitej.util.data.Obfuscate;

/**
 *
 * @author Julio Leite
 */
public interface VaultSecretIV extends LtmObjectModelling {

	public String getLtmKeyStoreAlias();

	public void setLtmKeyStoreAlias(String ltmKeyStoreAlias);

	@Index
	public String getAlias();

	public void setAlias(String alias);

	@Obfuscate
	public String getIv();

	public void setIv(String iv);

}

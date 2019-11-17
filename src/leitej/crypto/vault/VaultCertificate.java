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

import leitej.ltm.LtmBinary;
import leitej.ltm.LtmObjectModelling;
import leitej.ltm.annotation.CascadeTypeEnum;
import leitej.ltm.annotation.Column;
import leitej.ltm.annotation.JoinColumn;
import leitej.ltm.annotation.LongTermMemory;
import leitej.ltm.annotation.ManyToOne;

/**
 *
 * @author Julio Leite
 */
@LongTermMemory(name = "lt_vault_certificate")
public interface VaultCertificate extends LtmObjectModelling {

	public static final String FIELD_KEY_LTM_STORE_ALIAS = "keyLtmStoreAlias";

	@Column(updatable = false, nullable = false)
	public String getKeyLtmStoreAlias();

	public void setKeyLtmStoreAlias(String keyLtmStoreAlias);

	// TODO: index keyLtmStoreAlias + alias

	public static final String FIELD_ALIAS = "alias";

	@Column(updatable = false, nullable = false)
	public String getAlias();

	public void setAlias(String alias);

	public static final String FIELD_CERTIFICATE = "certificate";

	public LtmBinary getCertificate();

	public void setCertificate(LtmBinary certificate);

	public static final String FIELD_ISSUER = "issuer";

	@ManyToOne(cascade = { CascadeTypeEnum.SAVE })
	@JoinColumn(nullable = true, updatable = false)
	public VaultCertificate getIssuer();

	public void setIssuer(VaultCertificate issuer);

}

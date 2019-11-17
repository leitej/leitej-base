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

package leitej.net.csl.secure.vault;

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
@LongTermMemory(name = "csl_vault_cadastre")
public interface Cadastre extends LtmObjectModelling {

	public static final String FIELD_ALIAS = "alias";

	@Column(updatable = false, unique = true, nullable = false)
	public String getAlias();

	public void setAlias(String alias);

	public static final String FIELD_SALT_IN = "saltIn";

	@Column(nullable = false)
	public Byte[] getSaltIn();

	public void setSaltIn(Byte[] saltIn);

	public static final String FIELD_SALT_OUT = "saltOut";

	@Column(nullable = false)
	public Byte[] getSaltOut();

	public void setSaltOut(Byte[] saltOut);

	public static final String FIELD_ISSUER = "issuer";

	@ManyToOne(cascade = { CascadeTypeEnum.SAVE })
	@JoinColumn(nullable = false, updatable = false)
	public CadastreIssuer getIssuer();

	public void setIssuer(CadastreIssuer issuer);

}

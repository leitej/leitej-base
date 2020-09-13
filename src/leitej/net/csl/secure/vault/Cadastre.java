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

/**
 *
 * @author Julio Leite
 */
public interface Cadastre extends LtmObjectModelling {

	public static final String FIELD_ALIAS = "alias";

	public String getAlias();

	public void setAlias(String alias);

	public static final String FIELD_SALT_IN = "saltIn";

	public byte[] getSaltIn();

	public void setSaltIn(byte[] saltIn);

	public static final String FIELD_SALT_OUT = "saltOut";

	public byte[] getSaltOut();

	public void setSaltOut(byte[] saltOut);

	public static final String FIELD_ISSUER = "issuer";

	public CadastreIssuer getIssuer();

	public void setIssuer(CadastreIssuer issuer);

}

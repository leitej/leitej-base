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
public interface CadastreIssuer extends LtmObjectModelling {

	// TODO replace the alias and issuer with
	// MAP<alias,issuer>

	public static final String FIELD_ALIAS = "alias";

	public String getAlias();

	public void setAlias(String alias);

	public static final String FIELD_ISSUER = "issuer";

	public CadastreIssuer getIssuer();

	public void setIssuer(CadastreIssuer issuer);

}

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

package leitej.crypto;

/**
 *
 * @author Julio Leite
 */
public final class ConstantCrypto {

	private ConstantCrypto() {
	}

	// public static final String DEFAULT_JCE_PROVIDER = "SunJCE";

	public static final String BASIC_CONSTRAIN_OID = "2.5.29.19";
	public static final String SUBJECT_ALTERNATIVE_NAME_OID = "2.5.29.17";

	public static final String CERTIFICATE_FILE_EXTENSION = ".crt";
	public static final String REQUEST_FILE_EXTENSION = ".req";

	public static final String DEFAULT_UBER_KEYSTORE_FILE_EXTENSION = ".vault.ubr";

}

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

package leitej.crypto.asymmetric;

import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
public enum PaddingAsymEnum {
	NoPadding, PKCS1Padding, OAEPWithSHA1AndMGF1Padding, OAEPWithMD5AndMGF1Padding, OAEPWithSHA224AndMGF1Padding,
	OAEPWithSHA256AndMGF1Padding, OAEPWithSHA384AndMGF1Padding, OAEPWithSHA512AndMGF1Padding, ISO9796_1Padding;

	public String getName() {
		String result;
		switch (this) {
		case NoPadding:
			result = "NoPadding";
			break;
		case PKCS1Padding:
			result = "PKCS1Padding";
			break;
		case OAEPWithSHA1AndMGF1Padding:
			result = "OAEPWithSHA1AndMGF1Padding";
			break;
		case OAEPWithMD5AndMGF1Padding:
			result = "OAEPWithMD5AndMGF1Padding";
			break;
		case OAEPWithSHA224AndMGF1Padding:
			result = "OAEPWithSHA224AndMGF1Padding";
			break;
		case OAEPWithSHA256AndMGF1Padding:
			result = "OAEPWithSHA256AndMGF1Padding";
			break;
		case OAEPWithSHA384AndMGF1Padding:
			result = "OAEPWithSHA384AndMGF1Padding";
			break;
		case OAEPWithSHA512AndMGF1Padding:
			result = "OAEPWithSHA512AndMGF1Padding";
			break;
		case ISO9796_1Padding:
			result = "ISO9796-1Padding";
			break;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
		return result;
	}
}

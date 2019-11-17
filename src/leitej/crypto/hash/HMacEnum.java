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

package leitej.crypto.hash;

import leitej.exception.ImplementationLtRtException;

/**
 * Hash MAC (Message authentication code).<br/>
 * The JCA specifies a naming convention for HMAC objects: HmacDigest.<br/>
 * It also specifies a naming convention for MACs based on password-based
 * encryption (PBE), which is PBEwithMac, where Mac is the full name of the MAC
 * being used.
 *
 * @author Julio Leite
 */
public enum HMacEnum {
	HMacMD2, HMacMD4, HMacMD5, HMacRIPEMD128, HMacRIPEMD160, HMacSHA1, HMacSHA224, HMacSHA256, HMacSHA384, HMacSHA512,
	PBEWithHMacRIPEMD160, PBEWithHMacSHA1;

	public String getName() {
		String result;
		switch (this) {
		case HMacMD2:
			result = "HMacMD2";
			break;
		case HMacMD4:
			result = "HMacMD4";
			break;
		case HMacMD5:
			result = "HMacMD5";
			break;
		case HMacRIPEMD128:
			result = "HMacRIPEMD128";
			break;
		case HMacRIPEMD160:
			result = "HMacRIPEMD160";
			break;
		case HMacSHA1:
			result = "HMacSHA1";
			break;
		case HMacSHA224:
			result = "HMacSHA224";
			break;
		case HMacSHA256:
			result = "HMacSHA256";
			break;
		case HMacSHA384:
			result = "HMacSHA384";
			break;
		case HMacSHA512:
			result = "HMacSHA512";
			break;
		case PBEWithHMacRIPEMD160:
			result = "PBEWithHMacRIPEMD160";
			break;
		case PBEWithHMacSHA1:
			result = "PBEWithHMacSHA1";
			break;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
		return result;
	}
}

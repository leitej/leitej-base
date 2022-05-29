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

package leitej.crypto.symmetric;

import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
public enum CipherEnum {
	AES, Blowfish, CAST5, CAST6, DES, DESEDE, GOST_28147, RC2, RC5, RC6, Rijndael, Serpent, Skipjack, Twofish;

	public String getName() {
		String result;
		switch (this) {
		case Twofish:
			result = "Twofish";
			break;
		case AES:
			result = "AES";
			break;
		case Blowfish:
			result = "Blowfish";
			break;
		case CAST5:
			result = "CAST5";
			break;
		case CAST6:
			result = "CAST6";
			break;
		case DES:
			result = "DES";
			break;
		case DESEDE:
			result = "DESEDE";
			break;
		case GOST_28147:
			result = "GOST-28147";
			break;
		case RC2:
			result = "RC2";
			break;
		case RC5:
			result = "RC5";
			break;
		case RC6:
			result = "RC6";
			break;
		case Rijndael:
			result = "Rijndael";
			break;
		case Serpent:
			result = "Serpent";
			break;
		case Skipjack:
			result = "Skipjack";
			break;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
		return result;
	}

	public int bestKeyBitSize() {
		int result;
		switch (this) {
		case AES:
			result = 256;
			break;
		case Blowfish:
			result = 128;
			break;
		case CAST5:
			throw new ImplementationLtRtException(this.toString());
		case CAST6:
			result = 256;
			break;
		case DES:
			result = 64;
			break;
		case DESEDE:
			result = 192;
			break;
		case GOST_28147:
			result = 256;
			break;
		case RC2:
			result = 128;
			break;
		case RC5:
			result = 128;
			break;
		case RC6:
			result = 256;
			break;
		case Rijndael:
			result = 192;
			break;
		case Serpent:
			result = 192;
			break;
		case Skipjack:
			result = 80;
			break;
		case Twofish:
			result = 256;
			break;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
		return result;
	}

	// TODO:
	public int ivBitSize() {
		int result;
		switch (this) {
		case AES:
			result = 128;
			break;
		case Blowfish:
			throw new ImplementationLtRtException(this.toString());
		case CAST5:
			throw new ImplementationLtRtException(this.toString());
		case CAST6:
			throw new ImplementationLtRtException(this.toString());
		case DES:
			throw new ImplementationLtRtException(this.toString());
		case DESEDE:
			throw new ImplementationLtRtException(this.toString());
		case GOST_28147:
			throw new ImplementationLtRtException(this.toString());
		case RC2:
			throw new ImplementationLtRtException(this.toString());
		case RC5:
			throw new ImplementationLtRtException(this.toString());
		case RC6:
			throw new ImplementationLtRtException(this.toString());
		case Rijndael:
			result = 128;
			break;
		case Serpent:
			throw new ImplementationLtRtException(this.toString());
		case Skipjack:
			throw new ImplementationLtRtException(this.toString());
		case Twofish:
			result = 128;
			break;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
		return result;
	}

}

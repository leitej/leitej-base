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
 * Message digest (or cryptographic hash).<br/>
 * Message digests compute a cryptographic hash, or secure checksum, for a
 * particular message.
 *
 * @author Julio Leite
 */
public enum MessageDigestEnum {
	GOST3411, MD2, MD4, MD5, RIPEMD128, RIPEMD160, RIPEMD256, RIPEMD320, SHA1, SHA224, SHA256, SHA384, SHA512, Tiger,
	Whirlpool;

	public String getName() {
		String result;
		switch (this) {
		case GOST3411:
			result = "GOST3411";
			break;
		case MD2:
			result = "MD2";
			break;
		case MD4:
			result = "MD4";
			break;
		case MD5:
			result = "MD5";
			break;
		case RIPEMD128:
			result = "RIPEMD128";
			break;
		case RIPEMD160:
			result = "RIPEMD160";
			break;
		case RIPEMD256:
			result = "RIPEMD256";
			break;
		case RIPEMD320:
			result = "RIPEMD320";
			break;
		case SHA1:
			result = "SHA1";
			break;
		case SHA224:
			result = "SHA224";
			break;
		case SHA256:
			result = "SHA256";
			break;
		case SHA384:
			result = "SHA384";
			break;
		case SHA512:
			result = "SHA512";
			break;
		case Tiger:
			result = "Tiger";
			break;
		case Whirlpool:
			result = "Whirlpool";
			break;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
		return result;
	}
}

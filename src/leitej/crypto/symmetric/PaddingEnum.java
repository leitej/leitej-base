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

/*
 * NoPadding. 						No Padding
 * PKCS7Padding (PKCS5Padding). 	Padding mechanism defined in PKCS #5 and PKCS #7 comes from Public-Key
 * 									Cryptography Standards that were developed by RSA Security.
 * ISO10126-2Padding. 				A padding mechanism defined in ISO10126-2. The last byte of the padding is
 * 									the number of pad bytes; the remaining bytes of the padding are made up of
 * 									random data.
 * ISO7816-4Padding. 				A padding mechanism defined in ISO7816-4. The first byte of the padding is
 * 									the value 0x80; the remaining bytes of the padding are made up of zeros.
 * TBCPadding. 						For Trailing Bit Complement padding. If the data ends in a zero bit, the
 * 									padding will be full of ones; if the data ends in a one bit, the padding will
 * 									be full of zeros.
 * X9.23Padding. 					A padding mechanism defined in X9.23. The last byte of the padding is the
 * 									number of pad bytes; outside of the last byte, the pad bytes are then either
 * 									made up of zeros or random data.
 * ZeroBytePadding. 				Do not use this one unless you have to deal with a legacy application. It is
 * 									really only suitable for use with printable ASCII data. In this case the padding
 * 									is performed by padding out with one or more bytes of zero value. Obviously, if
 * 									your data might end with bytes of zero value, this padding mechanism will not work
 * 									very well.
 */
/**
 *
 * @author Julio Leite
 */
public enum PaddingEnum {
	NoPadding, PKCS7Padding, ISO10126_2Padding, ISO7816_4Padding, TBCPadding, X9_23Padding, ZeroBytePadding;

	public String getName() {
		String result;
		switch (this) {
		case NoPadding:
			result = "NoPadding";
			break;
		case PKCS7Padding:
			result = "PKCS7Padding";
			break;
		case ISO10126_2Padding:
			result = "ISO10126-2Padding";
			break;
		case ISO7816_4Padding:
			result = "ISO7816-4Padding";
			break;
		case TBCPadding:
			result = "TBCPadding";
			break;
		case X9_23Padding:
			result = "X9.23Padding";
			break;
		case ZeroBytePadding:
			result = "ZeroBytePadding";
			break;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
		return result;
	}
}

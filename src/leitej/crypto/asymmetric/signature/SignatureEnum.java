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

package leitej.crypto.asymmetric.signature;

import leitej.exception.ImplementationLtRtException;

/*
 * DSA, ECDSA, GOST-3410 (GOST-3410-94), ECGOST-3410 (GOST-3410-2001)
 * <digest> WithRSAEncryption, where <digest> is one of MD2, MD4, MD5, SHA1, SHA224, SHA256, SHA384, SHA512, RIPEMD128, RIPEMD160, or RIPEMD256
 * <digest> WithRSAAndMGF1, where <digest> is one of SHA1, SHA224, SHA256, SHA384, or SHA512
 * <digest> WithRSA/ISO9796-2, where <digest> is one of MD5, SHA1, or RIPEMD160
 */
/**
 *
 * @author Julio Leite
 */
public enum SignatureEnum {
	DSA, ECDSA, GOST_3410, ECGOST_3410, MD2WithRSAEncryption, MD4WithRSAEncryption, MD5WithRSAEncryption,
	SHA1WithRSAEncryption, SHA224WithRSAEncryption, SHA256WithRSAEncryption, SHA384WithRSAEncryption,
	SHA512WithRSAEncryption, RIPEMD128WithRSAEncryption, RIPEMD160WithRSAEncryption, RIPEMD256WithRSAEncryption,
	SHA1WithRSAAndMGF1, SHA224WithRSAAndMGF1, SHA256WithRSAAndMGF1, SHA384WithRSAAndMGF1, SHA512WithRSAAndMGF1,
	MD5WithRSA_ISO9796_2, SHA1WithRSA_ISO9796_2, RIPEMD160WithRSA_ISO9796_2;

	public String getName() {
		String result;
		switch (this) {
		case DSA:
			result = "DSA";
			break;
		case ECDSA:
			result = "ECDSA";
			break;
		case GOST_3410:
			result = "GOST-3410";
			break;
		case ECGOST_3410:
			result = "ECGOST-3410";
			break;
		case MD2WithRSAEncryption:
			result = "MD2WithRSAEncryption";
			break;
		case MD4WithRSAEncryption:
			result = "MD4WithRSAEncryption";
			break;
		case MD5WithRSAEncryption:
			result = "MD5WithRSAEncryption";
			break;
		case SHA1WithRSAEncryption:
			result = "SHA1WithRSAEncryption";
			break;
		case SHA224WithRSAEncryption:
			result = "SHA224WithRSAEncryption";
			break;
		case SHA256WithRSAEncryption:
			result = "SHA256WithRSAEncryption";
			break;
		case SHA384WithRSAEncryption:
			result = "SHA384WithRSAEncryption";
			break;
		case SHA512WithRSAEncryption:
			result = "SHA512WithRSAEncryption";
			break;
		case RIPEMD128WithRSAEncryption:
			result = "RIPEMD128WithRSAEncryption";
			break;
		case RIPEMD160WithRSAEncryption:
			result = "RIPEMD160WithRSAEncryption";
			break;
		case RIPEMD256WithRSAEncryption:
			result = "RIPEMD256WithRSAEncryption";
			break;
		case SHA1WithRSAAndMGF1:
			result = "SHA1withRSAandMGF1";
			break;
		case SHA224WithRSAAndMGF1:
			result = "SHA224withRSAandMGF1";
			break;
		case SHA256WithRSAAndMGF1:
			result = "SHA256withRSAandMGF1";
			break;
		case SHA384WithRSAAndMGF1:
			result = "SHA384withRSAandMGF1";
			break;
		case SHA512WithRSAAndMGF1:
			result = "SHA512withRSAandMGF1";
			break;
		case MD5WithRSA_ISO9796_2:
			result = "MD5WithRSA/ISO9796-2";
			break;
		case SHA1WithRSA_ISO9796_2:
			result = "SHA1WithRSA/ISO9796-2";
			break;
		case RIPEMD160WithRSA_ISO9796_2:
			result = "RIPEMD160WithRSA/ISO9796-2";
			break;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
		return result;
	}
}

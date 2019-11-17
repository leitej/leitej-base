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
 * Password-based encryption (PBE).<br/>
 * <br/>
 * PBE mechanisms are based on cryptographic hashing mechanisms. In essence, a
 * password and a salt, which is just random data, is fed in some fashion into a
 * mixing function based around a secure hash and the function is applied the
 * number of times dictated by an iteration count. Once the mixing is complete,
 * the resulting byte stream coming out of the PBE mechanism is used to create
 * the key for the cipher to be used and possibly an initialisation vector as
 * well.<br/>
 * <br/>
 * The most widespread PBE mechanisms are published in PKCS #5 and PKCS #12.
 * There is also a PBE scheme that can be used with S/MIME, which is published
 * in RFC 3211. In the latter case, the key generated from the password is used
 * to generate another key.
 *
 * @author Julio Leite
 */
public enum PBEEnum {
	PBEWithSHAAndTwofish_CBC, PBEWithSHA1And3_KeyTripleDES, PBEWithMD5AndDES, PBEWithSHA1AndDES, PBEWithSHA1AndRC2,
	PBEWithMD5AndRC2,
//	PBEWithSHA1And2_KeyTripleDES,
//	PBEWithSHA1And40BitRC2,
//	PBEWithSHA1And40BitRC4,
//	PBEWithSHA1And128BitRC2,
//	PBEWithSHA1And128BitRC4,
//	PBEWithSHA1AndTwofish
	;

	/**
	 * PBE mechanisms are named using the rule PBEwith<function>And<cipher> where
	 * function is the algorithm (usually an hash) used to support the mechanism
	 * that generates the key and cipher is the underlying cipher used for the
	 * encryption.
	 *
	 * @return
	 */
	public String getName() {
		String result;
		switch (this) {
		case PBEWithSHAAndTwofish_CBC:
			result = "PBEWithSHAAndTwofish-CBC";
			break;
		case PBEWithMD5AndDES:
			result = "PBEWithMD5AndDES";
			break;
		case PBEWithSHA1AndDES:
			result = "PBEWithSHA1AndDES";
			break;
		case PBEWithSHA1AndRC2:
			result = "PBEWithSHA1AndRC2";
			break;
		case PBEWithMD5AndRC2:
			result = "PBEWithMD5AndRC2";
			break;
		case PBEWithSHA1And3_KeyTripleDES:
			result = "PBEWithSHAAnd3KeyTripleDES";
			break;
//		case PBEWithSHA1And2_KeyTripleDES:
//			result = "PBEWithSHA1And2-KeyTripleDES";
//			break;
//		case PBEWithSHA1And40BitRC2:
//			result = "PBEWithSHA1And40BitRC2";
//			break;
//		case PBEWithSHA1And40BitRC4:
//			result = "PBEWithSHA1And40BitRC4";
//			break;
//		case PBEWithSHA1And128BitRC2:
//			result = "PBEWithSHA1And128BitRC2";
//			break;
//		case PBEWithSHA1And128BitRC4:
//			result = "PBEWithSHA1And128BitRC4";
//			break;
//		case PBEWithSHA1AndTwofish:
//			result = "PBEWithSHA1AndTwofish";
//			break;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
		return result;
	}

	/**
	 * The salt is a public value - as in you should assume an attacker can find
	 * it.<br/>
	 * <br/>
	 * The reason for the salt is that by adding a string of random bytes to the
	 * password, the same password can be used as a source for a large number of
	 * different keys. This is useful because it forces attackers to perform the key
	 * generation calculation every time they wish to try a password, for every
	 * piece of encrypted data they wish to attack.<br/>
	 * <br/>
	 * If you can, make the salt at least as large as the block size of the function
	 * used to process the password. Usually the block size of the function is the
	 * same as that of the underlying message digest used by it.
	 *
	 * @return
	 */
	public int goodSaltByteSize() {
		// TODO:
		int result;
		switch (this) {
		case PBEWithSHAAndTwofish_CBC:
			throw new ImplementationLtRtException(this.toString());
		case PBEWithSHA1And3_KeyTripleDES:
			result = 8;
			break;
		case PBEWithMD5AndDES:
			throw new ImplementationLtRtException(this.toString());
		case PBEWithSHA1AndDES:
			throw new ImplementationLtRtException(this.toString());
		case PBEWithSHA1AndRC2:
			throw new ImplementationLtRtException(this.toString());
		case PBEWithMD5AndRC2:
			throw new ImplementationLtRtException(this.toString());
		default:
			throw new ImplementationLtRtException(this.toString());
		}
		return result;
	}

	/**
	 * Iteration count is a public value - as in you should assume an attacker can
	 * find it.<br/>
	 * <br/>
	 * The sole purpose of the iteration count is to increase the computation time
	 * required to convert a password to a key. For example, imagine someone is
	 * trying to launch an attack on data that has been encrypted using PBE by using
	 * a dictionary of common words, phrases, and names-more commonly referred to as
	 * a dictionary attack. If the PBE mechanism has been used with an iteration
	 * count of 1,000 rather than 1, it will require 1,000 times more processing to
	 * calculate a key from a password.
	 *
	 * @author Julio Leite
	 */
	public static enum IterationCountEnum {
		EASY_1024, MEDIUM_2048, HARD_4096;

		public int iterationCount() {
			int result;
			switch (this) {
			case EASY_1024:
				result = 1024;
				break;
			case MEDIUM_2048:
				result = 2048;
				break;
			case HARD_4096:
				result = 4096;
				break;
			default:
				throw new ImplementationLtRtException(this.toString());
			}
			return result;
		}
	}

}

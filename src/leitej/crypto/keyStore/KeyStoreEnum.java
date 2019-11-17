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

package leitej.crypto.keyStore;

/*
 * *** Standard JDK Keystore Types
 * JCEKS	-This is a Sun format type that was introduced with the JCE. In addition to
 * 			being able to contain private keys and certificates, it can also contain
 * 			symmetric keys. It differs from the JKS in that the encryption used to protect
 * 			private keys is based on Triple-DES, which is much stronger than the algorithm
 * 			used in the JKS. Aliases are case-insensitive.
 * JKS		-This is the original Sun format keystore type. It will only contain private
 * 			keys and certificates, and aliases are case-insensitive. There is also a
 * 			variation on it, CaseExactJKS, which recognizes the aliases with the same
 * 			spelling but different case.
 * PKCS12	-Aversion of the format defined in RSA Security's PKCS #12. Up till JDK 1.5
 * 			this type was read-only, but you can now write them as well. Aliases are
 * 			case-insensitive. The store cannot be used to store trusted certificates.
 *
 * *** Bouncy Castle Keystore Types
 * BKS		-This store encrypts keys using Triple-DES but otherwise creates the store
 * 			with the same level of security as the JKS store. Aliases are case-sensitive
 * 			and the store can handle symmetric keys as well as private keys and certificates.
 * UBER		-This store encrypts keys using Triple-DES and then encrypts the store using
 * 			the Twofish algorithm. This offers a higher level of security than the BKS
 * 			format, but it does mean it will not work with some tools, such as the keytool.
 * 			Aliases are case-sensitive and the store can handle all the types the BKS store
 * 			can handle.
 * PKCS12	-Another version of the format defined in PKCS #12. The store is readable and
 * 			writable, with case-sensitive naming. It can also be used to store trusted
 * 			certificates. The type is also aliased, because BCPKCS12 and PKCS-12DEF.
 * 			PKCS-12DEF uses the Sun certificate factory for creating X.509 certificates,
 * 			rather than the Bouncy Castle one. Aliases are case-sensitive, and individual
 * 			key passwords are ignored, but keys and certificates are encrypted using the
 * 			password used to save the store.
 *
 */
/**
 *
 * @author Julio Leite
 */
public enum KeyStoreEnum {
	JCEKS, JKS, PKCS12, BKS, UBER;

	public String getName() {
		String result;
		switch (this) {
		case JCEKS:
			result = "JCEKS";
			break;
		case JKS:
			result = "JKS";
			break;
		case PKCS12:
			result = "PKCS12";
			break;
		case BKS:
			result = "BKS";
			break;
		case UBER:
			result = "UBER";
			break;
		default:
			throw new IllegalStateException(this.toString());
		}
		return result;
	}
}

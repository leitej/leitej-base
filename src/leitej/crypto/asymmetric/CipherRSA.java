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

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import leitej.crypto.Cryptography;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;

/**
 * The maximum amount of data that can be encrypted with RSA or El Gamal is
 * normally limited by the size of the key, less any padding overhead that might
 * exist.
 *
 * @author Julio Leite
 */
public class CipherRSA {

	private transient volatile Cipher cipherEncPbc;
	private transient volatile Cipher cipherDecPvd;

	public CipherRSA(final KeyPair myKeys, final PaddingAsymEnum padding) throws InvalidKeyException {
		this(myKeys.getPublic(), myKeys.getPrivate(), padding);
	}

	public CipherRSA(final PublicKey publicKeyToEnc, final PrivateKey privateKeyToDec, final PaddingAsymEnum padding)
			throws InvalidKeyException {
		if (PaddingAsymEnum.NoPadding.equals(padding)) {
			throw new IllegalArgumentLtRtException();
		}
		try {
			this.cipherEncPbc = Cryptography.getCipher(CipherAsymEnum.RSA, ModeAsymEnum.NONE, padding);
			this.cipherDecPvd = Cryptography.getCipher(CipherAsymEnum.RSA, ModeAsymEnum.NONE, padding);
		} catch (final NoSuchAlgorithmException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchProviderException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchPaddingException e) {
			throw new ImplementationLtRtException(e);
		}
		this.cipherEncPbc.init(Cipher.ENCRYPT_MODE, publicKeyToEnc);
		this.cipherDecPvd.init(Cipher.DECRYPT_MODE, privateKeyToDec);
	}

	public byte[] encript(final byte[] message) {
		try {
			return this.cipherEncPbc.doFinal(message);
		} catch (final IllegalBlockSizeException e) {
			throw new IllegalStateException(e);
		} catch (final BadPaddingException e) {
			throw new IllegalStateException(e);
		}
	}

	public byte[] decript(final byte[] message) {
		try {
			return this.cipherDecPvd.doFinal(message);
		} catch (final IllegalBlockSizeException e) {
			throw new IllegalStateException(e);
		} catch (final BadPaddingException e) {
			throw new IllegalStateException(e);
		}
	}

//	public int getEncryptBitStrength() {
//		return cipherEncPbc.getOutputSize(1)*8;
//	}
//
//	public int getBlockSize() {
//		return cipherDecPvd.getBlockSize();
//	}

}

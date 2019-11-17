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

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;

import leitej.crypto.Cryptography;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.util.BinaryUtil;
import leitej.util.MathUtil;

/**
 * Segmented Integer Counter - Input Rotate - Partial Block Chaining (SICIRPBC)
 *
 * or Counter Input Rotate Chaining (CIRC)
 *
 * @author Julio Leite
 */
public final class CircBlockCipher {

	private final Cipher cipher;
	private final int blockByteSize;

	private final byte[] iv;
	private final byte[] counter;
	private final byte[] irc;
	private final byte[] counterIrc;
	private final byte[] counterIrcOut;

	private final byte[] nBlockBuffer;

	private boolean encryptMode;
	private boolean rotatePositionFlag;
	private int consumerPointer;

	private boolean initialized;

	public CircBlockCipher(final CipherEnum algorithm) {
		try {
			this.cipher = Cryptography.getCipher(algorithm, ModeEnum.ECB, PaddingEnum.NoPadding);
		} catch (final NoSuchAlgorithmException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchProviderException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchPaddingException e) {
			throw new ImplementationLtRtException(e);
		}
		this.blockByteSize = this.cipher.getBlockSize();
		if (this.blockByteSize < 8) {
			throw new ImplementationLtRtException();// devido ao blockSeek()
		}
		this.iv = new byte[this.blockByteSize];
		this.counter = new byte[this.blockByteSize];
		this.irc = new byte[this.blockByteSize];
		this.counterIrc = new byte[this.blockByteSize];
		this.counterIrcOut = new byte[this.blockByteSize];
		this.nBlockBuffer = new byte[BinaryUtil.LONG_BYTE_LENGTH];
		this.initialized = false;
	}

	/**
	 *
	 * @param forEncryption
	 * @param key
	 * @param ivSpec
	 * @throws InvalidKeyException
	 * @throws IllegalArgumentLtRtException if <code>ivSpec</code> is null or has a
	 *                                      size different from the block size
	 */
	public void init(final boolean forEncryption, final Key key, final IvParameterSpec ivSpec)
			throws InvalidKeyException, IllegalArgumentLtRtException {
		if (ivSpec == null || ivSpec.getIV() == null || ivSpec.getIV().length < this.blockByteSize) {
			throw new IllegalArgumentLtRtException();
		}
		this.cipher.init(Cipher.ENCRYPT_MODE, key);
		for (int i = 0; i < this.blockByteSize; i++) {
			this.iv[i] = ivSpec.getIV()[i];
			this.counter[i] = this.iv[i];
			this.irc[i] = 0x00;
		}
		this.encryptMode = forEncryption;
		this.rotatePositionFlag = false;
		this.consumerPointer = this.blockByteSize;
		if (!this.initialized) {
			this.initialized = true;
		}
	}

	/**
	 * if <code>nBlock</code> is even then <code>propagationBlock</code> has to be
	 * plain-text of preceding block, else <code>propagationBlock</code> has to be
	 * cipher-text of preceding block.
	 *
	 * @param nBlock
	 * @param propagationBlock
	 */
	public void blockSeek(final long nBlock, final byte[] propagationBlock) {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		if (propagationBlock == null || propagationBlock.length != this.blockByteSize) {
			throw new IllegalArgumentLtRtException();
		}
		if (nBlock < 0) {
			throw new IndexOutOfBoundsException();
		}
		for (int i = 0; i < this.blockByteSize; i++) {
			this.irc[i] = propagationBlock[i];
		}
		counterSet(nBlock);
		this.rotatePositionFlag = MathUtil.isOdd(nBlock);
		this.consumerPointer = this.blockByteSize;
	}

	private void counterSet(final long nBlock) {
		BinaryUtil.writeLong64bit(this.nBlockBuffer, nBlock);
		int carry = 0;
		int x;
		for (int i = this.blockByteSize - 1; i >= 0; i--) {
			if (i >= this.blockByteSize - this.nBlockBuffer.length) {
				x = (this.iv[i] & 0xff)
						+ (this.nBlockBuffer[i - (this.blockByteSize - this.nBlockBuffer.length)] & 0xff) + carry;
			} else {
				x = (this.iv[i] & 0xff) + carry;
			}
			if (x > 0xff) {
				carry = 1;
			} else {
				carry = 0;
			}
			this.counter[i] = (byte) x;
		}
	}

	public int process(final byte[] src, final int srcOff, final int len, final byte[] dst, final int dstOff) {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		if (src == null) {
			throw new NullPointerException();
		} else if (srcOff < 0 || len < 0 || len > src.length - srcOff) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		if (dst == null) {
			throw new NullPointerException();
		} else if (dstOff < 0 || len > dst.length - dstOff) {
			throw new IndexOutOfBoundsException();
		}
		updateBlock();
		final int result = Math.min(len, this.blockByteSize - this.consumerPointer);
		for (int i = 0; i < result; i++) {
			dst[dstOff + i] = (byte) (this.counterIrcOut[this.consumerPointer + i] ^ src[srcOff + i]);
			// encryptMode == true -> src = plain-text & dst = cipher-text
			// encryptMode == false -> src = cipher-text & dst = plain-text
			if (this.rotatePositionFlag == this.encryptMode) {// rotatePositionFlag && encryptMode ||
																// !rotatePositionFlag && !encryptMode
				this.irc[this.consumerPointer + i] = dst[dstOff + i];
			} else {
				this.irc[this.consumerPointer + i] = src[srcOff + i];
			}
		}
		this.consumerPointer += result;
		return result;
	}

	private void updateBlock() {
		if (this.consumerPointer == this.blockByteSize) {
			for (int i = 0; i < this.blockByteSize; i++) {
				this.counterIrc[i] = (byte) (this.counter[i] ^ this.irc[i]);
			}
			try {
				if (this.cipher.update(this.counterIrc, 0, this.blockByteSize, this.counterIrcOut,
						0) != this.blockByteSize) {
					throw new ImplementationLtRtException();
				}
			} catch (final ShortBufferException e) {
				throw new ImplementationLtRtException(e);
			}
			this.consumerPointer = 0;
			this.rotatePositionFlag = !this.rotatePositionFlag;
			// counter add 1
			int carry = 1;
			int x;
			for (int i = this.counter.length - 1; i >= 0; i--) {
				x = (this.counter[i] & 0xff) + carry;
				if (x > 0xff) {
					carry = 1;
				} else {
					carry = 0;
				}
				this.counter[i] = (byte) x;
			}
		}
	}

	public int getBlockByteSize() {
		return this.blockByteSize;
	}

	public void reset() {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
//		try {
//			if(cipher.doFinal(counterIrcOut, 0) != 0) throw new ImplementationLtRtException();
//		} catch (IllegalBlockSizeException e) {
//			throw new ImplementationLtRtException(e);
//		} catch (ShortBufferException e) {
//			throw new ImplementationLtRtException(e);
//		} catch (BadPaddingException e) {
//			throw new ImplementationLtRtException(e);
//		}
		for (int i = 0; i < this.blockByteSize; i++) {
			this.counter[i] = this.iv[i];
			this.irc[i] = 0x00;
		}
		this.rotatePositionFlag = false;
		this.consumerPointer = this.blockByteSize;
	}

}

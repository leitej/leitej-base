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

package leitej.crypto.asymmetric.signature.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

import leitej.crypto.Cryptography;
import leitej.crypto.asymmetric.signature.SignatureEnum;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
public final class SignatureOutputStream extends OutputStream {

	// [bc0] - byte control for data
	// [bc1] - byte control for signature
	// [d128] - 128 bytes of data
	// [s128] - 128 bytes of sign
	//
	// (([bc0][d128])* [bc1][d128] ([bc1][s128])* [bc0][s128])+
	//
	// the firsts left 7bits of byte control are used to truncate the next 128Byte
	// sequence,
	// only when it change from [bc0] to [bc1] or vice-versa.
	// the first right 1bit of byte control is for defining the next second 128Byte
	// type,
	// data or sign.

	static final int BYTE_CONTROL_STEP = 128;

	private final transient Signature signature;
	private final boolean activatePadding;
	private final SecureRandom secureRandom;
	private final byte[] byteArrayBuffer;
	private int bafPointer;
	private final OutputStream out;
	private final int secureStepLength;
	private byte[] signatureBytes;
	private int stepCount;
	private int readControlDone;

	/**
	 *
	 * @param os
	 * @param secureStepLength   the considered amount of data that can be processed
	 *                           by <code>signatureAlgorithm</code> without lose
	 *                           security
	 * @param privateKey
	 * @param signatureAlgorithm
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeyException
	 */
	public SignatureOutputStream(final OutputStream os, final int secureStepLength, final PrivateKey privateKey,
			final SignatureEnum signatureAlgorithm)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
		this(os, secureStepLength, privateKey, signatureAlgorithm, false);
	}

	/**
	 *
	 * @param os
	 * @param secureStepLength
	 * @param privateKey
	 * @param signatureAlgorithm
	 * @param activatePadding
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeyException
	 */
	public SignatureOutputStream(final OutputStream os, final int secureStepLength, final PrivateKey privateKey,
			final SignatureEnum signatureAlgorithm, final boolean activatePadding)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
		if (os == null || secureStepLength <= BYTE_CONTROL_STEP || privateKey == null) {
			throw new IllegalArgumentLtRtException();
		}
		this.signature = Cryptography.getSignature(signatureAlgorithm);
		this.signature.initSign(privateKey);
		this.activatePadding = activatePadding;
		if (activatePadding) {
			this.secureRandom = new SecureRandom();
		} else {
			this.secureRandom = null;
		}
		this.byteArrayBuffer = new byte[BYTE_CONTROL_STEP];
		this.bafPointer = 0;
		this.out = os;
		this.secureStepLength = secureStepLength;
		this.stepCount = 0;
		this.readControlDone = 0;
	}

	@Override
	public synchronized void write(final int b) throws IOException {
		stepControl();
		this.byteArrayBuffer[this.bafPointer] = (byte) (b & 0xff);
		this.bafPointer++;
		this.stepCount++;
	}

	@Override
	public void write(final byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public synchronized void write(final byte b[], final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		stepControl();
		int offset = off;
		int length = len;
		int bufSpace;
		while (this.bafPointer != 0 || this.stepCount == 0) {
			bufSpace = BYTE_CONTROL_STEP - this.bafPointer;
			if (length <= bufSpace) {
				writeToBuffer(b, offset, length);
				return;
			} else {
				writeToBuffer(b, offset, bufSpace);
				offset += bufSpace;
				length -= bufSpace;
				stepControl();
			}
		}
		int count = 0;
		int secSpace;
		while ((length - count) > BYTE_CONTROL_STEP) {
			// TODO:remove next line (should be never true at this point)
			if (this.bafPointer != 0) {
				throw new ImplementationLtRtException();
			}
			if (this.secureStepLength <= (this.stepCount + BYTE_CONTROL_STEP)) {
				try {
					this.signature.update(b, offset, count);
				} catch (final SignatureException e) {
					throw new IOException(e);
				}
				offset += count;
				length -= count;
				count = 0;
				secSpace = this.secureStepLength - this.stepCount;
				writeToBuffer(b, offset, secSpace);
				offset += secSpace;
				length -= secSpace;
				stepControl();
				// TODO:remove next line (should be never true at this point)
				if (this.bafPointer != 0) {
					throw new ImplementationLtRtException();
				}
				if (length > BYTE_CONTROL_STEP) {
					this.readControlDone = -1;
					stepControl();
				}
			} else {
				this.out.write(b, offset + count, BYTE_CONTROL_STEP);
				count += BYTE_CONTROL_STEP;
				this.stepCount += BYTE_CONTROL_STEP;
				stepControl();
			}
		}
		try {
			this.signature.update(b, offset, count);
		} catch (final SignatureException e) {
			throw new IOException(e);
		}
		writeToBuffer(b, offset + count, length - count);
	}

	private void writeToBuffer(final byte b[], int offset, int length) throws IOException {
		// TODO:remove next line (should be never true at this point)
		if (length > BYTE_CONTROL_STEP - this.bafPointer) {
			throw new ImplementationLtRtException();
		}
		int secSpace;
		if (this.secureStepLength < (this.stepCount + length)) {
			secSpace = this.secureStepLength - this.stepCount;
			System.arraycopy(b, offset, this.byteArrayBuffer, this.bafPointer, secSpace);
			offset += secSpace;
			length -= secSpace;
			this.bafPointer += secSpace;
			this.stepCount += secSpace;
			stepControl();
		}
		System.arraycopy(b, offset, this.byteArrayBuffer, this.bafPointer, length);
		this.bafPointer += length;
		this.stepCount += length;
	}

	private void stepControl() throws IOException {
		if (this.secureStepLength == this.stepCount) {
			flush();
		} else if (this.readControlDone != this.stepCount) {
			if (this.bafPointer == BYTE_CONTROL_STEP) {
				sendByteControlContinueWrite();
				this.out.write(this.byteArrayBuffer, 0, BYTE_CONTROL_STEP);
				try {
					this.signature.update(this.byteArrayBuffer, 0, BYTE_CONTROL_STEP);
				} catch (final SignatureException e) {
					throw new IOException(e);
				}
				this.bafPointer = 0;
				this.readControlDone = this.stepCount;
			} else if (this.bafPointer == 0) {
				sendByteControlContinueWrite();
				this.readControlDone = this.stepCount;
			}
		}
	}

	private void sendByteControlChangeToSign(final int ignoreCount) throws IOException {
		// TODO:remove next line (should be never true at this point)
		if (ignoreCount > 0x7f) {
			throw new ImplementationLtRtException();
		}
		// send periodic byte control with first right bit 1 (does has signature
		// followed)
		this.out.write((ignoreCount << 1) | 0x01);
	}

	private void sendByteControlContinueWrite() throws IOException {
		// send periodic byte control with first right bit 0 (does not has signature
		// followed)
		if (this.activatePadding) {
			this.out.write(this.secureRandom.nextInt() & 0xfe);
		} else {
			this.out.write(0);
		}
	}

	private void nextSecureRandomBytes(final byte[] bytes, final int off, final int len) {
		for (int i = 0; i < len;) {
			for (int rnd = this.secureRandom.nextInt(), n = Math.min(len - i,
					Integer.SIZE / Byte.SIZE); n-- > 0; rnd >>= Byte.SIZE) {
				bytes[off + i] = (byte) rnd;
				i++;
			}
		}
	}

	@Override
	public synchronized void flush() throws IOException {
		if (this.stepCount != 0) {
			// TODO:remove next line (should be never true at this point)
			if (this.bafPointer == 0) {
				throw new ImplementationLtRtException();
			}
			int padSpace = BYTE_CONTROL_STEP - this.bafPointer;
			sendByteControlChangeToSign(padSpace);
			if (this.activatePadding) {
				nextSecureRandomBytes(this.byteArrayBuffer, this.bafPointer, BYTE_CONTROL_STEP - this.bafPointer);
			}
			this.out.write(this.byteArrayBuffer, 0, BYTE_CONTROL_STEP);
			try {
				this.signature.update(this.byteArrayBuffer, 0, this.bafPointer);
			} catch (final SignatureException e) {
				throw new IOException(e);
			}
			this.bafPointer = 0;
			// prepare signature
			try {
				if (this.signatureBytes == null) {
					this.signatureBytes = this.signature.sign();
				} else {
					if (this.signature.sign(this.signatureBytes, 0, this.signatureBytes.length) != this.signatureBytes.length) {
						throw new ImplementationLtRtException();
					}
				}
			} catch (final SignatureException e) {
				throw new IOException(e);
			}
			// send signature
			int count = 0;
			while (this.signatureBytes.length - count > BYTE_CONTROL_STEP) {
				sendByteControlContinueSign();
				this.out.write(this.signatureBytes, count, BYTE_CONTROL_STEP);
				count += BYTE_CONTROL_STEP;
			}
			padSpace = BYTE_CONTROL_STEP - (this.signatureBytes.length - count);
			sendByteControlChangeToData(padSpace);
			if (this.activatePadding) {
				nextSecureRandomBytes(this.byteArrayBuffer, 0, padSpace);
			}
			this.out.write(this.signatureBytes, count, this.signatureBytes.length - count);
			this.out.write(this.byteArrayBuffer, 0, padSpace);
			// effective flush
			this.out.flush();
			// initiate step
			this.stepCount = 0;
			this.readControlDone = 0;
		}
	}

	private void sendByteControlChangeToData(final int ignoreCount) throws IOException {
		// TODO:remove next line (should be never true at this point)
		if (ignoreCount > 0x7f) {
			throw new ImplementationLtRtException();
		}
		// send periodic byte control with first right bit 0 (does has data followed)
		this.out.write(ignoreCount << 1);
	}

	private void sendByteControlContinueSign() throws IOException {
		// send periodic byte control with first right bit 1 (does not has data
		// followed)
		if (this.activatePadding) {
			this.out.write((this.secureRandom.nextInt() | 0x01) & 0xff);
		} else {
			this.out.write(1);
		}
	}

	@Override
	public synchronized void close() throws IOException {
		this.out.close();
	}

}

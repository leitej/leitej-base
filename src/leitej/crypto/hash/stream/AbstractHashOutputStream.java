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

package leitej.crypto.hash.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestException;
import java.security.SecureRandom;

import javax.crypto.ShortBufferException;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
abstract class AbstractHashOutputStream extends OutputStream {

	static final int BYTE_CONTROL_STEP = 128;

	private final SecureRandom secureRandom;
	private final byte[] byteArray;
	private final OutputStream out;
	private final int stepLength;
	private byte[] digestArray;
	private int stepCount;
	private int readControlDone;

	/**
	 *
	 * @param os
	 * @param stepLength auto send verify digest at <code>stepLength</code> length
	 *                   of data, after last digest sent
	 * @throws IllegalArgumentLtRtException if <code>os</code> is null or
	 *                                      <code>stepLength</code> is less then 1
	 */
	protected AbstractHashOutputStream(final OutputStream os, final int stepLength)
			throws IllegalArgumentLtRtException {
		if (os == null || stepLength < 1) {
			throw new IllegalArgumentLtRtException();
		}
		this.secureRandom = new SecureRandom();
		this.byteArray = new byte[BYTE_CONTROL_STEP];
		this.out = os;
		this.stepLength = stepLength;
		this.stepCount = 0;
		this.readControlDone = 0;
	}

	/**
	 *
	 * @return the digest length in bytes
	 */
	protected abstract int digestLength();

	/**
	 * Updates the digest using the specified array of bytes, starting at the
	 * specified offset.
	 *
	 * @param input  the array of bytes
	 * @param offset the offset to start from in the array of bytes
	 * @param len    the number of bytes to use, starting at <code>offset</code>
	 */
	protected abstract void update(byte[] input, int offset, int len);

	/**
	 * Completes the hash computation by performing final operations such as
	 * padding. The digest is reset after this call is made.
	 *
	 * @param buf    output buffer for the computed digest
	 * @param offset into the output buffer to begin storing the digest
	 * @param len    number of bytes within buf allotted for the digest
	 * @throws DigestException      if an error occurs
	 * @throws ShortBufferException
	 */
	protected abstract void digest(byte[] buf, int offset) throws DigestException, ShortBufferException;

	@Override
	public synchronized void write(final int b) throws IOException {
		stepControl();
		this.out.write(b);
		this.byteArray[0] = (byte) (b & 0xff);
		update(this.byteArray, 0, 1);
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
		int count = 0;
		int tmp;
		while ((len - count) > 0) {
			stepControl();
			tmp = Math.min(Math.min(len - count, BYTE_CONTROL_STEP - (this.stepCount & 0x7f)),
					this.stepLength - this.stepCount);
			this.out.write(b, off + count, tmp);
			update(b, off + count, tmp);
			count += tmp;
			this.stepCount += tmp;
		}
	}

	private void stepControl() throws IOException {
		if (this.stepLength == this.stepCount) {
			flush();
		} else if (this.readControlDone != this.stepCount && (this.stepCount & 0x7f) == 0) {
			// send periodic byte control with first bit 0 (does not has digest followed)
			nextSecureRandomBytes(this.byteArray, 0, 1);
			this.byteArray[0] &= 0x7f;
			this.out.write(this.byteArray, 0, 1);
			this.readControlDone = this.stepCount;
		}
	}

	@Override
	public synchronized void flush() throws IOException {
		if (this.stepCount != 0) {
			// prepare digest
			if (this.digestArray == null) {
				this.digestArray = new byte[digestLength()];
			}
			try {
				digest(this.digestArray, 0);
			} catch (final DigestException e) {
				throw new IOException(e);
			} catch (final ShortBufferException e) {
				throw new ImplementationLtRtException(e);
			}
			// prepare padding
			final int paddingSize = (BYTE_CONTROL_STEP - (this.stepCount & 0x7f)) & 0x7f;
			nextSecureRandomBytes(this.byteArray, 0, paddingSize);
			// send periodic byte control with first bit 1 (does has digest followed)
			// and the next bits indicate padding size to be ignored
			this.byteArray[paddingSize] = (byte) (0x80 | (paddingSize & 0x7f));
			this.out.write(this.byteArray, 0, paddingSize + 1);
			// send digest
			this.out.write(this.digestArray, 0, this.digestArray.length);
			// effective flush
			this.out.flush();
			// initiate step
			this.stepCount = 0;
			this.readControlDone = 0;
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
	public synchronized void close() throws IOException {
		this.out.close();
	}

}

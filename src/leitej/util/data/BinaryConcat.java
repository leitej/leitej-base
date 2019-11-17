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

package leitej.util.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An useful class to help in concatenating bits and bytes.
 *
 * @author Julio Leite
 */
public final class BinaryConcat {

	private static final byte ONE_BIT = 0x01;
	private static final byte TWO_BIT = 0x03;
	private static final byte THREE_BIT = 0x07;
	private static final byte FOUR_BIT = 0x0f;
	private static final byte FIVE_BIT = 0x1f;
	private static final byte SIX_BIT = 0x3f;
	private static final byte SEVEN_BIT = 0x7f;
	private static final byte EIGHT_BIT = (byte) 0xff;

	private final XbaOutputStream bOut;
	private byte bBuf = 0x00;
	private int spBB = 0;

	/**
	 * Creates a new instance of BinaryConcat.
	 */
	public BinaryConcat() {
		this.bOut = new XbaOutputStream();
	}

	/**
	 * Creates a new instance of BinaryConcat.
	 *
	 * @param size the initial size
	 * @throws IllegalArgumentException If size is negative
	 */
	public BinaryConcat(final int size) throws IllegalArgumentException {
		this.bOut = new XbaOutputStream(size);
	}

	/**
	 * Add more bits at the sequence.
	 *
	 * @param b   the data
	 * @param off the start offset in the data
	 * @param len the number of bits to write
	 */
	public void add(int b, final int off, final int len) {
		if ((off < 0) || (off > 7) || (len < 0) || ((off + len) > 8) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		if (len == 8 && this.spBB == 0) {
			this.bOut.write(b);
		} else {
			b = b >>> off;
			switch (len) {
			case 1:
				b &= ONE_BIT;
				break;
			case 2:
				b &= TWO_BIT;
				break;
			case 3:
				b &= THREE_BIT;
				break;
			case 4:
				b &= FOUR_BIT;
				break;
			case 5:
				b &= FIVE_BIT;
				break;
			case 6:
				b &= SIX_BIT;
				break;
			case 7:
				b &= SEVEN_BIT;
				break;
			case 8:
				b &= EIGHT_BIT;
				break;
			default:
				throw new IndexOutOfBoundsException();
			}
			final int overAdd = len - (8 - this.spBB);
			if (overAdd <= 0) {
				this.bBuf |= b << (8 - this.spBB) - len;
				this.spBB += len;
			} else {
				this.bBuf |= (b >>> overAdd);
				this.spBB += (len - overAdd);
			}
			if (this.spBB == 8) {
				this.bOut.write(this.bBuf);
				this.bBuf = 0x00;
				this.spBB = 0;
			}
			if (overAdd > 0) {
				add(b, 0, overAdd);
			}
		}
	}

	/**
	 * Add a byte at the sequence.
	 *
	 * @param b the byte
	 */
	public void add(final int b) {
		if (this.spBB == 0) {
			this.bOut.write(b);
		} else {
			add(b, 0, 8);
		}
	}

	/**
	 * Add bytes at the sequence.
	 *
	 * @param b   the data
	 * @param off the start offset in the data
	 * @param len the number of bytes to write
	 */
	public void add(final byte b[], final int off, final int len) {
		if (this.spBB == 0) {
			this.bOut.write(b, off, len);
		} else {
			if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return;
			}
			for (int i = off; i < off + len; i++) {
				add(b[i]);
			}
		}
	}

	/**
	 * Add bytes at the sequence.
	 *
	 * @param b the data
	 */
	public void add(final byte b[]) {
		add(b, 0, b.length);
	}

	/**
	 * Gives the size of the sequence in bits.
	 *
	 * @return bit count
	 */
	public long bitSize() {
		return ((long) this.bOut.size()) * 8 + this.spBB;
	}

	/**
	 * Gives the size of the sequence in bytes without rest.
	 *
	 * @return byte count
	 */
	public int size() {
		return this.bOut.size();
	}

	/**
	 * .<br/>
	 * The return suppresses the last bits if they don't complete a byte.
	 *
	 * @param from
	 * @param b
	 * @return the total number of bytes read into the buffer, or <code>-1</code> if
	 *         there is no more data because the end of the sequence has been
	 *         reached.
	 */
	public int writeTo(final int from, final byte b[]) {
		return writeTo(from, b, 0, b.length);
	}

	public int writeTo(final int from, final byte b[], final int off, final int len) {
		return this.bOut.writeTo(from, b, off, len);
	}

	public int writeTo(final OutputStream os) throws IOException {
		return this.bOut.writeTo(0, os, this.bOut.size());
	}

	public int writeTo(final int from, final OutputStream os, final int len) throws IOException {
		return this.bOut.writeTo(from, os, len);
	}

	/**
	 * Clean the sequence.
	 */
	public void reset() {
		this.bOut.reset();
		this.bBuf = 0x00;
		this.spBB = 0;
	}

	/**
	 * Clean the sequence.<br/>
	 * The return suppresses the last bits if they don't complete a byte.
	 *
	 * @return content of the sequence
	 */
	public byte[] resetSuppress() {
		final byte[] result = this.bOut.toByteArray();
		reset();
		return result;
	}

	/**
	 * Clean the sequence.<br/>
	 * The return adds the last bits as a byte if they don't complete a byte.
	 *
	 * @return content of the sequence
	 */
	public byte[] resetExcess() {
		if (this.spBB > 0) {
			this.bOut.write(this.bBuf);
		}
		final byte[] result = this.bOut.toByteArray();
		reset();
		return result;
	}

	/*
	 * eXtended ByteArrayOutputStream
	 */
	private final class XbaOutputStream extends ByteArrayOutputStream {

		private XbaOutputStream() {
			super();
		}

		private XbaOutputStream(final int size) {
			super(size);
		}

		private int writeTo(final int from, final byte b[], final int off, final int len) {
			if (b == null) {
				throw new NullPointerException();
			} else if (from < 0) {
				throw new IndexOutOfBoundsException();
			} else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return 0;
			}
			if (from >= this.count) {
				return -1;
			}
			final int result = Math.min(this.count - from, len);
			System.arraycopy(this.buf, from, b, off, result);
			return result;
		}

		private int writeTo(final int from, final OutputStream os, final int len) throws IOException {
			if (os == null) {
				throw new NullPointerException();
			} else if (from < 0) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return 0;
			}
			if (from >= this.count) {
				return -1;
			}
			final int result = Math.min(this.count - from, len);
			os.write(this.buf, from, result);
			return result;
		}

	}

}

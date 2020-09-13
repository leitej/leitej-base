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
import java.io.InputStream;
import java.security.DigestException;

import javax.crypto.ShortBufferException;

import leitej.Constant;
import leitej.exception.DataOverflowLtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.TamperproofLtException;

/**
 *
 * @author Julio Leite
 */
abstract class AbstractHashInputStream extends InputStream {

	private static final int SKIP_BUFFER_SIZE = Constant.IO_BUFFER_SIZE;
	private static final int BYTE_CONTROL_STEP = AbstractHashOutputStream.BYTE_CONTROL_STEP;

	private final InputStream in;
	private final int maxStepLength;
	private final byte[] data;
	private int dataPointer;
	private boolean trust;
	private byte[] digestArray;
	private int digestArrayPointer;
	private byte[] digestArrayProcessed;
	private int byteStepControlCount;
	private int readControlDone;
	private final byte[] garbageData;
	private int garbageDataPointer;
	private boolean missControlByte;
	private boolean ended;

	/**
	 *
	 * @param is
	 * @param maxStepLength maximum amount of untrusted data received before digest
	 *                      verify
	 * @throws IllegalArgumentLtRtException if <code>is</code> is null or
	 *                                      <code>maxStepLength</code> is less then
	 *                                      1
	 */
	protected AbstractHashInputStream(final InputStream is, final int maxStepLength)
			throws IllegalArgumentLtRtException {
		if (is == null || maxStepLength < 1) {
			throw new IllegalArgumentLtRtException();
		}
		this.in = is;
		this.maxStepLength = maxStepLength;
		this.data = new byte[this.maxStepLength];
		this.dataPointer = 0;
		this.trust = false;
		this.digestArrayPointer = 0;
		this.byteStepControlCount = 0;
		this.readControlDone = 0;
		this.garbageData = new byte[BYTE_CONTROL_STEP];
		this.garbageDataPointer = 0;
		this.missControlByte = true;
		this.ended = false;
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
	public synchronized int read() throws IOException {
		fillData(true);
		if (this.ended) {
			return -1;
		}
		return this.data[this.dataPointer++];
	}

	@Override
	public int read(final byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public synchronized int read(final byte b[], final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		fillData(false);
		if (this.ended) {
			return -1;
		}
		int result;
		if (this.trust) {
			final int readLen = Math.min(len, this.byteStepControlCount - this.dataPointer);
			System.arraycopy(this.data, this.dataPointer, b, off, readLen);
			this.dataPointer += readLen;
			result = readLen;
		} else {
			result = 0;
		}
		return result;
	}

	private void fillData(final boolean fully) throws IOException {
		if (!this.ended && this.trust && this.dataPointer == this.byteStepControlCount) {
			this.trust = false;
			this.dataPointer = 0;
			this.byteStepControlCount = 0;
			this.readControlDone = 0;
		}
		if (!this.trust) {
			int len;
			int rc = -1;
			readControl(fully);
			if (!this.ended) {
				do {
					if (rc == 0) {
						rc = this.in.read();
						if (rc != -1) {
							this.data[this.byteStepControlCount] = (byte) (rc & 0xff);
							rc = 1;
						}
					} else {
						len = Math.min(this.maxStepLength - this.byteStepControlCount,
								BYTE_CONTROL_STEP - (this.byteStepControlCount & 0x7f));
						rc = this.in.read(this.data, this.byteStepControlCount, len);
					}
					if (rc == -1) {
						internalClose();
					} else {
						this.byteStepControlCount += rc;
						readControl(fully);
					}
				} while (!this.trust && (fully || rc > 0));
			}
		}
	}

	private void readControl(final boolean fully) throws IOException {
		if (this.byteStepControlCount == this.maxStepLength || (this.byteStepControlCount & 0x7f) == 0) {
			if (this.readControlDone != this.byteStepControlCount) {
				// read if need garbage data
				int len = ((BYTE_CONTROL_STEP - (this.byteStepControlCount & 0x7f)) - this.garbageDataPointer) & 0x7f;
				if (len != 0) {
					int rc = -1;
					do {
						if (rc == 0) {
							rc = this.in.read();
							if (rc != -1) {
								this.garbageData[this.garbageDataPointer] = (byte) (rc & 0xff);
								rc = 1;
							}
						} else {
							rc = this.in.read(this.garbageData, this.garbageDataPointer, len);
						}
						if (rc == -1) {
							internalClose();
						} else {
							this.garbageDataPointer += rc;
							len -= rc;
						}
					} while (!this.ended && fully && len != 0);
				}
				// read control byte
				if (!this.ended && len == 0 && this.missControlByte) {
					int rc;
					if (fully) {
						rc = this.in.read();
						if (rc != -1) {
							this.garbageData[0] = (byte) (rc & 0xff);
							rc = 1;
						}
					} else {
						rc = this.in.read(this.garbageData, 0, 1);
					}
					if (rc == -1) {
						internalClose();
					} else if (rc != 0) {
						final boolean readDigest = (this.garbageData[0] & 0x80) == 0x80;
						if (readDigest) {
							int disposeBytes = this.garbageData[0] & 0x7f;
							disposeBytes -= this.garbageDataPointer;
							if (disposeBytes < 0) {
								internalClose();
								throw new IOException(new TamperproofLtException());
							}
							this.byteStepControlCount -= disposeBytes;
							if (this.byteStepControlCount < 0) {
								internalClose();
								throw new IOException(new TamperproofLtException());
							}
							this.missControlByte = false;
						} else {
							if (this.byteStepControlCount == this.maxStepLength) {
								internalClose();
								throw new IOException(new DataOverflowLtException());
							}
							resetReadControl();
						}
					}
				}
				// read digested data
				if (!this.ended && this.readControlDone != this.byteStepControlCount && !this.missControlByte) {
					if (this.digestArray == null) {
						this.digestArray = new byte[digestLength()];
						this.digestArrayProcessed = new byte[digestLength()];
					}
					int rc = -1;
					do {
						if (rc == 0) {
							rc = this.in.read();
							if (rc != -1) {
								this.digestArray[this.digestArrayPointer] = (byte) (rc & 0xff);
								rc = 1;
							}
						} else {
							rc = this.in.read(this.digestArray, this.digestArrayPointer,
									this.digestArray.length - this.digestArrayPointer);
						}
						if (rc == -1) {
							internalClose();
						} else {
							this.digestArrayPointer += rc;
						}
					} while (!this.ended && fully && this.digestArrayPointer != this.digestArray.length);
					if (this.digestArrayPointer == this.digestArray.length) {
						update(this.data, 0, this.byteStepControlCount);
						try {
							digest(this.digestArrayProcessed, 0);
						} catch (final DigestException e) {
							internalClose();
							throw new IOException(e);
						} catch (final ShortBufferException e) {
							internalClose();
							throw new ImplementationLtRtException(e);
						}
						for (int i = 0; i < this.digestArrayProcessed.length; i++) {
							if (this.digestArray[i] != this.digestArrayProcessed[i]) {
								internalClose();
								throw new IOException(new TamperproofLtException());
							}
						}
						resetReadControl();
						this.trust = true;
					}
				}
			}
		}
	}

	private void resetReadControl() {
		this.readControlDone = this.byteStepControlCount;
		this.missControlByte = true;
		this.garbageDataPointer = 0;
		this.digestArrayPointer = 0;
	}

	private void internalClose() {
		this.trust = true;
		this.dataPointer = 0;
		this.byteStepControlCount = 0;
		this.ended = true;
	}

	@Override
	public synchronized long skip(final long n) throws IOException {

		long remaining = n;
		int nr;

		if (n <= 0) {
			return 0;
		}

		final int size = (int) Math.min(SKIP_BUFFER_SIZE, remaining);
		final byte[] skipBuffer = new byte[size];

		while (remaining > 0) {
			nr = read(skipBuffer, 0, (int) Math.min(size, remaining));

			if (nr < 0) {
				break;
			}
			remaining -= nr;
		}

		return n - remaining;
	}

	@Override
	public synchronized int available() throws IOException {
		int result;
		if (this.trust) {
			result = this.byteStepControlCount - this.dataPointer;
		} else {
			result = 0;
		}
		return result;
	}

	@Override
	public synchronized void close() throws IOException {
		this.in.close();
		internalClose();
	}

	@Override
	public synchronized void mark(final int readlimit) {
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}

	@Override
	public boolean markSupported() {
		return false;
	}

}

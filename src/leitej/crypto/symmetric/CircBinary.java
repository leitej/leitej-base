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

import java.io.EOFException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Arrays;
import java.util.ConcurrentModificationException;

import javax.crypto.spec.IvParameterSpec;

import leitej.Constant;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.util.MathUtil;
import leitej.util.data.BinaryConcat;
import leitej.util.stream.FractionInputStream;
import leitej.util.stream.FractionOutputStream;
import leitej.util.stream.RandomAccessBinaryItf;

/**
 *
 * @author Julio Leite
 */
public final class CircBinary implements RandomAccessBinaryItf {

	private static final int ENCRYPTED_IO_BUFFER_SIZE = Constant.IO_BUFFER_SIZE;

	private final RandomAccessBinaryItf encryptedBinary;
	private final CircBlockCipher circDecrypt;
	private final byte[] readSeekBlockBuffer;
	private final byte[] readEncryptedDataBuffer;
	private final byte[] readPlainDataBuffer;
	private final CircBlockCipher circEncrypt;
	private final byte[] writeSeekBlockBuffer;
	private final byte[] writePlainDataBuffer;
	private final byte[] writeEncryptedDataBuffer;
	private final BinaryConcat writeEncryptedDataBC;

	private final int readBlockSize;
	private long readPosition;
	private long readEndPosition;
	private final int writeBlockSize;
	private long writePosition;
	private long writePositionSPoint;

	private volatile boolean initialized;

	public CircBinary(final RandomAccessBinaryItf encryptedBinary, final CipherEnum cipher) {
		this.encryptedBinary = encryptedBinary;
		this.circDecrypt = new CircBlockCipher(cipher);
		this.circEncrypt = new CircBlockCipher(cipher);
		this.readBlockSize = this.circDecrypt.getBlockByteSize();
		this.readSeekBlockBuffer = new byte[this.readBlockSize];
		this.readEncryptedDataBuffer = new byte[this.readBlockSize];
		this.readPlainDataBuffer = new byte[this.readBlockSize];
		this.readPosition = -this.readBlockSize << 1;
		this.readEndPosition = Long.MAX_VALUE;
		this.writeBlockSize = this.circEncrypt.getBlockByteSize();
		this.writeSeekBlockBuffer = new byte[this.writeBlockSize];
		this.writePlainDataBuffer = new byte[this.writeBlockSize];
		this.writeEncryptedDataBuffer = new byte[ENCRYPTED_IO_BUFFER_SIZE];
		this.writeEncryptedDataBC = new BinaryConcat();
		this.writePosition = -this.writeBlockSize << 1;
		this.writePositionSPoint = 0;
		this.initialized = false;
	}

	public int getBlockByteSize() {
		return this.readBlockSize;
	}

	public synchronized void init(final Key key, final IvParameterSpec ivSpec)
			throws InvalidKeyException, IllegalArgumentLtRtException {
		if (this.initialized) {
			throw new IllegalStateLtRtException();
		}
		this.circDecrypt.init(false, key, ivSpec);
		this.circEncrypt.init(true, key, ivSpec);
		this.initialized = true;
	}

	private void fillReadPlainDataBuffer(final long pos) throws IOException {
		if (pos < this.readPosition || pos >= this.readPosition + this.readBlockSize) {
			this.readEndPosition = Long.MAX_VALUE;
			if (pos >= this.readPosition + this.readBlockSize && pos < this.readPosition + (this.readBlockSize << 1)) {
				// immediate next block
				this.readPosition += this.readBlockSize;
				auxFillReadPlainDataBuffer();
			} else {
				// any block
				long nBlock;
				if (pos == 0) {
					nBlock = 0;
				} else {
					nBlock = pos / this.readBlockSize;
				}
				if (MathUtil.isEven(nBlock)) {
					if (nBlock == 0) {
						Arrays.fill(this.readSeekBlockBuffer, (byte) 0x00);
					} else {
						try {
							this.encryptedBinary.readFully((nBlock - 2) * this.readBlockSize, this.readSeekBlockBuffer);
						} catch (final EOFException e) {
							this.readEndPosition = pos;
						}
					}
					if (this.readEndPosition == Long.MAX_VALUE) {
						if (nBlock == 0) {
							this.circDecrypt.blockSeek(0, this.readSeekBlockBuffer);
							this.readPosition = 0;
							auxFillReadPlainDataBuffer();
						} else {
							this.circDecrypt.blockSeek(nBlock - 1, this.readSeekBlockBuffer);
							this.readPosition = (nBlock - 1) * this.readBlockSize;
							auxFillReadPlainDataBuffer();
							this.readPosition += this.readBlockSize;
							auxFillReadPlainDataBuffer();
						}
					}
				} else {
					try {
						this.encryptedBinary.readFully((nBlock - 1) * this.readBlockSize, this.readSeekBlockBuffer);
					} catch (final EOFException e) {
						this.readEndPosition = pos;
					}
					if (this.readEndPosition == Long.MAX_VALUE) {
						this.circDecrypt.blockSeek(nBlock, this.readSeekBlockBuffer);
						this.readPosition = nBlock * this.readBlockSize;
						auxFillReadPlainDataBuffer();
					}
				}
			}
		}
	}

	private void auxFillReadPlainDataBuffer() throws IOException {
		int rc;
		int count = 0;
		while (count < this.readBlockSize && this.readEndPosition == Long.MAX_VALUE) {
			rc = this.encryptedBinary.read(this.readPosition, this.readEncryptedDataBuffer, count,
					this.readBlockSize - count);
			if (rc == -1) {
				this.readEndPosition = this.readPosition + count;
			} else {
				count += rc;
			}
		}
		this.circDecrypt.process(this.readEncryptedDataBuffer, 0, count, this.readPlainDataBuffer, 0);
	}

	@Override
	public synchronized int read(final long offset) throws IllegalArgumentLtRtException, IOException {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		if (offset < 0) {
			throw new IllegalArgumentLtRtException();
		}
		fillReadPlainDataBuffer(offset);
		if (offset >= this.readPosition && offset < this.readPosition + this.readBlockSize
				&& offset < this.readEndPosition) {
			return this.readPlainDataBuffer[(int) (offset - this.readPosition)];
		}
		return -1;
	}

	@Override
	public synchronized int read(final long offset, final byte[] buff)
			throws IllegalArgumentLtRtException, NullPointerException, IOException {
		return read(offset, buff, 0, buff.length);
	}

	@Override
	public synchronized int read(final long offset, final byte[] buff, final int off, final int len)
			throws IllegalArgumentLtRtException, NullPointerException, IOException {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		if (offset < 0) {
			throw new IllegalArgumentLtRtException();
		}
		if (buff == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > buff.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		fillReadPlainDataBuffer(offset);
		if (offset < this.readEndPosition) {
			int result = 0;
			final int startCopy = (int) (offset - this.readPosition);
			int rc = (int) Math.min((long) Math.min(len, this.readBlockSize - startCopy),
					this.readEndPosition - this.readPosition);
			System.arraycopy(this.readPlainDataBuffer, startCopy, buff, off, rc);
			result += rc;
			fillReadPlainDataBuffer(offset + result);
			while (result < len && offset + result < this.readEndPosition) {
				fillReadPlainDataBuffer(offset + result);
				rc = Math.min(this.readBlockSize, len - result);
				System.arraycopy(this.readPlainDataBuffer, 0, buff, off + result, rc);
				result += rc;
			}
			return result;
		}
		return -1;
	}

	@Override
	public synchronized void readFully(final long offset, final byte[] buff)
			throws IllegalArgumentLtRtException, EOFException, IOException {
		readFully(offset, buff, 0, buff.length);
	}

	@Override
	public synchronized void readFully(final long offset, final byte[] buff, final int off, final int len)
			throws IllegalArgumentLtRtException, EOFException, IOException {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		int countRead = 0;
		int tmp;
		while (countRead < len) {
			tmp = read(offset + countRead, buff, off + countRead, len - countRead);
			if (tmp == -1) {
				throw new EOFException();
			} else {
				countRead += tmp;
			}
		}
	}

	private void prepareWriteBuffer(final long pos, final int len) throws IOException {
		if (this.encryptedBinary.length() < pos + len) {
			this.encryptedBinary.setLength(pos + len);
		}
		this.writeEncryptedDataBC.reset();
		if (pos < this.writePosition || pos >= this.writePosition + this.writeBlockSize) {
			if (pos >= this.writePosition && pos < this.writePosition + this.writeBlockSize) {
				// immediate next block
				this.writePositionSPoint = this.writePosition;
			} else {
				// any block
				long nBlock;
				if (pos == 0) {
					nBlock = 0;
				} else {
					nBlock = pos / this.writeBlockSize;
				}
				this.writePositionSPoint = nBlock * this.writeBlockSize;
				this.writePosition = this.writePositionSPoint;
				if (MathUtil.isEven(nBlock)) {
					if (nBlock == 0) {
						Arrays.fill(this.writeSeekBlockBuffer, (byte) 0x00);
					} else {
						try {
							readFully((nBlock - 1) * this.writeBlockSize, this.writeSeekBlockBuffer, 0,
									this.writeBlockSize);
						} catch (final EOFException e) {
							throw new IOException(new ConcurrentModificationException());
						}
					}
				} else {
					try {
						this.encryptedBinary.readFully((nBlock - 1) * this.writeBlockSize, this.writeSeekBlockBuffer, 0,
								this.writeBlockSize);
					} catch (final EOFException e) {
						throw new IOException(new ConcurrentModificationException());
					}
				}
				this.circEncrypt.blockSeek(nBlock, this.writeSeekBlockBuffer);
			}
		}
		fillWritePlainDataBuffer();
	}

	private void fillWritePlainDataBuffer() throws IOException {
		final int length = (int) Math.min(this.writeBlockSize, this.encryptedBinary.length() - this.writePosition);
		if (length < 1) {
			throw new IOException(new ConcurrentModificationException());
		}
		int rc;
		int off = 0;
		while (off < length) {
			rc = read(this.writePosition + off, this.writePlainDataBuffer, off, length - off);
			if (rc == -1) {
				throw new IOException(new ConcurrentModificationException());
			}
			off += rc;
		}
	}

	private void nextWriteBuffer(final int lastLen, final int len) throws IOException {
		final int realProcessLen = Math.max(lastLen,
				(int) Math.min(this.writeBlockSize, this.encryptedBinary.length() - this.writePosition));
		this.circEncrypt.process(this.writePlainDataBuffer, 0, this.writeBlockSize, this.writeEncryptedDataBuffer, 0);
		this.writeEncryptedDataBC.add(this.writeEncryptedDataBuffer, 0, realProcessLen);
		this.writePosition += this.writeBlockSize;
		if ((len < this.writeBlockSize && len > 0)
				|| (len == 0 && (this.encryptedBinary.length() - this.writePosition > 0))) {
			fillWritePlainDataBuffer();
		}
	}

	private void endWriteBuffer() throws IOException {
		final long nextBlock = (this.writePosition + 1) / this.writeBlockSize;
		if (MathUtil.isEven(nextBlock)) {
			nextWriteBuffer(0, 0);
			nextWriteBuffer(0, this.writeBlockSize);
		} else {
			nextWriteBuffer(0, this.writeBlockSize);
		}
		if (this.encryptedBinary.length() - this.writePosition < 0) {
			this.writePosition = -this.writeBlockSize << 1;
		}
		int rc;
		final int len = this.writeEncryptedDataBC.size();
		if (len > 0) {
			this.readPosition = -this.readBlockSize << 1;
		}
		int count = 0;
		while (count < len) {
			rc = this.writeEncryptedDataBC.writeTo(count, this.writeEncryptedDataBuffer, 0, ENCRYPTED_IO_BUFFER_SIZE);
			if (rc == -1) {
				throw new ImplementationLtRtException();
			}
			this.encryptedBinary.write(this.writePositionSPoint + count, this.writeEncryptedDataBuffer, 0, rc);
			count += rc;
		}
	}

	@Override
	public synchronized void write(final long offset, final int b) throws IllegalArgumentLtRtException, IOException {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		if (offset < 0) {
			throw new IllegalArgumentLtRtException();
		}
		prepareWriteBuffer(offset, 1);
		this.writePlainDataBuffer[(int) (this.writePosition - offset)] = (byte) (b & 0xff);
		nextWriteBuffer(1, 0);
		endWriteBuffer();
	}

	@Override
	public synchronized void write(final long offset, final byte[] buff)
			throws IllegalArgumentLtRtException, IOException {
		write(offset, buff, 0, buff.length);
	}

	@Override
	public synchronized void write(final long offset, final byte[] buff, final int off, final int len)
			throws IllegalArgumentLtRtException, IOException {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		if (offset < 0) {
			throw new IllegalArgumentLtRtException();
		}
		if (buff == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > buff.length) || (len < 0) || ((off + len) > buff.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		prepareWriteBuffer(offset, len);
		int length;
		int count = 0;
		final int firstPos = (int) (offset - this.writePosition);
		int lastLength = Math.min(this.writeBlockSize - firstPos, len);
		System.arraycopy(buff, off, this.writePlainDataBuffer, firstPos, lastLength);
		do {
			count += lastLength;
			length = Math.min(this.writeBlockSize, len - count);
			nextWriteBuffer(lastLength, length);
			if (length > 0) {
				System.arraycopy(buff, off + count, this.writePlainDataBuffer, 0, length);
				lastLength = length;
			}
		} while (count < len);
		endWriteBuffer();
	}

	@Override
	public FractionInputStream newInputStream() throws IOException {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		return new CircBinaryFractionInputStream(this, 0, -1);
	}

	@Override
	public FractionInputStream newInputStream(final long offset) throws IllegalArgumentLtRtException, IOException {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		return new CircBinaryFractionInputStream(this, offset, -1);
	}

	@Override
	public FractionInputStream newInputStream(final long offset, final long length)
			throws IllegalArgumentLtRtException, IOException {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		return new CircBinaryFractionInputStream(this, offset, length);
	}

	@Override
	public FractionOutputStream newOutputStream() throws IOException {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		return new CircBinaryFractionOutputStream(this, 0);
	}

	@Override
	public FractionOutputStream newOutputStream(final long offset) throws IllegalArgumentLtRtException, IOException {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		return new CircBinaryFractionOutputStream(this, offset);
	}

	@Override
	public synchronized long length() throws IOException {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		return this.encryptedBinary.length();
	}

	@Override
	public synchronized void setLength(final long length) throws IllegalArgumentLtRtException, IOException {
		if (!this.initialized) {
			throw new IllegalStateLtRtException();
		}
		this.encryptedBinary.setLength(length);
	}

	@Override
	public synchronized void close() throws IOException {
		if (!this.initialized) {
			this.initialized = true;
		}
		this.encryptedBinary.close();
	}

	/*
	 * EXCLUSIVES SUB CLASSES
	 */

	private final class CircBinaryFractionOutputStream extends FractionOutputStream {

		private final CircBinary circBinary;
		private long sPointer;

		private CircBinaryFractionOutputStream(final CircBinary circBinary, final long offset)
				throws IllegalArgumentLtRtException {
			if (offset < 0) {
				throw new IllegalArgumentLtRtException();
			}
			this.circBinary = circBinary;
			this.sPointer = offset;
		}

		@Override
		public void write(final int b) throws IOException {
			synchronized (this.circBinary) {
				this.circBinary.write(this.sPointer++, b);
			}
		}

		@Override
		public void write(final byte b[]) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(final byte b[], final int off, final int len) throws IOException {
			if (b == null) {
				throw new NullPointerException();
			} else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return;
			}
			synchronized (this.circBinary) {
				this.circBinary.write(this.sPointer, b, off, len);
				this.sPointer += len;
			}
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void close() throws IOException {
		}

	}

	private final class CircBinaryFractionInputStream extends FractionInputStream {

		private final CircBinary circBinary;
		private long sPointer;
		private final long lastFractionByte;

		private CircBinaryFractionInputStream(final CircBinary circBinary, final long offset, final long length)
				throws IllegalArgumentLtRtException {
			if (offset < 0) {
				throw new IllegalArgumentLtRtException();
			}
			this.circBinary = circBinary;
			this.sPointer = offset;
			if (length < 0) {
				this.lastFractionByte = -1;
			} else {
				this.lastFractionByte = offset + length;
			}
		}

		@Override
		public int read() throws IOException {
			synchronized (this.circBinary) {
				if (this.lastFractionByte != -1 && this.sPointer >= this.lastFractionByte) {
					return -1;
				}
				final int result = this.circBinary.read(this.sPointer);
				if (result != -1) {
					this.sPointer++;
				}
				return result;
			}
		}

		@Override
		public int readFractionReferenced() throws IOException {
			if (this.lastFractionByte != -1) {
				synchronized (this.circBinary) {
					if (this.sPointer >= this.lastFractionByte) {
						return -1;
					}
				}
				int result;
				do {
					synchronized (this.circBinary) {
						if (this.sPointer < this.lastFractionByte) {
							result = read();
						} else {
							return -1;
						}
					}
					if (result == -1) {
						try {
							Thread.sleep(Constant.FRACTION_INPUT_STREAM_REFRESH_WAIT_IO);
						} catch (final InterruptedException e) {
							new IOException(e);
						}
					}
				} while (result == -1);
				return result;
			}
			return read();
		}

		@Override
		public int read(final byte b[]) throws IOException {
			return read(b, 0, b.length);
		}

		@Override
		public int readFractionReferenced(final byte[] b) throws IOException {
			return readFractionReferenced(b, 0, b.length);
		}

		@Override
		public int read(final byte b[], final int off, final int len) throws IOException {
			if (b == null) {
				throw new NullPointerException();
			} else if (off < 0 || len < 0 || len > b.length - off) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return 0;
			}
			synchronized (this.circBinary) {
				if (this.lastFractionByte != -1 && this.sPointer >= this.lastFractionByte) {
					return -1;
				}
				int length;
				if (this.lastFractionByte == -1) {
					length = len;
				} else {
					length = Math.min(len, Long.valueOf(Math.max(0, this.lastFractionByte - this.sPointer)).intValue());
				}
				final int result = this.circBinary.read(this.sPointer, b, off, length);
				if (result != -1) {
					this.sPointer += result;
				}
				return result;
			}
		}

		@Override
		public int readFractionReferenced(final byte[] b, final int off, final int len) throws IOException {
			synchronized (this.circBinary) {
				final int result = read(b, off, len);
				if (result == -1 && this.lastFractionByte != -1 && this.sPointer < this.lastFractionByte) {
					return 0;
				}
				return result;
			}
		}

		@Override
		public long skip(final long n) throws IOException {
			synchronized (this.circBinary) {
				if (n <= 0) {
					return 0;
				}
				long result;
				if (this.sPointer + n > this.lastFractionByte) {
					result = this.lastFractionByte - this.sPointer;
					this.sPointer = this.lastFractionByte;
				} else {
					result = n;
					this.sPointer += n;
				}
				return result;
			}
		}

		@Override
		public int available() throws IOException {
			synchronized (this.circBinary) {
				int result;
				if (this.lastFractionByte == -1) {
					result = Long.valueOf(this.circBinary.length() - this.sPointer).intValue();
				} else {
					result = Long.valueOf(Math.min(this.lastFractionByte, this.circBinary.length()) - this.sPointer)
							.intValue();
				}
				return ((result < 0) ? 0 : result);
			}
		}

		@Override
		public long length() throws IOException {
			return this.circBinary.length();
		}

		@Override
		public void close() throws IOException {
			synchronized (this.circBinary) {
			}
		}

		@Override
		public void mark(final int readlimit) {
			synchronized (this.circBinary) {
			}
		}

		@Override
		public void reset() throws IOException {
			synchronized (this.circBinary) {
				throw new IOException("mark/reset not supported");
			}
		}

		@Override
		public boolean markSupported() {
			return false;
		}

	}

}

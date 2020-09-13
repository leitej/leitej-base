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

package leitej.util.stream;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import leitej.Constant;

/**
 *
 *
 * @author Julio Leite
 */
public final class BinaryFileFractionInputStream extends FractionInputStream {

	private final Object mutex;
	private final RandomAccessFile raFile;
	private final long endFilePointer;

	public BinaryFileFractionInputStream(final File file, final long offset) throws IOException {
		this(file, offset, -1, null);
	}

	public BinaryFileFractionInputStream(final File file, final long offset, final long length) throws IOException {
		this(file, offset, length, null);
	}

	BinaryFileFractionInputStream(final File file, long offset, final long length, final Object mutex)
			throws IOException {
		if (offset < 0) {
			offset = 0;
		}
		this.raFile = new RandomAccessFile(file, RandomAccessModeEnum.R.getRandomAccessFileMode());
		try {
			this.raFile.seek(offset);
		} catch (final IOException e) {
			this.raFile.close();
			throw e;
		}
		if (length < 0) {
			this.endFilePointer = -1;
		} else {
			this.endFilePointer = offset + length;
		}
		if (mutex == null) {
			this.mutex = this;
		} else {
			this.mutex = mutex;
		}
	}

	@Override
	public int read() throws IOException {
		synchronized (this.mutex) {
			if (this.endFilePointer != -1 && this.raFile.getFilePointer() >= this.endFilePointer) {
				return -1;
			}
			return this.raFile.read();
		}
	}

	@Override
	public int readFractionReferenced() throws IOException {
		if (this.endFilePointer != -1) {
			synchronized (this.mutex) {
				if (this.raFile.getFilePointer() >= this.endFilePointer) {
					return -1;
				}
			}
			int result;
			do {
				synchronized (this.mutex) {
					if (this.raFile.getFilePointer() < this.endFilePointer) {
						result = this.raFile.read();
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
		return this.raFile.read();
	}

	@Override
	public int read(final byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int readFractionReferenced(final byte b[]) throws IOException {
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
		synchronized (this.mutex) {
			if (this.endFilePointer != -1 && this.raFile.getFilePointer() >= this.endFilePointer) {
				return -1;
			}
			int length;
			if (this.endFilePointer == -1) {
				length = len;
			} else {
				length = Math.min(len,
						Long.valueOf(Math.max(0, this.endFilePointer - this.raFile.getFilePointer())).intValue());
			}
			return this.raFile.read(b, off, length);
		}
	}

	@Override
	public int readFractionReferenced(final byte b[], final int off, final int len) throws IOException {
		synchronized (this.mutex) {
			final int result = read(b, off, len);
			if (result == -1 && this.endFilePointer != -1 && this.raFile.getFilePointer() < this.endFilePointer) {
				return 0;
			}
			return result;
		}
	}

	@Override
	public long skip(final long n) throws IOException {
		synchronized (this.mutex) {
			if (n <= 0) {
				return 0;
			}
			long result;
			if (this.raFile.getFilePointer() + n > this.endFilePointer) {
				result = this.endFilePointer - this.raFile.getFilePointer();
				this.raFile.seek(this.endFilePointer);
			} else {
				result = n;
				this.raFile.seek(this.raFile.getFilePointer() + n);
			}
			return result;
		}
	}

	@Override
	public int available() throws IOException {
		synchronized (this.mutex) {
			int result;
			if (this.endFilePointer == -1) {
				result = Long.valueOf(this.raFile.length() - this.raFile.getFilePointer()).intValue();
			} else {
				result = Long
						.valueOf(Math.min(this.endFilePointer, this.raFile.length()) - this.raFile.getFilePointer())
						.intValue();
			}
			return ((result < 0) ? 0 : result);
		}
	}

	@Override
	public long length() throws IOException {
		synchronized (this.mutex) {
			return this.raFile.length();
		}
	}

	@Override
	public void close() throws IOException {
		synchronized (this.mutex) {
			this.raFile.close();
		}
	}

	@Override
	public void mark(final int readlimit) {
		synchronized (this.mutex) {
		}
	}

	@Override
	public void reset() throws IOException {
		synchronized (this.mutex) {
			throw new IOException("mark/reset not supported");
		}
	}

	@Override
	public boolean markSupported() {
		return false;
	}
}

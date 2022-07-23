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

/**
 *
 *
 * @author Julio Leite
 */
public final class BinaryFileFractionOutputStream extends FractionOutputStream {

	private final Object mutex;
	private final RandomAccessFile raFile;
	private final long endFilePointer;

	public BinaryFileFractionOutputStream(final File file, final long offset, final boolean syncWrite)
			throws IOException {
		this(file, -1, offset, -1, syncWrite, null);
	}

	public BinaryFileFractionOutputStream(final File file, final long offset, final long length, final boolean syncWrite)
			throws IOException {
		this(file, -1, offset, length, syncWrite, null);
	}

	public BinaryFileFractionOutputStream(final File file, final long filesize, final long offset, final long length,
			final boolean syncWrite) throws IOException {
		this(file, filesize, offset, length, syncWrite, null);
	}

	BinaryFileFractionOutputStream(final File file, final long filesize, long offset, final long length,
			final boolean syncWrite, final Object mutex) throws IOException {
		if (offset < 0) {
			offset = 0;
		}
		this.raFile = new RandomAccessFile(file, ((syncWrite) ? RandomAccessModeEnum.RWS.getRandomAccessFileMode()
				: RandomAccessModeEnum.RW.getRandomAccessFileMode()));
		try {
			if (filesize >= 0) {
				this.raFile.setLength(filesize);
			}
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
	public void write(final int b) throws IOException {
		synchronized (this.mutex) {
			if (this.endFilePointer != -1 && this.raFile.getFilePointer() >= this.endFilePointer) {
				throw new IndexOutOfBoundsException();
			}
			this.raFile.write(b);
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
		synchronized (this.mutex) {
			if (this.endFilePointer != -1 && this.raFile.getFilePointer() + len > this.endFilePointer) {
				throw new IndexOutOfBoundsException();
			}
			this.raFile.write(b, off, len);
		}
	}

	/**
	 * NOOP<br/>
	 * <br/>
	 * It's defined from constructor if is to be written synchronously to the
	 * underlying storage device.<br/>
	 */
	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
		synchronized (this.mutex) {
			this.raFile.close();
		}
	}
}

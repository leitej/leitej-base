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

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import leitej.Constant;
import leitej.exception.ClosedLtRtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.log.Logger;
import leitej.util.DateUtil;
import leitej.util.stream.BinaryFile;
import leitej.util.stream.FileUtil;
import leitej.util.stream.FractionInputStream;
import leitej.util.stream.FractionOutputStream;
import leitej.util.stream.RandomAccessBinary;
import leitej.util.stream.RandomAccessModeEnum;
import leitej.util.stream.StreamUtil;

/**
 *
 * @author Julio Leite
 */
public class BigBinary implements RandomAccessBinary, Closeable {

	private static final Logger LOG = Logger.getInstance();

	public static final File BIG_BINARY_TEMPORARY_DIRECTORY;

	static {
		BIG_BINARY_TEMPORARY_DIRECTORY = new File(Constant.DEFAULT_DATA_FILE_DIR, "bigBinary");
		BIG_BINARY_TEMPORARY_DIRECTORY.mkdirs();
		clean(BIG_BINARY_TEMPORARY_DIRECTORY);
	}

	/**
	 * Deletes,from file system, all bigBinaries on the <code>directory</code>, by
	 * file name pattern.
	 *
	 * @param directory to delete all BigBinaries
	 * @return true only if all BigBinaries were deleted
	 * @throws NullPointerException if the <code>baseDirectory</code> argument is
	 *                              <code>null</code> or does not denote a directory
	 */
	public static final boolean clean(final File directory) throws NullPointerException {
		boolean result = true;
		final Pattern patternSubDirectory = Pattern.compile("^\\d{4}+$");
		final Pattern patternFilename = Pattern.compile("^\\d*+\\.\\d{3}+$");
		for (final File subDir : directory.listFiles()) {
			if (patternSubDirectory.matcher(subDir.getName()).matches() && subDir.isDirectory()) {
				for (final File file : subDir.listFiles()) {
					if (patternFilename.matcher(file.getName()).matches() && file.isFile()) {
						result &= file.delete();
					}
				}
			}
		}
		return result;
	}

	private final boolean isTemporary;
	private final long id;
	private final File file;
	private BinaryFile binaryFile;

	/**
	 * Creates a temporary big binary object.
	 *
	 */
	public BigBinary() {
		this.isTemporary = true;
		this.id = DateUtil.generateUniqueNumberPerJVM();
		this.file = FileUtil.generateFileFrom(BIG_BINARY_TEMPORARY_DIRECTORY, this.id);
		this.binaryFile = null;
	}

	/**
	 * Creates a big binary, to be let on file system, between multiple JVM
	 * execution process.
	 *
	 * @param directory location to the big binary file
	 * @param id        representing this BigBinary
	 */
	public BigBinary(final File directory, final long id) {
		if (BIG_BINARY_TEMPORARY_DIRECTORY.equals(directory)) {
			throw new IllegalArgumentLtRtException();
		}
		this.isTemporary = false;
		this.id = id;
		this.file = FileUtil.generateFileFrom(directory, this.id);
		this.binaryFile = null;
	}

	public synchronized void open() throws IOException {
		if (this.binaryFile == null) {
			FileUtil.createPathForFile(this.file);
			this.file.createNewFile();
			this.binaryFile = new BinaryFile(this.file, RandomAccessModeEnum.RW);
		}
	}

	public long getId() {
		return this.id;
	}

	@Override
	public int read(final long offset) throws IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.read(offset);
	}

	@Override
	public int read(final long offset, final byte[] buff) throws NullPointerException, IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.read(offset, buff, 0, buff.length);
	}

	@Override
	public void readFully(final long offset, final byte[] buff) throws EOFException, IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		this.binaryFile.readFully(offset, buff);
	}

	@Override
	public int read(final long offset, final byte[] buff, final int off, final int len)
			throws IndexOutOfBoundsException, NullPointerException, IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.read(offset, buff, off, len);
	}

	@Override
	public void readFully(final long offset, final byte[] buff, final int off, final int len)
			throws EOFException, IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		this.binaryFile.readFully(offset, buff, off, len);
	}

	@Override
	public void write(final long offset, final int b) throws IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		this.binaryFile.write(offset, b);
	}

	@Override
	public void write(final long offset, final byte[] buff) throws IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		this.binaryFile.write(offset, buff, 0, buff.length);
	}

	@Override
	public void write(final long offset, final byte[] buff, final int off, final int len) throws IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		this.binaryFile.write(offset, buff, off, len);
	}

	@Override
	public long length() throws IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.length();
	}

	@Override
	public void setLength(final long length) throws IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		this.binaryFile.setLength(length);
	}

	public byte[] md5() throws NoSuchAlgorithmException, IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		synchronized (this.binaryFile) {
			final byte[] result;
			FractionInputStream fis = null;
			try {
				fis = newInputStream();
				result = StreamUtil.md5(fis);
			} finally {
				if (fis != null) {
					fis.close();
				}
			}
			return result;
		}
	}

	@Override
	public FractionInputStream newInputStream() throws IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.newInputStream();
	}

	@Override
	public FractionInputStream newInputStream(final long offset) throws IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.newInputStream(offset);
	}

	@Override
	public FractionInputStream newInputStream(final long offset, final long length) throws IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.newInputStream(offset, length);
	}

	@Override
	public FractionOutputStream newOutputStream() throws IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.newOutputStream();
	}

	@Override
	public FractionOutputStream newOutputStream(final long offset) throws IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.newOutputStream(offset);
	}

	public long append(final InputStream in) throws IOException {
		if (this.binaryFile == null) {
			throw new IOException(new ClosedLtRtException());
		}
		synchronized (this.binaryFile) {
			long result = 0;
			final byte[] buffer = new byte[Constant.IO_BUFFER_SIZE];
			long offset = length();
			LOG.debug("#0", offset);
			int numRead;
			while ((numRead = in.read(buffer)) >= 0) {
				write(offset, buffer, 0, numRead);
				result += numRead;
				offset += numRead;
			}
			LOG.debug("#0", result);
			return result;
		}
	}

	public synchronized boolean delete() throws IOException {
		if (!this.file.exists()) {
			return true;
		}
		close();
		return this.file.delete();
	}

	@Override
	public synchronized void close() throws IOException {
		if (this.binaryFile != null) {
			this.binaryFile.close();
			this.binaryFile = null;
		}
	}

	@Override
	protected final void finalize() throws Throwable {
		if (this.isTemporary) {
			delete();
		} else {
			close();
		}
		super.finalize();
	}

}

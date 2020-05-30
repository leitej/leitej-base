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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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
public final class BigBinary implements RandomAccessBinary, Serializable {

	private static final long serialVersionUID = 4282770784264419884L;

	private static final Logger LOG = Logger.getInstance();

	private static final CacheWeak<String, BigBinary> INSTANCE_MAP = new CacheWeak<>();

	public static final BigBinary valueOf(final String baseDirectory, final long id) throws IOException {
		if (Constant.BIG_BINARY_TEMPORARY_DIRECTORY.equals(baseDirectory)) {
			throw new IllegalArgumentLtRtException();
		}
		final String absolutePath = generateAbsolutePath(baseDirectory, id);
		BigBinary result;
		synchronized (INSTANCE_MAP) {
			result = INSTANCE_MAP.get(absolutePath);
			if (result == null || result.released) {
				result = new BigBinary(absolutePath, id);
				INSTANCE_MAP.set(absolutePath, result);
			}
		}
		return result;
	}

	/*
	 * 2..2'333'1111
	 *
	 * 1111 - directory 2..2 - file name 333 - file extension
	 */
	private static final String generateAbsolutePath(final String baseDirectory, final long id)
			throws IllegalArgumentLtRtException {
		if (id < 1000000) {
			throw new IllegalArgumentLtRtException();
		}
		final String tmp = String.valueOf(id);
		return (new File(baseDirectory, // base directory
				tmp.substring(tmp.length() - 4, tmp.length()) + // directory
						FileUtil.FILE_SEPARATOR + //
						tmp.substring(0, tmp.length() - 7) + "." + // name
						tmp.substring(tmp.length() - 7, tmp.length() - 4))).getAbsolutePath(); // extension
	}

	/**
	 *
	 * @param baseDirectory to delete all BigBinaries
	 * @return true only if all BigBinaries were deleted
	 * @throws NullPointerException if the <code>baseDirectory</code> argument is
	 *                              <code>null</code> or does not denote a directory
	 */
	public static final boolean deleteAll(final String baseDirectory) throws NullPointerException {
		boolean result = true;
		final File directory = new File(baseDirectory);
		final Pattern patternSubDirectory = Pattern.compile("^\\d{4}+$");
		final Pattern patternFilename = Pattern.compile("^\\d*+\\.\\d{3}+$");
		BigBinary bigBinary;
		synchronized (INSTANCE_MAP) {
			for (final File subDir : directory.listFiles()) {
				if (patternSubDirectory.matcher(subDir.getName()).matches() && subDir.isDirectory()) {
					for (final File file : subDir.listFiles()) {
						if (patternFilename.matcher(file.getName()).matches()) {
							bigBinary = INSTANCE_MAP.get(file.getAbsolutePath());
							try {
								if (bigBinary != null && !bigBinary.released) {
									bigBinary.internalRelease();
								}
								if (!file.delete()) {
									LOG.error("Fail: #0", file.getAbsolutePath());
									result = false;
								}
							} catch (final IOException e) {
								LOG.error("#0", e);
								result = false;
							}
						}
					}
				}
			}
		}
		return result;
	}

	public static final boolean delete(final String baseDirectory, final long id) {
		if (Constant.BIG_BINARY_TEMPORARY_DIRECTORY.equals(baseDirectory)) {
			throw new IllegalArgumentLtRtException();
		}
		final String absolutePath = generateAbsolutePath(baseDirectory, id);
		return delete(absolutePath);
	}

	private static final boolean delete(final String absolutePath) {
		LOG.debug("#0", absolutePath);
		synchronized (INSTANCE_MAP) {
			final BigBinary bigBinary = INSTANCE_MAP.get(absolutePath);
			try {
				if (bigBinary != null && !bigBinary.released) {
					bigBinary.internalRelease();
				}
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
		}
		return (new File(absolutePath)).delete();
	}

	static {
		(new File(Constant.BIG_BINARY_TEMPORARY_DIRECTORY)).mkdirs();
		deleteAll(Constant.BIG_BINARY_TEMPORARY_DIRECTORY);
	}

	private final boolean temporary;
	private final long id;
	private final String absolutePath;
	private final BinaryFile binaryFile;
	private volatile boolean released;

	public BigBinary() throws IOException {
		this.temporary = true;
		this.id = DateUtil.generateUniqueNumberPerJVM();
		this.absolutePath = generateAbsolutePath(Constant.BIG_BINARY_TEMPORARY_DIRECTORY, this.id);
		FileUtil.createFile(this.absolutePath);
		this.binaryFile = new BinaryFile(this.absolutePath, RandomAccessModeEnum.RW);
		this.released = false;
	}

	private BigBinary(final String absolutePath, final long id) throws IOException {
		this.temporary = false;
		this.id = id;
		this.absolutePath = absolutePath;
		FileUtil.createFile(this.absolutePath);
		this.binaryFile = new BinaryFile(this.absolutePath, RandomAccessModeEnum.RW);
		this.released = false;
	}

	public long getId() {
		return this.id;
	}

	@Override
	public int read(final long offset) throws IOException {
		return this.binaryFile.read(offset);
	}

	@Override
	public int read(final long offset, final byte[] buff) throws NullPointerException, IOException {
		return this.binaryFile.read(offset, buff, 0, buff.length);
	}

	@Override
	public void readFully(final long offset, final byte[] buff) throws EOFException, IOException {
		this.binaryFile.readFully(offset, buff);
	}

	@Override
	public int read(final long offset, final byte[] buff, final int off, final int len)
			throws IndexOutOfBoundsException, NullPointerException, IOException {
		return this.binaryFile.read(offset, buff, off, len);
	}

	@Override
	public void readFully(final long offset, final byte[] buff, final int off, final int len)
			throws EOFException, IOException {
		this.binaryFile.readFully(offset, buff, off, len);
	}

	@Override
	public void write(final long offset, final int b) throws IOException {
		this.binaryFile.write(offset, b);
	}

	@Override
	public void write(final long offset, final byte[] buff) throws IOException {
		this.binaryFile.write(offset, buff, 0, buff.length);
	}

	@Override
	public void write(final long offset, final byte[] buff, final int off, final int len) throws IOException {
		this.binaryFile.write(offset, buff, off, len);
	}

	@Override
	public long length() throws IOException {
		return this.binaryFile.length();
	}

	@Override
	public void setLength(final long length) throws IOException {
		this.binaryFile.setLength(length);
	}

	public byte[] md5() throws NoSuchAlgorithmException, IOException {
		synchronized (this.binaryFile) {
			final FractionInputStream fis = newInputStream();
			final byte[] result = StreamUtil.md5(fis);
			fis.close();
			return result;
		}
	}

	@Override
	public FractionInputStream newInputStream() throws IOException {
		if (this.released) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.newInputStream();
	}

	@Override
	public FractionInputStream newInputStream(final long offset) throws IOException {
		if (this.released) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.newInputStream(offset);
	}

	@Override
	public FractionInputStream newInputStream(final long offset, final long length) throws IOException {
		if (this.released) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.newInputStream(offset, length);
	}

	@Override
	public FractionOutputStream newOutputStream() throws IOException {
		if (this.released) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.newOutputStream();
	}

	@Override
	public FractionOutputStream newOutputStream(final long offset) throws IOException {
		if (this.released) {
			throw new IOException(new ClosedLtRtException());
		}
		return this.binaryFile.newOutputStream(offset);
	}

	public long append(final InputStream in) throws IOException {
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

	private void internalRelease() throws IOException {
		this.binaryFile.close();
		this.released = true;
	}

	public boolean delete() {
		synchronized (this.binaryFile) {
			if (!this.released) {
				try {
					internalRelease();
				} catch (final IOException e) {
					LOG.error("#0", e);
					return false;
				}
			}
			return delete(this.absolutePath);
		}
	}

	public boolean saveIn(final String baseDirectory) throws IOException {
		if (Constant.BIG_BINARY_TEMPORARY_DIRECTORY.equals(baseDirectory)) {
			throw new IllegalArgumentLtRtException();
		}
		synchronized (this.binaryFile) {
			return moveTo(baseDirectory);
		}
	}

	public boolean moveToTemporary() throws IOException {
		synchronized (this.binaryFile) {
			if (this.temporary) {
				return false;
			}
			return moveTo(Constant.BIG_BINARY_TEMPORARY_DIRECTORY);
		}
	}

	private boolean moveTo(final String baseDirectory) throws IOException {
		synchronized (this.binaryFile) {
			if (!this.released) {
				internalRelease();
			}
			return FileUtil.rename(this.absolutePath, generateAbsolutePath(baseDirectory, this.id));
		}
	}

	@Override
	public void close() throws IOException {
		synchronized (this.binaryFile) {
			internalRelease();
			if (this.temporary) {
				delete(this.absolutePath);
			}
		}
	}

	@Override
	protected final void finalize() throws Throwable {
		close();
		super.finalize();
	}

}

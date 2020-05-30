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

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * An useful class to help read or write a file stream piece by piece.
 *
 * @author Julio Leite
 */
public final class BinaryFile implements RandomAccessBinary {

	private final String filename;
	private final RandomAccessFile raFile;
	private final boolean isSynchronously;

	/**
	 * Creates a new instance of FilePieces.
	 *
	 * @param filename the system-dependent filename
	 * @param mode     the access mode
	 * @throws FileNotFoundException if the mode is <tt>"read"</tt> but the given
	 *                               string does not denote an existing regular
	 *                               file, or if the mode is
	 *                               <tt>"read and write"</tt> but the given string
	 *                               does not denote an existing, writable regular
	 *                               file and a new regular file of that name cannot
	 *                               be created, or if some other error occurs while
	 *                               opening or creating the file
	 * @throws SecurityException     if a security manager exists and its
	 *                               <code>checkRead</code> method denies read
	 *                               access to the file or the mode is "rw" and the
	 *                               security manager's <code>checkWrite</code>
	 *                               method denies write access to the file
	 */
	public BinaryFile(final String filename, final RandomAccessModeEnum mode)
			throws FileNotFoundException, SecurityException {
		this.filename = filename;
		this.isSynchronously = mode.isSynchronously();
		this.raFile = new RandomAccessFile(filename, mode.getRandomAccessFileMode());
	}

	@Override
	public synchronized int read(final long fileOff) throws IOException {
		this.raFile.seek(fileOff);
		return this.raFile.read();
	}

	@Override
	public synchronized int read(final long fileOff, final byte[] buff) throws NullPointerException, IOException {
		return read(fileOff, buff, 0, buff.length);
	}

	@Override
	public synchronized int read(final long fileOff, final byte[] buff, final int off, final int len)
			throws IndexOutOfBoundsException, NullPointerException, IOException {
		this.raFile.seek(fileOff);
		return this.raFile.read(buff, off, len);
	}

	@Override
	public synchronized void readFully(final long fileOff, final byte[] buff) throws EOFException, IOException {
		readFully(fileOff, buff, 0, buff.length);
	}

	@Override
	public synchronized void readFully(final long fileOff, final byte[] buff, final int off, final int len)
			throws EOFException, IOException {
		this.raFile.seek(fileOff);
		this.raFile.readFully(buff, off, len);
	}

	@Override
	public synchronized void write(final long fileOff, final int b) throws IOException {
		this.raFile.seek(fileOff);
		this.raFile.write(b);
	}

	@Override
	public synchronized void write(final long fileOff, final byte[] buff) throws IOException {
		write(fileOff, buff, 0, buff.length);
	}

	@Override
	public synchronized void write(final long fileOff, final byte[] buff, final int off, final int len)
			throws IOException {
		this.raFile.seek(fileOff);
		this.raFile.write(buff, off, len);
	}

	@Override
	public FractionInputStream newInputStream() throws IOException {
		return new BinaryFileFractionInputStream(this.filename, 0, -1, this);
	}

	@Override
	public FractionInputStream newInputStream(final long offset) throws IOException {
		return new BinaryFileFractionInputStream(this.filename, offset, -1, this);
	}

	@Override
	public FractionInputStream newInputStream(final long offset, final long length) throws IOException {
		return new BinaryFileFractionInputStream(this.filename, offset, length, this);
	}

	@Override
	public FractionOutputStream newOutputStream() throws IOException {
		return new BinaryFileFractionOutputStream(this.filename, -1, 0, -1, this.isSynchronously, this);
	}

	@Override
	public FractionOutputStream newOutputStream(final long offset) throws IOException {
		return new BinaryFileFractionOutputStream(this.filename, -1, offset, -1, this.isSynchronously, this);
	}

	@Override
	public synchronized long length() throws IOException {
		return this.raFile.length();
	}

	@Override
	public synchronized void setLength(final long length) throws IOException {
		this.raFile.setLength(length);
	}

	@Override
	public synchronized void close() throws IOException {
		this.raFile.close();
	}

}

/*******************************************************************************
 * Copyright (C) 2011 Julio Leite
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

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import leitej.exception.IllegalArgumentLtRtException;

/**
 *
 * @author Julio Leite
 */
public interface RandomAccessBinaryItf extends Closeable {

	public int read(long offset) throws IllegalArgumentLtRtException, IOException;

	/**
	 * Reads up to <code>buff.length</code> bytes of data from this file into an
	 * array of bytes. This method blocks until at least one byte of input is
	 * available.
	 * <p>
	 * Although <code>FilePieces</code> is not a subclass of
	 * <code>InputStream</code>, this method behaves in exactly the same way as the
	 * {@link InputStream#read(byte[], int, int)} method of
	 * <code>InputStream</code>.
	 *
	 * @param offset the first byte of the file to be read
	 * @param buff   the buffer into which the data is read
	 * @return the maximum number of bytes read
	 * @throws NullPointerException If <code>buff</code> is <code>null</code>
	 * @throws IOException          If the first byte cannot be read for any reason
	 *                              other than end of file, or if the random access
	 *                              file has been closed, or if some other I/O error
	 *                              occurs
	 */
	public int read(long offset, byte[] buff) throws IllegalArgumentLtRtException, NullPointerException, IOException;

	/**
	 * Reads up to <code>len</code> bytes of data from this file into an array of
	 * bytes. This method blocks until at least one byte of input is available.
	 * <p>
	 * Although <code>FilePieces</code> is not a subclass of
	 * <code>InputStream</code>, this method behaves in exactly the same way as the
	 * {@link InputStream#read(byte[], int, int)} method of
	 * <code>InputStream</code>.
	 *
	 * @param offset the first byte of the file to be read
	 * @param buff   the buffer into which the data is read
	 * @param off    the start offset in array b at which the data is written
	 * @param len    the maximum number of bytes read
	 * @return the total number of bytes read into the buffer, or -1 if there is no
	 *         more data because the end of the file has been reached
	 * @throws IllegalArgumentLtRtException If <code>off</code> is negative,
	 *                                      <code>len</code> is negative, or
	 *                                      <code>len</code> is greater than
	 *                                      <code>b.length - off</code>
	 * @throws NullPointerException         If <code>buff</code> is
	 *                                      <code>null</code>
	 * @throws IOException                  If the first byte cannot be read for any
	 *                                      reason other than end of file, or if the
	 *                                      random access file has been closed, or
	 *                                      if some other I/O error occurs
	 */
	public int read(long offset, byte[] buff, int off, int len)
			throws IllegalArgumentLtRtException, NullPointerException, IOException;

	public void readFully(long offset, byte[] buff) throws IllegalArgumentLtRtException, EOFException, IOException;

	public void readFully(long offset, byte[] buff, int off, int len)
			throws IllegalArgumentLtRtException, EOFException, IOException;

	public void write(long offset, int b) throws IllegalArgumentLtRtException, IOException;

	/**
	 * Writes <code>buff.length</code> bytes from the specified byte array to this
	 * file at <code>fileOff</code> position.
	 *
	 * @param offset the first byte of the file to be written
	 * @param buff   the data
	 * @throws IOException If an I/O error occurs
	 */
	public void write(long offset, byte[] buff) throws IllegalArgumentLtRtException, IOException;

	/**
	 * Writes <code>len</code> bytes from the specified byte array to this file at
	 * <code>fileOff</code> position.
	 *
	 * @param offset the first byte of the file to be written
	 * @param buff   the data
	 * @param off    the start offset in the data
	 * @param len    the number of bytes to write
	 * @throws IOException If an I/O error occurs
	 */
	public void write(long offset, byte[] buff, int off, int len) throws IllegalArgumentLtRtException, IOException;

	public FractionInputStream newInputStream() throws IOException;

	public FractionInputStream newInputStream(long offset) throws IllegalArgumentLtRtException, IOException;

	public FractionInputStream newInputStream(long offset, long length)
			throws IllegalArgumentLtRtException, IOException;

	public FractionOutputStream newOutputStream() throws IOException;

	public FractionOutputStream newOutputStream(long offset) throws IllegalArgumentLtRtException, IOException;

	public long length() throws IOException;

	public void setLength(long length) throws IllegalArgumentLtRtException, IOException;

}

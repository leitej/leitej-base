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

package leitej.xml.om;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import leitej.exception.XmlInvalidLtException;
import leitej.exception.XmlomInvalidLtException;

/**
 * XML Object Modelling Output Stream<br/>
 *
 * @author Julio Leite
 * @see leitej.xml.om.XmlomWriter
 */
public final class XmlomReader implements Closeable {

	private final Parser in;

	/**
	 * Creates a new instance of XmlomInputStream.
	 *
	 * @param in      an InputStream
	 * @param charset
	 * @throws XmlomInvalidLtException If do not find the root xmlom element
	 * @throws XmlInvalidLtException   If encounter an invalid syntax
	 * @throws IOException             If an I/O error occurs
	 */
	public XmlomReader(final InputStream in, final Charset charset)
			throws XmlomInvalidLtException, XmlInvalidLtException, IOException {
		this.in = new Parser(new InputStreamReader(in, charset));
	}

	/**
	 * Reads an object of type <code>XmlObjectModelling</code> from the stream.
	 *
	 * <p>
	 * If the end of the stream has been reached, the value <code>null</code> is
	 * returned. This method blocks until object is available, the end of the stream
	 * is detected, or an exception is thrown.
	 *
	 * @return the next object, or <code>null</code> if the end of the reader is
	 *         reached
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 */
	public XmlObjectModelling read() throws IOException, XmlInvalidLtException {
		return this.in.read(XmlObjectModelling.class);
	}

	/**
	 * Reads an object of type <code>interfaceClass</code> from the reader.
	 *
	 * <p>
	 * If the end of the stream has been reached, the value <code>null</code> is
	 * returned. This method blocks until object is available, the end of the stream
	 * is detected, or an exception is thrown.
	 *
	 * @param interfaceClass type of object to be ridden
	 * @return the next object, or <code>null</code> if the end of the reader is
	 *         reached
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 */
	public <I extends XmlObjectModelling> I read(final Class<I> interfaceClass)
			throws IOException, XmlInvalidLtException {
		return this.in.read(interfaceClass);
	}

	/**
	 * Reads some number of objects from the reader and stores them into the buffer
	 * <code>array</code>. The number of objects actually read is returned as an
	 * integer. This method blocks until all <code>array</code> objects is
	 * available, end of stream is detected, or an exception is thrown.
	 *
	 * <p>
	 * If the length of <code>array</code> is zero, then no objects are read and
	 * <code>0</code> is returned; otherwise, there is an attempt to read at least
	 * one object. If no object is available because the stream is at the end, the
	 * value <code>-1</code> is returned.
	 *
	 * @param array into which object is read
	 * @return the total number of objects read into the array, or <code>-1</code>
	 *         if there is no more objects because the end of the reader has been
	 *         reached
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 * @throws NullPointerException  If <code>array</code> is <code>null</code>
	 */
	public <I extends XmlObjectModelling> int read(final I[] array) throws IOException, XmlInvalidLtException {
		return read(array, 0, array.length);
	}

	/**
	 * Reads up to <code>len</code> objects from the reader and stores them into the
	 * buffer <code>array</code>. The number of objects actually read is returned as
	 * an integer.This method blocks until objects are available, end of stream is
	 * detected, or an exception is thrown.
	 *
	 * <p>
	 * If <code>len</code> is zero, then no objects are read and <code>0</code> is
	 * returned; otherwise, there is an attempt to read at least one object. If no
	 * object is available because the stream is at the end, the value
	 * <code>-1</code> is returned.
	 *
	 * <p>
	 * The first object read is stored into element <code>array[off]</code>, the
	 * next one into <code>array[off+1]</code>, and so on. The number of objects
	 * read is, at most, equal to <code>len</code>.
	 *
	 * <p>
	 * In every case, elements <code>array[0]</code> through <code>array[off]</code>
	 * and elements <code>array[off+len]</code> through
	 * <code>array[b.length-1]</code> are unaffected.
	 *
	 * @param array into which object is read
	 * @param off   the start offset in <code>array</code> at which the object is
	 *              written
	 * @param len   the maximum number of objects to read
	 * @return the total number of objects read into the array, or <code>-1</code>
	 *         if there is no more objects because the end of the reader has been
	 *         reached
	 * @throws IOException               If an I/O error occurs
	 * @throws XmlInvalidLtException     If is reading a corrupted XML
	 * @throws NullPointerException      If <code>array</code> is <code>null</code>
	 * @throws IndexOutOfBoundsException If <code>off</code> is negative,
	 *                                   <code>len</code> is negative, or
	 *                                   <code>len</code> is greater than
	 *                                   <code>b.length - off</code>
	 */
	public <I extends XmlObjectModelling> int read(final I[] array, final int off, final int len)
			throws IOException, XmlInvalidLtException {
		if (array == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > array.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		@SuppressWarnings("unchecked")
		final Class<I> interfaceClass = (Class<I>) array.getClass().getComponentType();
		int count = 0;
		I obj;
		do {
			obj = read(interfaceClass);
			array[count] = obj;
			count++;
		} while (obj != null && count < array.length);
		if (obj == null && count == 1) {
			return -1;
		}
		return count - ((obj == null) ? 1 : 0);
	}

	/**
	 * Reads all the objects of type <code>interfaceClass</code> till
	 * <code>len</code> or the end of reader.
	 *
	 * <p>
	 * If <code>len</code> is zero, then no objects are read and an empty list is
	 * returned; otherwise, there is an attempt to read at least one object. If no
	 * object is available because the reader is at the end, the value
	 * <code>null</code> is returned.
	 *
	 * @param interfaceClass type of object to be ridden
	 * @param len            max number of objects to read
	 * @return list of all the ridden objects, or <code>null</code> if there is no
	 *         more objects because the end of the reader has been reached
	 * @throws IOException               If an I/O error occurs
	 * @throws XmlInvalidLtException     If is reading a corrupted XML
	 * @throws IndexOutOfBoundsException If <code>len</code> is negative
	 */
	public <I extends XmlObjectModelling> List<I> read(final Class<I> interfaceClass, final int len)
			throws IOException, XmlInvalidLtException {
		if (len < 0) {
			throw new IndexOutOfBoundsException();
		}
		final List<I> result = new ArrayList<>(len);
		I obj;
		if (len != 0) {
			int count = 0;
			do {
				obj = read(interfaceClass);
				if (obj == null) {
					break;
				}
				result.add(obj);
				count++;
			} while (count < len);
		}
		return result;
	}

	/**
	 * Skips over and discards <code>n</code> objects from this reader. The
	 * <code>skip</code> method may, for a variety of reasons, end up skipping over
	 * some smaller number of objects, possibly <code>0</code>. This may result from
	 * any of a number of conditions; reaching end of reader before <code>n</code>
	 * objects have been skipped is only one possibility. The actual number of
	 * objects skipped is returned. If {@code n} is negative, the {@code skip}
	 * method always returns 0, and no objects are skipped.
	 *
	 * @param n the number of objects to be skipped
	 * @return the actual number of bytes skipped
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 */
	public long skip(final long n) throws IOException, XmlInvalidLtException {
		long result = 0;
		while (result < n) {
			read();
			result++;
		}
		return result;
	}

	/**
	 * Closes this reader and releases any system resources associated with the
	 * stream.
	 *
	 * @throws IOException If an I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		this.in.close();
	}

}

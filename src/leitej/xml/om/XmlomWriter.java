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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import leitej.exception.XmlInvalidLtException;

/**
 * XML Object Modelling Writer<br/>
 * <br/>
 * The stream writer do not allow send two references from the same object
 * without flush the stream.
 *
 * @author Julio Leite
 * @see leitej.xml.om.XmlomReader
 */
public final class XmlomWriter {

	public static <I extends XmlObjectModelling> I newXmlObjectModelling(final Class<I> interfaceClass) {
		return Pool.poolXmlObjectModelling(interfaceClass);
	}

	private final Producer out;

	/**
	 * Creates a new instance of XmlomOutputStream.
	 *
	 * @param out     an OutputStream
	 * @param charset
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 * @throws IOException                  If an I/O error occurs
	 */
	public XmlomWriter(final OutputStream out, final Charset charset) throws UnsupportedEncodingException, IOException {
		this(out, charset, true);
	}

	/**
	 * Creates a new instance of XmlomOutputStream.
	 *
	 * @param out      an OutputStream
	 * @param charset
	 * @param minified when false produces a human readable XML, other wise outputs
	 *                 a clean strait line
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 * @throws IOException                  If an I/O error occurs
	 */
	public XmlomWriter(final OutputStream out, final Charset charset, final boolean minified)
			throws UnsupportedEncodingException, IOException {
		try {
			this.out = new Producer(new OutputStreamWriter(out, charset), minified);
		} catch (final XmlInvalidLtException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Puts the objects in list to be written when flush the stream.
	 *
	 * @param xmlom objects to write
	 */
	public <I extends XmlObjectModelling> void write(final I xmlom) {
		this.out.add(xmlom);
	}

	/**
	 * Puts the objects in list to be written when flush the stream.
	 *
	 * @param xmlom objects to write
	 */
	public <I extends XmlObjectModelling> void write(final I[] xmlom) {
		this.out.add(xmlom);
	}

	/**
	 * Flushes this writer stream and forces any buffered output xmlom objects to be
	 * written out.
	 *
	 * @exception IOException if an I/O error occurs.
	 */
	public void flush() throws IOException {
		try {
			this.out.flush();
		} catch (final XmlInvalidLtException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Closes this output stream and releases any system resources associated with
	 * this stream. A closed writer cannot perform output operations and cannot be
	 * reopened.
	 *
	 * @exception IOException if an I/O error occurs.
	 */
	public void close() throws IOException {
		try {
			this.out.close();
		} catch (final XmlInvalidLtException e) {
			throw new IOException(e);
		}
	}

}

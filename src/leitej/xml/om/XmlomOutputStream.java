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

import leitej.exception.XmlInvalidLtException;

/**
 * XML Object Modelling Output Stream<br/>
 * <br/>
 * The stream do not allow send two references from the same object without
 * flush the stream.
 *
 * @author Julio Leite
 * @see leitej.xml.om.XmlomInputStream
 */
public final class XmlomOutputStream extends OutputStream {

	private static final DataProxy DATA_PROXY = DataProxy.getInstance();

	public static <I extends XmlObjectModelling> I newXmlObjectModelling(final Class<I> interfaceClass) {
		return Pool.poolXmlObjectModelling(interfaceClass);
	}

	private final Producer out;

	/**
	 * Creates a new instance of XmlomOutputStream.
	 *
	 * @param out         an OutputStream
	 * @param charsetName the name of a supported {@link java.nio.charset.Charset
	 *                    charset}
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 */
	public XmlomOutputStream(final OutputStream out, final String charsetName) throws UnsupportedEncodingException {
		this(out, charsetName, true);
	}

	/**
	 * Creates a new instance of XmlomOutputStream.
	 *
	 * @param out         an OutputStream
	 * @param charsetName the name of a supported {@link java.nio.charset.Charset
	 *                    charset}
	 * @param minified    when false produces a human readable XML, other wise
	 *                    outputs a clean strait line
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 */
	public XmlomOutputStream(final OutputStream out, final String charsetName, final boolean minified)
			throws UnsupportedEncodingException {
		this.out = new Producer(new OutputStreamWriter(out, charsetName), minified);
	}

	@Override
	public void write(final int b) throws IOException {
		final XmlObjectModelling tmp = newXmlObjectModelling(XmlObjectModelling.class);
		DATA_PROXY.getInvocationHandler(tmp).setByteArray(new byte[] { (byte) (b & 0xff) });
		this.write(tmp);
	}

	@Override
	public void write(final byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(final byte b[], final int off, final int len) throws IOException {
		if ((off | len | (b.length - (len + off)) | (off + len)) < 0) {
			throw new IndexOutOfBoundsException();
		}
		final XmlObjectModelling tmp = newXmlObjectModelling(XmlObjectModelling.class);
		final byte bDest[] = new byte[len];
		System.arraycopy(b, off, bDest, 0, len);
		DATA_PROXY.getInvocationHandler(tmp).setByteArray(bDest);
		this.write(tmp);
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

	@Override
	public void flush() throws IOException {
		try {
			this.out.flush();
		} catch (final XmlInvalidLtException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Finalizes the XML.<br/>
	 * This is the last method to be called that writes to the stream.
	 *
	 * @throws IOException If an I/O error occurs
	 */
	public void doFinal() throws IOException {
		try {
			this.out.doFinal();
		} catch (final XmlInvalidLtException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			this.out.close();
		} catch (final XmlInvalidLtException e) {
			throw new IOException(e);
		}
	}

}

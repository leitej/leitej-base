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

package leitej.xml.om;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.XmlInvalidLtException;
import leitej.exception.XmlomInvalidLtException;

/**
 * XML Object Modelling Output Stream<br/>
 *
 * @author Julio Leite
 * @see leitej.xml.om.XmlomOutputStream
 */
public final class XmlomInputStream extends InputStream {

	private static final DataProxy DATA_PROXY = DataProxy.getInstance();

	public static <I extends XmlObjectModelling> void registry(final Class<I> interfaceClass)
			throws IllegalArgumentLtRtException {
		TrustClassname.registry(interfaceClass);
	}

	private final Parser in;
	private byte[] buff;
	private int spBuff = 0;

	/**
	 * Creates a new instance of XmlomInputStream.
	 *
	 * @param in          an InputStream
	 * @param charsetName the name of a supported {@link java.nio.charset.Charset
	 *                    charset}
	 * @throws UnsupportedEncodingException If the named charset is not supported
	 */
	public XmlomInputStream(final InputStream in, final String charsetName)
			throws NullPointerException, UnsupportedEncodingException {
		this.in = new Parser(new InputStreamReader(in, charsetName));
	}

	private int receiveBytes() throws IOException, XmlInvalidLtException {
		if (this.buff == null || this.spBuff == this.buff.length) {
			final XmlObjectModelling b = read(XmlObjectModelling.class);
			if (b == null) {
				return -1;
			}
			final DataProxyHandler dph = DATA_PROXY.getInvocationHandler(b);
			if (!XmlObjectModelling.class.equals(dph.getInterface())) {
				throw new IOException(new ClassCastException());
			}
			this.spBuff = 0;
			this.buff = dph.getByteArray();
			if (this.buff == null || this.buff.length == 0) {
				throw new IOException();
			}
			return this.buff.length;
		}
		return 0;
	}

	@Override
	public int read() throws IOException {
		try {
			if (receiveBytes() == -1) {
				return -1;
			}
			return this.buff[this.spBuff++] & 0xff;
		} catch (final ClassCastException e) {
			throw new IOException(e);
		} catch (final XmlInvalidLtException e) {
			throw new IOException(e);
		}
	}

	@Override
	public int read(final byte b[]) throws IOException {
		return read(b, 0, b.length);
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
		try {
			if (receiveBytes() == -1) {
				return -1;
			}
			final int length = Math.min(len, this.buff.length - this.spBuff);
			System.arraycopy(this.buff, this.spBuff, b, off, length);
			this.spBuff += length;
			return length;
		} catch (final ClassCastException e) {
			throw new IOException(e);
		} catch (final XmlInvalidLtException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Reads an object of type <code>clazz</code> from the stream.
	 *
	 * @param interfaceClass type of object to be ridden
	 * @return the ridden object
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 */
	public <I extends XmlObjectModelling> I read(final Class<I> interfaceClass)
			throws IOException, XmlInvalidLtException {
		if (this.buff != null && this.spBuff != this.buff.length) {
			throw new XmlomInvalidLtException();
		}
		return this.in.read(interfaceClass);
	}

	/**
	 * Reads all the objects till the end of stream.
	 *
	 * @return all the ridden objects
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 */
	public List<XmlObjectModelling> readAll() throws IOException, XmlInvalidLtException {
		final List<XmlObjectModelling> result = new ArrayList<>();
		XmlObjectModelling tmp = read(XmlObjectModelling.class);
		while (tmp != null) {
			result.add(tmp);
			tmp = read(XmlObjectModelling.class);
		}
		return result;
	}

	/**
	 * Reads all the objects till the end of stream.
	 *
	 * @param interfaceClass type of object to be ridden
	 * @return all the ridden objects
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 */
	public <I extends XmlObjectModelling> List<I> readAll(final Class<I> interfaceClass)
			throws IOException, XmlInvalidLtException {
		final List<I> result = new ArrayList<>();
		I tmp = read(interfaceClass);
		while (tmp != null) {
			result.add(tmp);
			tmp = read(interfaceClass);
		}
		return result;
	}

	@Override
	public void close() throws IOException {
		this.in.close();
	}

	@Override
	public long skip(final long n) throws IOException {
		throw new IOException("skip/available not supported");
	}

	@Override
	public int available() throws IOException {
		throw new IOException("skip/available not supported");
	}

	@Override
	public void mark(final int readlimit) {
	}

	@Override
	public void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}

	@Override
	public boolean markSupported() {
		return false;
	}

}

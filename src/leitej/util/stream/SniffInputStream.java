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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An useful sniffer of input data stream.
 *
 * @author Julio Leite
 */
public class SniffInputStream extends InputStream {

	private volatile InputStream in;
	private OutputStream out_sniffer;

	/**
	 * Creates a new instance of SniffInputStream.
	 *
	 * @param in          the underlying input stream to be sniffed, or
	 *                    <code>null</code> if this instance is to be created
	 *                    without an underlying stream
	 * @param out_sniffer the underlying output stream to be assigned to the field
	 *                    <tt>this.out_sniffer</tt> for later use, or
	 *                    <code>null</code> if this instance is to be created
	 *                    without an underlying stream
	 */
	public SniffInputStream(final InputStream in, final OutputStream out_sniffer) {
		this.in = in;
		this.out_sniffer = out_sniffer;
	}

	@Override
	public int read() throws IOException {
		final int tmp = this.in.read();
		if (this.out_sniffer != null) {
			this.out_sniffer.write(tmp);
		}
		return tmp;
	}

	@Override
	public int read(final byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(final byte b[], final int off, final int len) throws IOException {
		final int tmp = this.in.read(b, off, len);
		if (this.out_sniffer != null && tmp != -1) {
			this.out_sniffer.write(b, off, tmp);
		}
		return tmp;
	}

	@Override
	public long skip(final long n) throws IOException {
		return this.in.skip(n);
	}

	@Override
	public int available() throws IOException {
		return this.in.available();
	}

	@Override
	public void close() throws IOException {
		this.in.close();
	}

	/**
	 * Closes this <code>out_sniffer</code> stream and releases any system resources
	 * associated with this stream.
	 *
	 * @throws IOException If an I/O error occurs
	 */
	public void closeSniff() throws IOException {
		if (this.out_sniffer != null) {
			try {
				this.out_sniffer.flush();
			} catch (final IOException ignored) {
			}
			this.out_sniffer.close();
			this.out_sniffer = null;
		}
	}

	@Override
	public synchronized void mark(final int readlimit) {
		this.in.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		this.in.reset();
	}

	@Override
	public boolean markSupported() {
		return this.in.markSupported();
	}
}

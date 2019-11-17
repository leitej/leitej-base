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

import java.io.IOException;
import java.io.OutputStream;

/**
 * An useful sniffer of output data stream.
 *
 * @author Julio Leite
 */
public class SniffOutputStream extends OutputStream {

	private final OutputStream out;
	private OutputStream out_sniffer;

	/**
	 * Creates a new instance of SniffOutputStream.
	 *
	 * @param out         the underlying output stream to be sniffed, or
	 *                    <code>null</code> if this instance is to be created
	 *                    without an underlying stream
	 * @param out_sniffer the underlying output stream to be assigned to the field
	 *                    <tt>this.out_sniffer</tt> for later use, or
	 *                    <code>null</code> if this instance is to be created
	 *                    without an underlying stream
	 */
	public SniffOutputStream(final OutputStream out, final OutputStream out_sniffer) {
		this.out = out;
		this.out_sniffer = out_sniffer;
	}

	@Override
	public void write(final int b) throws IOException {
		this.out.write(b);
		if (this.out_sniffer != null) {
			this.out_sniffer.write(b);
		}
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
		this.out.write(b, off, len);
		if (this.out_sniffer != null) {
			this.out_sniffer.write(b, off, len);
		}
	}

	@Override
	public void flush() throws IOException {
		this.out.flush();
		if (this.out_sniffer != null) {
			this.out_sniffer.flush();
		}
	}

	@Override
	public void close() throws IOException {
		try {
			this.out.flush();
		} catch (final IOException ignored) {
		}
		this.out.close();
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

}

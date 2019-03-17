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
import java.io.OutputStream;

import leitej.exception.DataOverflowLtException;

/**
 * An useful control data of output stream.
 *
 * @author Julio Leite
 */
public class ControlDataOutputStream extends OutputStream {

	private final OutputStream out;
	private final ControlDataFlow cdf;
	private final int bytePerSecond;

	/**
	 * Creates a new instance of ControlDataOutputStream.
	 *
	 * @param out the underlying output stream to be sniffed, or <code>null</code>
	 *            if this instance is to be created without an underlying stream
	 */
	public ControlDataOutputStream(final OutputStream out, final int bytePerSecond) {
		this.out = out;
		this.cdf = new ControlDataFlow(bytePerSecond, 0);
		this.bytePerSecond = bytePerSecond;
	}

	@Override
	public void write(final int b) throws IOException {
		try {
			this.cdf.initTrans();
			this.out.write(b);
			this.cdf.endTrans(1);
		} catch (final InterruptedException e) {
			throw new IOException(e);
		} catch (final DataOverflowLtException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void write(final byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(final byte b[], int off, int len) throws IOException {
		if ((off | len | (b.length - (len + off)) | (off + len)) < 0) {
			throw new IndexOutOfBoundsException();
		}
		if (this.bytePerSecond > 0) {
			while (len > this.bytePerSecond) {
				writeAux(b, off, this.bytePerSecond);
				off += this.bytePerSecond;
				len -= this.bytePerSecond;
			}
		}
		writeAux(b, off, len);
	}

	private void writeAux(final byte b[], final int off, final int len) throws IOException {
		try {
			this.cdf.initTrans();
			this.out.write(b, off, len);
			this.cdf.endTrans(len);
		} catch (final InterruptedException e) {
			throw new IOException(e);
		} catch (final DataOverflowLtException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void flush() throws IOException {
		this.out.flush();
	}

	@Override
	public void close() throws IOException {
		try {
			this.out.flush();
		} catch (final IOException ignored) {
		}
		try {
			this.cdf.close();
		} catch (final DataOverflowLtException e) {
			throw new IOException(e);
		} catch (final InterruptedException e) {
			throw new IOException(e);
		} finally {
			this.out.close();
		}
	}

}

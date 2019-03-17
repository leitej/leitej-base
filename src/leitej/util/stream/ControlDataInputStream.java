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

import leitej.exception.DataOverflowLtException;

/**
 * An useful control data of input stream.
 *
 * @author Julio Leite
 */
public class ControlDataInputStream extends InputStream {

	private final InputStream in;
	private final ControlDataFlow cdf;
	private final int bytePerSecond;

	/**
	 * Creates a new instance of ControlDataInputStream.
	 *
	 * @param in the underlying input stream to be sniffed, or <code>null</code> if
	 *           this instance is to be created without an underlying stream
	 */
	public ControlDataInputStream(final InputStream in, final int bytePerSecond, final long maxBytePerStep) {
		this.in = in;
		this.cdf = new ControlDataFlow(bytePerSecond, maxBytePerStep);
		this.bytePerSecond = bytePerSecond;
	}

	@Override
	public int read() throws IOException {
		try {
			this.cdf.initTrans();
			final int tmp = this.in.read();
			this.cdf.endTrans(1);
			return tmp;
		} catch (final InterruptedException e) {
			throw new IOException(e);
		} catch (final DataOverflowLtException e) {
			throw new IOException(e);
		}
	}

	@Override
	public int read(final byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(final byte b[], int off, int len) throws IOException {
		int result = 0;
		int aux = 0;
		if (this.bytePerSecond > 0) {
			while (len > this.bytePerSecond) {
				aux = readAux(b, off, this.bytePerSecond);
				if (aux > 0) {
					result += aux;
					off += aux;
					len -= aux;
				}
				if (aux != this.bytePerSecond) {
					len = 0;
				}
			}
		}
		if (len > 0) {
			aux = readAux(b, off, len);
			if (aux > -1) {
				result += aux;
			} else if (result == 0) {
				result += aux;
			}
		}
		return result;
	}

	private int readAux(final byte b[], final int off, final int len) throws IOException {
		try {
			this.cdf.initTrans();
			final int tmp = this.in.read(b, off, len);
			this.cdf.endTrans(tmp);
			return tmp;
		} catch (final InterruptedException e) {
			throw new IOException(e);
		} catch (final DataOverflowLtException e) {
			throw new IOException(e);
		}
	}

	public synchronized void changeStep() {
		this.cdf.changeStep();
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
		try {
			this.cdf.close();
		} catch (final DataOverflowLtException e) {
			throw new IOException(e);
		} catch (final InterruptedException e) {
			throw new IOException(e);
		} finally {
			this.in.close();
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

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
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import leitej.util.data.BinaryConcat;

/**
 * An useful compressor of input data stream.
 *
 * @author Julio Leite
 */
public class CompressInputStream extends InputStream {

	private static final int BLOCK_SIZE = CompressOutputStream.BLOCK_SIZE;
	private static final int DATA_BLOCK_SIZE = CompressOutputStream.DATA_BLOCK_SIZE;

	private final InputStream in;
	private final Inflater inflater;
	private final byte[] compressed;
	private final BinaryConcat bufferCompressed;
	private final byte[] buffer;
	private int spBuffer;
	private int maxSpBuffer;
	private boolean streamEnd;

	/**
	 * Creates a new instance of CompressInputStream.
	 * 
	 * @param in the underlying input stream, or <code>null</code> if this instance
	 *           is to be created without an underlying stream
	 */
	public CompressInputStream(final InputStream in) {
		this.in = in;
		this.inflater = new Inflater();
		this.compressed = new byte[BLOCK_SIZE];
		this.bufferCompressed = new BinaryConcat();
		this.buffer = new byte[512];
		this.spBuffer = 0;
		this.maxSpBuffer = 0;
		this.streamEnd = false;
	}

	private void inflateData() throws IOException {
		try {
			this.maxSpBuffer = this.inflater.inflate(this.buffer);
			this.spBuffer = 0;
		} catch (final DataFormatException e) {
			throw new IOException(e);
		}
		if (this.inflater.finished()) {
			this.inflater.reset();
		}
	}

	private void fillBuffer() throws IOException {
		if (!this.streamEnd) {
			int fillMeter;
			int tmp;
			inflateData();
			while (this.spBuffer == this.maxSpBuffer) {
				// fillBlock
				fillMeter = 0;
				while (fillMeter < BLOCK_SIZE) {
					tmp = this.in.read(this.compressed, fillMeter, BLOCK_SIZE - fillMeter);
					if (tmp == -1) {
						this.streamEnd = true;
						return;
					}
					fillMeter += tmp;
				}
				// parseBlock
				tmp = ((int) (this.compressed[DATA_BLOCK_SIZE] & 0xff)) >>> 1;
				this.bufferCompressed.add(this.compressed, 0, DATA_BLOCK_SIZE - tmp);
				if (((int) (this.compressed[DATA_BLOCK_SIZE] & 0x01)) == 1) {
					// reach last block
					this.inflater.setInput(this.bufferCompressed.resetSuppress());
					inflateData();
				}
			}
		}
	}

	@Override
	public int read() throws IOException {
		if (this.streamEnd) {
			return -1;
		}
		if (this.spBuffer < this.maxSpBuffer) {
			return ((int) (this.buffer[this.spBuffer++] & 0xff));
		} else {
			fillBuffer();
			return read();
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

		int c = read();
		if (c == -1) {
			return -1;
		}
		b[off] = (byte) (c & 0xff);

		int i = 1;
		try {
			for (; i < len; i++) {
				if (this.spBuffer == this.maxSpBuffer) {
					break;
				}
				c = read();
				if (c == -1) {
					break;
				}
				b[off + i] = (byte) (c & 0xff);
			}
		} catch (final IOException ee) {
		}
		return i;
	}

	@Override
	public void close() throws IOException {
		this.in.close();
		this.spBuffer = this.maxSpBuffer;
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
	public synchronized void mark(final int readlimit) {
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}

	@Override
	public boolean markSupported() {
		return false;
	}

}

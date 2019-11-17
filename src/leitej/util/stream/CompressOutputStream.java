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
import java.util.Random;
import java.util.zip.Deflater;

import leitej.util.data.BinaryConcat;

/**
 * An useful compressor of output data stream.
 *
 * @author Julio Leite
 */
public class CompressOutputStream extends OutputStream {

	/*
	 * Compressed block -> 1024b (128Bytes) 1016b - compressed data | random data to
	 * be ignored 7b - last bites to ignore on compressed data 1b - defining the
	 * last block
	 */

	static final int AUTO_FLUSH_SIZE = 634;
	static final int BLOCK_SIZE = 1024 / 8;
	static final int DATA_BLOCK_SIZE = BLOCK_SIZE - 1;

	private final Random rnd;
	private final OutputStream out;
	private final Deflater deflater;
	private final BinaryConcat buffer;
	private final byte[] compressed;
	private final boolean autoFlush;

	/**
	 * Creates a new instance of CompressOutputStream, with
	 * <code>DEFAULT_COMPRESSION</code> level.
	 * 
	 * @param out the underlying output stream to be assigned to the field
	 *            <tt>this.out</tt> for later use, or <code>null</code> if this
	 *            instance is to be created without an underlying stream
	 */
	public CompressOutputStream(final OutputStream out) {
		this(out, CompressEnum.DEFAULT_COMPRESSION, true);
	}

	/**
	 * Creates a new instance of CompressOutputStream.
	 * 
	 * @param out       the underlying output stream to be assigned to the field
	 *                  <tt>this.out</tt> for later use, or <code>null</code> if
	 *                  this instance is to be created without an underlying stream
	 * @param compress  the compression level
	 * @param autoFlush defines the use of automatic flush
	 */
	public CompressOutputStream(final OutputStream out, final CompressEnum compress, final boolean autoFlush) {
		this.rnd = new Random();
		this.out = out;
		this.deflater = new Deflater(compress.deflaterValue(), false);
		this.deflater.setStrategy(Deflater.DEFAULT_STRATEGY);
		this.buffer = new BinaryConcat();
		this.compressed = new byte[BLOCK_SIZE];
		this.autoFlush = autoFlush;
	}

	private void outWrite() throws IOException {
		if (this.buffer.size() > 0) {
			int tmp;
			int gap;
			int ctrl;
			this.deflater.setInput(this.buffer.resetSuppress());
			this.deflater.finish();
			while (!this.deflater.finished()) {
				tmp = this.deflater.deflate(this.compressed, 0, DATA_BLOCK_SIZE);
				gap = DATA_BLOCK_SIZE - tmp;
				while (tmp < DATA_BLOCK_SIZE) {
					this.compressed[tmp++] = (byte) (this.rnd.nextInt() & 0xff);
				}
				ctrl = ((gap & 0x7f) << 1);
				if (this.deflater.finished()) {
					ctrl |= 0x01;
				}
				this.compressed[DATA_BLOCK_SIZE] = (byte) (ctrl & 0xff);
				this.out.write(this.compressed);
			}
			this.deflater.reset();
		}
	}

	@Override
	public void write(final int b) throws IOException {
		this.buffer.add(b);
		if (this.autoFlush && this.buffer.size() > AUTO_FLUSH_SIZE) {
			outWrite();
		}
	}

	@Override
	public void write(final byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(final byte b[], final int off, final int len) throws IOException {
		this.buffer.add(b, off, len);
		if (this.autoFlush && this.buffer.size() > AUTO_FLUSH_SIZE) {
			outWrite();
		}
	}

	@Override
	public void flush() throws IOException {
		outWrite();
		this.out.flush();
	}

	@Override
	public void close() throws IOException {
		try {
			flush();
		} catch (final IOException ignored) {
		}
		this.out.close();
	}

}

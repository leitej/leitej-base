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
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

import leitej.exception.ClosedLtRtException;
import leitej.exception.ImplementationLtRtException;

/**
 * @author Julio Leite
 *
 */
public abstract class ChunkInputStream extends InputStream {

	private final Queue<byte[]> chunks = new LinkedList<>();
	private byte[] currentChunk = null;
	private int readPosition = 0;
	private boolean closed = false;

	/**
	 * This method is called when stream data is empty and is not closed.
	 */
	protected abstract void waitFeed();

	protected final boolean feed(final byte[] chunk) {
		if (this.closed) {
			throw new ClosedLtRtException("Already closed stream");
		}
		if (chunk != null && chunk.length > 0) {
			synchronized (this.chunks) {
				return this.chunks.offer(chunk);
			}
		}
		return false;
	}

	private final void load() {
		if (this.currentChunk == null) {
			synchronized (this.chunks) {
				this.currentChunk = this.chunks.poll();
			}
		}
	}

	private final void loadWait() {
		while (!this.closed && this.currentChunk == null && this.chunks.isEmpty()) {
			waitFeed();
		}
		load();
	}

	@Override
	public final synchronized int read() throws IOException {
		int result = -1;
		loadWait();
		if (this.currentChunk != null) {
			result = this.currentChunk[this.readPosition] & 0xff;
			this.readPosition += 1;
			if (this.readPosition == this.currentChunk.length) {
				this.readPosition = 0;
				this.currentChunk = null;
			}
		}
		if (result == -1 && (!this.closed || this.currentChunk != null || !this.chunks.isEmpty())) {
			throw new ImplementationLtRtException();
		}
		return result;
	}

	@Override
	public final int read(final byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public final synchronized int read(final byte b[], final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		loadWait();
		if (this.currentChunk != null) {
			final int available = this.currentChunk.length - this.readPosition;
			if (len < available) {
				for (int i = 0; i < len; i++) {
					b[off + i] = this.currentChunk[this.readPosition++];
				}
				return len;
			} else {
				for (int i = 0; i < available; i++) {
					b[off + i] = this.currentChunk[this.readPosition++];
				}
				this.readPosition = 0;
				this.currentChunk = null;
				return available;
			}
		}
		if (!this.closed || !this.chunks.isEmpty()) {
			throw new ImplementationLtRtException();
		}
		return -1;
	}

	@Override
	public final synchronized long skip(final long n) throws IOException {
		if (n > 0) {
			loadWait();
			if (this.currentChunk != null) {
				final int available = this.currentChunk.length - this.readPosition;
				if (n < available) {
					this.readPosition += n;
					return n;
				} else {
					this.readPosition = 0;
					this.currentChunk = null;
					return available;
				}
			}
		}
		return 0;
	}

	@Override
	public final synchronized int available() throws IOException {
		load();
		if (this.currentChunk != null) {
			return this.currentChunk.length - this.readPosition;
		}
		return 0;
	}

	@Override
	public final void close() {
		this.closed = true;
	}

}

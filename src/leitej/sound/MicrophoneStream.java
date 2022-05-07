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

package leitej.sound;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import leitej.exception.ClosedLtRtException;
import leitej.log.Logger;
import leitej.util.DateUtil;

/**
 * @author Julio Leite
 *
 */
public final class MicrophoneStream extends InputStream {

	private static final Logger LOG = Logger.getInstance();

	private final TargetDataLine line;
	private final long durationMS;
	private long endMS;
	private final byte[] buffer;
	private int bPos;
	private int bLen;
	private boolean started;
	private boolean closed;

	public MicrophoneStream(final long durationMS) throws IOException {
		this(ConstantSound.PCM_8KHZ_8BIT_MONO, durationMS);
	}

	public MicrophoneStream(final AudioFormat audioFormat, final long durationMS) throws IOException {
		try {
			LOG.debug("audioFormat: #0", audioFormat);
			this.line = AudioSystem.getTargetDataLine(audioFormat);
			this.line.open(audioFormat);
			this.durationMS = durationMS;
			this.buffer = new byte[this.line.getBufferSize() / 5];
			this.bPos = 0;
			this.bLen = 0;
			this.started = false;
			this.closed = false;
		} catch (final LineUnavailableException e) {
			throw new IOException(e);
		}
	}

	private void ensureOpen() throws IOException {
		if (this.closed) {
			throw new IOException(new ClosedLtRtException());
		}
		if (!this.started) {
			this.started = true;
			this.line.start();
			this.line.flush();
			this.endMS = DateUtil.nowTime() + this.durationMS;
		}
	}

	private void ensureFeed() {
		while (this.bPos == this.bLen && this.endMS > DateUtil.nowTime()) {
			this.bLen = this.line.read(this.buffer, 0, this.buffer.length);
			this.bPos = 0;
		}
	}

	@Override
	public int read() throws IOException {
		ensureOpen();
		ensureFeed();
		if (this.bPos == this.bLen) {
			return -1;
		}
		return this.buffer[this.bPos++] & 0xff;
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		ensureOpen();
		ensureFeed();
		if (this.bPos == this.bLen) {
			return -1;
		}
		final int result = Math.min(this.bLen - this.bPos, len);
		System.arraycopy(this.buffer, this.bPos, b, off, result);
		this.bPos += result;
		return result;
	}

	@Override
	public int available() throws IOException {
		return this.bLen - this.bPos;
	}

	@Override
	public void close() throws IOException {
		if (!this.closed) {
			this.line.drain();
			this.line.stop();
			this.closed = true;
			this.line.close();
		}
	}

}

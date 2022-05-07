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
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import leitej.exception.ClosedLtRtException;
import leitej.log.Logger;

/**
 * @author Julio Leite
 *
 */
public final class SpeakerStream extends OutputStream {

	private static final Logger LOG = Logger.getInstance();

	private final SourceDataLine line;
	private final byte[] byteArray;
	private int baPos;
	private boolean started;
	private boolean closed;

	public SpeakerStream() throws IOException {
		this(ConstantSound.PCM_8KHZ_8BIT_MONO);
	}

	public SpeakerStream(final AudioFormat audioFormat) throws IOException {
		try {
			LOG.debug("audioFormat: #0", audioFormat);
			this.line = AudioSystem.getSourceDataLine(audioFormat);
			this.line.open(audioFormat);
			this.byteArray = new byte[((audioFormat.getFrameSize() == AudioSystem.NOT_SPECIFIED) ? 1
					: audioFormat.getFrameSize())];
			this.baPos = 0;
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
		}
	}

	@Override
	public void write(final int b) throws IOException {
		ensureOpen();
		this.byteArray[this.baPos++] = (byte) (b & 0xff);
		if (this.baPos == this.byteArray.length) {
			this.line.write(this.byteArray, 0, this.baPos);
			this.baPos = 0;
		}
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		ensureOpen();
		if (this.baPos != 0) {
			throw new IOException("Unexpected write array, without complete frame size write int");
		}
		this.line.write(b, off, len);
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

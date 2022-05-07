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

import leitej.log.Logger;

/**
 * @author Julio Leite
 *
 */
public final class SoundUtil {

	private static final Logger LOG = Logger.getInstance();

	public static void beep() throws IOException {
		beep(1600, 750, 1.0);
	}

	public static void beep(final int hertz, final int durationMS, final double volume) throws IOException {
		LOG.debug("hertz: #0, durationMS: #1, volume: #2", hertz, durationMS, volume);
		final OutputStream speaker = new SpeakerStream(ConstantSound.PCM_8KHZ_8BIT_MONO);
		try {
			for (int i = 0; i < durationMS * 8; i++) {
				final double angle = i / (ConstantSound.PCM_8KHZ_8BIT_MONO.getSampleRate() / hertz) * 2.0 * Math.PI;
				speaker.write((int) (Math.sin(angle) * 127.0d * volume));
			}
		} finally {
			speaker.close();
		}
	}

}

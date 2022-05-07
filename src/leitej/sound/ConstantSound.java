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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

import leitej.Constant;
import leitej.log.Logger;

/**
 *
 * @author Julio Leite
 */
public final class ConstantSound {

	private static final Logger LOG = Logger.getInstance();

	static {
		final Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (final Mixer.Info info : mixerInfos) {
			final Mixer mixer = AudioSystem.getMixer(info);
			final Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
			for (final Line.Info targetLineInfo : targetLineInfos) {
				LOG.info("#0 - targetLineInfo: #1", info.getName(), targetLineInfo);
			}
			final Line.Info[] sourceLineInfos = mixer.getSourceLineInfo();
			for (final Line.Info sourceLineInfo : sourceLineInfos) {
				LOG.info("#0 - sourceLineInfo: #1", info.getName(), sourceLineInfo);
			}
		}
	}

	public static final AudioFormat PCM_8KHZ_8BIT_MONO = new AudioFormat(8000.0f, 8, 1, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_8KHZ_16BIT_MONO = new AudioFormat(8000.0f, 16, 1, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_16KHZ_16BIT_MONO = new AudioFormat(16000.0f, 16, 1, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_22KHZ_16BIT_MONO = new AudioFormat(22000.0f, 16, 1, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_44KHZ_16BIT_MONO = new AudioFormat(44000.0f, 16, 1, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_8KHZ_32BIT_MONO = new AudioFormat(8000.0f, 32, 1, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_16KHZ_32BIT_MONO = new AudioFormat(16000.0f, 32, 1, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_22KHZ_32BIT_MONO = new AudioFormat(22000.0f, 32, 1, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_44KHZ_32BIT_MONO = new AudioFormat(44000.0f, 32, 1, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_8KHZ_16BIT_STEREO = new AudioFormat(8000.0f, 16, 2, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_16KHZ_16BIT_STEREO = new AudioFormat(16000.0f, 16, 2, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_22KHZ_16BIT_STEREO = new AudioFormat(22000.0f, 16, 2, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_44KHZ_16BIT_STEREO = new AudioFormat(44000.0f, 16, 2, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_8KHZ_32BIT_STEREO = new AudioFormat(8000.0f, 32, 2, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_16KHZ_32BIT_STEREO = new AudioFormat(16000.0f, 32, 2, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_22KHZ_32BIT_STEREO = new AudioFormat(22000.0f, 32, 2, true,
			Constant.IS_BIG_ENDIAN_NATIVE);
	public static final AudioFormat PCM_44KHZ_32BIT_STEREO = new AudioFormat(44000.0f, 32, 2, true,
			Constant.IS_BIG_ENDIAN_NATIVE);

}

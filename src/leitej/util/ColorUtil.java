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

package leitej.util;

import leitej.exception.IllegalArgumentLtRtException;

/**
 *
 * @author Julio Leite
 */
public final class ColorUtil {

	private static final int EIGHT_BITS = 256;
	public static final int POSSIBLE_COLORS = EIGHT_BITS * EIGHT_BITS * EIGHT_BITS; // 16777216 colors

	public static final int ALPHA_OPAQUE = 255;

	public static int rgbaColor(final int r, final int g, final int b) {
		return rgbaColor(r, g, b, ALPHA_OPAQUE);
	}

	public static int rgbaColor(final int r, final int g, final int b, final int a) {
		return rgbaColor(rgbColor(r, g, b), a);
	}

	public static int rgbaColor(final int rgb, final int a) {
		if (rgb < 0 || rgb >= POSSIBLE_COLORS || a < 0 || a >= EIGHT_BITS) {
			throw new IllegalArgumentLtRtException();
		}
		return (a << 24) + rgb;
	}

	public static int rgbColor(final int r, final int g, final int b) {
		if (r < 0 || r >= EIGHT_BITS || g < 0 || g >= EIGHT_BITS || b < 0 || b >= EIGHT_BITS) {
			throw new IllegalArgumentLtRtException();
		}
		return (r << 16) + (g << 8) + b;
	}

	public static String webColor(final int r, final int g, final int b) {
		if (r < 0 || r >= EIGHT_BITS || g < 0 || g >= EIGHT_BITS || b < 0 || b >= EIGHT_BITS) {
			throw new IllegalArgumentLtRtException();
		}
		return "#" + HexaUtil.toHex(new byte[] { (byte) r }) + HexaUtil.toHex(new byte[] { (byte) g })
				+ HexaUtil.toHex(new byte[] { (byte) b });
	}

	public static String webColor(final int rgb) {
		if (rgb < 0 || rgb >= POSSIBLE_COLORS) {
			throw new IllegalArgumentLtRtException();
		}
		return webColor(redComponent(rgb), greenComponent(rgb), blueComponent(rgb));
	}

	public static int alphaComponent(final int rgba) {
		return (rgba >>> 24) & 0xFF;
	}

	public static int redComponent(final int rgb_a) {
		return (rgb_a >>> 16) & 0xFF;
	}

	public static int greenComponent(final int rgb_a) {
		return (rgb_a >>> 8) & 0xFF;
	}

	public static int blueComponent(final int rgb_a) {
		return rgb_a & 0xFF;
	}

	/**
	 * Creates a new instance of ColorUtil.
	 */
	private ColorUtil() {
	}

}

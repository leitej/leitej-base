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

package leitej.gui.util;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.gui.uniform.model.Color;
import leitej.util.ColorEnum;
import leitej.util.ColorUtil;
import leitej.xml.om.XmlomIOStream;

/**
 *
 * @author Julio Leite
 */
public final class StyleUtil {

	public static final Color newColor(final ColorEnum color) {
		final Color result = XmlomIOStream.newXmlObjectModelling(Color.class);
		result.setRed(color.redComponent());
		result.setGreen(color.greenComponent());
		result.setBlue(color.blueComponent());
		result.setAlpha(ColorUtil.ALPHA_OPAQUE);
		return result;
	}

	public static final Color newColor(final ColorEnum color, final int alpha) {
		if (alpha < 0 || alpha >= 255) {
			throw new IllegalArgumentLtRtException();
		}
		final Color result = XmlomIOStream.newXmlObjectModelling(Color.class);
		result.setRed(color.redComponent());
		result.setGreen(color.greenComponent());
		result.setBlue(color.blueComponent());
		result.setAlpha(alpha);
		return result;
	}

	private StyleUtil() {
	}

}

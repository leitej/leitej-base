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

import java.awt.Image;

/**
 *
 * @author Julio Leite
 */
public enum ImageScaleEnum {
	SCALE_DEFAULT, SCALE_FAST, SCALE_SMOOTH, SCALE_REPLICATE, SCALE_AREA_AVERAGING;

	int hints() {
		switch (this) {
		case SCALE_DEFAULT:
			return Image.SCALE_DEFAULT;
		case SCALE_FAST:
			return Image.SCALE_FAST;
		case SCALE_SMOOTH:
			return Image.SCALE_SMOOTH;
		case SCALE_REPLICATE:
			return Image.SCALE_REPLICATE;
		case SCALE_AREA_AVERAGING:
			return Image.SCALE_AREA_AVERAGING;
		default:
			throw new IllegalStateException(this.toString());

		}
	}
}

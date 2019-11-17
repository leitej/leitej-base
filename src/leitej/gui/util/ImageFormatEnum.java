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

/**
 *
 * @author Julio Leite
 */
public enum ImageFormatEnum {
	PNG, JPEG, BMP, GIF, WBMP;

	public String getName() {
		switch (this) {
		case PNG:
			return "PNG";
		case JPEG:
			return "JPEG";
		case BMP:
			return "BMP";
		case GIF:
			return "GIF";
		case WBMP:
			return "WBMP";
		default:
			throw new IllegalStateException(this.toString());
		}
	}
}

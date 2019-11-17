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

package leitej.gui.uniform.model;

import javax.swing.border.TitledBorder;

import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
public enum BorderTitleJustificationEnum {
	DEFAULT_JUSTIFICATION, // use the default justification for the title text
	LEFT, // position title text at the left side of the border line
	CENTER, // position title text in the center of the border line
	RIGHT, // position title text at the right side of the border line
	LEADING, // position title text at the left side of the border line for left to right
				// orientation, at the right side of the border line for right to left
				// orientation
	TRAILING, // position title text at the right side of the border line for left to right
				// orientation, at the left side of the border line for right to left
				// orientation
	;

	public int getJustification() {
		switch (this) {
		case DEFAULT_JUSTIFICATION:
			return TitledBorder.DEFAULT_JUSTIFICATION;
		case LEFT:
			return TitledBorder.LEFT;
		case CENTER:
			return TitledBorder.CENTER;
		case RIGHT:
			return TitledBorder.RIGHT;
		case LEADING:
			return TitledBorder.LEADING;
		case TRAILING:
			return TitledBorder.TRAILING;
		default:
			throw new ImplementationLtRtException();
		}
	}
}

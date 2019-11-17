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
public enum BorderTitlePositionEnum {
	DEFAULT_POSITION, // use the default vertical orientation for the title text
	ABOVE_TOP, // position the title above the border's top line
	TOP, // position the title in the middle of the border's top line
	BELOW_TOP, // position the title below the border's top line
	ABOVE_BOTTOM, // position the title above the border's bottom line
	BOTTOM, // position the title in the middle of the border's bottom line
	BELOW_BOTTOM, // position the title below the border's bottom line
	;

	public int getPosition() {
		switch (this) {
		case DEFAULT_POSITION:
			return TitledBorder.DEFAULT_POSITION;
		case ABOVE_TOP:
			return TitledBorder.ABOVE_TOP;
		case TOP:
			return TitledBorder.TOP;
		case BELOW_TOP:
			return TitledBorder.BELOW_TOP;
		case ABOVE_BOTTOM:
			return TitledBorder.ABOVE_BOTTOM;
		case BOTTOM:
			return TitledBorder.BOTTOM;
		case BELOW_BOTTOM:
			return TitledBorder.BELOW_BOTTOM;
		default:
			throw new ImplementationLtRtException();
		}
	}
}

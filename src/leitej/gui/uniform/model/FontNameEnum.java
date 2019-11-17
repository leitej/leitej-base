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

import java.awt.Font;

import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
public enum FontNameEnum {
	DIALOG, DIALOG_INPUT, SANS_SERIF, SERIF, MONOSPACED;

	public String getName() {
		switch (this) {
		case DIALOG:
			return Font.DIALOG;
		case DIALOG_INPUT:
			return Font.DIALOG_INPUT;
		case SANS_SERIF:
			return Font.SANS_SERIF;
		case SERIF:
			return Font.SERIF;
		case MONOSPACED:
			return Font.MONOSPACED;
		default:
			throw new ImplementationLtRtException();
		}
	}
}

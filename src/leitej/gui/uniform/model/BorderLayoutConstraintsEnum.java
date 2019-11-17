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

import java.awt.BorderLayout;

import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
public enum BorderLayoutConstraintsEnum {
	PAGE_START, PAGE_END, LINE_START, LINE_END, CENTER;

	public Object getConstraints() {
		switch (this) {
		case PAGE_START:
			return BorderLayout.PAGE_START;
		case PAGE_END:
			return BorderLayout.PAGE_END;
		case LINE_START:
			return BorderLayout.LINE_START;
		case LINE_END:
			return BorderLayout.LINE_END;
		case CENTER:
			return BorderLayout.CENTER;
		default:
			throw new ImplementationLtRtException();
		}
	}
}

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

import javax.swing.JScrollPane;

import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
public enum ScrollPolicyEnum {
	NEVER, AS_NEEDED, ALWAYS;

	public int getVerticalPolicy() {
		switch (this) {
		case NEVER:
			return JScrollPane.VERTICAL_SCROLLBAR_NEVER;
		case AS_NEEDED:
			return JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
		case ALWAYS:
			return JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
		default:
			throw new ImplementationLtRtException();
		}
	}

	public int getHorizontalPolicy() {
		switch (this) {
		case NEVER:
			return JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
		case AS_NEEDED:
			return JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
		case ALWAYS:
			return JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
		default:
			throw new ImplementationLtRtException();
		}
	}
}

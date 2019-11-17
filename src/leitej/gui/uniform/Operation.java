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

package leitej.gui.uniform;

import leitej.gui.uniform.model.ActionEnum;

/**
 *
 * @author Julio Leite
 */
final class Operation {

	private final String index;
	private final ActionEnum action;
	private final String[] arguments;

	Operation(final String index, final ActionEnum action, final String[] arguments) {
		this.index = index;
		this.action = action;
		this.arguments = arguments;
	}

	final String getIndex() {
		return this.index;
	}

	final ActionEnum getAction() {
		return this.action;
	}

	final String[] getArguments() {
		return this.arguments;
	}

}

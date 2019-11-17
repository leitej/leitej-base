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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 *
 * @author Julio Leite
 */
final class OperationGroup {

	private final String elementId;
	private final JComponent component;
	private final List<Operation> operationList;

	OperationGroup(final String elementId, final JComponent component) {
		this.elementId = elementId;
		this.component = component;
		this.operationList = new ArrayList<>();
	}

	final String getElementId() {
		return this.elementId;
	}

	final JComponent getComponent() {
		return this.component;
	}

	final List<Operation> getOperationList() {
		return this.operationList;
	}

}

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

package leitej.xml.om;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ArrayElement
 *
 * @author Julio Leite
 */
final class ArrayElement {

	// ATENCION: to add more array here, has to implement in :
	// Parser.readArrayObject(...)
	// TrustClassname.registry(...)
	// Producer.printprintObject(...)
	private static final Class<?>[] ARRAY_CLASS = {
			// class array
			Set.class, Map.class, List.class };

	private ArrayElement() {
	}

	static boolean has(final Class<?> clazz) {
		if (clazz == null) {
			return false;
		}
		if (clazz.isArray()) {
			return true;
		}
		for (final Class<?> c : ARRAY_CLASS) {
			if (c.isAssignableFrom(clazz)) {
				return true;
			}
		}
		return false;
	}

}

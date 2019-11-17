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
/*
 *
 *
 */

package leitej.ltm;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.util.data.Cache;
import leitej.util.data.CacheSoft;

/**
 *
 *
 * @author Julio Leite
 */
final class ElementTableCache {

	private final Cache<Object, LtmObjectModelling> cache = new CacheSoft<>();

	ElementTableCache() {
	}

	@SuppressWarnings("unchecked")
	<T extends LtmObjectModelling> T get(final Object id) throws IllegalArgumentLtRtException {
		if (id == null) {
			throw new IllegalArgumentLtRtException();
		}
		return (T) this.cache.get(id);
	}

	<T extends LtmObjectModelling> void put(final T table) throws IllegalArgumentLtRtException {
		if (table == null) {
			throw new IllegalArgumentLtRtException();
		}
		this.cache.set(table.getId(), table);
	}

	<T extends LtmObjectModelling> void remove(final Object id) {
		if (id != null) {
			this.cache.remove(id);
		}
	}

}

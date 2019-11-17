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

import java.util.HashMap;
import java.util.Map;

import leitej.util.data.AbstractWeakQueue;

/**
 *
 * @author Julio Leite
 */
final class Pool<I extends XmlObjectModelling> extends AbstractWeakQueue<I> {

	private static final long serialVersionUID = 7202411173807158048L;

	private static final DataProxy DATA_PROXY = DataProxy.getInstance();
	private static final Map<Class<?>, Pool<?>> MAP = new HashMap<>();

	private final Class<I> iClass;

	private Pool(final Class<I> iClass) {
		this.iClass = iClass;
	}

	@Override
	protected I newObject() {
		return DATA_PROXY.newXmlObjectModelling(this.iClass);
	}

	static <I extends XmlObjectModelling> I poolXmlObjectModelling(final Class<I> iClass) {
		return getPool(iClass).poll();
	}

	static <I extends XmlObjectModelling> void offerXmlObjectModelling(final Class<I> iClass, final I i) {
		getPool(iClass).offer(i);
	}

	@SuppressWarnings("unchecked")
	private static <I extends XmlObjectModelling> Pool<I> getPool(final Class<I> iClass) {
		Pool<I> result;
		synchronized (MAP) {
			result = (Pool<I>) MAP.get(iClass);
			if (result == null) {
				result = new Pool<>(iClass);
				MAP.put(iClass, result);
			}
		}
		return result;
	}

}

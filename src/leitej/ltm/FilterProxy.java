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

package leitej.ltm;

import leitej.util.data.AbstractDataProxy;

/**
 * @author Julio Leite
 *
 */
final class FilterProxy extends AbstractDataProxy<LtmObjectModelling, FilterHandler> {

	private static final long serialVersionUID = -7578587610182683780L;

	FilterProxy() {
	}

	<T extends LtmObjectModelling> T newProxyInstance(final Class<T> ltmFilterClass, final LtmFilter<T> ltmFilter) {
		final T result = newProxyInstance(ltmFilterClass, new FilterHandler(ltmFilterClass));
		getInvocationHandler(result).setFilter(ltmFilter);
		return result;
	}

	<T extends LtmObjectModelling> FilterHandler getHandler(final T filter) {
		return getInvocationHandler(filter);
	}

}

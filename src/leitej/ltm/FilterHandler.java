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

import leitej.exception.IllegalArgumentLtRtException;
import leitej.util.data.AbstractDataProxyHandler;
import leitej.util.data.Obfuscate;
import leitej.util.data.ObfuscateUtil;

/**
 * @author Julio Leite
 *
 */
final class FilterHandler extends AbstractDataProxyHandler<LtmObjectModelling> {

	private static final long serialVersionUID = -8120089006746451609L;

	private LtmFilter<?> filter;
	private boolean obSwitchSet;
	private boolean obSwitchGet;

	protected <T extends LtmObjectModelling> FilterHandler(final Class<T> dataInterfaceClass)
			throws IllegalArgumentLtRtException {
		super(dataInterfaceClass);
		this.obSwitchSet = false;
		this.obSwitchGet = false;
	}

	@Override
	protected Object get(final String dataName) {
		throw new UnsupportedOperationException("The LTM filter do not have the get data component.");
	}

	@Override
	protected void set(final String dataName, final Object value) {
		final boolean obfuscatedValue = this.obSwitchSet != this.obSwitchGet;
		this.filter.setDataFilter(dataName, value, obfuscatedValue);
		if (obfuscatedValue) {
			this.obSwitchGet = !this.obSwitchGet;
		}
	}

	@Override
	protected <O> O deObfuscate(final Obfuscate annot, final O value) {
		throw new UnsupportedOperationException("The LTM filter do not have the deObfuscate component.");
	}

	@Override
	protected <O> O obfuscate(final Obfuscate annot, final O value) {
		this.obSwitchSet = !this.obSwitchSet;
		return ObfuscateUtil.hide(annot, value);
	}

	<T extends LtmObjectModelling> void setFilter(final LtmFilter<T> filter) {
		this.filter = filter;
	}

	Class<?> getType(final String dataname) {
		return dataMethodsGetSet(dataname)[0].getReturnType();
	}

}

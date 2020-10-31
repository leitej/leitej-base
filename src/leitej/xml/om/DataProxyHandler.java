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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leitej.util.data.AbstractDataProxyHandler;
import leitej.util.data.Obfuscate;
import leitej.util.data.ObfuscateUtil;

/**
 *
 * @author Julio Leite
 */
final class DataProxyHandler extends AbstractDataProxyHandler<XmlObjectModelling> {

	private static final long serialVersionUID = -6964650269146829894L;

	private final Map<String, Object> data;

	<I extends XmlObjectModelling> DataProxyHandler(final Class<I> dataInterfaceClass) {
		super(dataInterfaceClass);
		this.data = Collections.synchronizedMap(new HashMap<String, Object>());
	}

	@Override
	protected Object get(final String dataName) {
		return this.data.get(dataName);
	}

	@Override
	protected void set(final String dataName, final Object value) {
		this.data.put(dataName, value);
	}

	@Override
	protected <O> O deObfuscate(final Obfuscate annot, final O value) {
		return ObfuscateUtil.unHide(annot, value);
	}

	@Override
	protected <O> O obfuscate(final Obfuscate annot, final O value) {
		return ObfuscateUtil.hide(annot, value);
	}

	<I extends XmlObjectModelling> Class<I> getInterface() {
		return getDataInterfaceClass();
	}

	List<String> getDataNames() {
		return dataNameList();
	}

	Map<String, Object> getDataMap() {
		return this.data;
	}

	Method[] getMethodsGetSet(final String dataName) {
		return dataMethodsGetSet(dataName);
	}

	boolean existsDataName(final String dataName) {
		return existsData(dataName);
	}

}

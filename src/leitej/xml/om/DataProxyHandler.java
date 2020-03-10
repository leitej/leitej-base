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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leitej.exception.ImplementationLtRtException;
import leitej.util.AgnosticUtil;
import leitej.util.data.AbstractDataProxyHandler;

/**
 *
 * @author Julio Leite
 */
final class DataProxyHandler extends AbstractDataProxyHandler<XmlObjectModelling> {

	private static final long serialVersionUID = -6964650269146829894L;

	private static final String METHOD_NAME_RELEASE = "release";
	private static final DataProxy DATA_PROXY = DataProxy.getInstance();

	private final Map<String, Object> data;
	private volatile boolean releasing;

	<I extends XmlObjectModelling> DataProxyHandler(final Class<I> dataInterfaceClass) {
		super(dataInterfaceClass);
		this.data = Collections.synchronizedMap(new HashMap<String, Object>());
		this.releasing = false;
	}

	@Override
	protected Object invokeSpecial(final Object proxy, final Method method, final Object[] args)
			throws NoSuchMethodException {
		final String methodName = method.getName();
		if (args == null && methodName.equals(METHOD_NAME_RELEASE)) {
			release(proxy);
			return null;
		}
		throw new NoSuchMethodException();
	}

	@Override
	protected Object get(final String dataName) {
		return this.data.get(dataName);
	}

	@Override
	protected void set(final String dataName, final Object value) {
		this.data.put(dataName, value);
	}

	@SuppressWarnings("unchecked")
	private <I extends XmlObjectModelling> void release(final Object proxy) {
		boolean doIt = false;
		synchronized (this.data) {
			if (!this.releasing) {
				this.releasing = true;
				doIt = true;
			}
		}
		if (doIt) {
			Object tmp;
			for (final String dataName : getDataNames()) {
				tmp = this.data.get(dataName);
				if (tmp != null) {
					try {
						if (ArrayElement.has(tmp.getClass())) {
							if (tmp.getClass().isArray()) {
								if (XmlObjectModelling.class
										.isAssignableFrom(AgnosticUtil.getMultiDimensionalComponentType(
												dataMethodsGetSet(dataName)[0].getReturnType()))) {
									releaseArrayXmlObjectModelling(tmp);
								}
							} else {
								final Class<?>[] parameterizedClasses = AgnosticUtil
										.getReturnParameterizedTypes(dataMethodsGetSet(dataName)[0]);
								if (parameterizedClasses != null && parameterizedClasses.length != 0) {
									if (parameterizedClasses.length == 1) {
										if (XmlObjectModelling.class.isAssignableFrom(parameterizedClasses[0])) {
											releaseArrayXmlObjectModelling(((Collection<?>) tmp).toArray());
										}
									} else if (parameterizedClasses.length == 2) {
										if (XmlObjectModelling.class.isAssignableFrom(parameterizedClasses[0])
												|| XmlObjectModelling.class.isAssignableFrom(parameterizedClasses[1])) {
											final Map<Object, Object> tmpMap = (Map<Object, Object>) tmp;
											if (XmlObjectModelling.class.isAssignableFrom(parameterizedClasses[0])) {
												for (final Object o : tmpMap.keySet().toArray()) {
													releaseArrayXmlObjectModelling(o);
												}
											}
											if (XmlObjectModelling.class.isAssignableFrom(parameterizedClasses[1])) {
												for (final Object o : tmpMap.values().toArray()) {
													releaseArrayXmlObjectModelling(o);
												}
											}
										}
									} else {
										new ImplementationLtRtException();
									}
								} else {
									new ImplementationLtRtException();
								}
							}
						} else if (!LeafElement.has(tmp.getClass())) {
							DATA_PROXY.getInvocationHandler((I) tmp).release(tmp);
						}
					} catch (final ClassCastException e) {
						new ImplementationLtRtException(e);
					}
				}
			}
			synchronized (this.data) {
				this.data.clear();
				Pool.offerXmlObjectModelling(getDataInterfaceClass(), (I) proxy);
				this.releasing = false;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <I extends XmlObjectModelling> void releaseArrayXmlObjectModelling(final Object object) {
		if (object != null) {
			if (object.getClass().isArray()) {
				final Object[] tmpArray = (Object[]) object;
				for (int i = 0; i < tmpArray.length; i++) {
					releaseArrayXmlObjectModelling(tmpArray[i]);
				}
			} else {
				DATA_PROXY.getInvocationHandler((I) object).release(object);
			}
		}
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

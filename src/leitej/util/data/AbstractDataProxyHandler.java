/*******************************************************************************
 * Copyright (C) 2011 Julio Leite
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

package leitej.util.data;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.util.AgnosticUtil;

/**
 *
 * @author Julio Leite
 */
public abstract class AbstractDataProxyHandler<I> implements InvocationHandler, Serializable {

	private static final long serialVersionUID = 1349354940246444022L;

	private static final String METHOD_NAME_EQUALS = "equals";
	private static final String METHOD_NAME_TO_STRING = "toString";
	private static final String METHOD_NAME_TO_HASH_CODE = "hashCode";
	private static final Map<Class<?>, Map<Method, Boolean>> IS_GETTER_MAP_BY_CLASS = new HashMap<>();
	private static final Map<Class<?>, Map<Method, String>> DATA_NAME_FOR_METHOD_MAP_BY_CLASS = new HashMap<>();
	private static final Map<Class<?>, List<String>> ALL_DATA_NAME_MAP_BY_CLASS = new HashMap<>();
	private static final Map<Class<?>, Map<String, Method[]>> DATA_METHODS_GET_SET_MAP_BY_CLASS = new HashMap<>();
	private static final StringBuilder SB = new StringBuilder();

	private final Class<?> dataInterfaceClass;
	private I myProxy;
	private final Map<Method, Boolean> isGetterMap;
	private final Map<Method, String> dataNameForMethodMap;
	private final List<String> allDataNameList;
	private final Map<String, Method[]> methodsGetSet;

	/**
	 *
	 * @param dataInterfaceClass
	 * @throws IllegalArgumentLtRtException if <code>dataInterfaceClass</code> in
	 *                                      parameter is null or does not represents
	 *                                      an interface
	 */
	protected <T extends I> AbstractDataProxyHandler(final Class<T> dataInterfaceClass)
			throws IllegalArgumentLtRtException {
		if (dataInterfaceClass == null || !dataInterfaceClass.isInterface()) {
			throw new IllegalArgumentLtRtException();
		}
		this.dataInterfaceClass = dataInterfaceClass;
		Map<Method, Boolean> isGetterMapTmp;
		synchronized (IS_GETTER_MAP_BY_CLASS) {
			isGetterMapTmp = IS_GETTER_MAP_BY_CLASS.get(dataInterfaceClass);
			if (isGetterMapTmp == null) {
				registryClass(dataInterfaceClass);
				isGetterMapTmp = IS_GETTER_MAP_BY_CLASS.get(dataInterfaceClass);
			}
		}
		this.isGetterMap = isGetterMapTmp;
		this.dataNameForMethodMap = DATA_NAME_FOR_METHOD_MAP_BY_CLASS.get(dataInterfaceClass);
		this.allDataNameList = ALL_DATA_NAME_MAP_BY_CLASS.get(dataInterfaceClass);
		this.methodsGetSet = DATA_METHODS_GET_SET_MAP_BY_CLASS.get(dataInterfaceClass);
	}

	private static void registryClass(final Class<?> dataInterfaceClass) {
		final Map<Method, Boolean> isGetterMap = new HashMap<>();
		final Map<Method, String> dataNameForMethodMap = new HashMap<>();
		final List<String> allDataNameMap = new ArrayList<>();
		final Map<String, Method[]> methodsGetSet = new HashMap<>();
		String dataNameTmp;
		for (final Method[] mGetSet : AgnosticUtil.getMethodsGetSet(dataInterfaceClass)) {
			isGetterMap.put(mGetSet[0], Boolean.TRUE);
			isGetterMap.put(mGetSet[1], Boolean.FALSE);
			dataNameTmp = AgnosticUtil.writeDataSetterName(SB, mGetSet[1]).toString();
			dataNameForMethodMap.put(mGetSet[0], dataNameTmp);
			dataNameForMethodMap.put(mGetSet[1], dataNameTmp);
			allDataNameMap.add(dataNameTmp);
			methodsGetSet.put(dataNameTmp, mGetSet);
		}
		IS_GETTER_MAP_BY_CLASS.put(dataInterfaceClass, isGetterMap);
		DATA_NAME_FOR_METHOD_MAP_BY_CLASS.put(dataInterfaceClass, dataNameForMethodMap);
		ALL_DATA_NAME_MAP_BY_CLASS.put(dataInterfaceClass, allDataNameMap);
		DATA_METHODS_GET_SET_MAP_BY_CLASS.put(dataInterfaceClass, methodsGetSet);
	}

	@Override
	public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final String methodName = method.getName();
//		LtSystemOut.debug("Calling #0.#1()", this.dataInterfaceClass.getName(), methodName);
		// equals
		if (args != null && args.length == 1 && methodName.equals(METHOD_NAME_EQUALS)) {
			return this.equals(args[0]);
		}
		// toString
		if (args == null && methodName.equals(METHOD_NAME_TO_STRING)) {
			return this.toString();
		}
		// hashCode
		if (args == null && methodName.equals(METHOD_NAME_TO_HASH_CODE)) {
			return this.hashCode();
		}
		// getters
		if (args == null && isGetterMethod(method)) {
			return validadePrimitive(method, get(dataNameGetter(method)));
		}
		// setter
		if (args != null && args.length == 1 && isSetterMethod(method)) {
			set(dataNameSetter(method), args[0]);
			return null;
		}
		// special methods
		return invokeSpecial(proxy, method, args);
	}

	private boolean isGetterMethod(final Method method) {
		final Boolean tmp = this.isGetterMap.get(method);
		return tmp != null && tmp.booleanValue();
	}

	private Object validadePrimitive(final Method method, final Object obj) {
		Object result = obj;
		if (obj == null && method.getReturnType().isPrimitive()) {
			final Class<?> pClass = method.getReturnType();
			if (AgnosticUtil.PRIMITIVE_BOOLEAN_CLASS.equals(pClass)) {
				result = AgnosticUtil.PRIMITIVE_BOOLEAN_DEFAULT_VALUE;
			} else if (AgnosticUtil.PRIMITIVE_BYTE_CLASS.equals(pClass)) {
				result = AgnosticUtil.PRIMITIVE_BYTE_DEFAULT_VALUE;
			} else if (AgnosticUtil.PRIMITIVE_CHAR_CLASS.equals(pClass)) {
				result = AgnosticUtil.PRIMITIVE_CHAR_DEFAULT_VALUE;
			} else if (AgnosticUtil.PRIMITIVE_DOUBLE_CLASS.equals(pClass)) {
				result = AgnosticUtil.PRIMITIVE_DOUBLE_DEFAULT_VALUE;
			} else if (AgnosticUtil.PRIMITIVE_FLOAT_CLASS.equals(pClass)) {
				result = AgnosticUtil.PRIMITIVE_FLOAT_DEFAULT_VALUE;
			} else if (AgnosticUtil.PRIMITIVE_INT_CLASS.equals(pClass)) {
				result = AgnosticUtil.PRIMITIVE_INT_DEFAULT_VALUE;
			} else if (AgnosticUtil.PRIMITIVE_LONG_CLASS.equals(pClass)) {
				result = AgnosticUtil.PRIMITIVE_LONG_DEFAULT_VALUE;
			} else if (AgnosticUtil.PRIMITIVE_SHORT_CLASS.equals(pClass)) {
				result = AgnosticUtil.PRIMITIVE_SHORT_DEFAULT_VALUE;
			} else {
				new ImplementationLtRtException();
			}
			set(dataNameGetter(method), result);
		}
		return result;
	}

	private boolean isSetterMethod(final Method method) {
		final Boolean tmp = this.isGetterMap.get(method);
		return tmp != null && !tmp.booleanValue();
	}

	private String dataNameGetter(final Method method) {
		return this.dataNameForMethodMap.get(method);
	}

	private String dataNameSetter(final Method method) {
		return this.dataNameForMethodMap.get(method);
	}

	protected Object invokeSpecial(final Object proxy, final Method method, final Object[] args) throws Throwable {
		throw new NoSuchMethodException();
	}

	protected abstract Object get(String dataName);

	protected abstract void set(String dataName, Object value);

	@SuppressWarnings("unchecked")
	protected final <T extends I> Class<T> getDataInterfaceClass() {
		return (Class<T>) this.dataInterfaceClass;
	}

	protected final List<String> dataNameList() {
		return this.allDataNameList;
	}

	protected final boolean existsData(final String dataName) {
		return this.allDataNameList.contains(dataName);
	}

	protected final Method[] dataMethodsGetSet(final String dataName) {
		return this.methodsGetSet.get(dataName);
	}

	@Override
	public String toString() {
		return getDataInterfaceClass().toString() + "<handledBy>" + super.toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj == null || !AbstractDataProxy.isProxyClass(obj.getClass())) {
			return false;
		}
		return super.equals(Proxy.getInvocationHandler(obj));
	}

	protected final I getMyProxy() {
		return this.myProxy;
	}

	final void setMyProxy(final I myProxy) {
		this.myProxy = myProxy;
	}

}

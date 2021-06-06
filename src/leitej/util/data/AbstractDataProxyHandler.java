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

package leitej.util.data;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
	private static final Map<Class<?>, Map<Method, Obfuscate>> OBFUSCATE_MAP_BY_CLASS = new HashMap<>();
	private static final StringBuilder SB = new StringBuilder();

	private final Class<?> dataInterfaceClass;
	private I myProxy;
	private final Map<Method, Boolean> isGetterMap;
	private final Map<Method, String> dataNameForMethodMap;
	private final List<String> allDataNameList;
	private final Map<String, Method[]> methodsGetSet;
	private final Map<Method, Obfuscate> obfuscateMap;

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
		this.obfuscateMap = OBFUSCATE_MAP_BY_CLASS.get(dataInterfaceClass);
	}

	private static void registryClass(final Class<?> dataInterfaceClass) {
		final Map<Method, Boolean> isGetterMap = new HashMap<>();
		final Map<Method, String> dataNameForMethodMap = new HashMap<>();
		final List<String> allDataNameMap = new ArrayList<>();
		final Map<String, Method[]> methodsGetSet = new HashMap<>();
		final Map<Method, Obfuscate> obfuscateMap = new HashMap<>();
		String dataNameTmp;
		Obfuscate obfuscateGet;
		Obfuscate obfuscateSet;
		for (final Method[] mGetSet : AgnosticUtil.getMethodsGetSet(dataInterfaceClass)) {
			isGetterMap.put(mGetSet[0], Boolean.TRUE);
			isGetterMap.put(mGetSet[1], Boolean.FALSE);
			dataNameTmp = AgnosticUtil.writeDataSetterName(SB, mGetSet[1]).toString();
			dataNameForMethodMap.put(mGetSet[0], dataNameTmp);
			dataNameForMethodMap.put(mGetSet[1], dataNameTmp);
			allDataNameMap.add(dataNameTmp);
			methodsGetSet.put(dataNameTmp, mGetSet);
			obfuscateGet = mGetSet[0].getAnnotation(Obfuscate.class);
			obfuscateSet = mGetSet[1].getAnnotation(Obfuscate.class);
			if (obfuscateGet != null || obfuscateSet != null) {
				obfuscateMap.put(mGetSet[0], ((obfuscateGet != null) ? obfuscateGet : obfuscateSet));
				obfuscateMap.put(mGetSet[1], ((obfuscateSet != null) ? obfuscateSet : obfuscateGet));
			}
		}
		IS_GETTER_MAP_BY_CLASS.put(dataInterfaceClass, isGetterMap);
		DATA_NAME_FOR_METHOD_MAP_BY_CLASS.put(dataInterfaceClass, dataNameForMethodMap);
		ALL_DATA_NAME_MAP_BY_CLASS.put(dataInterfaceClass, allDataNameMap);
		DATA_METHODS_GET_SET_MAP_BY_CLASS.put(dataInterfaceClass, methodsGetSet);
		OBFUSCATE_MAP_BY_CLASS.put(dataInterfaceClass, obfuscateMap);
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
			final Object result = validatePrimitive(method, get(dataNameGetter(method)));
			if (this.obfuscateMap.get(method) == null) {
				return result;
			} else {
				return deObfuscate(this.obfuscateMap.get(method), result);
			}
		}
		// setter
		if (args != null && args.length == 1 && isSetterMethod(method)) {
			if (this.obfuscateMap.get(method) == null) {
				set(dataNameSetter(method), args[0]);
				return null;
			} else {
				set(dataNameSetter(method), obfuscate(this.obfuscateMap.get(method), args[0]));
				return null;
			}
		}
		// special methods
		return invokeSpecial(proxy, method, args);
	}

	private boolean isGetterMethod(final Method method) {
		final Boolean tmp = this.isGetterMap.get(method);
		return tmp != null && tmp.booleanValue();
	}

	private Object validatePrimitive(final Method method, final Object obj) {
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

	/**
	 * DeObfuscate <code>value</code>, following <code>annotation</code> policy. If
	 * <code>value</code> is not obfuscated returns <code>value</code>.
	 *
	 * @param annotation obfuscation policy
	 * @param value      to deObfuscate
	 * @return pain state, correspondent to the obfuscated <code>value</code>
	 */
	protected abstract <O extends Object> O deObfuscate(Obfuscate annotation, O value);

	/**
	 * Obfuscate <code>value</code>, following <code>annotation</code> policy. If
	 * <code>value</code> is already obfuscated returns <code>value</code>.
	 *
	 * @param annotation obfuscation policy
	 * @param value      to obfuscate
	 * @return representation of <code>value</code> in obfuscate state
	 */
	protected abstract <O extends Object> O obfuscate(Obfuscate annotation, O value);

	/**
	 * Verify if <code>value</code> is obfuscated.
	 *
	 * @param value to verify
	 * @return true if <code>value</code> is obfuscated
	 */
	protected abstract boolean isObfuscated(Object value);

	/**
	 * It iterates data with obfuscated annotation and check if is in obfuscated
	 * state. If no data has to be obfuscated this method returns true.<br>
	 * <br>
	 * Attention: this method iterates through all linked
	 * <code>AbstractDataProxyHandler</code> recursively starting from
	 * <code>this</code>, it is protected to not check the same link twice.
	 *
	 * @return false if there is data that has be obfuscated and was load in plain
	 *         state
	 */
	protected final boolean isDataSerializeObfustated() {
		boolean result = true;
		final Stack<AbstractDataProxyHandler<?>> toCheck = new Stack<>();
		final List<AbstractDataProxyHandler<?>> blockLoop = new ArrayList<>();
		toCheck.push(this);
		blockLoop.add(this);
		AbstractDataProxyHandler<?> dataHandler;
		while (!toCheck.isEmpty()) {
			dataHandler = toCheck.pop();
			for (final Object method : dataHandler.obfuscateMap.keySet()) {
				result &= dataHandler
						.isObfuscated(dataHandler.get(dataHandler.dataNameGetter(Method.class.cast(method))));
			}
			if (result) {
				Object o;
				for (final String dataName : dataHandler.dataNameList()) {
					o = dataHandler.get(dataName);
					if (o != null) {
						if (AbstractDataProxy.isProxyClass(o.getClass())) {
							flatten(AbstractDataProxyHandler.class.cast(Proxy.getInvocationHandler(o)), toCheck,
									blockLoop);
						} else if (Collection.class.isInstance(o)) {
							flatten(Collection.class.cast(o).iterator(), toCheck, blockLoop);
						} else if (o.getClass().isArray()) {
							flatten((Object[]) o, toCheck, blockLoop);
						}
					}
				}
			} else {
				toCheck.clear();
			}
		}
		return result;
	}

	private void flatten(final Iterator<?> data, final Stack<AbstractDataProxyHandler<?>> toCheck,
			final List<AbstractDataProxyHandler<?>> blockLoop) {
		Object o;
		while (data.hasNext()) {
			o = data.next();
			if (o != null) {
				if (AbstractDataProxy.isProxyClass(o.getClass())) {
					flatten(AbstractDataProxyHandler.class.cast(Proxy.getInvocationHandler(o)), toCheck, blockLoop);
				} else if (Collection.class.isInstance(o)) {
					flatten(Collection.class.cast(o).iterator(), toCheck, blockLoop);
				} else if (o.getClass().isArray()) {
					flatten((Object[]) o, toCheck, blockLoop);
				}
			}
		}
	}

	private void flatten(final Object[] data, final Stack<AbstractDataProxyHandler<?>> toCheck,
			final List<AbstractDataProxyHandler<?>> blockLoop) {
		Object o;
		for (int i = 0; i < data.length; i++) {
			o = data[i];
			if (o != null) {
				if (AbstractDataProxy.isProxyClass(o.getClass())) {
					flatten(AbstractDataProxyHandler.class.cast(Proxy.getInvocationHandler(o)), toCheck, blockLoop);
				} else if (Collection.class.isInstance(o)) {
					flatten(Collection.class.cast(o).iterator(), toCheck, blockLoop);
				} else if (o.getClass().isArray()) {
					flatten((Object[]) o, toCheck, blockLoop);
				}
			}
		}
	}

	private void flatten(final AbstractDataProxyHandler<?> data, final Stack<AbstractDataProxyHandler<?>> toCheck,
			final List<AbstractDataProxyHandler<?>> blockLoop) {
		if (!blockLoop.contains(data)) {
			toCheck.push(data);
			blockLoop.add(data);
		}
	}

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

	protected final boolean isDataToObfuscate(final String dataName) {
		return (this.obfuscateMap.get(dataMethodsGetSet(dataName)[0])) != null;
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

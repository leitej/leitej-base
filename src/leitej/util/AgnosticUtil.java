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

package leitej.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leitej.Constant;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.util.data.InvokeItf;
import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * An useful class to help in agnostics methods.
 *
 * @author Julio Leite
 */
public final class AgnosticUtil implements Serializable {

	private static final long serialVersionUID = 2213501515460083415L;

	public static final String ARRAY_CLASS_KEY = "[";
	public static final String OBJECT_ARRAY_CLASS_KEY = "[L";
	public static final String OBJECT_ARRAY_CLASS_KEY_END = ";";
	public static final Class<?> VOID_CLASS = void.class;
	public static final String VOID_CLASS_NAME = "void";
	public static final Class<?> PRIMITIVE_BYTE_CLASS = byte.class;
	public static final String PRIMITIVE_BYTE_CLASS_NAME = "byte";
	public static final byte PRIMITIVE_BYTE_DEFAULT_VALUE = (byte) 0;
	public static final String PRIMITIVE_BYTE_ARRAY_CLASS_KEY = "[B";
	public static final Class<?> PRIMITIVE_SHORT_CLASS = short.class;
	public static final String PRIMITIVE_SHORT_CLASS_NAME = "short";
	public static final short PRIMITIVE_SHORT_DEFAULT_VALUE = (short) 0;
	public static final String PRIMITIVE_SHORT_ARRAY_CLASS_KEY = "[S";
	public static final Class<?> PRIMITIVE_INT_CLASS = int.class;
	public static final String PRIMITIVE_INT_CLASS_NAME = "int";
	public static final int PRIMITIVE_INT_DEFAULT_VALUE = (int) 0;
	public static final String PRIMITIVE_INT_ARRAY_CLASS_KEY = "[I";
	public static final Class<?> PRIMITIVE_LONG_CLASS = long.class;
	public static final String PRIMITIVE_LONG_CLASS_NAME = "long";
	public static final long PRIMITIVE_LONG_DEFAULT_VALUE = (long) 0;
	public static final String PRIMITIVE_LONG_ARRAY_CLASS_KEY = "[J";
	public static final Class<?> PRIMITIVE_FLOAT_CLASS = float.class;
	public static final String PRIMITIVE_FLOAT_CLASS_NAME = "float";
	public static final float PRIMITIVE_FLOAT_DEFAULT_VALUE = (float) 0;
	public static final String PRIMITIVE_FLOAT_ARRAY_CLASS_KEY = "[F";
	public static final Class<?> PRIMITIVE_DOUBLE_CLASS = double.class;
	public static final String PRIMITIVE_DOUBLE_CLASS_NAME = "double";
	public static final double PRIMITIVE_DOUBLE_DEFAULT_VALUE = (double) 0;
	public static final String PRIMITIVE_DOUBLE_ARRAY_CLASS_KEY = "[D";
	public static final Class<?> PRIMITIVE_BOOLEAN_CLASS = boolean.class;
	public static final String PRIMITIVE_BOOLEAN_CLASS_NAME = "boolean";
	public static final boolean PRIMITIVE_BOOLEAN_DEFAULT_VALUE = false;
	public static final String PRIMITIVE_BOOLEAN_ARRAY_CLASS_KEY = "[Z";
	public static final Class<?> PRIMITIVE_CHAR_CLASS = char.class;
	public static final String PRIMITIVE_CHAR_CLASS_NAME = "char";
	public static final char PRIMITIVE_CHAR_DEFAULT_VALUE = (char) 0;
	public static final String PRIMITIVE_CHAR_ARRAY_CLASS_KEY = "[C";

	private static final AgnosticUtil INSTANCE = new AgnosticUtil();

	private final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
	private final Map<CharSequence, Class<?>> classCache = new HashMap<>();

	private final Map<Class<?>, Method[][]> methodsGetSet = new HashMap<>();
	private final Map<Class<?>, Method[][]> declaredMethodsGetSet = new HashMap<>();

	private final StringBuilder sbTmp1 = new StringBuilder();
	private final StringBuilder sbTmp2 = new StringBuilder();

	private AgnosticUtil() {
		this.classCache.put(PRIMITIVE_BYTE_CLASS_NAME, PRIMITIVE_BYTE_CLASS);
		this.classCache.put(PRIMITIVE_SHORT_CLASS_NAME, PRIMITIVE_SHORT_CLASS);
		this.classCache.put(PRIMITIVE_INT_CLASS_NAME, PRIMITIVE_INT_CLASS);
		this.classCache.put(PRIMITIVE_LONG_CLASS_NAME, PRIMITIVE_LONG_CLASS);
		this.classCache.put(PRIMITIVE_FLOAT_CLASS_NAME, PRIMITIVE_FLOAT_CLASS);
		this.classCache.put(PRIMITIVE_DOUBLE_CLASS_NAME, PRIMITIVE_DOUBLE_CLASS);
		this.classCache.put(PRIMITIVE_BOOLEAN_CLASS_NAME, PRIMITIVE_BOOLEAN_CLASS);
		this.classCache.put(PRIMITIVE_CHAR_CLASS_NAME, PRIMITIVE_CHAR_CLASS);
		this.classCache.put(VOID_CLASS_NAME, VOID_CLASS);
		this.classCache.put(PRIMITIVE_BYTE_ARRAY_CLASS_KEY, Array.newInstance(PRIMITIVE_BYTE_CLASS, 0).getClass());
		this.classCache.put(PRIMITIVE_SHORT_ARRAY_CLASS_KEY, Array.newInstance(PRIMITIVE_SHORT_CLASS, 0).getClass());
		this.classCache.put(PRIMITIVE_INT_ARRAY_CLASS_KEY, Array.newInstance(PRIMITIVE_INT_CLASS, 0).getClass());
		this.classCache.put(PRIMITIVE_LONG_ARRAY_CLASS_KEY, Array.newInstance(PRIMITIVE_LONG_CLASS, 0).getClass());
		this.classCache.put(PRIMITIVE_FLOAT_ARRAY_CLASS_KEY, Array.newInstance(PRIMITIVE_FLOAT_CLASS, 0).getClass());
		this.classCache.put(PRIMITIVE_DOUBLE_ARRAY_CLASS_KEY, Array.newInstance(PRIMITIVE_DOUBLE_CLASS, 0).getClass());
		this.classCache.put(PRIMITIVE_BOOLEAN_ARRAY_CLASS_KEY,
				Array.newInstance(PRIMITIVE_BOOLEAN_CLASS, 0).getClass());
		this.classCache.put(PRIMITIVE_CHAR_ARRAY_CLASS_KEY, Array.newInstance(PRIMITIVE_CHAR_CLASS, 0).getClass());
	}

	/**
	 * Get a method agnostic.<br/>
	 * If the method is static send the respective class in parameter object.
	 *
	 * @param object     to get a method
	 * @param methodName name of the method
	 * @param args       the list of parameters method
	 * @return method specified by arguments
	 * @throws SecurityException            if a security manager denies access
	 * @throws NoSuchMethodException        if a matching method is not found
	 * @throws IllegalArgumentLtRtException if <code>object</code> parameter is null
	 */
	public static Method getMethod(final Object object, final String methodName, final Class<?>... args)
			throws IllegalArgumentLtRtException, SecurityException, NoSuchMethodException {
		if (object == null) {
			throw new IllegalArgumentLtRtException("lt.AgnArgNull");
		}
		Method method = null;
		if (Class.class.isInstance(object)) {
			method = ((Class<?>) object).getDeclaredMethod(methodName, args);
		} else {
			method = object.getClass().getMethod(methodName, args);
		}
		return method;
	}

	/**
	 * Invoke the method described by {@link leitej.util.data.InvokeItf InvokeItf}.
	 *
	 * @param data describing the invocation
	 * @return the invocation result
	 * @throws IllegalArgumentException    if the method is an instance method and
	 *                                     the specified object argument is not an
	 *                                     instance of the class or interface
	 *                                     declaring the underlying method (or of a
	 *                                     subclass or implementor thereof); if the
	 *                                     number of actual and formal parameters
	 *                                     differ; if an unwrapping conversion for
	 *                                     primitive arguments fails; or if, after
	 *                                     possible unwrapping, a parameter value
	 *                                     cannot be converted to the corresponding
	 *                                     formal parameter type by a method
	 *                                     invocation conversion
	 * @throws IllegalAccessException      if this Method object enforces Java
	 *                                     language access control and the
	 *                                     underlying method is inaccessible
	 * @throws InvocationTargetException   if the underlying method throws an
	 *                                     exception
	 * @throws NullPointerException        if the specified object is null and the
	 *                                     method is an instance method
	 * @throws ExceptionInInitializerError if the initialisation provoked by this
	 *                                     method fails
	 */
	public static Object invoke(final InvokeItf data) throws ExceptionInInitializerError, NullPointerException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return invoke(data.getObject(), data.getMethod(), data.getArgs());
	}

	/**
	 * Invoke the method described by arguments.<br/>
	 * NOTE: if one argument of method to invoke is an array, this has to be cast to
	 * object.
	 *
	 * @param object the object the underlying method is invoked from
	 * @param method to invoke
	 * @param args   the arguments used for the method call
	 * @return invoke result
	 * @throws IllegalArgumentException    if the method is an instance method and
	 *                                     the specified object argument is not an
	 *                                     instance of the class or interface
	 *                                     declaring the underlying method (or of a
	 *                                     subclass or implementor thereof); if the
	 *                                     number of actual and formal parameters
	 *                                     differ; if an unwrapping conversion for
	 *                                     primitive arguments fails; or if, after
	 *                                     possible unwrapping, a parameter value
	 *                                     cannot be converted to the corresponding
	 *                                     formal parameter type by a method
	 *                                     invocation conversion
	 * @throws IllegalAccessException      if this Method object enforces Java
	 *                                     language access control and the
	 *                                     underlying method is inaccessible
	 * @throws InvocationTargetException   if the underlying method throws an
	 *                                     exception
	 * @throws NullPointerException        if the specified object is null and the
	 *                                     method is an instance method
	 * @throws ExceptionInInitializerError if the initialisation provoked by this
	 *                                     method fails
	 */
	public static Object invoke(final Object object, final Method method, final Object... args)
			throws ExceptionInInitializerError, NullPointerException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		return method.invoke(object, args);
	}

	/**
	 * Verifies if the class name represents an array.
	 *
	 * @param className Class name to be verified
	 * @return boolean
	 */
	public static boolean isArray(final CharSequence className) {
		return className != null && className.length() > 1 && ARRAY_CLASS_KEY.charAt(0) == className.charAt(0);
	}

	/**
	 * Get the class through the class name.
	 *
	 * @param className name
	 * @return class represented by name
	 * @throws ClassNotFoundException if the class was not found
	 */
	public static Class<?> getClass(final CharSequence className) throws ClassNotFoundException {
		Class<?> result = null;
		// cache verify
		if (className != null) {
			synchronized (INSTANCE.classCache) {
				result = INSTANCE.classCache.get(className);
				if (result == null) {
					// find array
					if (isArray(className)) {
						if (ARRAY_CLASS_KEY.charAt(0) == className.charAt(1)) {
							// multiple dimensions
							result = Array.newInstance(getClass(className.subSequence(1, className.length())), 0)
									.getClass();
						} else {
							if (className.length() > 3
									&& OBJECT_ARRAY_CLASS_KEY_END.charAt(0) == className.charAt(className.length() - 1)
									&& OBJECT_ARRAY_CLASS_KEY.charAt(1) == className.charAt(1)) {
								result = Array
										.newInstance(getClass(className.subSequence(2, className.length() - 1)), 0)
										.getClass();
							}
						}
					}
					// find object class
					if (result == null) {
						result = INSTANCE.systemClassLoader.loadClass(className.toString());
					}
					// add result to cache
					INSTANCE.classCache.put(className.toString(), result);
				}
			}
		}
		return result;
	}

	public static Class<?> getMultiDimensionalComponentType(final Class<?> arrayClass) {
		Class<?> result = null;
		if (arrayClass.isArray()) {
			result = arrayClass;
			while (result.isArray()) {
				result = result.getComponentType();
			}
		}
		return result;
	}

	/**
	 * Get the class of method's return in argument.<br/>
	 * If the return of <code>method</code> is void then the result is null.
	 *
	 * @param method to get the return class
	 * @return result class
	 */
	public static final Class<?> getReturnType(final Method method) {
		final Class<?> result = method.getReturnType();
		return (!result.getName().equals(VOID_CLASS_NAME)) ? result : null;
	}

	/**
	 *
	 * @param method
	 * @return
	 */
	public static final Class<?>[] getReturnParameterizedTypes(final Method method) {
		return getParameterizedClasses(method.getGenericReturnType());
	}

	/**
	 *
	 * @param type
	 * @return the parameterized classes or null if the <code>type</code> is not a
	 *         <code>ParameterizedType</code>
	 */
	public static Class<?>[] getParameterizedClasses(final Type type) {
		Class<?>[] result = null;
		if (type instanceof ParameterizedType) {
			final Type[] parameterizedTypes = ParameterizedType.class.cast(type).getActualTypeArguments();
			result = new Class<?>[parameterizedTypes.length];
			for (int i = 0; i < result.length; i++) {
				System.out.println(parameterizedTypes[i]);
				result[i] = getClass(parameterizedTypes[i]);
				System.out.println(result[i]);
			}
		}
		return result;
	}

	private static Class<?> getClass(final Type type) {
		Class<?> result = null;
		if (ParameterizedTypeImpl.class.isInstance(type)) {
			result = getClass(ParameterizedTypeImpl.class.cast(type));
		} else if (GenericArrayTypeImpl.class.isInstance(type)) {
			result = getClass(GenericArrayTypeImpl.class.cast(type));
		} else if (Class.class.isInstance(type)) {
			result = (Class<?>) type;
		} else {
			new ImplementationLtRtException(type.toString());
		}
		return result;
	}

	private static Class<?> getClass(final ParameterizedTypeImpl pti) {
		return pti.getRawType();
	}

	private static Class<?> getClass(GenericArrayTypeImpl gati) {
		int dim = 1;
		while (GenericArrayTypeImpl.class.isInstance(gati.getGenericComponentType())) {
			gati = GenericArrayTypeImpl.class.cast(gati.getGenericComponentType());
			dim++;
		}
		return Array.newInstance(getClass(gati.getGenericComponentType()), new int[dim]).getClass();
	}

	public static final Method[][] getDeclaredMethodsGetSet(final Class<?> clazz) {
		Method[][] result = null;
		// cache verify
		synchronized (INSTANCE.declaredMethodsGetSet) {
			result = INSTANCE.declaredMethodsGetSet.get(clazz);
			if (result == null) {
				Method tmpSetMethod = null;
				final List<Method[]> tmpMethodsGetSet = new ArrayList<>();
				for (final Method m : clazz.getDeclaredMethods()) {
					if (isGetterMethod(m)) {
						try {
							tmpSetMethod = getDeclaredSetterMethod(m);
						} catch (final NoSuchMethodException e) {
							tmpSetMethod = null;
						}
						if (tmpSetMethod != null) {
							tmpMethodsGetSet.add(new Method[] { m, tmpSetMethod });
						}
					}
				}
				result = tmpMethodsGetSet.toArray(new Method[tmpMethodsGetSet.size()][2]);
			}
			// add result to cache
			INSTANCE.declaredMethodsGetSet.put(clazz, result);
		}
		return result;
	}

	/**
	 * @throws SecurityException     If a security manager, <i>s</i>, is present and
	 *                               any of the following conditions is met:
	 *
	 *                               <ul>
	 *
	 *                               <li>invocation of
	 *                               <tt>{@link SecurityManager#checkMemberAccess
	 *             s.checkMemberAccess(this, Member.DECLARED)}</tt> denies access to
	 *                               the declared method
	 *
	 *                               <li>the caller's class loader is not the same
	 *                               as or an ancestor of the class loader for the
	 *                               current class and invocation of
	 *                               <tt>{@link SecurityManager#checkPackageAccess
	 *             s.checkPackageAccess()}</tt> denies access to the package of this
	 *                               class
	 *
	 *                               </ul>
	 * @throws NoSuchMethodException if a matching method is not found
	 */
	private static Method getDeclaredSetterMethod(final Method methodGet)
			throws SecurityException, NoSuchMethodException {
		Method result = null;
		synchronized (INSTANCE.sbTmp1) {
			writeSetterMethodName(INSTANCE.sbTmp2, writeDataGetterName(INSTANCE.sbTmp1, methodGet));
			result = methodGet.getDeclaringClass().getDeclaredMethod(INSTANCE.sbTmp2.toString(),
					methodGet.getReturnType());
		}
		return result;
	}

	public static final Method[][] getMethodsGetSet(final Class<?> clazz) {
		Method[][] result = null;
		// cache verify
		synchronized (INSTANCE.methodsGetSet) {
			result = INSTANCE.methodsGetSet.get(clazz);
			if (result == null) {
				Method tmpSetMethod = null;
				final List<Method[]> tmpMethodsGetSet = new ArrayList<>();
				for (final Method m : clazz.getMethods()) {
					if (isGetterMethod(m)) {
						try {
							tmpSetMethod = getSetterMethod(m);
						} catch (final NoSuchMethodException e) {
							tmpSetMethod = null;
						}
						if (tmpSetMethod != null) {
							tmpMethodsGetSet.add(new Method[] { m, tmpSetMethod });
						}
					}
				}
				result = tmpMethodsGetSet.toArray(new Method[tmpMethodsGetSet.size()][2]);
			}
			// add result to cache
			INSTANCE.methodsGetSet.put(clazz, result);
		}
		return result;
	}

	/**
	 * @throws SecurityException     If a security manager, <i>s</i>, is present and
	 *                               any of the following conditions is met:
	 *
	 *                               <ul>
	 *
	 *                               <li>invocation of
	 *                               <tt>{@link SecurityManager#checkMemberAccess
	 *             s.checkMemberAccess(this, Member.DECLARED)}</tt> denies access to
	 *                               the declared method
	 *
	 *                               <li>the caller's class loader is not the same
	 *                               as or an ancestor of the class loader for the
	 *                               current class and invocation of
	 *                               <tt>{@link SecurityManager#checkPackageAccess
	 *             s.checkPackageAccess()}</tt> denies access to the package of this
	 *                               class
	 *
	 *                               </ul>
	 * @throws NoSuchMethodException if a matching method is not found
	 */
	private static Method getSetterMethod(final Method methodGet) throws SecurityException, NoSuchMethodException {
		Method result = null;
		synchronized (INSTANCE.sbTmp1) {
			writeSetterMethodName(INSTANCE.sbTmp2, writeDataGetterName(INSTANCE.sbTmp1, methodGet));
			result = methodGet.getDeclaringClass().getMethod(INSTANCE.sbTmp2.toString(), methodGet.getReturnType());
		}
		return result;
	}

	public static boolean isGetterMethod(final Method methodGet) {
		boolean result = false;
		final String methodName = methodGet.getName();
		if (getReturnType(methodGet) != null && methodGet.getParameterTypes().length == 0
				&& ((methodName.length() > 3 && methodName.substring(0, 3).equals(Constant.GET_PREFIX)
						&& Character.isUpperCase(methodName.charAt(3)))
						|| (methodName.length() > 2 && methodName.substring(0, 2).equals(Constant.IS_PREFIX)
								&& Character.isUpperCase(methodName.charAt(2))))) {
			result = true;
		}
		return result;
	}

	public static boolean isSetterMethod(final Method methodSet) {
		boolean result = false;
		final String methodName = methodSet.getName();
		if (getReturnType(methodSet) == null && methodSet.getParameterTypes().length == 1
				&& (methodName.length() > 3 && methodName.substring(0, 3).equals(Constant.SET_PREFIX)
						&& Character.isUpperCase(methodName.charAt(3)))) {
			result = true;
		}
		return result;
	}

	public static final StringBuilder writeGetterMethodName(final StringBuilder out, final StringBuilder dataName) {
		out.setLength(0);
		out.append(Constant.GET_PREFIX);
		out.append(StringUtil.firstCharacterToUpperCase(dataName));
		return out;
	}

	public static final StringBuilder writeSetterMethodName(final StringBuilder out, final StringBuilder dataName) {
		out.setLength(0);
		out.append(Constant.SET_PREFIX);
		out.append(StringUtil.firstCharacterToUpperCase(dataName));
		return out;
	}

	public static final StringBuilder writeDataGetterName(final StringBuilder out, final Method methodGet) {
		out.setLength(0);
		if (boolean.class.equals(methodGet.getReturnType()) && methodGet.getName().length() > 2) {
			out.append(Character.toLowerCase(methodGet.getName().charAt(2)));
			out.append(methodGet.getName().substring(3));
		} else if (methodGet.getName().length() > 3) {
			out.append(Character.toLowerCase(methodGet.getName().charAt(3)));
			out.append(methodGet.getName().substring(4));
		} else {
			throw new ImplementationLtRtException();
		}
		return out;
	}

	public static final StringBuilder writeDataSetterName(final StringBuilder out, final Method methodSet) {
		out.setLength(0);
		if (methodSet.getName().length() > 3) {
			out.append(Character.toLowerCase(methodSet.getName().charAt(3)));
			out.append(methodSet.getName().substring(4));
		} else {
			throw new ImplementationLtRtException();
		}
		return out;
	}

}

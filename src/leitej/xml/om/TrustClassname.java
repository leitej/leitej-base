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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.util.AgnosticUtil;

/**
 * TrustClassname
 *
 * @author Julio Leite
 */
final class TrustClassname {

	private static final TrustClassname INSTANCE = new TrustClassname();

	private final Map<String, Boolean> trustMap = new HashMap<>();
	private final List<String> trustList = new ArrayList<>();

	private TrustClassname() {
		for (int i = 1; i < TypeVsClassname.typeVsClassname.length; i = i + 2) {
			this.trustList.add(TypeVsClassname.typeVsClassname[i]);
		}
	}

	/**
	 * As interfaces so podem ter dados com o tipo: LeafElement Interface Collection
	 * com parameterizacao directa: LeafElement ou Interface ArrayElement
	 * multidimensional com componente: LeafElement ou Interface
	 *
	 * @param interfaceClass
	 * @throws IllegalArgumentLtRtException if <code>interfaceClass</code> in
	 *                                      parameter is null or does not represents
	 *                                      a valid interface
	 *
	 * @see Producer#printObject(Method,Object,Class<?>,StringBuilder)
	 */
	@SuppressWarnings("unchecked")
	static <I extends XmlObjectModelling> void registry(final Class<I> interfaceClass)
			throws IllegalArgumentLtRtException {
		if (interfaceClass == null || !interfaceClass.isInterface()) {
			throw new IllegalArgumentLtRtException();
		}
		synchronized (INSTANCE.trustMap) {
			if (!has(interfaceClass.getName())) {
				INSTANCE.trustList.add(interfaceClass.getName());
				INSTANCE.trustMap.put(interfaceClass.getName(), Boolean.TRUE);
				Class<?> dataClass;
				for (final Method[] gsMethods : AgnosticUtil.getMethodsGetSet(interfaceClass)) {
					dataClass = gsMethods[0].getReturnType();
					if (!LeafElement.has(dataClass)) {
						if (!ArrayElement.has(dataClass)) {
							if (!XmlObjectModelling.class.isAssignableFrom(dataClass)) {
								throw new IllegalArgumentLtRtException(dataClass.toString());
							}
							registry((Class<I>) dataClass);
						} else {
							if (dataClass.isArray()) {
								final Class<?> dataComponentClass = AgnosticUtil
										.getMultiDimensionalComponentType(dataClass);
								if (!LeafElement.has(dataComponentClass)) {
									if (!XmlObjectModelling.class.isAssignableFrom(dataComponentClass)) {
										throw new IllegalArgumentLtRtException(dataComponentClass.toString());
									}
									registry((Class<I>) dataComponentClass);
								}
							} else if (List.class.isAssignableFrom(dataClass) || Set.class.equals(dataClass)) {
								final Class<?>[] dataParameterizedClasses = AgnosticUtil
										.getReturnParameterizedTypes(gsMethods[0]);
								if (!LeafElement.has(dataParameterizedClasses[0])) {
									if (!XmlObjectModelling.class.isAssignableFrom(dataParameterizedClasses[0])) {
										throw new IllegalArgumentLtRtException(dataParameterizedClasses[0].toString());
									}
									registry((Class<I>) dataParameterizedClasses[0]);
								}
							} else if (Map.class.equals(dataClass)) {
								final Class<?>[] dataParameterizedClasses = AgnosticUtil
										.getReturnParameterizedTypes(gsMethods[0]);
								if (!LeafElement.has(dataParameterizedClasses[0])) {
									if (!XmlObjectModelling.class.isAssignableFrom(dataParameterizedClasses[0])) {
										throw new IllegalArgumentLtRtException(dataParameterizedClasses[0].toString());
									}
									registry((Class<I>) dataParameterizedClasses[0]);
								}
								if (!LeafElement.has(dataParameterizedClasses[1])) {
									if (!XmlObjectModelling.class.isAssignableFrom(dataParameterizedClasses[1])) {
										throw new IllegalArgumentLtRtException(dataParameterizedClasses[1].toString());
									}
									registry((Class<I>) dataParameterizedClasses[1]);
								}
							} else {
								throw new IllegalArgumentLtRtException(
										new ImplementationLtRtException(dataClass.toString()));
							}
						}
					}
				}
			}
		}
	}

	static boolean has(final String classname) {
		Boolean tmp;
		synchronized (INSTANCE.trustMap) {
			tmp = INSTANCE.trustMap.get(classname);
			if (tmp == null) {
				tmp = INSTANCE.findHas(classname);
				INSTANCE.trustMap.put(classname, tmp);
			}
		}
		return tmp != null && tmp.booleanValue();
	}

	private Boolean findHas(final String classname) {
		boolean result = false;
		if (AgnosticUtil.isArray(classname)) {
			int count = 0;
			while (classname.length() + 1 > count
					&& classname.charAt(count) == AgnosticUtil.ARRAY_CLASS_KEY.charAt(0)) {
				count++;
			}
			if (classname.charAt(count) == AgnosticUtil.PRIMITIVE_BYTE_ARRAY_CLASS_KEY.charAt(1)
					|| classname.charAt(count) == AgnosticUtil.PRIMITIVE_SHORT_ARRAY_CLASS_KEY.charAt(1)
					|| classname.charAt(count) == AgnosticUtil.PRIMITIVE_INT_ARRAY_CLASS_KEY.charAt(1)
					|| classname.charAt(count) == AgnosticUtil.PRIMITIVE_LONG_ARRAY_CLASS_KEY.charAt(1)
					|| classname.charAt(count) == AgnosticUtil.PRIMITIVE_FLOAT_ARRAY_CLASS_KEY.charAt(1)
					|| classname.charAt(count) == AgnosticUtil.PRIMITIVE_DOUBLE_ARRAY_CLASS_KEY.charAt(1)
					|| classname.charAt(count) == AgnosticUtil.PRIMITIVE_BOOLEAN_ARRAY_CLASS_KEY.charAt(1)
					|| classname.charAt(count) == AgnosticUtil.PRIMITIVE_CHAR_ARRAY_CLASS_KEY.charAt(1)) {
				if (classname.length() == count + 1) {
					return true;
				} else {
					return false;
				}
			}
			if (classname.charAt(count) == AgnosticUtil.OBJECT_ARRAY_CLASS_KEY.charAt(1)
					&& classname.charAt(count + 1) != AgnosticUtil.ARRAY_CLASS_KEY.charAt(0)
					&& count + 2 < classname.length()
					&& classname.charAt(classname.length() - 1) == AgnosticUtil.OBJECT_ARRAY_CLASS_KEY_END.charAt(0)) {
				return findHas(classname.substring(count + 1, classname.length() - 1));
			}
			return false;
		}
		for (int i = 0; i < this.trustList.size() && !result; i++) {
			if (this.trustList.get(i).equals(classname)) {
				result = true;
			}
		}
		return result;
	}

}

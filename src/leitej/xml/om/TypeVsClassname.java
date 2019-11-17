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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import leitej.Constant;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.util.AgnosticUtil;

/**
 * TypeVsClassname
 *
 * @author Julio Leite
 */
final class TypeVsClassname {

	private static final TypeVsClassname INSTANCE = new TypeVsClassname();
	static final String[] typeVsClassname = {	
			"Pbyte",						"byte",
			"Pshort",						"short",
			"Pint",							"int",
			"Plong",						"long",
			"Pfloat",						"float",
			"Pdouble",						"double",
			"Pboolean",						"boolean",
			"Pchar",						"char",
			"Byte",							"java.lang.Byte",
			"Short",						"java.lang.Short",
			"Integer",						"java.lang.Integer",
			"Long",							"java.lang.Long",
			"Float",						"java.lang.Float",
			"Double",						"java.lang.Double",
			"Boolean",						"java.lang.Boolean",
			"Character",					"java.lang.Character",
			"String",						"java.lang.String",
			"Date",							"java.util.Date",
			"Set",							"java.util.Set",
//			"LinkedHashSet",				"java.util.LinkedHashSet",
//			"HashSet",						"java.util.HashSet",
//			"TreeSet",						"java.util.TreeSet",
//			"SortedSet",					"java.util.SortedSet",
//			"EnumSet",						"java.util.EnumSet",
			"Map",							"java.util.Map",
//			"SortedMap",					"java.util.SortedMap",
//			"EnumMap",						"java.util.EnumMap",
//			"HashMap",						"java.util.HashMap",
//			"Hashtable",					"java.util.Hashtable",
//			"LinkedHashMap",				"java.util.LinkedHashMap",
//			"Properties",					"java.util.Properties",
//			"TreeMap",						"java.util.TreeMap",
			"List",							"java.util.List",
//			"ArrayList",					"java.util.ArrayList",
//			"LinkedList",					"java.util.LinkedList",
//			"Stack",						"java.util.Stack",
//			"Vector",						"java.util.Vector"
			"leitej.XmlObjectModelling",	"leitej.xml.om.XmlObjectModelling"
			};

	private final Map<String, String> mapClass = new HashMap<>();
	private final Map<String, String> mapType = new HashMap<>();

	private TypeVsClassname() {
	}

	private static String convert(String conv, final int way) {
		if (conv == null) {
			return null;
		}
		String result = null;
		String dimL = null;
		if (AgnosticUtil.isArray(conv)) {
			final String[] tmp = conv.split("\\[L");
			if (tmp.length == 2 && tmp[1].length() > 1) {
				dimL = tmp[0];
				conv = tmp[1].substring(0, tmp[1].length() - 1);
			}
		}
		for (int i = way; i < typeVsClassname.length && result == null; i = i + 2) {
			if (typeVsClassname[i].equals(conv)) {
				result = typeVsClassname[i + (1 - 2 * way)];
			}
		}
		if (result == null) {
			result = conv;
		}
		if (dimL != null) {
			result = (new StringBuilder()).append(dimL).append("[L").append(result).append(";").toString();
		}
		return result;
	}

	static void registry(final Class<?> clazz) throws IllegalArgumentLtRtException {
		if (clazz == null) {
			throw new IllegalArgumentLtRtException();
		}
		if (XmlomDataItf.class.isAssignableFrom(clazz)) {
			if (clazz.isInterface()) {
				throw new IllegalArgumentLtRtException();
			}
			try {
				AgnosticUtil.getMethod(clazz, Constant.VALUEOF_METHOD_NAME, String.class);
			} catch (final NoSuchMethodException e) {
				throw new ImplementationLtRtException(e);
			}
			String aliasClassName;
			try {
				final Method method = AgnosticUtil.getMethod(clazz, Constant.ALIAS_CLASS_NAME_METHOD_NAME);
				try {
					aliasClassName = (String) AgnosticUtil.invoke(clazz, method);
				} catch (final ExceptionInInitializerError e) {
					throw new ImplementationLtRtException(e);
				} catch (final NullPointerException e) {
					throw new ImplementationLtRtException(e);
				} catch (final IllegalArgumentException e) {
					throw new ImplementationLtRtException(e);
				} catch (final IllegalAccessException e) {
					throw new ImplementationLtRtException(e);
				} catch (final InvocationTargetException e) {
					throw new ImplementationLtRtException(e);
				}
			} catch (final NoSuchMethodException e) {
				aliasClassName = clazz.getName();
			}
			String result;
			synchronized (INSTANCE.mapClass) {
				result = INSTANCE.mapClass.get(aliasClassName);
				if (result == null) {
					INSTANCE.mapClass.put(aliasClassName, clazz.getName());
				}
			}
			synchronized (INSTANCE.mapType) {
				result = INSTANCE.mapType.get(clazz.getName());
				if (result == null) {
					INSTANCE.mapType.put(clazz.getName(), aliasClassName);
				}
			}
		} else {
			throw new ImplementationLtRtException();
		}
	}

	static String getClassname(final String type) {
		String result;
		synchronized (INSTANCE.mapClass) {
			result = INSTANCE.mapClass.get(type);
			if (result == null) {
				result = convert(type, 0);
				INSTANCE.mapClass.put(type, result);
			}
		}
		return result;
	}

	static String getType(final String classname) {
		String result;
		synchronized (INSTANCE.mapType) {
			result = INSTANCE.mapType.get(classname);
			if (result == null) {
				result = convert(classname, 1);
				INSTANCE.mapType.put(classname, result);
			}
		}
		return result;
	}

}

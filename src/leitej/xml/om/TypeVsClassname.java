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

import leitej.util.AgnosticUtil;

/**
 * TypeVsClassname
 *
 * @author Julio Leite
 */
final class TypeVsClassname {

	private static final TypeVsClassname INSTANCE = new TypeVsClassname();
	static final String[] typeVsClassname = { //
			"Pbyte", "byte", //
			"Pshort", "short", //
			"Pint", "int", //
			"Plong", "long", //
			"Pfloat", "float", //
			"Pdouble", "double", //
			"Pboolean", "boolean", //
			"Pchar", "char", //
			"Byte", "java.lang.Byte", //
			"Short", "java.lang.Short", //
			"Integer", "java.lang.Integer", //
			"Long", "java.lang.Long", //
			"Float", "java.lang.Float", //
			"Double", "java.lang.Double", //
			"Boolean", "java.lang.Boolean", //
			"Character", "java.lang.Character", //
			"String", "java.lang.String", //
			"Date", "java.util.Date", //
			"Set", "java.util.Set", //
//			"LinkedHashSet",				"java.util.LinkedHashSet",
//			"HashSet",						"java.util.HashSet",
//			"TreeSet",						"java.util.TreeSet",
//			"SortedSet",					"java.util.SortedSet",
//			"EnumSet",						"java.util.EnumSet",
			"Map", "java.util.Map", //
//			"SortedMap",					"java.util.SortedMap",
//			"EnumMap",						"java.util.EnumMap",
//			"HashMap",						"java.util.HashMap",
//			"Hashtable",					"java.util.Hashtable",
//			"LinkedHashMap",				"java.util.LinkedHashMap",
//			"Properties",					"java.util.Properties",
//			"TreeMap",						"java.util.TreeMap",
			"List", "java.util.List", //
//			"ArrayList",					"java.util.ArrayList",
//			"LinkedList",					"java.util.LinkedList",
//			"Stack",						"java.util.Stack",
//			"Vector",						"java.util.Vector"
			"leitej.XmlObjectModelling", "leitej.xml.om.XmlObjectModelling"//
	};

	private final Map<String, String> mapClass = new HashMap<>();
	private final Map<String, String> mapType = new HashMap<>();

	private TypeVsClassname() {
	}

	private static String convert(final String conv, final int way) {
		if (conv == null) {
			return null;
		}
		String result = null;
		String dimL = null;
		String convFind = conv;
		if (AgnosticUtil.isArray(conv)) {
			int countDim = 1;
			while (conv.charAt(countDim) == '[') {
				countDim++;
			}
			if (conv.charAt(countDim) == 'L' && conv.length() > countDim + 2) {
				countDim++;
				dimL = conv.substring(0, countDim);
				convFind = conv.substring(countDim, conv.length() - 1);
			}
		}
		for (int i = way; i < typeVsClassname.length && result == null; i = i + 2) {
			if (typeVsClassname[i].equals(convFind)) {
				result = typeVsClassname[i + (1 - 2 * way)];
			}
		}
		if (result == null) {
			result = convFind;
		}
		if (dimL != null) {
			result = (new StringBuilder()).append(dimL).append(result).append(";").toString();
		}
		return result;
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

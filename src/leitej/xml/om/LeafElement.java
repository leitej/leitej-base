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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LeafElement
 *
 * @author Julio Leite
 */
final class LeafElement {

	/*
	 * Rules:
	 *
	 * The leaf class must have the method valueOf(String) The leaf class must have
	 * the method toString like leaf.equals(leaf.valueOf(leaf.toString()))
	 *
	 * Exceptions: String, Date, [B
	 */
	private static final List<Class<?>> LEAF_CLASS = Collections.synchronizedList(new ArrayList<Class<?>>(20));
	static {
//		implements XmlomDataItf,
//		LEAF_CLASS.add(Enum.class);
		LEAF_CLASS.add(byte[].class);
		LEAF_CLASS.add(byte.class);
		LEAF_CLASS.add(short.class);
		LEAF_CLASS.add(int.class);
		LEAF_CLASS.add(long.class);
		LEAF_CLASS.add(float.class);
		LEAF_CLASS.add(double.class);
		LEAF_CLASS.add(boolean.class);
		LEAF_CLASS.add(char.class);
		LEAF_CLASS.add(java.lang.Byte.class);
		LEAF_CLASS.add(java.lang.Short.class);
		LEAF_CLASS.add(java.lang.Integer.class);
		LEAF_CLASS.add(java.lang.Long.class);
		LEAF_CLASS.add(java.lang.Float.class);
		LEAF_CLASS.add(java.lang.Double.class);
		LEAF_CLASS.add(java.lang.Boolean.class);
		LEAF_CLASS.add(java.lang.Character.class);
		LEAF_CLASS.add(java.lang.String.class);
		LEAF_CLASS.add(java.util.Date.class);
	}

	private LeafElement() {
	}

	private static void registry(final Class<?> leafClazz) {
		LEAF_CLASS.add(leafClazz);
		TypeVsClassname.registry(leafClazz);
	}

	static boolean has(final Class<?> clazz) {
		if (clazz == null) {
			return true;
		}
		if (clazz.isEnum()) {
			return true;
		}
		final boolean result = LEAF_CLASS.contains(clazz);
		if (!result && XmlomDataItf.class.isAssignableFrom(clazz)) {
			registry(clazz);
			return true;
		}
		return result;
	}

}

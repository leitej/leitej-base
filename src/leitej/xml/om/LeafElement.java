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
	 * Exceptions: String, Date, [B, Raw
	 */
	private static final Class<?>[] LEAF_CLASS = new Class<?>[] {
//		Enum.class,
		byte[].class,
		byte.class,
		short.class,
		int.class,
		long.class,
		float.class,
		double.class,
		boolean.class,
		char.class,
		java.lang.Byte.class,
		java.lang.Short.class,
		java.lang.Integer.class,
		java.lang.Long.class,
		java.lang.Float.class,
		java.lang.Double.class,
		java.lang.Boolean.class,
		java.lang.Character.class,
		java.lang.String.class,
		java.util.Date.class,
		java.io.InputStream.class
	};

	private LeafElement() {
	}

	static boolean has(final Class<?> clazz) {
		if (clazz == null) {
			return true;
		}
		if (clazz.isEnum()) {
			return true;
		}
		for (int i = 0; i < LEAF_CLASS.length; i++) {
			if (LEAF_CLASS[i].equals(clazz)) {
				return true;
			}
		}
		return false;
	}

}

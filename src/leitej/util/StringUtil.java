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

import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

import leitej.exception.IllegalArgumentLtRtException;

/**
 * An useful class to help in String changes.
 *
 * @author Julio Leite
 */
public final class StringUtil {

	/**
	 * Creates a new instance of StringUtil.
	 */
	private StringUtil() {
	}

	/**
	 * Verifies if <code>str</code> is null if don't have any character different
	 * from white space.
	 *
	 * @param str string to verifies
	 * @return boolean
	 */
	public static boolean isNullOrEmpty(final String str) {
		return (str == null || str.trim().length() == 0);
	}

	/**
	 * Change the first character of <code>str</code> to his corresponding upper
	 * case.
	 *
	 * @param str string to change
	 * @return changed
	 * @throws IndexOutOfBoundsException if <code>str</code> has length < 1
	 */
	public static StringBuffer firstCharacterToUpperCase(final StringBuffer str) throws IndexOutOfBoundsException {
		str.setCharAt(0, Character.toUpperCase(str.charAt(0)));
		return str;
	}

	/**
	 * Change the first character of <code>str</code> to his corresponding upper
	 * case.
	 *
	 * @param str string to change
	 * @return changed
	 * @throws IndexOutOfBoundsException if <code>str</code> has length < 1
	 */
	public static StringBuilder firstCharacterToUpperCase(final StringBuilder str) throws IndexOutOfBoundsException {
		str.setCharAt(0, Character.toUpperCase(str.charAt(0)));
		return str;
	}

	/**
	 * Change the first character of <code>str</code> to his corresponding lower
	 * case.
	 *
	 * @param str string to change
	 * @return changed
	 * @throws IndexOutOfBoundsException if <code>str</code> has length < 1
	 */
	public static StringBuffer firstCharacterToLowerCase(final StringBuffer str) throws IndexOutOfBoundsException {
		str.setCharAt(0, Character.toLowerCase(str.charAt(0)));
		return str;
	}

	/**
	 * Change the first character of <code>str</code> to his corresponding lower
	 * case.
	 *
	 * @param str string to change
	 * @return changed
	 * @throws IndexOutOfBoundsException if <code>str</code> has length < 1
	 */
	public static StringBuilder firstCharacterToLowerCase(final StringBuilder str) throws IndexOutOfBoundsException {
		str.setCharAt(0, Character.toLowerCase(str.charAt(0)));
		return str;
	}

	/**
	 * This method gives the combination between <code>msg</code> and
	 * <code>args</code>.<br/>
	 * For every occurrence of a number (i) following by '#' will concatenate the
	 * index i of <code>args</code> toString.
	 *
	 * @param msg  the data
	 * @param args the objects
	 * @return string compounded
	 */
	public static String insertObjects(final String msg, final Object... args) {
		if (args == null || args.length == 0 || msg == null) {
			return msg;
		}
		final int msgLength = msg.length();
		final StringBuilder resultText = new StringBuilder(32);
		int argPos = -1;
		char c;
		for (int i = 0; i < msgLength; i++) {
			c = msg.charAt(i);
			if (argPos != -1) {
				argPos = argPos * 10 + Integer.valueOf(String.valueOf(c));
				if (!(i + 1 < msgLength && Character.isDigit(msg.charAt(i + 1)))) {
					if (argPos < args.length) {
						concatObject(resultText, args[argPos]);
					} else {
						resultText.append("#" + argPos);
					}
					argPos = -1;
				}
			} else {
				if (c == '#' && (i + 1 < msgLength && Character.isDigit(msg.charAt(i + 1)))) {
					argPos = 0;
				} else {
					resultText.append(c);
				}
			}
		}
		return resultText.toString();
	}

	public static String concatObjects(final String msg, final String regex, final Object[] args)
			throws PatternSyntaxException {
		if (args == null || args.length == 0 || msg == null || regex == null || msg.length() == 0
				|| regex.length() == 0) {
			return msg;
		}
		final StringBuilder resultText = new StringBuilder(32);
		final String[] tmp = msg.split(regex, -1);
		resultText.append(tmp[0]);
		for (int i = 1; i < tmp.length; i++) {
			if ((i - 1) < args.length) {
				concatObject(resultText, args[i - 1]);
			} else {
				resultText.append(regex);
			}
			resultText.append(tmp[i]);
		}
		return resultText.toString();
	}

	private static void concatObject(final StringBuilder sb, final Object obj) {
		if (obj != null && obj.getClass().isArray()) {
			final Class<?> tmp = obj.getClass().getComponentType();
			if (!tmp.isPrimitive()) {
				sb.append(Arrays.deepToString((Object[]) obj));
			} else {
				if (tmp == byte.class) {
					sb.append(Arrays.toString((byte[]) obj));
				} else if (tmp == short.class) {
					sb.append(Arrays.toString((short[]) obj));
				} else if (tmp == int.class) {
					sb.append(Arrays.toString((int[]) obj));
				} else if (tmp == long.class) {
					sb.append(Arrays.toString((long[]) obj));
				} else if (tmp == char.class) {
					sb.append(Arrays.toString((char[]) obj));
				} else if (tmp == float.class) {
					sb.append(Arrays.toString((float[]) obj));
				} else if (tmp == double.class) {
					sb.append(Arrays.toString((double[]) obj));
				} else if (tmp == boolean.class) {
					sb.append(Arrays.toString((boolean[]) obj));
				} else {
					throw new IllegalStateException("an unknown primitive !!!");
				}
			}
		} else {
			sb.append(obj);
		}
	}

	/**
	 * Converts a byte array into a char array.<br/>
	 * Respecting that which two bytes result in one char.
	 *
	 * @param bytes to convert
	 * @return converted char array
	 * @throws IllegalArgumentLtRtException if bytes.length is odd
	 */
	public static char[] toCharArray(final byte[] bytes) throws IllegalArgumentLtRtException {
		if (MathUtil.isOdd(bytes.length)) {
			throw new IllegalArgumentLtRtException();
		}
		final char[] result = new char[bytes.length >>> 1];
		int j;
		for (int i = 0; i < result.length; i++) {
			j = i << 1;
			result[i] = (char) (((bytes[j] & 0xff) << 8) + (bytes[j + 1] & 0xff));
		}
		return result;
	}

	/**
	 * Converts a char array into a byte array.<br/>
	 * Respecting that which char results in two bytes.
	 *
	 * @param chars to convert
	 * @return converted byte array
	 */
	public static byte[] toByteArray(final char[] chars) {
		final byte[] result = new byte[chars.length << 1];
		int j;
		for (int i = 0; i < chars.length; i++) {
			j = i << 1;
			result[j] = (byte) ((chars[i] & 0xff00) >>> 8);
			result[j + 1] = (byte) (chars[i] & 0x00ff);
		}
		return result;
	}

	/**
	 * Converts a CharSequence into a byte array.<br/>
	 * Respecting that which char results in two bytes.
	 *
	 * @param chars to convert
	 * @return converted byte array
	 */
	public static byte[] toByteArray(final CharSequence cs) {
		final byte[] result = new byte[cs.length() << 1];
		int j;
		char c;
		for (int i = 0; i < cs.length(); i++) {
			j = i << 1;
			c = cs.charAt(i);
			result[j] = (byte) ((c & 0xff00) >>> 8);
			result[j + 1] = (byte) (c & 0x00ff);
		}
		return result;
	}

	public static boolean isEquals(final CharSequence cs1, final CharSequence cs2) {
		if (cs1 == cs2) {
			return true;
		}
		if (cs1 == null || cs2 == null || cs1.length() != cs2.length()) {
			return false;
		}
		for (int i = 0; i < cs1.length(); i++) {
			if (cs1.charAt(i) != cs2.charAt(i)) {
				return false;
			}
		}
		return true;
	}

}

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

package leitej.util;

import leitej.exception.IllegalArgumentLtRtException;

/**
 * An useful class to help convert to/from hex.
 *
 * @author Julio Leite
 */
public final class HexaUtil {

	private static final String digits = "0123456789ABCDEF";
	private static final int DIGIT_GAP = -((int) '0');
	private static final int NON_DIGIT_GAP = -((int) 'A') + 10 - DIGIT_GAP;
	private static final int NON_DIGIT_LOWER_GAP = -((int) 'a') + ((int) 'A');

	/**
	 * Creates a new instance of HexaUtil.
	 */
	private HexaUtil() {
	}

	/**
	 * Gives a string representation of data in hex.
	 *
	 * @param data in bytes
	 * @return string representation of data in hex
	 */
	public static String toHex(final byte[] data) {
		return toHex(data, 0, data.length);
	}

	/**
	 * Gives a string representation of data in hex.
	 *
	 * @param data in bytes
	 * @param off  the start offset in the data
	 * @param len  the number of bytes to write
	 * @return string representation of data in hex
	 * @throws IndexOutOfBoundsException if <code>off</code> and <code>len</code>
	 *                                   represents an invalid position or interval
	 *                                   in the <code>data</code> sequence
	 */
	public static String toHex(final byte[] data, final int off, final int len) throws IndexOutOfBoundsException {
		final StringBuilder buffer = new StringBuilder();
		toHex(buffer, data, off, len);
		return buffer.toString();
	}

	/**
	 * Converts data in bytes to a CharSequence representation of data in hex.
	 *
	 * @param dest to write the hex value
	 * @param data in bytes
	 */
	public static void toHex(final StringBuilder dest, final byte[] data) {
		toHex(dest, data, 0, data.length);
	}

	/**
	 * Converts data in bytes to a CharSequence representation of data in hex.
	 *
	 * @param dest to write the hex value
	 * @param data in bytes
	 * @param off  the start offset in the data
	 * @param len  the number of characters to convert
	 * @throws IndexOutOfBoundsException if <code>off</code> and <code>len</code>
	 *                                   represents an invalid position or interval
	 *                                   in the <code>data</code> sequence
	 */
	public static void toHex(final StringBuilder dest, final byte[] data, final int off, final int len)
			throws IndexOutOfBoundsException {
		if ((off | len | (data.length - (len + off)) | (off + len)) < 0) {
			throw new IndexOutOfBoundsException();
		}
		dest.ensureCapacity(dest.length() + len * 2);
		for (int i = off; i < len + off; i++) {
			final int v = data[i] & 0xff;
			dest.append(digits.charAt(v >> 4));
			dest.append(digits.charAt(v & 0xf));
		}
	}

	/**
	 * Converts CharSequence representation of data in hex to data in bytes.
	 *
	 * @param data the data
	 * @return byte array of data
	 * @throws IllegalArgumentLtRtException if data are not in hex or the length is
	 *                                      not even
	 */
	public static byte[] toByte(final CharSequence data) throws IllegalArgumentLtRtException {
		return toByte(data, 0, data.length());
	}

	/**
	 * Converts CharSequence representation of data in hex to data in bytes.
	 *
	 * @param data the data
	 * @param off  the start offset in the data
	 * @param len  the number of characters to convert
	 * @return byte array of data
	 * @throws IllegalArgumentLtRtException if data are not in hex or the len is not
	 *                                      even
	 * @throws IndexOutOfBoundsException    if <code>off</code> and <code>len</code>
	 *                                      represents an invalid position or
	 *                                      interval in the <code>data</code>
	 *                                      sequence
	 */
	public static byte[] toByte(final CharSequence data, final int off, final int len)
			throws IllegalArgumentLtRtException, IndexOutOfBoundsException {
		if ((off | len | (data.length() - (len + off)) | (off + len)) < 0) {
			throw new IndexOutOfBoundsException();
		}
		if (MathUtil.isOdd(len)) {
			throw new IllegalArgumentLtRtException("lt.HexaInvalid");
		}
		final byte[] result = new byte[len / 2];
		int tmp1;
		int tmp2;
		int r = 0;
		for (int i = off; i < len + off; i += 2) {
			tmp1 = data.charAt(i) + DIGIT_GAP;
			if (tmp1 > 9) {
				tmp1 += NON_DIGIT_GAP;
			}
			if (tmp1 > 15) {
				tmp1 += NON_DIGIT_LOWER_GAP;
			}
			if (tmp1 < 0) {
				throw new IllegalArgumentLtRtException("lt.HexaInvalid");
			}
			tmp2 = data.charAt(i + 1) + DIGIT_GAP;
			if (tmp2 > 9) {
				tmp2 += NON_DIGIT_GAP;
			}
			if (tmp2 > 15) {
				tmp2 += NON_DIGIT_LOWER_GAP;
			}
			if (tmp2 < 0) {
				throw new IllegalArgumentLtRtException("lt.HexaInvalid");
			}
			result[r++] = (byte) ((byte) tmp1 << 4 | tmp2);
		}
		return result;
	}

}

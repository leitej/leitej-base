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

import leitej.exception.IllegalArgumentLtRtException;

/**
 * An useful class to help money representations.
 *
 * @author Julio Leite
 */
public final class MoneyUtil {

	private static final char EURO_CURRENCY_SYMBOL = '\u20AC';
	private static final String EURO_FORMAT_REG_EXP = "(-){0,1}\\d{1,3}((\\.){0,1}\\d{3})*(,\\d+){0,1}(\\p{Blank}*"
			+ EURO_CURRENCY_SYMBOL + "){0,1}";

	/**
	 * Creates a new instance of MoneyUtil.
	 */
	private MoneyUtil() {
	}

	/**
	 * Gives a euro string format of value with two decimals digits.
	 *
	 * @param value the data
	 * @return euro string format
	 */
	public static String euroFormat(final long value) {
		return euroFormat(value, 2);
	}

	/**
	 * Gives a euro string format of value with <code>indexUnit</code> decimals
	 * digits.
	 *
	 * @param value     the data
	 * @param indexUnit number of decimals digits
	 * @return euro string format
	 * @throws IllegalArgumentLtRtException If indexUnit is negative
	 */
	public static String euroFormat(final long value, final int indexUnit) throws IllegalArgumentLtRtException {
		return euroFormat(String.valueOf(value).toCharArray(), indexUnit);
	}

	/**
	 * Gives a euro string format of value with <code>indexUnit</code> decimals
	 * digits.
	 *
	 * @param value     the data
	 * @param indexUnit number of decimals digits
	 * @return string format
	 * @throws IllegalArgumentLtRtException If indexUnit is negative
	 */
	private static String euroFormat(final char[] value, final int indexUnit) throws IllegalArgumentLtRtException {
		if (indexUnit < 0) {
			throw new IllegalArgumentLtRtException("lt.MoneyNegativeIndex");
		}
		int negative;
		if (value[0] == '-') {
			negative = 1;
		} else {
			negative = 0;
		}
		final StringBuilder result = new StringBuilder();
		final int indexTmp = value.length;
		int count = 0 + negative;
		int take = 0;
		while ((indexTmp - indexUnit - count) > 0) {
			if (result.length() > 0) {
				result.append('.');
				if (take != 3) {
					take = 3;
				}
			} else {
				if (negative != 0) {
					result.append('-');
				}
				take = (indexTmp - indexUnit - count) % 3;
				if (take == 0) {
					take = 3;
				}
			}
			for (int i = count; i < count + take; i++) {
				result.append(value[i]);
			}
			count += take;
		}
		if (result.length() == 0) {
			if (negative != 0) {
				result.append('-');
			}
			result.append("0,");
		} else {
			result.append(',');
		}
		for (int i = 0; i < indexUnit; i++) {
			if (value.length - (indexUnit - i) > -1 + negative) {
				result.append(value[value.length - (indexUnit - i)]);
			} else {
				result.append('0');
			}
		}
		result.append(" ");
		result.append(EURO_CURRENCY_SYMBOL);
		return result.toString();
	}

	/**
	 * Verifies if <code>value</code> is a valid representation of euro format.
	 *
	 * @param value the data
	 * @return boolean
	 */
	public static boolean isEuroFormatValid(final String value) {
		return value.matches(EURO_FORMAT_REG_EXP);
	}

	/**
	 * Converts the string euro format to a long value.
	 *
	 * @param value the data
	 * @param round boolean defining if is to round the result if necessary
	 * @return long euro value specified
	 * @throws IllegalArgumentLtRtException If the <code>value</code> is not a valid
	 *                                      representation of euro format or the
	 *                                      <code>value</code> is too high for the
	 *                                      type long
	 */
	public static long euroValue(final String value, final boolean round) throws IllegalArgumentLtRtException {
		return euroValue(value, round, 2);
	}

	/**
	 * Converts the string euro format to a long value.
	 *
	 * @param value     the data
	 * @param round
	 * @param indexUnit number of decimals digits
	 * @return long euro value specified
	 * @throws IllegalArgumentLtRtException If indexUnit is negative; the
	 *                                      <code>value</code> is not a valid
	 *                                      representation of euro format or the
	 *                                      <code>value</code> is too high for the
	 *                                      type long
	 */
	public static long euroValue(final String value, final boolean round, final int indexUnit)
			throws IllegalArgumentLtRtException {
		if (indexUnit < 0) {
			throw new IllegalArgumentLtRtException("lt.MoneyNegativeIndex");
		}
		if (!isEuroFormatValid(value)) {
			throw new IllegalArgumentLtRtException("lt.MoneyArgInvalidFormat");
		}
		long result = 0L;
		final char[] tmp = value.toCharArray();
		boolean negative;
		boolean startDecimal = false;
		int countDecimal = 0;
		int count;
		if (tmp[0] == '-') {
			negative = true;
			count = 1;
		} else {
			negative = false;
			count = 0;
		}
		for (int i = count; i < tmp.length; i++) {
			if (Character.isDigit(tmp[i])) {
				if (startDecimal) {
					countDecimal++;
				}
				if (countDecimal <= indexUnit) {
					result = result * 10 + Character.digit(tmp[i], 10);
				} else if (countDecimal == (indexUnit + 1) && round) {
					if (Character.digit(tmp[i], 10) > 4) {
						result++;
					}
				}
			} else if (tmp[i] == ',') {
				startDecimal = true;
			}
			if (result < 0) {
				throw new IllegalArgumentLtRtException("lt.MoneyArgTooHigh");
			}
		}
		while (countDecimal < indexUnit) {
			countDecimal++;
			result *= 10;
			if (result < 0) {
				throw new IllegalArgumentLtRtException("lt.MoneyArgTooHigh");
			}
		}
		if (negative) {
			result *= -1;
		}
		return result;
	}

}

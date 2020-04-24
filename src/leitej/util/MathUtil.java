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

import java.math.BigInteger;

import leitej.exception.IllegalArgumentLtRtException;

/**
 * An useful class to help math computations.
 *
 * @author Julio Leite
 */
public final class MathUtil {

	/**
	 * Creates a new instance of MathUtil.
	 */
	private MathUtil() {
	}

	/**
	 * Calculates the factorial value.
	 *
	 * @param n argument
	 * @return BigInteger value
	 * @throws IllegalArgumentLtRtException if <code>n</code> parameter is negative
	 */
	public static BigInteger factorial(final long n) throws IllegalArgumentLtRtException {
		if (n < 0) {
			throw new IllegalArgumentLtRtException("Can't calculate with negative argument");
		}
		BigInteger result = BigInteger.ONE;
		for (long i = 2L; i <= n; i++) {
			result = result.multiply(BigInteger.valueOf(i));
		}
		return result;
	}

	/**
	 * Calculates the factorial value.
	 *
	 * @param n argument
	 * @return BigInteger value
	 * @throws IllegalArgumentLtRtException if <code>n</code> parameter is negative
	 */
	public static BigInteger factorial(final BigInteger n) throws IllegalArgumentLtRtException {
		if (n.compareTo(BigInteger.ZERO) < 0) {
			throw new IllegalArgumentLtRtException("Can't calculate with negative argument");
		}
		BigInteger result = BigInteger.ONE;
		for (BigInteger i = BigInteger.valueOf(2); i.compareTo(n) <= 0; i = i.add(BigInteger.ONE)) {
			result = result.multiply(i);
		}
		return result;
	}

	/**
	 * Calculates the value of combinations.
	 *
	 * @param n all elements
	 * @param k distinct elements
	 * @return BigInteger value
	 * @throws IllegalArgumentLtRtException if <code>n</code> parameter is negative
	 *                                      or <code>k</code> parameter is negative
	 *                                      or <code>n-k</code> is negative
	 */
	public static BigInteger combination(final long n, final long k) throws IllegalArgumentLtRtException {
		final BigInteger nFact = MathUtil.factorial(n);
		final BigInteger kFact = MathUtil.factorial(k);
		final BigInteger nMkFact = MathUtil.factorial((n - k));
		return nFact.divide(kFact.multiply(nMkFact));
	}

	/**
	 * Calculates the value of combinations.
	 *
	 * @param n all elements
	 * @param k distinct elements
	 * @return BigInteger value
	 * @throws IllegalArgumentLtRtException if <code>n</code> parameter is negative
	 *                                      or <code>k</code> parameter is negative
	 *                                      or <code>n-k</code> is negative
	 */
	public static BigInteger combination(final BigInteger n, final BigInteger k) throws IllegalArgumentLtRtException {
		final BigInteger nFact = MathUtil.factorial(n);
		final BigInteger kFact = MathUtil.factorial(k);
		final BigInteger nMkFact = MathUtil.factorial(n.subtract(k));
		return nFact.divide(kFact.multiply(nMkFact));
	}

	/**
	 * Identifies whether a number is even.<br/>
	 * Note that the zero will be considered even.
	 *
	 * @param n argument
	 * @return true if is even
	 */
	public static boolean isEven(final int n) {
		if (n < 0) {
			isEven(-n);
		}
		return (n & 0x01) == 0;
	}

	/**
	 * Identifies whether a number is even.<br/>
	 * Note that the zero will be considered even.
	 *
	 * @param n argument
	 * @return true if is even
	 */
	public static boolean isEven(final long n) {
		if (n < 0) {
			isEven(-n);
		}
		return (n & 0x01) == 0;
	}

	/**
	 * Identifies whether a number is odd.<br/>
	 * Note that the zero will be considered even.
	 *
	 * @param n argument
	 * @return true if is odd
	 */
	public static boolean isOdd(final int n) {
		return !isEven(n);
	}

	/**
	 * Identifies whether a number is odd.<br/>
	 * Note that the zero will be considered even.
	 *
	 * @param n argument
	 * @return true if is odd
	 */
	public static boolean isOdd(final long n) {
		return !isEven(n);
	}

}

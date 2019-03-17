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

package test.util;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Arrays;

import leitej.util.MathUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JMathUtil {
	
	private static BigInteger[] factResults = new BigInteger[]{
		BigInteger.valueOf(1),
		BigInteger.valueOf(1),
		BigInteger.valueOf(2),
		BigInteger.valueOf(6),
		BigInteger.valueOf(24),
		BigInteger.valueOf(120),
		BigInteger.valueOf(720),
		BigInteger.valueOf(5040),
		BigInteger.valueOf(40320),
		BigInteger.valueOf(362880),
		BigInteger.valueOf(3628800),
		BigInteger.valueOf(39916800),
		BigInteger.valueOf(479001600),
		new BigInteger("6227020800",10),
		new BigInteger("87178291200",10),
		new BigInteger("1307674368000",10),
		new BigInteger("20922789888000",10),
		new BigInteger("355687428096000",10),
		new BigInteger("6402373705728000",10),
		new BigInteger("121645100408832000",10),
		new BigInteger("2432902008176640000",10)
	};

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_factorial() {
		final BigInteger[] results = new BigInteger[21];
		for (int i = 0; i <= 20; i++) {
			results[i] = MathUtil.factorial(i);
			System.out.println(i + "! = " + results[i]);
		}
		assertTrue(Arrays.equals(factResults, results));
	}

	@Test
	public void test_factorial2() {
		final BigInteger[] results = new BigInteger[21];
		final BigInteger top = BigInteger.valueOf(20);
		for (BigInteger i = BigInteger.ZERO; i.compareTo(top) <= 0; i = i.add(BigInteger.ONE)) {
			results[(int) (i.longValue())] = MathUtil.factorial(i);
			System.out.println(i + "! = " + results[(int) (i.longValue())]);
		}
		assertTrue(Arrays.equals(factResults, results));
	}

	@Test
	public void test_combination() {
		assertTrue(BigInteger.valueOf(455).equals(MathUtil.combination(15, 12)));
	}

	@Test
	public void test_combination2() {
		assertTrue(BigInteger.valueOf(455).equals(MathUtil.combination(BigInteger.valueOf(15), BigInteger.valueOf(3))));
	}

	@Test
	public void test_odd() {
		for (int i = -10; i < 11; i = i + 2) {
			assertTrue(MathUtil.isEven(i));
		}
		for (int i = -11; i < 10; i = i + 2) {
			assertTrue(MathUtil.isOdd(i));
		}
		for (long i = -10; i < 11; i = i + 2) {
			assertTrue(MathUtil.isEven(i));
		}
		for (long i = -11; i < 10; i = i + 2) {
			assertTrue(MathUtil.isOdd(i));
		}
	}

}

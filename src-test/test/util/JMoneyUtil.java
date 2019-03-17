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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.util.MoneyUtil;

public class JMoneyUtil {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_format_valid() {
		// CORRECT
		assertTrue(MoneyUtil.isEuroFormatValid("00"));
		assertTrue(MoneyUtil.isEuroFormatValid("0"));
		assertTrue(MoneyUtil.isEuroFormatValid("0,0"));
		assertTrue(MoneyUtil.isEuroFormatValid("0,00"));
		assertTrue(MoneyUtil.isEuroFormatValid("00,000"));
		assertTrue(MoneyUtil.isEuroFormatValid("00000,00000"));
		assertTrue(MoneyUtil.isEuroFormatValid("00.000,00000"));
		assertTrue(MoneyUtil.isEuroFormatValid("0 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("0€"));
		assertTrue(MoneyUtil.isEuroFormatValid("0,0€"));
		assertTrue(MoneyUtil.isEuroFormatValid("0,00€"));
		assertTrue(MoneyUtil.isEuroFormatValid("00000,00000€"));
		assertTrue(MoneyUtil.isEuroFormatValid("00.000,00000€"));
		assertTrue(MoneyUtil.isEuroFormatValid("0,00 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("0,01 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("0,12 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("1,23 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("12,34 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("123,45 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("1.234,56 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("1.234,56"));
		assertTrue(MoneyUtil.isEuroFormatValid("1.234,56\t \t   €"));
		assertTrue(MoneyUtil.isEuroFormatValid("12.345,67 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("12.345.678.901.234.567,89 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("12345678901234567,89 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("12.345678901234567,89 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("12345.678901234567,89 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("12345.678.901234567,89 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("12.345678901.234.567,89 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("12345678901.234.567,89 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("12,3456 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("-0,00 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("-0,01 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("-0,12 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("-1,23 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("-12,34 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("-123,45 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("-1.234,56 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("-12.345,67 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("-12.345.678.901.234.567,89 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("-12345678901234567,89 €"));
		assertTrue(MoneyUtil.isEuroFormatValid("-12,3456 €"));
		// INCORRECT
		assertFalse(MoneyUtil.isEuroFormatValid(""));
		assertFalse(MoneyUtil.isEuroFormatValid("0,-1 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("-.123,45 €"));
		assertFalse(MoneyUtil.isEuroFormatValid(".123,45 €"));
		assertFalse(MoneyUtil.isEuroFormatValid(",12 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("-,12 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("0,00 $"));
		assertFalse(MoneyUtil.isEuroFormatValid("--0,00 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("12,345.67 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("12,345,67 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("12..345,67 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("12.0.345,67 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("123.45 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("123,,45 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("12.345.67 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("123.45678901234567,89 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("123456.78901234567,89 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("1234567.890.1234567,89 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("1.2345678901234567,89 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("123.45678901234567,89 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("12,345.678 €"));
		assertFalse(MoneyUtil.isEuroFormatValid("12.345, €"));
		assertFalse(MoneyUtil.isEuroFormatValid("12.345,57 "));
		assertFalse(MoneyUtil.isEuroFormatValid(" 12.345,57"));
	}

	@Test
	public void test_format() {
		assertTrue(MoneyUtil.euroFormat(0).equals("0,00 €"));
		assertTrue(MoneyUtil.euroFormat(1).equals("0,01 €"));
		assertTrue(MoneyUtil.euroFormat(12).equals("0,12 €"));
		assertTrue(MoneyUtil.euroFormat(123).equals("1,23 €"));
		assertTrue(MoneyUtil.euroFormat(1234).equals("12,34 €"));
		assertTrue(MoneyUtil.euroFormat(12345).equals("123,45 €"));
		assertTrue(MoneyUtil.euroFormat(123456).equals("1.234,56 €"));
		assertTrue(MoneyUtil.euroFormat(1234567).equals("12.345,67 €"));
		assertTrue(MoneyUtil.euroFormat(1234567890123456789L).equals("12.345.678.901.234.567,89 €"));
		assertTrue(MoneyUtil.euroFormat(123456, 4).equals("12,3456 €"));
		assertTrue(MoneyUtil.euroFormat(-0).equals("0,00 €"));
		assertTrue(MoneyUtil.euroFormat(-1).equals("-0,01 €"));
		assertTrue(MoneyUtil.euroFormat(-12).equals("-0,12 €"));
		assertTrue(MoneyUtil.euroFormat(-123).equals("-1,23 €"));
		assertTrue(MoneyUtil.euroFormat(-1234).equals("-12,34 €"));
		assertTrue(MoneyUtil.euroFormat(-12345).equals("-123,45 €"));
		assertTrue(MoneyUtil.euroFormat(-123456).equals("-1.234,56 €"));
		assertTrue(MoneyUtil.euroFormat(-1234567).equals("-12.345,67 €"));
		assertTrue(MoneyUtil.euroFormat(-1234567890123456789L).equals("-12.345.678.901.234.567,89 €"));
		assertTrue(MoneyUtil.euroFormat(-123456, 4).equals("-12,3456 €"));
	}

	@Test(expected = IllegalArgumentLtRtException.class)
	public void test_invalid() {
		MoneyUtil.euroFormat(123456, -1);
	}

	@Test
	public void test_parse_value() {
		assertTrue(MoneyUtil.euroValue("0,00 €", false) == 0);
		assertTrue(MoneyUtil.euroValue("0,01 €", false) == 1);
		assertTrue(MoneyUtil.euroValue("0,12 €", false) == 12);
		assertTrue(MoneyUtil.euroValue("1,23 €", false) == 123);
		assertTrue(MoneyUtil.euroValue("12,34 €", false) == 1234);
		assertTrue(MoneyUtil.euroValue("123,45 €", false) == 12345);
		assertTrue(MoneyUtil.euroValue("1.234,56 €", false) == 123456);
		assertTrue(MoneyUtil.euroValue("12.345,67 €", false) == 1234567);
		assertTrue(MoneyUtil.euroValue("1.345.678.901.234.567,89 €", false) == 134567890123456789L);
		assertTrue(MoneyUtil.euroValue("12,3456 €", false, 4) == 123456);
		assertTrue(MoneyUtil.euroValue("-0,00 €", false) == -0);
		assertTrue(MoneyUtil.euroValue("-0,01 €", false) == -1);
		assertTrue(MoneyUtil.euroValue("-0,12 €", false) == -12);
		assertTrue(MoneyUtil.euroValue("-1,23 €", false) == -123);
		assertTrue(MoneyUtil.euroValue("-12,34 €", false) == -1234);
		assertTrue(MoneyUtil.euroValue("-123,45 €", false) == -12345);
		assertTrue(MoneyUtil.euroValue("-1.234,56 €", false) == -123456);
		assertTrue(MoneyUtil.euroValue("-12.345,67 €", false) == -1234567);
		assertTrue(MoneyUtil.euroValue("-1.345.678.901.234.567,89 €", false) == -134567890123456789L);
		assertTrue(MoneyUtil.euroValue("-12,3456 €", false, 4) == -123456);
		assertTrue(MoneyUtil.euroValue("12 €", false, 4) == 120000);
		assertTrue(MoneyUtil.euroValue("-12 €", false, 4) == -120000);
		assertTrue(MoneyUtil.euroValue("12,3456 €", false) == 1234);
		assertTrue(MoneyUtil.euroValue("1.234,56 €", false, 0) == 1234);
		assertTrue(MoneyUtil.euroValue("1.234,56 €", false, 1) == 12345);
		assertTrue(MoneyUtil.euroValue("1.234,56 €", false, 2) == 123456);
		assertTrue(MoneyUtil.euroValue("1.234,56 €", false, 3) == 1234560);
		assertTrue(MoneyUtil.euroValue("12,3456 €", true) == 1235);
		assertTrue(MoneyUtil.euroValue("1.234,56 €", true, 0) == 1235);
		assertTrue(MoneyUtil.euroValue("1.234,56 €", true, 1) == 12346);
		assertTrue(MoneyUtil.euroValue("1.234,56 €", true, 2) == 123456);
		assertTrue(MoneyUtil.euroValue("1.234,56 €", true, 3) == 1234560);
		assertTrue(MoneyUtil.euroValue("1.234,5 €", true, 1) == 12345);
		assertTrue(MoneyUtil.euroValue("1.234,50 €", true, 1) == 12345);
		assertTrue(MoneyUtil.euroValue("1.234,51 €", true, 1) == 12345);
		assertTrue(MoneyUtil.euroValue("1.234,52 €", true, 1) == 12345);
		assertTrue(MoneyUtil.euroValue("1.234,53 €", true, 1) == 12345);
		assertTrue(MoneyUtil.euroValue("1.234,54 €", true, 1) == 12345);
		assertTrue(MoneyUtil.euroValue("1.234,55 €", true, 1) == 12346);
		assertTrue(MoneyUtil.euroValue("1.234,56 €", true, 1) == 12346);
		assertTrue(MoneyUtil.euroValue("1.234,57 €", true, 1) == 12346);
		assertTrue(MoneyUtil.euroValue("1.234,58 €", true, 1) == 12346);
		assertTrue(MoneyUtil.euroValue("1.234,59 €", true, 1) == 12346);
		assertTrue(MoneyUtil.euroValue("1.234,509 €", true, 1) == 12345);
		assertTrue(MoneyUtil.euroValue("1.234,549 €", true, 1) == 12345);
		assertTrue(MoneyUtil.euroValue("1.234,554 €", true, 1) == 12346);
		assertTrue(MoneyUtil.euroValue("1.234,549 €", true, 6) == 1234549000);
	}

	@Test(expected = IllegalArgumentLtRtException.class)
	public void test_parse_big_value1() {
		MoneyUtil.euroValue("1.234,549 €", false, 17);
	}

	@Test(expected = IllegalArgumentLtRtException.class)
	public void test_parse_big_value2() {
		assertTrue(MoneyUtil.euroValue("0.999.999.999.999.999.999 €", true, 0) == 999999999999999999L);
		assertTrue(MoneyUtil.euroValue("0.999.999.999.999.999.999,9 €", true, 0) == 1000000000000000000L);
		MoneyUtil.euroValue("9.999.999.999.999.999.999 €", true, 0);
	}

	@Test(expected = IllegalArgumentLtRtException.class)
	public void test_parse_big_value3() {
		assertTrue(MoneyUtil.euroValue("9.223.372.036.854.775.807 €", true, 0) == 9223372036854775807L);
		assertTrue(MoneyUtil.euroValue("9.223.372.036.854.775,807 €", true, 3) == 9223372036854775807L);
		MoneyUtil.euroValue("9.223.372.036.854.775,8075 €", true, 3);
	}

	@Test(expected = IllegalArgumentLtRtException.class)
	public void test_parse_big_value4() {
		assertTrue(MoneyUtil.euroValue("-9.223.372.036.854.775.808 €", true, 0) == -9223372036854775808L);
		assertTrue(MoneyUtil.euroValue("-9.223.372.036.854.775,808 €", true, 3) == -9223372036854775808L);
		MoneyUtil.euroValue("-9.223.372.036.854.775,8085 €", true, 3);
	}

}

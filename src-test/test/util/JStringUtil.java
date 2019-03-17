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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.util.StringUtil;

public class JStringUtil {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_isNullOrEmpty() {
		assertTrue(StringUtil.isNullOrEmpty(null));
		assertTrue(StringUtil.isNullOrEmpty(""));
		assertTrue(StringUtil.isNullOrEmpty(" "));
		assertTrue(StringUtil.isNullOrEmpty("\t"));
		assertTrue(StringUtil.isNullOrEmpty("   "));
		assertTrue(StringUtil.isNullOrEmpty(" \t"));
		assertTrue(!StringUtil.isNullOrEmpty("a"));
	}

	@Test
	public void test_firstCharacter() {
		final StringBuilder sb = new StringBuilder();
		final StringBuffer sbf = new StringBuffer();

		try {
			StringUtil.firstCharacterToUpperCase((StringBuilder) null);
			fail();
		} catch (final NullPointerException e) {
		}
		try {
			StringUtil.firstCharacterToUpperCase((StringBuffer) null);
			fail();
		} catch (final NullPointerException e) {
		}
		try {
			StringUtil.firstCharacterToLowerCase((StringBuilder) null);
			fail();
		} catch (final NullPointerException e) {
		}
		try {
			StringUtil.firstCharacterToLowerCase((StringBuffer) null);
			fail();
		} catch (final NullPointerException e) {
		}

		sb.append("abcd aASDF");
		sbf.append("abcd aASDF");
		assertTrue(StringUtil.firstCharacterToUpperCase(sb).toString().equals("Abcd aASDF"));
		assertTrue(StringUtil.firstCharacterToUpperCase(sbf).toString().equals("Abcd aASDF"));
		assertTrue(StringUtil.firstCharacterToLowerCase(sb).toString().equals("abcd aASDF"));
		assertTrue(StringUtil.firstCharacterToLowerCase(sbf).toString().equals("abcd aASDF"));

		sb.setLength(0);
		sbf.setLength(0);
		sb.append("AAbcd aASDF");
		sbf.append("AAbcd aASDF");
		assertTrue(StringUtil.firstCharacterToUpperCase(sb).toString().equals("AAbcd aASDF"));
		assertTrue(StringUtil.firstCharacterToUpperCase(sbf).toString().equals("AAbcd aASDF"));
		assertTrue(StringUtil.firstCharacterToLowerCase(sb).toString().equals("aAbcd aASDF"));
		assertTrue(StringUtil.firstCharacterToLowerCase(sbf).toString().equals("aAbcd aASDF"));

		sb.setLength(0);
		sbf.setLength(0);
		sb.append(" bcd aASDF");
		sbf.append(" bcd aASDF");
		assertTrue(StringUtil.firstCharacterToUpperCase(sb).toString().equals(" bcd aASDF"));
		assertTrue(StringUtil.firstCharacterToUpperCase(sbf).toString().equals(" bcd aASDF"));
		assertTrue(StringUtil.firstCharacterToLowerCase(sb).toString().equals(" bcd aASDF"));
		assertTrue(StringUtil.firstCharacterToLowerCase(sbf).toString().equals(" bcd aASDF"));
	}

	@Test
	public void test_insertObjects() {
		assertTrue(StringUtil.insertObjects(null) == null);
		assertTrue(StringUtil.insertObjects("").equals(""));
		assertTrue(StringUtil.insertObjects("e oru#0agre#123we fw").equals("e oru#0agre#123we fw"));

		assertTrue(StringUtil.insertObjects(null, (Object[]) null) == null);
		assertTrue(StringUtil.insertObjects("", (Object[]) null).equals(""));
		assertTrue(StringUtil.insertObjects("e oru#0agre#123we fw", (Object[]) null).equals("e oru#0agre#123we fw"));

		assertTrue(StringUtil.insertObjects(null, new Object[] {}) == null);
		assertTrue(StringUtil.insertObjects("", new Object[] {}).equals(""));
		assertTrue(StringUtil.insertObjects("e oru#0agre#123we fw", new Object[] {}).equals("e oru#0agre#123we fw"));

		assertTrue(StringUtil.insertObjects(null, new Object()) == null);
		assertTrue(StringUtil.insertObjects("", new Object()).equals(""));
		assertTrue(StringUtil.insertObjects("e oru#0agre#123we fw", ":)").equals("e oru:)agre#123we fw"));

		assertTrue(StringUtil.insertObjects(null, new Object[] { ":)", ":D" }) == null);
		assertTrue(StringUtil.insertObjects("", new Object[] { ":)", ":D" }).equals(""));
		assertTrue(StringUtil.insertObjects("as#dqw21fs#fq123jhb 1# khbe2#", new Object[] { ":)", ":D" })
				.equals("as#dqw21fs#fq123jhb 1# khbe2#"));
		assertTrue(StringUtil.insertObjects("as#dqw21fs#fq123jhb #1# khbe2#", new Object[] { ":)", ":D" })
				.equals("as#dqw21fs#fq123jhb :D# khbe2#"));
		assertTrue(StringUtil.insertObjects("14424123", new Object[] { ":)", ":D" }).equals("14424123"));
		assertTrue(StringUtil.insertObjects("e oru#0agre#123we fw", new Object[] { ":)", ":D" })
				.equals("e oru:)agre#123we fw"));

		final Object[] tmp = new Object[124];
		tmp[0] = ":)";
		tmp[123] = ":D";
		assertTrue(StringUtil.insertObjects("e oru#0agre#123we fw", tmp).equals("e oru:)agre:Dwe fw"));
	}

	@Test
	public void test_concatObjects() {
		assertTrue(StringUtil.concatObjects(null, null, null) == null);
		assertTrue(StringUtil.concatObjects("", null, null).equals(""));
		assertTrue(StringUtil.concatObjects("e oru#agre#123we fw", "#", null).equals("e oru#agre#123we fw"));

		assertTrue(StringUtil.concatObjects(null, "#", (Object[]) null) == null);
		assertTrue(StringUtil.concatObjects("", "#", (Object[]) null).equals(""));
		assertTrue(StringUtil.concatObjects("e oru#agre#123we fw", "#", (Object[]) null).equals("e oru#agre#123we fw"));

		assertTrue(StringUtil.concatObjects(null, "#", new Object[] {}) == null);
		assertTrue(StringUtil.concatObjects("", "#", new Object[] {}).equals(""));
		assertTrue(StringUtil.concatObjects("", "#", new Object[] { ":)" }).equals(""));
		assertTrue(StringUtil.concatObjects("e oru#agre#123we fw", "#", new Object[] {}).equals("e oru#agre#123we fw"));

		assertTrue(StringUtil.concatObjects("e oru#agre#123we fw", "#", new Object[] { ":)" })
				.equals("e oru:)agre#123we fw"));

		assertTrue(StringUtil.concatObjects(null, "#", new Object[] { ":)", ":D" }) == null);
		assertTrue(StringUtil.concatObjects("", "#", new Object[] { ":)", ":D" }).equals(""));
		assertTrue(StringUtil.concatObjects("as#dqw21fs#fq123jhb 1# khbe2", "#", new Object[] { ":)", ":D" })
				.equals("as:)dqw21fs:Dfq123jhb 1# khbe2"));
		assertTrue(StringUtil.concatObjects("##as#dqw21fs#fq123jhb 1### khbe2##", "#", new Object[] { ":)", ":D" })
				.equals(":):Das#dqw21fs#fq123jhb 1### khbe2##"));
		assertTrue(StringUtil.concatObjects("asdqw21fsfq123jhb #1## khbe2##", "#", new Object[] { ":)", ":D" })
				.equals("asdqw21fsfq123jhb :)1:D# khbe2##"));
		assertTrue(StringUtil.concatObjects("14424123", "#", new Object[] { ":)", ":D" }).equals("14424123"));
		assertTrue(StringUtil.concatObjects("e oru#agre#123we fw", "#", new Object[] { ":)", ":D" })
				.equals("e oru:)agre:D123we fw"));

	}

	@Test
	public void test_char_byte() {
		try {
			StringUtil.toCharArray(null);
			fail();
		} catch (final NullPointerException e) {
		}
		assertTrue(String.valueOf(StringUtil.toCharArray(new byte[] {})).equals(""));
		try {
			StringUtil.toCharArray(new byte[] { 'o', 'l', 'a' });
			fail();
		} catch (final IllegalArgumentLtRtException e) {
		}
		final Random rnd = new Random();
		final byte[] buffer = new byte[1024];
		rnd.nextBytes(buffer);
		assertTrue(Arrays.equals(StringUtil.toByteArray(StringUtil.toCharArray(buffer)), buffer));
		assertTrue(Arrays.equals(StringUtil.toByteArray(String.valueOf(StringUtil.toCharArray(buffer))), buffer));
	}

	@Test
	public void test_isEquals() {
		assertTrue(StringUtil.isEquals(null, null));
		assertTrue(!StringUtil.isEquals("", null));
		assertTrue(!StringUtil.isEquals(null, ""));
		assertTrue(StringUtil.isEquals("", ""));
		assertTrue(StringUtil.isEquals("", new StringBuilder()));
		assertTrue(StringUtil.isEquals("asd", new StringBuilder("asd")));
		assertTrue(!StringUtil.isEquals("asd", new StringBuilder("as")));
	}

}

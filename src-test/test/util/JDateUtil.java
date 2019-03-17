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

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.util.DateUtil;
import leitej.util.data.DateFieldEnum;

public class JDateUtil {

	private final String f;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	public JDateUtil() {
		final StringBuffer sb = new StringBuffer();
		sb.append(DateUtil.KEY_FORMAT_YEAR);
		sb.append("-");
		sb.append(DateUtil.KEY_FORMAT_MONTH);
		sb.append("-");
		sb.append(DateUtil.KEY_FORMAT_DAY_IN_MONTH);
		sb.append(" ");
		sb.append(DateUtil.KEY_FORMAT_HOUR);
		sb.append(":");
		sb.append(DateUtil.KEY_FORMAT_MINUTE);
		sb.append(":");
		sb.append(DateUtil.KEY_FORMAT_SECOND);
		this.f = sb.toString();
	}

	@Test
	public final void test_format() {
		assertTrue("2010-11-30 17:05:12".equals(DateUtil.format(DateUtil.newDate(2010, 11, 30, 17, 05, 12), this.f)));
	}

	@Test
	public final void test_get_set_field() {
		final Date d = DateUtil.newDate(2010, 11, 30, 17, 05, 12);
		assertTrue(DateUtil.get(d, DateFieldEnum.MONTH) == 11);
		DateUtil.set(d, DateFieldEnum.MONTH, 2);
		assertTrue(DateUtil.get(d, DateFieldEnum.MONTH) == 3); // 30-2-2010 goes to 02-3-2010
		DateUtil.set(d, DateFieldEnum.MONTH, 2);
		assertTrue(DateUtil.get(d, DateFieldEnum.MONTH) == 2);
		assertTrue(DateUtil.get(d, DateFieldEnum.DAY_OF_MONTH) == 2);
		DateUtil.set(d, DateFieldEnum.DAY_OF_MONTH, 4);
		assertTrue(DateUtil.get(d, DateFieldEnum.DAY_OF_MONTH) == 4);
		DateUtil.set(d, DateFieldEnum.MONTH, 1);
		assertTrue(DateUtil.get(d, DateFieldEnum.WEEK_OF_YEAR) == 2); // Mon Jan 04 17:05:12 WET 2010
	}

	@Test
	public final void test_zero_till() {
		final Date d = DateUtil.newDate(2010, 11, 30, 17, 05, 12);
		DateUtil.zeroTill(d, DateFieldEnum.DAY_OF_MONTH);
		assertTrue("2010-11-30 00:00:00".equals(DateUtil.format(d, this.f)));
		DateUtil.zeroTill(d, DateFieldEnum.MONTH);
		assertTrue("2010-11-01 00:00:00".equals(DateUtil.format(d, this.f)));
		DateUtil.zeroTill(d, DateFieldEnum.YEAR);
		assertTrue("2010-01-01 00:00:00".equals(DateUtil.format(d, this.f)));
	}

	@Test
	public final void test_WEEK_OF_YEAR() {
		final Date d = DateUtil.newDate(2007, 12, 31);
		assertTrue(DateUtil.get(d, DateFieldEnum.WEEK_OF_YEAR) == 53);
		DateUtil.set(d, DateFieldEnum.WEEK_OF_YEAR, 52);
		assertTrue("2007-12-24 00:00:00".equals(DateUtil.format(d, this.f)));
		DateUtil.set(d, DateFieldEnum.WEEK_OF_YEAR, 53);
		assertTrue("2007-12-31 00:00:00".equals(DateUtil.format(d, this.f)));
		assertTrue(DateUtil.get(d, DateFieldEnum.WEEK_OF_YEAR) == 53);
		DateUtil.set(d, DateFieldEnum.WEEK_OF_YEAR, 54);
		assertTrue("2008-01-07 00:00:00".equals(DateUtil.format(d, this.f)));
		assertTrue(DateUtil.get(d, DateFieldEnum.WEEK_OF_YEAR) == 2);
		DateUtil.set(d, DateFieldEnum.WEEK_OF_YEAR, 1);
		assertTrue("2007-12-31 00:00:00".equals(DateUtil.format(d, this.f)));
		assertTrue(DateUtil.get(d, DateFieldEnum.WEEK_OF_YEAR) == 53);
		DateUtil.add(d, DateFieldEnum.DAY_OF_MONTH, 1);
		assertTrue("2008-01-01 00:00:00".equals(DateUtil.format(d, this.f)));
		assertTrue(DateUtil.get(d, DateFieldEnum.WEEK_OF_YEAR) == 1);

	}

}

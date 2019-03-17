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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.util.MainUtil;

public class JMainUtil {

	private String[] args;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		assertTrue(MainUtil.getOption(null, null) == null);
		assertTrue(MainUtil.getOption(null, "") == null);
		assertTrue(MainUtil.getOption(null, " ") == null);
		assertTrue(MainUtil.getOption(null, "-a") == null);

		this.args = new String[] {};
		assertTrue(MainUtil.getOption(this.args, null) == null);
		assertTrue(MainUtil.getOption(this.args, "") == null);
		assertTrue(MainUtil.getOption(null, " ") == null);

		this.args = new String[] { "-a asc" };
		assertTrue(MainUtil.getOption(this.args, null) == null);
		assertTrue(MainUtil.getOption(this.args, "") == null);
		assertTrue(MainUtil.getOption(null, " ") == null);
		assertTrue(MainUtil.getOption(this.args, "-b") == null);
		assertTrue(MainUtil.getOption(this.args, "-a").equals(" asc"));
		assertTrue(MainUtil.getOption(this.args, "-a ").equals("asc"));

		this.args = new String[] { "", "afawe", "   ", "-bqwerty", "-a asc", "-?" };
		assertTrue(MainUtil.getOption(this.args, null) == null);
		assertTrue(MainUtil.getOption(this.args, "") == null);
		assertTrue(MainUtil.getOption(null, " ") == null);
		assertTrue(MainUtil.getOption(this.args, "-b").equals("qwerty"));
		assertTrue(MainUtil.getOption(this.args, "-a").equals(" asc"));
		assertTrue(MainUtil.getOption(this.args, "-").equals("bqwerty"));
		assertTrue(MainUtil.getOption(this.args, "-?").equals(""));
		assertTrue(MainUtil.getOption(this.args, "-a asc").equals(""));
	}

}

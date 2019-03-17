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

import leitej.util.data.SyncCounter;

public class JSyncCounter {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		SyncCounter sc;
		try {
			sc = new SyncCounter(0, 0, -1);
			assertTrue(false);
		} catch (final IllegalArgumentException e) {
			assertTrue(true);
		}
		try {
			sc = new SyncCounter(0, -1, 0);
			assertTrue(false);
		} catch (final IllegalArgumentException e) {
			assertTrue(true);
		}
		try {
			sc = new SyncCounter(1, 0, 0);
			assertTrue(false);
		} catch (final IllegalArgumentException e) {
			assertTrue(true);
		}
		try {
			sc = new SyncCounter(1, 1, 0);
			assertTrue(false);
		} catch (final IllegalArgumentException e) {
			assertTrue(true);
		}
		try {
			sc = new SyncCounter(0, 1, 0);
			assertTrue(false);
		} catch (final IllegalArgumentException e) {
			assertTrue(true);
		}
		sc = new SyncCounter(0, 0, 0);
		assertTrue(!sc.inc());
		assertTrue(!sc.dec());
		sc = new SyncCounter(0, 0, 1);
		assertTrue(!sc.dec());
		assertTrue(sc.inc());
		assertTrue(!sc.inc());
		assertTrue(sc.dec());
		assertTrue(!sc.dec());
	}

}

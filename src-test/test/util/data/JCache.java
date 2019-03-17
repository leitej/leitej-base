/*******************************************************************************
 * Copyright (C) 2018 Julio Leite
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

package test.util.data;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import leitej.util.data.Cache;
import leitej.util.data.CacheSoft;
import leitej.util.machine.HeapMemoryUsageWarn;
import leitej.util.machine.HeapMemoryUsageWarn.Listener;

/**
 * @author julio
 *
 */
public class JCache {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	static boolean loop = true;

	@Test
	public void test() throws InterruptedException {
		final Cache<String, Object> cache = new CacheSoft<>();
		final double factor = 0.98;
		HeapMemoryUsageWarn.setPercentageUsageThreshold(factor);
		final HeapListener hl = new HeapListener();
		HeapMemoryUsageWarn.addListener(hl);
		long count = 0;
		while (loop) {
			cache.set(String.valueOf(count), new byte[1024]);
			count++;
		}
		System.out.println(count);
		System.out.println(cache.reindex());
		final Map<String, Object> tmp = new HashMap<>();
		for (int i = 0; i < (count / 2); i++) {
			tmp.put(String.valueOf(i), new byte[1024]);
		}
		System.out.println(cache.reindex());
		for (int i = 0; i < count; i++) {
			cache.set(String.valueOf(i + count), new byte[1024]);
		}
		assertTrue(tmp.get(123 + "") != null);
		assertTrue(cache.get(123 + "") == null);
	}

	public class HeapListener implements Listener {

		@Override
		public void memoryUsageLow(final long usedMemory, final long maxMemory) {
			loop = false;
		}

	}
}

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

package test.util.machine;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.util.machine.HeapMemoryUsageWarn;

public class JHeapMemoryUsageWarn {

	private static volatile boolean stop = false;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		HeapMemoryUsageWarn.setPercentageUsageThreshold(0.7);

		HeapMemoryUsageWarn.addListener(new HeapMemoryUsageWarn.Listener() {
			@Override
			public void memoryUsageLow(final long usedMemory, final long maxMemory) {
				stop = true;
				System.out.println("Memory usage low!!!");
				final double percentageUsed = ((double) usedMemory) / maxMemory;
				System.out.println("usedMemory = " + usedMemory);
				System.out.println("maxMemory = " + maxMemory);
				System.out.println("percentageUsed = " + percentageUsed);
				// MemoryWarningSystem.setPercentageUsageThreshold(0.8);
			}
		});

		final Collection<Double> numbers = new LinkedList<>();
		while (!stop) {
			numbers.add(Math.random());
		}
		System.out.println("stopped ;)");
	}

}

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

package test.util.data;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import leitej.util.data.AbstractWeakQueue;
import leitej.util.machine.VMMonitor;

/**
 * @author julio
 *
 */
public class JAbstractWeakQueue {

	private final static int SIZE_BLOCK;
	static {
		System.out.println(Long.valueOf(Integer.MAX_VALUE).longValue());
		System.out.println(VMMonitor.heapMemoryUsage().getMax());
		if (Long.valueOf(Integer.MAX_VALUE).longValue() < VMMonitor.heapMemoryUsage().getMax()) {
			throw new IllegalStateException();
		}
		SIZE_BLOCK = Long.valueOf(VMMonitor.heapMemoryUsage().getMax()).intValue() / 10;
		System.out.println("SIZE_BLOCK " + SIZE_BLOCK);
	}

	@Test
	public void test() {
		System.out.println("MAX_HEAP " + VMMonitor.heapMemoryUsage().getMax());
		System.gc();
		System.out.println(VMMonitor.heapMemoryUsage().getUsed() + "## ");
		final Object[] tmp1 = new Object[6];
		final AWQtest test1 = new AWQtest();
		final AWQtest test2 = new AWQtest();
		for (int i = 0; i < tmp1.length; i++) {
			tmp1[i] = test1.poll();
		}
		System.out.println(VMMonitor.heapMemoryUsage().getUsed());
		for (int i = 0; i < 8; i++) {
			System.out.println(VMMonitor.heapMemoryUsage().getUsed());
			test2.poll();
		}
		System.gc();
		System.out.println(VMMonitor.heapMemoryUsage().getUsed() + "## ");
		for (int i = 0; i < 8; i++) {
			System.out.println(VMMonitor.heapMemoryUsage().getUsed());
			assertTrue(test1.offer(test1.poll()));
		}
		for (int i = 0; i < tmp1.length; i++) {
			assertTrue(test1.offer(tmp1[i]));
			tmp1[i] = null;
		}
		System.gc();
		System.out.println(VMMonitor.heapMemoryUsage().getUsed() + "## ");
		for (int i = 0; i < 8; i++) {
			System.out.println(VMMonitor.heapMemoryUsage().getUsed());
			assertTrue(test1.offer(test2.poll()));
		}
		System.gc();
		System.out.println(VMMonitor.heapMemoryUsage().getUsed() + "## ");
		for (int i = 0; i < 8; i++) {
			System.out.println(VMMonitor.heapMemoryUsage().getUsed());
			test1.poll();
		}
		System.gc();
		System.out.println(VMMonitor.heapMemoryUsage().getUsed() + "## ");
		assertTrue(true);
	}

	private class AWQtest extends AbstractWeakQueue<Object> {

		private static final long serialVersionUID = -5763064608019629736L;

		@Override
		protected Object newObject() {
			return new Object() {
				@SuppressWarnings("unused")
				private final byte[] garb = new byte[SIZE_BLOCK];
			};
		}

	}

}

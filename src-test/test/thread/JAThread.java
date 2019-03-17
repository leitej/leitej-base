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

package test.thread;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Array;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.exception.AgnosticThreadLtException;
import leitej.exception.LtException;
import leitej.thread.AgnosticThread;
import leitej.thread.ThreadData;
import leitej.util.AgnosticUtil;
import leitej.util.data.Invoke;
import leitej.util.data.InvokeItf;

public class JAThread {

	private Sample sample;

	@Before
	public void setUp() throws Exception {
		this.sample = new Sample();
	}

	@After
	public void tearDown() throws Exception {
		this.sample.reset();
	}

	@Test(timeout = 2000)
	public void testAThread_1() {
		try {
			final AgnosticThread athread = new AgnosticThread(
					new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "hitScount")));
			try {
				athread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
				fail(e.toString());
			}
			assertTrue(Sample.getScount().equals(1L));
		} catch (final SecurityException e) {
			e.printStackTrace();
			fail(e.toString());
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test(timeout = 2000)
	public void testAThread_2() {
		try {
			final AgnosticThread athread1 = new AgnosticThread(
					new ThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "hitScount"))));
			final AgnosticThread athread2 = new AgnosticThread(
					new ThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "hitScount"))));
			try {
				athread1.join();
				athread2.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
				fail(e.toString());
			}
			assertTrue(Sample.getScount().equals(2L));
		} catch (final SecurityException e) {
			e.printStackTrace();
			fail(e.toString());
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test(timeout = 2000)
	public void testAThread_collision() {
		try {
			final AgnosticThread athread1 = new AgnosticThread(
					new ThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "collisionTest"))));
			final AgnosticThread athread2 = new AgnosticThread(
					new ThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "collisionTest"))));
			try {
				athread1.join();
				athread2.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
				fail(e.toString());
			}
			assertTrue(Sample.getAssync().equals(Boolean.TRUE));
		} catch (final SecurityException e) {
			e.printStackTrace();
			fail(e.toString());
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test(timeout = 3000)
	public void testAThread_collision_not() {
		try {
			final AgnosticThread athread1 = new AgnosticThread(
					new ThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "synchronizedTest"))));
			final AgnosticThread athread2 = new AgnosticThread(
					new ThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "synchronizedTest"))));
			try {
				athread1.join();
				athread2.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
				fail(e.toString());
			}
			assertTrue(Sample.getAssync().equals(Boolean.FALSE));
		} catch (final SecurityException e) {
			e.printStackTrace();
			fail(e.toString());
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test(expected = AgnosticThreadLtException.class)
	public void testAThread_AgnosticThreadException() throws LtException {
		try {
			final ThreadData td = new ThreadData(
					new Invoke(this.sample, AgnosticUtil.getMethod(this.sample, "sleep2sec")));
			final AgnosticThread athread = new AgnosticThread(td);
			athread.workOn(td);
			try {
				athread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
				fail(e.toString());
			}
		} catch (final SecurityException e) {
			e.printStackTrace();
			fail(e.toString());
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test(timeout = 10000)
	public void testAThread_o10() throws InterruptedException {
		final AgnosticThread[] ats = (AgnosticThread[]) Array.newInstance(AgnosticThread.class, 10);
		try {
			for (int i = 0; i < ats.length; i++) {
				ats[i] = new AgnosticThread(
						new ThreadData(new Invoke(this.sample, AgnosticUtil.getMethod(this.sample, "hitCount"))));
			}
			try {
				for (int i = 0; i < ats.length; i++) {
					ats[i].join();
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
				fail(e.toString());
			}
			final ThreadData td = new ThreadData(
					new Invoke(this.sample, AgnosticUtil.getMethod(this.sample, "getCountDelay5")));
			new AgnosticThread(td);
			assertTrue(((Integer) td.getBlockingResult()).equals(10));
		} catch (final SecurityException e) {
			e.printStackTrace();
			fail(e.toString());
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test(timeout = 100000)
	public void testAThread_o1000() throws InterruptedException {
		final int length = 1000;
		final AgnosticThread[] ats = (AgnosticThread[]) Array.newInstance(AgnosticThread.class, length);
		try {
			final InvokeItf invoke = new Invoke(this.sample,
					AgnosticUtil.getMethod(this.sample, "hitCountAssyncDelay2"));
			for (int i = 0; i < ats.length; i++) {
				ats[i] = new AgnosticThread(invoke);
			}
			try {
				for (int i = 0; i < ats.length; i++) {
					ats[i].join();
					assertTrue(ats[i].isTerminated());
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
				fail(e.toString());
			}
			if (this.sample.getCount() != length) {
				fail(String.valueOf(this.sample.getCount()));
			}
			assertTrue(true);
		} catch (final SecurityException e) {
			e.printStackTrace();
			fail(e.toString());
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test
	public void testInterruptedException() {
		Thread.currentThread().interrupt();
		try {
			Thread.sleep(5000);
			fail();
		} catch (final InterruptedException e) {
		}
		assertTrue(true);
	}

}

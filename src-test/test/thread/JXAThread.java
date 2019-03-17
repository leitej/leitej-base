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

import java.lang.reflect.Array;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.exception.LtException;
import leitej.log.Logger;
import leitej.thread.ThreadData;
import leitej.thread.XAgnosticThread;
import leitej.thread.XThreadData;
import leitej.util.AgnosticUtil;
import leitej.util.data.DateFieldEnum;
import leitej.util.data.DateTimer;
import leitej.util.data.Invoke;
import leitej.util.data.InvokeItf;
import leitej.util.data.Stopwatch;

public class JXAThread {

	private static final Logger LOG = Logger.getInstance();

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
	public void testXAThread_1() throws SecurityException, NoSuchMethodException, LtException, InterruptedException {
		final XAgnosticThread xathread = new XAgnosticThread(
				new ThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "hitScount"))));
		xathread.join();
		assertTrue(Sample.getScount().equals(1L));
	}

	@Test(timeout = 2000)
	public void testXAThread_X1() throws SecurityException, NoSuchMethodException, LtException, InterruptedException {
		final XAgnosticThread xathread = new XAgnosticThread(
				new XThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "hitScount"))));
		Thread.sleep(1000);
		xathread.close();
		assertTrue(Sample.getScount().equals(1L));
	}

	@Test(timeout = 2000)
	public void testXAThread_X_1() throws SecurityException, NoSuchMethodException, LtException, InterruptedException {
		final XAgnosticThread xathread = new XAgnosticThread(false);
		xathread.workOn(new XThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "hitScount"))));
		xathread.join();
		assertTrue(Sample.getScount().equals(1L));
	}

	@Test(timeout = 10000)
	public void testXAThread_X1_Delay6_Rep1()
			throws SecurityException, NoSuchMethodException, LtException, InterruptedException {
		final XAgnosticThread xathread = new XAgnosticThread(true);
		xathread.start();
		Thread.sleep(6000);
		xathread.workOn(new XThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "hitScount")),
				new DateTimer(DateFieldEnum.MILLISECOND, 1000, 1)));
		Thread.sleep(1500);
		xathread.close();
		assertTrue(Sample.getScount().equals(1L));
	}

	@Test(timeout = 10000)
	public void testXAThread_X1_Delay8_Rep1()
			throws SecurityException, NoSuchMethodException, LtException, InterruptedException {
		final XAgnosticThread xathread = new XAgnosticThread(true);
		xathread.workOn(new XThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "hitScount")),
				new DateTimer(DateFieldEnum.MILLISECOND, 8000, 1)));
		Thread.sleep(9000);
		xathread.close();
		assertTrue(Sample.getScount().equals(1L));
	}

	@Test(timeout = 2000)
	public void testXAThread_X1_Rep2()
			throws SecurityException, NoSuchMethodException, LtException, InterruptedException {
		final XAgnosticThread xathread = new XAgnosticThread(true);
		xathread.workOn(new XThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "hitScount")),
				new DateTimer(DateFieldEnum.MILLISECOND, 500, 2)));
		Thread.sleep(1500);
		xathread.close();
		assertTrue(Sample.getScount().equals(2L));
	}

	@Test(timeout = 4000)
	public void testXAThread_X1_Rep1()
			throws SecurityException, NoSuchMethodException, LtException, InterruptedException {
		final XAgnosticThread xathread = new XAgnosticThread(true);
		xathread.workOn(new XThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "hitScount")),
				new DateTimer(DateFieldEnum.SECOND, 2, 1)));
		Thread.sleep(1000);
		xathread.close();
		assertTrue(Sample.getScount().equals(0L));
	}

	@Test(timeout = 4000)
	public void testXAThread_X1_Rep1_work()
			throws SecurityException, NoSuchMethodException, LtException, InterruptedException {
		final XAgnosticThread xathread = new XAgnosticThread(true);
		xathread.workOn(new XThreadData(new Invoke(Sample.class, AgnosticUtil.getMethod(Sample.class, "hitScount")),
				new DateTimer(DateFieldEnum.SECOND, 2, 1)));
		Thread.sleep(3000);
		xathread.close();
		assertTrue(Sample.getScount().equals(1L));
	}

	@Test(timeout = 100000)
	public void testXAThread_X1000_Rep2()
			throws SecurityException, NoSuchMethodException, LtException, InterruptedException {
		final Stopwatch sw = Stopwatch.getInstance("testXAThread_X1000_Rep2");
		sw.start("init");
		final int n = 1000;
		final XAgnosticThread[] xat = (XAgnosticThread[]) Array.newInstance(XAgnosticThread.class, n);
		for (int i = 0; i < n; i++) {
			xat[i] = new XAgnosticThread(true);
			xat[i].start();
		}
		sw.step("instantied and start " + n);
		final InvokeItf invoke = new Invoke(this.sample, AgnosticUtil.getMethod(this.sample, "hitCount"));
		for (int i = 0; i < n; i++) {
			xat[i].workOn(new XThreadData(invoke, new DateTimer(DateFieldEnum.SECOND, 4, 2)));
		}
		sw.step("put to work " + n);
		while (this.sample.getCount() < n) {
			Thread.sleep(100);
		}
		sw.step("wait 1/2");
		while (this.sample.getCount() < 2 * n) {
			Thread.sleep(100);
		}
		sw.step("wait 2/2");
		for (int i = 0; i < n; i++) {
			xat[i].close();
		}
		sw.step("close " + n);
		LOG.info("#0", sw);
		assertTrue(this.sample.getCount() == 2 * n);
	}

}

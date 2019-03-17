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

package test.log;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.exception.LtException;
import leitej.exception.LtRtException;
import leitej.exception.PoolAgnosticThreadLtException;
import leitej.exception.SeppukuLtRtException;
import leitej.log.Logger;
import leitej.thread.PoolAgnosticThread;
import leitej.thread.XThreadData;
import leitej.util.AgnosticUtil;
import leitej.util.data.Invoke;

public class JLogger {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_sign() {
		final StackTraceElement[] ste = (new Throwable()).getStackTrace();
		final String signLog = ste[0].getClassName();
		System.out.println(ste[0]);
		System.out.println(ste[1]);
		System.out.println(ste[2]);
		assertTrue("test.log.JLogger".equals(signLog));
	}

	@Test
	public void test_log() throws InterruptedException {
		final Logger log = Logger.getInstance();
		log.info("ola");
		log.trace("#0", new LtRtException());
		Thread.sleep(1000);
		assertTrue(true);
	}

	@Test
	public void test_close_overloading()
			throws InterruptedException, PoolAgnosticThreadLtException, SecurityException, NoSuchMethodException {
		final PoolAgnosticThread xatPool = PoolAgnosticThread.newInstance();
		for (int i = 0; i < 200; i++) {
			xatPool.workOn(new XThreadData(new Invoke(this, AgnosticUtil.getMethod(this, "hit"))));
		}
		Thread.sleep(1000);
		assertTrue(true);
	}

	private static volatile int c = 0;

	public void hit() throws InterruptedException {
		Thread.sleep(250);
		Logger.getInstance().info(++c + "");
	}

	@Test(expected = SeppukuLtRtException.class)
	public void zzz_test_fatal()
			throws InterruptedException, PoolAgnosticThreadLtException, SecurityException, NoSuchMethodException {
		test_log();
		test_close_overloading();
		Thread.sleep(3000);
		Logger.getInstance().fatal("some problem", new LtException());
		Thread.sleep(1000);
		assertTrue(false);
	}

}

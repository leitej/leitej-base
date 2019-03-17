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

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.LtException;
import leitej.log.Logger;
import leitej.thread.AgnosticThread;
import leitej.thread.PoolAgnosticThread;
import leitej.thread.XThreadData;
import leitej.util.AgnosticUtil;
import leitej.util.data.DateFieldEnum;
import leitej.util.data.DateTimer;
import leitej.util.data.Invoke;
import leitej.util.machine.VMMonitor;

public class JPoolAThread {

	private static final Logger LOG = Logger.getInstance();

	private PoolAgnosticThread aThreadPool;
	private static final int NUM_POOL = 20;
	private static final int NUM_HIT_ITERATE = 20000;
	private static final int NUM_HIT_CRON = 60;

	private volatile PoolAgnosticThread[] arrayPool;
	private volatile boolean[] arrayPoolIterator;
	private volatile Integer[] arrayCronIterator;
	private volatile boolean[] arrayIterator;

	@Before
	public void setUp() throws Exception {
		this.aThreadPool = PoolAgnosticThread.newInstance();
		this.arrayIterator = new boolean[NUM_HIT_ITERATE];
		this.arrayCronIterator = new Integer[NUM_HIT_CRON];
		this.arrayPool = new PoolAgnosticThread[NUM_POOL];
	}

	@After
	public void tearDown() throws Exception {
		this.aThreadPool.close();
		this.aThreadPool = null;
		this.arrayIterator = null;
		this.arrayPoolIterator = null;
		this.arrayCronIterator = null;
		this.arrayPool = null;
	}

	@Test(expected = IllegalArgumentLtRtException.class)
	public void testWorkOn() throws LtException {
		this.aThreadPool.workOn(null);
	}

	@Test
	public void testWorkOn_n() throws SecurityException, LtException, NoSuchMethodException, InterruptedException {
		for (int i = 0; i < NUM_HIT_ITERATE; i++) {
			this.aThreadPool
					.workOn(new XThreadData(new Invoke(this, AgnosticUtil.getMethod(this, "hit", int.class), i)));
		}
		this.aThreadPool.closeWaitAll();
		for (int i = 0; i < NUM_HIT_ITERATE; i++) {
			assertTrue(this.arrayIterator[i] == true);
		}
	}

	public void hit(final int n) {
		this.arrayIterator[n] = true;
	}

	@Test
	public void testWorkOn_wait() throws InterruptedException {
		Thread.sleep(5000);
	}

	@Test
	public void testWorkOn_n_multi()
			throws SecurityException, LtException, NoSuchMethodException, InterruptedException, IOException {
		this.arrayPoolIterator = new boolean[NUM_HIT_ITERATE * NUM_POOL];
		for (int i = 0; i < NUM_POOL; i++) {
			this.arrayPool[i] = PoolAgnosticThread.newInstance();
		}
		for (int i = 0; i < NUM_POOL; i++) {
			this.aThreadPool
					.workOn(new XThreadData(new Invoke(this, AgnosticUtil.getMethod(this, "hitPool", int.class), i)));
		}
		this.aThreadPool.closeWaitAll();
		for (int i = 0; i < NUM_POOL; i++) {
			for (int j = 0; j < NUM_HIT_ITERATE; j++) {
				if (!this.arrayPoolIterator[j + (i * NUM_HIT_ITERATE)]) {
					final int r = j + (i * NUM_HIT_ITERATE);
					System.out.println("pool:" + i + " - hit:" + j + " fail " + r);
				}
			}
		}
		for (int i = 0; i < NUM_HIT_ITERATE * NUM_POOL; i++) {
			assertTrue(this.arrayPoolIterator[i] == true);
		}
	}

	public void hitPool(final int nPool)
			throws SecurityException, LtException, NoSuchMethodException, InterruptedException {
		for (int i = 0; i < NUM_HIT_ITERATE; i++) {
			this.arrayPool[nPool].workOn(new XThreadData(new Invoke(this,
					AgnosticUtil.getMethod(this, "hitPoolMulti", int.class), (nPool * NUM_HIT_ITERATE) + i)));
		}
		this.arrayPool[nPool].closeWaitAll();
	}

	public void hitPoolMulti(final int n) {
		synchronized (this.arrayPoolIterator) {
			this.arrayPoolIterator[n] = true;
		}
	}

	@Test
	public void testWorkOn_n_cron()
			throws SecurityException, LtException, NoSuchMethodException, InterruptedException, IOException {
		Thread.sleep(4000);
		for (int i = 0; i < NUM_HIT_CRON; i++) {
			this.arrayCronIterator[i] = 0;
		}
		for (int i = 0; i < NUM_HIT_CRON; i++) {
			this.aThreadPool
					.workOn(new XThreadData(new Invoke(this, AgnosticUtil.getMethod(this, "hit_n_cron", int.class), i),
							new DateTimer(DateFieldEnum.SECOND, 2, i + 1)));
		}
		this.aThreadPool.closeWaitAll();
		for (int i = 0; i < NUM_HIT_CRON; i++) {
			if (this.arrayCronIterator[i] != i + 1) {
				System.out.println(i + ": " + this.arrayCronIterator[i] + " (" + (i + 1) + ")");
			}
		}
		for (int i = 0; i < NUM_HIT_CRON; i++) {
			assertTrue(this.arrayCronIterator[i] == i + 1);
		}
	}

	public void hit_n_cron(final int n) {
		try {
			Thread.sleep(150);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		this.arrayCronIterator[n]++;
	}

	@Test
	public void testWorkOn_error() throws SecurityException, LtException, NoSuchMethodException, InterruptedException {
		LOG.info("threadCount: #0 (start)", VMMonitor.threadCount());
		for (int i = 0; i < PoolAgnosticThread.DEFAULT_MAX_NUM_THREAD; i++) {
			this.aThreadPool.workOn(new XThreadData(new Invoke(this, AgnosticUtil.getMethod(this, "methodWithError"))));
		}
		Thread.sleep(500);
		LOG.info("threadCount: #0 (with work)", VMMonitor.threadCount());
		Thread.sleep(4000);
		LOG.info("threadCount: #0 (with work error)", VMMonitor.threadCount());
		for (int i = 0; i < NUM_HIT_ITERATE; i++) {
			this.aThreadPool
					.workOn(new XThreadData(new Invoke(this, AgnosticUtil.getMethod(this, "hit", int.class), i)));
		}
		Thread.sleep(120000);
		LOG.info("threadCount: #0 (after normalizer)", VMMonitor.threadCount());
		for (int i = 0; i < NUM_HIT_ITERATE; i++) {
			assertTrue(this.arrayIterator[i] == true);
		}
	}

	public void methodWithError() throws InterruptedException {
		Thread.sleep(2000);
//		throw new java.lang.Error();
		final AgnosticThread ag = (AgnosticThread) Thread.currentThread();
		ag.close();
	}

}

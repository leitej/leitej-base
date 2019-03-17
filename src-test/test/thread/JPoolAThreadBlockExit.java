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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.PoolAgnosticThreadLtException;
import leitej.exception.SeppukuLtRtException;
import leitej.thread.PoolAgnosticThread;
import leitej.thread.XThreadData;
import leitej.util.AgnosticUtil;
import leitej.util.data.Invoke;
import leitej.util.data.SyncCounter;

/**
 *
 * @author Julio Leite
 */
public class JPoolAThreadBlockExit {

	private PoolAgnosticThread aThreadPool;
	private SyncCounter sc;

	@Before
	public void setUp() throws Exception {
		this.aThreadPool = PoolAgnosticThread.newInstance();
		this.sc = new SyncCounter(0, 0, Integer.MAX_VALUE);
	}

	@After
	public void tearDown() throws Exception {
		this.aThreadPool.close();
	}

	@Test
	public void test() throws SeppukuLtRtException, IllegalArgumentLtRtException, PoolAgnosticThreadLtException,
			SecurityException, NoSuchMethodException {
		this.aThreadPool.workOn(new XThreadData(new Invoke(this, AgnosticUtil.getMethod(this, "inc"))));
		this.aThreadPool.workOn(new XThreadData(new Invoke(this, AgnosticUtil.getMethod(this, "dec"))));
		assertTrue(true);
	}

	public void inc() throws InterruptedException {
		while (this.sc.getCount() < Integer.MAX_VALUE) {
			this.sc.inc();
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
				// SE NAO TIVER O CATCH O THREAD PARA COM A FINALIZACAO DA JVM
				e.printStackTrace();
				throw e;
			}
		}
	}

	public void dec() throws InterruptedException {
		while (this.sc.getCount() < Integer.MAX_VALUE) {
			this.sc.dec();
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
				// SE NAO TIVER O CATCH O THREAD PARA COM A FINALIZACAO DA JVM
				e.printStackTrace();
				throw e;
			}
		}
	}

}

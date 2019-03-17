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

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.exception.ClosedLtRtException;
import leitej.exception.LtException;
import leitej.thread.AgnosticThread;
import leitej.thread.ThreadData;
import leitej.util.AgnosticUtil;
import leitej.util.data.Invoke;
import leitej.util.data.QueueBlockingFIFO;

public class JFIFOBlockingQueue {

	QueueBlockingFIFO<Object> queue;

	@Before
	public void setUp() throws Exception {
		this.queue = new QueueBlockingFIFO<>(100);
	}

	@After
	public void tearDown() throws Exception {
		this.queue = null;
	}

	public void closeQueue() {
		this.queue.close();
	}

	public void closeQueueDelay5() throws InterruptedException {
		Thread.sleep(5000);
		this.queue.close();
	}

	@Test(timeout = 10000, expected = ClosedLtRtException.class)
	public final void testQueue_close()
			throws InterruptedException, LtException, SecurityException, NoSuchMethodException {
		new AgnosticThread(new ThreadData(new Invoke(this, AgnosticUtil.getMethod(this, "closeQueueDelay5"))));
		this.queue.poll();
		fail("Should not pass !");
	}

}

/*******************************************************************************
 * Copyright Julio Leite
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

package leitej.thread;

import leitej.exception.AgnosticThreadLtException;
import leitej.exception.ImplementationLtRtException;
import leitej.util.DateUtil;

/**
 * eXtended Agnostic Thread
 * <p>
 * The class XAgnosticThread extends {@link leitej.thread.AgnosticThread
 * AgnosticThread} to achieve an invocation by appointment.
 * </p>
 *
 * @author Julio Leite
 * @see java.lang.Thread
 * @see leitej.thread.AgnosticThread
 * @see leitej.thread.XThreadData
 * @see leitej.thread.ThreadData
 * @see leitej.util.data.InvokeItf
 */
public class XAgnosticThread extends AgnosticThread {

	private volatile XThreadData xThreadData = null;

	/**
	 * Creates a new instance of XAgnosticThread, with a thread name (null) and to
	 * run invoke only once.
	 */
	public XAgnosticThread() {
		super();
	}

	/**
	 * Creates a new instance of XAgnosticThread, to run invoke only once.
	 *
	 * @param name of thread
	 */
	public XAgnosticThread(final String name) {
		super(name);
	}

	/**
	 * Creates a new instance of XAgnosticThread, with a thread name (null).
	 * 
	 * @param keepAlive boolean defining if run invoke once or many
	 */
	public XAgnosticThread(final boolean keepAlive) {
		super(keepAlive);
	}

	/**
	 * Creates a new instance of XAgnosticThread.
	 * 
	 * @param name      of thread
	 * @param keepAlive boolean defining if run invoke once or many
	 */
	public XAgnosticThread(final String name, final boolean keepAlive) {
		super(name, keepAlive);
	}

	/**
	 * Creates a new instance of XAgnosticThread, with a thread name (null) and to
	 * run invoke only once.
	 * 
	 * @param threadData to work on
	 */
	public XAgnosticThread(final ThreadData threadData) {
		super(threadData);
	}

	/**
	 * Creates a new instance of XAgnosticThread, with a thread name (null) and to
	 * run invoke only once.
	 * 
	 * @param xThreadData to work on
	 */
	public XAgnosticThread(final XThreadData xThreadData) {
		super(true);
		try {
			workOn(xThreadData);
		} catch (final AgnosticThreadLtException e) {
			new ImplementationLtRtException(e);
		}
	}

	/**
	 * Use this method to put this thread working on parameter.
	 * 
	 * @param xThreadData to work on
	 * @throws AgnosticThreadLtException if is working on other invoke or already
	 *                                   terminated
	 */
	public synchronized void workOn(final XThreadData xThreadData) throws AgnosticThreadLtException {
		if (isWorking()) {
			throw new AgnosticThreadLtException("lt.ThreadAlreadyWork");
		}
		this.xThreadData = xThreadData;
		internalStart();
		pauseBreak();
	}

	@Override
	public synchronized boolean isWorking() {
		return (this.xThreadData != null && !this.xThreadData.isXDone() && !isTerminated()) || super.isWorking();
	}

	@Override
	protected void pause() {
		Long sleep = null;
		synchronized (this) {
			if (this.xThreadData != null && this.xThreadData.nextExecTime() != null) {
				sleep = this.xThreadData.nextExecTime().getTime() - DateUtil.nowTime();
			}
		}
		if (sleep == null) {
			sleep = SLEEP_TIME;
		}
		if (sleep < 8) {
			sleep = 1L;
		}
		try {
			Thread.sleep(sleep);
		} catch (final InterruptedException e) {
			/* ignored */}
		synchronized (this) {
			if (this.xThreadData != null && this.xThreadData.nextExecTime() == null) {
				this.xThreadData = null;
			}
			if (!super.closeThread && this.xThreadData != null && super.threadData == null
					&& !DateUtil.isFuture(this.xThreadData.nextExecTime())) {
				super.threadData = (ThreadData) this.xThreadData;
			}
		}
	}

}

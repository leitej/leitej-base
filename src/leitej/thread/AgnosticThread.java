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
import leitej.util.AgnosticUtil;
import leitej.util.StringUtil;
import leitej.util.data.InvokeItf;

/**
 * The <code>AgnosticThread</code> class extends {@link java.lang.Thread Thread}
 * to achieve an agnostic and asynchronous invocation of any object's public
 * methods.<br/>
 * <p>
 * Code example to use:<br/>
 * <code>
 * AgnosticThread athread = new AgnosticThread();<br/>
 * athread.workOn(<br/>
 * &nbsp;&nbsp;new Invoke(<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;this,<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;AgnosticUtil.getMethod(<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;this,<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"somePublicMethod"<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;)<br/>
 * &nbsp;&nbsp;)<br/>
 * )<br/>
 * ...<br/>
 * public void somePublicMethod(){};<br/>
 * </code>
 * </p>
 * The AgnosticThread instances can be reused for other invocations.
 *
 * @author Julio Leite
 * @see java.lang.Thread
 * @see leitej.thread.ThreadData
 * @see leitej.util.data.InvokeItf
 */
public class AgnosticThread extends Thread {

	protected static final long SLEEP_TIME_MS = 2000;

	private final boolean keepAlive;
	private final String prefixName;
	protected volatile boolean closeThread = false;
	protected volatile ThreadData threadData = null;
	private volatile boolean working = false;

	/**
	 * Creates a new instance of AgnosticThread, with a thread name (null) and to
	 * run invoke only once.
	 */
	public AgnosticThread() {
		this(null, false);
	}

	/**
	 * Creates a new instance of AgnosticThread, to run invoke only once.
	 *
	 * @param name of thread
	 */
	public AgnosticThread(final String name) {
		this(name, false);
	}

	/**
	 * Creates a new instance of AgnosticThread, with a thread name (null).
	 *
	 * @param keepAlive boolean defining if run invoke once or many
	 */
	public AgnosticThread(final boolean keepAlive) {
		this(null, keepAlive);
	}

	/**
	 * Creates a new instance of AgnosticThread.
	 *
	 * @param name      of thread
	 * @param keepAlive boolean defining if run invoke once or many
	 */
	public AgnosticThread(final String name, final boolean keepAlive) {
		super();
		setDaemon(false);
		this.keepAlive = keepAlive;
		if (!StringUtil.isNullOrEmpty(name)) {
			this.prefixName = name + "." + super.getName();
			setName(this.prefixName);
		} else {
			this.prefixName = super.getName();
		}
	}

	/**
	 * Creates a new instance of AgnosticThread, with a thread name (null) and to
	 * run invoke only once.<br/>
	 * This is like a fire and forget, don't need to check the return of invocation
	 * or is void.
	 *
	 * @param invoke to call
	 */
	public AgnosticThread(final InvokeItf invoke) {
		this(new ThreadData(invoke));
	}

	/**
	 * Creates a new instance of AgnosticThread, with a thread name (null) and to
	 * run invoke only once.
	 *
	 * @param threadData to work on
	 */
	public AgnosticThread(final ThreadData threadData) {
		this(null, false);
		try {
			workOn(threadData);
		} catch (final AgnosticThreadLtException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	@Override
	public void run() {
		do {
			pause();
			if (this.threadData != null && !this.threadData.isDone() && !this.closeThread) {
				try {
					synchronized (this) {
						this.working = true;
						Thread.interrupted();
					}
					setInvokeProperties();
					startTask();
					try {
						this.threadData.setResult(AgnosticUtil.invoke(this.threadData.getInvokeData()));
					} catch (final Exception e) {
						this.threadData.setException(e);
					} catch (final Error er) {
						er.printStackTrace();
						throw er;
					} finally {
						this.threadData.done();
						threadDataDispose();
						setDefaultProperties();
					}
				} finally {
					synchronized (this) {
						this.working = false;
					}
				}
			}
		} while (!this.closeThread && this.keepAlive);
	}

	/**
	 * Use this method to put this thread working on parameter.
	 *
	 * @param invoke to work on
	 * @throws AgnosticThreadLtException if is working on other invoke or already
	 *                                   terminated
	 */
	public synchronized void workOn(final InvokeItf invoke) throws AgnosticThreadLtException {
		workOn(new ThreadData(invoke));
	}

	/**
	 * Use this method to put this thread working on parameter.
	 *
	 * @param threadData to work on
	 * @throws AgnosticThreadLtException if is working on other invoke or already
	 *                                   terminated
	 */
	public synchronized void workOn(final ThreadData threadData) throws AgnosticThreadLtException {
		if (isWorking()) {
			throw new AgnosticThreadLtException("Only give work when not digesting other work! (#0)", getName());
		}
		this.threadData = threadData;
		this.internalStart();
		pauseBreak();
	}

	/**
	 * To be called internally by this thread
	 */
	protected synchronized final void pauseBreak() {
		if (isAlive() && !this.working) {
			interrupt();
		}
	}

	/**
	 * To know if has work to do and it's not done.
	 *
	 * @return boolean
	 */
	public synchronized boolean isWorking() {
		return (this.threadData != null && !this.threadData.isDone()) && !isTerminated();
	}

	/**
	 * To be called internally by this thread
	 */
	protected void pause() {
		try {
			Thread.sleep(SLEEP_TIME_MS);
		} catch (final InterruptedException e) {
			/* ignored */}
	}

	/**
	 * To be called internally by this thread
	 */
	protected void threadDataDispose() {
		this.threadData = null;
	}

	/**
	 * To be called internally by this thread
	 */
	protected void startTask() {
	}

	private void setInvokeProperties() {
		if (!StringUtil.isNullOrEmpty(this.threadData.getThreadName())) {
			setName((new StringBuilder()).append(this.threadData.getThreadName()).append(".").append(this.prefixName)
					.toString());
		}
		if (getPriority() != this.threadData.getPriority().getSystemThreadPriority()) {
			setPriority(this.threadData.getPriority().getSystemThreadPriority());
		}
	}

	private void setDefaultProperties() {
		if (!getName().equals(this.prefixName)) {
			setName(this.prefixName);
		}
		if (getPriority() != ThreadPriorityEnum.NORMAL.getSystemThreadPriority()) {
			setPriority(ThreadPriorityEnum.NORMAL.getSystemThreadPriority());
		}
	}

	/**
	 * Causes this thread to begin execution; the Java Virtual Machine calls the
	 * <code>run</code> method of this thread.<br/>
	 * <br/>
	 * The result is that two threads are running concurrently: the current thread
	 * (which returns from the call to the <code>start</code> method) and the other
	 * thread (which executes its <code>run</code> method).<br/>
	 * <br/>
	 * If this thread already started and still alive the call is a NOOP.
	 *
	 * @throws AgnosticThreadLtException if already terminated
	 */
	protected synchronized final void internalStart() throws AgnosticThreadLtException {
		try {
			if (!isAlive()) {
				super.start();
			}
		} catch (final IllegalThreadStateException e) {
			new AgnosticThreadLtException(e);
		}
	}

	/**
	 * Verifies if it is in {@link java.lang.Thread.State TERMINATED} state.
	 *
	 * @return boolean
	 */
	public boolean isTerminated() {
		return State.TERMINATED.equals(getState());
	}

	/**
	 * Closes this thread.<br/>
	 * The thread that calls this method waits until this finished.
	 *
	 * @throws InterruptedException to stop the waits until finished
	 */
	public synchronized void close() throws InterruptedException {
		if (!this.closeThread) {
			this.closeThread = true;
		}
		if (isAlive()) {
			pauseBreak();
			if (!this.equals(Thread.currentThread())) {
				join();
			}
		}
	}

	/**
	 * Closes this thread asynchronously.
	 */
	public synchronized void closeAsync() {
		if (!this.closeThread) {
			this.closeThread = true;
		}
		pauseBreak();
	}

}

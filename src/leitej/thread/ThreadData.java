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

package leitej.thread;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ParallelLtRtException;
import leitej.util.data.InvokeItf;

/**
 * This class uses {@link leitej.util.data.InvokeItf InvokeItf} to know what
 * will be executed.
 *
 * @author Julio Leite
 * @see leitej.util.data.InvokeItf
 */
public class ThreadData implements Serializable {

	private static final long serialVersionUID = 7699501305855165L;

	private final InvokeItf invokeData;

	private final String threadName;
	private final ThreadPriorityEnum priority;

	private Object result;
	private ParallelLtRtException parallelException;
	private volatile boolean done = false;

	private final List<Thread> threadsBlocked = new ArrayList<>();

	/**
	 * Creates a new instance of ThreadData.
	 *
	 * @param invokeData {@link leitej.util.data.InvokeItf InvokeItf} to be executed
	 * @throws IllegalArgumentLtRtException if <code>invokeData</code> is null
	 */
	public ThreadData(final InvokeItf invokeData) throws IllegalArgumentLtRtException {
		this(invokeData, null, null);
	}

	/**
	 * Creates a new instance of ThreadData.
	 *
	 * @param invokeData {@link leitej.util.data.InvokeItf InvokeItf} to be executed
	 * @param threadName partial name for the thread when executing
	 *                   <code>invokeData</code>
	 * @throws IllegalArgumentLtRtException if <code>invokeData</code> is null
	 */
	public ThreadData(final InvokeItf invokeData, final String threadName) throws IllegalArgumentLtRtException {
		this(invokeData, threadName, null);
	}

	/**
	 * Creates a new instance of ThreadData.
	 *
	 * @param invokeData {@link leitej.util.data.InvokeItf InvokeItf} to be executed
	 * @param priority   of thread execution
	 * @throws IllegalArgumentLtRtException if <code>invokeData</code> is null
	 */
	public ThreadData(final InvokeItf invokeData, final ThreadPriorityEnum priority)
			throws IllegalArgumentLtRtException {
		this(invokeData, null, priority);
	}

	/**
	 * Creates a new instance of ThreadData.
	 *
	 * @param invokeData {@link leitej.util.data.InvokeItf InvokeItf} to be executed
	 * @param threadName partial name for the thread when executing
	 *                   <code>invokeData</code>
	 * @param priority   of thread execution
	 * @throws IllegalArgumentLtRtException if <code>invokeData</code> is null
	 */
	public ThreadData(final InvokeItf invokeData, final String threadName, final ThreadPriorityEnum priority)
			throws IllegalArgumentLtRtException {
		if (invokeData == null) {
			throw new IllegalArgumentLtRtException();
		}
		this.invokeData = invokeData;
		this.threadName = threadName;
		if (priority != null) {
			this.priority = priority;
		} else {
			this.priority = ThreadPriorityEnum.NORMAL;
		}
	}

	/**
	 * Defines what will run in parallel.
	 *
	 * @return {@link leitej.util.data.InvokeItf InvokeItf}
	 */
	final InvokeItf getInvokeData() {
		return this.invokeData;
	}

	/**
	 * Defines partial name that is given to the thread while executes
	 * <code>getInvokeData()</code>.
	 *
	 * @return String name
	 */
	final String getThreadName() {
		return this.threadName;
	}

	/**
	 * Defines the priority that is given to the thread while executes
	 * <code>getInvokeData()</code>.
	 *
	 * @return {@link leitej.thread.ThreadPriorityEnum ThreadPriorityEnum}
	 */
	final ThreadPriorityEnum getPriority() {
		return this.priority;
	}

	/**
	 * Gives the result of the parallel execution.<br/>
	 * Will return null if the execution yet does not ended or does not give any
	 * result.
	 *
	 * @return Object with the parallel execution result
	 * @throws ParallelLtRtException if the parallel execution throws an exception
	 */
	public final Object getResult() throws ParallelLtRtException {
		if (isDone()) {
			if (this.parallelException != null) {
				throw this.parallelException;
			}
			return this.result;
		}
		return null;
	}

	/**
	 * Gives the result of the parallel execution.<br/>
	 * This method will block until the end of the parallel execution.
	 *
	 * @return Object with the parallel execution result
	 * @throws InterruptedException  if any thread has interrupted the current
	 *                               thread and the parallel execution doesn't ended
	 * @throws ParallelLtRtException if the parallel execution throws an exception
	 */
	public final Object getBlockingResult() throws InterruptedException, ParallelLtRtException {
		if (!isDone()) {
			synchronized (this.threadsBlocked) {
				this.threadsBlocked.add(Thread.currentThread());
			}
			try {
				while (!isDone()) {
					try {
						Thread.sleep(10000);
					} catch (final InterruptedException e) {
						if (!isDone()) {
							throw e;
						}
					}
				}
			} finally {
				synchronized (this.threadsBlocked) {
					this.threadsBlocked.remove(Thread.currentThread());
					Thread.interrupted();
				}
			}
		}
		return getResult();
	}

	/**
	 * Method called to put the result of the execution of
	 * <code>getInvokeData()</code>, if any.
	 *
	 * @param result Object with the result
	 */
	final void setResult(final Object result) {
		this.result = result;
	}

	/**
	 * Method called to put the exception of the execution of
	 * <code>getInvokeData()</code>, if any.
	 *
	 * @param exception
	 */
	final void setException(final Exception exception) {
		this.parallelException = new ParallelLtRtException(exception);
	}

	/**
	 * Is invoked when the execution of <code>getInvokeData()</code> ends.
	 */
	void done() {
		this.done = true;
		synchronized (this.threadsBlocked) {
			final Iterator<Thread> it = this.threadsBlocked.iterator();
			while (it.hasNext()) {
				try {
					it.next().interrupt();
				} catch (final SecurityException e) {
					e.printStackTrace();
				}
			}
			this.threadsBlocked.clear();
		}
	}

	/**
	 * Defines if <code>getInvokeData()</code> has already been executed.
	 *
	 * @return boolean
	 */
	public boolean isDone() {
		return this.done;
	}

	@Override
	public final String toString() {
		return (new StringBuilder()).append(this.getClass().getSimpleName()).append("@")
				.append(super.toString().split("@")[1]).append("><").append(this.getInvokeData().toString()).toString();
	}

}

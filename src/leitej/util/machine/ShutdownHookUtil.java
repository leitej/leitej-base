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

package leitej.util.machine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.PoolAgnosticThreadLtException;
import leitej.exception.SeppukuLtRtException;
import leitej.log.Logger;
import leitej.thread.PoolAgnosticThread;
import leitej.thread.XThreadData;
import leitej.util.AgnosticUtil;
import leitej.util.data.InvokeItf;
import leitej.util.data.InvokeSignature;
import leitej.util.data.SyncCounter;

/**
 * An useful class to help in interact with
 * {@link java.lang.Runtime#addShutdownHook(Thread)
 * Runtime.getRuntime().addShutdownHook(Thread)}.<br/>
 * <br/>
 * If the virtual machine is already in the process of shutting down or if a
 * security manager is present and it denies
 * <tt>{@link RuntimePermission}("shutdownHooks")</tt>, a
 * <code>SeppukuLtRtException</code> with code 1 will be raised.
 *
 * @author Julio Leite
 * @see leitej.exception.SeppukuLtRtException
 */
public final class ShutdownHookUtil extends Thread {

	private static final List<InvokeItf> FILO_TASKS_FIRST = new ArrayList<>();
	private static final List<InvokeItf> TASKS = new ArrayList<>();
	private static final List<InvokeItf> FILO_TASKS_LAST = new ArrayList<>();
	private static final ShutdownHookUtil INSTANCE = new ShutdownHookUtil();

	private Logger log;
	private SyncCounter sCount;
	private PoolAgnosticThread atPool;
	private Boolean hooked = Boolean.FALSE;

	/**
	 * Creates a new instance of ShutdownHookUtil.
	 *
	 * @throws SeppukuLtRtException exit(400) if the virtual machine is already in
	 *                              the process of shutting down or if a security
	 *                              manager is present and it denies
	 *                              <tt>{@link RuntimePermission}("shutdownHooks")</tt>
	 */
	private ShutdownHookUtil() throws SeppukuLtRtException {
		try {
			Runtime.getRuntime().addShutdownHook(this);
		} catch (final IllegalArgumentException e) {
			throw new SeppukuLtRtException(400, e);
		} catch (final IllegalStateException e) {
			throw new SeppukuLtRtException(400, e);
		} catch (final SecurityException e) {
			throw new SeppukuLtRtException(400, e);
		}
	}

	@Override
	public void run() {
		synchronized (this.hooked) {
			this.hooked = Boolean.TRUE;
			if (TASKS.size() > 0) {
				this.atPool = PoolAgnosticThread.newInstance();
			}
		}
		this.log = Logger.getInstance();
		this.log.warn("initialized");
		this.sCount = new SyncCounter(0, 0, Integer.MAX_VALUE);
		XThreadData xThreadData;
		InvokeSignature invokeSig;
		try {
			//
			for (int i = FILO_TASKS_FIRST.size() - 1; i >= 0; i--) {
				try {
					this.log.debug("#0", FILO_TASKS_FIRST.get(i));
					AgnosticUtil.invoke(FILO_TASKS_FIRST.get(i));
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			FILO_TASKS_FIRST.clear();
			//
			if (this.atPool != null) {
				invokeSig = new InvokeSignature(this, AgnosticUtil.getMethod(this, METHOD_RUN_TASK, InvokeItf.class));
				for (final InvokeItf task : TASKS) {
					xThreadData = new XThreadData(invokeSig.getInvoke(task));
					try {
						this.log.debug("#0", xThreadData);
						this.atPool.workOn(xThreadData);
						this.sCount.inc();
					} catch (final SeppukuLtRtException e) {
						e.printStackTrace();
					} catch (final IllegalArgumentLtRtException e) {
						(new ImplementationLtRtException(e)).printStackTrace();
					} catch (final PoolAgnosticThreadLtException e) {
						(new ImplementationLtRtException(e)).printStackTrace();
					}
				}
				TASKS.clear();
				this.atPool.closeWaitAll();
			}
			//
			for (int i = FILO_TASKS_LAST.size() - 1; i >= 0; i--) {
				try {
					this.log.debug("#0", FILO_TASKS_LAST.get(i));
					AgnosticUtil.invoke(FILO_TASKS_LAST.get(i));
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			FILO_TASKS_LAST.clear();
		} catch (final SecurityException e) {
			e.printStackTrace();
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (this.atPool != null) {
				this.atPool.closeAsync();
			}
		}
		// logDebug("ended");
	}

	private static final String METHOD_RUN_TASK = "runTask";

	public void runTask(final InvokeItf task) {
		if (PoolAgnosticThread.isCurrentThreadFrom(this.atPool)) {
			try {
				AgnosticUtil.invoke(task);
			} catch (final Exception e) {
				e.printStackTrace();
			} finally {
				this.sCount.dec();
			}
		}
	}

	/**
	 * Appends the specified element to the list to be called sequentially before
	 * generic tasks added by 'add' method. This list will be run in FILO order.
	 *
	 * @param invoke element to be appended to the list
	 * @return <tt>true</tt> (as specified by {@link Collection#add})
	 * @throws NullPointerException      if the specified element is null
	 * @throws IllegalArgumentException  if some property of this element prevents
	 *                                   it from being added to the list
	 * @throws IllegalStateLtRtException if shutdown hook already started
	 */
	public static boolean addToFirst(final InvokeItf invoke)
			throws NullPointerException, IllegalArgumentException, IllegalStateLtRtException {
		if (invoke == null) {
			throw new NullPointerException();
		}
		boolean result;
		synchronized (INSTANCE.hooked) {
			if (INSTANCE.hooked.booleanValue()) {
				throw new IllegalStateLtRtException("Shutdown hook already started");
			}
			result = FILO_TASKS_FIRST.add(invoke);
		}
		return result;
	}

	/**
	 * Appends the specified element to the list that will run this tasks in
	 * parallel mode.
	 *
	 * @param invoke element to be appended to the list
	 * @return <tt>true</tt> (as specified by {@link Collection#add})
	 * @throws NullPointerException      if the specified <code>invoke</code> is
	 *                                   null
	 * @throws IllegalArgumentException  if some property of this element prevents
	 *                                   it from being added to the list
	 * @throws IllegalStateLtRtException if shutdown hook already started
	 */
	public static boolean add(final InvokeItf invoke)
			throws NullPointerException, IllegalArgumentException, IllegalStateLtRtException {
		if (invoke == null) {
			throw new NullPointerException();
		}
		boolean result;
		synchronized (INSTANCE.hooked) {
			if (INSTANCE.hooked.booleanValue()) {
				throw new IllegalStateLtRtException("Shutdown hook already started");
			}
			result = TASKS.add(invoke);
		}
		return result;
	}

	/**
	 * Appends the specified element to the list to be called sequentially after
	 * generic tasks added by 'add' method. This list will be run in FILO order.
	 *
	 * @param invoke element to be appended to the list
	 * @return <tt>true</tt> (as specified by {@link Collection#add})
	 * @throws NullPointerException      if the specified element is null
	 * @throws IllegalArgumentException  if some property of this element prevents
	 *                                   it from being added to the list
	 * @throws IllegalStateLtRtException if shutdown hook already started
	 */
	public static boolean addToLast(final InvokeItf invoke)
			throws NullPointerException, IllegalArgumentException, IllegalStateLtRtException {
		if (invoke == null) {
			throw new NullPointerException();
		}
		boolean result;
		synchronized (INSTANCE.hooked) {
			if (INSTANCE.hooked.booleanValue()) {
				throw new IllegalStateLtRtException("Shutdown hook already started");
			}
			result = FILO_TASKS_LAST.add(invoke);
		}
		return result;
	}

	/**
	 * Removes the first occurrence of the specified element from the list, if it is
	 * present. If the list does not contain the element, it is unchanged. More
	 * formally, removes the element with the lowest index <tt>i</tt> such that
	 * <tt>(invoke.equals(get(i)))</tt> (if such an element exists). Returns
	 * <tt>true</tt> if the list contained the specified element (or equivalently,
	 * if the list changed as a result of the call).
	 *
	 * @param invoke element to be removed from the list, if present
	 * @return <tt>true</tt> if the list contained the specified element
	 * @throws NullPointerException      if the specified element is null
	 * @throws IllegalStateLtRtException if shutdown hook already started
	 */
	public static boolean remove(final InvokeItf invoke) throws NullPointerException, IllegalStateLtRtException {
		if (invoke == null) {
			throw new NullPointerException();
		}
		boolean result;
		synchronized (INSTANCE.hooked) {
			if (INSTANCE.hooked.booleanValue()) {
				throw new IllegalStateLtRtException("Shutdown hook already started");
			}
			result = FILO_TASKS_FIRST.remove(invoke);
			if (!result) {
				result = TASKS.remove(invoke);
			}
			if (!result) {
				result = FILO_TASKS_LAST.remove(invoke);
			}
		}
		return result;
	}

	/**
	 * Assert if the shutdown hook has been activated.
	 *
	 * @return if the shutdown hook has been activated
	 */
	public static boolean isActive() {
		synchronized (INSTANCE.hooked) {
			return INSTANCE.hooked;
		}
	}

	/**
	 * Asserts if the thread that calls this method is from this shutdown hook.
	 *
	 * @return if current thread is from this shutdown hook
	 */
	public static boolean isCurrentThreadFrom() {
		synchronized (INSTANCE.hooked) {
			if (!INSTANCE.hooked) {
				return false;
			}
		}
		return Thread.currentThread().equals(INSTANCE) || PoolAgnosticThread.isCurrentThreadFrom(INSTANCE.atPool);
	}
}

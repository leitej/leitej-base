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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import leitej.exception.AgnosticThreadLtException;
import leitej.exception.ClosedLtRtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.ParallelLtRtException;
import leitej.exception.PoolAgnosticThreadLtException;
import leitej.exception.SeppukuLtRtException;
import leitej.log.Logger;
import leitej.util.AgnosticUtil;
import leitej.util.DateUtil;
import leitej.util.data.DateFieldEnum;
import leitej.util.data.DateTimer;
import leitej.util.data.Invoke;
import leitej.util.data.InvokeItf;
import leitej.util.data.QueueBlockingFIFO;
import leitej.util.machine.ShutdownHookUtil;

/**
 * Pool of Agnostic Thread<br/>
 * <br/>
 * This class provides a pool of threads that extend
 * {@link leitej.thread.AgnosticThread AgnosticThread}.<br/>
 * You can get pools by setting the maximum and minimum numbers of threads
 * running simultaneously in this pool.<br/>
 * <br/>
 * The constructor will put a call to
 * {@link leitej.thread.PoolAgnosticThread#closeAsync() closeAsync()} in the
 * {@link leitej.util.machine.ShutdownHookUtil#add(InvokeItf)
 * ShutdownHookUtil.add(InvokeItf)}.
 *
 * @author Julio Leite
 * @see leitej.thread.XThreadData
 * @see leitej.thread.ThreadData
 */
public final class PoolAgnosticThread {

	private static final Logger LOG = Logger.getInstance();

	private static final int NORMALIZER_RUN_IN_MINUTELY_INTERVAL = 2;
	private static final long RESCUER_SLEEP_TIME = 6000;
	private static final long EXECUTIONER_SLEEP_TIME = 10000;
	/**
	 * Default minimum threads running on the pool. (={@value})
	 */
	public static final int DEFAULT_MIN_NUM_THREAD = 0;
	/**
	 * Default maximum threads running on the pool. (={@value})
	 */
	public static final int DEFAULT_MAX_NUM_THREAD = 64;

	/**
	 * Call this method to get a new PoolAgnosticThread instance.<br/>
	 * The new pool will have <code>DEFAULT_MIN_NUM_THREAD</code> and
	 * <code>DEFAULT_MAX_NUM_THREAD</code>.<br/>
	 * <br/>
	 * A call to {@link leitej.thread.PoolAgnosticThread#closeAsync() closeAsync()}
	 * is put in the {@link leitej.util.machine.ShutdownHookUtil#add(InvokeItf)
	 * ShutdownHookUtil.add(InvokeItf)}.
	 *
	 * @return the new <code>PoolAgnosticThread</code> instance
	 */
	public static PoolAgnosticThread newInstance() {
		return getPaT(DEFAULT_MIN_NUM_THREAD, DEFAULT_MAX_NUM_THREAD);
	}

	/**
	 * Call this method to get a new <code>PoolAgnosticThread</code> instance.<br/>
	 * The pool will have <code>minNumThread</code> and <code>maxNumThread</code> in
	 * parameters.<br/>
	 * <br/>
	 * A call to {@link leitej.thread.PoolAgnosticThread#closeAsync() closeAsync()}
	 * is put in the {@link leitej.util.machine.ShutdownHookUtil#add(InvokeItf)
	 * ShutdownHookUtil.add(InvokeItf)}.
	 *
	 * @param minNumThread defines minimum threads running on the pool
	 * @param maxNumThread defines maximum threads running on the pool
	 * @return the new <code>PoolAgnosticThread</code> instance
	 */
	public static PoolAgnosticThread newInstance(final int minNumThread, final int maxNumThread) {
		return getPaT(minNumThread, maxNumThread);
	}

	private synchronized static PoolAgnosticThread getPaT(final int minNumThread, final int maxNumThread)
			throws IllegalArgumentException {
		return new PoolAgnosticThread(minNumThread, maxNumThread);
	}

	/**
	 * Use this method to know if the thread calling it is from the pool in
	 * argument.
	 *
	 * @param pool to verify
	 * @return boolean
	 */
	public static boolean isCurrentThreadFrom(final PoolAgnosticThread pool) {
		boolean result = false;
		if (PoolEmbebedAgnosticThread.class.isInstance(Thread.currentThread())) {
			result = PoolEmbebedAgnosticThread.class.cast(Thread.currentThread()).isYourPool(pool);
		}
		return result;
	}

	private final String prefixThreadName;
	private final String normalizerThreadName;
	private final String rescuerThreadName;
	private final String executionerThreadName;
	private final int minNumThread;
	private final int maxNumThread;
	private volatile Integer numThread;

	// array of all threads
	private volatile PoolEmbebedAgnosticThread[] threadMonitor;

	// queue of threads waiting
	private final QueueBlockingFIFO<PoolEmbebedAgnosticThread> threadWaitingQueue;
	// map of idThreads and threads that are working
	private final Map<Long, PoolEmbebedAgnosticThread> threadWorkingMap;
	// queue of threads waiting
	private final QueueBlockingFIFO<PoolEmbebedAgnosticThread> threadDamagedQueue;

	// job queue waiting to start ordered by execution time
	private final SortedSet<PoolTaskStruct> taskWaitingSet;
	// map idThreads and TaskStruct that are working
	private final Map<Long, PoolTaskStruct> taskWorkingMap;

	// queue of idThreads that completed the work
	private final QueueBlockingFIFO<Long> threadIdToRescue;

	// thread responsible for the normalizes
	private final XAgnosticThread normalizer;
	// thread responsible for the rescue
	private final AgnosticThread rescuer;
	private volatile boolean rescuerPause;
	// thread responsible for putting tasks into execution
	private final AgnosticThread executioner;
	private volatile boolean executionerPause;

	// defines pool closure
	private volatile boolean closed;
	// defines pool closure only for workOn
	private volatile boolean closedWorkOn;
	// defines if the pool is created by shutdown thread
	private final InvokeItf closeAsyncInvoke;

	/**
	 * Creates a new instance of PoolAgnosticThread.<br/>
	 * <br/>
	 * A call to {@link leitej.thread.PoolAgnosticThread#closeAsync() closeAsync()}
	 * is put in the {@link leitej.util.machine.ShutdownHookUtil#add(InvokeItf)
	 * ShutdownHookUtil.add(InvokeItf)}.
	 *
	 * @param minNumThread defines minimum threads running on the pool
	 * @param maxNumThread defines maximum threads running on the pool
	 */
	private PoolAgnosticThread(final int minNumThread, final int maxNumThread) {
		this.prefixThreadName = "Pool_" + DateUtil.generateUniqueNumberPerJVM();
		this.normalizerThreadName = this.prefixThreadName + "_NORMALIZER";
		this.rescuerThreadName = this.prefixThreadName + "_RESCUER";
		this.executionerThreadName = this.prefixThreadName + "_EXECUTIONER";
		if (minNumThread >= 0) {
			this.minNumThread = minNumThread;
		} else {
			this.minNumThread = DEFAULT_MIN_NUM_THREAD;
		}
		if (maxNumThread > this.minNumThread) {
			this.maxNumThread = maxNumThread;
		} else if (DEFAULT_MAX_NUM_THREAD > this.minNumThread) {
			this.maxNumThread = DEFAULT_MAX_NUM_THREAD;
		} else {
			this.maxNumThread = this.minNumThread + 1;
		}
		this.numThread = 0;
		this.threadMonitor = new PoolEmbebedAgnosticThread[this.maxNumThread];
		this.threadWaitingQueue = new QueueBlockingFIFO<>(this.maxNumThread);
		this.threadWorkingMap = Collections.synchronizedMap(new HashMap<Long, PoolEmbebedAgnosticThread>());
		this.threadDamagedQueue = new QueueBlockingFIFO<>(this.maxNumThread);
		this.taskWaitingSet = Collections.synchronizedSortedSet(new TreeSet<PoolTaskStruct>());
		this.taskWorkingMap = Collections.synchronizedMap(new HashMap<Long, PoolTaskStruct>());
		this.threadIdToRescue = new QueueBlockingFIFO<>(this.maxNumThread);
		this.closed = false;
		this.closedWorkOn = false;
		// Initializes thread responsible for normalizing this pool
		this.normalizer = new XAgnosticThread(true);
		try {
			final XThreadData tdata = new XThreadData(
					new Invoke(this, AgnosticUtil.getMethod(this, METHOD_NORMALIZER_JOB)),
					new DateTimer(DateFieldEnum.MINUTE, NORMALIZER_RUN_IN_MINUTELY_INTERVAL), this.normalizerThreadName,
					ThreadPriorityEnum.MAXIMUM);
			this.normalizer.workOn(tdata);
		} catch (final IllegalArgumentLtRtException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		} catch (final AgnosticThreadLtException e) {
			throw new ImplementationLtRtException(e);
		}
		// Initializes thread responsible for recovering threads that finished the job
		this.rescuer = new AgnosticThread();
		try {
			final ThreadData tdata = new ThreadData(new Invoke(this, AgnosticUtil.getMethod(this, METHOD_RESCUER_JOB)),
					this.rescuerThreadName, ThreadPriorityEnum.MAXIMUM);
			this.rescuer.workOn(tdata);
		} catch (final IllegalArgumentLtRtException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		} catch (final AgnosticThreadLtException e) {
			throw new ImplementationLtRtException(e);
		}
		// Initializes thread responsible for putting the work to be performed
		this.executioner = new AgnosticThread();
		try {
			final ThreadData tdata = new ThreadData(
					new Invoke(this, AgnosticUtil.getMethod(this, METHOD_EXECUTIONER_JOB)), this.executionerThreadName,
					ThreadPriorityEnum.MAXIMUM);
			this.executioner.workOn(tdata);
		} catch (final IllegalArgumentLtRtException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		} catch (final AgnosticThreadLtException e) {
			throw new ImplementationLtRtException(e);
		}
		if (!ShutdownHookUtil.isCurrentThreadFrom()) {
			try {
				this.closeAsyncInvoke = new Invoke(this, AgnosticUtil.getMethod(this, METHOD_CLOSE_ASYNC));
				ShutdownHookUtil.add(this.closeAsyncInvoke);
			} catch (final NoSuchMethodException e) {
				throw new ImplementationLtRtException(e);
			}
		} else {
			this.closeAsyncInvoke = null;
		}
		LOG.trace("lt.NewInstance");
	}

	/**
	 * Creates new object thread if it has not reached the maximum.<br/>
	 * Adds to the thread queue.
	 *
	 * @throws InterruptedException if interrupted while waiting
	 * @throws ClosedLtRtException  if the offering queue is closed
	 */
	private void addNewThread() throws ClosedLtRtException, InterruptedException {
		if (this.maxNumThread > this.numThread) {
			final PoolEmbebedAgnosticThread peat = newThread();
			this.threadMonitor[this.numThread++] = peat;
			offerThread(peat);
			LOG.trace("lt.ThreadNew", peat.getName());
		} else {
			LOG.trace("lt.ThreadNewDeny", this.maxNumThread);
		}
	}

	private PoolEmbebedAgnosticThread newThread() {
		return new PoolEmbebedAgnosticThread(this.prefixThreadName, this);
	}

	/**
	 *
	 * @param thread
	 * @throws ClosedLtRtException  if the offering queue is closed
	 * @throws InterruptedException if interrupted while waiting
	 */
	private void offerThread(final PoolEmbebedAgnosticThread thread) throws ClosedLtRtException, InterruptedException {
		try {
			this.threadWaitingQueue.offer(thread);
			LOG.trace("#0", thread.getName());
		} catch (final ClassCastException e) {
			new ImplementationLtRtException(e);
		} catch (final NullPointerException e) {
			new ImplementationLtRtException(e);
		} catch (final IllegalArgumentException e) {
			new ImplementationLtRtException(e);
		}
	}

	/**
	 *
	 * @return thread from the waiting queue
	 * @throws ClosedLtRtException  if the offering or waiting queue is closed
	 * @throws InterruptedException if interrupted while waiting
	 */
	private PoolEmbebedAgnosticThread poolThread() throws ClosedLtRtException, InterruptedException {
		if (this.threadWaitingQueue.size() == 0) {
			addNewThread();
		}
		PoolEmbebedAgnosticThread result = this.threadWaitingQueue.poll();
		while (result.isTerminated()) {
			setThreadToDamaged(result);
			result = this.threadWaitingQueue.poll();
		}
		return result;
	}

	/**
	 * Closes the pool and wait for all registered invokes to execute.
	 *
	 * @throws InterruptedException to stop the waits until finished
	 */
	public synchronized void closeWaitAll() throws InterruptedException {
		if (!this.closed) {
			this.closedWorkOn = true;
			try {
				do {
					do {
						Thread.sleep(1000);
					} while (!this.taskWaitingSet.isEmpty() || !this.taskWorkingMap.isEmpty());
				} while (!this.rescuerPause || !this.threadIdToRescue.isEmpty());
			} catch (final InterruptedException e) {
				closeAsync();
				throw e;
			}
			this.closed = true;
			removeCloseAsyncInvokeFromShutdownHook();
			internalClose();
			LOG.trace("lt.Closed");
		}
	}

	/**
	 * Closes the pool and wait only for the already started invokes to
	 * execute.<br/>
	 * All other registered invokes will be ignored and not executed.
	 *
	 * @throws InterruptedException to stop the waits until finished
	 */
	public synchronized void close() throws InterruptedException {
		if (!this.closed) {
			this.closedWorkOn = true;
			this.closed = true;
			removeCloseAsyncInvokeFromShutdownHook();
			internalClose();
			LOG.trace("lt.Closed");
		}
	}

	private void internalClose() throws InterruptedException {
		try {
			this.normalizer.close();
			synchronized (this.executioner) {
				if (this.executionerPause) {
					this.executioner.interrupt();
				}
			}
			this.executioner.close();
			synchronized (this.rescuer) {
				if (this.rescuerPause) {
					this.rescuer.interrupt();
				}
			}
			this.rescuer.close();
			LOG.trace("NUM_THREAD: #0", this.numThread);
			LOG.trace("instance.threadWaitingQueue.size(): #0", this.threadWaitingQueue.size());
			LOG.trace("instance.threadWorkingMap.size(): #0", this.threadWorkingMap.size());
			for (int i = 0; i < this.threadMonitor.length; i++) {
				if (this.threadMonitor[i] != null) {
					this.threadMonitor[i].close();
				}
			}
		} catch (final InterruptedException e) {
			this.executioner.closeAsync();
			synchronized (this.executioner) {
				if (this.executionerPause) {
					this.executioner.interrupt();
				}
			}
			this.rescuer.closeAsync();
			synchronized (this.rescuer) {
				if (this.rescuerPause) {
					this.rescuer.interrupt();
				}
			}
			for (int i = 0; i < this.threadMonitor.length; i++) {
				if (this.threadMonitor[i] != null) {
					this.threadMonitor[i].closeAsync();
				}
			}
			throw e;
		}
	}

	private static final String METHOD_CLOSE_ASYNC = "closeAsync";

	/**
	 * Closes the pool asynchronously.
	 */
	public synchronized void closeAsync() {
		if (!this.closed) {
			this.closedWorkOn = true;
			this.closed = true;
			removeCloseAsyncInvokeFromShutdownHook();
			try {
				this.normalizer.close();
			} catch (final InterruptedException e) {
				while (this.normalizer.isAlive()) {
					this.normalizer.interrupt();
					try {
						this.normalizer.close();
					} catch (final InterruptedException e1) {
						LOG.trace("#0", e);
					}
				}
			}
			this.executioner.closeAsync();
			synchronized (this.executioner) {
				if (this.executionerPause) {
					this.executioner.interrupt();
				}
			}
			this.rescuer.closeAsync();
			synchronized (this.rescuer) {
				if (this.rescuerPause) {
					this.rescuer.interrupt();
				}
			}
			for (int i = 0; i < this.threadMonitor.length; i++) {
				if (this.threadMonitor[i] != null) {
					this.threadMonitor[i].closeAsync();
				}
			}
		}
	}

	private void removeCloseAsyncInvokeFromShutdownHook() {
		if (this.closeAsyncInvoke != null && !ShutdownHookUtil.isActive()) {
			try {
				ShutdownHookUtil.remove(this.closeAsyncInvoke);
			} catch (final IllegalStateLtRtException e) {
				LOG.trace("#0", e);
			}
		}
	}

	private static final String METHOD_NORMALIZER_JOB = "normalizerJob";

	/**
	 * Certify the stability of the pool.<br/>
	 * This method is only to be internally invoked by the thread normaliser.
	 */
	public void normalizerJob() {
		if (Thread.currentThread().getId() == this.normalizer.getId()) {
			try {
				LOG.debug("lt.Init");
				if (!this.executioner.isAlive() || !this.rescuer.isAlive()) {
					throw new SeppukuLtRtException(420, null);
				}
				if (!this.closed) {
					threadListDetectFaults();
				}
				if (!this.closed) {
					repairAtypicallyStoppedThreads();
				}
				if (!this.closed) {
					stopThreadsPausedWithoutWork();
				}
				if (!this.closed) {
					cleanPool();
				}
				if (!this.closed) {
					ensuresMinimalThread();
				}
				LOG.debug("lt.End");
			} catch (final ImplementationLtRtException e) {
				e.printStackTrace();
				throw new SeppukuLtRtException(420, e);
			} catch (final InterruptedException e) {
				LOG.debug("#0", e);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		} else {
			throw new ImplementationLtRtException("lt.ThreadNormalizerRule");
		}
	}

	/**
	 * Create the minimum threads objects.
	 *
	 * @throws InterruptedException if interrupted while waiting
	 */
	private void ensuresMinimalThread() throws InterruptedException {
		try {
			for (int i = this.numThread; i < this.minNumThread; i++) {
				addNewThread();
			}
		} catch (final ClosedLtRtException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 * Removes damaged threads of the pool and adds new clean to replace.
	 *
	 * @throws InterruptedException if interrupted while waiting
	 */
	private void cleanPool() throws InterruptedException {
		PoolEmbebedAgnosticThread peat = null;
		PoolEmbebedAgnosticThread newPeat;
		int tmIndex;
		while (!this.closed && this.threadDamagedQueue.size() > 0) {
			tmIndex = -1;
			newPeat = newThread();
			try {
				offerThread(newPeat);
				peat = this.threadDamagedQueue.poll();
			} catch (final InterruptedException e) {
				newPeat.closeAsync();
				throw e;
			} catch (final ClosedLtRtException e) {
				throw new ImplementationLtRtException(e);
			}
			if (peat != null) {
				peat.closeAsync();
				for (int i = 0; i < this.maxNumThread && tmIndex == -1; i++) {
					if (peat.equals(this.threadMonitor[i])) {
						tmIndex = i;
					}
				}
				if (tmIndex != -1) {
					this.threadMonitor[tmIndex] = newPeat;
				} else {
					throw new ImplementationLtRtException();
				}
			} else {
				throw new ImplementationLtRtException();
			}
		}
	}

	/**
	 * Reduce alive threads nicely.
	 *
	 * @throws InterruptedException if interrupted while waiting
	 */
	private void stopThreadsPausedWithoutWork() throws InterruptedException {
		PoolEmbebedAgnosticThread peat;
		int count = (this.maxNumThread - this.minNumThread) / 3;
		final int minimumLeft = (this.minNumThread > DEFAULT_MIN_NUM_THREAD) ? this.minNumThread
				: DEFAULT_MIN_NUM_THREAD + 1;
		while (count > 0 && this.threadWaitingQueue.size() > minimumLeft) {
			try {
				peat = poolThread();
			} catch (final ClosedLtRtException e) {
				throw new ImplementationLtRtException(e);
			}
			peat.closeAsync();
			setThreadToDamaged(peat);
			count--;
		}
	}

	/**
	 * Detect and repair atypically stopped threads
	 */
	private void repairAtypicallyStoppedThreads() {
		PoolEmbebedAgnosticThread peat;
		PoolTaskStruct ts;
		XThreadData xtd;
		for (int i = 0; i < this.maxNumThread && !this.closed; i++) {
			if (this.threadMonitor[i] != null && this.threadMonitor[i].isTerminated()) {
				peat = this.threadMonitor[i];
				peat.closeAsync();
				LOG.warn("lt.AtypicallyStoppedThreadDetected", peat.getName());
				ts = this.taskWorkingMap.get(peat.getId());
				if (ts != null) {
					xtd = (XThreadData) ts.getXThreadData();
					if (xtd != null) {
						try {
							xtd.getResult();
							xtd.setException(new PoolAgnosticThreadLtException("lt.AtypicallyStoppedThreadDetected",
									peat.getName()));
						} catch (final ParallelLtRtException e) {
							xtd.setException(new PoolAgnosticThreadLtException(e.getCause(),
									"lt.AtypicallyStoppedThreadDetected", peat.getName()));
						}
						xtd.stopAtypically();
						xtd.done();
					}
				}
				if (this.threadWorkingMap.containsKey(peat.getId())) {
					this.threadMonitor[i] = newThread();
					this.threadWorkingMap.put(peat.getId(), this.threadMonitor[i]);
					rescue(peat.getId());
				}
				peat = null;
			}
		}
	}

	/**
	 * Detect faults in the thread monitor list.
	 */
	private void threadListDetectFaults() {
		boolean flag = true;
		for (int i = 0; i < this.maxNumThread && !this.closed; i++) {
			if (this.threadMonitor[i] != null && !flag) {
				new ImplementationLtRtException("lt.FaultDetected");
			}
			if (this.threadMonitor[i] == null && flag) {
				flag = false;
			}
		}
	}

	private static final String METHOD_RESCUER_JOB = "rescuerJob";

	/**
	 * Recover threads that end his task.<br/>
	 * This method is only to be internally invoked by the thread rescuer.
	 */
	public void rescuerJob() {
		if (Thread.currentThread().getId() == this.rescuer.getId()) {
			try {
				LOG.debug("lt.Init");
				Long key = null;
				boolean poolKeyDone = false;
				boolean threadOfferDone = false;
				PoolEmbebedAgnosticThread thread;
				PoolTaskStruct ts;
				synchronized (this.rescuer) {
					this.rescuerPause = true;
				}
				while (!this.closed) {
					try {
						if (this.threadIdToRescue.isEmpty()) {
							Thread.sleep(RESCUER_SLEEP_TIME);
						}
					} catch (final InterruptedException e) {
						/* ignored */}
					synchronized (this.rescuer) {
						this.rescuerPause = false;
						Thread.interrupted();
					}
					while (!this.closed && !this.threadIdToRescue.isEmpty()) {
						thread = null;
						ts = null;
						// get id thread done
						poolKeyDone = false;
						while (!poolKeyDone) {
							try {
								key = this.threadIdToRescue.poll();
								poolKeyDone = true;
							} catch (final InterruptedException e) {
								/* ignored */}
						}
						LOG.trace("lt.ThreadOffer", key);
						// get thread done
						thread = this.threadWorkingMap.remove(key);
						// get task done
						ts = this.taskWorkingMap.remove(key);
						if (thread != null) {
							// put thread on waiting queue
							threadOfferDone = false;
							while (!threadOfferDone) {
								try {
									offerThread(thread);
									threadOfferDone = true;
								} catch (final InterruptedException e) {
									/* ignored */}
							}
						} else {
							new ImplementationLtRtException("lt.ThreadThreadNull", key);
						}
						if (ts != null) {
							ts.updateTask();
							// put task to work again
							addTaskToWork(ts);
						} else {
							new ImplementationLtRtException("lt.ThreadTaskNull", key);
						}
					}
					synchronized (this.rescuer) {
						this.rescuerPause = true;
					}
				}
				synchronized (this.rescuer) {
					this.rescuerPause = false;
					Thread.interrupted();
				}
				LOG.debug("lt.ThreadRescuerStop");
			} catch (final Exception e) {
				e.printStackTrace();
			}
		} else {
			throw new ImplementationLtRtException("lt.ThreadRescuerRule");
		}
	}

	private static final String METHOD_EXECUTIONER_JOB = "executionerJob";

	/**
	 * Give tasks to threads waiting for it.<br/>
	 * This method is only to be internally invoked by the thread executioner.
	 */
	public void executionerJob() throws ClosedLtRtException {
		if (Thread.currentThread().getId() == this.executioner.getId()) {
			try {
				LOG.debug("lt.Init");
				long sleepTime = EXECUTIONER_SLEEP_TIME;
				PoolEmbebedAgnosticThread threadTmp = null;
				PoolTaskStruct tsTmp = null;
				synchronized (this.executioner) {
					this.executionerPause = true;
				}
				while (!this.closed) {
					try {
						if (this.taskWaitingSet.isEmpty() || DateUtil.isFuture(this.taskWaitingSet.first().getDate())) {
							Thread.sleep(sleepTime);
						}
					} catch (final InterruptedException e) {
						/* ignored */}
					synchronized (this.executioner) {
						this.executionerPause = false;
						Thread.interrupted();
					}
					while (!this.closed && !this.taskWaitingSet.isEmpty()
							&& !DateUtil.isFuture(this.taskWaitingSet.first().getDate())) {
						tsTmp = this.taskWaitingSet.first();
						if (!tsTmp.getXThreadData().isAtypicallyDone()) {
							try {
								threadTmp = poolThread();
								if (!this.closed) {
									LOG.trace("lt.ThreadTaskWork", tsTmp, tsTmp.getDate().getTime());
									this.taskWorkingMap.put(threadTmp.getId(), tsTmp);
									threadTmp.workOn((ThreadData) tsTmp.getXThreadData());
									this.taskWaitingSet.remove(tsTmp);
								}
							} catch (final AgnosticThreadLtException e) {
								this.taskWorkingMap.remove(threadTmp.getId());
								LOG.error("#0", e);
								setThreadToDamaged(threadTmp);
							} catch (final InterruptedException e) {
								LOG.trace("#0", e);
							}
						} else {
							this.taskWaitingSet.remove(tsTmp);
							LOG.trace("lt.ThreadTaskLeaveAtypically",
									tsTmp.getXThreadData().getInvokeData().getMethod().getName());
						}
					}
					if (!this.taskWaitingSet.isEmpty()) {
						sleepTime = this.taskWaitingSet.first().getDate().getTime() - DateUtil.nowTime();
						if (sleepTime < 0) {
							sleepTime = 1;
						}
					} else {
						sleepTime = EXECUTIONER_SLEEP_TIME;
					}
					synchronized (this.executioner) {
						this.executionerPause = true;
					}
				}
				synchronized (this.executioner) {
					this.executionerPause = false;
					Thread.interrupted();
				}
				LOG.debug("lt.ThreadExecutionerStop");
			} catch (final Exception e) {
				e.printStackTrace();
			}
		} else {
			throw new ImplementationLtRtException("lt.ThreadExecutionerRule");
		}
	}

	/**
	 * Puts the <code>thread</code> in parameter in the damage queue.
	 *
	 * @param thread
	 * @throws ClosedLtRtException if the damage queue is closed
	 */
	private void setThreadToDamaged(final PoolEmbebedAgnosticThread thread) throws ClosedLtRtException {
		if (thread != null) {
			boolean offerDone = false;
			while (!offerDone) {
				try {
					this.threadDamagedQueue.offer(thread);
					offerDone = true;
				} catch (final ClassCastException e) {
					new ImplementationLtRtException(e);
				} catch (final NullPointerException e) {
					new ImplementationLtRtException(e);
				} catch (final IllegalArgumentException e) {
					new ImplementationLtRtException(e);
				} catch (final InterruptedException e1) {
					/* ignored */
				}
			}
		}
	}

	/**
	 * Puts the thread that invoked in the working map.
	 */
	void putWorking() {
		this.taskWaitingSet.remove(this.taskWorkingMap.get(Thread.currentThread().getId()));
		this.threadWorkingMap.put(Thread.currentThread().getId(), (PoolEmbebedAgnosticThread) Thread.currentThread());
	}

	/**
	 * Puts the thread that invoked in line to be rescued.
	 *
	 * @throws ClosedLtRtException if the rescue queue is closed
	 */
	void rescueMe() throws ClosedLtRtException {
		if (this.threadWorkingMap.containsKey(Thread.currentThread().getId())) {
			rescue(Thread.currentThread().getId());
		}
	}

	/**
	 * Puts <code>id</code> at rescue queue.
	 *
	 * @param id
	 * @throws ClosedLtRtException if the rescue queue is closed
	 */
	private void rescue(final Long id) throws ClosedLtRtException {
		try {
			Thread.interrupted();
			this.threadIdToRescue.offer(id);
			synchronized (this.rescuer) {
				if (this.rescuerPause) {
					this.rescuer.interrupt();
				}
			}
		} catch (final ClassCastException e) {
			new ImplementationLtRtException(e);
		} catch (final NullPointerException e) {
			new ImplementationLtRtException(e);
		} catch (final IllegalArgumentException e) {
			new ImplementationLtRtException(e);
		} catch (final InterruptedException e) {
			new ImplementationLtRtException(e);
		}
	}

	/**
	 * Adds work to the queue of the pool.
	 *
	 * @param xThreadData a {@link leitej.thread.XThreadDataItf XThreadDataItf}
	 *                    object specifying the work to be done.
	 * @throws PoolAgnosticThreadLtException if the pool has already closed.
	 * @throws SeppukuLtRtException          exit(420) if encounters the control
	 *                                       thread of the pool not alive
	 * @throws IllegalArgumentLtRtException  if <code>xThreadData</code> parameter
	 *                                       is null
	 */
	public void workOn(final XThreadData xThreadData)
			throws PoolAgnosticThreadLtException, SeppukuLtRtException, IllegalArgumentLtRtException {
		if (this.closed || this.closedWorkOn) {
			throw new PoolAgnosticThreadLtException(new ClosedLtRtException("lt.ThreadAlreadyClose"));
		}
		if (!this.normalizer.isAlive()) {
			throw new SeppukuLtRtException(420, null);
		}
		if (xThreadData == null) {
			throw new IllegalArgumentLtRtException("lt.ThreadWorkOnNull");
		}
		addTaskToWork(new PoolTaskStruct(xThreadData));
	}

	private void addTaskToWork(final PoolTaskStruct ts) {
		if (ts.getDate() != null) {
			this.taskWaitingSet.add(ts);
			LOG.trace("lt.ThreadTaskEnter", ts, this.taskWaitingSet.size());
			synchronized (this.executioner) {
				if (this.executionerPause) {
					this.executioner.interrupt();
				}
			}
		} else {
			LOG.trace("lt.ThreadTaskLeave", ts, this.taskWaitingSet.size());
		}
	}

}

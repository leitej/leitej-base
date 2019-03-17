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

package leitej.util;

import java.util.ArrayList;
import java.util.List;

import leitej.exception.IllegalStateLtRtException;
import leitej.log.Logger;

/**
 *
 * @author Julio Leite
 */
public final class ThreadLock {

	private static final int SLEEP_STEP_TIME_MILIS = 60000;

	private static final Logger LOG = Logger.getInstance();

	private final long id;
	private volatile int locked;
	private volatile Thread threadLocker;
	private final List<Thread> threadsBlocked;

	public ThreadLock() {
		this.id = DateUtil.generateUniqueNumberPerJVM();
		this.locked = 0;
		this.threadLocker = null;
		this.threadsBlocked = new ArrayList<>();
	}

	private boolean allowPassLock() {
		synchronized (this) {
			if (this.locked == 0 && Thread.currentThread().equals(this.threadsBlocked.get(0))) {
				this.threadLocker = this.threadsBlocked.remove(0);
				Thread.interrupted();
				this.locked++;
				return true;
			} else {
				return false;
			}
		}
	}

	public void lock() throws InterruptedException {
		LOG.trace("id: #0, #1", this.id, Thread.currentThread());
		synchronized (this) {
			if (this.locked != 0 && Thread.currentThread().equals(this.threadLocker)) {
				this.locked++;
				return;
			}
			LOG.debug("id: #0 - Wait for: #1", this.id, this.threadLocker);
			this.threadsBlocked.add(Thread.currentThread());
		}
		try {
			while (!allowPassLock()) {
				Thread.sleep(SLEEP_STEP_TIME_MILIS);
			}
		} catch (final InterruptedException e) {
			if (!allowPassLock()) {
				throw e;
			}
		}
	}

	public void unlock() {
		LOG.trace("id: #0, #1", this.id, Thread.currentThread());
		synchronized (this) {
			if (this.locked != 0 && Thread.currentThread().equals(this.threadLocker)) {
				this.locked--;
				if (this.locked == 0) {
					try {
						if (!this.threadsBlocked.isEmpty()) {
							this.threadsBlocked.get(0).interrupt();
						}
					} catch (final SecurityException e) {
						LOG.error("#0", e);
					}
				}
			} else {
				throw new IllegalStateLtRtException();
			}
		}
	}

}

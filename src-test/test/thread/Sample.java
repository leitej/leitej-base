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

import leitej.util.data.SyncCounter;

public class Sample {

	private volatile static Long scount = 0L;
	private SyncCounter count = new SyncCounter(0, 0, Integer.MAX_VALUE);

	private volatile static Boolean assync = Boolean.FALSE;
	private volatile static Long threadId = null;

	public synchronized void reset() {
		synchronized (Sample.class) {
			scount = 0L;
		}
		this.count = new SyncCounter(0, 0, Integer.MAX_VALUE);
		assync = Boolean.FALSE;
		threadId = null;
	}

	public void sleep2sec() {
		try {
			Thread.sleep(2000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static Boolean getAssync() {
		return assync;
	}

	public static void collisionTest() {
		if (threadId != null && Thread.currentThread().getId() != threadId) {
			assync = Boolean.TRUE;
		}
		threadId = Thread.currentThread().getId();
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		threadId = null;
	}

	public synchronized static void synchronizedTest() {
		if (threadId != null && Thread.currentThread().getId() != threadId) {
			assync = Boolean.TRUE;
		}
		threadId = Thread.currentThread().getId();
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		threadId = null;
	}

	public synchronized static Long getScount() {
		return scount;
	}

	public synchronized static void hitScount() {
		Sample.scount++;
	}

	public int getCount() {
		return this.count.getCount();
	}

	public int getCountDelay5() {
		try {
			Thread.sleep(5000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return getCount();
	}

	public void hitCount() {
		this.count.inc();
	}

	public void hitCountAssyncDelay2() {
		try {
			Thread.sleep(2000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		hitCount();
	}

}

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

package leitej.util.data;

import leitej.exception.IllegalStateLtRtException;
import leitej.util.DateUtil;

/**
 *
 * @author Julio Leite
 */
public final class AutoRecall {

	public static final long NO_TIME_LIMIT = Long.MAX_VALUE;
	private static final long DEFAULT_FIRST_PERIOD = 8L * 1000;
	private static final long DEFAULT_MAX_PERIOD = DateUtil.ONE_WEEK_IN_MS;

	private final boolean hasLimit;
	private final long timeLimit;
	private final long firstPeriod;
	private final long maxPeriod;

	private long period;
	private long initRecall;
	private long beforeLastRecall;
	private long lastRecall;

	public AutoRecall() {
		this(DEFAULT_FIRST_PERIOD, DEFAULT_MAX_PERIOD, NO_TIME_LIMIT);
	}

	public AutoRecall(final long fixedPeriod) throws IllegalArgumentException {
		this(fixedPeriod, fixedPeriod, NO_TIME_LIMIT);
	}

	public AutoRecall(final long fixedPeriod, final long timeLimit) throws IllegalArgumentException {
		this(fixedPeriod, fixedPeriod, timeLimit);
	}

	public AutoRecall(final long firstPeriod, final long maxPeriod, final long timeLimit)
			throws IllegalArgumentException {
		if (timeLimit < 0 || firstPeriod <= 0 || maxPeriod < 0) {
			throw new IllegalArgumentException();
		}
		this.hasLimit = NO_TIME_LIMIT != timeLimit;
		this.firstPeriod = firstPeriod;
		this.maxPeriod = maxPeriod;
		this.timeLimit = timeLimit;
		init();
	}

	public synchronized void init() {
		if (this.initRecall != 0) {
			this.initRecall = 0;
			this.lastRecall = 0;
		}
	}

	public synchronized boolean recall() {
		if (isOver()) {
			return false;
		}
		boolean result;
		final long now = DateUtil.nowTime();
		if (this.initRecall == 0) {
			result = true;
			this.period = this.firstPeriod;
			this.initRecall = now;
			this.beforeLastRecall = now;
			this.lastRecall = now;
		} else {
			result = now - this.lastRecall > this.period;
			if (result) {
				if (this.maxPeriod > this.period && (this.lastRecall - this.beforeLastRecall > this.period)) {
					this.period = this.period << 1;
				}
				this.beforeLastRecall = this.lastRecall;
				this.lastRecall = now;
			}
		}
		return result;
	}

	public void waitNextRecall() throws IllegalStateLtRtException, InterruptedException {
		if (isOver()) {
			throw new IllegalStateLtRtException();
		}
		while (!recall()) {
			Thread.sleep(this.period - (DateUtil.nowTime() - this.lastRecall));
		}
	}

	public synchronized boolean isOver() {
		return this.hasLimit && (this.lastRecall - this.initRecall > this.timeLimit);
	}

}

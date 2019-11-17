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

import java.io.Serializable;

/**
 * An useful counter class, multi-thread safe.
 *
 * @author Julio Leite
 */
public class SyncCounter implements Serializable {

	private static final long serialVersionUID = 8787110110825967536L;

	private volatile int count;
	private final int min;
	private final int max;

	/**
	 * Creates a new instance of SyncCounter.
	 */
	public SyncCounter() {
		this(Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
	}

	/**
	 * Creates a new instance of SyncCounter.
	 *
	 * @param startAt initial value of counter
	 * @param min     minimum value to be reached by the counter
	 * @param max     maximum value to be reached by the counter
	 * @throws IllegalArgumentException if arguments give a illegal state for this
	 *                                  counter
	 */
	public SyncCounter(final int min, final int startAt, final int max) throws IllegalArgumentException {
		if (min > max || startAt < min || startAt > max) {
			throw new IllegalArgumentException();
		}
		this.min = min;
		this.max = max;
		this.count = startAt;
	}

	/**
	 * Increases the value of the counter.
	 *
	 * @return false if the counter already reaches the maximum value allowed and
	 *         don't change the counter
	 */
	public synchronized boolean inc() {
		if (this.count < this.max) {
			this.count++;
			return true;
		}
		return false;
	}

	/**
	 * Decreases the value of the counter.
	 *
	 * @return false if the counter already reaches the minimum value allowed and
	 *         don't change the counter
	 */
	public synchronized boolean dec() {
		if (this.count > this.min) {
			this.count--;
			return true;
		}
		return false;
	}

	/**
	 * Gives the value of the counter.
	 *
	 * @return value
	 */
	public synchronized int getCount() {
		return this.count;
	}

}

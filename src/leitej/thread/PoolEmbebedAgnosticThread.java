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

import leitej.exception.IllegalArgumentLtRtException;

/**
 * PoolEmbebedAgnosticThread
 *
 * @author Julio Leite
 */
final class PoolEmbebedAgnosticThread extends AgnosticThread {

	private final PoolAgnosticThread myPool;
	private boolean workDoneFlag = false;

	/**
	 * Creates a new instance of <code>PoolEmbebedAgnosticThread</code>.
	 *
	 * @param myPool
	 * @throws IllegalArgumentLtRtException if <code>myPool</code> is null
	 */
	PoolEmbebedAgnosticThread(final String name, final PoolAgnosticThread myPool) {
		super(name, true);
		if (myPool == null) {
			throw new IllegalArgumentLtRtException("lt.ThreadEmbebedPoolNull");
		}
		this.myPool = myPool;
	}

	boolean isYourPool(final PoolAgnosticThread pool) {
		return this.myPool.equals(pool);
	}

	@Override
	protected void startTask() {
		this.myPool.putWorking();
		this.workDoneFlag = true;
		super.startTask();
	}

	@Override
	protected void pause() {
		if (this.workDoneFlag) {
			this.myPool.rescueMe();
			this.workDoneFlag = false;
		}
		super.pause();
	}

}

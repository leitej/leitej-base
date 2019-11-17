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

import java.io.Serializable;
import java.util.Date;

import leitej.exception.ImplementationLtRtException;
import leitej.log.Logger;
import leitej.util.DateUtil;

/**
 * PoolTaskStruct
 *
 * @author Julio Leite
 */
class PoolTaskStruct implements Comparable<PoolTaskStruct>, Serializable {

	private static final long serialVersionUID = 4061565369529414612L;

	private static final Logger LOG = Logger.getInstance();

	private Date date;
	private final XThreadData xThreadData;
	private long compareUniqueId;

	/**
	 * Creates a new instance of PoolTaskStruct.
	 *
	 * @param xThreadData
	 */
	PoolTaskStruct(final XThreadData xThreadData) {
		this.xThreadData = xThreadData;
		this.compareUniqueId = 0;
		updateTask();
		LOG.trace("lt.NewInstance");
	}

	@Override
	synchronized public int compareTo(final PoolTaskStruct o) {
		int comp = 0;
		if (!this.equals(o)) {
			if (this.date != null) {
				comp = this.date.compareTo(o.date);
			}
			if (comp == 0) {
				comp = this.hashCode() - o.hashCode();
				if (comp == 0) {
					if (this.compareUniqueId == 0) {
						this.compareUniqueId = DateUtil.generateUniqueNumberPerJVM();
					}
					if (o.compareUniqueId == 0) {
						o.compareUniqueId = DateUtil.generateUniqueNumberPerJVM();
					}
					comp = (int) (this.compareUniqueId - o.compareUniqueId);
					if (comp == 0) {
						throw new ImplementationLtRtException();
					}
				}
			}
		}
		return comp;
	}

	synchronized void updateTask() {
		this.date = this.xThreadData.nextExecTime();
	}

	synchronized Date getDate() {
		return this.date;
	}

	XThreadData getXThreadData() {
		return this.xThreadData;
	}

	@Override
	public String toString() {
		return (new StringBuilder()).append(super.toString()).append(".")
				.append(this.xThreadData.getInvokeData().getMethod().getName()).toString();
	}

}

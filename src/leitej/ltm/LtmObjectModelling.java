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

package leitej.ltm;

import leitej.ltm.annotation.LongTermMemory;
import leitej.ltm.exception.LtmLtRtException;

/**
 *
 *
 * @author Julio Leite
 */
@LongTermMemory
public abstract interface LtmObjectModelling {

	static final String GET_ID_METHOD_NAME = "getId";
	static final String SET_ID_METHOD_NAME = "setId";
	static final String ID_DATA_NAME = "id";
	static final Class<?> GET_ID_RETURN_CLASS = Long.class;

	public abstract Long getId();

	public abstract void setId(Long id);

	abstract boolean isNew();

	/**
	 *
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close <br/>
	 *                          +Cause ObjectPoolLtException if can't instantiate a
	 *                          new connection <br/>
	 *                          +Cause InterruptedException if interrupted while
	 *                          waiting for lock or waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection <br/>
	 */
	public abstract void save() throws LtmLtRtException;

	/**
	 *
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close <br/>
	 *                          +Cause ObjectPoolLtException if can't instantiate a
	 *                          new connection <br/>
	 *                          +Cause InterruptedException if interrupted while
	 *                          waiting for lock or waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection <br/>
	 */
	public abstract void remove() throws LtmLtRtException;

	/**
	 *
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close <br/>
	 *                          +Cause ObjectPoolLtException if can't instantiate a
	 *                          new connection <br/>
	 *                          +Cause InterruptedException if interrupted while
	 *                          waiting for lock or waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection <br/>
	 */
	public abstract void refresh() throws LtmLtRtException;

	public abstract int compareTo(LtmObjectModelling o);
}

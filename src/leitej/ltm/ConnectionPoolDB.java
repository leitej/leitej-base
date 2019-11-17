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

import java.sql.SQLException;

import leitej.exception.ClosedLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.util.data.AbstractObjectPool;

/**
 *
 *
 * @author Julio Leite
 */
final class ConnectionPoolDB extends AbstractObjectPool<ConnectionDB> {

	private static final long serialVersionUID = 101082769248443854L;

	private final AbstractLongTermMemory ltmManager;

	/**
	 * .<br/>
	 *
	 * @param manager
	 * @param maxConnections
	 * @throws IllegalArgumentException if maxObjects is less than 1
	 */
	<M extends AbstractLongTermMemory> ConnectionPoolDB(final AbstractLongTermMemory manager, final int maxConnections)
			throws IllegalArgumentException {
		super(maxConnections);
		this.ltmManager = manager;
	}

	@Override
	protected ConnectionDB newObject() throws ObjectPoolLtException {
		ConnectionDB result;
		try {
			result = new ConnectionDB(this, this.ltmManager.getDriverConnection());
			result.logMetaData();
		} catch (final SQLException e) {
			throw new ObjectPoolLtException(e);
		}
		return result;
	}

	@Override
	protected boolean isInactive(final ConnectionDB obj) {
		return obj.isInvalid();
	}

	@Override
	protected void deactivate(final ConnectionDB obj) {
		obj.close();
	}

	@Override
	protected ConnectionDB poll() throws ClosedLtRtException, ObjectPoolLtException, InterruptedException {
		return super.poll();
	}

	@Override
	protected void offer(final ConnectionDB conn) throws InterruptedException {
		try {
			super.offer(conn);
		} catch (final IllegalArgumentException e) {
			new ImplementationLtRtException(e);
		}
	}

	@Override
	protected void close() {
		super.close();
	}

	final AbstractLongTermMemory getLtmManager() {
		return this.ltmManager;
	}

}

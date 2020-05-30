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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leitej.exception.ClosedLtRtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.LtmLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.util.data.AbstractDataProxyHandler;

/**
 * @author Julio Leite
 *
 */
public final class DataProxyHandler extends AbstractDataProxyHandler<LtmObjectModelling> {

	private static final long serialVersionUID = 4926068475584840536L;

	private static Short SCOPE = 0;
	static final String ID = "id";

	private static final DataMemoryPool MEM_POOL = DataMemoryPool.getInstance();

	static final void eraseAll() throws SQLException, ClosedLtRtException, ObjectPoolLtException, InterruptedException {
		DataMemoryConnection conn = null;
		try {
			conn = MEM_POOL.poll();
			synchronized (SCOPE) {
				SCOPE++;
				conn.eraseAll();
			}
		} finally {
			if (conn != null) {
				MEM_POOL.offer(conn);
			}
		}
	}

	static final <T extends LtmObjectModelling> void delete(final Class<T> ltmClass, final long id)
			throws ClosedLtRtException, ObjectPoolLtException, InterruptedException, SQLException {
		DataMemoryConnection conn = null;
		try {
			conn = MEM_POOL.poll();
			conn.deleteRecord(ltmClass, id);
		} finally {
			if (conn != null) {
				MEM_POOL.offer(conn);
			}
		}
	}

	private final Short scope;
	private final long id;
	private final Map<String, Object> data;
	private LtmLtRtException occuredException;

	protected <T extends LtmObjectModelling> DataProxyHandler(final Class<T> ltmClass)
			throws IllegalArgumentLtRtException, ClosedLtRtException, ObjectPoolLtException, InterruptedException,
			SQLException {
		this(ltmClass, null);
	}

	protected <T extends LtmObjectModelling> DataProxyHandler(final Class<T> ltmClass, final Long id)
			throws IllegalArgumentLtRtException, ClosedLtRtException, ObjectPoolLtException, InterruptedException,
			SQLException {
		super(ltmClass);
		this.data = new HashMap<>();
		DataMemoryConnection conn = null;
		try {
			conn = MEM_POOL.poll();
			synchronized (SCOPE) {
				this.scope = SCOPE;
				conn.initialize(ltmClass, this);
				if (id == null) {
					this.id = conn.newRecord(ltmClass);
					this.data.put(ID, this.id);
				} else {
					this.id = id;
					conn.fetchRecord(ltmClass, this.id, this.data);
				}
			}
		} finally {
			if (conn != null) {
				MEM_POOL.offer(conn);
			}
		}
	}

	@Override
	protected Object get(final String dataName) {
		Object result;
		synchronized (this) {
			if (this.occuredException != null) {
				throw this.occuredException;
			}
			result = this.data.get(dataName);
		}
		// TODO other long term memory
		// TODO special column stream
		// TODO tabelas intermedias -set-map
		synchronized (SCOPE) {
			if (!SCOPE.equals(this.scope)) {
				throw new LtmLtRtException("This object was erased from long term memory");
			}
		}
		return result;
	}

	@Override
	protected void set(final String dataName, final Object value) {
		if (ID.equals(dataName)) {
			throw new LtmLtRtException("#0 can not be changed", ID);
		}
		synchronized (this) {
			if (this.occuredException != null) {
				throw this.occuredException;
			}
			try {
				DataMemoryConnection conn = null;
				try {
					conn = MEM_POOL.poll();
					conn.updateRecord(getDataInterfaceClass(), this.id, dataName, value);
				} finally {
					if (conn != null) {
						MEM_POOL.offer(conn);
					}
				}
			} catch (SQLException | ClosedLtRtException | ObjectPoolLtException | InterruptedException e) {
				this.occuredException = new LtmLtRtException(e);
				throw this.occuredException;
			}
			this.data.put(dataName, value);
		}
		// TODO other long term memory
		// TODO special column stream
		// TODO tabelas intermedias -set-map
		synchronized (SCOPE) {
			if (!SCOPE.equals(this.scope)) {
				throw new LtmLtRtException("This object was erased from long term memory");
			}
		}
	}

	<I extends LtmObjectModelling> Class<I> getInterface() {
		return getDataInterfaceClass();
	}

	long getId() {
		return this.id;
	}

	List<String> getDatanameList() {
		return dataNameList();
	}

	Class<?> getType(final String dataname) {
		return dataMethodsGetSet(dataname)[0].getReturnType();
	}

}

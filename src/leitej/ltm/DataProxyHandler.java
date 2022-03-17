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
import leitej.exception.ImplementationLtRtException;
import leitej.exception.LtmLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.util.data.AbstractDataProxyHandler;
import leitej.util.data.Obfuscate;
import leitej.util.data.ObfuscateUtil;

/**
 * @author Julio Leite
 *
 */
final class DataProxyHandler extends AbstractDataProxyHandler<LtmObjectModelling> {

	private static final long serialVersionUID = 4926068475584840536L;

	private static final LongTermMemory LTM = LongTermMemory.getInstance();

	private static final Map<Class<?>, PreparedClass> PREP_CLASS_MAP = new HashMap<>();

	private static volatile short SCOPE = 0;
	static final String LTM_ID = "ltmId";
	static final String SET_ID = "setId";
	static final String SET_VALUE = "setValue";

	private static final DataMemoryPool MEM_POOL = DataMemoryPool.getInstance();

	private static <T extends LtmObjectModelling> PreparedClass initialize(final DataMemoryConnection conn,
			final DataProxyHandler dph) throws SQLException, ClassNotFoundException {
		PreparedClass result;
		final Class<T> ltmClass = dph.getDataInterfaceClass();
		synchronized (ltmClass) {
			result = PREP_CLASS_MAP.get(ltmClass);
			if (result == null) {
				result = new PreparedClass(dph);
				PREP_CLASS_MAP.put(ltmClass, result);
				conn.initialize(result);
			}
		}
		return result;
	}

	static final void eraseAll() throws SQLException, ClosedLtRtException, ObjectPoolLtException, InterruptedException {
		DataMemoryConnection conn = null;
		try {
			conn = MEM_POOL.poll();
			synchronized (DataProxyHandler.class) {
				SCOPE++;
				PREP_CLASS_MAP.clear();
				conn.eraseAll();
			}
		} finally {
			if (conn != null) {
				MEM_POOL.offer(conn);
			}
		}
	}

	static final void delete(final PreparedClass preparedClass, final long id)
			throws ClosedLtRtException, ObjectPoolLtException, InterruptedException, SQLException {
		DataMemoryConnection conn = null;
		try {
			conn = MEM_POOL.poll();
			conn.deleteRecord(preparedClass, id);
		} finally {
			if (conn != null) {
				MEM_POOL.offer(conn);
			}
		}
	}

	private final short scope;
	private final PreparedClass preparedClass;
	private final long ltmId;
	private final Map<String, Object> data;
	private LtmLtRtException occuredException;

	protected <T extends LtmObjectModelling> DataProxyHandler(final Class<T> ltmClass)
			throws IllegalArgumentLtRtException, ClosedLtRtException, ObjectPoolLtException, InterruptedException,
			SQLException, ClassNotFoundException {
		this(ltmClass, null);
	}

	protected <T extends LtmObjectModelling> DataProxyHandler(final Class<T> ltmClass, final Long id)
			throws IllegalArgumentLtRtException, ClosedLtRtException, ObjectPoolLtException, InterruptedException,
			SQLException, ClassNotFoundException {
		super(ltmClass);
		this.data = new HashMap<>();
		DataMemoryConnection conn = null;
		try {
			conn = MEM_POOL.poll();
			synchronized (DataProxyHandler.class) {
				this.scope = SCOPE;
				this.preparedClass = initialize(conn, this);
				if (id == null) {
					this.ltmId = conn.newRecord(this.preparedClass);
					this.data.put(LTM_ID, this.ltmId);
				} else {
					this.ltmId = id;
					conn.fetchRecord(this, this.data);
				}
			}
		} finally {
			if (conn != null) {
				MEM_POOL.offer(conn);
			}
		}
	}

	void isValid() {
		synchronized (DataProxyHandler.class) {
			if (this.occuredException != null) {
				throw this.occuredException;
			} else if (SCOPE != this.scope) {
				this.occuredException = new LtmLtRtException("This object was erased from long term memory");
				throw this.occuredException;
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
			final Class<LtmObjectModelling> ltmClass = this.preparedClass.getLongTermMemoryClass(dataName);
			if (ltmClass == null) {
				result = this.data.get(dataName);
				if (result == null && this.preparedClass.getColumnsSet().contains(dataName)) {
					result = InitialSet.instantiateSet(this, dataName);
					this.data.put(dataName, result);
				}
			} else {
				final Object fetchId = this.data.get(dataName);
				if (fetchId == null) {
					result = null;
				} else {
					result = LTM.fetch(ltmClass, Long.class.cast(fetchId).longValue());
				}
			}
		}
		this.isValid();
		return result;
	}

	@Override
	protected void set(final String dataName, final Object value) {
		if (LTM_ID.equals(dataName) || this.preparedClass.getColumnsSet().contains(dataName)) {
			throw new LtmLtRtException("#0 can not be changed", dataName);
		}
		synchronized (this) {
			if (this.occuredException != null) {
				throw this.occuredException;
			}
			try {
				DataMemoryConnection conn = null;
				try {
					conn = MEM_POOL.poll();
					conn.updateRecord(this.preparedClass, this.ltmId, dataName, value, this.data.get(dataName));
				} finally {
					if (conn != null) {
						MEM_POOL.offer(conn);
					}
				}
			} catch (SQLException | ClosedLtRtException | ObjectPoolLtException | InterruptedException e) {
				this.occuredException = new LtmLtRtException(e);
				throw this.occuredException;
			}
			final Class<LtmObjectModelling> ltmClass = this.preparedClass.getLongTermMemoryClass(dataName);
			if (ltmClass == null || value == null) {
				this.data.put(dataName, value);
			} else {
				this.data.put(dataName, LtmObjectModelling.class.cast(value).getLtmId());
			}
		}
		this.isValid();
	}

	@Override
	protected <O> O deObfuscate(final Obfuscate annot, final O value) {
		return ObfuscateUtil.unHide(annot, value);
	}

	@Override
	protected <O> O obfuscate(final Obfuscate annot, final O value) {
		return ObfuscateUtil.hide(annot, value);
	}

	@Override
	protected boolean isObfuscated(final Object value) {
		throw new ImplementationLtRtException("Unexpecting use of this method!");
	}

	PreparedClass getPreparedClass() {
		return this.preparedClass;
	}

	<I extends LtmObjectModelling> Class<I> getInterface() {
		return getDataInterfaceClass();
	}

	long getLtmId() {
		return this.ltmId;
	}

	List<String> getDatanameList() {
		return dataNameList();
	}

	Class<?> getReturnClass(final String dataname) {
		return dataMethodsGetSet(dataname)[0].getReturnType();
	}

}

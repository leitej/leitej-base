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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Map;

import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.LtmLtRtException;
import leitej.log.Logger;

/**
 * @author Julio Leite
 *
 */
final class DataMemoryConnection {

	private static final Logger LOG = Logger.getInstance();

	static final void hibernate() throws SQLException, IOException {
		LOG.debug("shutdown init");
		HsqldbUtil.dbShutdown();
		LOG.debug("shutdown end");
	}

	private final Connection conn;

	DataMemoryConnection() throws SQLException {
		LOG.debug("new connection");
		this.conn = HsqldbUtil.newConnection();
		this.conn.setAutoCommit(false);
		this.conn.setReadOnly(false);
		this.conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
	}

	void initialize(final PreparedClass prepClass) throws SQLException {
		HsqldbUtil.initialize(this.conn, prepClass);
	}

	void initializeSet(final PreparedClass prepClass, final String datanameSet) throws SQLException {
		HsqldbUtil.createTableSet(this.conn, prepClass, datanameSet);
	}

	void eraseAll() throws SQLException {
		HsqldbUtil.dropSchema(this.conn);
		LargeMemory.eraseAll();
	}

	boolean isInactive() {
		try {
			return this.conn.isClosed();
		} catch (final SQLException e) {
			LOG.error("#0", e);
			return true;
		}
	}

	void close() throws SQLException {
		LOG.debug("connection close");
		try {
			this.conn.rollback();
		} finally {
			this.conn.close();
		}
	}

	long newRecord(final PreparedClass pClass) throws SQLException {
		final Statement stt = this.conn.createStatement();
		final boolean hasRS = stt.execute(pClass.getInsertNewRow());
		final long result;
		if (hasRS) {
			final ResultSet rs = stt.getResultSet();
			if (rs.next()) {
				result = rs.getLong(1);
				if (rs.next()) {
					throw new ImplementationLtRtException();
				}
			} else {
				throw new IllegalStateLtRtException();
			}
			rs.close();
			stt.close();
			this.conn.commit();
		} else {
			this.conn.rollback();
			throw new IllegalStateLtRtException();
		}
		return result;
	}

	void fetchRecord(final DataProxyHandler dph, final Map<String, Object> data) throws SQLException {
		LOG.trace("#0: #1", dph.getInterface(), dph.getLtmId());
		final PreparedClass pClass = dph.getPreparedClass();
		final PreparedStatement pStt = this.conn.prepareStatement(pClass.getSelectById());
		pStt.setLong(1, dph.getLtmId());
		final boolean hasRS = pStt.execute();
		if (hasRS) {
			final ResultSet rs = pStt.getResultSet();
			if (rs.next()) {
				DataMemoryUtil.map(rs, pClass, dph, data);
				if (rs.next()) {
					throw new ImplementationLtRtException();
				}
				rs.close();
			} else {
				throw new LtmLtRtException(dph.getInterface() + " do not remember id: " + dph.getLtmId());
			}
		} else {
			throw new IllegalStateLtRtException();
		}
		pStt.close();
		this.conn.rollback();
	}

	void updateRecord(final PreparedClass pClass, final long ltmId, final String field, final Object value,
			final Object prevValue) throws SQLException {
		final Class<LtmObjectModelling> ltmClass = pClass.getInterface();
		LOG.trace("#0: #1: #2", ltmClass, ltmId, field);
		final int dataPos = pClass.getDataNameList().indexOf(field);
		final PreparedStatement pStt = this.conn.prepareStatement(pClass.getUpdateColumnById().get(dataPos));
		final DataMemoryType type = pClass.getColumnTypeList().get(dataPos);
		if (DataMemoryType.ENUM.equals(type)) {
			HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), value.toString());
		} else if (DataMemoryType.LARGE_MEMORY.equals(type)) {
			if (prevValue != null) {
				LargeMemoryTracker.del(this.conn, ltmClass, ltmId, LargeMemory.class.cast(prevValue));
			}
			if (value == null) {
				HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), null);
			} else {
				HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), LargeMemory.class.cast(value).getId());
				LargeMemoryTracker.add(this.conn, ltmClass, ltmId, LargeMemory.class.cast(value));
			}
		} else if (DataMemoryType.LONG_TERM_MEMORY.equals(type)) {
			if (value == null) {
				HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), null);
			} else {
				HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), LtmObjectModelling.class.cast(value).getLtmId());
			}
		} else if (DataMemoryType.DATE.equals(type)) {
			if (value == null) {
				HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), null);
			} else {
				HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), Date.class.cast(value).getTime());
			}
		} else {
			HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), value);
		}
		pStt.setLong(2, ltmId);
		final int count = pStt.executeUpdate();
		if (count == 0) {
			this.conn.rollback();
			throw new LtmLtRtException(ltmClass + " do not remember id: " + ltmId);
		}
		pStt.close();
		this.conn.commit();
	}

	void deleteRecord(final PreparedClass pClass, final long ltmId) throws SQLException {
		final Class<LtmObjectModelling> ltmClass = pClass.getInterface();
		LOG.trace("#0: #1", ltmClass, ltmId);
		final PreparedStatement pStt = this.conn.prepareStatement(pClass.getDeleteById());
		pStt.setLong(1, ltmId);
		final int count = pStt.executeUpdate();
		if (count == 0) {
			this.conn.rollback();
			throw new LtmLtRtException(ltmClass + " do not remember id: " + ltmId);
		}
		pStt.close();
		LargeMemoryTracker.delFromLtmInstance(this.conn, ltmClass, ltmId);
		String dataname;
		String tablenameSet;
		DataMemoryType setType;
		for (int i = 0; i < pClass.getColumnsSet().size(); i++) {
			dataname = pClass.getColumnsSet().get(i);
			setType = pClass.getColumnsSetType(dataname);
			if (setType != null) {
				tablenameSet = pClass.getSetTablename(dataname);
				clearSetByLtmId(pClass.getDeleteOnSetByLtmId(dataname), tablenameSet, setType, ltmClass, ltmId);
			}
		}
		this.conn.commit();
	}

	void newLargeMemory(final LargeMemory largeMemory) throws SQLException {
		LargeMemoryTracker.init(this.conn, largeMemory);
		this.conn.commit();
		DataMemoryUtil.cacheLargeMemory(largeMemory);
	}

	Long getNextId(final String preparedStatement, final DataMemoryType[] types, final Object[] parameters,
			final long ltmId) throws SQLException {
		Long result = null;
		LOG.trace("preparedStatement: #0", preparedStatement);
		final PreparedStatement pStt = this.conn.prepareStatement(preparedStatement);
		pStt.setLong(1, ltmId);
		HsqldbUtil.setPrepStt(pStt, 2, types, parameters);
		if (pStt.execute()) {
			final ResultSet rSet = pStt.getResultSet();
			if (rSet.next()) {
				result = rSet.getLong(DataProxyHandler.LTM_ID);
			}
			if (rSet.next()) {
				this.conn.rollback();
				throw new ImplementationLtRtException();
			}
			rSet.close();
		} else {
			this.conn.rollback();
			throw new IllegalStateLtRtException();
		}
		pStt.close();
		this.conn.rollback();
		return result;
	}

	int countSet(final String pSttCount) throws SQLException {
		LOG.trace("pSttCount: #0", pSttCount);
		final Statement stt = this.conn.createStatement();
		final boolean hasRS = stt.execute(pSttCount);
		final int result;
		if (hasRS) {
			final ResultSet rs = stt.getResultSet();
			if (rs.next()) {
				result = rs.getInt(1);
				if (rs.next()) {
					throw new ImplementationLtRtException();
				}
			} else {
				throw new IllegalStateLtRtException();
			}
			rs.close();
			stt.close();
			this.conn.commit();
		} else {
			this.conn.rollback();
			throw new IllegalStateLtRtException();
		}
		return result;
	}

	boolean hasResultSet(final String pSttHasResult) throws SQLException {
		LOG.trace("pSttHasResult: #0", pSttHasResult);
		final boolean result;
		final Statement stt = this.conn.createStatement();
		if (stt.execute(pSttHasResult)) {
			final ResultSet rSet = stt.getResultSet();
			result = rSet.next();
			rSet.close();
			stt.close();
		} else {
			stt.close();
			this.conn.rollback();
			throw new IllegalStateLtRtException();
		}
		this.conn.rollback();
		return result;
	}

	private Object translate(final DataMemoryType dataType, final Object o) {
		if (dataType.equals(DataMemoryType.LONG_TERM_MEMORY)) {
			return LtmObjectModelling.class.cast(o).getLtmId();
		} else if (dataType.equals(DataMemoryType.LARGE_MEMORY)) {
			return LargeMemory.class.cast(o).getId();
		} else if (dataType.equals(DataMemoryType.DATE)) {
			return Date.class.cast(o).getTime();
		} else if (dataType.equals(DataMemoryType.ENUM)) {
			return o.toString();
		} else {
			return o;
		}
	}

	boolean containsValueSet(final String pSttSetContains, final DataMemoryType type, final Object other)
			throws SQLException {
		LOG.trace("pSttSetContains: #0", pSttSetContains);
		boolean result;
		final PreparedStatement pStt = this.conn.prepareStatement(pSttSetContains);
		HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), translate(type, other));
		if (pStt.execute()) {
			final ResultSet rSet = pStt.getResultSet();
			result = rSet.next();
			rSet.close();
			pStt.close();
			this.conn.rollback();
		} else {
			this.conn.rollback();
			throw new IllegalStateLtRtException();
		}
		return result;
	}

	<T extends LtmObjectModelling> boolean addValueSet(final String pSttSetAdd, final DataMemoryType type,
			final Object elem, final Class<T> ltmClass, final long ltmId) throws SQLException {
		LOG.trace("pSttSetAdd: #0", pSttSetAdd);
		final PreparedStatement pStt = this.conn.prepareStatement(pSttSetAdd);
		final Object o = translate(type, elem);
		HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), o);
		HsqldbUtil.setPrepStt(pStt, 2, type.getSqlType(), o);
		final boolean result = pStt.executeUpdate() != 0;
		pStt.close();
		if (DataMemoryType.LARGE_MEMORY.equals(type)) {
			LargeMemoryTracker.add(this.conn, ltmClass, ltmId, LargeMemory.class.cast(elem));
		}
		this.conn.commit();
		return result;
	}

	<T extends LtmObjectModelling> boolean removeValueSet(final String pSttSetRemove, final DataMemoryType type,
			final Object elem, final Class<T> ltmClass, final long ltmId) throws SQLException {
		LOG.trace("pSttSetRemove: #0", pSttSetRemove);
		final PreparedStatement pStt = this.conn.prepareStatement(pSttSetRemove);
		HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), translate(type, elem));
		final boolean result = pStt.executeUpdate() != 0;
		pStt.close();
		if (DataMemoryType.LARGE_MEMORY.equals(type)) {
			LargeMemoryTracker.del(this.conn, ltmClass, ltmId, LargeMemory.class.cast(elem));
		}
		this.conn.commit();
		return result;
	}

	void clearSetByLtmId(final String pSttSetClear, final String tablenameSet, final DataMemoryType setType,
			final Class<?> ltmClass, final long ltmId) throws SQLException {
		// update large memory tracker
		if (DataMemoryType.LARGE_MEMORY.equals(setType)) {
			LargeMemoryTracker.delFromLtmSetById(this.conn, tablenameSet, ltmClass, ltmId);
		}
		// clear set by ltm id
		LOG.trace("pSttSetClear: #0", pSttSetClear);
		final PreparedStatement pStt = this.conn.prepareStatement(pSttSetClear);
		pStt.setLong(1, ltmId);
		LOG.trace("delete count: #0", pStt.executeUpdate());
		pStt.close();
		this.conn.commit();
	}

	void prepareNext(final LtmSetIterator<?> it) throws SQLException {
		final PreparedStatement pStt = this.conn.prepareStatement(it.getpStt());
		pStt.setLong(1, it.getPositionID());
		if (pStt.execute()) {
			final ResultSet rSet = pStt.getResultSet();
			if (rSet.next()) {
				DataMemoryUtil.mapSetValue(rSet, it);
			}
			rSet.close();
		} else {
			this.conn.rollback();
			throw new IllegalStateLtRtException();
		}
		pStt.close();
		this.conn.rollback();
	}

}

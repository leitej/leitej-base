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

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import leitej.exception.ImplementationLtRtException;
import leitej.exception.LtmLtRtException;
import leitej.log.Logger;
import leitej.util.AgnosticUtil;
import leitej.util.machine.VMMonitor;

/**
 * @author Julio Leite
 *
 */
final class DataMemoryConnection {

	private static final Logger LOG = Logger.getInstance();

	static {
		LargeMemoryTracker.intialize();
	}

	static final void hibernate() throws SQLException {
		LOG.debug("shutdown init");
		HsqldbUtil.dbShutdown();
		LOG.debug("shutdown end");
	}

	static final boolean isToEraseTable(final String ltmInterface) {
		boolean isErase = false;
		try {
			// TODO tabelas intermedias -set-map
			AgnosticUtil.getClass(ltmInterface);
		} catch (final ClassNotFoundException e) {
			LOG.warn("#0", e.getMessage());
			final String eraseArg = "-LTM.Erase=" + ltmInterface;
			if (VMMonitor.javaArguments().contains(eraseArg)) {
				isErase = true;
			} else {
				LOG.warn("To erase data from #0 set jvm argument: #1", ltmInterface, eraseArg);
			}
		}
		return isErase;
	}

	private final Connection conn;

	DataMemoryConnection() throws SQLException {
		LOG.debug("new connection");
		this.conn = HsqldbUtil.newConnection();
		this.conn.setAutoCommit(false);
		this.conn.setReadOnly(false);
		this.conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
	}

	<T extends LtmObjectModelling> void initialize(final PreparedClass prepClass) throws SQLException {
		HsqldbUtil.initialize(this.conn, prepClass);
	}

	void eraseAll() throws SQLException {
		HsqldbUtil.dropSchema(this.conn);
		LargeMemoryTracker.eraseAll();
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
				throw new ImplementationLtRtException();
			}
			rs.close();
			stt.close();
			this.conn.commit();
		} else {
			this.conn.rollback();
			throw new ImplementationLtRtException();
		}
		return result;
	}

	void fetchRecord(final DataProxyHandler dph, final Map<String, Object> data) throws SQLException {
		LOG.trace("#0: #1", dph.getInterface(), dph.getId());
		final PreparedClass pClass = dph.getPreparedClass();
		final PreparedStatement pStt = this.conn.prepareStatement(pClass.getSelectById());
		pStt.setLong(1, dph.getId());
		final boolean hasRS = pStt.execute();
		if (hasRS) {
			final ResultSet rs = pStt.getResultSet();
			if (rs.next()) {
				DataMemoryUtil.map(rs, pClass, dph, data);
				// TODO tabelas intermedias -set-map
				if (rs.next()) {
					throw new ImplementationLtRtException();
				}
				rs.close();
			} else {
				throw new LtmLtRtException(dph.getInterface() + " do not remember id: " + dph.getId());
			}
		} else {
			throw new ImplementationLtRtException();
		}
		pStt.close();
		this.conn.rollback();
	}

	void updateRecord(final PreparedClass pClass, final long id, final String field, final Object value,
			final Object prevValue) throws SQLException {
		final Class<LtmObjectModelling> ltmClass = pClass.getInterface();
		LOG.trace("#0: #1: #2", ltmClass, id, field);
		final int dataPos = pClass.getColumns().indexOf(field);
		if (dataPos != -1) {
			final PreparedStatement pStt = this.conn.prepareStatement(pClass.getUpdateColumnById().get(dataPos));
			final DataMemoryType type = pClass.getColumnsTypes().get(dataPos);
			if (DataMemoryType.ENUM.equals(type)) {
				HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), value.toString());
			} else if (DataMemoryType.LARGE_MEMORY.equals(type)) {
				if (prevValue != null) {
					LargeMemoryTracker.del(this.conn, ltmClass, id, LargeMemory.class.cast(prevValue));
				}
				if (value == null) {
					HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), null);
				} else {
					HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), LargeMemory.class.cast(value).getId());
					LargeMemoryTracker.add(this.conn, ltmClass, id, LargeMemory.class.cast(value));
				}
			} else if (DataMemoryType.LONG_TERM_MEMORY.equals(type)) {
				if (value == null) {
					HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), null);
				} else {
					HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), LtmObjectModelling.class.cast(value).getId());
				}
			} else {
				HsqldbUtil.setPrepStt(pStt, 1, type.getSqlType(), value);
			}
			pStt.setLong(2, id);
			final int count = pStt.executeUpdate();
			if (count == 0) {
				this.conn.rollback();
				throw new LtmLtRtException(ltmClass + " do not remember id: " + id);
			}
			pStt.close();
			this.conn.commit();
		} else {
			// TODO tabelas intermedias -set-map
		}
	}

	void deleteRecord(final PreparedClass pClass, final long id) throws SQLException {
		final Class<LtmObjectModelling> ltmClass = pClass.getInterface();
		LOG.trace("#0: #1", ltmClass, id);
		final PreparedStatement pStt = this.conn.prepareStatement(pClass.getDeleteById());
		pStt.setLong(1, id);
		final int count = pStt.executeUpdate();
		if (count == 0) {
			this.conn.rollback();
			throw new LtmLtRtException(ltmClass + " do not remember id: " + id);
		}
		pStt.close();
		LargeMemoryTracker.delFromLtmInstance(this.conn, ltmClass, id);
		// TODO tabelas intermedias -set-map
		this.conn.commit();
	}

	void newLargeMemory(final LargeMemory largeMemory) throws SQLException {
		LargeMemoryTracker.init(this.conn, largeMemory);
		this.conn.commit();
		DataMemoryUtil.setLargeMemory(largeMemory);
	}

	int count(final String preparedStatement, final DataMemoryType[] types, final Object[] parameters)
			throws SQLException {
		final int result;
		LOG.trace("preparedStatement: #0", preparedStatement);
		final PreparedStatement pStt = this.conn.prepareStatement(preparedStatement);
		HsqldbUtil.setPrepStt(pStt, types, parameters);
		if (pStt.execute()) {
			final ResultSet rSet = pStt.getResultSet();
			if (rSet.next()) {
				result = rSet.getInt(1);
			} else {
				this.conn.rollback();
				throw new ImplementationLtRtException();
			}
			rSet.close();
		} else {
			this.conn.rollback();
			throw new ImplementationLtRtException();
		}
		pStt.close();
		this.conn.rollback();
		return result;
	}

	boolean hasResult(final String preparedStatement, final DataMemoryType[] types, final Object[] parameters)
			throws SQLException {
		final boolean result;
		LOG.trace("preparedStatement: #0", preparedStatement);
		final PreparedStatement pStt = this.conn.prepareStatement(preparedStatement);
		HsqldbUtil.setPrepStt(pStt, types, parameters);
		if (pStt.execute()) {
			final ResultSet rSet = pStt.getResultSet();
			result = rSet.next();
			rSet.close();
		} else {
			this.conn.rollback();
			throw new ImplementationLtRtException();
		}
		pStt.close();
		this.conn.rollback();
		return result;
	}

	boolean containsID(final String preparedStatement, final DataMemoryType[] types, final Object[] parameters,
			final Long id) throws SQLException {
		final Set<Long> existingIDset = getExistingIDs(preparedStatement, types, parameters, new Long[] { id });
		return existingIDset.contains(id);
	}

	boolean containsIDset(final String preparedStatement, final DataMemoryType[] types, final Object[] parameters,
			final Set<Long> idSet) throws SQLException {
		final Set<Long> existingIDset = getExistingIDs(preparedStatement, types, parameters, idSet.toArray());
		return existingIDset.containsAll(idSet);
	}

	private Set<Long> getExistingIDs(final String preparedStatement, final DataMemoryType[] types,
			final Object[] parameters, final Object[] ids) throws SQLException {
		final Set<Long> result = new HashSet<>();
		LOG.trace("preparedStatement: #0", preparedStatement);
		final PreparedStatement pStt = this.conn.prepareStatement(preparedStatement);
		final Array sqlArray = this.conn.createArrayOf(HsqldbUtil.getHsqlType(Types.BIGINT), ids);
		pStt.setArray(1, sqlArray);
		HsqldbUtil.setPrepStt(pStt, 2, types, parameters);
		if (pStt.execute()) {
			final ResultSet rSet = pStt.getResultSet();
			while (rSet.next()) {
				result.add(rSet.getLong(DataProxyHandler.ID));
			}
			rSet.close();
		} else {
			sqlArray.free();
			this.conn.rollback();
			throw new ImplementationLtRtException();
		}
		sqlArray.free();
		pStt.close();
		this.conn.rollback();
		return result;
	}

	Long getNextId(final String preparedStatement, final DataMemoryType[] types, final Object[] parameters,
			final long id) throws SQLException {
		Long result = null;
		LOG.trace("preparedStatement: #0", preparedStatement);
		final PreparedStatement pStt = this.conn.prepareStatement(preparedStatement);
		pStt.setLong(1, id);
		HsqldbUtil.setPrepStt(pStt, 2, types, parameters);
		if (pStt.execute()) {
			final ResultSet rSet = pStt.getResultSet();
			if (rSet.next()) {
				result = rSet.getLong(DataProxyHandler.ID);
			}
			if (rSet.next()) {
				this.conn.rollback();
				throw new ImplementationLtRtException();
			}
			rSet.close();
		} else {
			this.conn.rollback();
			throw new ImplementationLtRtException();
		}
		pStt.close();
		this.conn.rollback();
		return result;
	}

}

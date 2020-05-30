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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import leitej.exception.ImplementationLtRtException;
import leitej.log.Logger;
import leitej.util.AgnosticUtil;
import leitej.util.machine.VMMonitor;

/**
 * @author Julio Leite
 *
 */
final class DataMemoryConnection {

	private static final Logger LOG = Logger.getInstance();

	private static final Map<Class<?>, PreparedClass> PREP_CLASS_MAP = new HashMap<>();

	static final void hibernate() throws SQLException {
		LOG.debug("shutdown init");
		HsqldbUtil.dbShutdown();
		LOG.debug("shutdown end");
	}

	static final boolean isToEraseTable(final String ltmInterface) {
		boolean isErase = false;
		try {
			// TODO other long term memory
			// TODO special column stream
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

	<T extends LtmObjectModelling> void initialize(final Class<T> ltmClass, final DataProxyHandler dph)
			throws SQLException {
		synchronized (ltmClass) {
			if (!PREP_CLASS_MAP.containsKey(ltmClass)) {
				final PreparedClass prepClass = new PreparedClass(dph);
				PREP_CLASS_MAP.put(ltmClass, prepClass);
				HsqldbUtil.initialize(this.conn, prepClass);
			}
		}
	}

	void eraseAll() throws SQLException {
		HsqldbUtil.dropSchema(this.conn);
		PREP_CLASS_MAP.clear();
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

	<T extends LtmObjectModelling> long newRecord(final Class<T> ltmClass) throws SQLException {
		LOG.trace("#0", ltmClass);
		final PreparedClass pClass = PREP_CLASS_MAP.get(ltmClass);
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
			this.conn.commit();
			rs.close();
			stt.close();
		} else {
			throw new ImplementationLtRtException();
		}
		return result;
	}

	<T extends LtmObjectModelling> void fetchRecord(final Class<T> ltmClass, final long id,
			final Map<String, Object> data) throws SQLException {
		LOG.trace("#0: #1", ltmClass, id);
		final PreparedClass pClass = PREP_CLASS_MAP.get(ltmClass);
		final PreparedStatement pStt = this.conn.prepareStatement(pClass.getSelectById());
		pStt.setLong(1, id);
		final boolean hasRS = pStt.execute();
		if (hasRS) {
			final ResultSet rs = pStt.getResultSet();
			if (rs.next()) {
				DataMemoryUtil.map(rs, pClass, data);
				// TODO other long term memory
				// TODO special column stream
				// TODO tabelas intermedias -set-map
				if (rs.next()) {
					throw new ImplementationLtRtException();
				}
				rs.close();
			} else {
				throw new SQLException(ltmClass + " can not remember id: " + id);
			}
		} else {
			throw new ImplementationLtRtException();
		}
		pStt.close();
		this.conn.rollback();
	}

	<T extends LtmObjectModelling> void updateRecord(final Class<T> ltmClass, final long id, final String field,
			final Object value) throws SQLException {
		LOG.trace("#0: #1 #2", ltmClass, id, field);
		final PreparedClass pClass = PREP_CLASS_MAP.get(ltmClass);
		final int dataPos = pClass.getColumns().indexOf(field);
		if (dataPos != -1) {
			final PreparedStatement pStt = this.conn.prepareStatement(pClass.getUpdateColumnById().get(dataPos));
			final int sqlType = pClass.getColumnsTypes().get(dataPos);
			HsqldbUtil.setPrepStt(pStt, 1, sqlType, value);
			pStt.setLong(2, id);
			final int count = pStt.executeUpdate();
			if (count == 0) {
				this.conn.rollback();
				throw new SQLException(ltmClass + " can not remember id: " + id);
			}
			this.conn.commit();
			pStt.close();
		} else {
			// TODO other long term memory
			// TODO special column stream
			// TODO tabelas intermedias -set-map
		}
	}

	<T extends LtmObjectModelling> void deleteRecord(final Class<T> ltmClass, final long id) throws SQLException {
		LOG.trace("#0: #1", ltmClass, id);
		final PreparedClass pClass = PREP_CLASS_MAP.get(ltmClass);
		final PreparedStatement pStt = this.conn.prepareStatement(pClass.getDeleteById());
		pStt.setLong(1, id);
		final int count = pStt.executeUpdate();
		if (count == 0) {
			this.conn.rollback();
			throw new SQLException(ltmClass + " can not remember id: " + id);
		}
		// TODO other long term memory
		// TODO special column stream
		// TODO tabelas intermedias -set-map
		this.conn.commit();
		pStt.close();
	}

}

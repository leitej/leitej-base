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

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leitej.exception.ImplementationLtRtException;
import leitej.log.Logger;

/**
 * Wraps a <code>java.sql.Connection</code> for easy use.
 *
 * @author Julio Leite
 */
final class ConnectionDB {

	private static final Logger LOG = Logger.getInstance();

	private static enum EXECUTE {
		HAS_TABLE, UPDATE, BATCH, PARAMETERIZED_BATCH, PARAMETERIZED_BATCH_GROUP, SELECT, PARAMETERIZED_SELECT
	}

	private final ConnectionPoolDB myPool;
	private final Connection conn;

	private final List<Statement> statementList = new ArrayList<>();
	private Statement batchStmt = null;
	private PreparedStatement batchPStmt = null;
	private final List<String> batchGroupOrder = new ArrayList<>();
	private final Map<String, PreparedStatement> batchPStmtGroup = new HashMap<>();
	private Statement selectStmt = null;
	private ResultSet selectResult = null;
	private PreparedStatement selectPStmt = null;
	private ResultSet selectPResult = null;

	private boolean invalid = false;

	/**
	 * Wraps the connection <code>conn</code> for easy use.
	 *
	 * @param myPool the pool that this wrapped connection belongs
	 * @param conn   connection to be wrapped
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection
	 */
	ConnectionDB(final ConnectionPoolDB myPool, final Connection conn) throws SQLException {
		this.myPool = myPool;
		this.conn = conn;
		this.conn.setAutoCommit(false);
		this.conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
	}

	void logMetaData() throws SQLException {
		final DatabaseMetaData meta = this.conn.getMetaData();
		LOG.debug("lt.MngDriverName", meta.getDriverName());
	}

	void connError(final SQLException e) {
		if (!this.invalid) {
			this.invalid = this.myPool.getLtmManager().invalidatesConnection(e);
		}
	}

	boolean isInvalid() {
		try {
			return this.invalid || isClosed();
		} catch (final SQLException e) {
			connError(e);
			return this.invalid;
		}
	}

	/**
	 * Commits all changes and releases the connection to the pool.
	 *
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection
	 */
	synchronized void release() throws SQLException {
		try {
			if (!isClosed()) {
				this.conn.commit();
			}
		} catch (final SQLException e) {
			connError(e);
			LOG.debug("#0", e);
			throw e;
		} finally {
			reset();
			boolean offered = false;
			boolean interrupted = false;
			while (!offered) {
				try {
					this.myPool.offer(this);
					offered = true;
				} catch (final InterruptedException e) {
					if (!interrupted) {
						interrupted = true;
					}
					LOG.warn("#0", new ImplementationLtRtException(e));
				}
			}
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Undoes all changes made and releases the connection to the pool.
	 *
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection
	 */
	synchronized void releaseRollback() throws SQLException {
		try {
			if (!isClosed()) {
				this.conn.rollback();
			}
		} catch (final SQLException e) {
			connError(e);
			LOG.debug("#0", e);
			throw e;
		} finally {
			reset();
			boolean offered = false;
			boolean interrupted = false;
			while (!offered) {
				try {
					this.myPool.offer(this);
					offered = true;
				} catch (final InterruptedException e) {
					if (!interrupted) {
						interrupted = true;
					}
					LOG.warn("#0", new ImplementationLtRtException(e));
				}
			}
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void reset() {
		this.batchStmt = null;
		this.batchPStmt = null;
		this.batchPStmtGroup.clear();
		this.batchGroupOrder.clear();
		this.selectStmt = null;
		this.selectResult = null;
		this.selectPStmt = null;
		this.selectPResult = null;
		for (final Statement stmt : this.statementList) {
			try {
				stmt.close();
			} catch (final SQLException e) {
				connError(e);
				LOG.debug("#0", e);
			}
		}
		this.statementList.clear();
	}

	/**
	 * Retrieves whether this Connection object has been closed.
	 *
	 * @return if this Connection object is closed; false if it is still open
	 * @throws SQLException if a database access error occurs
	 */
	boolean isClosed() throws SQLException {
		return this.conn.isClosed();
	}

	/**
	 * Closes the connection.<br/>
	 * Doing a rollback before close.
	 */
	synchronized void close() {
		try {
			if (!this.conn.isClosed()) {
				this.conn.rollback();
				this.conn.close();
			}
		} catch (final SQLException e) {
			connError(e);
			LOG.error("#0", e);
		}
	}

	/**
	 *
	 * @return a new Statement object that will generate ResultSet objects
	 * @throws SQLException if a database access error occurs, this method is called
	 *                      on a closed connection
	 */
	private Statement newStatement() throws SQLException {
		final Statement result = this.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		this.statementList.add(result);
		return result;
	}

	/**
	 *
	 * @param sql a String object that is the SQL statement to be sent to the
	 *            database
	 * @return a new PreparedStatement object containing the pre-compiled SQL
	 *         statement that will produce ResultSet objects
	 * @throws SQLException if a database access error occurs, this method is called
	 *                      on a closed connection
	 */
	private PreparedStatement newPreparedStatement(final String sql) throws SQLException {
		final PreparedStatement result = this.conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		this.statementList.add(result);
		return result;
	}

	/**
	 * Executes changes.
	 *
	 * @param tp  type of execution
	 * @param sql an SQL statement to be sent to the database if necessary
	 * @return object that contains the data produced by the execution
	 * @throws SQLException if a database access error occurs, this method is called
	 *                      on a closed connection, if one of the commands sent to
	 *                      the database fails to execute properly or attempts to
	 *                      return an unexpected result
	 */
	private Object execute(final EXECUTE tp, final String sql) throws SQLException {
		Object result = null;
		Savepoint savepoint = null;
		try {
			savepoint = this.conn.setSavepoint();
			switch (tp) {
			case SELECT:
				result = this.selectStmt.executeQuery(sql);
				break;
			case PARAMETERIZED_SELECT:
				result = this.selectPStmt.executeQuery();
				break;
			case UPDATE:
				result = Integer.valueOf(newStatement().executeUpdate(sql));
				break;
			case HAS_TABLE:
				result = Boolean.valueOf(this.conn.getMetaData().getTables(null, null, sql, null).next());
				break;
			case BATCH:
				if (this.batchStmt != null) {
					result = this.batchStmt.executeBatch();
					this.batchStmt = null;
				}
				break;
			case PARAMETERIZED_BATCH:
				if (this.batchPStmt != null) {
					result = this.batchPStmt.executeBatch();
				}
				break;
			case PARAMETERIZED_BATCH_GROUP:
				final int[][] tmp = new int[this.batchGroupOrder.size()][];
				int i = 0;
				for (final String batch : this.batchGroupOrder) {
					tmp[i++] = this.batchPStmtGroup.get(batch).executeBatch();
				}
				this.batchGroupOrder.clear();
				result = tmp;
				break;
			default:
				throw new ImplementationLtRtException();
			}
		} catch (final SQLException e) {
			if (BatchUpdateException.class.isInstance(e)) {
				LOG.debug("batch result: #0", BatchUpdateException.class.cast(e).getUpdateCounts());
			} else {
				LOG.debug("sql: #0", sql);
			}
			connError(e);
			if (savepoint != null) {
				try {
					this.conn.rollback(savepoint);
				} catch (final SQLException e2) {
					connError(e2);
				}
			}
			throw e;
		}
		return result;
	}

	/**
	 *
	 * @param pstmt
	 * @param sqlTypes the array of the SQL types code defined in java.sql.Types for
	 *                 the <code>args</code>
	 * @param args     objects to set as parameters
	 * @throws SQLException if a database access error occurs or this method is
	 *                      called on a closed PreparedStatement
	 */
	private static void setPreparedStatement(final PreparedStatement pstmt, final int[] sqlTypes, final Object... args)
			throws SQLException {
		setPreparedStatement(pstmt, true, null, null, sqlTypes, args);
	}

	/**
	 *
	 * @param pstmt
	 * @param limitBefore  defines if <code>firstRow</code> and
	 *                     <code>NumberOfRows</code> should be inserted before the
	 *                     <code>args</code> parameters
	 * @param firstRow     first row to be retrieved
	 * @param NumberOfRows number of rows to retrieve
	 * @param sqlTypes     the array of the SQL types code defined in java.sql.Types
	 *                     for the <code>args</code>
	 * @param args         objects to set as parameters
	 * @throws SQLException if a database access error occurs or this method is
	 *                      called on a closed PreparedStatement
	 */
	private static void setPreparedStatement(final PreparedStatement pstmt, final boolean limitBefore,
			final Integer firstRow, final Integer NumberOfRows, final int[] sqlTypes, final Object... args)
			throws SQLException {
		int i = 1;
		if (firstRow != null && NumberOfRows != null && limitBefore) {
			pstmt.setInt(i++, firstRow);
			pstmt.setInt(i++, NumberOfRows);
		}
		if (sqlTypes != null && args != null) {
			for (int j = 0; j < sqlTypes.length && j < args.length; j++) {
				switch (sqlTypes[j]) {
				case Types.TINYINT:
					if (args[j] == null) {
						pstmt.setNull(i++, sqlTypes[j]);
					} else {
						pstmt.setByte(i++, Byte.class.cast(args[j]));
					}
					break;
				case Types.SMALLINT:
					if (args[j] == null) {
						pstmt.setNull(i++, sqlTypes[j]);
					} else {
						pstmt.setShort(i++, Short.class.cast(args[j]));
					}
					break;
				case Types.INTEGER:
					if (args[j] == null) {
						pstmt.setNull(i++, sqlTypes[j]);
					} else {
						pstmt.setInt(i++, Integer.class.cast(args[j]));
					}
					break;
				case Types.BIGINT:
					if (args[j] == null) {
						pstmt.setNull(i++, sqlTypes[j]);
					} else {
						pstmt.setLong(i++, Long.class.cast(args[j]));
					}
					break;
				case Types.DECIMAL:
					if (args[j] == null) {
						pstmt.setNull(i++, sqlTypes[j]);
					} else {
						pstmt.setBigDecimal(i++, BigDecimal.class.cast(args[j]));
					}
					break;
				case Types.DOUBLE:
					if (args[j] == null) {
						pstmt.setNull(i++, sqlTypes[j]);
					} else {
						pstmt.setDouble(i++, Double.class.cast(args[j]));
					}
					break;
				case Types.BOOLEAN:
					if (args[j] == null) {
						pstmt.setNull(i++, sqlTypes[j]);
					} else {
						pstmt.setBoolean(i++, Boolean.class.cast(args[j]));
					}
					break;
				case Types.VARCHAR:
					if (args[j] == null) {
						pstmt.setNull(i++, sqlTypes[j]);
					} else {
						pstmt.setString(i++, String.class.cast(args[j]));
					}
					break;
				case Types.ARRAY:
					if (args[j] == null) {
						pstmt.setNull(i++, sqlTypes[j]);
					} else {
						pstmt.setArray(i++, Array.class.cast(args[j]));
					}
					break;
				default:
					throw new ImplementationLtRtException();
				}
			}
		}
		if (firstRow != null && NumberOfRows != null && !limitBefore) {
			pstmt.setInt(i++, firstRow);
			pstmt.setInt(i++, NumberOfRows);
		}
	}

	/**
	 * Retrieves whether <code>tablename</code> exists.
	 *
	 * @param tablename
	 * @return
	 * @throws SQLException if a database access error occurs, this method is called
	 *                      on a closed connection
	 */
	synchronized boolean hasTable(final String tablename) throws SQLException {
		return Boolean.class.cast(execute(EXECUTE.HAS_TABLE, tablename.toUpperCase())).booleanValue();
	}

	synchronized int executeUpdate(final String sql) throws SQLException {
		return Integer.class.cast(execute(EXECUTE.UPDATE, sql)).intValue();
	}

	/**
	 *
	 * @param sql typically this is a SQL INSERT or UPDATE statement
	 * @throws SQLException if a database access error occurs, this method is called
	 *                      on a closed connection
	 */
	synchronized void addBatch(final String sql) throws SQLException {
		LOG.trace("#0 -> '#1'", this, sql);
		if (this.batchStmt == null) {
			this.batchStmt = newStatement();
		}
		this.batchStmt.addBatch(sql);
	}

	/**
	 *
	 * @return an array of update counts containing one element for each command in
	 *         the batch. The elements of the array are ordered according to the
	 *         order in which commands were added to the batch
	 * @throws SQLException if a database access error occurs, this method is called
	 *                      on a closed connection, if one of the commands sent to
	 *                      the database fails to execute properly or attempts to
	 *                      return an unexpected result
	 */
	synchronized int[] executeBatch() throws SQLException {
		return (int[]) execute(EXECUTE.BATCH, null);
	}

	/**
	 *
	 * @param sql a String object that is the SQL statement to be sent to the
	 *            database
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection
	 */
	synchronized void initParameterizedBatch(final String sql) throws SQLException {
		LOG.trace("#0 -> '#1'", this, sql);
		this.batchPStmt = newPreparedStatement(sql);
	}

	/**
	 *
	 * @param sqlTypes the array of the SQL types code defined in java.sql.Types for
	 *                 the <code>args</code>
	 * @param args     objects to set as parameters
	 * @throws SQLException if a database access error occurs or this method is
	 *                      called on a closed PreparedStatement
	 */
	synchronized void addParamBatch(final int[] sqlTypes, final Object... args) throws SQLException {
		setPreparedStatement(this.batchPStmt, sqlTypes, args);
		this.batchPStmt.addBatch();
	}

	/**
	 *
	 * @return an array of update counts containing one element for each command in
	 *         the batch. The elements of the array are ordered according to the
	 *         order in which commands were added to the batch
	 * @throws SQLException if a database access error occurs, this method is called
	 *                      on a closed connection, if one of the commands sent to
	 *                      the database fails to execute properly or attempts to
	 *                      return an unexpected result
	 */
	synchronized int[] executeParameterizedBatch() throws SQLException {
		return (int[]) execute(EXECUTE.PARAMETERIZED_BATCH, null);
	}

	/**
	 *
	 * @param sql      a String object that is the SQL statement to be sent to the
	 *                 database
	 * @param sqlTypes the array of the SQL types code defined in java.sql.Types for
	 *                 the <code>args</code>
	 * @param args     objects to set as parameters
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection
	 */
	synchronized void addParameterizedBatchGroup(final String sql, final int[] sqlTypes, final Object... args)
			throws SQLException {
		LOG.trace("#0 -> '#1'", this, sql);
		PreparedStatement tmp = this.batchPStmtGroup.get(sql);
		if (tmp == null) {
			tmp = newPreparedStatement(sql);
			this.batchPStmtGroup.put(sql, tmp);
		}
		if (!this.batchGroupOrder.contains(sql)) {
			this.batchGroupOrder.add(sql);
		}
		setPreparedStatement(tmp, sqlTypes, args);
		tmp.addBatch();
	}

	/**
	 *
	 * @return a two dimensional array; first dimension array of distinct sql's,
	 *         ordered by first occurrence added to the batch; second dimension
	 *         array of update counts containing one element for each command in the
	 *         batch. The elements of the array are ordered according to the order
	 *         in which commands were added to the batch
	 * @throws SQLException if a database access error occurs, this method is called
	 *                      on a closed connection, if one of the commands sent to
	 *                      the database fails to execute properly or attempts to
	 *                      return an unexpected result
	 */
	synchronized int[][] executeParameterizedBatchGroup() throws SQLException {
		return (int[][]) execute(EXECUTE.PARAMETERIZED_BATCH_GROUP, null);
	}

	/**
	 *
	 * @param sql an SQL statement to be sent to the database, typically a static
	 *            SQL SELECT statement
	 * @return a ResultSet object that contains the data produced by the given
	 *         query; never null
	 * @throws SQLException if a database access error occurs, this method is called
	 *                      on a closed connection or the given SQL produces
	 *                      anything other than a single ResultSet object
	 */
	synchronized ResultSet executeSelect(final String sql) throws SQLException {
		LOG.trace("#0 -> '#1'", this, sql);
		if (this.selectStmt == null) {
			this.selectStmt = newStatement();
		}
		if (this.selectResult != null) {
			this.selectResult.close();
		}
		this.selectResult = (ResultSet) execute(EXECUTE.SELECT, sql);
		return this.selectResult;
	}

	/**
	 *
	 * @param sql a String object that is the SQL statement to be sent to the
	 *            database
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection
	 */
	synchronized void initParameterizedSelect(final String sql) throws SQLException {
		LOG.trace("#0 -> '#1'", this, sql);
		this.selectPStmt = newPreparedStatement(sql);
	}

	/**
	 *
	 * @param sqlTypes the array of the SQL types code defined in java.sql.Types for
	 *                 the <code>args</code>
	 * @param args     objects to set as parameters
	 * @return a ResultSet object that contains the data produced by the query;
	 *         never null
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection or the SQL statement does
	 *                      not return a ResultSet object
	 */
	synchronized ResultSet executeParameterizedSelect(final int[] sqlTypes, final Object... args) throws SQLException {
		return executeParameterizedSelect(true, null, null, sqlTypes, args);
	}

	/**
	 *
	 * @param limitBefore  defines if <code>firstRow</code> and
	 *                     <code>NumberOfRows</code> should be inserted before the
	 *                     <code>args</code> parameters
	 * @param firstRow     first row to be retrieved
	 * @param NumberOfRows number of rows to retrieve
	 * @param sqlTypes     the array of the SQL types code defined in java.sql.Types
	 *                     for the <code>args</code>
	 * @param args         objects to set as parameters
	 * @return a ResultSet object that contains the data produced by the query;
	 *         never null
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection or the SQL statement does
	 *                      not return a ResultSet object
	 */
	synchronized ResultSet executeParameterizedSelect(final boolean limitBefore, final Integer firstRow,
			final Integer NumberOfRows, final int[] sqlTypes, final Object... args) throws SQLException {
		if (this.selectPResult != null) {
			this.selectPResult.close();
		}
		setPreparedStatement(this.selectPStmt, limitBefore, firstRow, NumberOfRows, sqlTypes, args);
		this.selectPResult = (ResultSet) execute(EXECUTE.PARAMETERIZED_SELECT, null);
		return this.selectPResult;
	}

}

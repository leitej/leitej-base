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
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import leitej.Constant;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.SeppukuLtRtException;
import leitej.log.Logger;

/**
 * @author Julio Leite
 *
 */
final class LargeMemoryTracker {

	private static final Logger LOG = Logger.getInstance();

	private static final String SCHEMA_QT = "\"" + HsqldbUtil.SCHEMA + "\"";
	private static final String TABLE_NAME = SCHEMA_QT + ".\"__large_memory_tracker__\"";
	private static final String COLUMN_LTM_NAME = "ltm_name";
	private static final String COLUMN_LTM_ID = "ltm_id";
	private static final String COLUMN_LM_ID = "lm_id";
	private static final String COLUMN_LM_REL = "lm_rel";
	private static final String COLUMN_LTM_NAME_QT = "\"" + COLUMN_LTM_NAME + "\"";
	private static final String COLUMN_LTM_ID_QT = "\"" + COLUMN_LTM_ID + "\"";
	private static final String COLUMN_LM_ID_QT = "\"" + COLUMN_LM_ID + "\"";
	private static final String COLUMN_LM_REL_QT = "\"" + COLUMN_LM_REL + "\"";
	private static final String SET_COLUMN_LTM_ID_QT = "\"" + DataProxyHandler.LTM_ID + "\"";
	private static final String SET_COLUMN_VALUE_QT = "\"" + DataProxyHandler.SET_VALUE + "\"";

	private static final String CREATE_TABLE = "create table if not exists " + TABLE_NAME + " (                       "
			+ COLUMN_LTM_NAME_QT + " " + HsqldbUtil.getHsqlType(Types.VARCHAR) + ",                                   "
			+ COLUMN_LTM_ID_QT + " " + HsqldbUtil.getHsqlType(Types.BIGINT) + ",                                      "
			+ COLUMN_LM_ID_QT + " " + HsqldbUtil.getHsqlType(Types.BIGINT) + ",                                       "
			+ COLUMN_LM_REL_QT + " " + HsqldbUtil.getHsqlType(Types.BIGINT) + "                                      )";

	private static final String CREATE_INDEX_1 = "create index if not exists \"ind1\" on " + TABLE_NAME + " (         "
			+ COLUMN_LM_ID_QT + ")";

	private static final String CREATE_INDEX_2 = "create index if not exists \"ind2\" on " + TABLE_NAME + " (         "
			+ COLUMN_LTM_NAME_QT + ", " + COLUMN_LTM_ID_QT + ")";

	private static final String CREATE_INDEX_3 = "create unique index if not exists \"ind3\" on " + TABLE_NAME + " (  "
			+ COLUMN_LTM_NAME_QT + ", " + COLUMN_LTM_ID_QT + ", " + COLUMN_LM_ID_QT + ")";

	private static final String CREATE_INDEX_4 = "create index if not exists \"ind4\" on " + TABLE_NAME + " (         "
			+ COLUMN_LTM_NAME_QT + ")";

	private static final String CREATE_INDEX_5 = "create unique index if not exists \"ind3\" on " + TABLE_NAME + " (  "
			+ COLUMN_LTM_NAME_QT + ", " + COLUMN_LM_ID_QT + ")";

	private static final String SELECT_REL = "select " + COLUMN_LM_REL_QT + " from " + TABLE_NAME + " where "
			+ COLUMN_LTM_NAME_QT + " = ? and " + COLUMN_LTM_ID_QT + " = ? and " + COLUMN_LM_ID_QT + " = ?";

	private static final String INSERT_NEW = "insert into " + TABLE_NAME + " values (null, 0, ?, 0)";

	private static final String INSERT_REL = "insert into " + TABLE_NAME + " values (?, ?, ?, 1)";

	private static final String UPDATE_INC = "update " + TABLE_NAME + " set " + COLUMN_LM_REL_QT + " = "
			+ COLUMN_LM_REL_QT + " + 1 where " + COLUMN_LTM_NAME_QT + " = ? and " + COLUMN_LTM_ID_QT + " = ? and "
			+ COLUMN_LM_ID_QT + " = ?";

	private static final String UPDATE_DEC = "update " + TABLE_NAME + " set " + COLUMN_LM_REL_QT + " = "
			+ COLUMN_LM_REL_QT + " - 1 where " + COLUMN_LTM_NAME_QT + " = ? and " + COLUMN_LTM_ID_QT + " = ? and "
			+ COLUMN_LM_ID_QT + " = ?";

	private static final String UPDATE_ZERO_LTM_NAME = "update " + TABLE_NAME + " set " + COLUMN_LM_REL_QT
			+ " = 0 where " + COLUMN_LTM_NAME_QT + " = ?";

	private static final String UPDATE_ZERO_LTM_ID = "update " + TABLE_NAME + " set " + COLUMN_LM_REL_QT + " = 0 where "
			+ COLUMN_LTM_NAME_QT + " = ? and " + COLUMN_LTM_ID_QT + " = ?";

	private static final String SELECT_REL_DEL = "select " + COLUMN_LM_ID_QT + " from " + TABLE_NAME + " group by "
			+ COLUMN_LM_ID_QT + " having sum(" + COLUMN_LM_REL_QT + ") < 1";

	private static final String DELETE_REL = "delete from " + TABLE_NAME + " where " + COLUMN_LM_ID_QT + " = ?";

	private static final Map<String, String> LTM_SET_CLEAR = new HashMap<>();

	static void intialize() {
		LOG.trace("mkdirs: #0", Constant.LTM_STREAM_DIR);
		Constant.LTM_STREAM_DIR.mkdirs();
		Connection conn = null;
		try {
			try {
				conn = HsqldbUtil.newConnection();
				createTable(conn);
				deleteNoRel(conn);
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		} catch (final SQLException | IOException e) {
			throw new SeppukuLtRtException(e);
		}
	}

	private static void createTable(final Connection conn) throws SQLException {
		Statement stt = conn.createStatement();
		LOG.trace("CREATE_TABLE: #0", CREATE_TABLE);
		stt.execute(CREATE_TABLE);
		stt.close();
		stt = conn.createStatement();
		LOG.trace("CREATE_INDEX_1: #0", CREATE_INDEX_1);
		stt.execute(CREATE_INDEX_1);
		stt.close();
		stt = conn.createStatement();
		LOG.trace("CREATE_INDEX_2: #0", CREATE_INDEX_2);
		stt.execute(CREATE_INDEX_2);
		stt.close();
		stt = conn.createStatement();
		LOG.trace("CREATE_INDEX_3: #0", CREATE_INDEX_3);
		stt.execute(CREATE_INDEX_3);
		stt.close();
		stt = conn.createStatement();
		LOG.trace("CREATE_INDEX_4: #0", CREATE_INDEX_4);
		stt.execute(CREATE_INDEX_4);
		stt.close();
		stt = conn.createStatement();
		LOG.trace("CREATE_INDEX_5: #0", CREATE_INDEX_5);
		stt.execute(CREATE_INDEX_5);
		stt.close();
		conn.commit();
	}

	private static void deleteNoRel(final Connection conn) throws SQLException, IOException {
		final Statement stt = conn.createStatement();
		stt.setFetchSize(5000);
		stt.setFetchDirection(ResultSet.FETCH_FORWARD);
		LOG.trace("SELECT_REL_DEL: #0", SELECT_REL_DEL);
		if (stt.execute(SELECT_REL_DEL)) {
			final ResultSet rs = stt.getResultSet();
			long lmId;
			PreparedStatement delPStt;
			boolean ioDel;
			while (rs.next()) {
				lmId = rs.getLong(COLUMN_LM_ID);
				LOG.debug("deleting large memory id: #0", lmId);
				ioDel = (new LargeMemory(lmId)).delete();
				if (!ioDel) {
					LOG.warn("FAIL delete large memory: #0: #1", Constant.LTM_STREAM_DIR, lmId);
				} else {
					delPStt = conn.prepareStatement(DELETE_REL);
					delPStt.setLong(1, lmId);
					delPStt.executeUpdate();
					delPStt.close();
				}
			}
			rs.close();
			conn.commit();
		} else {
			throw new ImplementationLtRtException();
		}
		stt.close();
	}

	static void init(final Connection conn, final LargeMemory largeMemory) throws SQLException {
		final PreparedStatement pStt = conn.prepareStatement(INSERT_NEW);
		pStt.setLong(1, largeMemory.getId());
		pStt.executeUpdate();
		pStt.close();
		// do not commit
	}

	static <T extends LtmObjectModelling> void add(final Connection conn, final Class<T> ltmClass, final long ltmId,
			final LargeMemory largeMemory) throws SQLException {
		// check if already has line
		final PreparedStatement checkPStt = conn.prepareStatement(SELECT_REL);
		checkPStt.setString(1, ltmClass.getName());
		checkPStt.setLong(2, ltmId);
		checkPStt.setLong(3, largeMemory.getId());
		if (checkPStt.execute()) {
			final ResultSet rs = checkPStt.getResultSet();
			final String incSql;
			if (rs.next()) {
				// update increment
				incSql = UPDATE_INC;
			} else {
				// insert one
				incSql = INSERT_REL;
			}
			rs.close();
			final PreparedStatement incPStt = conn.prepareStatement(incSql);
			incPStt.setString(1, ltmClass.getName());
			incPStt.setLong(2, ltmId);
			incPStt.setLong(3, largeMemory.getId());
			final int r = incPStt.executeUpdate();
			if (r != 1) {
				throw new IllegalStateLtRtException("#0: #1 lm_id: #2", ltmClass.getName(), ltmId, largeMemory.getId());
			}
			incPStt.close();
			// do not commit
		} else {
			throw new ImplementationLtRtException();
		}
		checkPStt.close();
		// do not commit
	}

	static <T extends LtmObjectModelling> void del(final Connection conn, final Class<T> ltmClass, final long ltmId,
			final LargeMemory largeMemory) throws SQLException {
		final PreparedStatement decPStt = conn.prepareStatement(UPDATE_DEC);
		decPStt.setString(1, ltmClass.getName());
		decPStt.setLong(2, ltmId);
		decPStt.setLong(3, largeMemory.getId());
		final int r = decPStt.executeUpdate();
		if (r != 1) {
			throw new IllegalStateLtRtException("#0: #1 lm_id: #2", ltmClass.getName(), ltmId, largeMemory.getId());
		}
		decPStt.close();
		// do not commit
	}

	static void delFromLtmClassName(final Connection conn, final String ltmClassName) throws SQLException {
		final PreparedStatement pStt = conn.prepareStatement(UPDATE_ZERO_LTM_NAME);
		pStt.setString(1, ltmClassName);
		pStt.executeUpdate();
		pStt.close();
		// do not commit
	}

	static <T extends LtmObjectModelling> void delFromLtmInstance(final Connection conn, final Class<T> ltmClass,
			final long ltmId) throws SQLException {
		final PreparedStatement pStt = conn.prepareStatement(UPDATE_ZERO_LTM_ID);
		pStt.setString(1, ltmClass.getName());
		pStt.setLong(2, ltmId);
		pStt.executeUpdate();
		pStt.close();
		// do not commit
	}

	static void delFromLtmSetById(final Connection conn, final String tablenameSet, final Class<?> ltmClass,
			final long ltmId) throws SQLException {
		String query;
		synchronized (LTM_SET_CLEAR) {
			query = LTM_SET_CLEAR.get(tablenameSet);
			if (query == null) {
				query = "update " + TABLE_NAME + " set " + COLUMN_LM_REL_QT + " = " + COLUMN_LM_REL_QT + " - 1 where "
						+ COLUMN_LTM_NAME_QT + " = ? and " + COLUMN_LTM_ID_QT + " = ? and " + COLUMN_LM_ID_QT
						+ " in (select " + SET_COLUMN_VALUE_QT + " from " + SCHEMA_QT + ".\"" + tablenameSet
						+ "\" where " + SET_COLUMN_LTM_ID_QT + " = ?)";
				LTM_SET_CLEAR.put(tablenameSet, query);
			}
		}
		LOG.trace("query: #0", query);
		final PreparedStatement pStt = conn.prepareStatement(query);
		pStt.setString(1, ltmClass.getName());
		pStt.setLong(2, ltmId);
		pStt.setLong(3, ltmId);
		pStt.executeUpdate();
		pStt.close();
		// do not commit
	}

	static void delFromLtmColumnSetDrop(final Connection conn, final String tablenameSet, final String ltmClassName)
			throws SQLException {
		final String query = "update " + TABLE_NAME + " set " + COLUMN_LM_REL_QT + " = " + COLUMN_LM_REL_QT
				+ " - 1 where " + COLUMN_LTM_NAME_QT + " = ? and (" + COLUMN_LTM_ID_QT + "," + COLUMN_LM_ID_QT
				+ ") in (select " + SET_COLUMN_LTM_ID_QT + "," + SET_COLUMN_VALUE_QT + " from " + SCHEMA_QT + ".\""
				+ tablenameSet + "\")";
		LOG.trace("query: #0", query);
		final PreparedStatement pStt = conn.prepareStatement(query);
		pStt.setString(1, ltmClassName);
		pStt.executeUpdate();
		pStt.close();
		// do not commit
	}

	static <T extends LtmObjectModelling> void delFromLtmColumnDrop(final Connection conn, final Class<T> ltmClass,
			final String ltmTablename, final String columnName) throws SQLException {
		final String query = "update " + TABLE_NAME + " set " + COLUMN_LM_REL_QT + " = " + COLUMN_LM_REL_QT
				+ " - 1 where " + COLUMN_LTM_NAME_QT + " = ? and (" + COLUMN_LTM_ID_QT + "," + COLUMN_LM_ID_QT
				+ ") in (select " + SET_COLUMN_LTM_ID_QT + ",\"" + columnName + "\" from " + SCHEMA_QT + ".\""
				+ ltmTablename + "\" where \"" + columnName + "\" is not null)";
		LOG.trace("query: #0", query);
		final PreparedStatement pStt = conn.prepareStatement(query);
		pStt.setString(1, ltmClass.getName());
		pStt.executeUpdate();
		pStt.close();
		// do not commit
	}

}

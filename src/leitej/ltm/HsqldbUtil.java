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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import leitej.Constant;
import leitej.exception.SeppukuLtRtException;
import leitej.exception.UnsupportedDataTypeLtRtException;
import leitej.log.Logger;
import leitej.util.HexaUtil;

/**
 * @author Julio Leite
 *
 */
final class HsqldbUtil {

	private static final Logger LOG = Logger.getInstance();

	private static final char TABLE_LTM_PREFIX = 'L';
	private static final char TABLE_SET_PREFIX = 'S';

	static final String SCHEMA = "ltm";
	private static final MessageDigest CREATE_TABLENAME;

	private static final Map<String, String> TABLE_COMMENT_MAP = new HashMap<>();
	private static final Map<String, List<String>> TABLE_COLUMN_MAP = new HashMap<>();

	static {
		try {
			System.setProperty("hsqldb.reconfig_logging", "false");
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (final ClassNotFoundException e) {
			throw new SeppukuLtRtException(e);
		}
		try {
			CREATE_TABLENAME = MessageDigest.getInstance("SHA-384");
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	static <I extends LtmObjectModelling> String getTablename(final Class<I> ltmClass) {
		synchronized (CREATE_TABLENAME) {
			return TABLE_LTM_PREFIX
					+ HexaUtil.toHex(CREATE_TABLENAME.digest(ltmClass.getName().getBytes(Constant.UTF8_CHARSET)));
		}
	}

	static <I extends LtmObjectModelling> String getTablenameSet(final Class<I> ltmClass, final String setDataname) {
		synchronized (CREATE_TABLENAME) {
			return TABLE_SET_PREFIX + HexaUtil
					.toHex(CREATE_TABLENAME.digest((setDataname + ltmClass.getName()).getBytes(Constant.UTF8_CHARSET)));
		}
	}

	static final Connection newConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:hsqldb:file:" + Constant.LTM_DBNAME_DIR, "SA", "");
	}

	static final void createSchema(final Connection conn) throws SQLException {
		final Statement stt = conn.createStatement();
		final String createSchema = "create schema \"" + SCHEMA + "\"";
		LOG.debug("createSchema: #0", createSchema);
		stt.execute(createSchema);
		stt.close();
		conn.commit();
	}

	static {
		Connection conn = null;
		try {
			try {
				conn = newConnection();
				// get schemas
				LOG.debug("schema: #0", SCHEMA);
				final ResultSet rsSchema = conn.getMetaData().getSchemas();
				String schema;
				boolean createSchema = true;
				while (rsSchema.next()) {
					schema = rsSchema.getString("TABLE_SCHEM");
					if (SCHEMA.equals(schema)) {
						createSchema = false;
					}
				}
				rsSchema.close();
				LOG.debug("createSchema: #0", createSchema);
				if (createSchema) {
					createSchema(conn);
				}
				// get tables
				final ResultSet rsTable = conn.getMetaData().getTables(null, SCHEMA, "%", new String[] { "TABLE" });
				String tablename;
				String remark;
				final List<String> dropTableList = new ArrayList<>();
				final List<String> dropTableRemarkList = new ArrayList<>();
				final List<String> dropLtmClassList = new ArrayList<>();
				while (rsTable.next()) {
					tablename = rsTable.getString("TABLE_NAME");
					remark = rsTable.getString("REMARKS");
					LOG.debug("schema: #0, remarks: #1, table: #2", SCHEMA, remark, tablename);
					if (DataMemoryUtil.isToEraseTable(remark)) {
						dropTableList.add(tablename);
						dropTableRemarkList.add(remark);
						if (tablename.charAt(0) == TABLE_LTM_PREFIX) {
							dropLtmClassList.add(DataMemoryUtil.getLtmClassName(remark));
						}
					} else {
						TABLE_COMMENT_MAP.put(tablename, remark);
						TABLE_COLUMN_MAP.put(tablename, new ArrayList<String>());
					}
				}
				rsTable.close();
				// get columns
				ResultSet rsColumn;
				List<String> columnList;
				String columnName;
				for (final Entry<String, List<String>> entry : TABLE_COLUMN_MAP.entrySet()) {
					LOG.debug("tablename: #0", entry.getKey());
					rsColumn = conn.getMetaData().getColumns(null, SCHEMA, entry.getKey(), "%");
					columnList = entry.getValue();
					while (rsColumn.next()) {
						columnName = rsColumn.getString("COLUMN_NAME");
						columnList.add(columnName);
					}
					rsColumn.close();
					LOG.debug("columnList: #0", columnList);
				}
				// drop tables
				if (!dropTableList.isEmpty()) {
					String dropSql;
					Statement stt;
					for (int i = 0; i < dropTableList.size(); i++) {
						// update large memory tracker
						if (dropTableList.get(i).charAt(0) == TABLE_SET_PREFIX) {
							if (DataMemoryType.LARGE_MEMORY
									.equals(DataMemoryUtil.getColumnSetDataMemoryType(dropTableRemarkList.get(i)))
									&& !dropLtmClassList
											.contains(DataMemoryUtil.getLtmClassName(dropTableRemarkList.get(i)))) {
								LargeMemoryTracker.delFromLtmColumnSetDrop(conn, dropTableList.get(i),
										DataMemoryUtil.getLtmClassName(dropTableRemarkList.get(i)));
							}
						} else {
							LargeMemoryTracker.delFromLtmClassName(conn,
									DataMemoryUtil.getLtmClassName(dropTableRemarkList.get(i)));
						}
						// drop the table
						dropSql = "drop table " + dropTableList.get(i);
						LOG.warn("erasing table: #0, remark: #1", dropTableList.get(i), dropTableRemarkList.get(i));
						stt = conn.createStatement();
						stt.execute(dropSql);
						stt.close();
					}
					conn.commit();
				}
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		} catch (final SQLException e) {
			throw new SeppukuLtRtException(e);
		}
	}

	/*
	 * WARN: shutdown is for all databases
	 */
	static final void dbShutdown() throws SQLException, IOException {
		if (CompactMemory.isToCompact()) {
			LOG.warn("Iniciating Compact Memory");
			newConnection().createStatement().executeUpdate("SHUTDOWN COMPACT");
			CompactMemory.compactDone();
		} else {
			newConnection().createStatement().executeUpdate("SHUTDOWN");
		}
	}

	private static void createTable(final Connection conn, final PreparedClass prepClass) throws SQLException {
		final StringBuilder query = new StringBuilder();
		// create table - ROW_ID, ...
		query.append("create table \"");
		query.append(SCHEMA);
		query.append("\".\"");
		query.append(prepClass.getTablename());
		query.append("\" (\"");
		query.append(DataProxyHandler.LTM_ID);
		query.append("\" bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY");
		for (int i = 0; i < prepClass.getColumnNameList().size(); i++) {
			query.append(", \"");
			query.append(prepClass.getColumnNameList().get(i));
			query.append("\" ");
			query.append(getHsqlType(prepClass.getColumnTypeList().get(i).getSqlType()));
		}
		query.append(")");
		// comment on table - class.name
		query.append("; comment on table \"");
		query.append(SCHEMA);
		query.append("\".\"");
		query.append(prepClass.getTablename());
		query.append("\" is '");
		final String remarks = DataMemoryUtil.genRemark(prepClass.getInterface());
		query.append(remarks);
		query.append("'");
		final String createTable = query.toString();
		LOG.debug("createTable: #0", createTable);
		final Statement stt = conn.createStatement();
		stt.execute(createTable);
		stt.close();
		// TODO create indexes
		query.setLength(0);
		//
		conn.commit();
		TABLE_COMMENT_MAP.put(prepClass.getTablename(), remarks);
		TABLE_COLUMN_MAP.put(prepClass.getTablename(), new ArrayList<>(prepClass.getColumnNameList()));
	}

	static void createTableSet(final Connection conn, final PreparedClass prepClass, final String datanameSet)
			throws SQLException {
		final StringBuilder query = new StringBuilder();
		final String setTablename = prepClass.getSetTablename(datanameSet);
		final DataMemoryType setDataMemoryType = prepClass.getColumnsSetType(datanameSet);
		final Class<?> setDataClass = prepClass.getColumnsSetClass(datanameSet);
		final String setSqlType = getHsqlType(setDataMemoryType.getSqlType());
		// create table - ROW_ID, LTM_ID, VALUE
		query.append("create table \"");
		query.append(SCHEMA);
		query.append("\".\"");
		query.append(setTablename);
		query.append("\" (\"");
		query.append(DataProxyHandler.SET_ID);
		query.append("\" bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY");
		query.append(", \"");
		query.append(DataProxyHandler.LTM_ID);
		query.append("\" ");
		query.append(getHsqlType(DataMemoryType.LONG_TERM_MEMORY.getSqlType()));
		query.append(", \"");
		query.append(DataProxyHandler.SET_VALUE);
		query.append("\" ");
		query.append(setSqlType);
		query.append(")");
		// comment on table - class.name;data_name_set;data_name_set_type
		query.append("; comment on table \"");
		query.append(SCHEMA);
		query.append("\".\"");
		query.append(setTablename);
		query.append("\" is '");
		final String remarks = DataMemoryUtil.genRemark(prepClass.getInterface(), datanameSet, setDataMemoryType,
				setDataClass);
		query.append(remarks);
		query.append("'");
		// execute it
		final String createSetTable = query.toString();
		LOG.debug("createSetTable: #0", createSetTable);
		final Statement stt = conn.createStatement();
		stt.execute(createSetTable);
		stt.close();
		// create indexes
		query.setLength(0);
		// create index - LTM_ID
		query.append("create index if not exists \"ind_");
		query.append(setTablename);
		query.append("_ltmid\" on \"");
		query.append(SCHEMA);
		query.append("\".\"");
		query.append(setTablename);
		query.append("\" (\"");
		query.append(DataProxyHandler.LTM_ID);
		query.append("\")");
		// create index - LTM_ID - SET_VALUE
		query.append("; create index if not exists \"ind_");
		query.append(setTablename);
		query.append("_ltmid_setvalue\" on \"");
		query.append(SCHEMA);
		query.append("\".\"");
		query.append(setTablename);
		query.append("\" (\"");
		query.append(DataProxyHandler.LTM_ID);
		query.append("\", \"");
		query.append(DataProxyHandler.SET_VALUE);
		query.append("\")");
		// create index - SET_ID - LTM_ID
		query.append("; create index if not exists \"ind_");
		query.append(setTablename);
		query.append("_setid_ltmid\" on \"");
		query.append(SCHEMA);
		query.append("\".\"");
		query.append(setTablename);
		query.append("\" (\"");
		query.append(DataProxyHandler.SET_ID);
		query.append("\", \"");
		query.append(DataProxyHandler.LTM_ID);
		query.append("\")");
		// execute it
		final String createSetIndex = query.toString();
		LOG.debug("createSetIndex: #0", createSetIndex);
		final Statement stti = conn.createStatement();
		stti.execute(createSetIndex);
		stti.close();
		// persist
		conn.commit();
		TABLE_COMMENT_MAP.put(setTablename, remarks);
		TABLE_COLUMN_MAP.put(setTablename, Arrays
				.asList(new String[] { DataProxyHandler.SET_ID, DataProxyHandler.LTM_ID, DataProxyHandler.SET_VALUE }));
	}

	static DataMemoryType getColumnSetDataMemoryType(final String setTablename) {
		return DataMemoryUtil.getColumnSetDataMemoryType(TABLE_COMMENT_MAP.get(setTablename));
	}

	static Class<?> getColumnSetParameterClass(final String setTablename) throws ClassNotFoundException {
		return DataMemoryUtil.getColumnSetParameterClass(TABLE_COMMENT_MAP.get(setTablename));
	}

	private static void dropColumns(final Connection conn, final String tablename, final List<String> toRemove)
			throws SQLException {
		if (!toRemove.isEmpty()) {
			final StringBuilder query = new StringBuilder();
			for (final String column : toRemove) {
				query.append("alter table \"");
				query.append(SCHEMA);
				query.append("\".\"");
				query.append(tablename);
				query.append("\"");
				query.append(" drop \"");
				query.append(column);
				query.append("\"; ");
			}
			final String dropColumns = query.toString();
			LOG.debug("dropColumns: #0", dropColumns);
			final Statement stt = conn.createStatement();
			stt.execute(dropColumns);
			stt.close();
			conn.commit();
		}
	}

	private static void addColumns(final Connection conn, final PreparedClass prepClass, final List<String> toAdd)
			throws SQLException {
		if (!toAdd.isEmpty()) {
			final StringBuilder query = new StringBuilder();
			for (final String column : toAdd) {
				query.append("alter table \"");
				query.append(SCHEMA);
				query.append("\".\"");
				query.append(prepClass.getTablename());
				query.append("\"");
				query.append(" add \"");
				query.append(column);
				query.append("\" ");
				query.append(getHsqlType(
						prepClass.getColumnTypeList().get(prepClass.getColumnNameList().indexOf(column)).getSqlType()));
				query.append("; ");
			}
			final String addColumns = query.toString();
			LOG.debug("addColumns: #0", addColumns);
			final Statement stt = conn.createStatement();
			stt.execute(addColumns);
			stt.close();
			conn.commit();
		}
	}

	private static <T extends LtmObjectModelling> void delLargeMemory(final Connection conn, final Class<T> ltmClass,
			final String ltmTablename, final List<String> toRemoveColumnNameList) throws SQLException {
		String columnName;
		for (int i = 0; i < toRemoveColumnNameList.size(); i++) {
			columnName = toRemoveColumnNameList.get(i);
			if (DataMemoryUtil.isColumnTypeLargeMemory(columnName)) {
				LargeMemoryTracker.delFromLtmColumnDrop(conn, ltmClass, ltmTablename, columnName);
			}
		}
	}

	static void initialize(final Connection conn, final PreparedClass prepClass) throws SQLException {
		LOG.trace("table: #0", prepClass.getTablename());
		final List<String> columnList = TABLE_COLUMN_MAP.get(prepClass.getTablename());
		// check if table already exists
		if (columnList == null) {
			createTable(conn, prepClass);
		} else {
			// TODO validate index remove or add - prepClass get index
			if (DataMemoryPool.CONFIG.isAutoForgetsInterfaceComponentMisses()) {
				final List<String> toRemove = new ArrayList<>();
				toRemove.addAll(columnList);
				toRemove.removeAll(prepClass.getColumnNameList());
				toRemove.remove(DataProxyHandler.LTM_ID);
				delLargeMemory(conn, prepClass.getInterface(), prepClass.getTablename(), toRemove);
				dropColumns(conn, prepClass.getTablename(), toRemove);
				columnList.removeAll(toRemove);
			}
			final List<String> toAdd = new ArrayList<>();
			toAdd.addAll(prepClass.getColumnNameList());
			toAdd.removeAll(columnList);
			addColumns(conn, prepClass, toAdd);
			columnList.addAll(toAdd);
		}
	}

	static void dropSchema(final Connection conn) throws SQLException {
		final Statement stt = conn.createStatement();
		final String dropSchema = "drop schema if exists \"" + SCHEMA + "\" cascade";
		LOG.debug("dropSchema: #0", dropSchema);
		stt.execute(dropSchema);
		stt.close();
		conn.commit();
		TABLE_COLUMN_MAP.clear();
		TABLE_COMMENT_MAP.clear();
		createSchema(conn);
	}

	static String getHsqlType(final int sqlType) {
		final String result;
		switch (sqlType) {
		case Types.TINYINT:
			result = "tinyint default null";
			break;
		case Types.SMALLINT:
			result = "smallint default null";
			break;
		case Types.INTEGER:
			result = "int default null";
			break;
		case Types.BIGINT:
			result = "bigint default null";
			break;
		case Types.DECIMAL:
			result = "decimal default null";
			break;
		case Types.DOUBLE:
			result = "float default null";
			break;
		case Types.FLOAT:
			result = "float default null";
			break;
		case Types.BOOLEAN:
			result = "bit default null";
			break;
		case Types.VARCHAR:
			result = "varchar (256) default null";
			break;
		case Types.LONGVARCHAR:
			result = "longvarchar default null";
			break;
		case Types.LONGVARBINARY:
			result = "longvarbinary default null";
			break;
		default:
			throw new UnsupportedDataTypeLtRtException("sqlType: #0", sqlType);
		}
		return result;
	}

	static Object parseValue(final ResultSet rSet, final String column, final int sqlType) throws SQLException {
		final Object result;
		if (rSet.getObject(column) == null || rSet.wasNull()) {
			result = null;
		} else {
			switch (sqlType) {
			case Types.TINYINT:
				result = rSet.getByte(column);
				break;
			case Types.SMALLINT:
				result = rSet.getShort(column);
				break;
			case Types.INTEGER:
				result = rSet.getInt(column);
				break;
			case Types.BIGINT:
				result = rSet.getLong(column);
				break;
			case Types.DECIMAL:
				result = rSet.getBigDecimal(column);
				break;
			case Types.DOUBLE:
				result = rSet.getDouble(column);
				break;
			case Types.FLOAT:
				result = rSet.getFloat(column);
				break;
			case Types.BOOLEAN:
				result = rSet.getBoolean(column);
				break;
			case Types.VARCHAR:
				result = rSet.getString(column);
				break;
			case Types.LONGVARCHAR:
				result = rSet.getString(column);
				break;
			case Types.LONGVARBINARY:
				result = rSet.getBytes(column);
				break;
			default:
				throw new UnsupportedDataTypeLtRtException("sqlType: #0", sqlType);
			}
		}
		return result;
	}

	static void setPrepStt(final PreparedStatement pStt, final DataMemoryType[] types, final Object[] parameters)
			throws SQLException {
		setPrepStt(pStt, 1, types, parameters);
	}

	static void setPrepStt(final PreparedStatement pStt, final int offset, final DataMemoryType[] types,
			final Object[] parameters) throws SQLException {
		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				setPrepStt(pStt, offset + i, types[i].getSqlType(), parameters[i]);
			}
		}
	}

	static void setPrepStt(final PreparedStatement pStt, final int pos, final int sqlType, final Object value)
			throws SQLException {
		switch (sqlType) {
		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
		case Types.BIGINT:
		case Types.DECIMAL:
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.BOOLEAN:
		case Types.VARCHAR:
			pStt.setObject(pos, value, sqlType);
			break;
		case Types.LONGVARCHAR:
			pStt.setString(pos, String.class.cast(value));
			break;
		case Types.LONGVARBINARY:
			pStt.setBytes(pos, (byte[]) value);
			break;
		default:
			throw new UnsupportedDataTypeLtRtException("sqlType: #0", sqlType);
		}
	}

	static String getStatementSelectById(final String tablename) {
		return "select * from \"" + SCHEMA + "\".\"" + tablename + "\" where \"" + DataProxyHandler.LTM_ID + "\" = ?";
	}

	static String getStatementInsertNewRow(final String tablename) {
		final StringBuilder result = new StringBuilder();
		result.append("insert into \"");
		result.append(SCHEMA);
		result.append("\".\"");
		result.append(tablename);
		result.append("\" default values; call identity()");
		return result.toString();
	}

	static String getStatementUpdateColumnById(final String tablename, final String columnname) {
		final StringBuilder result = new StringBuilder();
		result.append("update \"");
		result.append(SCHEMA);
		result.append("\".\"");
		result.append(tablename);
		result.append("\" set \"");
		result.append(columnname);
		result.append("\" = ? where \"");
		result.append(DataProxyHandler.LTM_ID);
		result.append("\" = ?");
		return result.toString();
	}

	static String getStatementDeleteById(final String tablename) {
		return "delete from \"" + SCHEMA + "\".\"" + tablename + "\" where \"" + DataProxyHandler.LTM_ID + "\" = ?";
	}

	static String getStatementSetClearByLtmId(final String tablenameSet) {
		return "delete from \"" + SCHEMA + "\".\"" + tablenameSet + "\" where \"" + DataProxyHandler.LTM_ID + "\" = ?";
	}

	static String getStatementSetCount(final String tablename, final long ltmId) {
		return "select count(*) from \"" + SCHEMA + "\".\"" + tablename + "\" where \"" + DataProxyHandler.LTM_ID
				+ "\" = " + String.valueOf(ltmId);
	}

	static String getStatementSetHasResult(final String tablename, final long ltmId) {
		return "select \"" + DataProxyHandler.LTM_ID + "\" from \"" + SCHEMA + "\".\"" + tablename + "\" where \""
				+ DataProxyHandler.LTM_ID + "\" = " + String.valueOf(ltmId) + " limit 1";
	}

	static String getStatementSetContains(final String tablename, final long ltmId) {
		return "select \"" + DataProxyHandler.SET_VALUE + "\" from \"" + SCHEMA + "\".\"" + tablename + "\" where \""
				+ DataProxyHandler.LTM_ID + "\" = " + String.valueOf(ltmId) + " and \"" + DataProxyHandler.SET_VALUE
				+ "\" = ? limit 1";
	}

	static String getStatementSetAdd(final String tablename, final String tablenameLtm, final long ltmId) {
		return "insert into \"" + SCHEMA + "\".\"" + tablename + "\" (\"" + DataProxyHandler.LTM_ID + "\", \""
				+ DataProxyHandler.SET_VALUE + "\") select a.\"" + DataProxyHandler.LTM_ID + "\", ? from \"" + SCHEMA
				+ "\".\"" + tablenameLtm + "\" a where a.\"" + DataProxyHandler.LTM_ID + "\" = " + String.valueOf(ltmId)
				+ " and not exists (select 1 from \"" + SCHEMA + "\".\"" + tablename + "\" b where b.\""
				+ DataProxyHandler.LTM_ID + "\" = " + String.valueOf(ltmId) + " and b.\"" + DataProxyHandler.SET_VALUE
				+ "\" = ? )";
	}

	static String getStatementSetRemove(final String tablename, final long ltmId) {
		return "delete from \"" + SCHEMA + "\".\"" + tablename + "\" where \"" + DataProxyHandler.LTM_ID + "\" = "
				+ String.valueOf(ltmId) + " and \"" + DataProxyHandler.SET_VALUE + "\" = ? ";
	}

	public static String getStatementSetIterator(final String tablename, final long ltmId) {
		return "select \"" + DataProxyHandler.SET_ID + "\", \"" + DataProxyHandler.SET_VALUE + "\" from \"" + SCHEMA
				+ "\".\"" + tablename + "\" where \"" + DataProxyHandler.SET_ID + "\" > ? and \""
				+ DataProxyHandler.LTM_ID + "\" = " + String.valueOf(ltmId) + " order by \"" + DataProxyHandler.SET_ID
				+ "\" asc limit 1";
	}

	static String getStatementScaledIterator(final String tablename, final String filter, final boolean desc) {
		return "select \"" + DataProxyHandler.LTM_ID + "\" from \"" + SCHEMA + "\".\"" + tablename + "\" where \""
				+ DataProxyHandler.LTM_ID + "\" " + ((desc) ? "<" : ">") + " ? and " + filter + " order by \""
				+ DataProxyHandler.LTM_ID + "\" " + ((desc) ? "desc" : "asc") + " limit 1";
	}

}

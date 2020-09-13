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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import leitej.Constant;
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
	private static final char TABLE_MAP_PREFIX = 'M';

	static final String SCHEMA = "ltm";
	private static final MessageDigest CREATE_TABLENAME;

	private static final Map<String, String> TABLE_COMMENT_MAP = new HashMap<>();
	private static final Map<String, List<String>> TABLE_COLUMN_MAP = new HashMap<>();

	static {
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (final ClassNotFoundException e) {
			LOG.fatal("#0", e);
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

	static <I extends LtmObjectModelling> String getTablenameMap(final Class<I> ltmClass, final String mapDataname) {
		synchronized (CREATE_TABLENAME) {
			return TABLE_MAP_PREFIX + HexaUtil
					.toHex(CREATE_TABLENAME.digest((mapDataname + ltmClass.getName()).getBytes(Constant.UTF8_CHARSET)));
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
				String remarks;
				final List<String> dropTable = new ArrayList<>();
				while (rsTable.next()) {
					tablename = rsTable.getString("TABLE_NAME");
					remarks = rsTable.getString("REMARKS");
					LOG.debug("schema: #0, remarks: #1, table: #2", SCHEMA, remarks, tablename);
					if (DataMemoryConnection.isToEraseTable(remarks)) {
						dropTable.add(tablename);
					} else {
						TABLE_COMMENT_MAP.put(tablename, remarks);
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
				if (!dropTable.isEmpty()) {
					String dropSql;
					Statement stt;
					for (final String droptable : dropTable) {
						dropSql = "drop table " + droptable;
						LOG.warn("erasing table: #0", droptable);
						stt = conn.createStatement();
						stt.execute(dropSql);
						stt.close();
						// TODO remove big binary files
						// BigBinaryTracer.delFromLtmClassName(conn, ltmClassName);
					}
					conn.commit();
				}
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		} catch (final SQLException e) {
			LOG.fatal("#0", e);
		}
	}

	/*
	 * WARN: shutdown is for all databases
	 */
	static final void dbShutdown() throws SQLException {
		// TODO newConnection().createStatement().executeUpdate("SHUTDOWN COMPACT");
		newConnection().createStatement().executeUpdate("SHUTDOWN");
	}

	private static void createTable(final Connection conn, final PreparedClass prepClass) throws SQLException {
		final StringBuilder query = new StringBuilder();
		// create table
		query.append("create table \"");
		query.append(SCHEMA);
		query.append("\".\"");
		query.append(prepClass.getTablename());
		query.append("\" (\"");
		query.append(DataProxyHandler.ID);
		query.append("\" bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY");
		for (int i = 0; i < prepClass.getColumns().size(); i++) {
			query.append(", \"");
			query.append(prepClass.getColumns().get(i));
			query.append("\" ");
			query.append(getHsqlType(prepClass.getColumnsTypes().get(i).getSqlType()));
		}
		query.append(")");
		// comment on table
		query.append("; comment on table \"");
		query.append(SCHEMA);
		query.append("\".\"");
		query.append(prepClass.getTablename());
		query.append("\" is '");
		query.append(prepClass.getInterface().getName());
		query.append("'");
		final String createTable = query.toString();
		LOG.debug("createTable: #0", createTable);
		final Statement stt = conn.createStatement();
		stt.execute(createTable);
		stt.close();
		conn.commit();
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
						prepClass.getColumnsTypes().get(prepClass.getColumns().indexOf(column)).getSqlType()));
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

	static void initialize(final Connection conn, final PreparedClass prepClass) throws SQLException {
		LOG.trace("table: #0", prepClass.getTablename());
		final List<String> columnList = TABLE_COLUMN_MAP.get(prepClass.getTablename());
		// check if table already exists
		if (columnList == null) {
			createTable(conn, prepClass);
			// TODO tabelas intermedias -set-map
		} else {
			if (DataMemoryPool.CONFIG.isAutoForgetsInterfaceComponentMisses()) {
				final List<String> toRemove = new ArrayList<>();
				toRemove.addAll(columnList);
				toRemove.removeAll(prepClass.getColumns());
				toRemove.remove(DataProxyHandler.ID);
				dropColumns(conn, prepClass.getTablename(), toRemove);
				columnList.removeAll(toRemove);
				// TODO tabelas intermedias -set-map
			}
			final List<String> toAdd = new ArrayList<>();
			toAdd.addAll(prepClass.getColumns());
			toAdd.removeAll(columnList);
			addColumns(conn, prepClass, toAdd);
			columnList.addAll(toAdd);
			// TODO tabelas intermedias -set-map
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
		return "select * from \"" + SCHEMA + "\".\"" + tablename + "\" where \"" + DataProxyHandler.ID + "\" = ?";
	}

	static String getStatementInsertNewRow(final String tablename) {
		final StringBuilder result = new StringBuilder();
		result.append("insert into \"");
		result.append(SCHEMA);
		result.append("\".\"");
		result.append(tablename);
		result.append("\" default values; call identity()");
		// TODO tabelas intermedias -set-map
		return result.toString();
	}

	static String getStatementUpdateColumnById(final String tablename, final String dataname) {
		final StringBuilder result = new StringBuilder();
		result.append("update \"");
		result.append(SCHEMA);
		result.append("\".\"");
		result.append(tablename);
		result.append("\" set \"");
		result.append(dataname);
		result.append("\" = ? where \"");
		result.append(DataProxyHandler.ID);
		result.append("\" = ?");
		// TODO tabelas intermedias -set-map
		return result.toString();
	}

	static String getStatementDeleteById(final String tablename) {
		return "delete from \"" + SCHEMA + "\".\"" + tablename + "\" where \"" + DataProxyHandler.ID + "\" = ?";
		// TODO tabelas intermedias -set-map
	}

	static String getStatementCount(final String tablename, final String filter) {
		return "select count(1) from \"" + SCHEMA + "\".\"" + tablename + "\" where " + filter;
	}

	static String getStatementHasResult(final String tablename, final String filter) {
		return "select \"" + DataProxyHandler.ID + "\" from \"" + SCHEMA + "\".\"" + tablename + "\" where " + filter
				+ " limit 1";
	}

	static String getStatementContains(final String tablename, final String filter) {
		return "select \"" + DataProxyHandler.ID + "\" from \"" + SCHEMA + "\".\"" + tablename + "\" where \""
				+ DataProxyHandler.ID + "\" in ? and " + filter;
	}

	static String getStatementScaledIterator(final String tablename, final String filter) {
		return "select \"" + DataProxyHandler.ID + "\" from \"" + SCHEMA + "\".\"" + tablename + "\" where \""
				+ DataProxyHandler.ID + "\" > ? and " + filter + " order by \"" + DataProxyHandler.ID
				+ "\" asc limit 1";
	}

}

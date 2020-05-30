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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import leitej.Constant;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.UnsupportedDataTypeLtRtException;
import leitej.log.Logger;
import leitej.util.HexaUtil;

/**
 * @author Julio Leite
 *
 */
final class HsqldbUtil {

	private static final Logger LOG = Logger.getInstance();

	private static final String SCHEMA = "ltm";
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
			return "O" + HexaUtil.toHex(CREATE_TABLENAME.digest(ltmClass.getName().getBytes(Constant.UTF8_CHARSET)));
		}
	}

	static <I extends LtmObjectModelling> String getTablenameSet(final Class<I> ltmClass, final String setDataname) {
		synchronized (CREATE_TABLENAME) {
			return "S" + HexaUtil.toHex(
					CREATE_TABLENAME.digest((setDataname + ltmClass.getName()).getBytes(Constant.UTF8_CHARSET)));
		}
	}

	static <I extends LtmObjectModelling> String getTablenameMap(final Class<I> ltmClass, final String mapDataname) {
		synchronized (CREATE_TABLENAME) {
			return "M" + HexaUtil.toHex(
					CREATE_TABLENAME.digest((mapDataname + ltmClass.getName()).getBytes(Constant.UTF8_CHARSET)));
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
		conn.commit();
		stt.close();
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
					for (final String droptable : dropTable) {
						dropSql = "drop table " + droptable;
						LOG.warn("erasing table: #0", droptable);
						conn.createStatement().execute(dropSql);
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
			query.append(getHsqlType(prepClass.getColumnsTypes().get(i)));
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
		conn.commit();
		stt.close();
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
			conn.commit();
			stt.close();
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
				query.append(getHsqlType(prepClass.getColumnsTypes().get(prepClass.getColumns().indexOf(column))));
				query.append("; ");
			}
			final String addColumns = query.toString();
			LOG.debug("addColumns: #0", addColumns);
			final Statement stt = conn.createStatement();
			stt.execute(addColumns);
			conn.commit();
			stt.close();
		}
	}

	static void initialize(final Connection conn, final PreparedClass prepClass) throws SQLException {
		LOG.trace("table: #0", prepClass.getTablename());
		List<String> columnList = TABLE_COLUMN_MAP.get(prepClass.getTablename());
		// check if table already exist
		if (columnList == null) {
			columnList = new ArrayList<>();
			TABLE_COLUMN_MAP.put(prepClass.getTablename(), columnList);
			createTable(conn, prepClass);
			// TODO other long term memory
			// TODO special column stream
			// TODO tabelas intermedias -set-map
		} else {
			if (DataMemoryPool.CONFIG.isAutoForgetsInterfaceComponentMisses()) {
				final List<String> toRemove = new ArrayList<>();
				toRemove.addAll(columnList);
				toRemove.removeAll(prepClass.getColumns());
				toRemove.remove(DataProxyHandler.ID);
				dropColumns(conn, prepClass.getTablename(), toRemove);
				columnList.removeAll(toRemove);
				// TODO other long term memory
				// TODO special column stream
				// TODO tabelas intermedias -set-map
			}
			final List<String> toAdd = new ArrayList<>();
			toAdd.addAll(prepClass.getColumns());
			toAdd.removeAll(columnList);
			addColumns(conn, prepClass, toAdd);
			columnList.addAll(toAdd);
			// TODO other long term memory
			// TODO special column stream
			// TODO tabelas intermedias -set-map
		}
	}

	static void dropSchema(final Connection conn) throws SQLException {
		final Statement stt = conn.createStatement();
		final String dropSchema = "drop schema if exists \"" + SCHEMA + "\" cascade";
		LOG.debug("dropSchema: #0", dropSchema);
		stt.execute(dropSchema);
		conn.commit();
		stt.close();
		TABLE_COLUMN_MAP.clear();
		createSchema(conn);
	}

	private static String getHsqlType(final Integer type) {
		final String result;
		switch (type) {
		case Types.TINYINT:
			result = "tinyint";
			break;
		case Types.SMALLINT:
			result = "smallint";
			break;
		case Types.INTEGER:
			result = "int";
			break;
		case Types.BIGINT:
			result = "bigint";
			break;
		case Types.DECIMAL:
			result = "decimal";
			break;
		case Types.DOUBLE:
			result = "float";
			break;
		case Types.FLOAT:
			result = "float";
			break;
		case Types.BOOLEAN:
			result = "bit";
			break;
		case Types.VARCHAR:
			result = "varchar (256)";
			break;
		case Types.CLOB:
			result = "longvarchar";
			break;
		case Types.BLOB:
			result = "longvarbinary";
			break;
		default:
			throw new UnsupportedDataTypeLtRtException(type.toString());
		}
		return result;
	}

	static Integer getSqlType(final Class<?> type) {
		final Integer result;
		if (Byte.class.isAssignableFrom(type) || byte.class.isAssignableFrom(type)) {
			result = Types.TINYINT;
		} else if (Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type)) {
			result = Types.SMALLINT;
		} else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
			result = Types.INTEGER;
		} else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
			result = Types.BIGINT;
		} else if (BigDecimal.class.isAssignableFrom(type)) {
			result = Types.DECIMAL;
		} else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
			result = Types.DOUBLE;
		} else if (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)) {
			result = Types.FLOAT;
		} else if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
			result = Types.BOOLEAN;
		} else if (type.isEnum()) {
			result = Types.VARCHAR;
		} else if (String.class.isAssignableFrom(type)) {
			result = Types.CLOB;
		} else if (type.isArray() && (byte.class.isAssignableFrom(type.getComponentType()))) {
			result = Types.BLOB;
		} else {
			result = null;
		}
		return result;
	}

	static void setPrepStt(final PreparedStatement pStt, final int pos, final int sqlType, final Object value)
			throws SQLException {
		try {
			LOG.trace("sqlType: #0", sqlType);
			switch (sqlType) {
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			case Types.BIGINT:
			case Types.DECIMAL:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.BOOLEAN:
				pStt.setObject(pos, value, sqlType);
				break;
			case Types.VARCHAR:
				pStt.setString(pos, value.toString());
				break;
			case Types.CLOB:
				pStt.setString(pos, String.class.cast(value));
				break;
			case Types.BLOB:
				pStt.setBytes(pos, (byte[]) value);
				break;
			default:
				throw new UnsupportedDataTypeLtRtException("sqlType: #0", sqlType);
			}
		} catch (final SQLSyntaxErrorException e) {
			throw new ImplementationLtRtException("sqlType: #0, #1", sqlType, e);
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
		// TODO other long term memory
		// TODO special column stream
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
		// TODO other long term memory
		// TODO special column stream
		// TODO tabelas intermedias -set-map
		return result.toString();
	}

	static String getStatementDeleteById(final String tablename) {
		return "delete from \"" + SCHEMA + "\".\"" + tablename + "\" where \"" + DataProxyHandler.ID + "\" = ?";
	}

}

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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.hsqldb.jdbc.JDBCArrayBasic;
import org.hsqldb.types.Type;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.log.Logger;
import leitej.util.DateUtil;
import leitej.util.MathUtil;

/**
 *
 *
 * @author Julio Leite
 */
abstract class AbstractHsqldb extends AbstractLongTermMemory {

	private static final Logger LOG = Logger.getInstance();

	private static final int DEFAULT_MAX_CONNECTIONS = 12;

	protected AbstractHsqldb() {
		super(DEFAULT_MAX_CONNECTIONS);
	}

	protected AbstractHsqldb(final int maxConnections) throws SQLException {
		super(maxConnections);
	}

	@Override
	protected Connection getDriverConnection() throws SQLException {
		LOG.debug("Calling DriverManager.getDriverConnection " + ConstantLtm.DEFAULT_DBNAME);
		return DriverManager.getConnection("jdbc:hsqldb:file:" + ConstantLtm.DEFAULT_DBNAME, "SA", "");
//		return DriverManager.getConnection("jdbc:hsqldb:hsql://127.0.0.1/xdb", "SA", "");					//FIXME: this line for debug only
	}

	@Override
	protected boolean invalidatesConnection(final SQLException e) {
		// TODO: do not close if the state of exception is known
		// and can continuing the connection
		// search for the exception states and defines each ones invalidates the
		// connection

//		    if (sqlState == null) {
//		        System.out.println("The SQL state is not defined!");
//		        return true;
//		    }
//		    // X0Y32: Jar file already exists in schema
//		    if (sqlState.equalsIgnoreCase("X0Y32")) return false;
//		    // 42Y55: Table already exists in schema
//		    if (sqlState.equalsIgnoreCase("42Y55")) return false;
//		    return true;

		return true;
	}

	// TODO: incorporar no close, atencao k shutdown sao para todas as bds
	protected synchronized void dbShutdown() throws SQLException, IllegalAccessException {
		try {
			super.close();
		} finally {
			getDriverConnection().createStatement().executeUpdate("SHUTDOWN");
		}
	}

	protected synchronized void dbShutdownCompact() throws SQLException, IllegalAccessException {
		try {
			super.close();
		} finally {
			getDriverConnection().createStatement().executeUpdate("SHUTDOWN COMPACT");
		}
	}

	// TINYINT, SMALLINT, INTEGER, BIGINT, NUMERIC and DECIMAL (without a decimal
	// point) ... For NUMERIC and DECIMAL, decimal precision is used
	// byte, short, int, long, BigDecimal and BigDecimal

	// For example, DECIMAL(10,2) means maximum total number of digits is 10 and
	// there are always 2 digits after the decimal point, while DECIMAL(10) means 10
	// digits without a decimal point
	// The default precision of NUMERIC and DECIMAL (when not defined) is 100.

	// REAL, FLOAT, DOUBLE
	// double

	// BOOLEAN
	// boolean

	// CHAR, VARCHAR, CLOB (CHARACTER LARGE OBJECT)
	// String

	@Override
	String byteAlias(final String columnDefinition) throws IllegalArgumentLtRtException {
		return "TINYINT";
	}

	@Override
	String shortAlias(final String columnDefinition) throws IllegalArgumentLtRtException {
		return "SMALLINT";
	}

	@Override
	String integerAlias(final String columnDefinition) throws IllegalArgumentLtRtException {
		return "INTEGER";
	}

	@Override
	String longAlias(final String columnDefinition) throws IllegalArgumentLtRtException {
		return "BIGINT";
	}

	@Override
	String bigDecimalAlias(final String columnDefinition, final int precision, final int scale)
			throws IllegalArgumentLtRtException {
		return "DECIMAL";
	}

	@Override
	String doubleAlias(final String columnDefinition, final int precision, final int scale)
			throws IllegalArgumentLtRtException {
		return "DOUBLE";
	}

	@Override
	String booleanAlias(final String columnDefinition) throws IllegalArgumentLtRtException {
		return "BOOLEAN";
	}

	@Override
	String stringAlias(final String columnDefinition, final int length) throws IllegalArgumentLtRtException {
		if (length < 1) {
			throw new IllegalArgumentLtRtException();
		}
		return "VARCHAR(" + length + ")";
	}

	private String addArrayAlias(final String columnDefinition, final int maxArrayLength)
			throws IllegalArgumentLtRtException {
		return columnDefinition + " ARRAY" + ((maxArrayLength >= 0) ? " [" + maxArrayLength + "]" : "");
	}

	@Override
	String byteArrayAlias(final String columnDefinition, final int maxArrayLength) throws IllegalArgumentLtRtException {
		return addArrayAlias(byteAlias(columnDefinition), maxArrayLength);
	}

	@Override
	String shortArrayAlias(final String columnDefinition, final int maxArrayLength)
			throws IllegalArgumentLtRtException {
		return addArrayAlias(shortAlias(columnDefinition), maxArrayLength);
	}

	@Override
	String integerArrayAlias(final String columnDefinition, final int maxArrayLength)
			throws IllegalArgumentLtRtException {
		return addArrayAlias(integerAlias(columnDefinition), maxArrayLength);
	}

	@Override
	String longArrayAlias(final String columnDefinition, final int maxArrayLength) throws IllegalArgumentLtRtException {
		return addArrayAlias(longAlias(columnDefinition), maxArrayLength);
	}

	@Override
	String booleanArrayAlias(final String columnDefinition, final int maxArrayLength)
			throws IllegalArgumentLtRtException {
		return addArrayAlias(booleanAlias(columnDefinition), maxArrayLength);
	}

	@Override
	Array createArraySQL(final ElementColumn ec, final Object[] array) throws IllegalArgumentLtRtException {
		Array result = null;
		if (array != null) {
			Class<?> tmp;
			if (ec.isArray()) {
				tmp = ec.getReturnType().getComponentType();
			} else {
				tmp = ec.getElementTable().getColumnId().getReturnType();
			}
			Type type;
			if (Byte.class.isAssignableFrom(tmp)) {
				type = Type.TINYINT;
			} else if (Short.class.isAssignableFrom(tmp)) {
				type = Type.SQL_SMALLINT;
			} else if (Integer.class.isAssignableFrom(tmp)) {
				type = Type.SQL_INTEGER;
			} else if (Long.class.isAssignableFrom(tmp)) {
				type = Type.SQL_BIGINT;
			} else if (Boolean.class.isAssignableFrom(tmp)) {
				type = Type.SQL_BOOLEAN;
			} else {
				throw new ImplementationLtRtException();
			}
			result = new JDBCArrayBasic(array, type);
		}
		return result;
	}

	@Override
	int byteTypes() {
		return Types.TINYINT;
	}

	@Override
	int shortTypes() {
		return Types.SMALLINT;
	}

	@Override
	int integerTypes() {
		return Types.INTEGER;
	}

	@Override
	int longTypes() {
		return Types.BIGINT;
	}

	@Override
	int bigDecimalTypes() {
		return Types.DECIMAL;
	}

	@Override
	int doubleTypes() {
		return Types.DOUBLE;
	}

	@Override
	int booleanTypes() {
		return Types.BOOLEAN;
	}

	@Override
	int stringTypes() {
		return Types.VARCHAR;
	}

	@Override
	int arrayTypes() {
		return Types.ARRAY;
	}

	@Override
	String generateDropTables() {
		return "DROP SCHEMA PUBLIC CASCADE";
	}

	@Override
	String[] generateCreateTableSQL(final ElementTable elementTable) {
		final List<String> result = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		sb.append("CREATE ");
		if (elementTable.isSmallSize()) {
			sb.append("MEMORY ");
		} else {
			sb.append("CACHED ");
		}
		sb.append("TABLE ");
		sb.append(elementTable.getSqlName());
		sb.append(" ( ");
		int i = 0;
		for (final ElementColumn elemColumn : elementTable.getColumns()) {
			if (!elemColumn.isMapped()) {
				if (i != 0) {
					sb.append(", ");
				}
				sb.append(elemColumn.getSqlName());
				sb.append(" ");
				sb.append(elemColumn.getColumnDefinition());
				if (!elemColumn.isNullable()) {
					sb.append(" NOT NULL");
				}
				if (elemColumn.isId()) {
					sb.append(" PRIMARY KEY");
				} else if (elemColumn.isUnique()) {
					sb.append(" UNIQUE");
				}
				i++;
			}
		}
		sb.append(")");
		result.add(sb.toString());
		// TODO: indexes
		return result.toArray(new String[result.size()]);
	}

	@Override
	String generateParameterizedSelectIdSQL(final ElementTable elementTable) {
		final StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		final ElementColumn[] elemColumns = elementTable.getSelectableColumns();
		for (int i = 0; i < elemColumns.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(elemColumns[i].getSqlName());
			sb.append(" ");
		}
		sb.append("FROM ");
		sb.append(elementTable.getSqlName());
		sb.append(" WHERE ");
		sb.append(elementTable.getColumnId().getSqlName());
		sb.append(" = ? ");
		return sb.toString();
	}

	@Override
	String generateParameterizedSelectCountMappedIdSQL(final ElementTable elementTable, final String mappedColumn) {
		final StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(*) FROM ");
		sb.append(elementTable.getSqlName());
		sb.append(" WHERE ");
		sb.append(elementTable.getColumn(mappedColumn).getSqlName());
		sb.append(" = ? AND ");
		sb.append(elementTable.getColumnId().getSqlName());
		sb.append(" NOT IN (UNNEST(?)) ");
		return sb.toString();
	}

	@Override
	long castCountResult(final Object countValue) {
		if (countValue == null) {
			return -1;
		}
		if (Integer.class.isInstance(countValue)) {
			return Integer.class.cast(countValue).longValue();
		}
		return (Long) countValue;
	}

	@Override
	String generateParameterizedSelectLimitedMappedIdSQL(final ElementTable elementTable, final String mappedColumn) {
		final StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		final ElementColumn[] elemColumns = elementTable.getSelectableColumns();
		for (int i = 0; i < elemColumns.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(elemColumns[i].getSqlName());
			sb.append(" ");
		}
		sb.append("FROM ");
		sb.append(elementTable.getSqlName());
		sb.append(" WHERE ");
		sb.append(elementTable.getColumn(mappedColumn).getSqlName());
		sb.append(" = ? AND ");
		sb.append(elementTable.getColumnId().getSqlName());
		sb.append(" NOT IN (UNNEST(?)) ORDER BY ");
		sb.append(elementTable.getColumnId().getSqlName());
		sb.append(" OFFSET ? FETCH ? ROWS ONLY ");
		return sb.toString();
	}

	@Override
	boolean isParameterizedLimitArgsBeforeColumns() {
		return false;
	}

	@Override
	int absoluteNumberOfFirstRow() {
		return 0;
	}

	@Override
	String generateParameterizedSelectCountHasIdAndMappedIdSQL(final ElementTable elementTable,
			final String mappedColumn) {
		final StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(*) FROM ");
		sb.append(elementTable.getSqlName());
		sb.append(" WHERE ");
		sb.append(elementTable.getColumnId().getSqlName());
		sb.append(" = ? AND ");
		sb.append(elementTable.getColumn(mappedColumn).getSqlName());
		sb.append(" = ? ");
		return sb.toString();
	}

	@Override
	String generateParameterizedInsertSQL(final ElementTable elementTable) {
		final StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(elementTable.getSqlName());
		sb.append(" (");
		final ElementColumn[] elemColumns = elementTable.getInsertableColumns();
		for (int i = 0; i < elemColumns.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(elemColumns[i].getSqlName());
		}
		sb.append(") VALUES (");
		for (int i = 0; i < elemColumns.length; i++) {
			if (i == 0) {
				sb.append("?");
			} else {
				sb.append(", ?");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	String generateParameterizedUpdateIdSQL(final ElementTable elementTable) {
		final StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(elementTable.getSqlName());
		sb.append(" SET ");
		final ElementColumn[] elemColumns = elementTable.getUpdatableColumns();
		for (int i = 0; i < elemColumns.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(elemColumns[i].getSqlName());
			sb.append("=?");
		}
		sb.append(" WHERE ");
		sb.append(elementTable.getColumnId().getSqlName());
		sb.append(" = ? ");
		return sb.toString();
	}

	@Override
	String generateParameterizedUpdateMappedIdListSQL(final ElementTable elementTable, final String mappedColumn) {
		final StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(elementTable.getSqlName());
		sb.append(" SET ");
		sb.append(elementTable.getColumn(mappedColumn).getSqlName());
		sb.append(" = ? WHERE ");
		sb.append(elementTable.getColumnId().getSqlName());
		sb.append(" in (UNNEST(?)) ");
		return sb.toString();
	}

	@Override
	String generateParameterizedRemoveIdSQL(final ElementTable elementTable) {
		final StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ");
		sb.append(elementTable.getSqlName());
		sb.append(" WHERE ");
		sb.append(elementTable.getColumnId().getSqlName());
		sb.append(" = ? ");
		return sb.toString();
	}

	@Override
	String generateParameterizedRemoveMappedIdSQL(final ElementTable elementTable, final String mappedColumn) {
		final StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(elementTable.getSqlName());
		sb.append(" SET ");
		sb.append(elementTable.getColumn(mappedColumn).getSqlName());
		sb.append(" = null WHERE ");
		sb.append(elementTable.getColumn(mappedColumn).getSqlName());
		sb.append(" = ? ");
		return sb.toString();
	}

	@Override
	String generateUniqueTableAlias() {
		return "a" + DateUtil.generateUniqueNumberPerJVM();
	}

	@Override
	<T extends LtmObjectModelling> QueryResult<T> find(final QueryEntrance<T> tableEntrance, final QueryFilterBy where,
			final QueryOrderBy orderBy) {
		final QueryTools queryTools = QueryPool.pollQueryTools();
		queryTools.addPrincipalTableEntrance(tableEntrance);
		final List<ElementColumn> ecParamsList = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		if (where != null) {
			drawWhere(sb, queryTools, ecParamsList, where);
		}
		final String where2Count = sb.toString();
		if (orderBy != null) {
			drawOrderBy(sb, queryTools, orderBy);
		}
		final String whereOrder = sb.toString();
		sb.setLength(0);
		sb.append(" FROM ");
		final List<QueryEntrance<?>> tableEntranceList = queryTools.getTableEntranceList();
		for (int i = 0; i < tableEntranceList.size(); i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(tableEntranceList.get(i).getElementTable().getSqlName());
			sb.append(" AS ");
			sb.append(tableEntranceList.get(i).getAlias());
		}
		if (where != null) {
			sb.append(" WHERE ");
			final List<QueryExpression> expressionList = queryTools.getExpressionList();
			for (final QueryExpression expression : expressionList) {
				drawExpression(sb, queryTools, ecParamsList, expression);
				sb.append(" AND ");
			}
		}
		final String selectCountQuery = "SELECT COUNT( DISTINCT "
				+ tableEntrance.getElementTable().getColumnId().getSqlName() + ") " + sb.toString() + where2Count;
		sb.append(whereOrder);
		sb.append(" OFFSET ? FETCH ? ROWS ONLY ");
		final String fromWhereOrderLimit = sb.toString();
		sb.setLength(0);
		sb.append("SELECT DISTINCT ");
		final ElementColumn[] elemColumns = tableEntrance.getElementTable().getSelectableColumns();
		for (int i = 0; i < elemColumns.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(tableEntrance.getAlias());
			sb.append(".");
			sb.append(elemColumns[i].getSqlName());
		}
		sb.append(fromWhereOrderLimit);
		final QueryResult<T> result = new QueryResult<>(tableEntrance.getElementTable(), selectCountQuery,
				sb.toString(), ecParamsList.toArray(new ElementColumn[ecParamsList.size()]));
		synchronized (QueryPool.class) {
			queryTools.release();
			tableEntrance.release();
			if (where != null) {
				where.release();
			}
			if (orderBy != null) {
				orderBy.release();
			}
		}
		return result;
	}

	private void drawWhere(final StringBuilder sbDest, final QueryTools queryTools,
			final List<ElementColumn> ecParamsList, final QueryFilterBy where) {
		sbDest.append("(");
		drawFilterArray(sbDest, queryTools, ecParamsList, where.getFilterArray());
		sbDest.append(")");
	}

	private void drawFilterArray(final StringBuilder sbDest, final QueryTools queryTools,
			final List<ElementColumn> ecParamsList, final AbstractQueryFilter[] filterArray) {
		for (int i = 0; i < filterArray.length; i++) {
			if (MathUtil.isEven(i)) {
				if (QueryParentheses.class.isInstance(filterArray[i])) {
					drawParentheses(sbDest, queryTools, ecParamsList, (QueryParentheses) filterArray[i]);
				} else if (QueryExpression.class.isInstance(filterArray[i])) {
					drawExpression(sbDest, queryTools, ecParamsList, (QueryExpression) filterArray[i]);
				} else {
					throw new IllegalStateException();
				}
			} else {
				if (QueryFilterOperator.class.isInstance(filterArray[i])) {
					QueryFilterOperator.class.cast(filterArray[i]).draw(sbDest);
				} else {
					throw new IllegalStateException();
				}
			}
		}
	}

	private void drawParentheses(final StringBuilder sbDest, final QueryTools queryTools,
			final List<ElementColumn> ecParamsList, final QueryParentheses parentheses) {
		sbDest.append(" (");
		drawFilterArray(sbDest, queryTools, ecParamsList, parentheses.getFilterArray());
		sbDest.append(") ");
	}

	private void drawExpression(final StringBuilder sbDest, final QueryTools queryTools,
			final List<ElementColumn> ecParamsList, final QueryExpression expression) {
		final QueryParameter leftParameter = expression.getLeftParameter();
		ElementColumn leftParameterEc = null;
		if (leftParameter != null) {
			sbDest.append(" ");
			leftParameterEc = drawParameter(sbDest, queryTools, ecParamsList, leftParameter);
		}
		sbDest.append(" ");
		expression.getOperator().draw(sbDest);
		sbDest.append(" ");
		final QueryParameter rightParameter = expression.getRightParameter();
		ElementColumn rightParameterEc = null;
		if (rightParameter != null) {
			rightParameterEc = drawParameter(sbDest, queryTools, ecParamsList, rightParameter);
			sbDest.append(" ");
		}
		// TODO: ? prever mais possiveis combinacoes de entrada de parametros 'ilegais'
		if (leftParameter != null && leftParameter.isSubQuery()) {
			if (!(expression.getOperator().getOperator().ordinal() == QueryOperator.OPERATOR.exists.ordinal()
					|| expression.getOperator().getOperator().ordinal() == QueryOperator.OPERATOR.not_exists
							.ordinal())) {
				throw new IllegalStateException();
			}
			if (rightParameter != null) {
				throw new IllegalStateException();
			}
		}
		if (rightParameter != null && rightParameter.isSubQuery()) {
			if (!(expression.getOperator().getOperator().ordinal() == QueryOperator.OPERATOR.exists.ordinal()
					|| expression.getOperator().getOperator().ordinal() == QueryOperator.OPERATOR.not_exists
							.ordinal())) {
				throw new IllegalStateException();
			}
			if (leftParameter != null) {
				throw new IllegalStateException();
			}
		}
		if (leftParameter != null && leftParameterEc == null && rightParameter != null && rightParameterEc == null) {
			throw new IllegalStateException();
		}
		if (leftParameter != null && leftParameter.isDynamicParameter()) {
			if (rightParameter != null && rightParameterEc != null) {
				ecParamsList.add(rightParameterEc);
			} else {
				throw new IllegalStateException();
			}
		}
		if (rightParameter != null && rightParameter.isDynamicParameter()) {
			if (leftParameter != null && leftParameterEc != null) {
				ecParamsList.add(leftParameterEc);
			} else {
				throw new IllegalStateException();
			}
		}
	}

	private ElementColumn drawParameter(final StringBuilder sbDest, final QueryTools queryTools,
			final List<ElementColumn> ecParamsList, final QueryParameter parameter) {
		if (parameter.isColumn()) {
			return queryTools.getParameterDraw(sbDest, parameter.getTableEntrance(), parameter.getColumnFields());
		} else if (parameter.isDynamicParameter()) {
			sbDest.append("?");
			return null;
		} else if (parameter.isSubQuery()) {
			return drawSubSelect(sbDest, queryTools, ecParamsList, parameter.getSubQuery());
		} else if (parameter.isLiteralParameter()) {
			drawLiteralParameter(sbDest, parameter.getLiteralParameter(), false);
			return null;
		} else {
			throw new IllegalStateException();
		}
	}

	private void drawLiteralParameter(final StringBuilder sbDest, final Object literal, final boolean loop) {
		if (literal == null) {
			throw new IllegalStateException();
		}
		if (Byte.class.equals(literal.getClass())) {
			sbDest.append(((Byte) literal).toString());
		} else if (Short.class.equals(literal.getClass())) {
			sbDest.append(((Short) literal).toString());
		} else if (Integer.class.equals(literal.getClass())) {
			sbDest.append(((Integer) literal).toString());
		} else if (Long.class.equals(literal.getClass())) {
			sbDest.append(((Long) literal).toString());
		} else if (Boolean.class.equals(literal.getClass())) {
			sbDest.append(((((Boolean) literal).booleanValue()) ? "TRUE" : "FALSE"));
		} else if (BigDecimal.class.equals(literal.getClass()) && !loop) {
			sbDest.append(((BigDecimal) literal).toString());
		} else if (Double.class.equals(literal.getClass()) && !loop) {
			sbDest.append(((Double) literal).toString());
		} else if (String.class.equals(literal.getClass()) && !loop) {
			sbDest.append("'");
			sbDest.append((String) literal);
			sbDest.append("'");
		} else if (literal.getClass().isArray() && !loop) {
			final Object[] tmp = (Object[]) literal;
			sbDest.append("ARRAY [");
			if (tmp.length > 0) {
				drawLiteralParameter(sbDest, tmp[0], true);
			}
			for (int i = 1; i < tmp.length; i++) {
				sbDest.append(",");
				drawLiteralParameter(sbDest, tmp[i], true);
			}
			sbDest.append("]");
		} else {
			throw new IllegalStateException();
		}
	}

	private ElementColumn drawSubSelect(final StringBuilder sbDest, final QueryTools queryTools,
			final List<ElementColumn> ecParamsList, final QuerySubFind subSelect) {
		queryTools.incLevel();
		queryTools.addPrincipalTableEntrance(subSelect.getTableEntrance());
		final QueryFilterBy where = subSelect.getWhere();
		final QueryOrderBy orderBy = subSelect.getOrderBy();
		final StringBuilder sb = new StringBuilder();
		if (where != null) {
			drawWhere(sb, queryTools, ecParamsList, where);
		}
		if (orderBy != null) {
			drawOrderBy(sb, queryTools, orderBy);
		}
		final String whereOrder = sb.toString();
		sb.setLength(0);
		sb.append(" FROM ");
		final List<QueryEntrance<?>> tableEntranceList = queryTools.getTableEntranceList();
		for (int i = 0; i < tableEntranceList.size(); i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(tableEntranceList.get(i).getElementTable().getSqlName());
			sb.append(" AS ");
			sb.append(tableEntranceList.get(i).getAlias());
		}
		if (where != null) {
			sb.append(" WHERE ");
			final List<QueryExpression> expressionList = queryTools.getExpressionList();
			for (final QueryExpression expression : expressionList) {
				drawExpression(sb, queryTools, ecParamsList, expression);
				sb.append(" AND ");
			}
		}
		sbDest.append("(");
		sbDest.append("SELECT *");
		sbDest.append(sb);
		sbDest.append(whereOrder);
		sbDest.append(")");
		queryTools.decLevel();
		return subSelect.getTableEntrance().getElementTable().getColumnId();
	}

	private void drawOrderBy(final StringBuilder sbDest, final QueryTools queryTools, final QueryOrderBy orderBy) {
		final QueryColumnToOrder[] columnArray = orderBy.getColumns();
		if (columnArray != null && columnArray.length > 0) {
			sbDest.append(" ORDER BY ");
			for (int i = 0; i < columnArray.length; i++) {
				if (i != 0) {
					sbDest.append(", ");
				}
				queryTools.getParameterDraw(sbDest, columnArray[i].getColumn().getTableEntrance(),
						columnArray[i].getColumn().getColumnFields());
				if (columnArray[i].isDesc()) {
					sbDest.append(" DESC");
				} else {
					sbDest.append(" ASC");
				}
			}
		}
	}

}

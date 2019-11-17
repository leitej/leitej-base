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

import leitej.ltm.QueryFilterOperator.FILTER_OPERATOR;
import leitej.ltm.QueryOperator.OPERATOR;

/**
 *
 *
 * @author Julio Leite
 */
public final class Query {

	private static final AbstractLongTermMemory LTM_MANAGER = LongTermMemory.getInstance();

	public static final String EXPRESSION_OPERATOR_LESSER = "lesser";
	public static final String EXPRESSION_OPERATOR_LESSER_EQUAL = "lesser_equal";
	public static final String EXPRESSION_OPERATOR_EQUAL = "equal";
	public static final String EXPRESSION_OPERATOR_EQUAL_GREATER = "equal_greater";
	public static final String EXPRESSION_OPERATOR_GREATER = "greater";
	public static final String EXPRESSION_OPERATOR_LIKE = "like";
	public static final String EXPRESSION_OPERATOR_NOT_LIKE = "not_like";
	public static final String EXPRESSION_OPERATOR_DIFFERENT = "different";
	public static final String EXPRESSION_OPERATOR_EXISTS = "exists";
	public static final String EXPRESSION_OPERATOR_NOT_EXISTS = "not_exists";
	public static final String EXPRESSION_OPERATOR_IS_NULL = "is_null";
	public static final String EXPRESSION_OPERATOR_IS_NOT_NULL = "is_not_null";

	private Query() {
	}

	public static <T extends LtmObjectModelling> QueryEntrance<T> newTableEntrance(final Class<T> ltmClass) {
		@SuppressWarnings("unchecked")
		final QueryEntrance<T> result = (QueryEntrance<T>) QueryPool.pollQueryEntrance();
		result.setQueryTableEntrance(ElementTable.getInstance(ltmClass));
		return result;
	}

	public static <T extends LtmObjectModelling> QueryEntrance<T> newTableEntrance(final String dData) {
		@SuppressWarnings("unchecked")
		final QueryEntrance<T> result = (QueryEntrance<T>) QueryPool.pollQueryEntrance();
		result.setQueryTableEntrance(ElementTable.getInstance(dData));
		return result;
	}

	public static <T extends LtmObjectModelling> QueryResult<T> find(final QueryEntrance<T> tableEntrance) {
		return LTM_MANAGER.find(tableEntrance, null, null);
	}

	public static <T extends LtmObjectModelling> QueryResult<T> find(final QueryEntrance<T> tableEntrance,
			final QueryFilterBy where) {
		return LTM_MANAGER.find(tableEntrance, where, null);
	}

	public static <T extends LtmObjectModelling> QueryResult<T> find(final QueryEntrance<T> tableEntrance,
			final QueryOrderBy orderBy) {
		return LTM_MANAGER.find(tableEntrance, null, orderBy);
	}

	public static <T extends LtmObjectModelling> QueryResult<T> find(final QueryEntrance<T> tableEntrance,
			final QueryFilterBy where, final QueryOrderBy orderBy) {
		return LTM_MANAGER.find(tableEntrance, where, orderBy);
	}

	public static QueryFilterBy filterBy(final AbstractQueryFilter... filters) {
		final QueryFilterBy where = QueryPool.pollQueryFilterBy();
		where.setFilterArray(filters);
		return where;
	}

	public static QueryParentheses parentheses(final AbstractQueryFilter... filters) {
		final QueryParentheses parentheses = QueryPool.pollQueryParentheses();
		parentheses.setFilterArray(filters);
		return parentheses;
	}

	public static QueryFilterOperator and() {
		final QueryFilterOperator and = QueryPool.pollQueryFilterOperator();
		and.setOperator(FILTER_OPERATOR.and);
		return and;
	}

	public static QueryFilterOperator or() {
		final QueryFilterOperator or = QueryPool.pollQueryFilterOperator();
		or.setOperator(FILTER_OPERATOR.or);
		return or;
	}

	public static QueryExpression expression(final QueryParameter leftParameter, final QueryOperator operator,
			final QueryParameter rightParameter) {
		final QueryExpression expression = QueryPool.pollQueryExpression();
		expression.setExpression(leftParameter, operator, rightParameter);
		return expression;
	}

	public static QueryExpression expression(final QueryOperator operator, final QueryParameter rightParameter) {
		final QueryExpression expression = QueryPool.pollQueryExpression();
		expression.setExpression(null, operator, rightParameter);
		return expression;
	}

	public static QueryExpression expression(final QueryParameter leftParameter, final QueryOperator operator) {
		final QueryExpression expression = QueryPool.pollQueryExpression();
		expression.setExpression(leftParameter, operator, null);
		return expression;
	}

	public static <T extends LtmObjectModelling> QueryParameter subFind(final QueryEntrance<T> tableEntrance,
			final QueryFilterBy where, final QueryOrderBy orderBy) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		final QuerySubFind select = QueryPool.pollQuerySubFind();
		select.setSelect(tableEntrance, where, orderBy);
		parameter.setSubQuery(select);
		return parameter;
	}

	public static <T extends LtmObjectModelling> QueryParameter subFind(final QueryEntrance<T> entrance) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		final QuerySubFind select = QueryPool.pollQuerySubFind();
		select.setSelect(entrance, null, null);
		parameter.setSubQuery(select);
		return parameter;
	}

	public static <T extends LtmObjectModelling> QueryParameter field(final QueryEntrance<T> entrance,
			final String... fields) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setColumn(entrance, fields);
		return parameter;
	}

	public static QueryParameter dynamicParameter() {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setDynamicParameter();
		return parameter;
	}

	public static QueryParameter literalParameter(final Byte param) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setLiteralParameter(param);
		return parameter;
	}

	public static QueryParameter literalParameter(final Short param) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setLiteralParameter(param);
		return parameter;
	}

	public static QueryParameter literalParameter(final Integer param) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setLiteralParameter(param);
		return parameter;
	}

	public static QueryParameter literalParameter(final Long param) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setLiteralParameter(param);
		return parameter;
	}

	public static QueryParameter literalParameter(final Boolean param) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setLiteralParameter(param);
		return parameter;
	}

	public static QueryParameter literalParameter(final BigDecimal param) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setLiteralParameter(param);
		return parameter;
	}

	public static QueryParameter literalParameter(final Double param) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setLiteralParameter(param);
		return parameter;
	}

	public static QueryParameter literalParameter(final String param) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setLiteralParameter(param);
		return parameter;
	}

	public static QueryParameter literalParameter(final Byte[] param) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setLiteralParameter(param);
		return parameter;
	}

	public static QueryParameter literalParameter(final Short[] param) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setLiteralParameter(param);
		return parameter;
	}

	public static QueryParameter literalParameter(final Integer[] param) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setLiteralParameter(param);
		return parameter;
	}

	public static QueryParameter literalParameter(final Long[] param) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setLiteralParameter(param);
		return parameter;
	}

	public static QueryParameter literalParameter(final Boolean[] param) {
		final QueryParameter parameter = QueryPool.pollQueryParameter();
		parameter.setLiteralParameter(param);
		return parameter;
	}

	public static QueryOperator expressionOperator(final String op)
			throws NullPointerException, IllegalArgumentException {
		final QueryOperator expressionOperator = QueryPool.pollQueryOperator();
		expressionOperator.setOperator(Enum.valueOf(OPERATOR.class, op));
		return expressionOperator;
	}

	public static QueryOperator lesser() {
		final QueryOperator lesser = QueryPool.pollQueryOperator();
		lesser.setOperator(OPERATOR.lesser);
		return lesser;
	}

	public static QueryOperator lesserEqual() {
		final QueryOperator lesserEqual = QueryPool.pollQueryOperator();
		lesserEqual.setOperator(OPERATOR.lesser_equal);
		return lesserEqual;
	}

	public static QueryOperator equal() {
		final QueryOperator equal = QueryPool.pollQueryOperator();
		equal.setOperator(OPERATOR.equal);
		return equal;
	}

	public static QueryOperator equalGreater() {
		final QueryOperator equalGreater = QueryPool.pollQueryOperator();
		equalGreater.setOperator(OPERATOR.equal_greater);
		return equalGreater;
	}

	public static QueryOperator greater() {
		final QueryOperator greater = QueryPool.pollQueryOperator();
		greater.setOperator(OPERATOR.greater);
		return greater;
	}

	public static QueryOperator like() {
		final QueryOperator like = QueryPool.pollQueryOperator();
		like.setOperator(OPERATOR.like);
		return like;
	}

	public static QueryOperator notLike() {
		final QueryOperator notLike = QueryPool.pollQueryOperator();
		notLike.setOperator(OPERATOR.not_like);
		return notLike;
	}

	public static QueryOperator different() {
		final QueryOperator different = QueryPool.pollQueryOperator();
		different.setOperator(OPERATOR.different);
		return different;
	}

	public static QueryOperator exists() {
		final QueryOperator exists = QueryPool.pollQueryOperator();
		exists.setOperator(OPERATOR.exists);
		return exists;
	}

	public static QueryOperator notExists() {
		final QueryOperator notExists = QueryPool.pollQueryOperator();
		notExists.setOperator(OPERATOR.not_exists);
		return notExists;
	}

	public static QueryOperator isNull() {
		final QueryOperator isNull = QueryPool.pollQueryOperator();
		isNull.setOperator(OPERATOR.is_null);
		return isNull;
	}

	public static QueryOperator isNotNull() {
		final QueryOperator isNotNull = QueryPool.pollQueryOperator();
		isNotNull.setOperator(OPERATOR.is_not_null);
		return isNotNull;
	}

	public static QueryOrderBy orderBy(final QueryColumnToOrder... fields) {
		final QueryOrderBy orderBy = QueryPool.pollQueryOrderBy();
		orderBy.setColumns(fields);
		return orderBy;
	}

	public static <T extends LtmObjectModelling> QueryColumnToOrder fieldAsc(final QueryEntrance<T> entrance,
			final String... fields) {
		final QueryColumnToOrder columnToOrder = QueryPool.pollQueryColumnToOrder();
		final QueryParameter column = QueryPool.pollQueryParameter();
		column.setColumn(entrance, fields);
		columnToOrder.setColumnOrder(column, false);
		return columnToOrder;
	}

	public static <T extends LtmObjectModelling> QueryColumnToOrder fieldDesc(final QueryEntrance<T> entrance,
			final String... fields) {
		final QueryColumnToOrder columnToOrder = QueryPool.pollQueryColumnToOrder();
		final QueryParameter column = QueryPool.pollQueryParameter();
		column.setColumn(entrance, fields);
		columnToOrder.setColumnOrder(column, true);
		return columnToOrder;
	}

}

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

import leitej.util.data.AbstractWeakQueue;

/**
 *
 *
 * @author Julio Leite
 */
final class QueryPool {

	private static final QuerySubFindQueue querySubFindQueue = new QuerySubFindQueue();

	private static final class QuerySubFindQueue extends AbstractWeakQueue<QuerySubFind> {
		private static final long serialVersionUID = -1436527800602522919L;

		@Override
		protected QuerySubFind newObject() {
			return new QuerySubFind();
		}
	};

	private static final QueryFilterByQueue queryFilterByQueue = new QueryFilterByQueue();

	private static final class QueryFilterByQueue extends AbstractWeakQueue<QueryFilterBy> {
		private static final long serialVersionUID = 5849260691497924077L;

		@Override
		protected QueryFilterBy newObject() {
			return new QueryFilterBy();
		}
	};

	private static final QueryOrderByQueue queryOrderByQueue = new QueryOrderByQueue();

	private static final class QueryOrderByQueue extends AbstractWeakQueue<QueryOrderBy> {
		private static final long serialVersionUID = -5141659520289961868L;

		@Override
		protected QueryOrderBy newObject() {
			return new QueryOrderBy();
		}
	};

	private static final QueryParenthesesQueue queryParenthesesQueue = new QueryParenthesesQueue();

	private static final class QueryParenthesesQueue extends AbstractWeakQueue<QueryParentheses> {
		private static final long serialVersionUID = -7715425238150860617L;

		@Override
		protected QueryParentheses newObject() {
			return new QueryParentheses();
		}
	};

	private static final QueryExpressionQueue queryExpressionQueue = new QueryExpressionQueue();

	private static final class QueryExpressionQueue extends AbstractWeakQueue<QueryExpression> {
		private static final long serialVersionUID = 2932016103678521870L;

		@Override
		protected QueryExpression newObject() {
			return new QueryExpression();
		}
	};

	private static final QueryParameterQueue queryParameterQueue = new QueryParameterQueue();

	private static final class QueryParameterQueue extends AbstractWeakQueue<QueryParameter> {
		private static final long serialVersionUID = -5240178416861011953L;

		@Override
		protected QueryParameter newObject() {
			return new QueryParameter();
		}
	};

	private static final QueryOperatorQueue queryOperatorQueue = new QueryOperatorQueue();

	private static final class QueryOperatorQueue extends AbstractWeakQueue<QueryOperator> {
		private static final long serialVersionUID = -5881311982571102845L;

		@Override
		protected QueryOperator newObject() {
			return new QueryOperator();
		}
	};

	private static final QueryColumnToOrderQueue queryColumnToOrderQueue = new QueryColumnToOrderQueue();

	private static final class QueryColumnToOrderQueue extends AbstractWeakQueue<QueryColumnToOrder> {
		private static final long serialVersionUID = -2882697014292941864L;

		@Override
		protected QueryColumnToOrder newObject() {
			return new QueryColumnToOrder();
		}
	};

	private static final QueryFilterOperatorQueue queryFilterOperatorQueue = new QueryFilterOperatorQueue();

	private static final class QueryFilterOperatorQueue extends AbstractWeakQueue<QueryFilterOperator> {
		private static final long serialVersionUID = -1087914059826253871L;

		@Override
		protected QueryFilterOperator newObject() {
			return new QueryFilterOperator();
		}
	};

	private static final QueryEntranceQueue queryEntranceQueue = new QueryEntranceQueue();

	private static final class QueryEntranceQueue extends AbstractWeakQueue<QueryEntrance<LtmObjectModelling>> {
		private static final long serialVersionUID = 296741354352796484L;

		@Override
		protected QueryEntrance<LtmObjectModelling> newObject() {
			return new QueryEntrance<>();
		}
	};

	private static final QueryToolsQueue queryToolsQueue = new QueryToolsQueue();

	private static final class QueryToolsQueue extends AbstractWeakQueue<QueryTools> {
		private static final long serialVersionUID = 7605941949761350655L;

		@Override
		protected QueryTools newObject() {
			return new QueryTools();
		}
	};

	private QueryPool() {
	}

	static QuerySubFind pollQuerySubFind() {
		return querySubFindQueue.poll();
	}

	static void offerQuerySubFind(final QuerySubFind querySelect) {
		querySubFindQueue.offer(querySelect);
	}

	static QueryFilterBy pollQueryFilterBy() {
		return queryFilterByQueue.poll();
	}

	static void offerQueryFilterBy(final QueryFilterBy queryWhere) {
		queryFilterByQueue.offer(queryWhere);
	}

	static QueryOrderBy pollQueryOrderBy() {
		return queryOrderByQueue.poll();
	}

	static void offerQueryOrderBy(final QueryOrderBy queryOrderBy) {
		queryOrderByQueue.offer(queryOrderBy);
	}

	static QueryParentheses pollQueryParentheses() {
		return queryParenthesesQueue.poll();
	}

	static void offerQueryParentheses(final QueryParentheses queryParentheses) {
		queryParenthesesQueue.offer(queryParentheses);
	}

	static QueryExpression pollQueryExpression() {
		return queryExpressionQueue.poll();
	}

	static void offerQueryExpression(final QueryExpression queryExpression) {
		queryExpressionQueue.offer(queryExpression);
	}

	static QueryParameter pollQueryParameter() {
		return queryParameterQueue.poll();
	}

	static void offerQueryParameter(final QueryParameter queryParameter) {
		queryParameterQueue.offer(queryParameter);
	}

	static QueryOperator pollQueryOperator() {
		return queryOperatorQueue.poll();
	}

	static void offerQueryOperator(final QueryOperator queryOperator) {
		queryOperatorQueue.offer(queryOperator);
	}

	static QueryColumnToOrder pollQueryColumnToOrder() {
		return queryColumnToOrderQueue.poll();
	}

	static void offerQueryColumnToOrder(final QueryColumnToOrder queryColumnToOrder) {
		queryColumnToOrderQueue.offer(queryColumnToOrder);
	}

	static QueryFilterOperator pollQueryFilterOperator() {
		return queryFilterOperatorQueue.poll();
	}

	static void offerQueryFilterOperator(final QueryFilterOperator queryFilterOperator) {
		queryFilterOperatorQueue.offer(queryFilterOperator);
	}

	static QueryEntrance<LtmObjectModelling> pollQueryEntrance() {
		return queryEntranceQueue.poll();
	}

	static void offerQueryEntrance(final QueryEntrance<LtmObjectModelling> queryEntrance) {
		queryEntranceQueue.offer(queryEntrance);
	}

	static QueryTools pollQueryTools() {
		return queryToolsQueue.poll();
	}

	static void offerQueryTools(final QueryTools queryTools) {
		queryToolsQueue.offer(queryTools);
	}

}

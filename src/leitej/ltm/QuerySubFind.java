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

/**
 *
 *
 * @author Julio Leite
 */
final class QuerySubFind extends AbstractQueryPool {

	private QueryEntrance<?> tableEntrance;
	private QueryFilterBy where;
	private QueryOrderBy orderBy;

	QuerySubFind() {
	}

	<T extends LtmObjectModelling> void setSelect(final QueryEntrance<T> tableEntrance, final QueryFilterBy where,
			final QueryOrderBy orderBy) {
		this.tableEntrance = tableEntrance;
		this.where = where;
		this.orderBy = orderBy;
	}

	@SuppressWarnings("unchecked")
	<T extends LtmObjectModelling> QueryEntrance<T> getTableEntrance() {
		return (QueryEntrance<T>) this.tableEntrance;
	}

	QueryFilterBy getWhere() {
		return this.where;
	}

	QueryOrderBy getOrderBy() {
		return this.orderBy;
	}

	@Override
	void release() {
		if (this.tableEntrance != null) {
			this.tableEntrance.release();
			this.tableEntrance = null;
		}
		if (this.where != null) {
			this.where.release();
			this.where = null;
		}
		if (this.orderBy != null) {
			this.orderBy.release();
			this.orderBy = null;
		}
		QueryPool.offerQuerySubFind(this);
	}

}

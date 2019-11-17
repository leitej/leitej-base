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
final class QueryParameter extends AbstractQueryPool {

	private QueryEntrance<?> tableEntrance;
	private String[] fields = null;
	private boolean dynamicParameter = false;
	private Object literalParameter = null;
	private QuerySubFind subQuery = null;

	QueryParameter() {
	}

	boolean isSubQuery() {
		return this.subQuery != null;
	}

	void setSubQuery(final QuerySubFind subQuery) {
		this.tableEntrance = null;
		this.fields = null;
		this.dynamicParameter = false;
		this.literalParameter = null;
		this.subQuery = subQuery;
	}

	QuerySubFind getSubQuery() {
		return this.subQuery;
	}

	boolean isColumn() {
		return this.fields != null;
	}

	<T extends LtmObjectModelling> void setColumn(final QueryEntrance<T> tableEntrance, final String[] fields) {
		this.tableEntrance = tableEntrance;
		this.fields = fields;
		this.dynamicParameter = false;
		this.literalParameter = null;
		this.subQuery = null;
	}

	@SuppressWarnings("unchecked")
	<T extends LtmObjectModelling> QueryEntrance<T> getTableEntrance() {
		return (QueryEntrance<T>) this.tableEntrance;
	}

	String[] getColumnFields() {
		return this.fields;
	}

	boolean isDynamicParameter() {
		return this.dynamicParameter;
	}

	void setDynamicParameter() {
		this.tableEntrance = null;
		this.fields = null;
		this.dynamicParameter = true;
		this.literalParameter = null;
		this.subQuery = null;
	}

	boolean isLiteralParameter() {
		return this.subQuery == null && this.fields == null && !this.dynamicParameter;
	}

	void setLiteralParameter(final Object param) {
		this.tableEntrance = null;
		this.fields = null;
		this.dynamicParameter = false;
		this.literalParameter = param;
		this.subQuery = null;
	}

	Object getLiteralParameter() {
		return this.literalParameter;
	}

	@Override
	void release() {
		if (this.tableEntrance != null) {
			this.tableEntrance.release();
			this.tableEntrance = null;
		}
		this.fields = null;
		this.dynamicParameter = false;
		this.literalParameter = null;
		if (this.subQuery != null) {
			this.subQuery.release();
			this.subQuery = null;
		}
		QueryPool.offerQueryParameter(this);
	}

}

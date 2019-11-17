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
final class QueryFilterOperator extends AbstractQueryFilter {

	static enum FILTER_OPERATOR {
		and, or
	};

	private FILTER_OPERATOR operator;

	QueryFilterOperator() {
	}

	FILTER_OPERATOR getOperator() {
		return this.operator;
	}

	void setOperator(final FILTER_OPERATOR operator) {
		this.operator = operator;
	}

	void draw(final StringBuilder sbDest) {
		switch (this.operator) {
		case and:
			sbDest.append(" and");
			break;
		case or:
			sbDest.append(" or");
			break;
		default:
			throw new IllegalStateException();
		}
	}

	@Override
	void release() {
		this.operator = null;
		QueryPool.offerQueryFilterOperator(this);
	}

}

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
final class QueryOperator extends AbstractQueryPool {

	static enum OPERATOR {
		lesser, lesser_equal, equal, equal_greater, greater, like, not_like, different, exists, not_exists, is_null,
		is_not_null
	}

	private OPERATOR operator;

	QueryOperator() {
	}

	OPERATOR getOperator() {
		return this.operator;
	}

	void setOperator(final OPERATOR operator) {
		this.operator = operator;
	}

	void draw(final StringBuilder sbDest) {
		switch (this.operator) {
		case lesser:
			sbDest.append("<");
			break;
		case lesser_equal:
			sbDest.append("<=");
			break;
		case equal:
			sbDest.append("=");
			break;
		case equal_greater:
			sbDest.append(">=");
			break;
		case greater:
			sbDest.append(">");
			break;
		case like:
			sbDest.append("LIKE");
			break;
		case not_like:
			sbDest.append("NOT LIKE");
			break;
		case different:
			sbDest.append("<>");
			break;
		case exists:
			sbDest.append("EXISTS");
			break;
		case not_exists:
			sbDest.append("NOT EXISTS");
			break;
		case is_null:
			sbDest.append("IS NULL");
			break;
		case is_not_null:
			sbDest.append("IS NOT NULL");
			break;
		default:
			throw new IllegalStateException();
		}
	}

	@Override
	void release() {
		this.operator = null;
		QueryPool.offerQueryOperator(this);
	}

}

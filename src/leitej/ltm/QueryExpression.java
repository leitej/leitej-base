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
final class QueryExpression extends AbstractQueryFilter {

	private QueryParameter leftParameter;
	private QueryOperator operator;
	private QueryParameter rightParameter;

	@Override
	void release() {
		if (this.leftParameter != null) {
			this.leftParameter.release();
			this.leftParameter = null;
		}
		if (this.operator != null) {
			this.operator.release();
			this.operator = null;
		}
		if (this.rightParameter != null) {
			this.rightParameter.release();
			this.rightParameter = null;
		}
		QueryPool.offerQueryExpression(this);
	}

	void setExpression(final QueryParameter leftParameter, final QueryOperator operator,
			final QueryParameter rightParameter) {
		this.leftParameter = leftParameter;
		this.operator = operator;
		this.rightParameter = rightParameter;
	}

	QueryParameter getLeftParameter() {
		return this.leftParameter;
	}

	QueryOperator getOperator() {
		return this.operator;
	}

	QueryParameter getRightParameter() {
		return this.rightParameter;
	}

}

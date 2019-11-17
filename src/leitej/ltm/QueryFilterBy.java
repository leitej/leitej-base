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
final class QueryFilterBy extends AbstractQueryPool {

	private AbstractQueryFilter[] filterArray;

	QueryFilterBy() {
	}

	AbstractQueryFilter[] getFilterArray() {
		return this.filterArray;
	}

	void setFilterArray(final AbstractQueryFilter[] filters) {
		this.filterArray = filters;
	}

	@Override
	void release() {
		if (this.filterArray != null) {
			for (final AbstractQueryFilter tmp : this.filterArray) {
				tmp.release();
			}
			this.filterArray = null;
		}
		QueryPool.offerQueryFilterBy(this);
	}

}

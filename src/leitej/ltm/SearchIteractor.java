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

import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import leitej.exception.LtmLtRtException;
import leitej.exception.ObjectPoolLtException;

/**
 * @author Julio Leite
 *
 */
final class SearchIteractor<T extends LtmObjectModelling> implements Iterator<T> {

	private static final DataMemoryPool MEM_POOL = DataMemoryPool.getInstance();
	private static final LongTermMemory LTM = LongTermMemory.getInstance();

	private final Class<T> ltmClass;
	private final String query;
	private final Object[] parameters;
	private final DataMemoryType[] types;
	private Long nextId;

	SearchIteractor(final Class<T> ltmClass, final String queryFilter, final Object[] parameters,
			final DataMemoryType[] types, final boolean orderDesc) {
		this.ltmClass = ltmClass;
		this.query = HsqldbUtil.getStatementScaledIterator(HsqldbUtil.getTableName(ltmClass), queryFilter, orderDesc);
		this.parameters = parameters;
		this.types = types;
		this.nextId = fetchNextId(((orderDesc) ? Long.MAX_VALUE : Long.MIN_VALUE));
	}

	@Override
	public boolean hasNext() {
		return this.nextId != null;
	}

	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		final long ltmId = this.nextId;
		this.nextId = fetchNextId(this.nextId);
		return LTM.fetch(this.ltmClass, ltmId);
	}

	private Long fetchNextId(final long id) {
		Long result;
		DataMemoryConnection conn = null;
		try {
			try {
				conn = MEM_POOL.poll();
				result = conn.getNextId(this.query, this.types, this.parameters, id);
			} finally {
				if (conn != null) {
					MEM_POOL.offer(conn);
				}
			}
		} catch (ObjectPoolLtException | InterruptedException | SQLException e) {
			throw new LtmLtRtException(e);
		}
		return result;
	}

	// Comparison and hashing

	@Override
	public boolean equals(final Object o) {
		return (this == o);
	}

	@Override
	public int hashCode() {
		return this.query.hashCode() + this.parameters.hashCode();
	}

}

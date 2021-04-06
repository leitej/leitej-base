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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import leitej.exception.LtmLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.log.Logger;

/**
 * @author Julio Leite
 *
 */
public final class LtmSet<E extends LtmObjectModelling> implements Set<E> {

	private static final Logger LOG = Logger.getInstance();

	private static final DataMemoryPool MEM_POOL = DataMemoryPool.getInstance();

	private final Class<E> ltmClass;
	private final String tablename;
	private final String queryFilter;
	private final Object[] parameters;
	private final DataMemoryType[] types;
	private final String pSttCount;
	private final String pSttHasResult;
	private final String pSttContains;

	LtmSet(final Class<E> ltmClass, final String queryFilter, final Object[] parameters, final DataMemoryType[] types) {
		LOG.trace("#0", ltmClass);
		this.ltmClass = ltmClass;
		this.tablename = HsqldbUtil.getTablename(ltmClass);
		this.queryFilter = queryFilter;
		this.parameters = parameters;
		this.types = types;
		this.pSttCount = HsqldbUtil.getStatementCount(this.tablename, this.queryFilter);
		this.pSttHasResult = HsqldbUtil.getStatementHasResult(this.tablename, this.queryFilter);
		this.pSttContains = HsqldbUtil.getStatementContains(this.tablename, this.queryFilter);
	}

	// Query Operations

	@Override
	public int size() {
		final int result;
		DataMemoryConnection conn = null;
		try {
			try {
				conn = MEM_POOL.poll();
				result = conn.count(this.pSttCount, this.types, this.parameters);
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

	@Override
	public boolean isEmpty() {
		boolean result;
		DataMemoryConnection conn = null;
		try {
			try {
				conn = MEM_POOL.poll();
				result = !conn.hasResult(this.pSttHasResult, this.types, this.parameters);
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

	@Override
	public boolean contains(final Object o) {
		boolean result;
		if (o != null && this.ltmClass.equals(o.getClass())) {
			final Long oId = LtmObjectModelling.class.cast(o).getId();
			DataMemoryConnection conn = null;
			try {
				try {
					conn = MEM_POOL.poll();
					result = conn.containsID(this.pSttContains, this.types, this.parameters, oId);
				} finally {
					if (conn != null) {
						MEM_POOL.offer(conn);
					}
				}
			} catch (ObjectPoolLtException | InterruptedException | SQLException e) {
				throw new LtmLtRtException(e);
			}
		} else {
			result = false;
		}
		return result;
	}

	@Override
	public Iterator<E> iterator() {
		return new LtmIteractor<>(this.ltmClass, this.queryFilter, this.parameters, this.types);
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		throw new UnsupportedOperationException();
	}

	// Modification Operations

	@Override
	public boolean add(final E e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(final Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	// Bulk Operations

	@Override
	public boolean containsAll(final Collection<?> c) {
		boolean result;
		if (c != null) {
			final Set<Long> idSet = new HashSet<>();
			boolean matchClass = true;
			final Iterator<?> it = c.iterator();
			Object o;
			while (matchClass && it.hasNext()) {
				o = it.next();
				if (o != null) {
					if (Long.class.equals(o)) {
						idSet.add(Long.class.cast(o));
					} else if (this.ltmClass.equals(o.getClass())) {
						idSet.add(LtmObjectModelling.class.cast(o).getId());
					} else {
						matchClass = false;
					}
				} else {
					matchClass = false;
				}
			}
			if (matchClass) {
				if (idSet.size() == 0) {
					result = true;
				} else {
					DataMemoryConnection conn = null;
					try {
						try {
							conn = MEM_POOL.poll();
							result = conn.containsIDset(this.pSttContains, this.types, this.parameters, idSet);
						} finally {
							if (conn != null) {
								MEM_POOL.offer(conn);
							}
						}
					} catch (ObjectPoolLtException | InterruptedException | SQLException e) {
						throw new LtmLtRtException(e);
					}
				}
			} else {
				result = false;
			}
		} else {
			result = false;
		}
		return result;
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	// Comparison and hashing

	@Override
	public boolean equals(final Object o) {
		if (o == null || !LtmSet.class.isInstance(o)) {
			return false;
		} else {
			return hashCode() == o.hashCode();
		}
	}

	@Override
	public int hashCode() {
		return this.pSttCount.hashCode() + this.parameters.hashCode();
	}

}

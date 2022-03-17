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
import java.util.Iterator;
import java.util.Set;

import leitej.exception.ImplementationLtRtException;
import leitej.exception.LtmLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.exception.UnsupportedDataTypeLtRtException;
import leitej.log.Logger;

/**
 * @author Julio Leite
 *
 */
public final class LtmSet<E> implements Set<E> {

	private static final Logger LOG = Logger.getInstance();

	private static final DataMemoryPool MEM_POOL = DataMemoryPool.getInstance();

	private final DataProxyHandler dataPH;
	private final Class<LtmObjectModelling> ltmClass;
	private final long ltmId;
	private final DataMemoryType dataType;
	private final Class<?> setParameterdataClass;
	private final String tablenameSet;
	private final String pSttCount;
	private final String pSttHasResult;
	private final String pSttSetContains;
	private final String pSttSetAdd;
	private final String pSttSetRemove;
	private final String pSttSetClear;

	LtmSet(final DataProxyHandler dataPH, final String datanameSet) {
		LOG.debug("#0: #1 - id: #2", dataPH.getPreparedClass().getInterface(), datanameSet, dataPH.getLtmId());
		final PreparedClass preparedClass = dataPH.getPreparedClass();
		this.dataPH = dataPH;
		this.ltmClass = dataPH.getInterface();
		this.ltmId = dataPH.getLtmId();
		this.dataType = preparedClass.getColumnsSetType(datanameSet);
		this.setParameterdataClass = preparedClass.getColumnsSetClass(datanameSet);
		this.tablenameSet = preparedClass.getSetTablename(datanameSet);
		this.pSttCount = HsqldbUtil.getStatementSetCount(this.tablenameSet, dataPH.getLtmId());
		this.pSttHasResult = HsqldbUtil.getStatementSetHasResult(this.tablenameSet, dataPH.getLtmId());
		this.pSttSetContains = HsqldbUtil.getStatementSetContains(this.tablenameSet, dataPH.getLtmId());
		this.pSttSetAdd = HsqldbUtil.getStatementSetAdd(this.tablenameSet, dataPH.getPreparedClass().getTablename(),
				dataPH.getLtmId());
		this.pSttSetRemove = HsqldbUtil.getStatementSetRemove(this.tablenameSet, dataPH.getLtmId());
		this.pSttSetClear = preparedClass.getDeleteOnSetByLtmId(datanameSet);
	}

	// Common

	public boolean isMaintainedByThisSet(final Object o) {
		try {
			if (o == null || !this.setParameterdataClass.isInstance(o)) {
				return false;
			}
		} catch (final UnsupportedDataTypeLtRtException e) {
			return false;
		}
		return true;
	}

	// Query Operations

	@Override
	public int size() {
		final int result;
		DataMemoryConnection conn = null;
		try {
			try {
				conn = MEM_POOL.poll();
				result = conn.countSet(this.pSttCount);
			} finally {
				if (conn != null) {
					MEM_POOL.offer(conn);
				}
			}
		} catch (ObjectPoolLtException | InterruptedException | SQLException e) {
			throw new LtmLtRtException(e);
		}
		this.dataPH.isValid();
		return result;
	}

	@Override
	public boolean isEmpty() {
		boolean result;
		DataMemoryConnection conn = null;
		try {
			try {
				conn = MEM_POOL.poll();
				result = !conn.hasResultSet(this.pSttHasResult);
			} finally {
				if (conn != null) {
					MEM_POOL.offer(conn);
				}
			}
		} catch (ObjectPoolLtException | InterruptedException | SQLException e) {
			throw new LtmLtRtException(e);
		}
		this.dataPH.isValid();
		return result;
	}

	@Override
	public boolean contains(final Object o) {
		if (!isMaintainedByThisSet(o)) {
			return false;
		}
		boolean result;
		DataMemoryConnection conn = null;
		try {
			try {
				conn = MEM_POOL.poll();
				result = conn.containsValueSet(this.pSttSetContains, this.dataType, o);
			} finally {
				if (conn != null) {
					MEM_POOL.offer(conn);
				}
			}
		} catch (ObjectPoolLtException | InterruptedException | SQLException e) {
			throw new LtmLtRtException(e);
		}
		this.dataPH.isValid();
		return result;
	}

	@Override
	public Iterator<E> iterator() {
		this.dataPH.isValid();
		return new LtmSetIterator<>(this.dataPH, this.dataType, this.setParameterdataClass, this.tablenameSet);
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
		if (!isMaintainedByThisSet(e)) {
			return false;
		}
		this.dataPH.isValid();
		boolean result;
		DataMemoryConnection conn = null;
		try {
			try {
				conn = MEM_POOL.poll();
				result = conn.addValueSet(this.pSttSetAdd, this.dataType, e, this.ltmClass, this.ltmId);
			} finally {
				if (conn != null) {
					MEM_POOL.offer(conn);
				}
			}
		} catch (ObjectPoolLtException | InterruptedException | SQLException ex) {
			throw new LtmLtRtException(ex);
		}
		return result;
	}

	@Override
	public boolean remove(final Object o) {
		if (!isMaintainedByThisSet(o)) {
			return false;
		}
		this.dataPH.isValid();
		boolean result;
		DataMemoryConnection conn = null;
		try {
			try {
				conn = MEM_POOL.poll();
				result = conn.removeValueSet(this.pSttSetRemove, this.dataType, o, this.ltmClass, this.ltmId);
			} finally {
				if (conn != null) {
					MEM_POOL.offer(conn);
				}
			}
		} catch (ObjectPoolLtException | InterruptedException | SQLException ex) {
			throw new LtmLtRtException(ex);
		}
		return result;
	}

	// Bulk Operations

	@Override
	public boolean containsAll(final Collection<?> c) {
		// TODO Optimize
		this.dataPH.isValid();
		if (c != null) {
			for (final Object object : c) {
				if (!contains(object)) {
					return false;
				}
			}
		}
		return true;
	}
//	@Override
//	public boolean containsAll(final Collection<?> c) {
//		boolean result;
//		if (c != null) {
//			final Set<Long> idSet = new HashSet<>();
//			boolean matchClass = true;
//			final Iterator<?> it = c.iterator();
//			Object o;
//			while (matchClass && it.hasNext()) {
//				o = it.next();
//				if (o != null) {
//					if (Long.class.equals(o)) {
//						idSet.add(Long.class.cast(o));
//					} else if (this.ltmClass.equals(o.getClass())) {
//						idSet.add(LtmObjectModelling.class.cast(o).getLtmId());
//					} else {
//						matchClass = false;
//					}
//				} else {
//					matchClass = false;
//				}
//			}
//			if (matchClass) {
//				if (idSet.size() == 0) {
//					result = true;
//				} else {
//					DataMemoryConnection conn = null;
//					try {
//						try {
//							conn = MEM_POOL.poll();
//							result = conn.containsLTMcollection(this.pSttContains, this.types, this.parameters, idSet);
//						} finally {
//							if (conn != null) {
//								MEM_POOL.offer(conn);
//							}
//						}
//					} catch (ObjectPoolLtException | InterruptedException | SQLException e) {
//						throw new LtmLtRtException(e);
//					}
//				}
//			} else {
//				result = false;
//			}
//		} else {
//			result = false;
//		}
//		return result;
//	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		// TODO Optimize
		// this.dataPH.isValid();
		boolean result = false;
		if (c != null) {
			for (final E object : c) {
				result |= add(object);
			}
		}
		return result;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		// TODO Auto-generated method stub
		this.dataPH.isValid();
		throw new ImplementationLtRtException("Not implemented, yet");
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		// TODO Optimize
		// this.dataPH.isValid();
		boolean result = false;
		if (c != null) {
			for (final Object object : c) {
				result |= remove(object);
			}
		}
		return result;
	}

	@Override
	public void clear() {
		this.dataPH.isValid();
		DataMemoryConnection conn = null;
		try {
			try {
				conn = MEM_POOL.poll();
				conn.clearSetByLtmId(this.pSttSetClear, this.tablenameSet, this.dataType, this.ltmClass, this.ltmId);
			} finally {
				if (conn != null) {
					MEM_POOL.offer(conn);
				}
			}
		} catch (ObjectPoolLtException | InterruptedException | SQLException ex) {
			throw new LtmLtRtException(ex);
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

}

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
import java.util.NoSuchElementException;
import java.util.Set;

import leitej.exception.LtmLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.log.Logger;

/**
 * @author Julio Leite
 *
 */
public final class InitialSet<E> implements Set<E> {

	private static final Logger LOG = Logger.getInstance();

	private static final DataMemoryPool MEM_POOL = DataMemoryPool.getInstance();

	static <E extends Object> Set<E> instantiateSet(final DataProxyHandler dataPH, final String datanameSet) {
		if (dataPH.getPreparedClass().getColumnsSetType(datanameSet) == null) {
			return new InitialSet<>(dataPH, datanameSet);
		} else {
			return new LtmSet<>(dataPH, datanameSet);
		}
	}

	private final DataProxyHandler dataPH;
	private final String datanameSet;
	private LtmSet<E> ltmSet;

	private InitialSet(final DataProxyHandler dataPH, final String datanameSet) {
		LOG.debug("#0: #1 - id: #2", dataPH.getPreparedClass().getInterface(), datanameSet, dataPH.getLtmId());
		this.dataPH = dataPH;
		this.datanameSet = datanameSet;
		this.ltmSet = null;
	}

	@Override
	public int size() {
		if (this.ltmSet == null) {
			this.dataPH.isValid();
			return 0;
		} else {
			return this.ltmSet.size();
		}
	}

	@Override
	public boolean isEmpty() {
		if (this.ltmSet == null) {
			this.dataPH.isValid();
			return true;
		} else {
			return this.ltmSet.isEmpty();
		}
	}

	@Override
	public boolean contains(final Object o) {
		if (this.ltmSet == null) {
			this.dataPH.isValid();
			return false;
		} else {
			return this.ltmSet.contains(o);
		}
	}

	@Override
	public Iterator<E> iterator() {
		if (this.ltmSet == null) {
			this.dataPH.isValid();
			return new EmptyIterator<>();
		} else {
			return this.ltmSet.iterator();
		}
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(final E e) {
		if (e == null) {
			return false;
		}
		synchronized (this.dataPH) {
			if (this.ltmSet == null) {
				// ensure prepareClass receive the type
				this.dataPH.getPreparedClass().registerSetParameter(this.datanameSet, e);
				// create the tableSet
				DataMemoryConnection conn = null;
				try {
					try {
						conn = MEM_POOL.poll();
						conn.initializeSet(this.dataPH.getPreparedClass(), this.datanameSet);
					} finally {
						if (conn != null) {
							MEM_POOL.offer(conn);
						}
					}
				} catch (ObjectPoolLtException | InterruptedException | SQLException ex) {
					throw new LtmLtRtException(ex);
				}
				this.ltmSet = new LtmSet<>(this.dataPH, this.datanameSet);
			}
		}
		return this.ltmSet.add(e);
	}

	@Override
	public boolean remove(final Object o) {
		if (this.ltmSet == null) {
			this.dataPH.isValid();
			return false;
		} else {
			return this.ltmSet.remove(o);
		}
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		if (this.ltmSet == null) {
			this.dataPH.isValid();
			return c.isEmpty();
		} else {
			return this.ltmSet.containsAll(c);
		}
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		if (this.ltmSet == null) {
			boolean result = false;
			if (c != null) {
				for (final E object : c) {
					result |= add(object);
				}
			}
			return result;
		} else {
			return this.ltmSet.addAll(c);
		}
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		if (this.ltmSet == null) {
			this.dataPH.isValid();
			return false;
		} else {
			return this.ltmSet.retainAll(c);
		}
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		if (this.ltmSet == null) {
			this.dataPH.isValid();
			return false;
		} else {
			return this.ltmSet.removeAll(c);
		}
	}

	@Override
	public void clear() {
		if (this.ltmSet == null) {
			this.dataPH.isValid();
			return;
		} else {
			this.ltmSet.clear();
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private static final class EmptyIterator<I> implements Iterator<I> {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public I next() {
			throw new NoSuchElementException();
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			throw new CloneNotSupportedException();
		}

	}

}

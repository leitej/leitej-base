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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import leitej.exception.ClosedLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.log.Logger;
import leitej.ltm.exception.LtmLtRtException;
import leitej.util.ThreadLock;

/**
 *
 *
 * @author Julio Leite
 */
public final class ScaledSet<T extends LtmObjectModelling> implements Set<T>, Serializable {

	private static final long serialVersionUID = -8247780550319549793L;

	private static final Logger LOG = Logger.getInstance();
	private static final DataProxy DATA_PROXY = DataProxy.getInstance();
	private static final AbstractLongTermMemory LTM_MANAGER = LongTermMemory.getInstance();

	private final ThreadLock dbAccess;

	private final LtmObjectModelling owner;
	private final ElementTable elementTable;
	private final String mappedColumn;

	private long dbResultSize;
	private final int fetchScale;
	private final int fetchScaleBorder;
	private final int absoluteFirstLineNum;
	private int firstLineNumDbCache;
	private int lastLineNumDbCache; // exclusive
	private final List<T> dbCacheList;
	private final List<T> addedList;
	private final List<T> removedList;
	private final List<Long> removedIdList;
	private boolean removeAll;
	private boolean isNew;
	private volatile boolean isScaled;

	private volatile boolean saveChangedOnPersist;

	private transient volatile int modCount = 0;

	/**
	 *
	 * @param owner
	 * @param elementTable
	 * @param mappedColumn
	 * @param fetchScale
	 */
	ScaledSet(final LtmObjectModelling owner, final ElementTable elementTable, final String mappedColumn,
			final int fetchScale) {
		this.dbAccess = new ThreadLock();
		this.owner = owner;
		this.elementTable = elementTable;
		this.mappedColumn = mappedColumn;
		this.dbResultSize = -1;
		this.fetchScale = fetchScale;
		this.fetchScaleBorder = calcFetchScaleBorder(fetchScale);
		this.absoluteFirstLineNum = LTM_MANAGER.absoluteNumberOfFirstRow();
		this.firstLineNumDbCache = this.absoluteFirstLineNum;
		this.lastLineNumDbCache = this.absoluteFirstLineNum;
		if (fetchScale == Integer.MAX_VALUE) {
			this.dbCacheList = new ArrayList<>();
		} else {
			this.dbCacheList = new ArrayList<>(fetchScale);
		}
		this.addedList = new ArrayList<>();
		this.removedList = new ArrayList<>();
		this.removedIdList = new ArrayList<>();
		this.removeAll = false;
		this.isNew = owner.isNew();
		this.isScaled = false;
	}

	private static int calcFetchScaleBorder(final int fetchScale) {
		int result;
		if (fetchScale > 11) {
			result = fetchScale / 4;
		} else if (fetchScale > 6) {
			result = 2;
		} else if (fetchScale > 2) {
			result = 1;
		} else {
			result = 0;
		}
		return result;
	}

	private void reset() {
		this.dbResultSize = -1;
		this.firstLineNumDbCache = this.absoluteFirstLineNum;
		this.lastLineNumDbCache = this.absoluteFirstLineNum;
		this.dbCacheList.clear();
		this.addedList.clear();
		this.removedList.clear();
		this.removedIdList.clear();
		this.removeAll = false;
		this.isNew = this.owner.isNew();
		this.isScaled = false;
	}

	/**
	 * 
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close <br/>
	 *                          +Cause ObjectPoolLtException if can't instantiate a
	 *                          new connection <br/>
	 *                          +Cause InterruptedException if interrupted while
	 *                          waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection
	 */
	private void initSet() throws LtmLtRtException {
		if (this.dbResultSize == -1) {
			dbCacheFetchFor(this.firstLineNumDbCache);
		}
	}

	/**
	 *
	 * @param lineNumber
	 * @return
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close <br/>
	 *                          +Cause ObjectPoolLtException if can't instantiate a
	 *                          new connection <br/>
	 *                          +Cause InterruptedException if interrupted while
	 *                          waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> dbCacheFetchFor(final int lineNumber) throws LtmLtRtException {
		try {
			List<Map<String, Object>> dataList = null;
			if (!this.isNew && !this.removeAll
					&& (lineNumber < this.firstLineNumDbCache || lineNumber >= this.lastLineNumDbCache)) {
				final int fetchFrom = calcFetchFrom(lineNumber);
				if (fetchFrom != -1) {
					dataList = this.elementTable.dbFetchMappedData(this.mappedColumn, this.owner.getId(),
							this.removedIdList, fetchFrom, this.fetchScale);
					if (dataList.size() == this.fetchScale
							|| (dataList.size() == 0 && fetchFrom > this.absoluteFirstLineNum)) {
						this.dbResultSize = this.elementTable.dbMappedDataSize(this.mappedColumn, this.owner.getId(),
								this.removedIdList);
					} else {
						this.dbResultSize = fetchFrom - this.absoluteFirstLineNum + dataList.size();
					}
					final List<T> tmp = new ArrayList<>(dataList.size());
					for (final Map<String, Object> data : dataList) {
						tmp.add((T) DATA_PROXY.getTableInstance(this.elementTable,
								(Long) data.get(this.elementTable.getColumnId().getJavaName()), data));
					}
					final int fetchTo = fetchFrom + tmp.size();
					int insertIndex;
					if (fetchFrom > this.lastLineNumDbCache || fetchTo - 1 < this.firstLineNumDbCache) {
						this.dbCacheList.clear();
						insertIndex = 0;
						this.firstLineNumDbCache = fetchFrom;
						this.lastLineNumDbCache = fetchFrom;
					} else {
						if (lineNumber < this.firstLineNumDbCache) {
							for (int j = this.dbCacheList.size() - 1; j > this.fetchScaleBorder; j--) {
								this.dbCacheList.remove(j);
							}
							insertIndex = 0;
							this.lastLineNumDbCache = this.firstLineNumDbCache + this.dbCacheList.size();
						} else {
							for (int j = 0; j < this.dbCacheList.size() - this.fetchScaleBorder; j++) {
								this.dbCacheList.remove(j);
							}
							insertIndex = this.dbCacheList.size();
							this.firstLineNumDbCache = this.lastLineNumDbCache - this.dbCacheList.size();
						}
					}
					this.dbCacheList.addAll(insertIndex, tmp);
					if (insertIndex == 0 && this.firstLineNumDbCache != this.lastLineNumDbCache) {
						this.firstLineNumDbCache = fetchFrom;
					} else {
						this.lastLineNumDbCache += tmp.size();
					}
					if (!this.isScaled && this.dbResultSize > this.fetchScale) {
						this.isScaled = true;
						if (this.removeAll) {
							this.removedList.clear();
						}
					}
				}
			} else {
				this.dbResultSize = 0;
			}
			return dataList;
		} catch (final SQLException e) {
			throw new LtmLtRtException(e);
		} catch (final ClosedLtRtException e) {
			throw new LtmLtRtException(e);
		} catch (final ObjectPoolLtException e) {
			throw new LtmLtRtException(e);
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		}
	}

	private int calcFetchFrom(final int lineNumber) {
		int result = this.absoluteFirstLineNum;
		if (this.dbResultSize > this.fetchScale) {
			if (lineNumber < this.firstLineNumDbCache) {
				if (lineNumber + this.fetchScale >= this.firstLineNumDbCache) {
					result = this.firstLineNumDbCache - this.fetchScale;
				} else {
					result = lineNumber - this.fetchScaleBorder;
				}
			} else { // lineNumber > lastMapLineNum
				if (lineNumber - this.fetchScaleBorder < this.lastLineNumDbCache) {
					result = this.lastLineNumDbCache;
				} else {
					result = lineNumber - this.fetchScaleBorder;
				}
			}
			if (result < this.absoluteFirstLineNum) {
				result = this.absoluteFirstLineNum;
			}
			if (result >= this.dbResultSize + this.absoluteFirstLineNum) {
				result = -1;
			}
		}
		return result;
	}

	/**
	 *
	 * @return
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close <br/>
	 *                          +Cause ObjectPoolLtException if can't instantiate a
	 *                          new connection <br/>
	 *                          +Cause InterruptedException if interrupted while
	 *                          waiting for lock or waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection
	 */
	public boolean isScaled() throws LtmLtRtException {
		try {
			this.dbAccess.lock();
			initSet();
			return this.isScaled;
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * Returns the number of elements in this set (its cardinality). If this set
	 * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
	 * <tt>Integer.MAX_VALUE</tt>.
	 *
	 * @return the number of elements in this set (its cardinality)
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close <br/>
	 *                          +Cause ObjectPoolLtException if can't instantiate a
	 *                          new connection <br/>
	 *                          +Cause InterruptedException if interrupted while
	 *                          waiting for lock or waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection
	 */
	@Override
	public int size() throws LtmLtRtException {
		try {
			this.dbAccess.lock();
			initSet();
			if (this.removeAll || this.isNew) {
				return this.addedList.size();
			} else {
				return Long.valueOf(this.dbResultSize).intValue() + this.addedList.size();
			}
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * Returns <tt>true</tt> if this set contains no elements.
	 *
	 * @return <tt>true</tt> if this set contains no elements
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close <br/>
	 *                          +Cause ObjectPoolLtException if can't instantiate a
	 *                          new connection <br/>
	 *                          +Cause InterruptedException if interrupted while
	 *                          waiting for lock or waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection
	 */
	@Override
	public boolean isEmpty() throws LtmLtRtException {
		return size() == 0;
	}

	/**
	 * Returns <tt>true</tt> if this set contains the specified element. More
	 * formally, returns <tt>true</tt> if and only if this set contains an element
	 * <tt>e</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
	 *
	 * @param o element whose presence in this set is to be tested
	 * @return <tt>true</tt> if this set contains the specified element
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close <br/>
	 *                          +Cause ObjectPoolLtException if can't instantiate a
	 *                          new connection <br/>
	 *                          +Cause InterruptedException if interrupted while
	 *                          waiting for lock or waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection
	 */
	@Override
	public boolean contains(final Object o) throws LtmLtRtException {
		try {
			this.dbAccess.lock();
			initSet();
			if (o == null) {
				return false;
			}
			boolean result = this.addedList.contains(o);
			if (!this.removeAll && !this.isNew) {
				if (this.removedList.contains(o)) {
					return false;
				}
				if (!result) {
					result = this.dbCacheList.contains(o);
				}
				if (!result && this.isScaled && LtmObjectModelling.class.isInstance(o)) {
					if (!LtmObjectModelling.class.cast(o).isNew()) {
						try {
							result = this.elementTable.dbMappedHasId(this.mappedColumn, this.owner.getId(),
									LtmObjectModelling.class.cast(o).getId());
						} catch (final ClosedLtRtException e) {
							throw new LtmLtRtException(e);
						} catch (final ObjectPoolLtException e) {
							throw new LtmLtRtException(e);
						} catch (final InterruptedException e) {
							throw new LtmLtRtException(e);
						} catch (final SQLException e) {
							throw new LtmLtRtException(e);
						}
					}
				}
			}
			return result;
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * Returns an iterator over the elements in this set. The elements are returned
	 * in no particular order (unless this set is an instance of some class that
	 * provides a guarantee).
	 *
	 * @return an iterator over the elements in this set
	 * @throws IndexOutOfBoundsException if is scaled
	 * @throws LtmLtRtException          <br/>
	 *                                   +Cause ClosedLtRtException if long term
	 *                                   memory already close <br/>
	 *                                   +Cause ObjectPoolLtException if can't
	 *                                   instantiate a new connection <br/>
	 *                                   +Cause InterruptedException if interrupted
	 *                                   while waiting for lock or waiting for the
	 *                                   connection <br/>
	 *                                   +Cause SQLException if a database access
	 *                                   error occurs, or this method is called on a
	 *                                   closed connection
	 */
	@Override
	public Iterator<T> iterator() throws LtmLtRtException, IndexOutOfBoundsException {
		try {
			this.dbAccess.lock();
			initSet();
			if (this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			return new Itr();
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * Returns an array containing all of the elements in this set. If this set
	 * makes any guarantees as to what order its elements are returned by its
	 * iterator, this method must return the elements in the same order.
	 *
	 * <p>
	 * The returned array will be "safe" in that no references to it are maintained
	 * by this set. (In other words, this method must allocate a new array even if
	 * this set is backed by an array). The caller is thus free to modify the
	 * returned array.
	 *
	 * <p>
	 * This method acts as bridge between array-based and collection-based APIs.
	 *
	 * @return an array containing all the elements in this set
	 * @throws UnsupportedOperationException if the <tt>toArray</tt> operation is
	 *                                       not supported by this set
	 * @throws IndexOutOfBoundsException     if is scaled
	 * @throws LtmLtRtException              <br/>
	 *                                       +Cause ClosedLtRtException if long term
	 *                                       memory already close <br/>
	 *                                       +Cause ObjectPoolLtException if can't
	 *                                       instantiate a new connection <br/>
	 *                                       +Cause InterruptedException if
	 *                                       interrupted while waiting for lock or
	 *                                       waiting for the connection <br/>
	 *                                       +Cause SQLException if a database
	 *                                       access error occurs, or this method is
	 *                                       called on a closed connection
	 */
	@Override
	public Object[] toArray() throws LtmLtRtException, IndexOutOfBoundsException, UnsupportedOperationException {
		try {
			this.dbAccess.lock();
			initSet();
			if (this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			// TODO:
			throw new UnsupportedOperationException();
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * Returns an array containing all of the elements in this set; the runtime type
	 * of the returned array is that of the specified array. If the set fits in the
	 * specified array, it is returned therein. Otherwise, a new array is allocated
	 * with the runtime type of the specified array and the size of this set.
	 *
	 * <p>
	 * If this set fits in the specified array with room to spare (i.e., the array
	 * has more elements than this set), the element in the array immediately
	 * following the end of the set is set to <tt>null</tt>. (This is useful in
	 * determining the length of this set <i>only</i> if the caller knows that this
	 * set does not contain any null elements.)
	 *
	 * <p>
	 * If this set makes any guarantees as to what order its elements are returned
	 * by its iterator, this method must return the elements in the same order.
	 *
	 * <p>
	 * Like the {@link #toArray()} method, this method acts as bridge between
	 * array-based and collection-based APIs. Further, this method allows precise
	 * control over the runtime type of the output array, and may, under certain
	 * circumstances, be used to save allocation costs.
	 *
	 * <p>
	 * Suppose <tt>x</tt> is a set known to contain only strings. The following code
	 * can be used to dump the set into a newly allocated array of <tt>String</tt>:
	 *
	 * <pre>
	 * String[] y = x.toArray(new String[0]);
	 * </pre>
	 *
	 * Note that <tt>toArray(new Object[0])</tt> is identical in function to
	 * <tt>toArray()</tt>.
	 *
	 * @param a the array into which the elements of this set are to be stored, if
	 *          it is big enough; otherwise, a new array of the same runtime type is
	 *          allocated for this purpose.
	 * @return an array containing all the elements in this set
	 * @throws UnsupportedOperationException if the <tt>toArray</tt> operation is
	 *                                       not supported by this set
	 * @throws ArrayStoreException           if the runtime type of the specified
	 *                                       array is not a supertype of the runtime
	 *                                       type of every element in this set
	 * @throws NullPointerException          if the specified array is null
	 * @throws IndexOutOfBoundsException     if is scaled
	 * @throws LtmLtRtException              <br/>
	 *                                       +Cause ClosedLtRtException if long term
	 *                                       memory already close <br/>
	 *                                       +Cause ObjectPoolLtException if can't
	 *                                       instantiate a new connection <br/>
	 *                                       +Cause InterruptedException if
	 *                                       interrupted while waiting for lock or
	 *                                       waiting for the connection <br/>
	 *                                       +Cause SQLException if a database
	 *                                       access error occurs, or this method is
	 *                                       called on a closed connection
	 */
	@Override
	public <E> E[] toArray(final E[] a)
			throws LtmLtRtException, IndexOutOfBoundsException, UnsupportedOperationException {
		try {
			this.dbAccess.lock();
			initSet();
			if (this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			// TODO:
			throw new UnsupportedOperationException();
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * Adds the specified element to this set if it is not already present (optional
	 * operation). More formally, adds the specified element <tt>e</tt> to this set
	 * if the set contains no element <tt>e2</tt> such that
	 * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>. If this set
	 * already contains the element, the call leaves the set unchanged and returns
	 * <tt>false</tt>. In combination with the restriction on constructors, this
	 * ensures that sets never contain duplicate elements.
	 *
	 * <p>
	 * The stipulation above does not imply that sets must accept all elements; sets
	 * may refuse to add any particular element, including <tt>null</tt>, and throw
	 * an exception, as described in the specification for {@link Collection#add
	 * Collection.add}. Individual set implementations should clearly document any
	 * restrictions on the elements that they may contain.
	 *
	 * @param e element to be added to this set
	 * @return <tt>true</tt> if this set did not already contain the specified
	 *         element
	 * @throws ClassCastException       if the class of the specified element
	 *                                  prevents it from being added to this set
	 * @throws NullPointerException     if the specified element is null
	 * @throws IllegalArgumentException if some property of the specified element
	 *                                  prevents it from being added to this set
	 * @throws LtmLtRtException         <br/>
	 *                                  +Cause ClosedLtRtException if long term
	 *                                  memory already close <br/>
	 *                                  +Cause ObjectPoolLtException if can't
	 *                                  instantiate a new connection <br/>
	 *                                  +Cause InterruptedException if interrupted
	 *                                  while waiting for lock or waiting for the
	 *                                  connection <br/>
	 *                                  +Cause SQLException if a database access
	 *                                  error occurs, or this method is called on a
	 *                                  closed connection
	 */
	@Override
	public boolean add(final T e) throws LtmLtRtException {
		if (e == null) {
			throw new NullPointerException();
		}
		try {
			this.dbAccess.lock();
			initSet();
			this.modCount++;
			boolean result = false;
			if (!contains(e)) {
				if (!this.removeAll && !this.isNew) {
					if (this.removedIdList.remove(e.getId())) {
						this.removedList.remove(e);
					}
				}
				result = this.addedList.add(e);
				if (result) {
					objectChangeMappedId(e, this.owner);
				}
			}
			return result;
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * Removes the specified element from this set if it is present (optional
	 * operation). More formally, removes an element <tt>e</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>, if this set
	 * contains such an element. Returns <tt>true</tt> if this set contained the
	 * element (or equivalently, if this set changed as a result of the call). (This
	 * set will not contain the element once the call returns.)
	 *
	 * @param o object to be removed from this set, if present
	 * @return <tt>true</tt> if this set contained the specified element
	 * @throws ClassCastException   if the type of the specified element is
	 *                              incompatible with this set (optional)
	 * @throws NullPointerException if the specified element is null
	 * @throws LtmLtRtException     <br/>
	 *                              +Cause ClosedLtRtException if long term memory
	 *                              already close <br/>
	 *                              +Cause ObjectPoolLtException if can't
	 *                              instantiate a new connection <br/>
	 *                              +Cause InterruptedException if interrupted while
	 *                              waiting for lock or waiting for the connection
	 *                              <br/>
	 *                              +Cause SQLException if a database access error
	 *                              occurs, or this method is called on a closed
	 *                              connection
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(final Object o) throws LtmLtRtException {
		if (o == null) {
			throw new NullPointerException();
		}
		try {
			this.dbAccess.lock();
			initSet();
			this.modCount++;
			boolean result = false;
			if (contains(o)) {
				if (!this.removeAll && !this.isNew) {
					result = this.dbCacheList.remove(o);
				}
				if (result) {
					this.lastLineNumDbCache--;
					this.dbResultSize--;
					if (!((T) o).isNew()) {
						this.removedIdList.add(((T) o).getId());
						this.removedList.add((T) o);
					}
				} else {
					result = this.addedList.remove(o);
				}
				if (result) {
					objectChangeMappedId(o, null);
				}
			}
			return result;
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} finally {
			this.dbAccess.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	private void objectChangeMappedId(final Object o, final LtmObjectModelling link) {
		DATA_PROXY.getInvocationHandler((T) o).set(this.mappedColumn, link);
//		try {
//			//TODO: procurar todos os acessos do genero seguinte e substituir pelo acesso directo ao map
//			elementTable.getColumn(mappedColumn).getSetMethod().invoke(o, link);
//		} catch (IllegalArgumentException e) {
//			throw new ImplementationLtRtException(e);
//		} catch (IllegalAccessException e) {
//			throw new ImplementationLtRtException(e);
//		} catch (InvocationTargetException e) {
//			throw new ImplementationLtRtException(e);
//		}
	}

	/**
	 * Returns <tt>true</tt> if this set contains all of the elements of the
	 * specified collection. If the specified collection is also a set, this method
	 * returns <tt>true</tt> if it is a <i>subset</i> of this set.
	 *
	 * @param c collection to be checked for containment in this set
	 * @return <tt>true</tt> if this set contains all of the elements of the
	 *         specified collection
	 * @throws UnsupportedOperationException if the <tt>containsAll</tt> operation
	 *                                       is not supported by this set
	 * @throws ClassCastException            if the types of one or more elements in
	 *                                       the specified collection are
	 *                                       incompatible with this set (optional)
	 * @throws NullPointerException          if the specified collection contains
	 *                                       one or more null elements and this set
	 *                                       does not permit null elements
	 *                                       (optional), or if the specified
	 *                                       collection is null
	 * @throws IndexOutOfBoundsException     if is scaled
	 * @throws LtmLtRtException              <br/>
	 *                                       +Cause ClosedLtRtException if long term
	 *                                       memory already close <br/>
	 *                                       +Cause ObjectPoolLtException if can't
	 *                                       instantiate a new connection <br/>
	 *                                       +Cause InterruptedException if
	 *                                       interrupted while waiting for lock or
	 *                                       waiting for the connection <br/>
	 *                                       +Cause SQLException if a database
	 *                                       access error occurs, or this method is
	 *                                       called on a closed connection
	 * @see #contains(Object)
	 */
	@Override
	public boolean containsAll(final Collection<?> c)
			throws LtmLtRtException, IndexOutOfBoundsException, UnsupportedOperationException {
		try {
			this.dbAccess.lock();
			initSet();
			if (this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			// TODO:
			throw new UnsupportedOperationException();
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * Adds all of the elements in the specified collection to this set if they're
	 * not already present (optional operation). If the specified collection is also
	 * a set, the <tt>addAll</tt> operation effectively modifies this set so that
	 * its value is the <i>union</i> of the two sets. The behavior of this operation
	 * is undefined if the specified collection is modified while the operation is
	 * in progress.
	 *
	 * @param c collection containing elements to be added to this set
	 * @return <tt>true</tt> if this set changed as a result of the call
	 *
	 * @throws UnsupportedOperationException if the <tt>addAll</tt> operation is not
	 *                                       supported by this set
	 * @throws ClassCastException            if the class of an element of the
	 *                                       specified collection prevents it from
	 *                                       being added to this set
	 * @throws NullPointerException          if the specified collection contains
	 *                                       one or more null elements and this set
	 *                                       does not permit null elements, or if
	 *                                       the specified collection is null
	 * @throws IllegalArgumentException      if some property of an element of the
	 *                                       specified collection prevents it from
	 *                                       being added to this set
	 * @throws IndexOutOfBoundsException     if is scaled
	 * @throws LtmLtRtException              <br/>
	 *                                       +Cause ClosedLtRtException if long term
	 *                                       memory already close <br/>
	 *                                       +Cause ObjectPoolLtException if can't
	 *                                       instantiate a new connection <br/>
	 *                                       +Cause InterruptedException if
	 *                                       interrupted while waiting for lock or
	 *                                       waiting for the connection <br/>
	 *                                       +Cause SQLException if a database
	 *                                       access error occurs, or this method is
	 *                                       called on a closed connection
	 * @see #add(Object)
	 */
	@Override
	public boolean addAll(final Collection<? extends T> c)
			throws LtmLtRtException, IndexOutOfBoundsException, UnsupportedOperationException {
		try {
			this.dbAccess.lock();
			initSet();
			if (this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			this.modCount++;
			// TODO:
			throw new UnsupportedOperationException();
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * Removes from this set all of its elements that are contained in the specified
	 * collection (optional operation). If the specified collection is also a set,
	 * this operation effectively modifies this set so that its value is the
	 * <i>asymmetric set difference</i> of the two sets.
	 *
	 * @param c collection containing elements to be removed from this set
	 * @return <tt>true</tt> if this set changed as a result of the call
	 * @throws UnsupportedOperationException if the <tt>removeAll</tt> operation is
	 *                                       not supported by this set
	 * @throws ClassCastException            if the class of an element of this set
	 *                                       is incompatible with the specified
	 *                                       collection (optional)
	 * @throws NullPointerException          if this set contains a null element and
	 *                                       the specified collection does not
	 *                                       permit null elements (optional), or if
	 *                                       the specified collection is null
	 * @throws IndexOutOfBoundsException     if is scaled
	 * @throws LtmLtRtException              <br/>
	 *                                       +Cause ClosedLtRtException if long term
	 *                                       memory already close <br/>
	 *                                       +Cause ObjectPoolLtException if can't
	 *                                       instantiate a new connection <br/>
	 *                                       +Cause InterruptedException if
	 *                                       interrupted while waiting for lock or
	 *                                       waiting for the connection <br/>
	 *                                       +Cause SQLException if a database
	 *                                       access error occurs, or this method is
	 *                                       called on a closed connection
	 * @see #remove(Object)
	 * @see #contains(Object)
	 */
	@Override
	public boolean removeAll(final Collection<?> c)
			throws LtmLtRtException, IndexOutOfBoundsException, UnsupportedOperationException {
		try {
			this.dbAccess.lock();
			initSet();
			if (this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			this.modCount++;
			// TODO:
			throw new UnsupportedOperationException();
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * Retains only the elements in this set that are contained in the specified
	 * collection (optional operation). In other words, removes from this set all of
	 * its elements that are not contained in the specified collection. If the
	 * specified collection is also a set, this operation effectively modifies this
	 * set so that its value is the <i>intersection</i> of the two sets.
	 *
	 * @param c collection containing elements to be retained in this set
	 * @return <tt>true</tt> if this set changed as a result of the call
	 * @throws UnsupportedOperationException if the <tt>retainAll</tt> operation is
	 *                                       not supported by this set
	 * @throws ClassCastException            if the class of an element of this set
	 *                                       is incompatible with the specified
	 *                                       collection (optional)
	 * @throws NullPointerException          if this set contains a null element and
	 *                                       the specified collection does not
	 *                                       permit null elements (optional), or if
	 *                                       the specified collection is null
	 * @throws IndexOutOfBoundsException     if is scaled
	 * @throws LtmLtRtException              <br/>
	 *                                       +Cause ClosedLtRtException if long term
	 *                                       memory already close <br/>
	 *                                       +Cause ObjectPoolLtException if can't
	 *                                       instantiate a new connection <br/>
	 *                                       +Cause InterruptedException if
	 *                                       interrupted while waiting for lock or
	 *                                       waiting for the connection <br/>
	 *                                       +Cause SQLException if a database
	 *                                       access error occurs, or this method is
	 *                                       called on a closed connection
	 * @see #remove(Object)
	 */
	@Override
	public boolean retainAll(final Collection<?> c)
			throws LtmLtRtException, IndexOutOfBoundsException, UnsupportedOperationException {
		try {
			this.dbAccess.lock();
			initSet();
			if (this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			this.modCount++;
			// TODO:
			throw new UnsupportedOperationException();
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * Removes all of the elements from this set (optional operation). The set will
	 * be empty after this call returns.
	 *
	 * @throws IndexOutOfBoundsException if is scaled
	 * @throws LtmLtRtException          <br/>
	 *                                   +Cause ClosedLtRtException if long term
	 *                                   memory already close <br/>
	 *                                   +Cause ObjectPoolLtException if can't
	 *                                   instantiate a new connection <br/>
	 *                                   +Cause InterruptedException if interrupted
	 *                                   while waiting for lock or waiting for the
	 *                                   connection <br/>
	 *                                   +Cause SQLException if a database access
	 *                                   error occurs, or this method is called on a
	 *                                   closed connection
	 */
	@Override
	public void clear() throws LtmLtRtException, IndexOutOfBoundsException {
		try {
			this.dbAccess.lock();
			initSet();
			if (this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			this.modCount++;
			this.removeAll = true;
			for (final Object obj : this.dbCacheList) {
				objectChangeMappedId(obj, null);
			}
			this.removedList.addAll(this.dbCacheList);
			for (final Object obj : this.addedList) {
				objectChangeMappedId(obj, null);
			}
			this.addedList.clear();
			this.removedIdList.clear();
			this.dbResultSize = 0;
			this.firstLineNumDbCache = this.absoluteFirstLineNum;
			this.lastLineNumDbCache = this.absoluteFirstLineNum;
			this.dbCacheList.clear();
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * Nao garante a nao concurrent modification caso isScaled, ou seja, por exemplo
	 * ao pedir em sequencia o indece 0 e depois o 1 pode dar o mesmo objecto.
	 *
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws LtmLtRtException          <br/>
	 *                                   +Cause ClosedLtRtException if long term
	 *                                   memory already close <br/>
	 *                                   +Cause ObjectPoolLtException if can't
	 *                                   instantiate a new connection <br/>
	 *                                   +Cause InterruptedException if interrupted
	 *                                   while waiting for lock or waiting for the
	 *                                   connection <br/>
	 *                                   +Cause SQLException if a database access
	 *                                   error occurs, or this method is called on a
	 *                                   closed connection
	 */
	public T get(final int index) throws LtmLtRtException, IndexOutOfBoundsException {
		try {
			this.dbAccess.lock();
			initSet();
			T result;
			if (this.removeAll) {
				result = this.addedList.get(index);
			} else {
				if (index < 0 || index >= size()) {
					throw new IndexOutOfBoundsException();
				}
				if (index < this.dbResultSize) {
					if (this.isScaled) {
						dbCacheFetchFor(index + this.absoluteFirstLineNum);
					}
					try {
						result = this.dbCacheList.get(index + this.absoluteFirstLineNum - this.firstLineNumDbCache);
					} catch (final IndexOutOfBoundsException e) {
						// IndexOutOfBoundsException, when a fetch for the new scale identifies a
						// smaller dbSize then expected
						if (index >= size()) {
							throw new IndexOutOfBoundsException();
						} else {
							result = this.addedList.get(index - Long.valueOf(this.dbResultSize).intValue());
						}
					}
				} else {
					result = this.addedList.get(index - Long.valueOf(this.dbResultSize).intValue());
				}
			}
			return result;
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 *
	 * @param index
	 * @param buff
	 * @param buffPos
	 * @param length
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws LtmLtRtException          <br/>
	 *                                   +Cause ClosedLtRtException if long term
	 *                                   memory already close <br/>
	 *                                   +Cause ObjectPoolLtException if can't
	 *                                   instantiate a new connection <br/>
	 *                                   +Cause InterruptedException if interrupted
	 *                                   while waiting for lock or waiting for the
	 *                                   connection <br/>
	 *                                   +Cause SQLException if a database access
	 *                                   error occurs, or this method is called on a
	 *                                   closed connection
	 */
	public int get(final int index, final T[] buff, final int buffPos, final int length)
			throws LtmLtRtException, IndexOutOfBoundsException {
		try {
			this.dbAccess.lock();
			initSet();
			if (buffPos < 0 || buffPos + length > buff.length || index < 0 || index >= size()) {
				throw new IndexOutOfBoundsException();
			}
			int result = 0;
			try {
				for (int i = index; i < index + length; i++) {
					buff[buffPos + result] = get(i);
					result++;
				}
			} catch (final IndexOutOfBoundsException e) {
				LOG.debug("#0", e);
			}
			return result;
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 *
	 * @param index
	 * @param buff
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws LtmLtRtException          <br/>
	 *                                   +Cause ClosedLtRtException if long term
	 *                                   memory already close <br/>
	 *                                   +Cause ObjectPoolLtException if can't
	 *                                   instantiate a new connection <br/>
	 *                                   +Cause InterruptedException if interrupted
	 *                                   while waiting for lock or waiting for the
	 *                                   connection <br/>
	 *                                   +Cause SQLException if a database access
	 *                                   error occurs, or this method is called on a
	 *                                   closed connection
	 */
	public int get(final int index, final T[] buff) throws LtmLtRtException, IndexOutOfBoundsException {
		return get(index, buff, 0, buff.length);
	}

	/**
	 * 
	 * @throws IndexOutOfBoundsException if is not scaled
	 * @throws LtmLtRtException          <br/>
	 *                                   +Cause ClosedLtRtException if long term
	 *                                   memory already close <br/>
	 *                                   +Cause ObjectPoolLtException if can't
	 *                                   instantiate a new connection <br/>
	 *                                   +Cause InterruptedException if interrupted
	 *                                   while waiting for lock or waiting for the
	 *                                   connection <br/>
	 *                                   +Cause SQLException if a database access
	 *                                   error occurs, or this method is called on a
	 *                                   closed connection
	 */
	public void saveScaled() throws LtmLtRtException, IndexOutOfBoundsException {
		try {
			this.dbAccess.lock();
			initSet();
			if (!this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			if (this.owner.isNew()) {
				throw new ImplementationLtRtException();
			}
			this.modCount++;
			final ConnectionDB conn = LTM_MANAGER.getConnection();
			boolean execute = false;
			try {
				if (this.removeAll) {
					this.elementTable.removeMappedIdStep(conn, this.mappedColumn, this.owner.getId());
					execute = true;
				} else {
					if (!this.removedIdList.isEmpty()) {
						this.elementTable.updateMappedIdStep(conn, this.mappedColumn, null, this.removedIdList);
						execute = true;
					}
				}
				if (!this.addedList.isEmpty()) {
					final List<Long> tmp = new ArrayList<>(this.addedList.size());
					for (final T o : this.addedList) {
						if (!o.isNew()) {
							tmp.add(o.getId());
						}
					}
					this.elementTable.updateMappedIdStep(conn, this.mappedColumn, this.owner.getId(), tmp);
					execute = true;
				}
				if (execute) {
					conn.executeParameterizedBatchGroup();
					conn.release();
					reset();
				} else {
					conn.releaseRollback();
				}
			} catch (final SQLException e) {
				try {
					conn.releaseRollback();
				} catch (final SQLException e1) {
					LOG.debug("#0", e1);
				}
				throw new LtmLtRtException(e);
			}
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} catch (final ClosedLtRtException ex) {
			throw new LtmLtRtException(ex);
		} catch (final ObjectPoolLtException ex) {
			throw new LtmLtRtException(ex);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * 
	 * @throws IndexOutOfBoundsException if is not scaled
	 * @throws LtmLtRtException          <br/>
	 *                                   +Cause ClosedLtRtException if long term
	 *                                   memory already close <br/>
	 *                                   +Cause ObjectPoolLtException if can't
	 *                                   instantiate a new connection <br/>
	 *                                   +Cause InterruptedException if interrupted
	 *                                   while waiting for lock or waiting for the
	 *                                   connection <br/>
	 *                                   +Cause SQLException if a database access
	 *                                   error occurs, or this method is called on a
	 *                                   closed connection
	 */
	public void removeScaled() throws LtmLtRtException, IndexOutOfBoundsException {
		try {
			this.dbAccess.lock();
			initSet();
			if (!this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			if (this.owner.isNew()) {
				throw new ImplementationLtRtException();
			}
			this.modCount++;
			final ConnectionDB conn = LTM_MANAGER.getConnection();
			try {
				this.elementTable.removeMappedIdStep(conn, this.mappedColumn, this.owner.getId());
				conn.executeParameterizedBatchGroup();
				conn.release();
				reset();
			} catch (final SQLException e) {
				try {
					conn.releaseRollback();
				} catch (final SQLException e1) {
					LOG.debug("#0", e1);
				}
				throw new LtmLtRtException(e);
			}
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} catch (final ClosedLtRtException ex) {
			throw new LtmLtRtException(ex);
		} catch (final ObjectPoolLtException ex) {
			throw new LtmLtRtException(ex);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 * 
	 * @throws IndexOutOfBoundsException if is not scaled
	 * @throws LtmLtRtException          <br/>
	 *                                   +Cause ClosedLtRtException if long term
	 *                                   memory already close <br/>
	 *                                   +Cause ObjectPoolLtException if can't
	 *                                   instantiate a new connection <br/>
	 *                                   +Cause InterruptedException if interrupted
	 *                                   while waiting for lock or waiting for the
	 *                                   connection <br/>
	 *                                   +Cause SQLException if a database access
	 *                                   error occurs, or this method is called on a
	 *                                   closed connection
	 */
	public void refreshScaled() throws LtmLtRtException, IndexOutOfBoundsException {
		try {
			this.dbAccess.lock();
			initSet();
			if (!this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			if (this.owner.isNew()) {
				throw new ImplementationLtRtException();
			}
			this.modCount++;
			reset();
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/**
	 *
	 * @param conn
	 * @param persistListLoop
	 * @param updateListLoop
	 * @param changeList
	 * @throws IndexOutOfBoundsException if is scaled
	 * @throws LtmLtRtException          <br/>
	 *                                   +Cause ClosedLtRtException if long term
	 *                                   memory already close <br/>
	 *                                   +Cause ObjectPoolLtException if can't
	 *                                   instantiate a new connection <br/>
	 *                                   +Cause InterruptedException if interrupted
	 *                                   while waiting for lock or waiting for the
	 *                                   connection <br/>
	 *                                   +Cause SQLException if a database access
	 *                                   error occurs, or this method is called on a
	 *                                   closed connection
	 */
	void save(final ConnectionDB conn, final List<Object> persistListLoop, final List<Object> updateListLoop,
			final List<Object> changeList) throws LtmLtRtException, IndexOutOfBoundsException {
		try {
			this.dbAccess.lock();
			initSet();
			if (this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			this.modCount++;
			final int detectChange = changeList.size();
			if (persistListLoop != null && !persistListLoop.contains(this)) {
				persistListLoop.add(this);
				for (final T obj : this.addedList) {
					DATA_PROXY.getInvocationHandler(obj).save(conn, persistListLoop, updateListLoop, changeList);
				}
				if (this.isNew || changeList.size() != detectChange) {
					changeList.add(this);
					this.saveChangedOnPersist = true;
				} else {
					this.saveChangedOnPersist = false;
				}
			}
			if (updateListLoop != null && !updateListLoop.contains(this)) {
				updateListLoop.add(this);
				for (final T removed : this.removedList) {
					DATA_PROXY.getInvocationHandler(removed).save(conn, persistListLoop, updateListLoop, changeList);
				}
				for (final T obj : this.addedList) {
					DATA_PROXY.getInvocationHandler(obj).save(conn, persistListLoop, updateListLoop, changeList);
				}
				if (changeList.size() != detectChange && !this.saveChangedOnPersist) {
					changeList.add(this);
				}
				if (changeList.size() == detectChange && !this.saveChangedOnPersist) {
					this.dbAccess.unlock();
					this.dbAccess.unlock();
				}
			}
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} catch (final IllegalArgumentException ex) {
			throw new ImplementationLtRtException(ex);
		} catch (final SQLException ex) {
			throw new LtmLtRtException(ex);
		}
	}

	void saveFail() {
		this.dbAccess.unlock();
		this.dbAccess.unlock();
	}

	void saveSuccess() {
		reset();
		this.dbAccess.unlock();
		this.dbAccess.unlock();
	}

	/**
	 *
	 * @param conn
	 * @param listLoop
	 * @param changeList
	 * @throws IndexOutOfBoundsException if is scaled
	 * @throws LtmLtRtException          <br/>
	 *                                   +Cause ClosedLtRtException if long term
	 *                                   memory already close <br/>
	 *                                   +Cause ObjectPoolLtException if can't
	 *                                   instantiate a new connection <br/>
	 *                                   +Cause InterruptedException if interrupted
	 *                                   while waiting for lock or waiting for the
	 *                                   connection <br/>
	 *                                   +Cause SQLException if a database access
	 *                                   error occurs, or this method is called on a
	 *                                   closed connection
	 */
	void remove(final ConnectionDB conn, final List<Object> listLoop, final List<Object> changeList)
			throws LtmLtRtException, IndexOutOfBoundsException {
		try {
			this.dbAccess.lock();
			initSet();
			if (this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			this.modCount++;
			if (!listLoop.contains(this)) {
				listLoop.add(this);
				changeList.add(this);
				if (!this.removedIdList.isEmpty()) {
					this.elementTable.updateMappedIdStep(conn, this.mappedColumn, null, this.removedIdList);
				}
				for (final T removed : this.dbCacheList) {
					DATA_PROXY.getInvocationHandler(removed).remove(conn, listLoop, changeList);
				}
				for (final T obj : this.addedList) {
					DATA_PROXY.getInvocationHandler(obj).remove(conn, listLoop, changeList);
				}
			}
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} catch (final IllegalArgumentException ex) {
			throw new ImplementationLtRtException(ex);
		} catch (final SQLException ex) {
			throw new LtmLtRtException(ex);
		}
	}

	void removeFail() {
		this.dbAccess.unlock();
	}

	void removeSuccess() {
		reset();
		this.dbAccess.unlock();
	}

	/**
	 *
	 * @param listLoop
	 * @throws IndexOutOfBoundsException if is scaled
	 * @throws LtmLtRtException          <br/>
	 *                                   +Cause ClosedLtRtException if long term
	 *                                   memory already close <br/>
	 *                                   +Cause ObjectPoolLtException if can't
	 *                                   instantiate a new connection <br/>
	 *                                   +Cause InterruptedException if interrupted
	 *                                   while waiting for lock or waiting for the
	 *                                   connection <br/>
	 *                                   +Cause SQLException if a database access
	 *                                   error occurs, or this method is called on a
	 *                                   closed connection
	 */
	void refresh(final List<Object> listLoop) throws LtmLtRtException, IndexOutOfBoundsException {
		try {
			this.dbAccess.lock();
			initSet();
			if (this.isScaled) {
				throw new IndexOutOfBoundsException();
			}
			this.modCount++;
			if (!listLoop.contains(this)) {
				listLoop.add(this);
				if (!this.isNew) {
					reset();
					final List<Map<String, Object>> dataList = dbCacheFetchFor(this.firstLineNumDbCache);
					if (this.isScaled) {
						throw new IndexOutOfBoundsException();
					}
					for (int i = 0; i < dataList.size(); i++) {
						DATA_PROXY.getInvocationHandler(this.dbCacheList.get(i)).refresh(listLoop, dataList.get(i));
					}
				} else {
					for (final T obj : this.addedList) {
						DATA_PROXY.getInvocationHandler(obj).refresh(listLoop);
					}
				}
			}
		} catch (final InterruptedException ex) {
			throw new LtmLtRtException(ex);
		} catch (final IllegalArgumentException ex) {
			throw new ImplementationLtRtException(ex);
		} finally {
			this.dbAccess.unlock();
		}
	}

	/*
	 * ITR
	 */
	private final class Itr implements Iterator<T> {

		private int cursor;
		private T lastRetrieve;
		private int expectedModCount;
		private final int staleSize;

		private Itr() {
			this.cursor = 0;
			this.lastRetrieve = null;
			this.expectedModCount = ScaledSet.this.modCount;
			this.staleSize = size();
		}

		@Override
		public boolean hasNext() {
			return this.cursor != this.staleSize;
		}

		@Override
		public T next() {
			validateExpectedModCount();
			try {
				final T result = get(this.cursor++);
				this.lastRetrieve = result;
				return result;
			} catch (final IndexOutOfBoundsException e) {
				validateExpectedModCount();
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			if (this.lastRetrieve == null) {
				throw new ImplementationLtRtException();
			}
			validateExpectedModCount();
			ScaledSet.this.remove(this.lastRetrieve);
			this.cursor--;
			this.lastRetrieve = null;
			this.expectedModCount = ScaledSet.this.modCount;
		}

		final void validateExpectedModCount() {
			if (ScaledSet.this.modCount != this.expectedModCount) {
				throw new ConcurrentModificationException();
			}
		}

	}
}

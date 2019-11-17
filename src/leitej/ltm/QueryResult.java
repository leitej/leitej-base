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

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leitej.exception.ClosedLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.log.Logger;
import leitej.ltm.exception.LtmLtRtException;

/**
 *
 *
 * @author Julio Leite
 */
public final class QueryResult<T extends LtmObjectModelling> {

	private static final Logger LOG = Logger.getInstance();
	private static final DataProxy DATA_PROXY = DataProxy.getInstance();
	private static final AbstractLongTermMemory LTM_MANAGER = LongTermMemory.getInstance();

	private static final int DEFAULT_FETCH_SCALE = 50;

	private final ElementTable elementTable;
	private final String countQuery;
	private final String selectQuery;
	private final ElementColumn[] ecParams;
	private final int[] types;
	private final boolean hasArray;
	private final int managerArrayTypes;
	private Object[] params;

	private long dbResultSize;
	private int fetchScale;
	private int fetchScaleBorder;
	private final int absoluteFirstLineNum;
	private int firstLineNumDbCache;
	private int lastLineNumDbCache; // exclusive
	private final List<T> dbCacheList;

	QueryResult(final ElementTable elementTable, final String countQuery, final String selectQuery,
			final ElementColumn[] ecParams) {
		this.elementTable = elementTable;
		this.countQuery = countQuery;
		this.selectQuery = selectQuery;
		this.ecParams = ecParams;
		this.types = new int[ecParams.length];
		this.managerArrayTypes = LTM_MANAGER.arrayTypes();
		boolean hasArray = false;
		for (int i = 0; i < this.types.length; i++) {
			this.types[i] = ecParams[i].getSqlType();
			if (this.types[i] == this.managerArrayTypes && !hasArray) {
				hasArray = true;
			}
		}
		this.hasArray = hasArray;
		this.dbResultSize = -1;
		this.setFetchScale(DEFAULT_FETCH_SCALE);
		this.absoluteFirstLineNum = LTM_MANAGER.absoluteNumberOfFirstRow();
		this.firstLineNumDbCache = this.absoluteFirstLineNum;
		this.lastLineNumDbCache = this.absoluteFirstLineNum;
		this.dbCacheList = new ArrayList<>();
		LOG.debug("#0", selectQuery);
	}

	/**
	 *
	 * @param lineNumber
	 * @throws ClosedLtRtException   if long term memory already close
	 * @throws ObjectPoolLtException if can't instantiate a new connection
	 * @throws InterruptedException  if interrupted while waiting for the connection
	 * @throws SQLException          if a database access error occurs, or this
	 *                               method is called on a closed connection or the
	 *                               SQL statement does not return a ResultSet
	 *                               object
	 */
	@SuppressWarnings("unchecked")
	private void dbFetchFor(final int lineNumber)
			throws ClosedLtRtException, ObjectPoolLtException, InterruptedException, SQLException {
		if (lineNumber < this.firstLineNumDbCache || lineNumber >= this.lastLineNumDbCache) {
			final int fetchFrom = calcFetchFrom(lineNumber);
			if (fetchFrom != -1) {
				final List<Map<String, Object>> dataList = dbFetch(fetchFrom);
				if (dataList.size() == this.fetchScale
						|| (dataList.size() == 0 && fetchFrom > this.absoluteFirstLineNum)) {
					this.dbResultSize = -1;
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
			}
		}
	}

	/**
	 *
	 * @param fetchFrom
	 * @return
	 * @throws ClosedLtRtException   if long term memory already close
	 * @throws ObjectPoolLtException if can't instantiate a new connection
	 * @throws InterruptedException  if interrupted while waiting for the connection
	 * @throws SQLException          if a database access error occurs, or this
	 *                               method is called on a closed connection or the
	 *                               SQL statement does not return a ResultSet
	 *                               object
	 */
	private List<Map<String, Object>> dbFetch(final Integer fetchFrom)
			throws ClosedLtRtException, ObjectPoolLtException, InterruptedException, SQLException {
		final List<Map<String, Object>> result = new ArrayList<>();
		final int dataColumnsLength = this.elementTable.getSelectableColumns().length;
		final ConnectionDB conn = LTM_MANAGER.getConnection();
		try {
			conn.initParameterizedSelect(this.selectQuery);
			Object[] sqlParams;
			List<Array> arrayToFree = null;
			if (this.hasArray) {
				arrayToFree = new ArrayList<>();
				sqlParams = new Object[this.params.length];
				Array tmp;
				for (int i = 0; i < this.types.length; i++) {
					if (this.types[i] == this.managerArrayTypes) {
						tmp = LTM_MANAGER.createArraySQL(this.ecParams[i], (Object[]) this.params[i]);
						sqlParams[i] = tmp;
						if (tmp != null) {
							arrayToFree.add(tmp);
						}
					} else {
						sqlParams[i] = this.params[i];
					}
				}
			} else {
				sqlParams = this.params;
			}
			final ResultSet rs = conn.executeParameterizedSelect(LTM_MANAGER.isParameterizedLimitArgsBeforeColumns(),
					fetchFrom, this.fetchScale, this.types, sqlParams);
			if (this.hasArray && arrayToFree != null) {
				for (final Array array : arrayToFree) {
					array.free();
				}
			}
			Map<String, Object> data;
			while (rs.next()) {
				data = new HashMap<>(dataColumnsLength);
				Object tmp;
				Array arrayTmp;
				for (final ElementColumn ec : this.elementTable.getSelectableColumns()) {
					tmp = rs.getObject(ec.getSqlName());
					if (Array.class.isInstance(tmp)) {
						arrayTmp = (Array) tmp;
						data.put(ec.getJavaName(), SqlParser.parseArray(ec.getReturnType(), arrayTmp));
						arrayTmp.free();
					} else {
						data.put(ec.getJavaName(), tmp);
					}
				}
				result.add(data);
			}
		} finally {
			if (conn != null) {
				conn.releaseRollback();
			}
		}
		return result;
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

	public synchronized void refresh() {
		this.dbResultSize = -1;
		this.firstLineNumDbCache = this.absoluteFirstLineNum;
		this.lastLineNumDbCache = this.absoluteFirstLineNum;
		this.dbCacheList.clear();
	}

	public synchronized void releaseResult() {
		refresh();
	}

	/**
	 *
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException if <code>index</code> is negative or
	 *                                   greater or equal to the result size
	 * @throws LtmLtRtException          <br/>
	 *                                   +Cause ClosedLtRtException if long term
	 *                                   memory already close <br/>
	 *                                   +Cause ObjectPoolLtException if can't
	 *                                   instantiate a new connection <br/>
	 *                                   +Cause InterruptedException if interrupted
	 *                                   while waiting for the connection <br/>
	 *                                   +Cause SQLException if a database access
	 *                                   error occurs, or this method is called on a
	 *                                   closed connection
	 */
	public synchronized T get(final int index) throws LtmLtRtException, IndexOutOfBoundsException {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException();
		}
		try {
			dbFetchFor(index + this.absoluteFirstLineNum);
		} catch (final SQLException e) {
			throw new LtmLtRtException(e);
		} catch (final ClosedLtRtException e) {
			throw new LtmLtRtException(e);
		} catch (final ObjectPoolLtException e) {
			throw new LtmLtRtException(e);
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		}
		return this.dbCacheList.get(index + this.absoluteFirstLineNum - this.firstLineNumDbCache);
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
	 *                                   while waiting for the connection <br/>
	 *                                   +Cause SQLException if a database access
	 *                                   error occurs, or this method is called on a
	 *                                   closed connection
	 */
	public synchronized int get(final int index, final T[] buff, final int buffPos, final int length)
			throws LtmLtRtException, IndexOutOfBoundsException {
		if (buffPos < 0 || buffPos + length > buff.length || index < 0) {
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
	}

	/**
	 *
	 * @param index
	 * @param buff
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
	public int get(final int index, final T[] buff) throws LtmLtRtException {
		return get(index, buff, 0, buff.length);
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
	 *                          waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection
	 */
	public synchronized long size() throws LtmLtRtException {
		if (this.dbResultSize < 0) {
			try {
				Object result = null;
				final ConnectionDB conn = LTM_MANAGER.getConnection();
				try {
					conn.initParameterizedSelect(this.countQuery);
					final ResultSet rs = conn.executeParameterizedSelect(this.types, this.params);
					if (rs.next()) {
						result = rs.getObject(1);
					}
					this.dbResultSize = LTM_MANAGER.castCountResult(result);
				} finally {
					if (conn != null) {
						conn.releaseRollback();
					}
				}
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
		return this.dbResultSize;
	}

	public synchronized int getFetchScale() {
		return this.fetchScale;
	}

	public synchronized void setFetchScale(final int fetchScale) {
		this.fetchScale = fetchScale;
		this.fetchScaleBorder = calcFetchScaleBorder(fetchScale);
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

	public synchronized void setParams(final Object... params) {
		if (this.params == null) {
			this.params = new Object[params.length];
		} else if (this.params.length != params.length) {
			throw new ImplementationLtRtException();
		}
		System.arraycopy(params, 0, this.params, 0, params.length);
		releaseResult();
	}
}

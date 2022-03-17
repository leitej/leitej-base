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
import leitej.log.Logger;

/**
 * @author Julio Leite
 *
 */
public class LtmSetIterator<E> implements Iterator<E> {

	private static final Logger LOG = Logger.getInstance();

	private static final DataMemoryPool MEM_POOL = DataMemoryPool.getInstance();

	private final DataProxyHandler dataPH;
	private final DataMemoryType dataType;
	private final Class<?> dataClass;
	private final String pStt;
	private long positionID;
	private Object next;
	private boolean hasNext;

	LtmSetIterator(final DataProxyHandler dataPH, final DataMemoryType dataType, final Class<?> dataClass,
			final String tablename) {
		this.dataPH = dataPH;
		this.dataType = dataType;
		this.dataClass = dataClass;
		this.pStt = HsqldbUtil.getStatementSetIterator(tablename, dataPH.getLtmId());
		LOG.trace("pStt: #0", this.pStt);
		this.positionID = -1;
		this.hasNext = true;
	}

	private void update() {
		if (this.hasNext && this.next == null) {
			DataMemoryConnection conn = null;
			try {
				try {
					conn = MEM_POOL.poll();
					conn.prepareNext(this);
				} finally {
					if (conn != null) {
						MEM_POOL.offer(conn);
					}
				}
			} catch (ObjectPoolLtException | InterruptedException | SQLException e) {
				throw new LtmLtRtException(e);
			}
			if (this.next == null) {
				this.hasNext = false;
			}
		}
	}

	@Override
	public synchronized boolean hasNext() {
		update();
		this.dataPH.isValid();
		return this.hasNext;
	}

	@Override
	public synchronized E next() {
		update();
		if (!this.hasNext) {
			throw new NoSuchElementException();
		}
		@SuppressWarnings("unchecked")
		final E result = (E) this.next;
		this.next = null;
		this.dataPH.isValid();
		return result;
	}

	String getpStt() {
		return this.pStt;
	}

	long getPositionID() {
		return this.positionID;
	}

	void setPositionID(final long positionID) {
		this.positionID = positionID;
	}

	DataMemoryType getDataType() {
		return this.dataType;
	}

	Class<?> getDataClass() {
		return this.dataClass;
	}

	void setNext(final Object next) {
		this.next = next;
	}

}

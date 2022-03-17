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

import java.io.Closeable;
import java.sql.SQLException;

import leitej.Constant;
import leitej.exception.ClosedLtRtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.LtmLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.exception.SeppukuLtRtException;
import leitej.log.Logger;
import leitej.util.DateUtil;
import leitej.util.data.BigBinary;
import leitej.util.stream.RandomAccessBinary;

/**
 * @author Julio Leite
 *
 */
public final class LargeMemory extends BigBinary implements RandomAccessBinary, Closeable {

	private static final Logger LOG = Logger.getInstance();

	private static final DataMemoryPool MEM_POOL = DataMemoryPool.getInstance();

	static {
		LargeMemoryTracker.intialize();
	}

	static boolean eraseAll() {
		final boolean result = BigBinary.clean(Constant.LTM_STREAM_DIR);
		if (!result) {
			throw new SeppukuLtRtException(
					new IllegalStateLtRtException("FAIL to delete all large memory: #0", Constant.LTM_STREAM_DIR));
		}
		LOG.warn("eraseAll: #0", Constant.LTM_STREAM_DIR);
		LargeMemoryTracker.intialize();
		return result;
	}

	public LargeMemory() throws LtmLtRtException {
		super(Constant.LTM_STREAM_DIR, DateUtil.generateUniqueNumberPerJVM());
		LOG.debug("#0", this);
		DataMemoryConnection conn = null;
		try {
			try {
				conn = MEM_POOL.poll();
				conn.newLargeMemory(this);
			} finally {
				if (conn != null) {
					MEM_POOL.offer(conn);
				}
			}
		} catch (ClosedLtRtException | IllegalArgumentException | InterruptedException | ObjectPoolLtException
				| SQLException e) {
			throw new LtmLtRtException(e);
		}
	}

	LargeMemory(final long id) {
		super(Constant.LTM_STREAM_DIR, id);
	}

	@Override
	public String toString() {
		return super.toString() + "-ID" + this.getId();
	}

}

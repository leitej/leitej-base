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
import java.util.HashMap;
import java.util.Map;

import leitej.exception.ClosedLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.util.data.AbstractDataProxy;

/**
 *
 * @author Julio Leite
 */
final class DataProxy extends AbstractDataProxy<LtmObjectModelling, DataProxyHandler> {

	private static final long serialVersionUID = -752068042212420771L;

	private static final DataProxy INSTANCE = new DataProxy();

	static DataProxy getInstance() {
		return INSTANCE;
	}

	private DataProxy() {
		super(Serializable.class, Comparable.class);
	}

	/**
	 *
	 * @param eTable
	 * @param id
	 * @return
	 * @throws ClosedLtRtException   if long term memory already close
	 * @throws ObjectPoolLtException if can't instantiate a new connection
	 * @throws InterruptedException  if interrupted while waiting for the connection
	 * @throws SQLException          if a database access error occurs, or this
	 *                               method is called on a closed connection
	 */
	<T extends LtmObjectModelling> T getTableInstance(final ElementTable eTable, final Long id)
			throws ClosedLtRtException, ObjectPoolLtException, InterruptedException, SQLException {
		return getTableInstance(eTable, id, null);
	}

	/**
	 *
	 * @param eTable
	 * @param id
	 * @param data
	 * @return
	 * @throws ClosedLtRtException   if long term memory already close
	 * @throws ObjectPoolLtException if can't instantiate a new connection
	 * @throws InterruptedException  if interrupted while waiting for the connection
	 * @throws SQLException          if a database access error occurs, or this
	 *                               method is called on a closed connection
	 */
	@SuppressWarnings("unchecked")
	<T extends LtmObjectModelling> T getTableInstance(final ElementTable eTable, final Long id,
			Map<String, Object> data)
			throws ClosedLtRtException, ObjectPoolLtException, InterruptedException, SQLException {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		T result = eTable.getCache().get(id);
		if (result == null) {
			if (data == null) {
				data = eTable.dbFetchData(id);
			}
			if (data != null) {
				final DataProxyHandler handler = new DataProxyHandler(eTable, id, data);
				result = (T) newProxyInstance(eTable.getClassTable(), handler);
				eTable.getCache().put((T) result);
			}
		}
		return result;
	}

	<T extends LtmObjectModelling> T newTableInstance(final ElementTable eTable) {
		final DataProxyHandler handler = new DataProxyHandler(eTable, null,
				new HashMap<String, Object>(eTable.getSelectableColumns().length));
		@SuppressWarnings("unchecked")
		final T result = (T) newProxyInstance(eTable.getClassTable(), handler);
		return result;
	}

}

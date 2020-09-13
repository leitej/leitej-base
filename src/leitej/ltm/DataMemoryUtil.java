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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import leitej.util.data.Cache;
import leitej.util.data.CacheWeak;

/**
 * @author Julio Leite
 *
 */
final class DataMemoryUtil {

	private static final Cache<Long, LargeMemory> LM_CACHE = new CacheWeak<>();

	private static synchronized LargeMemory getLargeMemory(final Long id) {
		LargeMemory result = null;
		if (id != null) {
			result = LM_CACHE.get(id);
			if (result == null) {
				result = new LargeMemory(id);
				LM_CACHE.set(id, result);
			}
		}
		return result;
	}

	static synchronized void setLargeMemory(final LargeMemory largeMemory) {
		LM_CACHE.set(largeMemory.getId(), largeMemory);
	}

	@SuppressWarnings("unchecked")
	static void map(final ResultSet rSet, final PreparedClass preparedClass, final DataProxyHandler dph,
			final Map<String, Object> proxyData) throws SQLException {
		final List<String> columnList = preparedClass.getColumns();
		final List<DataMemoryType> columnTypeList = preparedClass.getColumnsTypes();
		String key;
		Object value;
		proxyData.put(DataProxyHandler.ID,
				HsqldbUtil.parseValue(rSet, DataProxyHandler.ID, DataMemoryType.LONG.getSqlType()));
		for (int i = 0; i < columnList.size(); i++) {
			key = columnList.get(i);
			value = HsqldbUtil.parseValue(rSet, key, columnTypeList.get(i).getSqlType());
			if (value != null) {
				if (DataMemoryType.ENUM.equals(columnTypeList.get(i))) {
					value = Enum.valueOf(dph.getType(key).asSubclass(Enum.class), String.class.cast(value));
				} else if (DataMemoryType.LARGE_MEMORY.equals(columnTypeList.get(i))) {
					value = getLargeMemory(Long.class.cast(value));
				}
				proxyData.put(key, value);
			}
		}
		// TODO tabelas intermedias -set-map
	}

}

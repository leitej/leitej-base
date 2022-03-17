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

import leitej.log.Logger;
import leitej.util.AgnosticUtil;
import leitej.util.StringUtil;
import leitej.util.data.Cache;
import leitej.util.data.CacheWeak;
import leitej.util.machine.VMMonitor;

/**
 * @author Julio Leite
 *
 */
final class DataMemoryUtil {

	private static final Logger LOG = Logger.getInstance();

	private static final LongTermMemory LTM = LongTermMemory.getInstance();
	private static final Cache<Long, LargeMemory> LM_CACHE = new CacheWeak<>();

	static <T extends LtmObjectModelling> String genRemark(final Class<T> ltmInterface) {
		return genRemark(ltmInterface, null, null, null);
	}

	static <T extends LtmObjectModelling> String genRemark(final Class<T> ltmInterface, final String datanameSet,
			final DataMemoryType setDataMemoryType, final Class<?> setDataClass) {
		return ltmInterface.getName() + ";" + ((datanameSet == null) ? "" : datanameSet) + ";"
				+ ((setDataMemoryType == null) ? "" : setDataMemoryType) + ";"
				+ ((setDataClass == null) ? "" : setDataClass.getName());
	}

	static String getLtmClassName(final String remark) {
		return remark.split(";")[0];
	}

	@SuppressWarnings("unchecked")
	static <T extends LtmObjectModelling> Class<T> getLtmClass(final String remark) throws ClassNotFoundException {
		return (Class<T>) AgnosticUtil.getClass(getLtmClassName(remark));
	}

	static String getColumnSetDataName(final String remark) {
		String result = null;
		if (remark != null) {
			final String[] split = remark.split(";");
			if (split.length > 1 && !StringUtil.isNullOrEmpty(split[1])) {
				result = split[1];
			}
		}
		return result;
	}

	static DataMemoryType getColumnSetDataMemoryType(final String remark) {
		DataMemoryType result = null;
		if (remark != null) {
			final String[] split = remark.split(";");
			if (split.length > 2 && !StringUtil.isNullOrEmpty(split[2])) {
				result = DataMemoryType.valueOf(split[2]);
			}
		}
		return result;
	}

	static Class<?> getColumnSetParameterClass(final String remark) throws ClassNotFoundException {
		Class<?> result = null;
		if (remark != null) {
			final String[] split = remark.split(";");
			if (split.length > 3 && !StringUtil.isNullOrEmpty(split[3])) {
				result = AgnosticUtil.getClass(split[3]);
			}
		}
		return result;
	}

	static boolean isToEraseTable(final String remark) {
		boolean isErase = false;
		try {
			if (remark != null) {
				LOG.debug("check - ltmInterface: #0", remark);
				// check if class exists
				final Class<?> ltmClass = getLtmClass(remark);
				final String datanameSet = getColumnSetDataName(remark);
				if (DataMemoryPool.CONFIG.isAutoForgetsInterfaceComponentMisses() && datanameSet != null) {
					// check if the field SET still exists
					isErase = !AgnosticUtil.classContains(ltmClass, datanameSet);
				}
			}
		} catch (final ClassNotFoundException e) {
			LOG.warn("#0", e.getMessage());
			final String eraseArg = "-LTM.Erase=" + remark;
			if (VMMonitor.javaArguments().contains(eraseArg)) {
				isErase = true;
			} else {
				LOG.warn("To erase data from #0 set jvm argument: #1", remark, eraseArg);
			}
		}
		return isErase;
	}

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

	static synchronized void cacheLargeMemory(final LargeMemory largeMemory) {
		LM_CACHE.set(largeMemory.getId(), largeMemory);
	}

	static String genColumnName(final String dataName, final Class<?> returnClass) {
		return dataName + "_" + returnClass.getName().replaceAll("[^A-Za-z0-9]", "_");
	}

	static boolean isColumnTypeLargeMemory(final String columnName) {
		return columnName.endsWith(LargeMemory.class.getName().replaceAll("[^A-Za-z0-9]", "_"));
	}

	@SuppressWarnings("unchecked")
	static void map(final ResultSet rSet, final PreparedClass preparedClass, final DataProxyHandler dph,
			final Map<String, Object> proxyData) throws SQLException {
		final List<String> dataNameList = preparedClass.getDataNameList();
		final List<String> columnNameList = preparedClass.getColumnNameList();
		final List<DataMemoryType> columnTypeList = preparedClass.getColumnTypeList();
		String dataName;
		String columnName;
		Object value;
		proxyData.put(DataProxyHandler.LTM_ID,
				HsqldbUtil.parseValue(rSet, DataProxyHandler.LTM_ID, DataMemoryType.LONG.getSqlType()));
		for (int i = 0; i < dataNameList.size(); i++) {
			dataName = dataNameList.get(i);
			columnName = columnNameList.get(i);
			value = HsqldbUtil.parseValue(rSet, columnName, columnTypeList.get(i).getSqlType());
			if (value != null) {
				if (DataMemoryType.ENUM.equals(columnTypeList.get(i))) {
					value = Enum.valueOf(dph.getReturnClass(dataName).asSubclass(Enum.class), String.class.cast(value));
				} else if (DataMemoryType.LARGE_MEMORY.equals(columnTypeList.get(i))) {
					value = getLargeMemory(Long.class.cast(value));
				}
				proxyData.put(dataName, value);
			}
		}
	}

	@SuppressWarnings("unchecked")
	static void mapSetValue(final ResultSet rSet, final LtmSetIterator<?> it) throws SQLException {
		it.setPositionID(rSet.getLong(DataProxyHandler.SET_ID));
		final Object value = HsqldbUtil.parseValue(rSet, DataProxyHandler.SET_VALUE, it.getDataType().getSqlType());
		if (it.getDataType().equals(DataMemoryType.LONG_TERM_MEMORY)) {
			it.setNext(LTM.fetch(it.getDataClass().asSubclass(LtmObjectModelling.class), Long.class.cast(value)));
		} else if (it.getDataType().equals(DataMemoryType.LARGE_MEMORY)) {
			it.setNext(getLargeMemory(Long.class.cast(value)));
		} else if (it.getDataType().equals(DataMemoryType.ENUM)) {
			it.setNext(Enum.valueOf(it.getClass().asSubclass(Enum.class), String.class.cast(value)));
		} else {
			it.setNext(value);
		}
	}

}

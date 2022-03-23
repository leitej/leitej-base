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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import leitej.log.Logger;

/**
 * @author Julio Leite
 *
 */
final class PreparedClass {

	private static final Logger LOG = Logger.getInstance();

	private final Class<LtmObjectModelling> interfaceClass;
	private final String tablename;
	private final List<String> dataNameList;
	private final List<String> columnNameList;
	private final List<DataMemoryType> columnTypeList;
	private final Map<String, Class<?>> columnMapLongTermMemory;
	private final List<String> columnsSetTablename;
	private final List<String> columnsSet;
	private final List<DataMemoryType> columnsSetTypes;
	private final List<Class<?>> columnsSetClass;
	private final List<String> deleteOnSetByLtmId;
	private final String selectById;
	private final String insertNewRow;
	private final List<String> updateColumnById;
	private final String deleteById;

	PreparedClass(final DataProxyHandler dph) throws ClassNotFoundException {
		// TODO indexes - dph.getIndexes(dataname)
		this.interfaceClass = dph.getInterface();
		this.tablename = HsqldbUtil.getTablename(this.interfaceClass);
		LOG.debug("interfaceClass: #0, tablename: #1", this.interfaceClass, this.tablename);
		final List<String> datanameList = dph.getDatanameList();
		this.dataNameList = new ArrayList<>();
		this.columnNameList = new ArrayList<>();
		this.columnTypeList = new ArrayList<>();
		this.columnMapLongTermMemory = new HashMap<>();
		this.updateColumnById = new ArrayList<>();
		this.columnsSet = new ArrayList<>();
		this.columnsSetTablename = new ArrayList<>();
		this.columnsSetTypes = new ArrayList<>();
		this.columnsSetClass = new ArrayList<>();
		this.deleteOnSetByLtmId = new ArrayList<>();
		// parse data names and data types
		Class<?> dataType;
		DataMemoryType dataSqlType;
		String columnExtendTableName;
		for (final String dataname : datanameList) {
			dataType = dph.getReturnClass(dataname);
			if (!DataProxyHandler.LTM_ID.equals(dataname)) {
				if (Set.class.isAssignableFrom(dataType)) {
					this.columnsSet.add(dataname);
					columnExtendTableName = HsqldbUtil.getTablenameSet(this.interfaceClass, dataname);
					this.columnsSetTablename.add(columnExtendTableName);
					this.columnsSetTypes.add(HsqldbUtil.getColumnSetDataMemoryType(columnExtendTableName));
					this.columnsSetClass.add(HsqldbUtil.getColumnSetParameterClass(columnExtendTableName));
					this.deleteOnSetByLtmId.add(HsqldbUtil.getStatementSetClearByLtmId(columnExtendTableName));
					LOG.trace("column Set: #0, tablename: #1", dataname, columnExtendTableName);
				} else {
					dataSqlType = DataMemoryType.getDataMemoryType(dataType);
					this.dataNameList.add(dataname);
					final String columnName = DataMemoryUtil.genColumnName(dataname, dph.getReturnClass(dataname));
					this.columnNameList.add(columnName);
					this.columnTypeList.add(dataSqlType);
					if (LtmObjectModelling.class.isAssignableFrom(dataType)) {
						this.columnMapLongTermMemory.put(dataname, dataType);
					}
					LOG.trace("column: #0, dataSqlType: #1", columnName, dataSqlType);
					this.updateColumnById.add(HsqldbUtil.getStatementUpdateColumnById(this.tablename, columnName));
				}
			}
		}
		// prepare queries
		LOG.debug("updateColumnById: #0", this.updateColumnById);
		this.selectById = HsqldbUtil.getStatementSelectById(this.tablename);
		LOG.debug("selectById: #0", this.selectById);
		this.insertNewRow = HsqldbUtil.getStatementInsertNewRow(this.tablename);
		LOG.debug("insertNewRow: #0", this.insertNewRow);
		this.deleteById = HsqldbUtil.getStatementDeleteById(this.tablename);
		LOG.debug("deleteById: #0", this.deleteById);
	}

	Class<LtmObjectModelling> getInterface() {
		return this.interfaceClass;
	}

	String getTablename() {
		return this.tablename;
	}

	List<String> getDataNameList() {
		return this.dataNameList;
	}

	List<String> getColumnNameList() {
		return this.columnNameList;
	}

	List<DataMemoryType> getColumnTypeList() {
		return this.columnTypeList;
	}

	@SuppressWarnings("unchecked")
	<T extends LtmObjectModelling> Class<T> getLongTermMemoryClass(final String dataname) {
		return (Class<T>) this.columnMapLongTermMemory.get(dataname);
	}

	List<String> getColumnsSet() {
		return this.columnsSet;
	}

	<T extends Object> void registerSetParameter(final String dataname, final T sample) {
		if (sample != null) {
			final int pos = this.columnsSet.indexOf(dataname);
			if (this.columnsSetTypes.get(pos) == null) {
				synchronized (this.columnsSetTypes) {
					this.columnsSetTypes.set(pos, DataMemoryType.getDataMemoryType(sample.getClass()));
					if (LtmObjectModelling.class.isInstance(sample)) {
						this.columnsSetClass.set(pos,
								LongTermMemory.getHandler(LtmObjectModelling.class.cast(sample)).getInterface());
					} else {
						this.columnsSetClass.set(pos, sample.getClass());
					}
				}
			}
		}
	}

	String getSetTablename(final String dataname) {
		return this.columnsSetTablename.get(this.columnsSet.indexOf(dataname));
	}

	DataMemoryType getColumnsSetType(final String dataname) {
		return this.columnsSetTypes.get(this.columnsSet.indexOf(dataname));
	}

	Class<?> getColumnsSetClass(final String dataname) {
		return this.columnsSetClass.get(this.columnsSet.indexOf(dataname));
	}

	/*
	 * prepared queries
	 */

	String getSelectById() {
		return this.selectById;
	}

	String getInsertNewRow() {
		return this.insertNewRow;
	}

	List<String> getUpdateColumnById() {
		return this.updateColumnById;
	}

	String getDeleteById() {
		return this.deleteById;
	}

	String getDeleteOnSetByLtmId(final String dataname) {
		return this.deleteOnSetByLtmId.get(this.columnsSet.indexOf(dataname));
	}

}

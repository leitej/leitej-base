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

import leitej.log.Logger;

/**
 * @author Julio Leite
 *
 */
final class PreparedClass {

	private static final Logger LOG = Logger.getInstance();

	private final Class<LtmObjectModelling> interfaceClass;
	private final String tablename;
	private final List<String> columns;
	private final List<DataMemoryType> columnsTypes;
	private final Map<String, Class<?>> columnMapLongTermMemory;
	private final List<String> columnsSet;
	private final List<DataMemoryType> columnsSetTypes;
	private final List<String> columnsMap;
	private final List<DataMemoryType> columnsMapKeyTypes;
	private final List<DataMemoryType> columnsMapValueTypes;
	private final String selectById;
	private final String insertNewRow;
	private final List<String> updateColumnById;
	private final String deleteById;

	PreparedClass(final DataProxyHandler dph) {
		this.interfaceClass = dph.getInterface();
		this.tablename = HsqldbUtil.getTablename(this.interfaceClass);
		final List<String> datanameList = dph.getDatanameList();
		this.columns = new ArrayList<>();
		this.columnsTypes = new ArrayList<>();
		this.columnMapLongTermMemory = new HashMap<>();
		this.updateColumnById = new ArrayList<>();
		this.columnsSet = new ArrayList<>();
		this.columnsSetTypes = new ArrayList<>();
		this.columnsMap = new ArrayList<>();
		this.columnsMapKeyTypes = new ArrayList<>();
		this.columnsMapValueTypes = new ArrayList<>();
		// parse data names and data types
		Class<?> dataType;
		DataMemoryType dataSqlType;
		for (final String dataname : datanameList) {
			dataType = dph.getType(dataname);
			dataSqlType = DataMemoryType.getDataMemoryType(dataType);
			if (!DataProxyHandler.ID.equals(dataname)) {
				this.columns.add(dataname);
				this.columnsTypes.add(dataSqlType);
				this.updateColumnById.add(HsqldbUtil.getStatementUpdateColumnById(this.tablename, dataname));
				if (LtmObjectModelling.class.isAssignableFrom(dataType)) {
					this.columnMapLongTermMemory.put(dataname, dataType);
				}
			}
			// TODO Set - Map
		}
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

	List<String> getColumns() {
		return this.columns;
	}

	List<DataMemoryType> getColumnsTypes() {
		return this.columnsTypes;
	}

	@SuppressWarnings("unchecked")
	<T extends LtmObjectModelling> Class<T> getLongTermMemoryClass(final String dataname) {
		return (Class<T>) this.columnMapLongTermMemory.get(dataname);
	}

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

}

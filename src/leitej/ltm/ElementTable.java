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
import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leitej.exception.ClosedLtRtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.log.Logger;
import leitej.ltm.annotation.LongTermMemory;
import leitej.ltm.dynamic.DescriptorData;
import leitej.ltm.dynamic.DescriptorField;
import leitej.ltm.dynamic.DynamicLtmObjectModel;
import leitej.util.AgnosticUtil;
import leitej.util.StringUtil;

/**
 *
 *
 * @author Julio Leite
 */
final class ElementTable implements Serializable {

	// TODO: do not allow more than one class with the same SQL name table, throw
	// exception

	private static final long serialVersionUID = 7045634018212393104L;

	private static final Logger LOG = Logger.getInstance();

	private static final AbstractLongTermMemory LTM_MANAGER = leitej.ltm.LongTermMemory.getInstance();
	private static final Map<String, ElementTable> INSTANCE_MAP = Collections
			.synchronizedMap(new HashMap<String, ElementTable>());
	private static final Map<Class<?>, ElementTable> INSTANCE_MAP_FOR_CLASS = Collections
			.synchronizedMap(new HashMap<Class<?>, ElementTable>());

	private static final int[] ID_TYPES = new int[] { LTM_MANAGER.longTypes() };
	private static final int[] MAPPED_ID_NOT_IDS_TYPES = new int[] { LTM_MANAGER.longTypes(),
			LTM_MANAGER.arrayTypes() };
	private static final int[] HAS_ID_AND_MAPPED_ID_TYPES = new int[] { LTM_MANAGER.longTypes(),
			LTM_MANAGER.longTypes() };
	private static final int[] UPDATE_MAPPED_ID_TO_IDS_TYPES = new int[] { LTM_MANAGER.longTypes(),
			LTM_MANAGER.arrayTypes() };

	/**
	 * Constructs the singleton instance represented by <code>data</code>.
	 *
	 * @param data
	 * @return a unique identifier to the singleton instance (that can be used when
	 *         calling 'getInstace')
	 * @throws IllegalArgumentLtRtException if the data in parameter represents a
	 *                                      configuration not allowed or already
	 *                                      registered data with the same name
	 * @throws ClosedLtRtException          if already close
	 * @throws ObjectPoolLtException        if can't instantiate a new connection
	 * @throws InterruptedException         if interrupted while waiting for a
	 *                                      connection
	 * @throws SQLException                 if a database access error occurs, this
	 *                                      method is called on a closed connection,
	 *                                      if one of the commands sent to the
	 *                                      database fails to execute properly or
	 *                                      attempts to return an unexpected result
	 */
	static <T extends LtmObjectModelling> String registry(final Class<T> data) throws IllegalArgumentLtRtException,
			SQLException, ClosedLtRtException, ObjectPoolLtException, InterruptedException {
		synchronized (data) {
			final String name = getFormattedName(data);
			if (INSTANCE_MAP.containsKey(name)) {
				throw new IllegalArgumentLtRtException();
			}
			INSTANCE_MAP.put(name, new ElementTable(data));
			return name;
		}
	}

	/**
	 *
	 * @param descriptorData
	 * @return a unique identifier to use when calling 'getInstace' to get the
	 *         singleton instance
	 * @throws IllegalArgumentLtRtException if the data in parameter represents a
	 *                                      configuration not allowed or already
	 *                                      registered data with the same name
	 * @throws ClosedLtRtException          if already close
	 * @throws ObjectPoolLtException        if can't instantiate a new connection
	 * @throws InterruptedException         if interrupted while waiting for a
	 *                                      connection
	 * @throws SQLException                 if a database access error occurs, this
	 *                                      method is called on a closed connection,
	 *                                      if one of the commands sent to the
	 *                                      database fails to execute properly or
	 *                                      attempts to return an unexpected result
	 */
	static String registry(final DescriptorData descriptorData) throws IllegalArgumentLtRtException, SQLException,
			ClosedLtRtException, ObjectPoolLtException, InterruptedException {
		synchronized (descriptorData) {
			final String name = getFormattedName(descriptorData);
			if (StringUtil.isNullOrEmpty(name) || INSTANCE_MAP.containsKey(name)) {
				throw new IllegalArgumentLtRtException();
			}
			INSTANCE_MAP.put(name, new ElementTable(descriptorData));
			return name;
		}
	}

	/**
	 *
	 * @throws ClosedLtRtException   if already close
	 * @throws ObjectPoolLtException if can't instantiate a new connection
	 * @throws InterruptedException  if interrupted while waiting for the connection
	 * @throws SQLException          if a database access error occurs, this method
	 *                               is called on a closed connection, if one of the
	 *                               commands sent to the database fails to execute
	 *                               properly or attempts to return an unexpected
	 *                               result
	 */
	private static void checkTableDB(final ElementTable eTable)
			throws SQLException, ClosedLtRtException, ObjectPoolLtException, InterruptedException {
		// TODO: if already has the table check, add, remove column
		// TODO: if already has the table check, add, remove index
//		boolean created = false;
		final ConnectionDB conn = LTM_MANAGER.getConnection();
		try {
			if (!conn.hasTable(eTable.getSqlName())) {
				final String[] exec = LTM_MANAGER.generateCreateTableSQL(eTable);
				for (int i = 0; i < exec.length; i++) {
					conn.addBatch(exec[i]);
				}
				conn.executeBatch();
			}
			conn.release();
//			created = true;
		} catch (final SQLException e) {
			try {
				conn.releaseRollback();
			} catch (final SQLException e1) {
				LOG.debug("#0", e1);
			}
			throw e;
		}
//		if(created){
//			for(Class<LtmObjectModelling> classTable: eTable.getAllRelatedTableClasses()){
//				ElementTable.registry(classTable);
//			}
//		}
	}

	/**
	 * Gets the singleton instance represented by <code>classTable</code>.<br/>
	 * The respective singleton has to be registered before by calling 'registry()'
	 *
	 * @param classTable
	 * @return a singleton instance represented by <code>classTable</code>
	 * @throws IllegalArgumentLtRtException if try to get a ElementTable not
	 *                                      registered
	 */
	static <T extends LtmObjectModelling> ElementTable getInstance(final Class<T> classTable)
			throws IllegalArgumentLtRtException {
		final ElementTable result = INSTANCE_MAP_FOR_CLASS.get(classTable);
		if (result == null) {
			throw new IllegalArgumentLtRtException("#0", classTable.getName());
		}
		return result;
	}

	/**
	 * Gets the singleton instance represented by <code>table</code>.<br/>
	 * The respective singleton has to be registered before by calling 'registry()'
	 *
	 * @param table
	 * @return a singleton instance represented by <code>table</code>
	 * @throws IllegalArgumentLtRtException if try to get a ElementTable not
	 *                                      registered
	 */
	static <T extends LtmObjectModelling> ElementTable getInstance(final String table)
			throws IllegalArgumentLtRtException {
		final ElementTable result = INSTANCE_MAP.get(table);
		if (result == null) {
			throw new IllegalArgumentLtRtException();
		}
		return result;
	}

	static String getFormattedName(final Class<?> classTable) throws IllegalArgumentLtRtException {
		if (!directExtendsTableItf(classTable)) {
			throw new IllegalArgumentLtRtException();
		}
		final LongTermMemory tableAnnotation = classTable.getAnnotation(LongTermMemory.class);
		if (tableAnnotation == null) {
			throw new IllegalArgumentLtRtException();
		}
		final String sqlName = tableAnnotation.name();
		String result;
		if (!StringUtil.isNullOrEmpty(sqlName)) {
			result = sqlName;
		} else {
			result = classTable.getSimpleName();
		}
		return result.toUpperCase();
	}

	static String getFormattedName(final DescriptorData descriptorData) throws IllegalArgumentLtRtException {
		if (StringUtil.isNullOrEmpty(descriptorData.getName())) {
			throw new IllegalArgumentLtRtException();
		}
		return descriptorData.getName().toUpperCase();
	}

	private static <T extends LtmObjectModelling> boolean isSmallSize(final Class<T> classTable)
			throws IllegalArgumentLtRtException {
		final LongTermMemory tableAnnotation = classTable.getAnnotation(LongTermMemory.class);
		if (tableAnnotation == null || !directExtendsTableItf(classTable)) {
			throw new IllegalArgumentLtRtException();
		}
		return tableAnnotation.smallSize();
	}

	static boolean directExtendsTableItf(final Class<?> clazz) {
		if (!clazz.isInterface()) {
			return false;
		}
		final Class<?>[] tmp = clazz.getInterfaces();
		if (tmp.length == 1 && LtmObjectModelling.class.equals(tmp[0])) {
			return true;
		}
		return false;
	}

	private final ElementTableCache cache;

	private final Class<?> classTable;
	private final String sqlName;
	private final boolean smallSize;
	private final Map<String, ElementColumn> columnMap;
	private final ElementColumn[] columns;
	private final ElementColumn[] selectableColumnsName;
	private final ElementColumn[] insertableColumnsName;
	private final ElementColumn[] updatableColumnsName;
	private final ElementColumn columnId;

	private final String generatedParameterizedSelectIdSQL;
	private final String generateParameterizedInsertSQL;
	private final String generateParameterizedUpdateIdSQL;
	private final String generateParameterizedRemoveIdSQL;
	private final int[] generatedInsertableTypes;
	private final int[] generatedUpdatableTypes;
	private final Map<String, String> generatedParameterizedSelectLimitedMappedIdSQL;
	private final Map<String, String> generatedParameterizedSelectCountMappedSQL;
	private final Map<String, String> generateParameterizedSelectCountHasIdAndMappedIdSQL;
	private final Map<String, String> generateParameterizedRemoveMappedIdSQL;
	private final Map<String, String> generateParameterizedUpdateMappedIdListSQL;

	/**
	 *
	 * @param classTable
	 * @throws IllegalArgumentLtRtException if the data in parameter represents a
	 *                                      configuration not allowed
	 * @throws ClosedLtRtException          if already close
	 * @throws ObjectPoolLtException        if can't instantiate a new connection
	 * @throws InterruptedException         if interrupted while waiting for a
	 *                                      connection
	 * @throws SQLException                 if a database access error occurs, this
	 *                                      method is called on a closed connection,
	 *                                      if one of the commands sent to the
	 *                                      database fails to execute properly or
	 *                                      attempts to return an unexpected result
	 */
	private <T extends LtmObjectModelling> ElementTable(final Class<T> classTable) throws IllegalArgumentLtRtException,
			ClosedLtRtException, ObjectPoolLtException, SQLException, InterruptedException {
		this.cache = new ElementTableCache();
		this.classTable = classTable;
		// init sqlName
		this.sqlName = getFormattedName(classTable);
		this.smallSize = isSmallSize(classTable);
		ElementColumn columnId = null;
		final List<ElementColumn> columns = new ArrayList<>();
		final List<ElementColumn> selectableColumnsName = new ArrayList<>();
		final List<ElementColumn> insertableColumnsName = new ArrayList<>();
		final List<ElementColumn> updatableColumnsName = new ArrayList<>();
		// init columns
		for (final Method[] mGS : AgnosticUtil.getMethodsGetSet(classTable)) {
			columns.add(new ElementColumn(this, mGS[0], mGS[1]));
		}
		this.columns = columns.toArray(new ElementColumn[columns.size()]);
		this.columnMap = new HashMap<>(columns.size());
		for (final ElementColumn ec : this.columns) {
			if (ec.isId()) {
				if (columnId == null) {
					columnId = ec;
				} else {
					throw new ImplementationLtRtException();
				}
			}
			this.columnMap.put(ec.getJavaName(), ec);
			if (!ec.isMapped()) {
				selectableColumnsName.add(ec);
			}
			if (ec.isInsertable()) {
				insertableColumnsName.add(ec);
			}
			if (ec.isUpdatable()) {
				updatableColumnsName.add(ec);
			}
		}
		if (columnId == null) {
			throw new ImplementationLtRtException();
		}
		this.columnId = columnId;
		this.selectableColumnsName = selectableColumnsName.toArray(new ElementColumn[selectableColumnsName.size()]);
		this.insertableColumnsName = insertableColumnsName.toArray(new ElementColumn[insertableColumnsName.size()]);
		this.updatableColumnsName = updatableColumnsName.toArray(new ElementColumn[updatableColumnsName.size()]);
		//
		this.generatedParameterizedSelectIdSQL = LTM_MANAGER.generateParameterizedSelectIdSQL(this);
		this.generateParameterizedInsertSQL = LTM_MANAGER.generateParameterizedInsertSQL(this);
		this.generateParameterizedUpdateIdSQL = LTM_MANAGER.generateParameterizedUpdateIdSQL(this);
		this.generateParameterizedRemoveIdSQL = LTM_MANAGER.generateParameterizedRemoveIdSQL(this);
		this.generatedInsertableTypes = new int[this.insertableColumnsName.length];
		for (int i = 0; i < this.insertableColumnsName.length; i++) {
			this.generatedInsertableTypes[i] = this.insertableColumnsName[i].getSqlType();
		}
		this.generatedUpdatableTypes = new int[this.updatableColumnsName.length + 1];
		for (int i = 0; i < this.updatableColumnsName.length; i++) {
			this.generatedUpdatableTypes[i] = this.updatableColumnsName[i].getSqlType();
		}
		this.generatedUpdatableTypes[this.updatableColumnsName.length] = this.getColumnId().getSqlType();
		this.generatedParameterizedSelectLimitedMappedIdSQL = new HashMap<>();
		this.generatedParameterizedSelectCountMappedSQL = new HashMap<>();
		this.generateParameterizedSelectCountHasIdAndMappedIdSQL = new HashMap<>();
		this.generateParameterizedRemoveMappedIdSQL = new HashMap<>();
		this.generateParameterizedUpdateMappedIdListSQL = new HashMap<>();
		//
		INSTANCE_MAP_FOR_CLASS.put(classTable, this);
		checkTableDB(this);
	}

	private ElementTable(final DescriptorData descriptorData) throws IllegalArgumentLtRtException, ClosedLtRtException,
			ObjectPoolLtException, SQLException, InterruptedException {
		this.cache = new ElementTableCache();
		this.classTable = DynamicLtmObjectModel.class;
		// init sqlName
		this.sqlName = getFormattedName(descriptorData);
		if (descriptorData.getSmallSize() == null) {
			this.smallSize = LongTermMemory.DEFAULT_SMALL_SIZE;
		} else {
			this.smallSize = descriptorData.getSmallSize().booleanValue();
		}
		ElementColumn columnId = null;
		final List<ElementColumn> columns = new ArrayList<>();
		final List<ElementColumn> selectableColumnsName = new ArrayList<>();
		final List<ElementColumn> insertableColumnsName = new ArrayList<>();
		final List<ElementColumn> updatableColumnsName = new ArrayList<>();
		// init columns
		columns.add(new ElementColumn(this, LtmObjectModelling.GET_ID_RETURN_CLASS));
		if (descriptorData.getDescriptorFieldSet() != null) {
			for (final DescriptorField dField : descriptorData.getDescriptorFieldSet()) {
				columns.add(new ElementColumn(this, dField));
			}
		}
		this.columns = columns.toArray(new ElementColumn[columns.size()]);
		this.columnMap = new HashMap<>(columns.size());
		for (final ElementColumn ec : this.columns) {
			if (ec.isId()) {
				if (columnId == null) {
					columnId = ec;
				} else {
					throw new ImplementationLtRtException();
				}
			}
			this.columnMap.put(ec.getJavaName(), ec);
			if (!ec.isMapped()) {
				selectableColumnsName.add(ec);
			}
			if (ec.isInsertable()) {
				insertableColumnsName.add(ec);
			}
			if (ec.isUpdatable()) {
				updatableColumnsName.add(ec);
			}
		}
		if (columnId == null) {
			throw new ImplementationLtRtException();
		}
		this.columnId = columnId;
		this.selectableColumnsName = selectableColumnsName.toArray(new ElementColumn[selectableColumnsName.size()]);
		this.insertableColumnsName = insertableColumnsName.toArray(new ElementColumn[insertableColumnsName.size()]);
		this.updatableColumnsName = updatableColumnsName.toArray(new ElementColumn[updatableColumnsName.size()]);
		//
		this.generatedParameterizedSelectIdSQL = LTM_MANAGER.generateParameterizedSelectIdSQL(this);
		this.generateParameterizedInsertSQL = LTM_MANAGER.generateParameterizedInsertSQL(this);
		this.generateParameterizedUpdateIdSQL = LTM_MANAGER.generateParameterizedUpdateIdSQL(this);
		this.generateParameterizedRemoveIdSQL = LTM_MANAGER.generateParameterizedRemoveIdSQL(this);
		this.generatedInsertableTypes = new int[this.insertableColumnsName.length];
		for (int i = 0; i < this.insertableColumnsName.length; i++) {
			this.generatedInsertableTypes[i] = this.insertableColumnsName[i].getSqlType();
		}
		this.generatedUpdatableTypes = new int[this.updatableColumnsName.length + 1];
		for (int i = 0; i < this.updatableColumnsName.length; i++) {
			this.generatedUpdatableTypes[i] = this.updatableColumnsName[i].getSqlType();
		}
		this.generatedUpdatableTypes[this.updatableColumnsName.length] = this.getColumnId().getSqlType();
		this.generatedParameterizedSelectLimitedMappedIdSQL = new HashMap<>();
		this.generatedParameterizedSelectCountMappedSQL = new HashMap<>();
		this.generateParameterizedSelectCountHasIdAndMappedIdSQL = new HashMap<>();
		this.generateParameterizedRemoveMappedIdSQL = new HashMap<>();
		this.generateParameterizedUpdateMappedIdListSQL = new HashMap<>();
		//
		checkTableDB(this);
	}

	@SuppressWarnings("unchecked")
	<T extends LtmObjectModelling> Class<T> getClassTable() {
		return (Class<T>) this.classTable;
	}

	String getSqlName() {
		return this.sqlName;
	}

	String getJavaName() {
		return this.classTable.getName();
	}

	String getJavaSimpleName() {
		return this.classTable.getSimpleName();
	}

	ElementColumn[] getColumns() {
		return this.columns;
	}

	ElementTable[] getAllRelatedElementTable() {
		final List<ElementTable> result = new ArrayList<>();
		for (final ElementColumn c : getColumns()) {
			if (c.getRelatedElementTable() != null) {
				result.add(c.getRelatedElementTable());
			}
		}
		return (ElementTable[]) result.toArray(new ElementTable[result.size()]);
	}

	ElementColumn getColumn(final String name) {
		return this.columnMap.get(name);
	}

	String[] getAllColumnsName() {
		return this.columnMap.keySet().toArray(new String[this.columnMap.keySet().size()]);
	}

	boolean isSmallSize() {
		return this.smallSize;
	}

	ElementColumn[] getSelectableColumns() {
		return this.selectableColumnsName;
	}

	ElementColumn[] getInsertableColumns() {
		return this.insertableColumnsName;
	}

	ElementColumn[] getUpdatableColumns() {
		return this.updatableColumnsName;
	}

	ElementColumn getColumnId() {
		return this.columnId;
	}

	final ElementTableCache getCache() {
		return this.cache;
	}

	/*
	 *
	 */

	/**
	 *
	 * @param id
	 * @return
	 * @throws ClosedLtRtException   if long term memory already close
	 * @throws ObjectPoolLtException if can't instantiate a new connection
	 * @throws InterruptedException  if interrupted while waiting for the connection
	 * @throws SQLException          if a database access error occurs, or this
	 *                               method is called on a closed connection
	 */
	Map<String, Object> dbFetchData(final Long id)
			throws ClosedLtRtException, ObjectPoolLtException, InterruptedException, SQLException {
		Map<String, Object> result = null;
		if (id != null) {
			final ConnectionDB conn = LTM_MANAGER.getConnection();
			try {
				conn.initParameterizedSelect(this.generatedParameterizedSelectIdSQL);
				final ResultSet rs = conn.executeParameterizedSelect(ID_TYPES, id);
				if (rs.next()) {
					result = new HashMap<>(getSelectableColumns().length);
					Object tmp;
					Array arrayTmp;
					for (final ElementColumn ec : getSelectableColumns()) {
						tmp = rs.getObject(ec.getSqlName());
						if (Array.class.isInstance(tmp)) {
							arrayTmp = (Array) tmp;
							result.put(ec.getJavaName(), SqlParser.parseArray(ec.getReturnType(), arrayTmp));
							arrayTmp.free();
						} else {
							result.put(ec.getJavaName(), tmp);
						}
					}
				}
			} finally {
				if (conn != null) {
					conn.releaseRollback();
				}
			}
		}
		return result;
	}

	/**
	 *
	 * @param mappedColumn
	 * @param mappedId
	 * @param notIdList
	 * @param fetchFrom
	 * @param fetchScale
	 * @return
	 * @throws ClosedLtRtException   if long term memory already close
	 * @throws ObjectPoolLtException if can't instantiate a new connection
	 * @throws InterruptedException  if interrupted while waiting for the connection
	 * @throws SQLException          if a database access error occurs, or this
	 *                               method is called on a closed connection
	 */
	List<Map<String, Object>> dbFetchMappedData(final String mappedColumn, final Long mappedId,
			final List<Long> notIdList, final Integer fetchFrom, final Integer fetchScale)
			throws ClosedLtRtException, ObjectPoolLtException, InterruptedException, SQLException {
		final List<Map<String, Object>> result = new ArrayList<>();
		final int dataColumnsLength = getSelectableColumns().length;
		final ConnectionDB conn = LTM_MANAGER.getConnection();
		try {
			conn.initParameterizedSelect(getGeneratedParameterizedSelectLimitedMappedIdSQL(mappedColumn));
			final Array notIds = LTM_MANAGER.createArraySQL(getColumnId(),
					((notIdList != null) ? notIdList.toArray() : new Long[0]));
			// ATENCAO QUE A DB PODE LIMITAR O TAMANHO A RECEBER DO notIds - PQ VEM DE
			// 'WHERE ... IN (?)'
			final ResultSet rs = conn.executeParameterizedSelect(LTM_MANAGER.isParameterizedLimitArgsBeforeColumns(),
					fetchFrom, fetchScale, MAPPED_ID_NOT_IDS_TYPES, mappedId, notIds);
			notIds.free();
			Map<String, Object> data;
			while (rs.next()) {
				data = new HashMap<>(dataColumnsLength);
				Object tmp;
				Array arrayTmp;
				for (final ElementColumn ec : getSelectableColumns()) {
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

	private String getGeneratedParameterizedSelectLimitedMappedIdSQL(final String mappedColumn) {
		String result;
		synchronized (this.generatedParameterizedSelectLimitedMappedIdSQL) {
			result = this.generatedParameterizedSelectLimitedMappedIdSQL.get(mappedColumn);
			if (result == null) {
				result = LTM_MANAGER.generateParameterizedSelectLimitedMappedIdSQL(this, mappedColumn);
				this.generatedParameterizedSelectLimitedMappedIdSQL.put(mappedColumn, result);
			}
		}
		return result;
	}

	/**
	 *
	 * @param mappedColumn
	 * @param mappedId
	 * @param notIdList
	 * @return
	 * @throws ClosedLtRtException   if long term memory already close
	 * @throws ObjectPoolLtException if can't instantiate a new connection
	 * @throws InterruptedException  if interrupted while waiting for the connection
	 * @throws SQLException          if a database access error occurs, or this
	 *                               method is called on a closed connection
	 */
	long dbMappedDataSize(final String mappedColumn, final Long mappedId, final List<Long> notIdList)
			throws ClosedLtRtException, ObjectPoolLtException, InterruptedException, SQLException {
		Object result = null;
		final ConnectionDB conn = LTM_MANAGER.getConnection();
		try {
			conn.initParameterizedSelect(getGeneratedParameterizedSelectCountMappedSQL(mappedColumn));
			final Array notIds = LTM_MANAGER.createArraySQL(getColumnId(), notIdList.toArray());
			// ATENCAO QUE A DB PODE LIMITAR O TAMANHO A RECEBER DO notIds - PQ VEM DE
			// 'WHERE ... IN (?)'
			final ResultSet rs = conn.executeParameterizedSelect(MAPPED_ID_NOT_IDS_TYPES, mappedId, notIds);
			notIds.free();
			if (rs.next()) {
				result = rs.getObject(1);
			}
		} finally {
			if (conn != null) {
				conn.releaseRollback();
			}
		}
		return LTM_MANAGER.castCountResult(result);
	}

	private String getGeneratedParameterizedSelectCountMappedSQL(final String mappedColumn) {
		String result;
		synchronized (this.generatedParameterizedSelectCountMappedSQL) {
			result = this.generatedParameterizedSelectCountMappedSQL.get(mappedColumn);
			if (result == null) {
				result = LTM_MANAGER.generateParameterizedSelectCountMappedIdSQL(this, mappedColumn);
				this.generatedParameterizedSelectCountMappedSQL.put(mappedColumn, result);
			}
		}
		return result;
	}

	/**
	 *
	 * @param mappedColumn
	 * @param mappedId
	 * @param hasId
	 * @return
	 * @throws ClosedLtRtException   if long term memory already close
	 * @throws ObjectPoolLtException if can't instantiate a new connection
	 * @throws InterruptedException  if interrupted while waiting for the connection
	 * @throws SQLException          if a database access error occurs, or this
	 *                               method is called on a closed connection
	 */
	boolean dbMappedHasId(final String mappedColumn, final Long mappedId, final Long hasId)
			throws ClosedLtRtException, ObjectPoolLtException, InterruptedException, SQLException {
		Object result = null;
		final ConnectionDB conn = LTM_MANAGER.getConnection();
		try {
			conn.initParameterizedSelect(getGenerateParameterizedSelectCountHasIdAndMappedIdSQL(mappedColumn));
			final ResultSet rs = conn.executeParameterizedSelect(HAS_ID_AND_MAPPED_ID_TYPES, hasId, mappedId);
			if (rs.next()) {
				result = rs.getObject(1);
			}
		} finally {
			if (conn != null) {
				conn.releaseRollback();
			}
		}
		return LTM_MANAGER.castCountResult(result) > 0;
	}

	private String getGenerateParameterizedSelectCountHasIdAndMappedIdSQL(final String mappedColumn) {
		String result;
		synchronized (this.generateParameterizedSelectCountHasIdAndMappedIdSQL) {
			result = this.generateParameterizedSelectCountHasIdAndMappedIdSQL.get(mappedColumn);
			if (result == null) {
				result = LTM_MANAGER.generateParameterizedSelectCountHasIdAndMappedIdSQL(this, mappedColumn);
				this.generateParameterizedSelectCountHasIdAndMappedIdSQL.put(mappedColumn, result);
			}
		}
		return result;
	}

	/**
	 *
	 * @param conn
	 * @param data
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection
	 */
	void persistStep(final ConnectionDB conn, final Map<String, Object> data) throws SQLException {
		final ElementColumn[] columns = getInsertableColumns();
		final Object[] args = new Object[columns.length];
		List<Array> arrayToFree = null;
		Array tmp;
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].isArray()) {
				tmp = LTM_MANAGER.createArraySQL(columns[i], (Object[]) data.get(columns[i].getJavaName()));
				args[i] = tmp;
				if (arrayToFree == null) {
					arrayToFree = new ArrayList<>();
				}
				if (tmp != null) {
					arrayToFree.add(tmp);
				}
			} else {
				args[i] = data.get(columns[i].getJavaName());
			}
		}
		conn.addParameterizedBatchGroup(this.generateParameterizedInsertSQL, this.generatedInsertableTypes, args);
		if (arrayToFree != null) {
			for (final Array array : arrayToFree) {
				array.free();
			}
		}
	}

	/**
	 *
	 * @param conn
	 * @param data
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection
	 */
	void updateStep(final ConnectionDB conn, final Map<String, Object> data) throws SQLException {
		final ElementColumn[] columns = getUpdatableColumns();
		if (columns.length > 0) {
			final Object[] args = new Object[columns.length + 1];
			List<Array> arrayToFree = null;
			for (int i = 0; i < columns.length; i++) {
				if (columns[i].isArray()) {
					final Array tmp = LTM_MANAGER.createArraySQL(columns[i],
							(Object[]) data.get(columns[i].getJavaName()));
					args[i] = tmp;
					if (arrayToFree == null) {
						arrayToFree = new ArrayList<>();
					}
					if (tmp != null) {
						arrayToFree.add(tmp);
					}
				} else {
					args[i] = data.get(columns[i].getJavaName());
				}
			}
			args[columns.length] = data.get(getColumnId().getJavaName());
			conn.addParameterizedBatchGroup(this.generateParameterizedUpdateIdSQL, this.generatedUpdatableTypes, args);
			if (arrayToFree != null) {
				for (final Array array : arrayToFree) {
					array.free();
				}
			}
		}
	}

	/**
	 *
	 * @param conn
	 * @param mappedColumn
	 * @param mappedId
	 * @param ids
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection
	 */
	void updateMappedIdStep(final ConnectionDB conn, final String mappedColumn, final Long mappedId,
			final List<Long> ids) throws SQLException {
		final Array idsArray = LTM_MANAGER.createArraySQL(getColumn(mappedColumn), ids.toArray());
		conn.addParameterizedBatchGroup(getGenerateParameterizedUpdateMappedIdListSQL(mappedColumn),
				UPDATE_MAPPED_ID_TO_IDS_TYPES, mappedId, idsArray);
		idsArray.free();
	}

	private String getGenerateParameterizedUpdateMappedIdListSQL(final String mappedColumn) {
		String result;
		synchronized (this.generateParameterizedUpdateMappedIdListSQL) {
			result = this.generateParameterizedUpdateMappedIdListSQL.get(mappedColumn);
			if (result == null) {
				result = LTM_MANAGER.generateParameterizedUpdateMappedIdListSQL(this, mappedColumn);
				this.generateParameterizedUpdateMappedIdListSQL.put(mappedColumn, result);
			}
		}
		return result;
	}

	/**
	 *
	 * @param conn
	 * @param id
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection
	 */
	void removeStep(final ConnectionDB conn, final Long id) throws SQLException {
		conn.addParameterizedBatchGroup(this.generateParameterizedRemoveIdSQL, ID_TYPES, id);
	}

	/**
	 *
	 * @param conn
	 * @param mappedColumn
	 * @param mappedId
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection
	 */
	void removeMappedIdStep(final ConnectionDB conn, final String mappedColumn, final Long mappedId)
			throws SQLException {
		conn.addParameterizedBatchGroup(getGenerateParameterizedRemoveMappedIdSQL(mappedColumn), ID_TYPES, mappedId);
	}

	private String getGenerateParameterizedRemoveMappedIdSQL(final String mappedColumn) {
		String result;
		synchronized (this.generateParameterizedRemoveMappedIdSQL) {
			result = this.generateParameterizedRemoveMappedIdSQL.get(mappedColumn);
			if (result == null) {
				result = LTM_MANAGER.generateParameterizedRemoveMappedIdSQL(this, mappedColumn);
				this.generateParameterizedRemoveMappedIdSQL.put(mappedColumn, result);
			}
		}
		return result;
	}

}

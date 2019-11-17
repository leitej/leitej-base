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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Set;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.ltm.annotation.CascadeTypeEnum;
import leitej.ltm.annotation.Column;
import leitej.ltm.annotation.JoinColumn;
import leitej.ltm.annotation.ManyToOne;
import leitej.ltm.annotation.OneToMany;
import leitej.ltm.annotation.OneToOne;
import leitej.ltm.dynamic.DescriptorField;
import leitej.util.AgnosticUtil;
import leitej.util.StringUtil;

/**
 *
 *
 * @author Julio Leite
 */
final class ElementColumn implements Serializable {

	// TODO: predict the use of SQL keywords like 'group, select, cache, by, update,
	// table' (without case sensitive) for the name of columns and give an exception

	private static final long serialVersionUID = 8652830538539177813L;

	private static final AbstractLongTermMemory LTM_MANAGER = LongTermMemory.getInstance();

	static enum ElementColumnType {
		ID, LEAF, STREAM, ONE_TO_ONE, MANY_TO_ONE, ONE_TO_MANY
	}

	private static final Class<?>[] ACEPTED_LEAF_CLASSES = new Class<?>[] { Byte.class, Short.class, Integer.class,
			Long.class, Boolean.class, BigDecimal.class, Double.class, String.class };
	private static final Class<?>[] ACEPTED_ARRAY_COMPONENT_CLASSES = new Class<?>[] { Byte.class, Short.class,
			Integer.class, Long.class, Boolean.class, };

	private final ElementTable elementTable;

	private final ElementColumnType type;

	private final String sqlName;
	private final String javaName;
	private final boolean unique;
	private final boolean nullable;
	private final boolean insertable;
	private final boolean updatable;

	private final SequenceGeneratorEnum sequenceGenerator;

//	private final Class<?> relatedTableClass;
	private final String relatedTableName;

	private final Class<?> returnType;
	private final Integer sqlType; // java.sql.Types
	private final boolean array;
	private final int maxArrayLength;
	private final String columnDefinition;
	private final int length;
	private final int precision;
	private final int scale;

//	private final Class<?> listFromClassTable;
//	private final String mappedBy;
	private final String listFromTableName;
	private final String mappedByName;

	private final boolean isLink;
	private final int fetchScale; // number of rows to receive from DB which call
	private boolean cascadeAll;
	private boolean cascadeSave;
	private boolean cascadeRemove;
	private boolean cascadeRefresh;

	/**
	 *
	 * @param elementTable
	 * @param getMethod
	 * @param setMethod
	 * @throws IllegalArgumentLtRtException if the data type used for this column is
	 *                                      not allowed
	 */
	ElementColumn(final ElementTable elementTable, final Method getMethod, final Method setMethod)
			throws IllegalArgumentLtRtException {
		this.elementTable = elementTable;
		this.returnType = getMethod.getReturnType();
		this.array = this.returnType.isArray();
		if (!allowDataType(this.returnType)) {
			throw new IllegalArgumentLtRtException("#0 - #1", elementTable.getJavaSimpleName(), this.returnType);
		}
		// define type
		final OneToOne oneToOneAnnotation = getConfigAnnotation(OneToOne.class, getMethod, setMethod);
		final ManyToOne manyToOneAnnotation = getConfigAnnotation(ManyToOne.class, getMethod, setMethod);
		final OneToMany oneToManyAnnotation = getConfigAnnotation(OneToMany.class, getMethod, setMethod);
		if (getMethod.getName().equals(LtmObjectModelling.GET_ID_METHOD_NAME)) {
			if (!this.returnType.isAssignableFrom(LtmObjectModelling.GET_ID_RETURN_CLASS)) {
				throw new IllegalStateException();
			}
			this.type = ElementColumnType.ID;
		} else if (Set.class.isAssignableFrom(this.returnType)) {
			if (oneToManyAnnotation != null) {
				this.type = ElementColumnType.ONE_TO_MANY;
			} else {
				throw new IllegalStateException();
			}
		} else if (ElementTable.directExtendsTableItf(this.returnType)) {
			if (oneToOneAnnotation != null) {
				this.type = ElementColumnType.ONE_TO_ONE;
			} else {
				if (manyToOneAnnotation != null) {
					this.type = ElementColumnType.MANY_TO_ONE;
				} else {
					throw new IllegalStateException();
				}
			}
		} else if (LtmBinary.class.isAssignableFrom(this.returnType)) {
			this.type = ElementColumnType.STREAM;
		} else {
			this.type = ElementColumnType.LEAF;
		}
		// init element column
		final StringBuilder sb = new StringBuilder();
		String tmpDefinition = null;
		switch (this.type) {
		case ONE_TO_MANY:
			this.javaName = AgnosticUtil.writeDataGetterName(sb, getMethod).toString();
			this.sqlName = this.javaName.toUpperCase();
			// Without column
			this.unique = false;
			this.nullable = true;
			this.insertable = false;
			this.updatable = false;
			this.sequenceGenerator = null;
			this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
			this.columnDefinition = null;
			this.sqlType = null;
			this.length = 0;
			this.precision = 0;
			this.scale = 0;
			// setting map
			this.listFromTableName = ElementTable.getFormattedName(oneToManyAnnotation.mappedTableItf());
			this.relatedTableName = this.listFromTableName;
			this.mappedByName = oneToManyAnnotation.mappedBy();
//			this.fetch = oneToManyAnnotation.fetch();
			this.isLink = true;
			if (oneToManyAnnotation.fetchScale() < 1) {
				this.fetchScale = Integer.MAX_VALUE;
			} else {
				this.fetchScale = oneToManyAnnotation.fetchScale();
			}
			setCascade(oneToManyAnnotation.cascade());
			break;
		case ONE_TO_ONE:
			final JoinColumn otoJc = getConfigAnnotation(JoinColumn.class, getMethod, setMethod);
			final boolean isMapped = !StringUtil.isNullOrEmpty(oneToOneAnnotation.mappedBy());
			this.javaName = AgnosticUtil.writeDataGetterName(sb, getMethod).toString();
			this.sqlName = this.javaName.toUpperCase();
			if (otoJc != null) {
				if (isMapped) {
					throw new IllegalStateException();
				}
				this.unique = otoJc.unique();
				this.nullable = otoJc.nullable();
				this.insertable = otoJc.insertable();
				this.updatable = otoJc.updatable();
				this.sequenceGenerator = null;
			} else {
				if (isMapped) {
					// Without column
					this.unique = false;
					this.nullable = true;
					this.insertable = false;
					this.updatable = false;
					this.sequenceGenerator = null;
				} else {
					this.unique = JoinColumn.DEFAULT_UNIQUE;
					this.nullable = JoinColumn.DEFAULT_NULLABLE;
					this.insertable = JoinColumn.DEFAULT_INSERTABLE;
					this.updatable = JoinColumn.DEFAULT_UPDATABLE;
					this.sequenceGenerator = null;
				}
			}
			if (isMapped) {
				// Without column
				this.length = 0;
				this.precision = 0;
				this.scale = 0;
				this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
				this.columnDefinition = null;
				this.sqlType = null;
				this.mappedByName = oneToOneAnnotation.mappedBy();
			} else {
				this.length = ConstantLtm.ID_DEFAULT_LENGTH;
				this.precision = ConstantLtm.ID_DEFAULT_PRECISION;
				this.scale = ConstantLtm.ID_DEFAULT_SCALE;
				this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
				this.columnDefinition = getColumnDefinition(LtmObjectModelling.GET_ID_RETURN_CLASS, tmpDefinition,
						this.length, this.precision, this.scale);
				this.sqlType = getSqlType(LtmObjectModelling.GET_ID_RETURN_CLASS);
				this.mappedByName = null;
			}
			this.relatedTableName = ElementTable.getFormattedName(this.returnType);
			this.listFromTableName = null;
//			this.fetch = oneToOneAnnotation.fetch();
			this.isLink = true;
			this.fetchScale = 1;
			setCascade(oneToOneAnnotation.cascade());
			break;
		case MANY_TO_ONE:
			final JoinColumn jc = getConfigAnnotation(JoinColumn.class, getMethod, setMethod);
			this.javaName = AgnosticUtil.writeDataGetterName(sb, getMethod).toString();
			this.sqlName = this.javaName.toUpperCase();
			if (jc != null) {
				this.unique = jc.unique();
				this.nullable = jc.nullable();
				this.insertable = jc.insertable();
				this.updatable = jc.updatable();
				this.sequenceGenerator = null;
			} else {
				this.unique = JoinColumn.DEFAULT_UNIQUE;
				this.nullable = JoinColumn.DEFAULT_NULLABLE;
				this.insertable = JoinColumn.DEFAULT_INSERTABLE;
				this.updatable = JoinColumn.DEFAULT_UPDATABLE;
				this.sequenceGenerator = null;
			}
			this.length = ConstantLtm.ID_DEFAULT_LENGTH;
			this.precision = ConstantLtm.ID_DEFAULT_PRECISION;
			this.scale = ConstantLtm.ID_DEFAULT_SCALE;
			this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
			this.columnDefinition = getColumnDefinition(LtmObjectModelling.GET_ID_RETURN_CLASS, tmpDefinition,
					this.length, this.precision, this.scale);
			this.sqlType = getSqlType(LtmObjectModelling.GET_ID_RETURN_CLASS);
			this.relatedTableName = ElementTable.getFormattedName(this.returnType);
			this.listFromTableName = null;
			this.mappedByName = null;
//			this.fetch = manyToOneAnnotation.fetch();
			this.isLink = true;
			this.fetchScale = 1;
			setCascade(manyToOneAnnotation.cascade());
			break;
		case STREAM:
			// setting stream column
			this.javaName = AgnosticUtil.writeDataGetterName(sb, getMethod).toString();
			this.sqlName = this.javaName.toUpperCase();
			this.unique = ConstantLtm.STREAM_DEFAULT_UNIQUE;
			this.nullable = ConstantLtm.STREAM_DEFAULT_NULLABLE;
			this.insertable = ConstantLtm.STREAM_DEFAULT_INSERTABLE;
			this.updatable = ConstantLtm.STREAM_DEFAULT_UPDATABLE;
			this.sequenceGenerator = null;
			this.length = ConstantLtm.STREAM_DEFAULT_LENGTH;
			this.precision = ConstantLtm.STREAM_DEFAULT_PRECISION;
			this.scale = ConstantLtm.STREAM_DEFAULT_SCALE;
			this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
			this.columnDefinition = getColumnDefinition(LtmObjectModelling.GET_ID_RETURN_CLASS, tmpDefinition,
					this.length, this.precision, this.scale);
			this.sqlType = getSqlType(LtmObjectModelling.GET_ID_RETURN_CLASS);
			// without map
			this.relatedTableName = null;
			this.listFromTableName = null;
			this.mappedByName = null;
//			this.fetch = null;
			this.isLink = false;
			this.fetchScale = 0;
			this.cascadeAll = false;
			this.cascadeSave = false;
			this.cascadeRemove = false;
			this.cascadeRefresh = false;
			break;
		case LEAF:
			// setting column
			final Column c = getConfigAnnotation(Column.class, getMethod, setMethod);
			this.javaName = AgnosticUtil.writeDataGetterName(sb, getMethod).toString();
			this.sqlName = this.javaName.toUpperCase();
			if (c != null) {
				this.unique = c.unique();
				this.nullable = c.nullable();
				this.insertable = c.insertable();
				this.updatable = c.updatable();
				this.length = c.length();
				this.precision = c.precision();
				this.scale = c.scale();
				this.maxArrayLength = c.maxArrayLength();
				tmpDefinition = c.columnDefinition();
			} else {
				this.unique = Column.DEFAULT_UNIQUE;
				this.nullable = Column.DEFAULT_NULLABLE;
				this.insertable = Column.DEFAULT_INSERTABLE;
				this.updatable = Column.DEFAULT_UPDATABLE;
				this.length = Column.DEFAULT_LENGTH;
				this.precision = Column.DEFAULT_PRECISION;
				this.scale = Column.DEFAULT_SCALE;
				this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
			}
			if (isArray() && StringUtil.isNullOrEmpty(tmpDefinition)) {
				this.columnDefinition = getColumnArrayDefinition(this.returnType, tmpDefinition, this.length,
						this.precision, this.scale, this.maxArrayLength);
			} else {
				this.columnDefinition = getColumnDefinition(this.returnType, tmpDefinition, this.length, this.precision,
						this.scale);
			}
			this.sqlType = getSqlType(this.returnType);
			this.sequenceGenerator = null;
			// without map
			this.relatedTableName = null;
			this.listFromTableName = null;
			this.mappedByName = null;
//			this.fetch = null;
			this.isLink = false;
			this.fetchScale = 0;
			this.cascadeAll = false;
			this.cascadeSave = false;
			this.cascadeRemove = false;
			this.cascadeRefresh = false;
			break;
		case ID:
			// setting column
			this.javaName = AgnosticUtil.writeDataGetterName(sb, getMethod).toString();
			this.sqlName = this.javaName.toUpperCase();
			this.unique = ConstantLtm.ID_DEFAULT_UNIQUE;
			this.nullable = ConstantLtm.ID_DEFAULT_NULLABLE;
			this.insertable = ConstantLtm.ID_DEFAULT_INSERTABLE;
			this.updatable = ConstantLtm.ID_DEFAULT_UPDATABLE;
			this.sequenceGenerator = ConstantLtm.ID_DEFAULT_SEQUENCE_GENERATOR;
			this.length = ConstantLtm.ID_DEFAULT_LENGTH;
			this.precision = ConstantLtm.ID_DEFAULT_PRECISION;
			this.scale = ConstantLtm.ID_DEFAULT_SCALE;
			this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
			this.columnDefinition = getColumnDefinition(this.returnType, tmpDefinition, this.length, this.precision,
					this.scale);
			this.sqlType = getSqlType(this.returnType);
			// without map
			this.relatedTableName = null;
			this.listFromTableName = null;
			this.mappedByName = null;
//			this.fetch = null;
			this.isLink = false;
			this.fetchScale = 0;
			this.cascadeAll = false;
			this.cascadeSave = false;
			this.cascadeRemove = false;
			this.cascadeRefresh = false;
			break;
		default:
			throw new IllegalStateException();
		}
	}

	ElementColumn(final ElementTable elementTable, final Class<?> idClass) throws IllegalArgumentLtRtException {
		this.elementTable = elementTable;
		this.returnType = idClass;
		this.array = idClass.isArray();
		if (!allowDataType(this.returnType)) {
			throw new IllegalArgumentLtRtException();
		}
		// define type
		if (!this.returnType.isAssignableFrom(LtmObjectModelling.GET_ID_RETURN_CLASS)) {
			throw new IllegalArgumentLtRtException();
		}
		this.type = ElementColumnType.ID;
		// init element column
		final String tmpDefinition = null;
		// setting column
		this.javaName = LtmObjectModelling.ID_DATA_NAME;
		this.sqlName = this.javaName.toUpperCase();
		this.unique = ConstantLtm.ID_DEFAULT_UNIQUE;
		this.nullable = ConstantLtm.ID_DEFAULT_NULLABLE;
		this.insertable = ConstantLtm.ID_DEFAULT_INSERTABLE;
		this.updatable = ConstantLtm.ID_DEFAULT_UPDATABLE;
		this.sequenceGenerator = ConstantLtm.ID_DEFAULT_SEQUENCE_GENERATOR;
		this.length = ConstantLtm.ID_DEFAULT_LENGTH;
		this.precision = ConstantLtm.ID_DEFAULT_PRECISION;
		this.scale = ConstantLtm.ID_DEFAULT_SCALE;
		this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
		this.columnDefinition = getColumnDefinition(this.returnType, tmpDefinition, this.length, this.precision,
				this.scale);
		this.sqlType = getSqlType(this.returnType);
		// without map
		this.relatedTableName = null;
		this.listFromTableName = null;
		this.mappedByName = null;
//		this.fetch = null;
		this.isLink = false;
		this.fetchScale = 0;
		this.cascadeAll = false;
		this.cascadeSave = false;
		this.cascadeRemove = false;
		this.cascadeRefresh = false;
	}

	ElementColumn(final ElementTable elementTable, final DescriptorField dField) throws IllegalArgumentLtRtException {
		this.elementTable = elementTable;
		this.returnType = DataValueTypeEnum.getReturnType(dField);
		this.array = this.returnType.isArray();
		if (!allowDataType(this.returnType)) {
			throw new IllegalArgumentLtRtException();
		}
		// define type
		this.type = DataValueTypeEnum.getElementColumnType(dField);
		// init element column
		switch (this.type) {
		case ONE_TO_MANY:
			this.javaName = dField.getName();
			this.sqlName = this.javaName.toUpperCase();
			// Without column
			this.unique = false;
			this.nullable = true;
			this.insertable = false;
			this.updatable = false;
			this.sequenceGenerator = null;
			this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
			this.columnDefinition = null;
			this.sqlType = null;
			this.length = 0;
			this.precision = 0;
			this.scale = 0;
			// setting map
			this.listFromTableName = ElementTable.getFormattedName(dField.getMappedBy().getDescriptorData());
			this.relatedTableName = this.listFromTableName;
			this.mappedByName = dField.getMappedBy().getName();
//			this.fetch = oneToManyAnnotation.fetch();
			this.isLink = true;
			if (dField.getFetchScale() == null || dField.getFetchScale() < 1) {
				this.fetchScale = Integer.MAX_VALUE;
			} else {
				this.fetchScale = dField.getFetchScale().intValue();
			}
			setCascade(dField.getCascadeTypeEnumArray());
			break;
		case ONE_TO_ONE:
			final boolean isMapped = dField.getMappedBy() != null;
			this.javaName = dField.getName();
			this.sqlName = this.javaName.toUpperCase();
			if (isMapped) {
				// Without column
				this.unique = false;
				this.nullable = true;
				this.insertable = false;
				this.updatable = false;
				this.sequenceGenerator = null;
				this.length = 0;
				this.precision = 0;
				this.scale = 0;
				this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
				this.columnDefinition = null;
				this.sqlType = null;
				this.mappedByName = dField.getMappedBy().getName();
				this.relatedTableName = ElementTable.getFormattedName(dField.getMappedBy().getDescriptorData());
			} else {
				if (dField.getUnique() == null) {
					this.unique = JoinColumn.DEFAULT_UNIQUE;
				} else {
					this.unique = dField.getUnique().booleanValue();
				}
				if (dField.getNullable() == null) {
					this.nullable = JoinColumn.DEFAULT_NULLABLE;
				} else {
					this.nullable = dField.getNullable().booleanValue();
				}
				if (dField.getInsertable() == null) {
					this.insertable = JoinColumn.DEFAULT_INSERTABLE;
				} else {
					this.insertable = dField.getInsertable().booleanValue();
				}
				if (dField.getUpdatable() == null) {
					this.updatable = JoinColumn.DEFAULT_UPDATABLE;
				} else {
					this.updatable = dField.getUpdatable().booleanValue();
				}
				this.sequenceGenerator = null;
				this.length = ConstantLtm.ID_DEFAULT_LENGTH;
				this.precision = ConstantLtm.ID_DEFAULT_PRECISION;
				this.scale = ConstantLtm.ID_DEFAULT_SCALE;
				this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
				this.columnDefinition = getColumnDefinition(LtmObjectModelling.GET_ID_RETURN_CLASS, null, this.length,
						this.precision, this.scale);
				this.sqlType = getSqlType(LtmObjectModelling.GET_ID_RETURN_CLASS);
				this.mappedByName = null;
				this.relatedTableName = ElementTable.getFormattedName(dField.getReturnDescriptorData());
			}
			this.listFromTableName = null;
//			this.fetch = oneToOneAnnotation.fetch();
			this.isLink = true;
			this.fetchScale = 1;
			setCascade(dField.getCascadeTypeEnumArray());
			break;
		case MANY_TO_ONE:
			this.javaName = dField.getName();
			this.sqlName = this.javaName.toUpperCase();
			if (dField.getUnique() == null) {
				this.unique = JoinColumn.DEFAULT_UNIQUE;
			} else {
				this.unique = dField.getUnique().booleanValue();
			}
			if (dField.getNullable() == null) {
				this.nullable = JoinColumn.DEFAULT_NULLABLE;
			} else {
				this.nullable = dField.getNullable().booleanValue();
			}
			if (dField.getInsertable() == null) {
				this.insertable = JoinColumn.DEFAULT_INSERTABLE;
			} else {
				this.insertable = dField.getInsertable().booleanValue();
			}
			if (dField.getUpdatable() == null) {
				this.updatable = JoinColumn.DEFAULT_UPDATABLE;
			} else {
				this.updatable = dField.getUpdatable().booleanValue();
			}
			this.sequenceGenerator = null;
			this.length = ConstantLtm.ID_DEFAULT_LENGTH;
			this.precision = ConstantLtm.ID_DEFAULT_PRECISION;
			this.scale = ConstantLtm.ID_DEFAULT_SCALE;
			this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
			this.columnDefinition = getColumnDefinition(LtmObjectModelling.GET_ID_RETURN_CLASS, null, this.length,
					this.precision, this.scale);
			this.sqlType = getSqlType(LtmObjectModelling.GET_ID_RETURN_CLASS);
			this.relatedTableName = ElementTable.getFormattedName(dField.getReturnDescriptorData());
			this.listFromTableName = null;
			this.mappedByName = null;
//			this.fetch = manyToOneAnnotation.fetch();
			this.isLink = true;
			this.fetchScale = 1;
			setCascade(dField.getCascadeTypeEnumArray());
			break;
		case STREAM:
			// setting stream column
			this.javaName = dField.getName();
			this.sqlName = this.javaName.toUpperCase();
			this.unique = ConstantLtm.STREAM_DEFAULT_UNIQUE;
			this.nullable = ConstantLtm.STREAM_DEFAULT_NULLABLE;
			this.insertable = ConstantLtm.STREAM_DEFAULT_INSERTABLE;
			this.updatable = ConstantLtm.STREAM_DEFAULT_UPDATABLE;
			this.sequenceGenerator = null;
			this.length = ConstantLtm.STREAM_DEFAULT_LENGTH;
			this.precision = ConstantLtm.STREAM_DEFAULT_PRECISION;
			this.scale = ConstantLtm.STREAM_DEFAULT_SCALE;
			this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
			this.columnDefinition = getColumnDefinition(LtmObjectModelling.GET_ID_RETURN_CLASS, null, this.length,
					this.precision, this.scale);
			this.sqlType = getSqlType(LtmObjectModelling.GET_ID_RETURN_CLASS);
			// without map
			this.relatedTableName = null;
			this.listFromTableName = null;
			this.mappedByName = null;
//			this.fetch = null;
			this.isLink = false;
			this.fetchScale = 0;
			this.cascadeAll = false;
			this.cascadeSave = false;
			this.cascadeRemove = false;
			this.cascadeRefresh = false;
			break;
		case LEAF:
			this.javaName = dField.getName();
			this.sqlName = this.javaName.toUpperCase();
			// setting column
			if (dField.getUnique() == null) {
				this.unique = Column.DEFAULT_UNIQUE;
			} else {
				this.unique = dField.getUnique().booleanValue();
			}
			if (dField.getNullable() == null) {
				this.nullable = Column.DEFAULT_NULLABLE;
			} else {
				this.nullable = dField.getNullable().booleanValue();
			}
			if (dField.getInsertable() == null) {
				this.insertable = Column.DEFAULT_INSERTABLE;
			} else {
				this.insertable = dField.getInsertable().booleanValue();
			}
			if (dField.getUpdatable() == null) {
				this.updatable = Column.DEFAULT_UPDATABLE;
			} else {
				this.updatable = dField.getUpdatable().booleanValue();
			}
			this.length = Column.DEFAULT_LENGTH;
			this.precision = Column.DEFAULT_PRECISION;
			this.scale = Column.DEFAULT_SCALE;
			if (dField.getMaxArrayLength() == null) {
				this.maxArrayLength = Column.DEFAULT_MAX_ARRAY_LENGTH;
			} else {
				this.maxArrayLength = dField.getMaxArrayLength().intValue();
			}
			if (isArray()) {
				this.columnDefinition = getColumnArrayDefinition(this.returnType, null, this.length, this.precision,
						this.scale, this.maxArrayLength);
			} else {
				this.columnDefinition = getColumnDefinition(this.returnType, null, this.length, this.precision,
						this.scale);
			}
			this.sqlType = getSqlType(this.returnType);
			this.sequenceGenerator = null;
			// without map
			this.relatedTableName = null;
			this.listFromTableName = null;
			this.mappedByName = null;
//			this.fetch = null;
			this.isLink = false;
			this.fetchScale = 0;
			this.cascadeAll = false;
			this.cascadeSave = false;
			this.cascadeRemove = false;
			this.cascadeRefresh = false;
			break;
		default:
			throw new ImplementationLtRtException();
		}
	}

	private void setCascade(final CascadeTypeEnum[] cascade) {
		if (cascade != null) {
			boolean cascadeAll = false;
			boolean cascadeSave = false;
			boolean cascadeRemove = false;
			boolean cascadeRefresh = false;
			for (final CascadeTypeEnum ct : cascade) {
				switch (ct) {
				case ALL:
					cascadeAll = true;
					break;
				case SAVE:
					cascadeSave = true;
					break;
				case REMOVE:
					cascadeRemove = true;
					break;
				case REFRESH:
					cascadeRefresh = true;
					break;
				default:
					throw new IllegalStateException();
				}
			}
			this.cascadeAll = cascadeAll;
			this.cascadeSave = cascadeSave;
			this.cascadeRemove = cascadeRemove;
			this.cascadeRefresh = cascadeRefresh;
		}
	}

	private static int getSqlType(final Class<?> returnType) {
		int result;
		if (Byte.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.byteTypes();
		} else if (Short.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.shortTypes();
		} else if (Integer.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.integerTypes();
		} else if (Long.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.longTypes();
		} else if (BigDecimal.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.bigDecimalTypes();
		} else if (Double.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.doubleTypes();
		} else if (Boolean.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.booleanTypes();
		} else if (String.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.stringTypes();
		} else if (returnType.isArray()) {
			result = LTM_MANAGER.arrayTypes();
		} else {
			throw new ImplementationLtRtException();
		}
		return result;
	}

	private static String getColumnArrayDefinition(final Class<?> returnType, final String tmpDefinition,
			final int length, final int precision, final int scale, final int maxArrayLength)
			throws IllegalArgumentLtRtException {
		String result;
		final Class<?> returnComponentType = returnType.getComponentType();
		if (Byte.class.isAssignableFrom(returnComponentType)) {
			result = LTM_MANAGER.byteArrayAlias(tmpDefinition, maxArrayLength);
		} else if (Short.class.isAssignableFrom(returnComponentType)) {
			result = LTM_MANAGER.shortArrayAlias(tmpDefinition, maxArrayLength);
		} else if (Integer.class.isAssignableFrom(returnComponentType)) {
			result = LTM_MANAGER.integerArrayAlias(tmpDefinition, maxArrayLength);
		} else if (Long.class.isAssignableFrom(returnComponentType)) {
			result = LTM_MANAGER.longArrayAlias(tmpDefinition, maxArrayLength);
		} else if (Boolean.class.isAssignableFrom(returnComponentType)) {
			result = LTM_MANAGER.booleanArrayAlias(tmpDefinition, maxArrayLength);
		} else {
			throw new ImplementationLtRtException();
		}
		return result;
	}

	private static String getColumnDefinition(final Class<?> returnType, final String tmpDefinition, final int length,
			final int precision, final int scale) throws IllegalArgumentLtRtException {
		String result;
		if (Byte.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.byteAlias(tmpDefinition);
		} else if (Short.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.shortAlias(tmpDefinition);
		} else if (Integer.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.integerAlias(tmpDefinition);
		} else if (Long.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.longAlias(tmpDefinition);
		} else if (BigDecimal.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.bigDecimalAlias(tmpDefinition, precision, scale);
		} else if (Double.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.doubleAlias(tmpDefinition, precision, scale);
		} else if (Boolean.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.booleanAlias(tmpDefinition);
		} else if (String.class.isAssignableFrom(returnType)) {
			result = LTM_MANAGER.stringAlias(tmpDefinition, length);
		} else {
			throw new ImplementationLtRtException();
		}
		return result;
	}

	private static <T extends Annotation> T getConfigAnnotation(final Class<T> annotationClass, final Method getMethod,
			final Method setMethod) {
		T result = getMethod.getAnnotation(annotationClass);
		if (result == null) {
			result = setMethod.getAnnotation(annotationClass);
		}
		return result;
	}

	private static boolean allowDataType(final Class<?> clazz) {
		boolean result = false;
		if (Set.class.isAssignableFrom(clazz)) {
			result = true;
		}
		if (!result && ElementTable.directExtendsTableItf(clazz)) {
			result = true;
		}
		if (!result && LtmBinary.class.equals(clazz)) {
			result = true;
		}
		if (!result && clazz.isArray()) {
			for (int i = 0; !result && i < ACEPTED_ARRAY_COMPONENT_CLASSES.length; i++) {
				if (ACEPTED_ARRAY_COMPONENT_CLASSES[i].isAssignableFrom(clazz.getComponentType())) {
					result = true;
				}
			}
		} else {
			for (int i = 0; !result && i < ACEPTED_LEAF_CLASSES.length; i++) {
				if (ACEPTED_LEAF_CLASSES[i].isAssignableFrom(clazz)) {
					result = true;
				}
			}
		}
		return result;
	}

	ElementTable getElementTable() {
		return this.elementTable;
	}

	String getSqlName() {
		return this.sqlName;
	}

	String getJavaName() {
		return this.javaName;
	}

	boolean isUnique() {
		return this.unique;
	}

	boolean isNullable() {
		return this.nullable;
	}

	boolean isInsertable() {
		return this.insertable;
	}

	boolean isUpdatable() {
		return this.updatable;
	}

	Class<?> getReturnType() {
		return this.returnType;
	}

	int getSqlType() {
		return this.sqlType;
	}

	String getColumnDefinition() {
		return this.columnDefinition;
	}

	int getLength() {
		return this.length;
	}

	int getPrecision() {
		return this.precision;
	}

	int getScale() {
		return this.scale;
	}

//	FetchTypeEnum getFetch() {
//		return fetch;
//	}

	public boolean isLink() {
		return this.isLink;
	}

	int getFetchScale() {
		return this.fetchScale;
	}

	boolean isCascadeAll() {
		return this.cascadeAll;
	}

	boolean isCascadeSave() {
		return this.cascadeSave;
	}

	boolean isCascadeRemove() {
		return this.cascadeRemove;
	}

	boolean isCascadeRefresh() {
		return this.cascadeRefresh;
	}

	ElementTable getListFromElementTable() {
		ElementTable result;
		if (this.listFromTableName == null) {
			result = null;
		} else {
			result = ElementTable.getInstance(this.listFromTableName);
		}
		return result;
	}

	String getMappedByName() {
		return this.mappedByName;
	}

	ElementTable getRelatedElementTable() {
		ElementTable result;
		if (this.relatedTableName == null) {
			result = null;
		} else {
			result = ElementTable.getInstance(this.relatedTableName);
		}
		return result;
	}

	boolean isMapped() {
		return this.mappedByName != null;
	}

	boolean isId() {
		return ElementColumnType.ID.equals(this.type);
	}

	boolean isStream() {
		return ElementColumnType.STREAM.equals(this.type);
	}

	boolean isArray() {
		return this.array;
	}

	SequenceGeneratorEnum getSequenceGenerator() {
		return this.sequenceGenerator;
	}

}

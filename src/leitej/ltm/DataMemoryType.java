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

import java.math.BigDecimal;
import java.sql.Types;

import leitej.exception.UnsupportedDataTypeLtRtException;

/**
 * @author Julio Leite
 *
 */
enum DataMemoryType {
	BYTE, SHORT, INT, LONG, BIG_DECIMAL, DOUBLE, FLOAT, BOOLEAN, ENUM, STRING, BINARY, LARGE_MEMORY, LONG_TERM_MEMORY;

	int getSqlType() {
		int result;
		switch (this) {
		case BYTE:
			result = Types.TINYINT;
			break;
		case SHORT:
			result = Types.SMALLINT;
			break;
		case INT:
			result = Types.INTEGER;
			break;
		case LONG:
			result = Types.BIGINT;
			break;
		case BIG_DECIMAL:
			result = Types.DECIMAL;
			break;
		case DOUBLE:
			result = Types.DOUBLE;
			break;
		case FLOAT:
			result = Types.FLOAT;
			break;
		case BOOLEAN:
			result = Types.BOOLEAN;
			break;
		case ENUM:
			result = Types.VARCHAR;
			break;
		case STRING:
			result = Types.LONGVARCHAR;
			break;
		case BINARY:
			result = Types.LONGVARBINARY;
			break;
		case LARGE_MEMORY:
			result = Types.BIGINT;
			break;
		case LONG_TERM_MEMORY:
			result = Types.BIGINT;
			break;
		default:
			throw new UnsupportedDataTypeLtRtException("type: #0", this);
		}
		return result;
	}

	static DataMemoryType getDataMemoryType(final Class<?> type) {
		final DataMemoryType result;
		if (Byte.class.isAssignableFrom(type) || byte.class.isAssignableFrom(type)) {
			result = DataMemoryType.BYTE;
		} else if (Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type)) {
			result = DataMemoryType.SHORT;
		} else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
			result = DataMemoryType.INT;
		} else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
			result = DataMemoryType.LONG;
		} else if (BigDecimal.class.isAssignableFrom(type)) {
			result = DataMemoryType.BIG_DECIMAL;
		} else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
			result = DataMemoryType.DOUBLE;
		} else if (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)) {
			result = DataMemoryType.FLOAT;
		} else if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
			result = DataMemoryType.BOOLEAN;
		} else if (type.isEnum()) {
			result = DataMemoryType.ENUM;
		} else if (String.class.isAssignableFrom(type)) {
			result = DataMemoryType.STRING;
		} else if (type.isArray() && (byte.class.isAssignableFrom(type.getComponentType()))) {
			result = DataMemoryType.BINARY;
		} else if (LargeMemory.class.isAssignableFrom(type)) {
			result = DataMemoryType.LARGE_MEMORY;
		} else if (LtmObjectModelling.class.isAssignableFrom(type)) {
			result = DataMemoryType.LONG_TERM_MEMORY;
		} else {
			throw new UnsupportedDataTypeLtRtException("type: #0", type);
		}
		return result;
	}

}

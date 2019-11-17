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

import java.util.Set;

import leitej.exception.ImplementationLtRtException;
import leitej.ltm.ElementColumn.ElementColumnType;
import leitej.ltm.dynamic.DescriptorField;
import leitej.ltm.dynamic.DynamicLtmObjectModel;

/**
 *
 * @author Julio Leite
 */
public enum DataValueTypeEnum {
	BOOLEAN, BYTE, SHORT, INTEGER, LONG, DOUBLE, STRING,
	// BIGDECIMAL, //TODO: add? BigDecimal to xmlom

	ARRAY_BOOLEAN, ARRAY_BYTE, ARRAY_SHORT, ARRAY_INTEGER, ARRAY_LONG,

	LINK_ONE_TO_ONE, LINK_MANY_TO_ONE, LINK_ONE_TO_MANY,

	BINARY_STREAM;

	static Class<?> getReturnType(final DescriptorField dField) {
		Class<?> result;
		switch (dField.getDataValueTypeEnum()) {
		case BOOLEAN:
			result = Boolean.class;
			break;
		case BYTE:
			result = Byte.class;
			break;
		case SHORT:
			result = Short.class;
			break;
		case INTEGER:
			result = Integer.class;
			break;
		case LONG:
			result = Long.class;
			break;
		case DOUBLE:
			result = Double.class;
			break;
		case STRING:
			result = String.class;
			break;
		case ARRAY_BOOLEAN:
			result = ConstantLtm.CLASS_ARRAY_BOOLEAN;
			break;
		case ARRAY_BYTE:
			result = ConstantLtm.CLASS_ARRAY_BYTE;
			break;
		case ARRAY_SHORT:
			result = ConstantLtm.CLASS_ARRAY_SHORT;
			break;
		case ARRAY_INTEGER:
			result = ConstantLtm.CLASS_ARRAY_INTEGER;
			break;
		case ARRAY_LONG:
			result = ConstantLtm.CLASS_ARRAY_LONG;
			break;
		case LINK_ONE_TO_ONE:
		case LINK_MANY_TO_ONE:
			result = DynamicLtmObjectModel.class;
			break;
		case LINK_ONE_TO_MANY:
			result = Set.class;
			break;
		case BINARY_STREAM:
			result = LtmBinary.class;
			break;
		default:
			throw new ImplementationLtRtException();
		}
		return result;
	}

	static ElementColumnType getElementColumnType(final DescriptorField dField) {
		ElementColumnType result;
		switch (dField.getDataValueTypeEnum()) {
		case BOOLEAN:
		case BYTE:
		case SHORT:
		case INTEGER:
		case LONG:
		case DOUBLE:
		case STRING:
		case ARRAY_BOOLEAN:
		case ARRAY_BYTE:
		case ARRAY_SHORT:
		case ARRAY_INTEGER:
		case ARRAY_LONG:
			result = ElementColumnType.LEAF;
			break;
		case LINK_ONE_TO_ONE:
			result = ElementColumnType.ONE_TO_ONE;
			break;
		case LINK_MANY_TO_ONE:
			result = ElementColumnType.MANY_TO_ONE;
			break;
		case LINK_ONE_TO_MANY:
			result = ElementColumnType.ONE_TO_MANY;
			break;
		case BINARY_STREAM:
			result = ElementColumnType.STREAM;
			break;
		default:
			throw new ImplementationLtRtException();
		}
		return result;
	}

}

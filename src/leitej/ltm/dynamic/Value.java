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

package leitej.ltm.dynamic;

import leitej.ltm.DataValueTypeEnum;
import leitej.xml.om.XmlObjectModelling;

/**
 *
 * @author Julio Leite
 */
public abstract interface Value extends XmlObjectModelling {

	public abstract DataValueTypeEnum getType();

	public abstract void setType(DataValueTypeEnum type);

	public abstract Boolean getBooleanValue();

	public abstract void setBooleanValue(Boolean value);

	public abstract Byte getByteValue();

	public abstract void setByteValue(Byte value);

	public abstract Short getShortValue();

	public abstract void setShortValue(Short value);

	public abstract Integer getIntegerValue();

	public abstract void setIntegerValue(Integer value);

	public abstract Long getLongValue();

	public abstract void setLongValue(Long value);

//	public abstract BigDecimal getBigDecimalValue();

//	public abstract void setBigDecimalValue(BigDecimal value);

	public abstract Double getDoubleValue();

	public abstract void setDoubleValue(Double value);

	public abstract String getStringValue();

	public abstract void setStringValue(String value);

	public abstract Boolean[] getBooleanArrayValue();

	public abstract void setBooleanArrayValue(Boolean[] value);

	public abstract Byte[] getByteArrayValue();

	public abstract void setByteArrayValue(Byte[] value);

	public abstract Short[] getShortArrayValue();

	public abstract void setShortArrayValue(Short[] value);

	public abstract Integer[] getIntegerArrayValue();

	public abstract void setIntegerArrayValue(Integer[] value);

	public abstract Long[] getLongArrayValue();

	public abstract void setLongArrayValue(Long[] value);

//	public abstract Long getLink();

//	public abstract void setLink(Long link);

}

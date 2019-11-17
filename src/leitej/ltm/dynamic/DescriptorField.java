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
import leitej.ltm.annotation.CascadeTypeEnum;
import leitej.xml.om.XmlObjectModelling;

/**
 *
 * @author Julio Leite
 */
public abstract interface DescriptorField extends XmlObjectModelling {

	public abstract DescriptorData getDescriptorData();

	public abstract void setDescriptorData(DescriptorData descriptorData);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract DataValueTypeEnum getDataValueTypeEnum();

	public abstract void setDataValueTypeEnum(DataValueTypeEnum dataValueTypeEnum);

	public abstract CascadeTypeEnum[] getCascadeTypeEnumArray();

	public abstract void setCascadeTypeEnumArray(CascadeTypeEnum[] cascadeTypeEnumArray);

	public abstract Integer getFetchScale();

	public abstract void setFetchScale(Integer fetchScale);

	public abstract DescriptorData getReturnDescriptorData();

	public abstract void setReturnDescriptorData(DescriptorData returnDescriptorData);

	public abstract DescriptorField getMappedBy();

	public abstract void setMappedBy(DescriptorField mappedBy);

	public abstract Boolean getUnique();

	public abstract void setUnique(Boolean unique);

	public abstract Boolean getNullable();

	public abstract void setNullable(Boolean nullable);

	public abstract Boolean getInsertable();

	public abstract void setInsertable(Boolean insertable);

	public abstract Boolean getUpdatable();

	public abstract void setUpdatable(Boolean updatable);

	public abstract Integer getMaxArrayLength();

	public abstract void setMaxArrayLength(Integer maxArrayLength);

}

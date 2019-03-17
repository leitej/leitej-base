/*******************************************************************************
 * Copyright (C) 2011 Julio Leite
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

package test.xml.om;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import leitej.xml.om.XmlObjectModelling;

public interface Sample extends XmlObjectModelling {

	public Byte getZbyte();

	public void setZbyte(Byte zbyte);

	public Short getZshort();

	public void setZshort(Short zshort);

	public Integer getZint();

	public void setZint(Integer zint);

	public Long getZlong();

	public void setZlong(Long zlong);

	public Float getZfloat();

	public void setZfloat(Float zfloat);

	public Double getZdouble();

	public void setZdouble(Double zdouble);

	public Boolean getZboolean();

	public void setZboolean(Boolean zboolean);

	public Character getZchar();

	public void setZchar(Character zchar);

	public byte getPbyte();

	public void setPbyte(byte pbyte);

	public short getPshort();

	public void setPshort(short pshort);

	public int getPint();

	public void setPint(int pint);

	public long getPlong();

	public void setPlong(long plong);

	public float getPfloat();

	public void setPfloat(float pfloat);

	public double getPdouble();

	public void setPdouble(double pdouble);

	public boolean isPboolean();

	public void setPboolean(boolean pboolean);

	public char getPchar();

	public void setPchar(char pchar);

	public Date getDate();

	public void setDate(Date date);

	public String getName();

	public void setName(String name);

	public Sample getFilho();

	public void setFilho(Sample filho);

	public String[] getAo();

	public void setAo(String[] ao);

	public Set<Sample> getSo();

	public void setSo(Set<Sample> so);

	public Map<String, Sample> getMo();

	public void setMo(Map<String, Sample> mo);

	public List<Sample> getLo();

	public void setLo(List<Sample> lo);

	public Sample[] getAm();

	public void setAm(Sample[] am);

	public String[][][] getAs();

	public void setAs(String[][][] as);

}

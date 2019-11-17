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

package leitej.gui.uniform.model;

import leitej.xml.om.XmlObjectModelling;

/**
 *
 * @author Julio Leite
 */
public interface Border extends XmlObjectModelling {

	public BorderTypeEnum getType();

	public void setType(BorderTypeEnum type);

	public Color getColor();

	public void setColor(Color color);

	public Color getShadow();

	public void setShadow(Color shadow);

	public BorderThickness getThickness();

	public void setThickness(BorderThickness thickness);

	public boolean isRoundedCorners();

	public void setRoundedCorners(boolean roundedCorners);

	public byte[] getIconImage();

	public void setIconImage(byte[] iconImage);

	public BorderTitle getTitle();

	public void setTitle(BorderTitle title);

	public Border getCompound();

	public void setCompound(Border compoundBorder);

}

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
public interface TextArea extends XmlObjectModelling {

	public Scroll getScroll();

	public void setScroll(Scroll scroll);

	public boolean isEditable();

	public void setEditable(boolean editable);

	public Area getArea();

	public void setArea(Area area);

	public String getText();

	public void setText(String text);

}

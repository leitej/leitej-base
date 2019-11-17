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
public interface Value extends XmlObjectModelling {

	public String getIndex();

	public void setIndex(String index);

	public String getElementId();

	public void setElementId(String elementId);

	public ActionEnum getAction();

	public void setAction(ActionEnum action);

	public String[] getConstants();

	public void setConstants(String[] constants);

	public String getTextInput();

	public void setTextInput(String textInput);

	public char[] getPasswordInput();

	public void setPasswordInput(char[] passwordInput);

	public Point getPointInput();

	public void setPointInput(Point point);

	public byte[] getBinaryInput();

	public void setBinaryInput(byte[] binaryInput);

}

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
public interface ElementUpdate extends XmlObjectModelling {

	public String getId();

	public void setId(String id);

	public Style getStyle();

	public void setStyle(Style style);

	public Action[] getActions();

	public void setActions(Action[] actions);

	public String getToolTipText();

	public void setToolTipText(String toolTipText);

	public String getAccessibleName();

	public void setAccessibleName(String accessibleName);

	public String getAccessibleDescription();

	public void setAccessibleDescription(String accessibleDescription);

	/*
	 * Leafs
	 */

	public TextAreaUpdate getTextAreaUpdate();

	public void setTextAreaUpdate(TextAreaUpdate textAreaUpdate);

	public ImageUpdate getImageUpdate();

	public void setImageUpdate(ImageUpdate imageUpdate);

}

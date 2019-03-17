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

package leitej.log;

import leitej.xml.om.XmlObjectModelling;

/**
 * Object used exclusively to get and set configuration of
 * {@link leitej.log.Logger Logger}, owing to XMLOM concept.
 *
 * @author Julio Leite
 */
public abstract interface ConfigFile extends XmlObjectModelling {

	public abstract String getCharsetName();

	public abstract void setCharsetName(String charsetName);

	public abstract Boolean getAppendFile();

	public abstract void setAppendFile(Boolean appendFile);

	public abstract String getFileName();

	public abstract void setFileName(String fileName);

	public abstract ConfigDynFileName getDynName();

	public abstract void setDynName(ConfigDynFileName dynName);

	public abstract String getPath();

	public abstract void setPath(String path);

}

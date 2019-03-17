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

import java.util.Map;

import leitej.xml.om.XmlObjectModelling;

/**
 * Object used exclusively to get and set configuration of
 * {@link leitej.log.Logger Logger}, owing to XMLOM concept.
 *
 * @author Julio Leite
 */
public abstract interface Config extends XmlObjectModelling {

	public abstract boolean isConsole();

	public abstract void setConsole(boolean console);

	public abstract String getDateFormat();

	public abstract void setDateFormat(String dateFormat);

	public abstract LevelEnum getLogLevel();

	public abstract void setLogLevel(LevelEnum logLevel);

	public abstract Map<String, LevelEnum> getPackageLogLevel();

	public abstract void setPackageLogLevel(Map<String, LevelEnum> packageLogLevel);

	public abstract ConfigFile getFile();

	public abstract void setFile(ConfigFile file);

}

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

package leitej.net.dtp;

import leitej.xml.om.XmlObjectModelling;

/**
 *
 * @author Julio Leite
 */
public abstract interface RawDataPort extends XmlObjectModelling {

	public abstract long getCallNumber();

	public abstract void setCallNumber(long callNumber);

	public abstract long getId();

	public abstract void setId(long id);

	public abstract int getPort();

	public abstract void setPort(int port);

}

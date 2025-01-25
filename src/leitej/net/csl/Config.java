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

package leitej.net.csl;

import leitej.xml.om.XmlObjectModelling;

/**
 * @author Julio Leite
 *
 */
public abstract interface Config extends XmlObjectModelling {

	/**
	 *
	 * @return byte per second (0 infinite)
	 */
	abstract int getVelocity();

	abstract void setVelocity(int velocity);

	/**
	 *
	 * @return number of bytes per read step (0 infinite)
	 */
	abstract long getSizePerSentence();

	abstract void setSizePerSentence(long sizePerSentence);

	/**
	 *
	 * @return the specified timeout, in milliseconds (0 infinite)
	 */
	abstract int getTimeOutMs();

	abstract void setTimeOutMs(int timeOutMs);

	/**
	 * Returns setting for SO_TIMEOUT to be used at initiation of connection.<br/>
	 * 0 returns implies that the option is disabled (i.e., timeout of infinity).
	 *
	 * @return the specified timeout, in milliseconds (0 infinite)
	 */
	abstract int getInitCommTimeOutMs();

	abstract void setInitCommTimeOutMs(int initCommTimeOutMs);

}

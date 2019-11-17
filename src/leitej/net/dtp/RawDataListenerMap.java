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

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import leitej.exception.IllegalArgumentLtRtException;

/**
 *
 * @author Julio Leite
 */
final class RawDataListenerMap implements Serializable {

	private static final long serialVersionUID = -4314522188275395981L;

	private final int maxListenersPerBindAddr;
	private final Map<InetAddress, RawDataListenerBindedPool> map;

	RawDataListenerMap(final int maxListenersPerBindAddr) throws IllegalArgumentLtRtException {
		if (maxListenersPerBindAddr < 1) {
			throw new IllegalArgumentLtRtException();
		}
		this.maxListenersPerBindAddr = maxListenersPerBindAddr;
		this.map = new HashMap<>(2);
	}

	RawDataListenerBindedPool get(final InetAddress bindAddr) {
		RawDataListenerBindedPool result;
		synchronized (this.map) {
			result = this.map.get(bindAddr);
			if (result == null) {
				result = new RawDataListenerBindedPool(this.maxListenersPerBindAddr, bindAddr);
				this.map.put(bindAddr, result);
			}
		}
		return result;
	}

}

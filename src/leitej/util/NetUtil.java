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

package leitej.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * An useful class to help get information about the network.
 *
 * @author Julio Leite
 */
public final class NetUtil {

	/**
	 * Creates a new instance of NetUtil.
	 */
	private NetUtil() {
	}

	/**
	 * Determines the IP address of a host, given the host's name.
	 *
	 * @param hostName the specified host, or null
	 * @return an IP address for the given host name
	 * @throws UnknownHostException If no IP address for the host could be found, or
	 *                              if a scope_id was specified for a global IPv6
	 *                              address
	 * @throws SecurityException    If a security manager exists and its
	 *                              checkConnect method doesn't allow the operation
	 */
	public static InetAddress getInetAdress(final String hostName) throws SecurityException, UnknownHostException {
		return InetAddress.getByName(hostName);
	}

	/**
	 * Returns the local host.<br/>
	 * If there is a security manager, its checkConnect method is called with the
	 * local host name and -1 as its arguments to see if the operation is allowed.
	 * If the operation is not allowed, an InetAddress representing the loopback
	 * address is returned.
	 *
	 * @return the IP address of the local host
	 * @throws UnknownHostException If no IP address for the host could be found
	 */
	public static InetAddress getLocalInetAdress() throws UnknownHostException {
		return InetAddress.getLocalHost();
	}

	/**
	 * Returns the IP address string in textual presentation.
	 *
	 * @return the raw IP address in a string format
	 * @throws UnknownHostException If no IP address for the host could be found
	 */
	public static String localHostAdress() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}

	/**
	 * Gets the host name for this IP address.
	 *
	 * @return the host name for this IP address, or if the operation is not allowed
	 *         by the security check, the textual representation of the IP address
	 * @throws UnknownHostException If no IP address for the host could be found
	 */
	public static String localHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}

}

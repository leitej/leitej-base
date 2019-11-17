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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import leitej.net.exception.ConnectionLtException;

/**
 *
 * @author Julio Leite
 */
public final class CommunicationListener extends
		AbstractCommunicationListener<CommunicationFactory, CommunicationListener, CommunicationHost, CommunicationGuest> {

	/**
	 * Create a server with the specified port, listen backlog, and local IP address
	 * to bind to. The <i>bindAddr</i> argument can be used on a multi-homed host
	 * for a ServerSocket that will only accept connect requests to one of its
	 * addresses. If <i>bindAddr</i> is null, it will default accepting connections
	 * on any/all local addresses. The port must be between 0 and 65535, inclusive.
	 *
	 * @param factory
	 * @param port     the local TCP port
	 * @param backlog  the listen backlog
	 * @param bindAddr the local InetAddress the server will bind to
	 * @throws SecurityException if a security manager exists and its
	 *                           <code>checkListen</code> method doesn't allow the
	 *                           operation
	 * @throws SocketException   if there is an error in the underlying protocol,
	 *                           such as a TCP error
	 * @throws IOException       if an I/O error occurs when opening the socket
	 */
	CommunicationListener(final CommunicationFactory factory, final int port, final int backlog,
			final InetAddress bindAddr) throws SecurityException, SocketException, IOException {
		super(factory, port, backlog, bindAddr);
	}

	@Override
	protected final CommunicationHost getCommunicationSessionHost(final CommunicationFactory factory,
			final Socket socket) throws ConnectionLtException {
		try {
			return new CommunicationHost(factory, socket);
		} catch (final SocketException e) {
			throw new ConnectionLtException(e);
		}
	}

}

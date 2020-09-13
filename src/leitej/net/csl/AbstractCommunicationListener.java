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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import leitej.exception.ConnectionLtException;

/**
 * Server - Communication Session Layer
 *
 * @author Julio Leite
 */
public abstract class AbstractCommunicationListener<F extends AbstractCommunicationFactory<F, L, H, G>, L extends AbstractCommunicationListener<F, L, H, G>, H extends AbstractCommunicationSession<F, L, H, G>, G extends AbstractCommunicationSession<F, L, H, G>> {

	private final F factory;
	private final ServerSocket serverSocket;

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
	protected AbstractCommunicationListener(final F factory, final int port, final int backlog,
			final InetAddress bindAddr) throws SecurityException, SocketException, IOException {
		this.factory = factory;
		this.serverSocket = new ServerSocket(port, backlog, bindAddr);
		this.serverSocket.setSoTimeout(0);
	}

	/**
	 * Listens for a connection to be made to this server and accepts it. The method
	 * blocks until a connection is made.
	 *
	 * @return the new Communication Session
	 * @throws SecurityException     if a security manager exists and its
	 *                               <code>checkAccept</code> method doesn't allow
	 *                               the operation
	 * @throws SocketException       if there is an error in the underlying
	 *                               protocol, such as a TCP error
	 * @throws ConnectionLtException if any exception is raised due to waiting for a
	 *                               connection or the instantiation or
	 *                               initialisation, that exception should be put in
	 *                               the cause of the ConnectionLtException
	 */
	public final H accept() throws SocketException, SecurityException, ConnectionLtException {
		try {
			final Socket socket = this.serverSocket.accept();
			// TODO: build some blacked list IP for denial when in abuse, closing the socket
			// write here
			// the list can be accessed and populated at session class
			return getCommunicationSessionHost(this.factory, socket);
		} catch (final SocketException e) {
			throw e;
		} catch (final IOException e) {
			throw new ConnectionLtException(e);
		}
	}

	/**
	 * instantiates and initiates communication session host side
	 *
	 * @param factory
	 * @param socket
	 * @return the new Communication Session Host
	 * @throws ConnectionLtException if any exception is raised due to the
	 *                               instantiation or initialisation, that exception
	 *                               should be put in the cause of the
	 *                               ConnectionLtException
	 */
	protected abstract H getCommunicationSessionHost(F factory, Socket socket) throws ConnectionLtException;

	/**
	 *
	 * @return a <code>SocketAddress</code> representing the local endpoint, or
	 *         <code>null</code> if it is not bound yet
	 */
	public final SocketAddress getLocalSocketAddress() {
		return this.serverSocket.getLocalSocketAddress();
	}

	/**
	 *
	 * @return true if the socket has been closed
	 */
	public final boolean isClosed() {
		return this.serverSocket.isClosed();
	}

	/**
	 * Closes this socket.<br/>
	 * Any thread currently blocked in {@link #accept()} will throw a
	 * {@link SocketException}.
	 *
	 * @throws ConnectionLtException <br/>
	 *                               +Cause IOException if an I/O error occurs when
	 *                               closing the socket
	 */
	public final void close() throws ConnectionLtException {
		try {
			this.serverSocket.close();
		} catch (final IOException e) {
			throw new ConnectionLtException(e);
		}
	}

}

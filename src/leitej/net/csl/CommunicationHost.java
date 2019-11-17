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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import leitej.net.exception.ConnectionLtException;

/**
 *
 * @author Julio Leite
 */
public final class CommunicationHost extends
		AbstractCommunicationSession<CommunicationFactory, CommunicationListener, CommunicationHost, CommunicationGuest> {

	/**
	 * initiates session from host side.
	 *
	 * @param factory with settings to apply
	 * @param socket  connected to the guest
	 * @throws SocketException       if there is an error in the underlying
	 *                               protocol, such as a TCP error
	 * @throws ConnectionLtException <br/>
	 *                               +Cause IOException if an error occurs during
	 *                               the connection <br/>
	 *                               +Cause IllegalArgumentLtRtException if the
	 *                               charset name read from socket is not defined on
	 *                               host
	 */
	protected CommunicationHost(final CommunicationFactory factory, final Socket socket)
			throws SocketException, ConnectionLtException {
		super(factory, socket);
	}

	@Override
	protected final void initiateCommunication(final InputStream in, final OutputStream out)
			throws ConnectionLtException {
	}

	@Override
	protected final void initiateWrappedCommunication(final InputStream in, final OutputStream out)
			throws ConnectionLtException {
	}

}

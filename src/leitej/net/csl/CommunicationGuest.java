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
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;

import leitej.exception.ConnectionLtException;
import leitej.exception.IllegalArgumentLtRtException;

/**
 *
 * @author Julio Leite
 */
public final class CommunicationGuest extends
		AbstractCommunicationSession<CommunicationFactory, CommunicationListener, CommunicationHost, CommunicationGuest> {

	/**
	 * Connects and initiates session from guest side.
	 *
	 * @param factory  with settings to apply
	 * @param endpoint the SocketAddress
	 * @param charset
	 * @throws SocketException              if there is an error in the underlying
	 *                                      protocol, such as a TCP error
	 * @throws IllegalArgumentException     if endpoint is null or is a
	 *                                      SocketAddress subclass not supported by
	 *                                      this socket
	 * @throws IllegalArgumentLtRtException if the charset is not defined
	 * @throws ConnectionLtException        <br/>
	 *                                      +Cause IOException if an error occurs
	 *                                      during the connection
	 */
	CommunicationGuest(final CommunicationFactory factory, final SocketAddress endpoint, final Charset charset)
			throws SocketException, IllegalArgumentLtRtException, IllegalArgumentException, ConnectionLtException {
		super(factory, endpoint, charset);
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

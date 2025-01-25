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
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;

import leitej.exception.ConnectionLtException;
import leitej.exception.IllegalArgumentLtRtException;

/**
 *
 * @author Julio Leite
 */
public final class CommunicationFactory extends
		AbstractCommunicationFactory<CommunicationFactory, CommunicationListener, CommunicationHost, CommunicationGuest> {

	/**
	 *
	 */
	public CommunicationFactory() {
		super(AbstractCommunicationFactory.DEFAULT_CONFIG);
	}

	/**
	 *
	 * @param config
	 */
	public CommunicationFactory(final Config config) {
		super(config);
	}

	@Override
	public CommunicationGuest clientInstanciation(final SocketAddress endpoint, final Charset charset)
			throws ConnectionLtException {
		try {
			return new CommunicationGuest(this, endpoint, charset);
		} catch (final IllegalArgumentLtRtException e) {
			throw new ConnectionLtException(e);
		} catch (final SocketException e) {
			throw new ConnectionLtException(e);
		} catch (final IllegalArgumentException e) {
			throw new ConnectionLtException(e);
		}
	}

	@Override
	public CommunicationListener serverInstanciation(final int port, final int backlog, final InetAddress bindAddr)
			throws ConnectionLtException {
		try {
			return new CommunicationListener(this, port, backlog, bindAddr);
		} catch (final SecurityException e) {
			throw new ConnectionLtException(e);
		} catch (final SocketException e) {
			throw new ConnectionLtException(e);
		} catch (final IOException e) {
			throw new ConnectionLtException(e);
		}
	}

}

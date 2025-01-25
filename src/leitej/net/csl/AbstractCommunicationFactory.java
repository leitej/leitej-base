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

import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;

import leitej.exception.ConnectionLtException;
import leitej.net.ConstantNet;
import leitej.xml.om.Xmlom;

/**
 * Factory - Communication Session Layer
 *
 * @author Julio Leite
 */
public abstract class AbstractCommunicationFactory<F extends AbstractCommunicationFactory<F, L, H, G>, L extends AbstractCommunicationListener<F, L, H, G>, H extends AbstractCommunicationSession<F, L, H, G>, G extends AbstractCommunicationSession<F, L, H, G>> {

	public static final Config DEFAULT_CONFIG;

	static {
		DEFAULT_CONFIG = Xmlom.newInstance(Config.class);
		DEFAULT_CONFIG.setVelocity(ConstantNet.DEFAULT_VELOCITY);
		DEFAULT_CONFIG.setSizePerSentence(ConstantNet.DEFAULT_SIZE_PER_SENTENCE);
		DEFAULT_CONFIG.setTimeOutMs(ConstantNet.DEFAULT_TIMEOUT_MS);
		DEFAULT_CONFIG.setInitCommTimeOutMs(ConstantNet.DEFAULT_INITIATE_COMMUNICATION_TIMEOUT_MS);
	}

	private final Config config;

	/**
	 *
	 * @param config
	 */
	public AbstractCommunicationFactory(final Config config) {
		this.config = config;
	}

	/**
	 *
	 * @param port the local TCP port
	 * @return new server instance
	 * @throws ConnectionLtException if an exception was raised and is related with
	 *                               the connection of the server, that exception
	 *                               should be put in the cause of the
	 *                               ConnectionLtException
	 */
	public final L serverInstanciation(final int port) throws ConnectionLtException {
		return serverInstanciation(port, 0, (InetAddress) null);
	}

	/**
	 *
	 * @param port    the local TCP port
	 * @param backlog the listen backlog
	 * @return new server instance
	 * @throws ConnectionLtException if an exception was raised and is related with
	 *                               the connection of the server, that exception
	 *                               should be put in the cause of the
	 *                               ConnectionLtException
	 */
	public final L serverInstanciation(final int port, final int backlog) throws ConnectionLtException {
		return serverInstanciation(port, backlog, null);
	}

	/**
	 *
	 * @param port     the local TCP port
	 * @param backlog  the listen backlog
	 * @param bindAddr the local InetAddress the server will bind to
	 * @return new server instance
	 * @throws ConnectionLtException if an exception was raised and is related with
	 *                               the connection of the server, that exception
	 *                               should be put in the cause of the
	 *                               ConnectionLtException
	 */
	public abstract L serverInstanciation(int port, int backlog, InetAddress bindAddr) throws ConnectionLtException;

	/**
	 *
	 * @param endpoint the SocketAddress
	 * @return new client instance
	 * @throws ConnectionLtException if an exception was raised and is related with
	 *                               the connection of the client, that exception
	 *                               should be put in the cause of the
	 *                               ConnectionLtException
	 */
	public final G clientInstanciation(final SocketAddress endpoint) throws ConnectionLtException {
		return clientInstanciation(endpoint, (Charset) null);
	}

	/**
	 *
	 * @param endpoint the SocketAddress
	 * @param charset  supported
	 * @return new client instance
	 * @throws ConnectionLtException if an exception was raised and is related with
	 *                               the connection of the client, that exception
	 *                               should be put in the cause of the
	 *                               ConnectionLtException
	 */
	public abstract G clientInstanciation(SocketAddress endpoint, Charset charset) throws ConnectionLtException;

	/**
	 *
	 * @return configuration
	 */
	final Config getConfig() {
		return this.config;
	}

}

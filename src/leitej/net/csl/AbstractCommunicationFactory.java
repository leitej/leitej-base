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

import leitej.exception.ConnectionLtException;

/**
 * Factory - Communication Session Layer
 *
 * @author Julio Leite
 */
public abstract class AbstractCommunicationFactory<F extends AbstractCommunicationFactory<F, L, H, G>, L extends AbstractCommunicationListener<F, L, H, G>, H extends AbstractCommunicationSession<F, L, H, G>, G extends AbstractCommunicationSession<F, L, H, G>> {

	private final int velocity;
	private final int sizePerSentence;
	private final int msTimeOut;

	/**
	 *
	 * @param velocity        byte per second (0 infinite)
	 * @param sizePerSentence number of bytes per read step (0 infinite)
	 * @param msTimeOut       the specified timeout, in milliseconds (0 infinite)
	 */
	public AbstractCommunicationFactory(final int velocity, final int sizePerSentence, final int msTimeOut) {
		this.velocity = velocity;
		this.sizePerSentence = sizePerSentence;
		this.msTimeOut = msTimeOut;
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
		return clientInstanciation(endpoint, (String) null);
	}

	/**
	 *
	 * @param endpoint    the SocketAddress
	 * @param charsetName the name of a supported {@link java.nio.charset.Charset
	 *                    charset}
	 * @return new client instance
	 * @throws ConnectionLtException if an exception was raised and is related with
	 *                               the connection of the client, that exception
	 *                               should be put in the cause of the
	 *                               ConnectionLtException
	 */
	public abstract G clientInstanciation(SocketAddress endpoint, String charsetName) throws ConnectionLtException;

	/**
	 *
	 * @return byte per second (0 infinite)
	 */
	final int getVelocity() {
		return this.velocity;
	}

	/**
	 *
	 * @return number of bytes per read step (0 infinite)
	 */
	final int getSizePerSentence() {
		return this.sizePerSentence;
	}

	/**
	 *
	 * @return the specified timeout, in milliseconds (0 infinite)
	 */
	final int getMsTimeOut() {
		return this.msTimeOut;
	}

}

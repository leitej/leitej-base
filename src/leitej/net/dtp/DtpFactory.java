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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import leitej.net.ConstantNet;
import leitej.net.csl.AbstractCommunicationFactory;
import leitej.net.exception.ConnectionLtException;
import leitej.thread.PoolAgnosticThread;
import leitej.xml.om.XmlomIOStream;

/**
 * Data Transport Protocol (DTP)
 *
 * Factory
 *
 * @author Julio Leite
 */
public final class DtpFactory<H extends AbstractDtpHandler> {

	static {
		XmlomIOStream.registry(InternalMessage.class);
	}

	private final AbstractCommunicationFactory<?, ?, ?, ?> comFactory;
	private final Class<H> handlerClass;
	private final PoolAgnosticThread handlerThreadPool;
	private final RawDataListenerMap rawDataListenerMap;
	private final PoolAgnosticThread rawDataThreadPool;

	/**
	 *
	 * @param comFactory   settings to apply at communication session
	 * @param handlerClass class to handle the communication
	 */
	public DtpFactory(final AbstractCommunicationFactory<?, ?, ?, ?> comFactory, final Class<H> handlerClass) {
		this(comFactory, handlerClass, ConstantNet.DEFAULT_DTP_MAX_HANDLER_THREADS,
				ConstantNet.RAW_DATA_MAX_LISTENERS_PER_BIND_ADDR, ConstantNet.RAW_DATA_MAX_LISTENER_THREADS);
	}

	/**
	 *
	 * @param comFactory              settings to apply at communication session
	 * @param handlerClass            class to handle the communication
	 * @param maxHandlerThreads       maximum number of threads in parallel process
	 *                                to respond at handler
	 * @param maxListenersPerBindAddr maximum number of listener ports opened per
	 *                                bind address to receive raw data
	 * @param maxListenerThreads      maximum number of threads in parallel process
	 *                                to process raw data
	 */
	public DtpFactory(final AbstractCommunicationFactory<?, ?, ?, ?> comFactory, final Class<H> handlerClass,
			final int maxHandlerThreads, final int maxListenersPerBindAddr, final int maxListenerThreads) {
		this.comFactory = comFactory;
		this.handlerClass = handlerClass;
		this.handlerThreadPool = PoolAgnosticThread.newInstance(0, maxHandlerThreads);
		this.rawDataListenerMap = new RawDataListenerMap(maxListenersPerBindAddr);
		this.rawDataThreadPool = PoolAgnosticThread.newInstance(0, maxListenerThreads);
	}

	/**
	 *
	 * @return new server instance
	 * @throws ConnectionLtException if an exception was raised and is related with
	 *                               the connection of the server, that exception
	 *                               should be put in the cause of the
	 *                               ConnectionLtException
	 */
	public final DtpServer serverInstanciation() throws ConnectionLtException {
		return serverInstanciation(ConstantNet.DEFAULT_DTP_PORT, ConstantNet.DEFAULT_DTP_BACKLOG,
				ConstantNet.DEFAULT_DTP_BIND_ADDR);
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
	public DtpServer serverInstanciation(final int port, final int backlog, final InetAddress bindAddr)
			throws ConnectionLtException {
		return new DtpServer(this.comFactory.serverInstanciation(port, backlog, bindAddr), this.handlerClass,
				this.handlerThreadPool, this.rawDataListenerMap.get(bindAddr), this.rawDataThreadPool);
	}

	/**
	 *
	 * @param host the Host name
	 * @return new client instance
	 * @throws IllegalArgumentException if the hostname parameter is
	 *                                  <code>null</code>.
	 * @throws SecurityException        if a security manager is present and
	 *                                  permission to resolve the host name is
	 *                                  denied.
	 * @throws ConnectionLtException    if an exception was raised and is related
	 *                                  with the connection of the client, that
	 *                                  exception should be in the cause of the
	 *                                  ConnectionLtException
	 */
	public final DtpClient clientInstanciation(final String host)
			throws ConnectionLtException, IllegalArgumentException, SecurityException {
		return clientInstanciation(new InetSocketAddress(host, ConstantNet.DEFAULT_DTP_PORT),
				ConstantNet.DEFAULT_DTP_CHARSET_NAME);
	}

	/**
	 *
	 * @param host the Host name
	 * @param port the port number
	 * @return new client instance
	 * @throws IllegalArgumentException if the port parameter is outside the range
	 *                                  of valid port values, or if the hostname
	 *                                  parameter is <code>null</code>.
	 * @throws SecurityException        if a security manager is present and
	 *                                  permission to resolve the host name is
	 *                                  denied.
	 * @throws ConnectionLtException    if an exception was raised and is related
	 *                                  with the connection of the client, that
	 *                                  exception should be in the cause of the
	 *                                  ConnectionLtException
	 */
	public final DtpClient clientInstanciation(final String host, final int port)
			throws ConnectionLtException, IllegalArgumentException, SecurityException {
		return clientInstanciation(new InetSocketAddress(host, port), ConstantNet.DEFAULT_DTP_CHARSET_NAME);
	}

	/**
	 *
	 * @param endpoint the SocketAddress
	 * @return new client instance
	 * @throws ConnectionLtException if an exception was raised and is related with
	 *                               the connection of the client, that exception
	 *                               should be in the cause of the
	 *                               ConnectionLtException
	 */
	public final DtpClient clientInstanciation(final SocketAddress endpoint) throws ConnectionLtException {
		return clientInstanciation(endpoint, ConstantNet.DEFAULT_DTP_CHARSET_NAME);
	}

	/**
	 *
	 * @param endpoint    the SocketAddress
	 * @param charsetName the name of a supported {@link java.nio.charset.Charset
	 *                    charset}
	 * @return new client instance
	 * @throws ConnectionLtException if an exception was raised and is related with
	 *                               the connection of the client, that exception
	 *                               should be in the cause of the
	 *                               ConnectionLtException
	 */
	public DtpClient clientInstanciation(final SocketAddress endpoint, final String charsetName)
			throws ConnectionLtException {
		return new DtpClient(this.comFactory.clientInstanciation(endpoint, charsetName), this.rawDataThreadPool);
	}

	// TODO: close to shutdown hook close rawDataListenerMap rawDataThreadPool
	// handlerThreadPool

}

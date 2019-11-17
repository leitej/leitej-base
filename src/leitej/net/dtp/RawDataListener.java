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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import leitej.exception.ImplementationLtRtException;
import leitej.log.Logger;
import leitej.net.ConstantNet;
import leitej.util.BinaryUtil;
import leitej.util.stream.StreamUtil;

/**
 *
 * @author Julio Leite
 */
final class RawDataListener {

	private static final Logger LOG = Logger.getInstance();

	private final ServerSocket server;
	private final RawDataListenerBindedPool myPool;
	private volatile boolean handling;
	private Socket socket;

	RawDataListener(final RawDataListenerBindedPool pool, final InetAddress bindAddr) throws IOException {
		ServerSocket server = null;
		while (server == null) {
			try {
				server = new ServerSocket(0, ConstantNet.RAW_DATA_LISTENER_BACKLOG, bindAddr);
				server.setSoTimeout(ConstantNet.RAW_DATA_LISTENER_TIME_OUT);
				LOG.trace("#0", server.getLocalPort());
			} catch (final BindException e) {
				LOG.trace("#0", e);
			}
		}
		this.server = server;
		this.myPool = pool;
		this.handling = false;
	}

	void handleSend(final RawData in, final long callNumber) throws SocketTimeoutException, IOException {
		LOG.trace("lt.Init");
		if (this.handling) {
			throw new ImplementationLtRtException();
		}
		try {
			this.handling = true;
			final Socket socket = this.server.accept();
			final byte[] tmp = new byte[8];
			if (socket.getInputStream().read(tmp) < 8 || callNumber != BinaryUtil.readLong64bit(tmp)) {
				socket.close();
				throw new IOException();
			}
			final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
			StreamUtil.pipe(in, out, true);
			out.flush();
			out.close();
			socket.close();
		} finally {
			this.handling = false;
			try {
				this.myPool.offer(this);
			} catch (final InterruptedException e) {
				LOG.error("#0", e);
				close();
			}
		}
	}

	void setReceiveInputStream(final RawData in, final long callNumber) throws SocketTimeoutException, IOException {
		LOG.trace("lt.Init");
		if (this.handling) {
			throw new ImplementationLtRtException();
		}
		boolean success = false;
		try {
			this.handling = true;
			this.socket = this.server.accept();
			this.socket.setSoTimeout(ConstantNet.RAW_DATA_SOCKET_TIME_OUT);
			final byte[] tmp = new byte[8];
			BinaryUtil.writeLong64bit(tmp, callNumber);
			this.socket.getOutputStream().write(tmp);
			this.socket.getOutputStream().flush();
			in.setInputStream(this);
			success = true;
		} finally {
			if (!success) {
				endReceiveInputStream();
			}
		}
	}

	final Socket getSocket() {
		return this.socket;
	}

	void endReceiveInputStream() {
		try {
			if (this.socket != null) {
				this.socket.close();
			}
		} catch (final IOException e) {
			LOG.error("#0", e);
		}
		this.handling = false;
		try {
			this.myPool.offer(this);
		} catch (final InterruptedException e) {
			LOG.error("#0", e);
			close();
		}
	}

	boolean isInactive() {
		return this.server.isClosed();
	}

	int getPort() {
		return this.server.getLocalPort();
	}

	void close() {
		try {
			this.server.close();
		} catch (final IOException e) {
			LOG.error("#0", e);
		}
		this.myPool.offerInactive(this);
	}

}

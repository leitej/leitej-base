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
import java.net.InetAddress;
import java.net.Socket;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.PoolAgnosticThreadLtException;
import leitej.log.Logger;
import leitej.net.ConstantNet;
import leitej.thread.PoolAgnosticThread;
import leitej.thread.XThreadData;
import leitej.util.AgnosticUtil;
import leitej.util.BinaryUtil;
import leitej.util.data.InvokeSignature;
import leitej.util.stream.StreamUtil;

/**
 *
 * @author Julio Leite
 */
public final class RawDataClient {

	private static final Logger LOG = Logger.getInstance();

	private static final InvokeSignature INVOKE_SIGN_HANDLE_SEND;
	private static final InvokeSignature INVOKE_SIGN_SET_RECEIVE_INPUT_STREAM;

	static final void handleSendAsync(final PoolAgnosticThread rawDataThreadPool, final InetAddress address,
			final int port, final RawData in, final long callNumber) {
		try {
			rawDataThreadPool.workOn(new XThreadData(INVOKE_SIGN_HANDLE_SEND.getInvoke(address, port, in, callNumber)));
		} catch (final PoolAgnosticThreadLtException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	private static final String METHOD_NAME_HANDLE_SEND = "handleSend";

	public static void handleSend(final InetAddress address, final int port, final RawData in, final long callNumber) {
		LOG.trace("initialized");
		// TODO: if(!PoolAgnosticThread.isCurrentThreadFrom(rawDataThreadPool)) throw
		// new ImplementationLtRtException("This method only can be called by abstract class AbstractHandler");
		try {
			final Socket socket = new Socket(address, port);
			final byte[] tmp = new byte[8];
			if (socket.getInputStream().read(tmp) < 8 || callNumber != BinaryUtil.readLong64bit(tmp)) {
				socket.close();
				throw new IOException();
			}
			BufferedOutputStream out = null;
			try {
				out = new BufferedOutputStream(socket.getOutputStream());
				StreamUtil.pipe(in, out, true);
				out.flush();
			} finally {
				if (out != null) {
					out.close();
				}
				socket.close();
			}
		} catch (final Exception e) {
			LOG.error("#0", e);
		}
	}

	static {
		try {
			INVOKE_SIGN_HANDLE_SEND = new InvokeSignature(RawDataClient.class,
					AgnosticUtil.getMethod(RawDataClient.class, METHOD_NAME_HANDLE_SEND, InetAddress.class, int.class,
							RawData.class, long.class));
		} catch (final IllegalArgumentLtRtException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	static final void setReceiveInputStreamAsync(final PoolAgnosticThread rawDataThreadPool, final InetAddress address,
			final int port, final RawData in, final long callNumber) {
		try {
			rawDataThreadPool.workOn(
					new XThreadData(INVOKE_SIGN_SET_RECEIVE_INPUT_STREAM.getInvoke(address, port, in, callNumber)));
		} catch (final PoolAgnosticThreadLtException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	private static final String METHOD_NAME_SET_RECEIVE_INPUT_STREAM = "setReceiveInputStream";

	public static void setReceiveInputStream(final InetAddress address, final int port, final RawData in,
			final long callNumber) {
		LOG.trace("initialized");
		// TODO: if(!PoolAgnosticThread.isCurrentThreadFrom(rawDataThreadPool)) throw
		// new ImplementationLtRtException("This method only can be called by abstract class AbstractHandler");
		try {
			final byte[] tmp = new byte[8];
			final Socket socket = new Socket(address, port);
			socket.setSoTimeout(ConstantNet.RAW_DATA_SOCKET_TIME_OUT);
			BinaryUtil.writeLong64bit(tmp, callNumber);
			socket.getOutputStream().write(tmp);
			socket.getOutputStream().flush();
			in.setInputStream(socket);
		} catch (final Exception e) {
			LOG.error("#0", e);
		}
	}

	static {
		try {
			INVOKE_SIGN_SET_RECEIVE_INPUT_STREAM = new InvokeSignature(RawDataClient.class,
					AgnosticUtil.getMethod(RawDataClient.class, METHOD_NAME_SET_RECEIVE_INPUT_STREAM, InetAddress.class,
							int.class, RawData.class, long.class));
		} catch (final IllegalArgumentLtRtException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	private RawDataClient() {
	};

}

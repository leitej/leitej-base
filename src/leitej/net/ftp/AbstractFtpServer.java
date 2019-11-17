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

package leitej.net.ftp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import leitej.exception.AgnosticThreadLtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.log.Logger;
import leitej.thread.AgnosticThread;
import leitej.thread.PoolAgnosticThread;
import leitej.thread.ThreadData;
import leitej.thread.ThreadPriorityEnum;
import leitej.util.AgnosticUtil;
import leitej.util.data.Invoke;
import leitej.util.data.InvokeItf;
import leitej.util.machine.ShutdownHookUtil;

/**
 * <p>
 * The constructor will put a call to
 * {@link leitej.net.ftp.AbstractFtpServer#close() close()} in the
 * {@link leitej.util.machine.ShutdownHookUtil#addToFirst(InvokeItf)
 * ShutdownHookUtil.addToFirst(InvokeItf)}.
 * </p>
 *
 * @author Julio Leite
 */
public abstract class AbstractFtpServer<H extends AbstractFtpServerHandler> {

	private static final Logger LOG = Logger.getInstance();
	private static final PoolAgnosticThread POOL_ATHREAD = PoolAgnosticThread.newInstance(0, 230);

	private final ServerSocket serverSocket;
	private final AgnosticThread aThread = new AgnosticThread();
	private final List<H> handlers = new ArrayList<>();

	private volatile boolean closed = false;
	private final InvokeItf closeAtShutdownInvoke;

	/**
	 * .<br/>
	 * <br/>
	 * A call to {@link leitej.net.ftp.AbstractFtpServer#close() close()} is put in
	 * the {@link leitej.util.machine.ShutdownHookUtil#addToFirst(InvokeItf)
	 * ShutdownHookUtil.addToFirst(InvokeItf)}.
	 *
	 * @throws SecurityException if a security manager exists and its
	 *                           <code>checkListen</code> method doesn't allow the
	 *                           operation
	 * @throws IOException       if an I/O error occurs when opening the server
	 *                           socket
	 */
	protected AbstractFtpServer() throws IOException, SecurityException {
		this(ConstantFtp.DEFAULT_PORT, ConstantFtp.DEFAULT_BACKLOG, ConstantFtp.DEFAULT_BIND_ADDR);
	}

	/**
	 * .<br/>
	 * <br/>
	 * A call to {@link leitej.net.ftp.AbstractFtpServer#close() close()} is put in
	 * the {@link leitej.util.machine.ShutdownHookUtil#addToFirst(InvokeItf)
	 * ShutdownHookUtil.addToFirst(InvokeItf)}.
	 *
	 * @param port
	 * @param backlog
	 * @param bindAddr
	 * @throws SecurityException if a security manager exists and its
	 *                           <code>checkListen</code> method doesn't allow the
	 *                           operation
	 * @throws IOException       if an I/O error occurs when opening the server
	 *                           socket
	 */
	protected AbstractFtpServer(final int port, final int backlog, final InetAddress bindAddr)
			throws IOException, SecurityException {
		this.serverSocket = new ServerSocket(port, backlog, bindAddr);
		this.aThread.setName(ConstantFtp.FTP_SERVER_THREAD_NAME + this.aThread.getId());
		try {
			this.closeAtShutdownInvoke = new Invoke(this, AgnosticUtil.getMethod(this, METHOD_CLOSE));
			ShutdownHookUtil.addToFirst(this.closeAtShutdownInvoke);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	protected abstract H newHandler();

	private static final String METHOD_START_SERVER = "startServer";

	public synchronized final void startServer() {
		if (!this.closed && Thread.currentThread().getId() == this.aThread.getId()) {
			LOG.info("lt.FTPStart", this.serverSocket.getLocalSocketAddress());
			H handler;
			try {
				while (!this.closed) {
					handler = newHandler();
					handler.startAsync(POOL_ATHREAD, this, this.serverSocket.accept());
					synchronized (this.handlers) {
						this.handlers.add(handler);
					}
				}
			} catch (final SocketException e) {
				if (this.closed) {
					LOG.info("#0", e.toString());
				} else {
					LOG.error("#0", e);
				}
			} catch (final Exception e) {
				LOG.error("#0", e);
			} catch (final Throwable e) {
				e.printStackTrace();
			} finally {
				if (!this.closed) {
					close();
				}
			}
		} else {
			if (this.closed) {
				LOG.warn("lt.FTPAlreadyClosed");
			} else {
				if (Thread.currentThread().getId() != this.aThread.getId() && !this.aThread.isAlive()) {
					try {
						final ThreadData td = new ThreadData(
								new Invoke(this, AgnosticUtil.getMethod(this, METHOD_START_SERVER)),
								ThreadPriorityEnum.MAXIMUM);
						this.aThread.workOn(td);
					} catch (final AgnosticThreadLtException e) {
						throw new ImplementationLtRtException(e);
					} catch (final NoSuchMethodException e) {
						throw new ImplementationLtRtException(e);
					}
				}
			}
		}
	}

	final void endHandler(final AbstractFtpServerHandler handler) {
		synchronized (this.handlers) {
			this.handlers.remove(handler);
		}
	}

	public final boolean isClosed() {
		return this.closed;
	}

	private static final String METHOD_CLOSE = "close";

	/**
	 * Closes the FTP server.
	 */
	public final void close() {
		if (!this.closed) {
			this.closed = true;
			removeCloseAsyncInvokeFromShutdownHook();
		}
		if (!this.serverSocket.isClosed()) {
			LOG.debug("lt.FTPClosing");
			try {
				this.serverSocket.close();
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
		}
		try {
			if (!Thread.currentThread().equals(this.aThread)) {
				this.aThread.join();
			}
		} catch (final InterruptedException e) {
			LOG.error("#0", e);
		}
		int size;
		H handler = null;
		do {
			synchronized (this.handlers) {
				size = this.handlers.size();
				if (size > 0) {
					handler = this.handlers.remove(this.handlers.size() - 1);
					handler.close();
				} else {
					handler = null;
				}
			}
		} while (handler != null);
	}

	private void removeCloseAsyncInvokeFromShutdownHook() {
		if (this.closeAtShutdownInvoke != null && !ShutdownHookUtil.isActive()) {
			try {
				ShutdownHookUtil.remove(this.closeAtShutdownInvoke);
			} catch (final IllegalStateLtRtException e) {
				LOG.trace("#0", e);
			}
		}
	}

}

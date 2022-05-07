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

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import leitej.exception.AgnosticThreadLtException;
import leitej.exception.ConnectionLtException;
import leitej.exception.DataOverflowLtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.log.Logger;
import leitej.net.ConstantNet;
import leitej.net.csl.AbstractCommunicationListener;
import leitej.thread.AgnosticThread;
import leitej.thread.PoolAgnosticThread;
import leitej.thread.ThreadData;
import leitej.thread.ThreadPriorityEnum;
import leitej.util.AgnosticUtil;
import leitej.util.data.Invoke;
import leitej.util.data.InvokeItf;
import leitej.util.machine.ShutdownHookUtil;

/**
 * Data Transport Protocol (DTP) <br/>
 * Server
 *
 * <p>
 * The constructor will put a call to {@link leitej.net.dtp.DtpServer#close()
 * close()} in the
 * {@link leitej.util.machine.ShutdownHookUtil#addToFirst(InvokeItf)
 * ShutdownHookUtil.addToFirst(InvokeItf)}.
 * </p>
 *
 * @author Julio Leite
 */
public final class DtpServer {

	private static final Logger LOG = Logger.getInstance();

	private final PoolAgnosticThread handlerThreadPool;
	private final RawDataListenerBindedPool rawDataListenerBindedPool;
	private final PoolAgnosticThread rawDataThreadPool;
	private final AbstractCommunicationListener<?, ?, ?, ?> comServer;
	private final Class<?> handlerClass;
	private final AgnosticThread aThread = new AgnosticThread();
	private final List<AbstractDtpHandler> handlers = new ArrayList<>();

	private volatile boolean closed = false;
	private final InvokeItf closeAtShutdownInvoke;

	/**
	 * .<br/>
	 * <br/>
	 * A call to {@link leitej.net.dtp.DtpServer#close() close()} is put in the
	 * {@link leitej.util.machine.ShutdownHookUtil#addToFirst(InvokeItf)
	 * ShutdownHookUtil.addToFirst(InvokeItf)}.
	 *
	 * @param handlerThreadPool
	 * @param comServer
	 * @param handlerClass
	 */
	<H extends AbstractDtpHandler> DtpServer(final AbstractCommunicationListener<?, ?, ?, ?> comServer,
			final Class<H> handlerClass, final PoolAgnosticThread handlerThreadPool,
			final RawDataListenerBindedPool rawDataListenerBindedPool, final PoolAgnosticThread rawDataThreadPool) {
		if (handlerClass == null) {
			throw new ImplementationLtRtException();
		}
		this.handlerThreadPool = handlerThreadPool;
		this.rawDataListenerBindedPool = rawDataListenerBindedPool;
		this.rawDataThreadPool = rawDataThreadPool;
		this.aThread.setName(ConstantNet.DTP_SERVER_THREAD_NAME + this.aThread.getId());
		try {
			this.closeAtShutdownInvoke = new Invoke(this, AgnosticUtil.getMethod(this, METHOD_CLOSE));
			ShutdownHookUtil.addToFirst(this.closeAtShutdownInvoke);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		}
		this.comServer = comServer;
		this.handlerClass = handlerClass;
	}

	private static final String METHOD_START_SERVER = "startServer";

	/**
	 * Starts the listener in asynchronous mode.
	 */
	public final void startServer() {
		if (Thread.currentThread().getId() == this.aThread.getId()) {
			if (!this.closed) {
				LOG.info("Start server at #0", this.comServer.getLocalSocketAddress());
				AbstractDtpHandler handler;
				try {
					while (!this.closed) {
						try {
							handler = (AbstractDtpHandler) this.handlerClass.getDeclaredConstructor().newInstance();
							synchronized (handler) {
								handler.startAsync(this.comServer.accept(), this);
								synchronized (this.handlers) {
									this.handlers.add(handler);
								}
							}
							LOG.trace("client received");
						} catch (final IOException e) {
							if (DataOverflowLtException.class.isInstance(e.getCause())) {
								LOG.warn("#0", e);
							} else {
								throw e;
							}
						}
					}
				} catch (final InstantiationException e) {
					LOG.error("#0", e);
				} catch (final IllegalAccessException e) {
					LOG.error("#0", e);
				} catch (final SocketException e) {
					if (this.closed) {
						LOG.info("#0", e.toString());
					} else {
						LOG.error("#0", e);
					}
				} catch (final Exception e) {
					LOG.error("#0", e);
				} finally {
					if (!this.closed) {
						close();
					}
				}
			} else {
				if (this.closed) {
					LOG.warn("Already closed");
				}
			}
		} else {
			synchronized (this) {
				if (!this.aThread.isAlive()) {
					try {
						final ThreadData td = new ThreadData(
								new Invoke(this, AgnosticUtil.getMethod(this, METHOD_START_SERVER)),
								ThreadPriorityEnum.MAXIMUM);
						this.aThread.workOn(td);
					} catch (final AgnosticThreadLtException e) {
						new ImplementationLtRtException(e);
					} catch (final SecurityException e) {
						new ImplementationLtRtException(e);
					} catch (final NoSuchMethodException e) {
						new ImplementationLtRtException(e);
					}
				}
			}
		}
	}

	final void endHandler(final AbstractDtpHandler handler) {
		synchronized (handler) {
			synchronized (this.handlers) {
				this.handlers.remove(handler);
			}
		}
	}

	/**
	 *
	 * @return a <code>SocketAddress</code> representing the local endpoint, or
	 *         <code>null</code> if it is not bound yet
	 */
	public final SocketAddress getLocalSocketAddress() {
		return this.comServer.getLocalSocketAddress();
	}

	public final boolean isClosed() {
		return this.closed;
	}

	private static final String METHOD_CLOSE = "close";

	public final void close() {
		if (!this.closed) {
			this.closed = true;
			removeCloseAsyncInvokeFromShutdownHook();
		}
		if (!this.comServer.isClosed()) {
			LOG.debug("Closing DTP server");
			try {
				this.comServer.close();
			} catch (final ConnectionLtException e) {
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
		AbstractDtpHandler handler = null;
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

	final PoolAgnosticThread getHandlerThreadPool() {
		return this.handlerThreadPool;
	}

	final RawDataListenerBindedPool getRawDataListenerBindedPool() {
		return this.rawDataListenerBindedPool;
	}

	final PoolAgnosticThread getRawDataThreadPool() {
		return this.rawDataThreadPool;
	}

}

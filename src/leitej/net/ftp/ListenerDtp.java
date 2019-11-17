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
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import leitej.exception.ImplementationLtRtException;
import leitej.exception.PoolAgnosticThreadLtException;
import leitej.log.Logger;
import leitej.thread.PoolAgnosticThread;
import leitej.thread.XThreadData;
import leitej.util.AgnosticUtil;
import leitej.util.DateUtil;
import leitej.util.data.Invoke;

/**
 *
 * @author Julio Leite
 */
public final class ListenerDtp {

	private static final Logger LOG = Logger.getInstance();

	private static final PoolAgnosticThread THREAD_POOL = PoolAgnosticThread.newInstance(0, 64);
	private static final int ACCEPT_TIME_OUT = (int) (2 * DateUtil.ONE_MINUTE_IN_MS); // 2 minutes
	private static volatile int portSeq = 8192;

	private final ServerSocket server;
	private final ListenDtpPool myPool;

	private volatile Boolean accepting;
	private Socket socket;
	private SessionData session;
	private int numCall;

	ListenerDtp(final ListenDtpPool pool, final InetAddress bindAddr) throws IOException {
		ServerSocket server = null;
		while (server == null) {
			try {
				server = new ServerSocket(portSeq++, 0, bindAddr);
			} catch (final BindException e) {
				LOG.trace("#0", e);
			}
		}
		this.server = server;
		this.server.setSoTimeout(ACCEPT_TIME_OUT);
		this.myPool = pool;
		this.accepting = null;
		this.socket = null;
		this.session = null;
		this.numCall = 0;
	}

	boolean isInactive() {
		return this.server.isClosed();
	}

	synchronized void close() {
		if (this.server != null) {
			try {
				this.server.close();
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
			this.myPool.offerInactive(this);
		}
	}

	synchronized int acceptAssync(final SessionData forSession) {
		if (this.accepting != null) {
			throw new ImplementationLtRtException();
		}
		this.session = forSession;
		this.numCall++;
		try {
			THREAD_POOL.workOn(new XThreadData(new Invoke(this, AgnosticUtil.getMethod(this, METHOD_LISTEN))));
		} catch (final PoolAgnosticThreadLtException e1) {
			throw new ImplementationLtRtException(e1);
		} catch (final NoSuchMethodException e1) {
			throw new ImplementationLtRtException(e1);
		}
		while (this.accepting == null) {
			try {
				Thread.sleep(500);
			} catch (final InterruptedException e) {
				LOG.debug("#0", e);
			}
		}
		return this.numCall;
	}

	private static final String METHOD_LISTEN = "listen";

	public void listen() {
		if (!PoolAgnosticThread.isCurrentThreadFrom(THREAD_POOL)) {
			throw new ImplementationLtRtException();
		}
		try {
			this.accepting = Boolean.TRUE;
			this.socket = this.server.accept();
			this.accepting = Boolean.FALSE;
		} catch (final SocketTimeoutException e) {
			LOG.warn("#0", e);
			this.accepting = null;
			try {
				this.myPool.offer(this);
			} catch (final InterruptedException e1) {
				LOG.error("#0", e1);
				close();
			}
		} catch (final IOException e) {
			LOG.error("#0", e);
			this.accepting = null;
			close();
		}
	}

	synchronized Socket getCorrespondent(final SessionData forSession, final int callNumber) {
		if (callNumber != this.numCall || !forSession.equals(this.session)) {
			return null;
		}
		while (this.accepting != null && this.accepting.booleanValue()) {
			try {
				Thread.sleep(500);
			} catch (final InterruptedException e) {
				LOG.debug("#0", e);
			}
		}
		if (this.accepting == null) {
			return null;
		}
		final Socket result = this.socket;
		this.socket = null;
		this.accepting = null;
		try {
			this.myPool.offer(this);
		} catch (final InterruptedException e) {
			LOG.error("#0", e);
			close();
			return null;
		}
		return result;
	}

	public int getPort() {
		return this.server.getLocalPort();
	}

}

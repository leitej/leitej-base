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
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.List;

import leitej.exception.ClosedLtRtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.LtException;
import leitej.exception.PoolAgnosticThreadLtException;
import leitej.log.Logger;
import leitej.net.ConstantNet;
import leitej.net.csl.AbstractCommunicationSession;
import leitej.net.exception.ConnectionLtException;
import leitej.thread.PoolAgnosticThread;
import leitej.thread.XThreadData;
import leitej.util.AgnosticUtil;
import leitej.util.DateUtil;
import leitej.util.data.Invoke;
import leitej.util.data.XmlomUtil;
import leitej.xml.om.XmlObjectModelling;

/**
 * Data Transport Protocol (DTP)
 *
 * Handler - abstract
 *
 * @author Julio Leite
 */
public abstract class AbstractDtpHandler {

	private static final Logger LOG = Logger.getInstance();

	private static final Method METHOD_SET_RECEIVE_INPUT_STREAM;
	private static final Method METHOD_HANDLE_SEND_RAW_DATA;

	private DtpServer server;
	private AbstractCommunicationSession<?, ?, ?, ?> comSession;
	private volatile boolean closed;

	/**
	 * Handles the business logic.
	 *
	 * @param request of the remote correspondent
	 * @return response to be sent to the remote correspondent
	 */
	protected abstract XmlObjectModelling responder(XmlObjectModelling request);

	/**
	 * Takes the decision of release the arguments.<br />
	 * Is invoked after process and send the result of responder method.
	 *
	 * @param request  of the remote correspondent
	 * @param response result of responder method
	 */
	protected abstract void releaseDeal(XmlObjectModelling request, XmlObjectModelling response);

	/**
	 * This method is invoked in finally block at the end of dealing process.
	 */
	protected abstract void finalizeHandler();

	final void startAsync(final AbstractCommunicationSession<?, ?, ?, ?> comSession, final DtpServer server) {
		if (this.server != null) {
			throw new ImplementationLtRtException();
		}
		this.server = server;
		this.comSession = comSession;
		this.closed = false;
		try {
			final XThreadData xtd = new XThreadData(new Invoke(this, AgnosticUtil.getMethod(this, METHOD_NAME_DEAL)),
					ConstantNet.DTP_HANDLER_THREAD_NAME + comSession.getRemoteSocketAddress());
			this.server.getHandlerThreadPool().workOn(xtd);
		} catch (final SecurityException e) {
			LOG.error("#0", e);
		} catch (final NoSuchMethodException e) {
			LOG.error("#0", e);
		} catch (final LtException e) {
			LOG.error("#0", e);
		}
	}

	protected final byte[] getRemoteAddress() {
		return ((InetSocketAddress) this.comSession.getRemoteSocketAddress()).getAddress().getAddress();
	}

	protected final boolean isRemoteAddressLoopbackAddress() {
		return ((InetSocketAddress) this.comSession.getRemoteSocketAddress()).getAddress().isLoopbackAddress();
	}

	private static final String METHOD_NAME_DEAL = "deal";

	public synchronized final void deal() {
		if (!PoolAgnosticThread.isCurrentThreadFrom(this.server.getHandlerThreadPool())) {
			throw new ImplementationLtRtException("lt.DTPWrongCall");
		}
		try {
			XmlObjectModelling request = null;
			boolean requestReceived = false;
			InternalMessage dtpIM = null;
			List<RawData> rawDataList;
			RawDataListener rawDataListener;
			RawDataPort[] rawDataPorts;
			while (!this.closed) {
				try {
					RawData.initRequest();
					request = this.comSession.read(XmlObjectModelling.class);
					requestReceived = true;
					rawDataList = RawData.endRequest();
					if (rawDataList.size() > 0) {
						dtpIM = XmlomUtil.newXmlObjectModelling(InternalMessage.class);
						dtpIM.setAction(InternalMessageAction.RAW_DATA_PORTS);
						rawDataPorts = new RawDataPort[rawDataList.size()];
						for (int i = 0; i < rawDataPorts.length; i++) {
							rawDataPorts[i] = XmlomUtil.newXmlObjectModelling(RawDataPort.class);
							rawDataPorts[i].setCallNumber(DateUtil.generateUniqueNumberPerJVM());
							rawDataPorts[i].setId(rawDataList.get(i).getId());
							rawDataListener = this.server.getRawDataListenerBindedPool().poll();
							setReceiveInputStreamAsync(rawDataListener, rawDataList.get(i),
									rawDataPorts[i].getCallNumber());
							rawDataPorts[i].setPort(rawDataListener.getPort());
						}
						dtpIM.setRawDataPorts(rawDataPorts);
					}
				} catch (final ConnectionLtException e) {
					if (this.closed
							|| (e.getCause() != null && SocketTimeoutException.class.equals(e.getCause().getClass()))) {
						throw new ClosedLtRtException(e);
					}
					requestReceived = false;
					LOG.warn("#0", e);
					dtpIM = XmlomUtil.newXmlObjectModelling(InternalMessage.class);
					dtpIM.setAction(InternalMessageAction.INTERNAL_ERROR);
					dtpIM.setMessage(e.getMessage());
				}
				synchronized (this.comSession) {
					if (dtpIM != null) {
						this.comSession.write(dtpIM);
						this.comSession.flush();
						dtpIM.release();
						dtpIM = null;
					}
					if (requestReceived) {
						if (request != null) {
							RawData.initResponse();
							final XmlObjectModelling response = responder(request);
							this.comSession.write(response);
							this.comSession.flush();
							rawDataList = RawData.endResponse();
							if (rawDataList.size() > 0) {
								dtpIM = XmlomUtil.newXmlObjectModelling(InternalMessage.class);
								dtpIM.setAction(InternalMessageAction.RAW_DATA_PORTS);
								rawDataPorts = new RawDataPort[rawDataList.size()];
								for (int i = 0; i < rawDataPorts.length; i++) {
									rawDataPorts[i] = XmlomUtil.newXmlObjectModelling(RawDataPort.class);
									rawDataPorts[i].setCallNumber(DateUtil.generateUniqueNumberPerJVM());
									rawDataPorts[i].setId(rawDataList.get(i).getId());
									rawDataListener = this.server.getRawDataListenerBindedPool().poll();
									handleSendAsync(rawDataListener, rawDataList.get(i),
											rawDataPorts[i].getCallNumber());
									rawDataPorts[i].setPort(rawDataListener.getPort());
								}
								dtpIM.setRawDataPorts(rawDataPorts);
								this.comSession.write(dtpIM);
								this.comSession.flush();
								dtpIM.release();
								dtpIM = null;
							}
							releaseDeal(request, response);
						} else {
							internalClose();
						}
					}
				}
			}
		} catch (final ClosedLtRtException e) {
			LOG.warn("#0", e);
		} catch (final ConnectionLtException e) {
			LOG.error("#0", e);
		} catch (final Exception e) {
			LOG.error("#0", e);
		} finally {
			try {
				internalClose();
			} catch (final ConnectionLtException e) {
				LOG.error("#0", e);
			} finally {
				finalizeHandler();
			}
		}
	}

	private final void setReceiveInputStreamAsync(final RawDataListener rawDataListener, final RawData rawData,
			final long callNumber) {
		LOG.trace("lt.Init");
		try {
			this.server.getRawDataThreadPool().workOn(new XThreadData(
					new Invoke(this, METHOD_SET_RECEIVE_INPUT_STREAM, rawDataListener, rawData, callNumber)));
		} catch (final PoolAgnosticThreadLtException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	private static final String METHOD_NAME_SET_RECEIVE_INPUT_STREAM = "setReceiveInputStream";

	public final void setReceiveInputStream(final RawDataListener rawDataListener, final RawData rawData,
			final long callNumber) {
		if (!PoolAgnosticThread.isCurrentThreadFrom(this.server.getRawDataThreadPool())) {
			throw new ImplementationLtRtException("lt.DTPWrongCall");
		}
		try {
			rawDataListener.setReceiveInputStream(rawData, callNumber);
		} catch (final SocketTimeoutException e) {
			LOG.error("#0", e);
		} catch (final IOException e) {
			LOG.error("#0", e);
		}
	}

	static {
		try {
			METHOD_SET_RECEIVE_INPUT_STREAM = AgnosticUtil.getMethod(AbstractDtpHandler.class,
					METHOD_NAME_SET_RECEIVE_INPUT_STREAM, RawDataListener.class, RawData.class, long.class);
		} catch (final IllegalArgumentLtRtException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	private final void handleSendAsync(final RawDataListener rawDataListener, final RawData rawData,
			final long callNumber) {
		LOG.trace("lt.Init");
		try {
			this.server.getRawDataThreadPool().workOn(new XThreadData(
					new Invoke(this, METHOD_HANDLE_SEND_RAW_DATA, rawDataListener, rawData, callNumber)));
		} catch (final PoolAgnosticThreadLtException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	private static final String METHOD_NAME_HANDLE_SEND_RAW_DATA = "handleSend";

	public final void handleSend(final RawDataListener rawDataListener, final RawData rawData, final long callNumber) {
		if (!PoolAgnosticThread.isCurrentThreadFrom(this.server.getRawDataThreadPool())) {
			throw new ImplementationLtRtException("lt.DTPWrongCall");
		}
		try {
			rawDataListener.handleSend(rawData, callNumber);
		} catch (final SocketTimeoutException e) {
			LOG.error("#0", e);
		} catch (final IOException e) {
			LOG.error("#0", e);
		}
	}

	static {
		try {
			METHOD_HANDLE_SEND_RAW_DATA = AgnosticUtil.getMethod(AbstractDtpHandler.class,
					METHOD_NAME_HANDLE_SEND_RAW_DATA, RawDataListener.class, RawData.class, long.class);
		} catch (final IllegalArgumentLtRtException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	private final void internalClose() throws ConnectionLtException {
		synchronized (this.comSession) {
			this.closed = true;
			this.server.endHandler(this);
			if (!this.comSession.isClosed()) {
				this.comSession.close();
			}
		}
	}

	final void close() {
		synchronized (this.comSession) {
			try {
				if (!this.comSession.isClosed()) {
					this.closed = true;
					this.comSession.close();
				}
			} catch (final ConnectionLtException e) {
				LOG.error("#0", e);
			}
		}
	}

}

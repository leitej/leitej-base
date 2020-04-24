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

import java.util.List;

import leitej.net.ConnectionClientItf;
import leitej.net.csl.AbstractCommunicationSession;
import leitej.net.exception.ConnectionLtException;
import leitej.net.exception.DtpLtException;
import leitej.thread.PoolAgnosticThread;
import leitej.xml.om.XmlObjectModelling;

/**
 * Data Transport Protocol (DTP) Client
 *
 * @author Julio Leite
 */
public final class DtpClient implements ConnectionClientItf {

	private final AbstractCommunicationSession<?, ?, ?, ?> comSession;
	private final PoolAgnosticThread rawDataThreadPool;

	<H extends AbstractDtpHandler> DtpClient(final AbstractCommunicationSession<?, ?, ?, ?> comSession,
			final PoolAgnosticThread rawDataThreadPool) {
		this.comSession = comSession;
		this.rawDataThreadPool = rawDataThreadPool;
	}

	@Override
	public final <I extends XmlObjectModelling> I getResponse(final Class<I> responseClass,
			final XmlObjectModelling request) throws ConnectionLtException {
		XmlObjectModelling result;
		InternalMessage internalMessage;
		if (!this.comSession.isConnected()) {
			throw new ConnectionLtException(new DtpLtException("Not connected"));
		}
		List<RawData> rawDataList;
		RawData.initRequest();
//		XmlomUtil.registry(responseClass);
		this.comSession.write(request);
		this.comSession.flush();
		rawDataList = RawData.endRequest();
		if (rawDataList.size() > 0) {
			internalMessage = this.comSession.read(InternalMessage.class);
			if (internalMessage == null || !InternalMessage.class.isInstance(internalMessage)
					|| !InternalMessageAction.RAW_DATA_PORTS
							.equals(InternalMessage.class.cast(internalMessage).getAction())) {
				throw new ConnectionLtException(new DtpLtException());
			}
			final RawDataPort[] rawDataPorts = InternalMessage.class.cast(internalMessage).getRawDataPorts();
			if (rawDataPorts == null || rawDataPorts.length != rawDataList.size()) {
				throw new ConnectionLtException(new DtpLtException());
			}
			RawDataPort rawDataPort;
			RawData rawData;
			for (int i = 0; i < rawDataPorts.length; i++) {
				rawDataPort = rawDataPorts[i];
				rawData = null;
				for (int j = 0; j < rawDataList.size() && rawData == null; j++) {
					if (rawDataList.get(j).getId() == rawDataPort.getId()) {
						rawData = rawDataList.get(j);
						RawDataClient.handleSendAsync(this.rawDataThreadPool, this.comSession.getRemoteInetAddress(),
								rawDataPort.getPort(), rawData, rawDataPort.getCallNumber());
					}
				}
			}
		}
		RawData.initResponse();
		result = this.comSession.read(XmlObjectModelling.class);
		rawDataList = RawData.endResponse();
		if (result != null) {
			if (InternalMessage.class.isInstance(result)) {
				throw new ConnectionLtException(new DtpLtException(
						"Connection remote response: " + InternalMessage.class.cast(result).getMessage()));
			}
			if (rawDataList.size() > 0) {
				internalMessage = this.comSession.read(InternalMessage.class);
				if (internalMessage == null || !InternalMessage.class.isInstance(internalMessage)
						|| !InternalMessageAction.RAW_DATA_PORTS
								.equals(InternalMessage.class.cast(internalMessage).getAction())) {
					throw new ConnectionLtException(new DtpLtException());
				}
				final RawDataPort[] rawDataPorts = InternalMessage.class.cast(internalMessage).getRawDataPorts();
				if (rawDataPorts == null || rawDataPorts.length != rawDataList.size()) {
					throw new ConnectionLtException(new DtpLtException());
				}
				RawDataPort rawDataPort;
				RawData rawData;
				for (int i = 0; i < rawDataPorts.length; i++) {
					rawDataPort = rawDataPorts[i];
					rawData = null;
					for (int j = 0; j < rawDataList.size() && rawData == null; j++) {
						if (rawDataList.get(j).getId() == rawDataPort.getId()) {
							rawData = rawDataList.get(j);
							RawDataClient.setReceiveInputStreamAsync(this.rawDataThreadPool,
									this.comSession.getRemoteInetAddress(), rawDataPort.getPort(), rawData,
									rawDataPort.getCallNumber());
						}
					}
				}
			}
			if (!responseClass.isInstance(result)) {
				throw new ConnectionLtException(new DtpLtException(new ClassCastException()));
			}
		}
		return responseClass.cast(result);
	}

	@Override
	public final boolean isConnected() {
		return this.comSession.isConnected();
	}

	/**
	 * Closes the connection.
	 *
	 * @throws ConnectionLtException <br/>
	 *                               +Cause IOException if an I/O error occurs when
	 *                               closing the socket
	 */
	@Override
	public final void close() throws ConnectionLtException {
		this.comSession.close();
	}

}

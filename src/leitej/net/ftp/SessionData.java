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

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import leitej.exception.ClosedLtRtException;
import leitej.exception.ObjectPoolLtException;

/**
 *
 * @author Julio Leite
 */
public final class SessionData implements Serializable {

	private static final long serialVersionUID = -1290030647208885969L;

	private static final Map<InetAddress, ListenDtpPool> LISTEN_DTP_MAP = new HashMap<>();

	// TODO: take more attention to password security ('bouncyCastle uses a
	// Arrays.fill owned') - Arrays.fill(passKey, (byte)0);

	private InetAddress localInetAddress;
	private SocketAddress localSocketAddress;
	private SocketAddress remoteSocketAddress;

	private String userName;
	// TODO: melhorar o controlo desta password
	private transient volatile char[] password;
	private boolean auth;

	private CommandEnum lastCommandReceived;
	private CodeEnum lastCodeSent;
	private String lastArgsReceived;

	private TransferModeEnum transferMode;
	private String clientDtpIp;
	private int clientDtpPort;

	private ListenerDtp listener;
	private int callNumber;

	private ConnectionTypeEnum connectionType;

	SessionData() {
		this.localSocketAddress = null;
		this.remoteSocketAddress = null;
		this.userName = null;
		this.password = null;
		this.auth = false;
		this.lastCommandReceived = null;
		this.lastCodeSent = CodeEnum._500;
		this.transferMode = TransferModeEnum.ACTIVE;
		this.clientDtpIp = null;
		this.clientDtpPort = -1;
		this.listener = null;
		this.callNumber = -1;
		this.connectionType = ConnectionTypeEnum.BINARY;
	}

	public String getUserName() {
		return this.userName;
	}

	void setUserName(final String user) {
		this.userName = user;
		this.auth = false;
	}

	public char[] getPassword() {
		return this.password;
	}

	void setPassword(final char[] password) {
		if (this.password != null) {
			for (int i = 0; i < this.password.length; i++) {
				this.password[i] = '\0';
			}
		}
		this.password = password;
		this.auth = false;
	}

	public CodeEnum getLastCodeSent() {
		return this.lastCodeSent;
	}

	void setLastCodeSent(final CodeEnum lastCodeSent) {
		this.lastCodeSent = lastCodeSent;
	}

	public boolean isAuth() {
		return this.auth;
	}

	void setAuth(final boolean auth) {
		this.auth = auth;
	}

	TransferModeEnum getTransferMode() {
		return this.transferMode;
	}

	void setTransferMode(final TransferModeEnum transferMode) {
		this.transferMode = transferMode;
	}

	String getClientDtpIp() {
		return this.clientDtpIp;
	}

	int getClientDtpPort() {
		return this.clientDtpPort;
	}

	void setClientDtpIpPort(final String clientIP, final int clientPort) {
		this.clientDtpIp = clientIP;
		this.clientDtpPort = clientPort;
		this.transferMode = TransferModeEnum.ACTIVE;
	}

	String setPassiv() {
		ListenDtpPool pool;
		synchronized (LISTEN_DTP_MAP) {
			pool = LISTEN_DTP_MAP.get(this.localInetAddress);
			if (pool == null) {
				pool = new ListenDtpPool(64, this.localInetAddress);
				LISTEN_DTP_MAP.put(this.localInetAddress, pool);
			}
		}
		try {
			this.listener = pool.poll();
			this.callNumber = this.listener.acceptAssync(this);
			this.transferMode = TransferModeEnum.PASSIV;
			final byte[] addr = this.localInetAddress.getAddress();
			final StringBuilder result = new StringBuilder();
			for (int i = 0; i < 4; i++) {
				result.append(addr[i] & 0xff);
				result.append(",");
			}
			final int port = this.listener.getPort();
			result.append((int) (port >>> 8 & 0xff));
			result.append(",");
			result.append((int) (port & 0xff));
			return result.toString();
		} catch (final ClosedLtRtException e) {
			return null;
		} catch (final ObjectPoolLtException e) {
			return null;
		} catch (final InterruptedException e) {
			return null;
		}
	}

	Socket getSocketDtp() {
		return this.listener.getCorrespondent(this, this.callNumber);
	}

	ConnectionTypeEnum getConnectionType() {
		return this.connectionType;
	}

	void setConnectionType(final ConnectionTypeEnum connectionType) {
		this.connectionType = connectionType;
	}

	CommandEnum getLastCommandReceived() {
		return this.lastCommandReceived;
	}

	void setLastCommandReceived(final CommandEnum lastCommandReceived) {
		this.lastCommandReceived = lastCommandReceived;
	}

	public String getLastArgsReceived() {
		return this.lastArgsReceived;
	}

	public void setLastArgsReceived(final String lastArgsReceived) {
		this.lastArgsReceived = lastArgsReceived;
	}

	final SocketAddress getLocalSocketAddress() {
		return this.localSocketAddress;
	}

	final void setLocalSocketAddress(final SocketAddress localSocketAddress) {
		this.localSocketAddress = localSocketAddress;
	}

	final SocketAddress getRemoteSocketAddress() {
		return this.remoteSocketAddress;
	}

	final void setRemoteSocketAddress(final SocketAddress remoteSocketAddress) {
		this.remoteSocketAddress = remoteSocketAddress;
	}

	final InetAddress getLocalInetAddress() {
		return this.localInetAddress;
	}

	final void setLocalInetAddress(final InetAddress localInetAddress) {
		this.localInetAddress = localInetAddress;
	}

}

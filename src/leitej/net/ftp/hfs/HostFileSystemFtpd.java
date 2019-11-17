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

package leitej.net.ftp.hfs;

import java.io.IOException;
import java.net.InetAddress;

import leitej.net.ftp.AbstractFtpServer;
import leitej.util.fs.host.HostFileSystem;

/**
 *
 * @author Julio Leite
 */
public final class HostFileSystemFtpd extends AbstractFtpServer<HostFileSystemFtpdHandler> {

	private final HostFileSystem hfs;

	public HostFileSystemFtpd(final HostFileSystem hfs) throws IOException {
		this(hfs, 21, InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));
	}

	public HostFileSystemFtpd(final HostFileSystem hfs, final int port) throws IOException {
		this(hfs, port, InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));
	}

	public HostFileSystemFtpd(final HostFileSystem hfs, final int port, final InetAddress bindAddress)
			throws IOException {
		super(port, 0, bindAddress);
		this.hfs = hfs;
	}

	@Override
	protected HostFileSystemFtpdHandler newHandler() {
		return new HostFileSystemFtpdHandler(this.hfs);
	}

}

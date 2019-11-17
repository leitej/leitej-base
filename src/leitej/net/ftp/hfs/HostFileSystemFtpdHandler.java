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

import leitej.net.ftp.AbstractFileSystemFtpdHandler;
import leitej.net.ftp.SessionData;
import leitej.util.fs.host.HostFileSystem;
import leitej.util.fs.host.HostFileSystemNode;

/**
 *
 * @author Julio Leite
 */
final class HostFileSystemFtpdHandler extends AbstractFileSystemFtpdHandler<HostFileSystem, HostFileSystemNode> {

	protected HostFileSystemFtpdHandler(final HostFileSystem fileSystem) {
		super(fileSystem);
	}

	@Override
	protected boolean auth(final SessionData session) {
		return true;
	}

}

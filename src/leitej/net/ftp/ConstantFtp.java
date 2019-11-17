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

import java.net.InetAddress;

/**
 *
 * @author Julio Leite
 */
final class ConstantFtp {

	static final int DEFAULT_PORT = 21;
	static final int DEFAULT_BACKLOG = 0;
	static final InetAddress DEFAULT_BIND_ADDR = null;

	static final String FTP_SERVER_THREAD_NAME = "Ftp-Server-";
	static final String FTP_HANDLER_THREAD_NAME = "Ftp-Handler-";

}

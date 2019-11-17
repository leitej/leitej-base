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

package leitej.net;

import java.net.InetAddress;

import leitej.Constant;
import leitej.util.DateUtil;

/**
 * Constants net
 *
 * @author Julio Leite
 */
public final class ConstantNet {

	private ConstantNet() {
	}

	public static final int DEFAULT_VELOCITY = 0; // No control
	public static final int DEFAULT_SIZE_PER_SENTENCE = 32 * Constant.MEGA;
	public static final int DEFAULT_TIMEOUT_MS = (int) (4 * DateUtil.ONE_HOUR_IN_MS);

	public static final int INITIATE_COMMUNICATION_TIMEOUT_MS = (int) (15 * DateUtil.ONE_SECOND_IN_MS);

	public static final int DEFAULT_DTP_PORT = 26;
	public static final int DEFAULT_DTP_BACKLOG = 0;
	public static final InetAddress DEFAULT_DTP_BIND_ADDR = null;

	public static final int DEFAULT_DTP_MAX_HANDLER_THREADS = 16;
	public static final String DTP_SERVER_THREAD_NAME = "Dtp-Server-";
	public static final String DTP_HANDLER_THREAD_NAME = "Dtp-Handler-";

	public static final String DEFAULT_DTP_CHARSET_NAME = Constant.UTF8_CHARSET_NAME;

	public static final int RAW_DATA_MAX_LISTENERS_PER_BIND_ADDR = 16;
	public static final int RAW_DATA_MAX_LISTENER_THREADS = RAW_DATA_MAX_LISTENERS_PER_BIND_ADDR * 2;
	public static final int RAW_DATA_LISTENER_BACKLOG = 0;
	public static final int RAW_DATA_LISTENER_TIME_OUT = (int) (2 * DateUtil.ONE_MINUTE_IN_MS);
	public static final int RAW_DATA_SOCKET_TIME_OUT = (int) (2 * DateUtil.ONE_MINUTE_IN_MS);

}

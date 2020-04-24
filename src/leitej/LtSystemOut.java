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

package leitej;

import leitej.util.DateUtil;
import leitej.util.StringUtil;

/**
 * This is used to write debug messages to system.out and can not use the
 * leitej.log because the class in case is used by the logger it self (making a
 * loop dependence which is resolved by the jvm with a null pointer)
 *
 * @author Julio Leite
 */
public final class LtSystemOut {

	static {
		debug(LtSystemOut.class.getCanonicalName() + " is Active");
	}

	private LtSystemOut() {
	}

	/**
	 * Adds log at debug level.
	 *
	 * @param msg  text of the message to log
	 * @param args objects to compose the text message
	 */
	public static final void debug(final String msg, final Object... args) {
		if (Constant.DEBUG_ACTIVE) {
			System.out.println("DEBUG_TO_SYS_OUT " + DateUtil.nowTime() + " [" + Thread.currentThread().getName() + "] "
					+ (new Throwable()).getStackTrace()[1].toString() + " - " + StringUtil.insertObjects(msg, args));
		}
	}

}

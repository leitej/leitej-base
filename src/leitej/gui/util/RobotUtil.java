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

package leitej.gui.util;

import java.awt.AWTException;
import java.awt.Robot;

/**
 *
 * @author Julio Leite
 */
final class RobotUtil {

	private static Robot robot;
	private static Exception exception;

	static {
		try {
			robot = new Robot();
		} catch (final AWTException e) {
			exception = e;
		} catch (final SecurityException e) {
			exception = e;
		}
	}

	/**
	 * @throws AWTException      - if the platform configuration does not allow
	 *                           low-level input control. This exception is always
	 *                           thrown when GraphicsEnvironment.isHeadless()
	 *                           returns true
	 * @throws SecurityException - if createRobot permission is not granted
	 */
	private static void allowRobot() throws AWTException, SecurityException {
		if (exception != null) {
			if (AWTException.class.isInstance(exception)) {
				throw (AWTException) exception;
			} else {
				throw (SecurityException) exception;
			}
		}
	}

	static Robot getRobot() throws SecurityException, AWTException {
		allowRobot();
		return robot;
	}

}

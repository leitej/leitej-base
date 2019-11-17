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
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;

/**
 *
 * @author Julio Leite
 */
public final class MouseUtil {

	public static PointerInfo getPointerInfo() {
		return MouseInfo.getPointerInfo();
	}

	public static int getNumberOfButtons() {
		return MouseInfo.getNumberOfButtons();
	}

	public static Point getPointerLocation() {
		return MouseInfo.getPointerInfo().getLocation();
	}

	/**
	 * Moves mouse pointer to given screen coordinates.
	 *
	 * @param x X position
	 * @param y Y position
	 * @throws AWTException      if the platform configuration does not allow
	 *                           low-level input control. This exception is always
	 *                           thrown when GraphicsEnvironment.isHeadless()
	 *                           returns true
	 * @throws SecurityException - if createRobot permission is not granted
	 */
	public static void move(final int x, final int y) throws AWTException, SecurityException {
		RobotUtil.getRobot().mouseMove(x, y);
	}

	public static void addMove(final int x, final int y) throws AWTException, SecurityException {
		RobotUtil.getRobot().mouseMove(x + (int) getPointerLocation().getX(), y + (int) getPointerLocation().getY());
	}

	public static void move(final int x, final int y, final int delayPerPixel)
			throws AWTException, SecurityException, InterruptedException {
		if (delayPerPixel < 0) {
			throw new IllegalArgumentException("delay value is negative");
		}
		final double xo = getPointerLocation().getX();
		final double yo = getPointerLocation().getY();
		final double xvar = x - xo;
		final double yvar = y - yo;
		double absxvar = Math.abs(x - xo);
		double absyvar = Math.abs(y - yo);
		final int xstep = (xvar < 0) ? -1 : 1;
		final int ystep = (yvar < 0) ? -1 : 1;
		double tmpstep = 0;
		double varstep;
		if (Math.abs(xvar) - Math.abs(yvar) > 0) {
			varstep = Math.abs(yvar) / Math.abs(xvar);
			while (absxvar > 0) {
				Thread.sleep(delayPerPixel);
				absxvar--;
				tmpstep += varstep;
				addMove(xstep, ((tmpstep >= 1) ? ystep : 0));
				if (tmpstep >= 1) {
					tmpstep--;
				}
			}
			if ((int) MouseUtil.getPointerLocation().getY() - y != 0) {
				addMove(0, ystep);
			}
		} else {
			varstep = Math.abs(xvar) / Math.abs(yvar);
			while (absyvar > 0) {
				Thread.sleep(delayPerPixel);
				absyvar--;
				tmpstep += varstep;
				addMove(((tmpstep >= 1) ? xstep : 0), ystep);
				if (tmpstep >= 1) {
					tmpstep--;
				}
			}
			if ((int) MouseUtil.getPointerLocation().getX() - x != 0) {
				addMove(xstep, 0);
			}
		}
	}

	public static void threadWait4NewPosition(final int timeOut4NewPosition) throws InterruptedException {
		if (timeOut4NewPosition < 0) {
			throw new IllegalArgumentException("timeout value is negative");
		}
		double x = getPointerLocation().getX();
		double y = getPointerLocation().getY();
		while (x == getPointerLocation().getX() && y == getPointerLocation().getY()) {
			Thread.sleep(100);
		}
		x = getPointerLocation().getX();
		y = getPointerLocation().getY();
		Thread.sleep(timeOut4NewPosition);
		while (x != getPointerLocation().getX() && y != getPointerLocation().getY()) {
			Thread.sleep(timeOut4NewPosition);
			x = getPointerLocation().getX();
			y = getPointerLocation().getY();
		}
	}

	public static void threadWait4Move() throws InterruptedException {
		final double x = getPointerLocation().getX();
		final double y = getPointerLocation().getY();
		while (x == getPointerLocation().getX() && y == getPointerLocation().getY()) {
			Thread.sleep(100);
		}
	}

	/**
	 * Presses one or more mouse buttons. The mouse buttons should be released using
	 * the <code>mouseRelease</code> method.
	 *
	 * @param buttons the Button mask; a combination of one or more of these flags:
	 *                <ul>
	 *                <li><code>InputEvent.BUTTON1_MASK</code>
	 *                <li><code>InputEvent.BUTTON2_MASK</code>
	 *                <li><code>InputEvent.BUTTON3_MASK</code>
	 *                </ul>
	 * @see #release(int)
	 * @throws IllegalArgumentException if the button mask is not a valid
	 *                                  combination
	 * @throws AWTException             if the platform configuration does not allow
	 *                                  low-level input control. This exception is
	 *                                  always thrown when
	 *                                  GraphicsEnvironment.isHeadless() returns
	 *                                  true
	 * @throws SecurityException        if createRobot permission is not granted
	 */
	public static void press(final int buttons) throws SecurityException, AWTException {
		RobotUtil.getRobot().mousePress(buttons);
	}

	/**
	 * Releases one or more mouse buttons.
	 *
	 * @param buttons the Button mask; a combination of one or more of these flags:
	 *                <ul>
	 *                <li><code>InputEvent.BUTTON1_MASK</code>
	 *                <li><code>InputEvent.BUTTON2_MASK</code>
	 *                <li><code>InputEvent.BUTTON3_MASK</code>
	 *                </ul>
	 * @see #press(int)
	 * @throws IllegalArgumentException if the button mask is not a valid
	 *                                  combination
	 * @throws AWTException             if the platform configuration does not allow
	 *                                  low-level input control. This exception is
	 *                                  always thrown when
	 *                                  GraphicsEnvironment.isHeadless() returns
	 *                                  true
	 * @throws SecurityException        if createRobot permission is not granted
	 */
	public static void release(final int buttons) throws SecurityException, AWTException {
		RobotUtil.getRobot().mouseRelease(buttons);
	}

	/**
	 * Rotates the scroll wheel on wheel-equipped mice.
	 *
	 * @param wheelAmt number of "notches" to move the mouse wheel Negative values
	 *                 indicate movement up/away from the user, positive values
	 *                 indicate movement down/towards the user.
	 * @throws AWTException      if the platform configuration does not allow
	 *                           low-level input control. This exception is always
	 *                           thrown when GraphicsEnvironment.isHeadless()
	 *                           returns true
	 * @throws SecurityException if createRobot permission is not granted
	 */
	public static void wheel(final int wheelAmt) throws SecurityException, AWTException {
		RobotUtil.getRobot().mouseWheel(wheelAmt);
	}

}

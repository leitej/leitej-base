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
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Julio Leite
 */
public final class GraphicsUtil {

	private static volatile boolean systemLookAndFeelSetted = false;

	private GraphicsUtil() {
	}

	public synchronized static void setSystemLookAndFeel() {
		if (!systemLookAndFeelSetted) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				systemLookAndFeelSetted = true;
			} catch (final ClassNotFoundException e) {
				throw new IllegalStateException(e);
			} catch (final InstantiationException e) {
				throw new IllegalStateException(e);
			} catch (final IllegalAccessException e) {
				throw new IllegalStateException(e);
			} catch (final UnsupportedLookAndFeelException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * Tests whether or not a display, keyboard, and mouse can be supported in this
	 * environment. If this method returns false, a HeadlessException is thrown from
	 * areas of the Toolkit and GraphicsEnvironment that are dependent on a display,
	 * keyboard, or mouse.
	 * 
	 * @return <code>false</code> if this environment cannot support a display,
	 *         keyboard, and mouse; <code>true</code> otherwise
	 */
	public static boolean isSupported() {
		return !GraphicsEnvironment.isHeadless();
	}

	public static int countScreenDevices() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
	}

	private static GraphicsDevice getGraphicsDevice(final int screen) {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[screen];
	}

	private static GraphicsDevice getDefaultGraphicsDevice() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	}

	public static Rectangle screenDeviceInsetBounds(final int screen) {
		return screenDeviceInsetBounds(getGraphicsDevice(screen).getDefaultConfiguration());
	}

	public static Rectangle defaultScreenDeviceInsetBounds() {
		return screenDeviceInsetBounds(getDefaultGraphicsDevice().getDefaultConfiguration());
	}

	private static Rectangle screenDeviceInsetBounds(final GraphicsConfiguration gc) {
		final Insets ins = Toolkit.getDefaultToolkit().getScreenInsets(gc);
		return new Rectangle(gc.getBounds().x + ins.left, gc.getBounds().y - ins.top,
				gc.getBounds().width - ins.left - ins.right, gc.getBounds().height - ins.top - ins.bottom);
	}

	public static Rectangle screenDeviceBounds(final int screen) {
		return getGraphicsDevice(screen).getDefaultConfiguration().getBounds();
	}

	public static Rectangle defaultScreenDeviceBounds() {
		return getDefaultGraphicsDevice().getDefaultConfiguration().getBounds();
	}

	public static BufferedImage captureScreen(final int screen) throws SecurityException, AWTException {
		return RobotUtil.getRobot().createScreenCapture(screenDeviceBounds(screen));
	}

	public static BufferedImage captureDefaultScreen() throws SecurityException, AWTException {
		return RobotUtil.getRobot().createScreenCapture(defaultScreenDeviceBounds());
	}

	/**
	 * Returns the color of a pixel at the given screen coordinates.
	 *
	 * @param x X position of pixel
	 * @param y Y position of pixel
	 * @return Color of the pixel
	 * @throws AWTException      - if the platform configuration does not allow
	 *                           low-level input control. This exception is always
	 *                           thrown when GraphicsEnvironment.isHeadless()
	 *                           returns true
	 * @throws SecurityException - if createRobot permission is not granted
	 */
	public static Color getPixelColor(final int x, final int y) throws SecurityException, AWTException {
		return RobotUtil.getRobot().getPixelColor(x, y);
	}

}

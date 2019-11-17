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
package leitej.gui.uniform;

import javax.swing.WindowConstants;

/*
 * DO_NOTHING_ON_CLOSE
 * 		Do not do anything when the user requests that the window close. Instead, the program should probably use a window listener that performs some other action in its windowClosing method
 *
 * HIDE_ON_CLOSE (the default for JDialog and JFrame)
 * 		Hide the window when the user closes it. This removes the window from the screen but leaves it displayable
 *
 * DISPOSE_ON_CLOSE (the default for JInternalFrame)
 * 		Hide and dispose of the window when the user closes it. This removes the window from the screen and frees up any resources used by it
 *
 * EXIT_ON_CLOSE (defined in the JFrame class)
 * 		Exit the application, using System.exit(0). This is recommended for applications only. If used within an applet, a SecurityException may be thrown
 *
 */
public enum CloseOperationEnum {
	DO_NOTHING_ON_CLOSE, HIDE_ON_CLOSE, DISPOSE_ON_CLOSE, EXIT_ON_CLOSE;

	int windowConstants() {
		switch (this) {
		case DO_NOTHING_ON_CLOSE:
			return WindowConstants.DO_NOTHING_ON_CLOSE;
		case HIDE_ON_CLOSE:
			return WindowConstants.HIDE_ON_CLOSE;
		case DISPOSE_ON_CLOSE:
			return WindowConstants.DISPOSE_ON_CLOSE;
		case EXIT_ON_CLOSE:
			return WindowConstants.EXIT_ON_CLOSE;
		default:
			throw new IllegalStateException(this.toString());
		}
	}
}

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

package leitej.util.machine;

import java.awt.Toolkit;
import java.io.IOError;

import leitej.exception.IllegalStateLtRtException;
import leitej.log.Logger;
import leitej.util.StringUtil;

/**
 * An useful class to help in interact with console.
 *
 * @author Julio Leite
 */
public class ConsoleUtil {

	private static final Logger LOG = Logger.getInstance();

	/**
	 * Creates a new instance of ConsoleUtil.
	 */
	private ConsoleUtil() {
	}

	/**
	 * Pauses the console.
	 *
	 * @throws IOError                   If an I/O error occurs
	 * @throws IllegalStateLtRtException If the system doesn't gives a console
	 */
	public static void pause() throws IOError, IllegalStateLtRtException {
		if (!hasConsole()) {
			throw new IllegalStateLtRtException("lt.ConsoleNull");
		}
		LOG.warn("lt.ConsolePause");
		System.console().readLine();
		// while(System.in.available()>0){
		// System.in.skip(System.in.available());
		// }
		// System.in.read();
	}

	/**
	 * Emits an audio beep.
	 */
	public static void beep() {
		Toolkit.getDefaultToolkit().beep();
	}

	/**
	 * Verifies if the system gives a console.
	 *
	 * @return boolean
	 */
	public static boolean hasConsole() {
		return (System.console() != null);
	}

	/**
	 * A convenience method to write a formatted string to console's output stream
	 * using the specified format string and arguments.
	 *
	 * @param msg  the data
	 * @param args the objects
	 */
	public static void write(final String msg, final Object... args) {
		if (!hasConsole()) {
			throw new IllegalStateLtRtException("lt.ConsoleNull");
		}
		System.console().printf("%1$s", StringUtil.insertObjects(msg, args));
	}

	/**
	 * Reads a password securely from the console.
	 *
	 * @return password
	 * @throws IOError                   If an I/O error occurs
	 * @throws IllegalStateLtRtException If the system doesn't gives a console
	 */
	public static char[] readPassword() throws IOError, IllegalStateException {
		if (!hasConsole()) {
			throw new IllegalStateLtRtException("lt.ConsoleNull");
		}
		return System.console().readPassword();
	}

	/**
	 * Reads a password securely from the console.
	 *
	 * @param msgPrompt the data
	 * @param args      the objects
	 * @return password
	 * @throws IOError                   If an I/O error occurs
	 * @throws IllegalStateLtRtException If the system doesn't gives a console
	 */
	public static char[] readPassword(final String msgPrompt, final Object... args)
			throws IOError, IllegalStateException {
		if (!hasConsole()) {
			throw new IllegalStateLtRtException("lt.ConsoleNull");
		}
		return System.console().readPassword("%1$s", StringUtil.insertObjects(msgPrompt, args));
	}

	/**
	 * Reads a line from the console.
	 *
	 * @return line
	 * @throws IOError                   If an I/O error occurs
	 * @throws IllegalStateLtRtException If the system doesn't gives a console
	 */
	public static String readLine() throws IOError, IllegalStateException {
		if (!hasConsole()) {
			throw new IllegalStateLtRtException("lt.ConsoleNull");
		}
		return System.console().readLine();
	}

}

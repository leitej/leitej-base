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

package leitej.net.exception;

import leitej.exception.LtException;

/**
 * The class <code>ConnectionLtException</code> and its subclasses are a form of
 * <code>LtException</code>.
 *
 * @author Julio Leite
 * @see leitej.exception.LtException
 * @see java.lang.Exception
 */
public class ConnectionLtException extends LtException {

	private static final long serialVersionUID = 5465840574109510801L;

	/**
	 * Creates a new instance of <code>ConnectionLtException</code>.
	 */
	public ConnectionLtException() {
		super();
	}

	/**
	 * Creates a new instance of <code>ConnectionLtException</code>.
	 *
	 * @param message The error message.
	 * @param objects To use if needed to build the message.
	 */
	public ConnectionLtException(final String message, final Object... objects) {
		super(message, objects);
	}

	/**
	 * Creates a new instance of <code>ConnectionLtException</code>.
	 *
	 * @param cause The nested exception.
	 */
	public ConnectionLtException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new instance of <code>ConnectionLtException</code>.
	 *
	 * @param cause   The nested exception.
	 * @param message The error message.
	 * @param objects To use if needed to build the message.
	 */
	public ConnectionLtException(final Throwable cause, final String message, final Object... objects) {
		super(cause, message, objects);
	}

}
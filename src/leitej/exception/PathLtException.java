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

package leitej.exception;

/**
 * The class <code>PathLtException</code> and its subclasses are a form of
 * <code>LtException</code> that indicates conditions that a reasonable
 * application might want to catch.
 *
 * @author Julio Leite
 * @see leitej.exception.LtException
 * @see java.lang.Exception
 */
public class PathLtException extends LtException {

	private static final long serialVersionUID = 2032866640796044499L;

	/**
	 * Creates a new instance of <code>PathLtException</code>.
	 */
	public PathLtException() {
		super();
	}

	/**
	 * Creates a new instance of <code>PathLtException</code>.
	 *
	 * @param message The error message.
	 * @param objects To use if needed to build the message.
	 */
	public PathLtException(final String message, final Object... objects) {
		super(message, objects);
	}

	/**
	 * Creates a new instance of <code>PathLtException</code>.
	 *
	 * @param cause The nested exception.
	 */
	public PathLtException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new instance of <code>PathLtException</code>.
	 *
	 * @param cause   The nested exception.
	 * @param message The error message.
	 * @param objects To use if needed to build the message.
	 */
	public PathLtException(final Throwable cause, final String message, final Object... objects) {
		super(cause, message, objects);
	}

}

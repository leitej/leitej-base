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

package leitej.ltm.exception;

import leitej.exception.LtRtException;

/**
 * The class <code>LtmLtRtException</code> and its subclasses are a form of
 * <code>LtRtException</code>.
 *
 * @author Julio Leite
 */
public class LtmLtRtException extends LtRtException {

	private static final long serialVersionUID = -131197164661630939L;

	/**
	 * Creates a new instance of <code>LtmLtRtException</code>.
	 */
	public LtmLtRtException() {
		super();
	}

	/**
	 * Creates a new instance of <code>LtmLtRtException</code>.
	 *
	 * @param message The error message.
	 * @param objects To use if needed to build the message.
	 */
	public LtmLtRtException(final String message, final Object... objects) {
		super(message, objects);
	}

	/**
	 * Creates a new instance of <code>LtmLtRtException</code>.
	 *
	 * @param cause The nested exception.
	 */
	public LtmLtRtException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new instance of <code>LtmLtRtException</code>.
	 *
	 * @param cause   The nested exception.
	 * @param message The error message.
	 * @param objects To use if needed to build the message.
	 */
	public LtmLtRtException(final Throwable cause, final String message, final Object... objects) {
		super(cause, message, objects);
	}

}

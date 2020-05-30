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

import leitej.net.ftp.CodeEnum;

/**
 * The class <code>FtpLtException</code> and its subclasses are a form of
 * <code>LtException</code> that indicates conditions that a reasonable
 * application might want to catch.
 *
 * @author Julio Leite
 * @see leitej.exception.LtException
 */
public class FtpLtException extends LtException {

	private static final long serialVersionUID = 201101110643L;

	private final CodeEnum code;

	/**
	 * Creates a new instance of <code>FtpLtException</code>.
	 *
	 * @param message The error message.
	 * @param objects To use if needed to build the message.
	 */
	public FtpLtException(final CodeEnum code, final Object... objects) {
		super(code.getMessageKey(), objects);
		this.code = code;
	}

	/**
	 * Creates a new instance of <code>FtpLtException</code>.
	 *
	 * @param cause   The nested exception.
	 * @param message The error message.
	 * @param objects To use if needed to build the message.
	 */
	public FtpLtException(final Throwable cause, final CodeEnum code, final Object... objects) {
		super(cause, code.getMessageKey(), objects);
		this.code = code;
	}

	public CodeEnum getCode() {
		return this.code;
	}

}

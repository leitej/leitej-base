/*******************************************************************************
 * Copyright (C) 2011 Julio Leite
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
 * The class <code>ParallelLtRtException</code> and its subclasses are a form of
 * <code>LtRtException</code>.
 * <p>
 * A method that <code>throws</code> <code>ParallelLtRtException</code>
 * indicates that has been thrown an <code>Exception</code> or any subclass of
 * <code>Exception</code> in a parallel execution (other <code>Thread</code>).
 *
 *
 * @author Julio Leite
 * @see leitej.exception.LtRtException
 * @see java.lang.RuntimeException
 */
public class ParallelLtRtException extends LtRtException {

	private static final long serialVersionUID = -5276126360067289339L;

	/**
	 * Creates a new instance of <code>ParallelLtRtException</code>.
	 *
	 * @param cause The nested exception.
	 */
	public ParallelLtRtException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new instance of <code>ParallelLtRtException</code>.
	 *
	 * @param cause   The nested exception.
	 * @param message The error message.
	 * @param objects To use if needed to build the message.
	 */
	public ParallelLtRtException(final Throwable cause, final String message, final Object... objects) {
		super(cause, message, objects);
	}

}

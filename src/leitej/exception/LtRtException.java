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

import java.io.PrintStream;
import java.io.PrintWriter;

import leitej.locale.message.Messages;

/**
 * The class <code>LtRtException</code> and its subclasses are a form of
 * <code>RuntimeException</code> that can be thrown during the normal operation
 * of the Java Virtual Machine.
 * <p>
 * A method is not required to declare in its <code>throws</code> clause any
 * subclasses of <code>LtRtException</code> that might be thrown during the
 * execution of the method but not caught.
 *
 * @author Julio Leite
 * @see java.lang.RuntimeException
 */
public class LtRtException extends RuntimeException {

	private static final long serialVersionUID = 9121812865628048974L;
	private static final Messages MESSAGES = Messages.getInstance();

	/** The nested exception. May be null. */
	private final Throwable mCause;

	/**
	 * Creates a new instance of <code>LtRtException</code>.
	 */
	public LtRtException() {
		this((Throwable) null, null);
	}

	/**
	 * Creates a new instance of <code>LtRtException</code>.
	 *
	 * @param message The error message.
	 * @param objects To use if needed to build the message.
	 */
	public LtRtException(final String message, final Object... objects) {
		this(null, message, objects);
	}

	/**
	 * Creates a new instance of <code>LtRtException</code>.
	 *
	 * @param cause The nested exception.
	 */
	public LtRtException(final Throwable cause) {
		super((cause == null) ? null : cause.toString());
		this.mCause = cause;
	}

	/**
	 * Creates a new instance of <code>LtRtException</code>.
	 *
	 * @param cause   The nested exception.
	 * @param message The error message.
	 * @param objects To use if needed to build the message.
	 */
	public LtRtException(final Throwable cause, final String message, final Object... objects) {
		super(MESSAGES.get(message, objects));
		this.mCause = cause;
	}

	/**
	 * Gets the cause of this exception. (May be null)
	 *
	 * @return The cause of this exception.
	 */
	@Override
	public Throwable getCause() {
		return this.mCause;
	}

	/**
	 * Prints the stack trace of this exception an of the nested exception, if
	 * present.
	 *
	 * @param stream The stream to print to.
	 */
	@Override
	public void printStackTrace(final PrintStream stream) {
		synchronized (stream) {
			super.printStackTrace(stream);
			if ((this.mCause != null) && (!superClassPrintsCause())) {
				stream.println(MESSAGES.get("Caused by: #0", this.mCause.getMessage()));
				this.mCause.printStackTrace(stream);
			}
		}
	}

	/**
	 * Prints the stack trace of this exception an of the nested exception, if
	 * present.
	 *
	 * @param writer The writer to print to.
	 */
	@Override
	public void printStackTrace(final PrintWriter writer) {
		synchronized (writer) {
			super.printStackTrace(writer);
			if ((this.mCause != null) && (!superClassPrintsCause())) {
				writer.println(MESSAGES.get("Caused by: #0", this.mCause.getMessage()));
				this.mCause.printStackTrace(writer);
			}
		}
	}

	/**
	 * Gets whether the superclass is able to print the cause of the exception. This
	 * is true for Java 1.4 and above.
	 *
	 * @return Whether the superclass is able to print the cause of the exception.
	 */
	private boolean superClassPrintsCause() {
		// Check whether there is a getCause method in the super class
		try {
			getClass().getSuperclass().getMethod("getCause");
			// The superclass has a getCause method -> It must be Java 1.4 or more
			return true;
		} catch (final SecurityException e) {
			// Can't know if superclass has getCause method
			return false;
		} catch (final NoSuchMethodException e) {
			// The superclass has no getCause method -> It must be Java 1.3 or less
			return false;
		}
	}

}

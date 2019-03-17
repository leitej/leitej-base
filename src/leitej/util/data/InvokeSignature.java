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

package leitej.util.data;

import java.io.Serializable;
import java.lang.reflect.Method;

import leitej.exception.IllegalArgumentLtRtException;

/**
 * This class is designed to allow multiple invocations of the same method with
 * different arguments.<br/>
 * Each new invocation can be obtained through
 * <code>getInvoke(Object...)</code>.
 *
 * @author Julio Leite
 */
public class InvokeSignature implements Serializable {

	private static final long serialVersionUID = 4296768613065157698L;

	private final Object object;
	private final Method method;

	/**
	 * Creates a new instance of InvokeSignature.
	 *
	 * @param object to use in the invocation
	 * @param method to use in the invocation
	 */
	public InvokeSignature(final Object object, final Method method) {
		if (method == null) {
			throw new IllegalArgumentLtRtException("lt.GUIEventInvokeArgNull");
		}
		this.object = object;
		this.method = method;
	}

	/**
	 * Obtain the new invocation.
	 *
	 * @param args to use in the invocation
	 * @return an object in the {@link leitej.util.data.InvokeItf InvokeItf}
	 */
	public InvokeItf getInvoke(final Object... args) {
		return new Invoke(this.object, this.method, args);
	}

	/**
	 * Use this method to verify the arguments that can be used on the invocation.
	 *
	 * @param args classes representing objects to invoke
	 * @return boolean
	 */
	public boolean matchArguments(final Class<?>... args) {
		if (args == null) {
			if (this.method.getParameterTypes().length == 0) {
				return true;
			} else {
				return false;
			}
		}
		if (this.method.getParameterTypes().length != args.length) {
			return false;
		}
		for (int i = 0; i < args.length; i++) {
			try {
				args[i].asSubclass(this.method.getParameterTypes()[i]);
			} catch (final ClassCastException e) {
				return false;
			}
		}
		return true;
	}
}

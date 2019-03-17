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

import java.lang.reflect.Method;

/**
 * Simple class to save information.<br/>
 * Prepared to the XMLOM concept.
 *
 * @author Julio Leite
 */
public class Invoke implements InvokeItf {

	private static final long serialVersionUID = -3487184164253807840L;

	private Object object = null;
	private Method method = null;
	private Object[] args = null;

	/**
	 * Creates a new instance of Invoke.
	 */
	public Invoke() {
	}

	/**
	 * Creates a new instance of Invoke.<br/>
	 * <br/>
	 * NOTE: if one argument of method to invoke is an array, this has to be cast to
	 * object.
	 *
	 * @param object to use in the invocation
	 * @param method to use in the invocation
	 * @param args   to use in the invocation
	 */
	public Invoke(final Object object, final Method method, final Object... args) {
		this.object = object;
		this.method = method;
		this.args = args;
	}

	@Override
	public Object getObject() {
		return this.object;
	}

	@Override
	public Method getMethod() {
		return this.method;
	}

	@Override
	public Object[] getArgs() {
		return this.args;
	}

	public void setObject(final Object object) {
		this.object = object;
	}

	public void setMethod(final Method method) {
		this.method = method;
	}

	public void setArgs(final Object[] args) {
		this.args = args;
	}

	@Override
	public final String toString() {
		return (new StringBuilder()).append(this.getClass().getSimpleName()).append("@")
				.append(super.toString().split("@")[1]).append("><").append(this.object.getClass().getName())
				.append(".").append(this.getMethod().getName()).toString();
	}

}

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

package leitej.util.data;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
public abstract class AbstractDataProxy<I, H extends AbstractDataProxyHandler<I>> implements Serializable {

	private static final long serialVersionUID = -7819302693959997119L;

	/**
	 *
	 * @param interfaceForProxyClass
	 * @param handler
	 * @return
	 */
	protected final <T extends I> T newProxyInstance(final Class<T> interfaceForProxyClass, final H handler) {
		try {
			@SuppressWarnings("unchecked")
			final T result = (T) Proxy.newProxyInstance(interfaceForProxyClass.getClassLoader(),
					new Class<?>[] { interfaceForProxyClass }, handler);
			handler.setMyProxy(result);
			return result;
		} catch (final IllegalArgumentException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 *
	 * @param proxy
	 * @return
	 * @throws IllegalArgumentException if is not a proxy instance or the result
	 *                                  invocation handler is not from type I
	 */
	@SuppressWarnings("unchecked")
	protected final <T extends I> H getInvocationHandler(final T proxy) throws IllegalArgumentException {
		try {
			return (H) Proxy.getInvocationHandler(proxy);
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentLtRtException(e);
		} catch (final ClassCastException e) {
			throw new IllegalArgumentLtRtException(e);
		}
	}

	protected static boolean isProxyClass(final Class<?> cl) {
		return Proxy.isProxyClass(cl);
	}

}

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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import leitej.LtSystemOut;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
public abstract class AbstractDataProxy<I, H extends AbstractDataProxyHandler<I>> implements Serializable {

	private static final long serialVersionUID = -7819302693959997119L;

	private static final Class<?>[] PROXY_CONSTRUCTOR_ARGUMENT = new Class[] { InvocationHandler.class };

	private final Class<?>[] proxyInterfaces;
	private final Map<Class<?>, Class<?>> proxyClassMap;

	/**
	 *
	 * @param proxyClassInterfaceBase
	 * @param proxyClassImplementComplements
	 * @throws IllegalArgumentLtRtException if any of the classes passed in
	 *                                      parameter is null or does not represents
	 *                                      an interface
	 */
	protected AbstractDataProxy(final Class<?>... proxyClassImplementComplements) throws IllegalArgumentLtRtException {
		if (proxyClassImplementComplements != null) {
			for (final Class<?> itf : proxyClassImplementComplements) {
				if (itf == null || !itf.isInterface()) {
					throw new IllegalArgumentLtRtException();
				}
			}
			this.proxyInterfaces = new Class<?>[proxyClassImplementComplements.length + 1];
			System.arraycopy(proxyClassImplementComplements, 0, this.proxyInterfaces, 1,
					proxyClassImplementComplements.length);
		} else {
			this.proxyInterfaces = new Class<?>[1];
		}
		this.proxyClassMap = new HashMap<>();
	}

	/**
	 *
	 * @param interfaceForProxyClass
	 * @param handler
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected final <T extends I> T newProxyInstance(final Class<T> interfaceForProxyClass, final H handler) {
		try {
			final T result = (T) proxyClass(interfaceForProxyClass).getConstructor(PROXY_CONSTRUCTOR_ARGUMENT)
					.newInstance(new Object[] { handler });
			handler.setMyProxy(result);
			return result;
		} catch (final IllegalArgumentException e) {
			throw new ImplementationLtRtException(e);
		} catch (final InvocationTargetException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		} catch (final IllegalAccessException e) {
			throw new ImplementationLtRtException(e);
		} catch (final InstantiationException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 *
	 * @param interfaceForProxyClass
	 * @return
	 * @throws IllegalArgumentLtRtException if <code>interfaceForProxyClass</code>
	 *                                      in parameter is null
	 */
	private <T extends I> Class<?> proxyClass(final Class<T> interfaceForProxyClass) {
		if (interfaceForProxyClass == null) {
			throw new IllegalArgumentLtRtException();
		}
		Class<?> result;
		synchronized (this.proxyClassMap) {
			result = this.proxyClassMap.get(interfaceForProxyClass);
			if (result == null) {
				this.proxyInterfaces[0] = interfaceForProxyClass;
				result = Proxy.getProxyClass(interfaceForProxyClass.getClassLoader(), this.proxyInterfaces);
				LtSystemOut.debug("#0 -> #1", result.getName(), interfaceForProxyClass.getName());
				this.proxyClassMap.put(interfaceForProxyClass, result);
			}
		}
		return result;
	}

	/**
	 *
	 * @param proxy
	 * @return
	 * @throws IllegalArgumentException if is not a proxy instance or the result
	 *                                  invocation handler is not from type I
	 */
	@SuppressWarnings("unchecked")
	public final <T extends I> H getInvocationHandler(final T proxy) throws IllegalArgumentException {
		try {
			return (H) Proxy.getInvocationHandler(proxy);
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentLtRtException(e);
		} catch (final ClassCastException e) {
			throw new IllegalArgumentLtRtException(e);
		}
	}

	public static boolean isProxyClass(final Class<?> cl) {
		return Proxy.isProxyClass(cl);
	}

}

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

package leitej.util.machine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Permission;

import leitej.log.Logger;
import leitej.util.AgnosticUtil;
import leitej.util.data.InvokeItf;

/**
 * BlockSystemExit - do not allow the execution of System.exit
 *
 * @author Julio Leite
 */
public final class BlockSystemExit {

	private static final Logger LOG = Logger.getInstance();

	/**
	 * Block execution of <code>System.exit</code> only for the <code>Thread</code>
	 * that calls this method.
	 *
	 * @param invokeData invocation that has the call of System.exit to be blocked
	 * @return invoke result (if the block occurred return is null, if not occurred
	 *         the return correspond to the return of the invocation in argument)
	 * @throws IllegalArgumentException    If the method is an instance method and
	 *                                     the specified object argument is not an
	 *                                     instance of the class or interface
	 *                                     declaring the underlying method (or of a
	 *                                     subclass or implementor thereof); if the
	 *                                     number of actual and formal parameters
	 *                                     differ; if an unwrapping conversion for
	 *                                     primitive arguments fails; or if, after
	 *                                     possible unwrapping, a parameter value
	 *                                     cannot be converted to the corresponding
	 *                                     formal parameter type by a method
	 *                                     invocation conversion
	 * @throws IllegalAccessException      If this Method object enforces Java
	 *                                     language access control and the
	 *                                     underlying method is inaccessible
	 * @throws InvocationTargetException   If the underlying method throws an
	 *                                     exception
	 * @throws NullPointerException        If the specified object is null and the
	 *                                     method is an instance method
	 * @throws ExceptionInInitializerError If the initialisation provoked by this
	 *                                     method fails
	 */
	public synchronized static Object from(final InvokeItf invokeData) throws ExceptionInInitializerError,
			NullPointerException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return from(invokeData.getObject(), invokeData.getMethod(), invokeData.getArgs());
	}

	/**
	 * 
	 * @param object
	 * @param method
	 * @param args
	 * @return invoke result (if the block occurred return is null, if not occurred
	 *         the return correspond to the return of the invocation in argument)
	 * @throws IllegalArgumentException    If the method is an instance method and
	 *                                     the specified object argument is not an
	 *                                     instance of the class or interface
	 *                                     declaring the underlying method (or of a
	 *                                     subclass or implementor thereof); if the
	 *                                     number of actual and formal parameters
	 *                                     differ; if an unwrapping conversion for
	 *                                     primitive arguments fails; or if, after
	 *                                     possible unwrapping, a parameter value
	 *                                     cannot be converted to the corresponding
	 *                                     formal parameter type by a method
	 *                                     invocation conversion
	 * @throws IllegalAccessException      If this Method object enforces Java
	 *                                     language access control and the
	 *                                     underlying method is inaccessible
	 * @throws InvocationTargetException   If the underlying method throws an
	 *                                     exception
	 * @throws NullPointerException        If the specified object is null and the
	 *                                     method is an instance method
	 * @throws ExceptionInInitializerError If the initialisation provoked by this
	 *                                     method fails
	 */
	private synchronized static Object from(final Object object, final Method method, final Object... args)
			throws ExceptionInInitializerError, NullPointerException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Object result = null;
		synchronized (SecurityManager.class) {
			final Thread specialAllow = Thread.currentThread();
			try {
				// prepare the block
				final XSecurityManager xSecurityManager = new XSecurityManager() {
					@Override
					public void checkPermission(final Permission permission) {
						if (permission != null && permission.getName().matches("exitVM.*")
								&& Thread.currentThread().equals(specialAllow)) {
							LOG.debug("lt.BlockExit", permission.getName(), Thread.currentThread());
							throw new ExitTrappedException();
						}
						super.checkPermission(permission);
					}
				};
				try {
					// execute method
					LOG.trace("lt.BlockInvoke", method);
					result = AgnosticUtil.invoke(object, method, args);
				} catch (final InvocationTargetException e) {
					if (!ExitTrappedException.class.isInstance(e.getCause())) {
						throw e;
					}
				} finally {
					// remove the block
					xSecurityManager.end();
				}
			} catch (final SecurityException e) {
				LOG.error("#0", e);
				throw e;
			}
		}
		return result;
	}

	private static class ExitTrappedException extends SecurityException {
		private static final long serialVersionUID = 200912241925L;
	}
}

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

import java.security.Permission;

import leitej.log.Logger;
import leitej.util.machine.XSecurityManager;

/**
 * The class <code>SeppukuLtRtException</code> is a form of
 * <code>LtRtException</code>.
 * <p>
 * At the first instantiation of this class will produce an parallel call of
 * <code>System.exit</code>.
 *
 * @author Julio Leite
 * @see leitej.exception.LtRtException
 * @see java.lang.RuntimeException
 * @see java.lang.System#exit(int)
 */
public final class SeppukuLtRtException extends LtRtException {

	private static final long serialVersionUID = 6301535983643132432L;

	private static final Logger LOG = Logger.getInstance();

	private static volatile boolean EXIT_CALLED = false;

	private static synchronized void systemExit(final int status) {
		if (!EXIT_CALLED) {
			new Thread((new Runnable() {
				@Override
				public void run() {
					LOG.warn("lt.Init");
					try {
						System.exit(status);
					} catch (final SecurityException e) {
						LOG.warn("#0", e);
						try {
							synchronized (SecurityManager.class) {
								final Thread specialAllow = Thread.currentThread();
								new XSecurityManager() {
									@Override
									public void checkPermission(final Permission permission) {
										if (permission != null && permission.getName().matches("exitVM.*")
												&& Thread.currentThread().equals(specialAllow)) {
											return;
										}
										super.checkPermission(permission);
									}
								};
								System.exit(status);
							}
						} catch (final SecurityException e1) {
							LOG.error("lt.FaultDetected");
							LOG.error("#0", e1);
						}
					}
				}
			})).start();
			EXIT_CALLED = true;
		}
	}

	/**
	 * Creates a new instance of <code>SeppukuLtRtException</code>.
	 *
	 * @param exitStatus Exit status.
	 * @param cause      The nested exception.
	 */
	public SeppukuLtRtException(final int exitStatus, final Throwable cause) {
		super(cause);
		SeppukuLtRtException.systemExit(exitStatus);
	}

	/**
	 * Creates a new instance of <code>SeppukuLtRtException</code>.
	 *
	 * @param exitStatus Exit status.
	 * @param cause      The nested exception.
	 * @param message    The error message.
	 * @param objects    To use if needed to build the message.
	 */
	public SeppukuLtRtException(final int exitStatus, final Throwable cause, final String message,
			final Object... objects) {
		super(cause, message, objects);
		SeppukuLtRtException.systemExit(exitStatus);
	}

}

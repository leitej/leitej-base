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

package leitej.util.machine;

import java.security.Permission;

import leitej.exception.IllegalStateLtRtException;

/**
 * The class <code>XSecurityManager</code> and its subclasses are a form of
 * <code>SecurityManager</code>.<br/>
 * Useful to programmatically change JVM security measures.<br/>
 * <br/>
 * Only can be used one instance at a time. The constructor automatically set
 * <code>this</code> security manager in <code>System</code>.
 *
 * @author Julio Leite
 * @see java.lang.SecurityManager
 */
public class XSecurityManager extends SecurityManager {

	private static volatile boolean EXTENSION_ACTIVE = false;

	private final SecurityManager securityManager;
	private volatile boolean ended;
	private volatile Thread specialAllow;

	/**
	 * Constructs a new <code>XSecurityManager</code>.<br/>
	 * The constructor automatically set <code>this</code> security manager in
	 * <code>System</code>.
	 *
	 * @throws SecurityException         if a security manager already exists and
	 *                                   its <code>checkPermission</code> method
	 *                                   doesn't allow creation of a new security
	 *                                   manager or if the security manager has
	 *                                   already been set and doesn't allow it to be
	 *                                   replaced
	 * @throws IllegalStateLtRtException if there are already an instance active
	 */
	public XSecurityManager() throws SecurityException, IllegalStateLtRtException {
		super();
		synchronized (SecurityManager.class) {
			if (EXTENSION_ACTIVE) {
				throw new IllegalStateLtRtException("lt.OtherInstanceActive");
			}
			this.securityManager = System.getSecurityManager();
			System.setSecurityManager(this);
			this.ended = false;
			EXTENSION_ACTIVE = true;
		}
	}

	/**
	 * Ends the appliance of this class measures, returning to the anterior ones.
	 *
	 * @throws SecurityException if the security manager
	 *                           <code>checkPermission</code> method doesn't allow
	 *                           it to be replaced
	 */
	public void end() throws SecurityException {
		synchronized (SecurityManager.class) {
			if (!this.ended) {
				this.specialAllow = Thread.currentThread();
				System.setSecurityManager(this.securityManager);
				this.specialAllow = null;
				this.ended = true;
				EXTENSION_ACTIVE = false;
			}
		}
	}

	@Override
	public Object getSecurityContext() {
		if (this.securityManager != null) {
			return this.securityManager.getSecurityContext();
		} else {
			return super.getSecurityContext();
		}
	}

	@Override
	public void checkPermission(final Permission perm) {
		if (perm != null && perm.getName().matches("setSecurityManager")
				&& Thread.currentThread().equals(this.specialAllow)) {
			return;
		}
		if (this.securityManager != null) {
			this.securityManager.checkPermission(perm);
		} else {
			super.checkPermission(perm);
		}
	}

	@Override
	public void checkPermission(final Permission perm, final Object context) {
		if (this.securityManager != null) {
			this.securityManager.checkPermission(perm, context);
		} else {
			super.checkPermission(perm, context);
		}
	}

}

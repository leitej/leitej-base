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

import java.io.FileDescriptor;
import java.net.InetAddress;
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

	@Override
	public void checkCreateClassLoader() {
		if (this.securityManager != null) {
			this.securityManager.checkCreateClassLoader();
		} else {
			super.checkCreateClassLoader();
		}
	}

	@Override
	public void checkAccess(final Thread t) {
		if (this.securityManager != null) {
			this.securityManager.checkAccess(t);
		} else {
			super.checkAccess(t);
		}
	}

	@Override
	public void checkAccess(final ThreadGroup g) {
		if (this.securityManager != null) {
			this.securityManager.checkAccess(g);
		} else {
			super.checkAccess(g);
		}
	}

	@Override
	public void checkExit(final int status) {
		if (this.securityManager != null) {
			this.securityManager.checkExit(status);
		} else {
			super.checkExit(status);
		}
	}

	@Override
	public void checkExec(final String cmd) {
		if (this.securityManager != null) {
			this.securityManager.checkExec(cmd);
		} else {
			super.checkExec(cmd);
		}
	}

	@Override
	public void checkLink(final String lib) {
		if (this.securityManager != null) {
			this.securityManager.checkLink(lib);
		} else {
			super.checkLink(lib);
		}
	}

	@Override
	public void checkRead(final FileDescriptor fd) {
		if (this.securityManager != null) {
			this.securityManager.checkRead(fd);
		} else {
			super.checkRead(fd);
		}
	}

	@Override
	public void checkRead(final String file) {
		if (this.securityManager != null) {
			this.securityManager.checkRead(file);
		} else {
			super.checkRead(file);
		}
	}

	@Override
	public void checkRead(final String file, final Object context) {
		if (this.securityManager != null) {
			this.securityManager.checkRead(file, context);
		} else {
			super.checkRead(file, context);
		}
	}

	@Override
	public void checkWrite(final FileDescriptor fd) {
		if (this.securityManager != null) {
			this.securityManager.checkWrite(fd);
		} else {
			super.checkWrite(fd);
		}
	}

	@Override
	public void checkWrite(final String file) {
		if (this.securityManager != null) {
			this.securityManager.checkWrite(file);
		} else {
			super.checkWrite(file);
		}
	}

	@Override
	public void checkDelete(final String file) {
		if (this.securityManager != null) {
			this.securityManager.checkDelete(file);
		} else {
			super.checkDelete(file);
		}
	}

	@Override
	public void checkConnect(final String host, final int port) {
		if (this.securityManager != null) {
			this.securityManager.checkConnect(host, port);
		} else {
			super.checkConnect(host, port);
		}
	}

	@Override
	public void checkConnect(final String host, final int port, final Object context) {
		if (this.securityManager != null) {
			this.securityManager.checkConnect(host, port, context);
		} else {
			super.checkConnect(host, port, context);
		}
	}

	@Override
	public void checkListen(final int port) {
		if (this.securityManager != null) {
			this.securityManager.checkListen(port);
		} else {
			super.checkListen(port);
		}
	}

	@Override
	public void checkAccept(final String host, final int port) {
		if (this.securityManager != null) {
			this.securityManager.checkAccept(host, port);
		} else {
			super.checkAccept(host, port);
		}
	}

	@Override
	public void checkMulticast(final InetAddress maddr) {
		if (this.securityManager != null) {
			this.securityManager.checkMulticast(maddr);
		} else {
			super.checkMulticast(maddr);
		}
	}

	@Override
	@Deprecated
	public void checkMulticast(final InetAddress maddr, final byte ttl) {
		if (this.securityManager != null) {
			this.securityManager.checkMulticast(maddr, ttl);
		} else {
			super.checkMulticast(maddr, ttl);
		}
	}

	@Override
	public void checkPropertiesAccess() {
		if (this.securityManager != null) {
			this.securityManager.checkPropertiesAccess();
		} else {
			super.checkPropertiesAccess();
		}
	}

	@Override
	public void checkPropertyAccess(final String key) {
		if (this.securityManager != null) {
			this.securityManager.checkPropertyAccess(key);
		} else {
			super.checkPropertyAccess(key);
		}
	}

	@Override
	public boolean checkTopLevelWindow(final Object window) {
		if (this.securityManager != null) {
			return this.securityManager.checkTopLevelWindow(window);
		} else {
			return super.checkTopLevelWindow(window);
		}
	}

	@Override
	public void checkPrintJobAccess() {
		if (this.securityManager != null) {
			this.securityManager.checkPrintJobAccess();
		} else {
			super.checkPrintJobAccess();
		}
	}

	@Override
	public void checkSystemClipboardAccess() {
		if (this.securityManager != null) {
			this.securityManager.checkSystemClipboardAccess();
		} else {
			super.checkSystemClipboardAccess();
		}
	}

	@Override
	public void checkAwtEventQueueAccess() {
		if (this.securityManager != null) {
			this.securityManager.checkAwtEventQueueAccess();
		} else {
			super.checkAwtEventQueueAccess();
		}
	}

	@Override
	public void checkPackageAccess(final String pkg) {
		if (this.securityManager != null) {
			this.securityManager.checkPackageAccess(pkg);
		} else {
			super.checkPackageAccess(pkg);
		}
	}

	@Override
	public void checkPackageDefinition(final String pkg) {
		if (this.securityManager != null) {
			this.securityManager.checkPackageDefinition(pkg);
		} else {
			super.checkPackageDefinition(pkg);
		}
	}

	@Override
	public void checkSetFactory() {
		if (this.securityManager != null) {
			this.securityManager.checkSetFactory();
		} else {
			super.checkSetFactory();
		}
	}

	@Override
	public void checkMemberAccess(final Class<?> clazz, final int which) {
		if (this.securityManager != null) {
			this.securityManager.checkMemberAccess(clazz, which);
		} else {
			super.checkMemberAccess(clazz, which);
		}
	}

	@Override
	public void checkSecurityAccess(final String target) {
		if (this.securityManager != null) {
			this.securityManager.checkSecurityAccess(target);
		} else {
			super.checkSecurityAccess(target);
		}
	}

	@Override
	public ThreadGroup getThreadGroup() {
		if (this.securityManager != null) {
			return this.securityManager.getThreadGroup();
		} else {
			return super.getThreadGroup();
		}
	}

}

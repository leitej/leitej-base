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

package leitej.gui;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import leitej.LtIcon;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.gui.event.LtActionListener;
import leitej.gui.event.LtMouseListener;
import leitej.gui.exception.GuiLtException;
import leitej.gui.exception.TrayIconGuiLtRtException;
import leitej.gui.util.GraphicsUtil;
import leitej.log.Logger;
import leitej.util.AgnosticUtil;
import leitej.util.StringUtil;
import leitej.util.data.Invoke;
import leitej.util.data.InvokeItf;
import leitej.util.data.InvokeSignature;
import leitej.util.machine.ShutdownHookUtil;

/**
 *
 * @author Julio Leite
 */
public final class LtTrayIcon {

	static {
		GraphicsUtil.setSystemLookAndFeel();
	}

	private static final Logger LOG = Logger.getInstance();

	private static final Dimension ICON_DIMENSION = SystemTray.getSystemTray().getTrayIconSize();

	private final Map<Object, Image> registeredImageMap = new HashMap<>();
	private final Map<String, MenuItem> registeredMenuItemMap = new HashMap<>();

	private InternalTrayIcon itrayIcon;
	private Object activeImageKey = null;

	private final List<Thread> threadsBlocked = new ArrayList<>();
	private final InvokeItf closeAtShutdownInvoke;

	/**
	 *
	 * @param tooltip
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public LtTrayIcon(final String tooltip) throws GuiLtException, InterruptedException {
		this(tooltip, (byte[]) null);
	}

	/**
	 *
	 * @param tooltip
	 * @param iconImage
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public LtTrayIcon(final String tooltip, final byte[] iconImage) throws InterruptedException, GuiLtException {
		registerImage(null, iconImage);
		final LtTrayIcon me = this;
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					if (!SystemTray.isSupported()) {
						throw new TrayIconGuiLtRtException();
					}
					LtTrayIcon.this.itrayIcon = new InternalTrayIcon(me, LtTrayIcon.this.registeredImageMap.get(null),
							tooltip);
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
		try {
			this.closeAtShutdownInvoke = new Invoke(this, AgnosticUtil.getMethod(this, METHOD_CLOSE));
			ShutdownHookUtil.add(this.closeAtShutdownInvoke);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void hide() throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					LtTrayIcon.this.itrayIcon.setVisible(false);
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void unhide() throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					LtTrayIcon.this.itrayIcon.setVisible(true);
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	private static final String METHOD_CLOSE = "close";

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void close() throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					LtTrayIcon.this.itrayIcon.dispose();
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	private void dispose() {
		removeCloseAsyncInvokeFromShutdownHook();
		synchronized (this.threadsBlocked) {
			final Iterator<Thread> it = this.threadsBlocked.iterator();
			while (it.hasNext()) {
				try {
					it.next().interrupt();
				} catch (final SecurityException e) {
					LOG.error("#0", e);
				}
			}
			this.threadsBlocked.clear();
		}
	}

	private void removeCloseAsyncInvokeFromShutdownHook() {
		if (this.closeAtShutdownInvoke != null && !ShutdownHookUtil.isActive()) {
			try {
				ShutdownHookUtil.remove(this.closeAtShutdownInvoke);
			} catch (final IllegalStateLtRtException e) {
				LOG.trace("#0", e);
			}
		}
	}

	public void blockTillClosed() throws InterruptedException {
		if (!this.itrayIcon.disposed) {
			synchronized (this.threadsBlocked) {
				this.threadsBlocked.add(Thread.currentThread());
			}
			try {
				while (!this.itrayIcon.disposed) {
					try {
						Thread.sleep(10000);
					} catch (final InterruptedException e) {
						if (!this.itrayIcon.disposed) {
							throw e;
						}
					}
				}
			} finally {
				synchronized (this.threadsBlocked) {
					this.threadsBlocked.remove(Thread.currentThread());
					Thread.interrupted();
				}
			}
		}
	}

	/*
	 * Image
	 */

	public synchronized void registerImage(final Object key, final byte[] iconImage) {
		if (iconImage != null) {
			this.registeredImageMap.put(key, Toolkit.getDefaultToolkit().createImage(iconImage)
					.getScaledInstance(ICON_DIMENSION.width, ICON_DIMENSION.height, Image.SCALE_SMOOTH));
		} else {
			this.registeredImageMap.put(key, Toolkit.getDefaultToolkit().createImage(LtIcon.DATA_128_BG_W)
					.getScaledInstance(ICON_DIMENSION.width, ICON_DIMENSION.height, Image.SCALE_SMOOTH));
		}
	}

	public synchronized void registerImage(final Object key, final String iconImage) {
		if (!StringUtil.isNullOrEmpty(iconImage)) {
			this.registeredImageMap.put(key, Toolkit.getDefaultToolkit().createImage(iconImage)
					.getScaledInstance(ICON_DIMENSION.width, ICON_DIMENSION.height, Image.SCALE_SMOOTH));
		} else {
			this.registeredImageMap.put(key, Toolkit.getDefaultToolkit().createImage(LtIcon.DATA_128_BG_W)
					.getScaledInstance(ICON_DIMENSION.width, ICON_DIMENSION.height, Image.SCALE_SMOOTH));
		}
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void unregisterImage(final Object key) throws GuiLtException, InterruptedException {
		if (key == null) {
			throw new NullPointerException();
		}
		if (key.equals(this.activeImageKey)) {
			activeDefaultImage();
		}
		this.registeredImageMap.remove(key);
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void activeDefaultImage() throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					LtTrayIcon.this.itrayIcon.setImage(LtTrayIcon.this.registeredImageMap.get(null));
					LtTrayIcon.this.activeImageKey = null;
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void activeImage(final Object key) throws InterruptedException, GuiLtException {
		if (key == null) {
			throw new NullPointerException();
		}
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					if (LtTrayIcon.this.registeredImageMap.get(key) == null) {
						throw new TrayIconGuiLtRtException();
					}
					LtTrayIcon.this.itrayIcon.setImage(LtTrayIcon.this.registeredImageMap.get(key));
					LtTrayIcon.this.activeImageKey = key;
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	/*
	 * Menu item
	 */

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void addMenuItem(final String name, final InvokeSignature invokeSignature)
			throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					if (LtTrayIcon.this.itrayIcon.getPopupMenu() == null) {
						LtTrayIcon.this.itrayIcon.setPopupMenu(new PopupMenu());
					}
					final MenuItem menuItem = new MenuItem(name);
					if (invokeSignature != null) {
						menuItem.addActionListener(new LtActionListener(invokeSignature));
					}
					LtTrayIcon.this.itrayIcon.getPopupMenu().add(menuItem);
					LtTrayIcon.this.registeredMenuItemMap.put(name, menuItem);
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void addSeparator() throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					if (LtTrayIcon.this.itrayIcon.getPopupMenu() == null) {
						LtTrayIcon.this.itrayIcon.setPopupMenu(new PopupMenu());
					}
					LtTrayIcon.this.itrayIcon.getPopupMenu().addSeparator();
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void enabledMenuItem(final String name) throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					LtTrayIcon.this.registeredMenuItemMap.get(name).setEnabled(true);
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void disabledMenuItem(final String name) throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					LtTrayIcon.this.registeredMenuItemMap.get(name).setEnabled(false);
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	/*
	 * Listener
	 */

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void setMouseClicked(final InvokeSignature mouseClicked)
			throws GuiLtException, InterruptedException {
		setMouseClicked(mouseClicked, null, null, null, null);
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void setMouseClicked(final InvokeSignature mouseClicked, final InvokeSignature mouseEntered,
			final InvokeSignature mouseExited, final InvokeSignature mousePressed, final InvokeSignature mouseReleased)
			throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					while (LtTrayIcon.this.itrayIcon.getMouseListeners().length > 0) {
						LtTrayIcon.this.itrayIcon.removeMouseListener(LtTrayIcon.this.itrayIcon.getMouseListeners()[0]);
					}
					LtTrayIcon.this.itrayIcon.addMouseListener(
							new LtMouseListener(mouseClicked, mouseEntered, mouseExited, mousePressed, mouseReleased));
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void setActionListener(final InvokeSignature invokeSignature)
			throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					while (LtTrayIcon.this.itrayIcon.getActionListeners().length > 0) {
						LtTrayIcon.this.itrayIcon
								.removeActionListener(LtTrayIcon.this.itrayIcon.getActionListeners()[0]);
					}
					LtTrayIcon.this.itrayIcon.addActionListener(new LtActionListener(invokeSignature));
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	/*
	 * Display message
	 */

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void displayMessage(final String caption, final String text)
			throws GuiLtException, InterruptedException {
		displayMessage(caption, text, MessageType.NONE);
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void displayMessageInfo(final String caption, final String text)
			throws GuiLtException, InterruptedException {
		displayMessage(caption, text, MessageType.INFO);
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void displayMessageWarning(final String caption, final String text)
			throws GuiLtException, InterruptedException {
		displayMessage(caption, text, MessageType.WARNING);
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public synchronized void displayMessageError(final String caption, final String text)
			throws GuiLtException, InterruptedException {
		displayMessage(caption, text, MessageType.ERROR);
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	void displayMessage(final String caption, final String text, final MessageType messageType)
			throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					LtTrayIcon.this.itrayIcon.displayMessage(caption, text, messageType);
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	private static class InternalTrayIcon extends TrayIcon {

		private final LtTrayIcon ltTrayIcon;
		private volatile boolean onSystray = false;
		private volatile boolean disposed = false;

		/**
		 * Creates a <code>TrayIcon</code> with the specified image and tooltip text.
		 * 
		 * @param ltTrayIcon
		 * @param image
		 * @param tooltip
		 * @throws IllegalArgumentException      if <code>image</code> is
		 *                                       <code>null</code>
		 * @throws UnsupportedOperationException if the system tray isn't supported by
		 *                                       the current platform
		 * @throws HeadlessException             if
		 *                                       {@code GraphicsEnvironment.isHeadless()}
		 *                                       returns {@code true}
		 * @throws SecurityException             if {@code accessSystemTray} permission
		 *                                       is not granted
		 */
		private InternalTrayIcon(final LtTrayIcon ltTrayIcon, final Image image, final String tooltip) {
			super(image, tooltip);
			this.ltTrayIcon = ltTrayIcon;
		}

		private void setVisible(final boolean b) {
			if (this.disposed) {
				throw new TrayIconGuiLtRtException();
			}
			if (b != this.onSystray) {
				if (b) {
					try {
						SystemTray.getSystemTray().add(this);
					} catch (final AWTException e) {
						throw new TrayIconGuiLtRtException(e);
					}
					this.onSystray = true;
				} else {
					SystemTray.getSystemTray().remove(this);
					this.onSystray = false;
				}
			}
		}

		private void dispose() {
			if (!this.disposed) {
				setVisible(false);
				this.disposed = true;
				this.ltTrayIcon.dispose();
			}
		}

	}

}

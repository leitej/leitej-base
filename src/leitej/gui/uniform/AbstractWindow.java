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

package leitej.gui.uniform;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.gui.exception.GuiLtException;
import leitej.gui.uniform.model.Dimension;
import leitej.gui.uniform.model.LocationEnum;
import leitej.gui.util.GraphicsUtil;
import leitej.log.Logger;
import leitej.util.AgnosticUtil;
import leitej.util.data.Invoke;
import leitej.util.data.InvokeItf;
import leitej.util.machine.ShutdownHookUtil;

/**
 *
 * @author Julio Leite
 */
abstract class AbstractWindow {

	static {
		GraphicsUtil.setSystemLookAndFeel();
	}

	private static final Logger LOG = Logger.getInstance();

	private LtWindow ltWindow;
	private LocationEnum location;
	private Integer locationMargin;
	private final List<Thread> threadsBlocked;
	private volatile boolean disposed;
	private final InvokeItf closeAtShutdownInvoke;

	protected AbstractWindow(final Window owner, final GraphicsConfiguration gc)
			throws InterruptedException, GuiLtException {
		try {
			this.disposed = false;
			this.threadsBlocked = new ArrayList<>();
			final AbstractWindow me = this;
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					AbstractWindow.this.ltWindow = new LtWindow(me, owner, gc);
					final Component glassPane = getGlassPane();
					if (glassPane != null) {
						AbstractWindow.this.ltWindow.setGlassPane(glassPane);
					}
					AbstractWindow.this.ltWindow.getContentPane().add(getMainComponent(), BorderLayout.CENTER);
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

	protected abstract Component getMainComponent();

	protected Component getGlassPane() {
		//
		return null;
	}

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public void pack() throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					AbstractWindow.this.ltWindow.pack();
					if (AbstractWindow.this.location != null) {
						if (LocationEnum.AUTO.equals(AbstractWindow.this.location)) {
							AbstractWindow.this.ltWindow.setLocationByPlatform(true);
						} else {
							int x;
							int y;
							final int margin = ((AbstractWindow.this.locationMargin == null) ? 0
									: AbstractWindow.this.locationMargin);
							final Rectangle defaultScreenInsetBounds = GraphicsUtil.defaultScreenDeviceInsetBounds();
							final int ltWindowWidth = (int) AbstractWindow.this.ltWindow.getSize().getWidth();
							final int ltWindowHeigh = (int) AbstractWindow.this.ltWindow.getSize().getHeight();
							switch (AbstractWindow.this.location) {
							case TOP_LEFT:
								x = defaultScreenInsetBounds.x + margin;
								y = defaultScreenInsetBounds.y + margin;
								break;
							case TOP_CENTER:
								x = defaultScreenInsetBounds.x + defaultScreenInsetBounds.width / 2 - ltWindowWidth / 2;
								y = defaultScreenInsetBounds.y + margin;
								break;
							case TOP_RIGHT:
								x = defaultScreenInsetBounds.x + defaultScreenInsetBounds.width - ltWindowWidth
										- margin;
								y = defaultScreenInsetBounds.y + margin;
								break;
							case LEFT_CENTER:
								x = defaultScreenInsetBounds.x + margin;
								y = defaultScreenInsetBounds.y + defaultScreenInsetBounds.height / 2
										- ltWindowHeigh / 2;
								break;
							case CENTER:
								x = defaultScreenInsetBounds.x + defaultScreenInsetBounds.width / 2 - ltWindowWidth / 2;
								y = defaultScreenInsetBounds.y + defaultScreenInsetBounds.height / 2
										- ltWindowHeigh / 2;
								break;
							case RIGHT_CENTER:
								x = defaultScreenInsetBounds.x + defaultScreenInsetBounds.width - ltWindowWidth
										- margin;
								y = defaultScreenInsetBounds.y + defaultScreenInsetBounds.height / 2
										- ltWindowHeigh / 2;
								break;
							case BOTTOM_LEFT:
								x = defaultScreenInsetBounds.x + margin;
								y = defaultScreenInsetBounds.y + defaultScreenInsetBounds.height - ltWindowHeigh
										- margin;
								break;
							case BOTTOM_CENTER:
								x = defaultScreenInsetBounds.x + defaultScreenInsetBounds.width / 2 - ltWindowWidth / 2;
								y = defaultScreenInsetBounds.y + defaultScreenInsetBounds.height - ltWindowHeigh
										- margin;
								break;
							case BOTTOM_RIGHT:
								x = defaultScreenInsetBounds.x + defaultScreenInsetBounds.width - ltWindowWidth
										- margin;
								y = defaultScreenInsetBounds.y + defaultScreenInsetBounds.height - ltWindowHeigh
										- margin;
								break;
							default:
								throw new ImplementationLtRtException();
							}
							AbstractWindow.this.ltWindow.setLocation(x, y);
						}
					}
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	/**
	 *
	 * @param location
	 * @param locationMargin
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	void setLocation(final LocationEnum location, final Integer locationMargin)
			throws GuiLtException, InterruptedException {
		this.location = location;
		this.locationMargin = locationMargin;
	}

	/**
	 *
	 * @param dimension
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public void setSize(final Dimension dimension) throws InterruptedException, GuiLtException {
		if (dimension != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						AbstractWindow.this.ltWindow
								.setSize(new java.awt.Dimension(dimension.getWidth(), dimension.getHeight()));
					}
				});
			} catch (final InvocationTargetException e) {
				throw new GuiLtException(e.getCause(), e.getMessage());
			}
		} else {
			pack();
		}
	}

	/**
	 *
	 * @param alwaysOnTop
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public void setAlwaysOnTop(final boolean alwaysOnTop) throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					AbstractWindow.this.ltWindow.setAlwaysOnTop(alwaysOnTop);
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	protected static final String METHOD_HIDE = "hide";

	/**
	 *
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public void hide() throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					AbstractWindow.this.ltWindow.setVisible(false);
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
	public void unhide() throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					AbstractWindow.this.ltWindow.setVisible(true);
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
	public void close() throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					AbstractWindow.this.ltWindow.dispose();
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	protected boolean isDisposed() {
		return this.disposed;
	}

	protected void dispose() {
		removeCloseAsyncInvokeFromShutdownHook();
		this.disposed = true;
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
		if (!this.disposed) {
			synchronized (this.threadsBlocked) {
				this.threadsBlocked.add(Thread.currentThread());
			}
			try {
				while (!this.disposed) {
					try {
						Thread.sleep(10000);
					} catch (final InterruptedException e) {
						if (!this.disposed) {
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

	private static final class LtWindow extends JWindow {

		private static final long serialVersionUID = 1419090879903411278L;

		private final AbstractWindow window;

		/**
		 *
		 * @param window
		 * @param owner
		 * @param gc
		 * @throws HeadlessException        if
		 *                                  <code>GraphicsEnvironment.isHeadless()</code>
		 *                                  returns true.
		 * @throws IllegalArgumentException if <code>gc</code> is not from a screen
		 *                                  device
		 */
		private LtWindow(final AbstractWindow window, final Window owner, final GraphicsConfiguration gc)
				throws IllegalArgumentException {
			super(owner, gc);
			this.window = window;
		}

		@Override
		public void dispose() {
			super.dispose();
			this.window.dispose();
		}

	}

}

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
import java.awt.Image;
import java.awt.MenuBar;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import leitej.LtIcon;
import leitej.exception.GuiLtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.gui.uniform.model.Dimension;
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
abstract class AbstractFrame {

	static {
		GraphicsUtil.setSystemLookAndFeel();
	}

	private static final Logger LOG = Logger.getInstance();

	private LtFrame ltFrame;
	private final List<Thread> threadsBlocked;
	private volatile boolean disposed;
	private final InvokeItf closeAtShutdownInvoke;

	/**
	 *
	 * @param closeOperation
	 * @param gc
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	protected AbstractFrame(final CloseOperationEnum closeOperation, final GraphicsConfiguration gc)
			throws InterruptedException, GuiLtException {
		try {
			this.disposed = false;
			this.threadsBlocked = new ArrayList<>();
			final AbstractFrame me = this;
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					AbstractFrame.this.ltFrame = new LtFrame(me, gc);
					if (closeOperation == null) {
						AbstractFrame.this.ltFrame
								.setDefaultCloseOperation(CloseOperationEnum.HIDE_ON_CLOSE.windowConstants());
					} else {
						AbstractFrame.this.ltFrame.setDefaultCloseOperation(closeOperation.windowConstants());
					}
					AbstractFrame.this.ltFrame.setMenuBar(getMenuBar());
					final Component glassPane = getGlassPane();
					if (glassPane != null) {
						AbstractFrame.this.ltFrame.setGlassPane(glassPane);
					}
					AbstractFrame.this.ltFrame.getContentPane().add(getMainComponent(), BorderLayout.CENTER);
					AbstractFrame.this.ltFrame.setLocationByPlatform(true);
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

	protected MenuBar getMenuBar() {
		// //Create the menu bar. Make it have a green background.
		// JMenuBar greenMenuBar = new JMenuBar();
		// greenMenuBar.setOpaque(true);
		// greenMenuBar.setBackground(new Color(154, 165, 127));
		// greenMenuBar.setPreferredSize(new Dimension(200, 20));
		return null;
	}

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
					AbstractFrame.this.ltFrame.pack();
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
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
						AbstractFrame.this.ltFrame
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
	 * @param title
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public void setTitle(final String title) throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					AbstractFrame.this.ltFrame.setTitle(title);
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
	}

	/**
	 *
	 * @param iconImage
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public void setIconImage(final Image iconImage) throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					if (iconImage == null) {
						AbstractFrame.this.ltFrame.setIconImage(iconImage);
						// jFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(iconFileName));
					} else {
						AbstractFrame.this.ltFrame
								.setIconImage(Toolkit.getDefaultToolkit().createImage(LtIcon.DATA_128_BG_W));
					}
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
	public void hide() throws InterruptedException, GuiLtException {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					AbstractFrame.this.ltFrame.setVisible(false);
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
					AbstractFrame.this.ltFrame.setVisible(true);
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
					AbstractFrame.this.ltFrame.dispose();
				}
			});
		} catch (final InvocationTargetException e) {
			throw new GuiLtException(e.getCause(), e.getMessage());
		}
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

	private static final class LtFrame extends JFrame {

		private static final long serialVersionUID = 5939704497817332427L;

		private final AbstractFrame frame;

		/**
		 *
		 * @param frame
		 * @param gc
		 * @throws IllegalArgumentException if <code>gc</code> is not from a screen
		 *                                  device. This exception is always thrown when
		 *                                  GraphicsEnvironment.isHeadless() returns
		 *                                  true
		 */
		private LtFrame(final AbstractFrame frame, final GraphicsConfiguration gc) throws IllegalArgumentException {
			super(gc);
			this.frame = frame;
		}

		@Override
		public void dispose() {
			super.dispose();
			this.frame.dispose();
		}

	}

}

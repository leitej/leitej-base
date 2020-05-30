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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import leitej.exception.ClosedLtRtException;
import leitej.exception.GuiLtException;
import leitej.exception.ImplementationLtRtException;
import leitej.gui.event.LtMouseListener;
import leitej.gui.event.LtMouseMotionAdapter;
import leitej.gui.uniform.model.ActionInput;
import leitej.gui.uniform.model.Data;
import leitej.gui.uniform.model.ElementUpdate;
import leitej.log.Logger;
import leitej.util.AgnosticUtil;
import leitej.util.data.InvokeSignature;
import leitej.util.data.QueueBlockingFIFO;

/**
 *
 * @author Julio Leite
 */
public final class InsetData {

	private static final Logger LOG = Logger.getInstance();

	private static final Color GLASS_PANE_BACKGROUND_COLOR = new Color(0, 0, 0, 15);

	private final Map<String, JComponent> componentMapById;
	private final Map<String, Map<String, OperationGroup>> operationGroupMapByIndexAndId;
	private final Map<JComponent, OperationGroup> operationGroupMapByComponent;
	private final JPanel mainComponent;
	private final WaitGlassPane waitGlassPane;
	private final QueueBlockingFIFO<ActionInput> inputList;

	InsetData() {
		this.componentMapById = new HashMap<>();
		this.operationGroupMapByIndexAndId = new HashMap<>();
		this.operationGroupMapByComponent = new HashMap<>();
		this.inputList = new QueueBlockingFIFO<>(6);
		this.mainComponent = new JPanel(new BorderLayout());
		this.waitGlassPane = new WaitGlassPane(this);
		LOG.trace("initialized");
	}

	final void close() {
		this.inputList.close();
	}

	/*
	 * Component
	 */

	final Component mainComponent() {
		return this.mainComponent;
	}

	final Map<String, JComponent> getComponentMapById() {
		return this.componentMapById;
	}

	final Map<String, Map<String, OperationGroup>> getOperationGroupMapByIndexAndId() {
		return this.operationGroupMapByIndexAndId;
	}

	final Map<JComponent, OperationGroup> getOperationGroupMapByComponent() {
		return this.operationGroupMapByComponent;
	}

	final void set(final Data data) throws InterruptedException, GuiLtException {
		synchronized (this.mainComponent) {
			final InsetData me = this;
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						UniformGuiBus.set(me, data);
					}
				});
			} catch (final InvocationTargetException e) {
				throw new GuiLtException(e.getCause(), e.getMessage());
			}
		}
	}

	final void update(final ElementUpdate... elements) throws InterruptedException, GuiLtException {
		synchronized (this.mainComponent) {
			if (elements != null && elements.length != 0) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							for (final ElementUpdate element : elements) {
								UniformGuiBus.update(element, InsetData.this.componentMapById);
							}
						}
					});
				} catch (final InvocationTargetException e) {
					throw new GuiLtException(e.getCause(), e.getMessage());
				}
			}
		}
	}

	/*
	 * GlassPane
	 */

	final Component glassPane() {
		return this.waitGlassPane;
	}

	final void setWaitMode(final boolean b) throws InterruptedException, GuiLtException {
		synchronized (this.waitGlassPane) {
			if (b || this.inputList.isEmpty()) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							if (InsetData.this.waitGlassPane.isVisible() != b) {
								InsetData.this.waitGlassPane.setPoint(null);
								InsetData.this.waitGlassPane.setVisible(b);
							}
						}
					});
				} catch (final InvocationTargetException e) {
					throw new GuiLtException(e.getCause(), e.getMessage());
				}
			}
		}
	}

	private static final String METHOD_NAME_REFRESH_GLASS_PANE = "refreshGlassPane";

	public final void refreshGlassPane(final MouseEvent e) throws InterruptedException, GuiLtException {
		synchronized (this.waitGlassPane) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						InsetData.this.waitGlassPane.setPoint(e.getPoint());
						InsetData.this.waitGlassPane.repaint();
					}
				});
			} catch (final InvocationTargetException e1) {
				throw new GuiLtException(e1.getMessage(), e1.getCause());
			}
		}
	}

	private final class WaitGlassPane extends JComponent {

		private static final long serialVersionUID = 7187044610458615485L;

		private Point point;

		private WaitGlassPane(final InsetData insetData) {
			try {
				final LtMouseListener listener = new LtMouseListener(new InvokeSignature(insetData,
						AgnosticUtil.getMethod(insetData, METHOD_NAME_REFRESH_GLASS_PANE, MouseEvent.class)));
				addMouseListener(listener);
				final LtMouseMotionAdapter motionListener = new LtMouseMotionAdapter(
						new InvokeSignature(insetData,
								AgnosticUtil.getMethod(insetData, METHOD_NAME_REFRESH_GLASS_PANE, MouseEvent.class)),
						null);
				addMouseMotionListener(motionListener);
				setPreferredSize(new Dimension(640, 480));
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				// setOpaque(true); //for default background paint
				setBackground(GLASS_PANE_BACKGROUND_COLOR);
				setForeground(Color.red);
			} catch (final NoSuchMethodException e) {
				throw new ImplementationLtRtException(e);
			}
		}

		private void setPoint(final Point p) {
			this.point = p;
		}

		@Override
		protected final void paintComponent(final Graphics g) {
			// super.paintComponent(g); //for default background paint
			grabFocus();
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			if (this.point != null) {
				g.setColor(getForeground());
				g.fillOval(this.point.x - 10, this.point.y - 10, 20, 20);
			}
		}

	}

	/*
	 * Listener
	 */

	static final String METHOD_NAME_ENTER_KEY_TYPED_LISTENER = "enterKeyTypedListener";

	public void enterKeyTypedListener(final KeyEvent keyEvent) throws GuiLtException, InterruptedException {
		final InsetData me = this;
		synchronized (this.operationGroupMapByComponent) {
			setWaitMode(true);
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						UniformGuiBus.enterKeyTypedListener(keyEvent, me);
					}
				});
			} catch (final InvocationTargetException e) {
				throw new GuiLtException(e.getCause(), e.getMessage());
			}
		}
	}

	static final String METHOD_NAME_MOUSE_CLICKED_LISTENER = "mouseClickedListener";

	public void mouseClickedListener(final MouseEvent mouseEvent) throws GuiLtException, InterruptedException {
		final InsetData me = this;
		synchronized (this.operationGroupMapByComponent) {
			setWaitMode(true);
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						UniformGuiBus.mouseClickedListener(mouseEvent, me);
					}
				});
			} catch (final InvocationTargetException e) {
				throw new GuiLtException(e.getCause(), e.getMessage());
			}
		}
	}

	static final String METHOD_NAME_FOCUS_GAINED_LISTENER = "focusGainedListener";

	public void focusGainedListener(final FocusEvent focusEvent) throws GuiLtException, InterruptedException {
		final InsetData me = this;
		synchronized (this.operationGroupMapByComponent) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						UniformGuiBus.focusGainedListener(focusEvent, me);
					}
				});
			} catch (final InvocationTargetException e) {
				throw new GuiLtException(e.getCause(), e.getMessage());
			}
		}
	}

	/*
	 * Input
	 */

	void addInput(final ActionInput actionInput) throws NullPointerException, InterruptedException {
		this.inputList.offer(actionInput);
	}

	ActionInput pollActionInput() throws ClosedLtRtException, GuiLtException, InterruptedException {
		final ActionInput result = this.inputList.poll();
		setWaitMode(false);
		return result;
	}

}

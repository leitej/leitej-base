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

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;

import leitej.exception.ClosedLtRtException;
import leitej.gui.exception.GuiLtException;
import leitej.gui.uniform.model.ActionInput;
import leitej.gui.uniform.model.ElementUpdate;
import leitej.gui.uniform.model.Frame;
import leitej.gui.uniform.model.MetaData;
import leitej.log.Logger;

/**
 *
 * @author Julio Leite
 */
public class UniformFrame extends AbstractFrame {

	private static final Logger LOG = Logger.getInstance();

	private InsetData insetData;

	/**
	 *
	 * @param closeOperation
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public UniformFrame(final CloseOperationEnum closeOperation) throws InterruptedException, GuiLtException {
		this(null, closeOperation, null);
	}

	/**
	 *
	 * @param frame
	 * @param closeOperation
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public UniformFrame(final Frame frame, final CloseOperationEnum closeOperation)
			throws InterruptedException, GuiLtException {
		this(frame, closeOperation, null);
	}

	/**
	 *
	 * @param frame
	 * @param closeOperation
	 * @param gc
	 * @throws InterruptedException if we're interrupted while waiting for the event
	 *                              dispatching thread to finish executing
	 * @throws GuiLtException       if an exception is thrown while running the
	 *                              event dispatching thread
	 */
	public UniformFrame(final Frame frame, final CloseOperationEnum closeOperation, final GraphicsConfiguration gc)
			throws InterruptedException, GuiLtException {
		super(closeOperation, gc);
		if (this.insetData == null) {
			this.insetData = new InsetData();
		}
		if (frame != null) {
			set(frame);
		}
		LOG.trace("initialized");
	}

	@Override
	protected void dispose() {
		super.dispose();
		this.insetData.close();
	}

	/*
	 * Component
	 */

	@Override
	protected final Component getMainComponent() {
		if (this.insetData == null) {
			this.insetData = new InsetData();
		}
		return this.insetData.mainComponent();
	}

	public final void set(final Frame frame) throws NullPointerException, InterruptedException, GuiLtException {
		synchronized (this.insetData) {
			this.insetData.setWaitMode(true);
			if (frame.getMetaData() != null) {
				set(frame.getMetaData());
			}
			if (frame.getData() != null) {
				this.insetData.set(frame.getData());
			}
			if (frame.getMetaData() == null || frame.getMetaData().getDimension() == null) {
				pack();
			}
			this.insetData.setWaitMode(false);
		}
	}

	private final void set(final MetaData metadata) throws InterruptedException, GuiLtException {
		if (metadata.getTitle() != null) {
			setTitle(metadata.getTitle());
		}
		if (metadata.getIconImage() != null) {
			setIconImage(Toolkit.getDefaultToolkit().createImage(metadata.getIconImage()));
		}
		if (metadata.getDimension() != null) {
			setSize(metadata.getDimension());
		}
	}

	public final void update(final ElementUpdate... elements) throws InterruptedException, GuiLtException {
		update(false, elements);
	}

	public final void fastUpdate(final ElementUpdate... elements) throws InterruptedException, GuiLtException {
		update(true, elements);
	}

	private final void update(final boolean fast, final ElementUpdate... elements)
			throws InterruptedException, GuiLtException {
		synchronized (this.insetData) {
			if (elements != null && elements.length != 0) {
				if (!fast) {
					this.insetData.setWaitMode(true);
				}
				this.insetData.update(elements);
			}
			this.insetData.setWaitMode(false);
		}
	}

	/*
	 * GlassPane
	 */

	@Override
	protected final Component getGlassPane() {
		if (this.insetData == null) {
			this.insetData = new InsetData();
		}
		return this.insetData.glassPane();
	}

	/*
	 * Input
	 */

	public ActionInput pollActionInput() throws ClosedLtRtException, GuiLtException, InterruptedException {
		return this.insetData.pollActionInput();
	}

}

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

import leitej.exception.ClosedLtRtException;
import leitej.gui.exception.GuiLtException;
import leitej.gui.uniform.model.ActionInput;
import leitej.gui.uniform.model.ElementUpdate;
import leitej.gui.uniform.model.Window;
import leitej.gui.uniform.model.WindowMetaData;
import leitej.log.Logger;

/**
 *
 * @author Julio Leite
 */
public class UniformWindow extends AbstractWindow {

	private static final Logger LOG = Logger.getInstance();

	private InsetData insetData;

	/**
	 * @param owner
	 * @param gc
	 * @throws InterruptedException
	 * @throws GuiLtException
	 */
	protected UniformWindow(final Window window, final java.awt.Window owner, final GraphicsConfiguration gc)
			throws InterruptedException, GuiLtException {
		super(owner, gc);
		if (this.insetData == null) {
			this.insetData = new InsetData();
		}
		if (window != null) {
			set(window);
		}
		LOG.trace("lt.Init");
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

	public final void set(final Window window) throws NullPointerException, InterruptedException, GuiLtException {
		synchronized (this.insetData) {
			this.insetData.setWaitMode(true);
			if (window.getWindowMetaData() != null) {
				set(window.getWindowMetaData());
			}
			if (window.getData() != null) {
				this.insetData.set(window.getData());
			}
			pack();
			this.insetData.setWaitMode(false);
		}
	}

	private final void set(final WindowMetaData windowMetadata) throws InterruptedException, GuiLtException {
		if (windowMetadata.getAlwaysOnTop() != null) {
			setAlwaysOnTop(windowMetadata.getAlwaysOnTop());
		}
		setLocation(windowMetadata.getLocation(), windowMetadata.getLocationMargin());
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

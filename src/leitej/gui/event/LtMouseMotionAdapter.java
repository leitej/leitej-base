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

package leitej.gui.event;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.LtException;
import leitej.log.Logger;
import leitej.thread.PoolAgnosticThread;
import leitej.thread.XThreadData;
import leitej.util.data.InvokeSignature;

/**
 *
 * @author Julio Leite
 */
public final class LtMouseMotionAdapter extends MouseMotionAdapter {

	private static final Logger LOG = Logger.getInstance();
	private static final PoolAgnosticThread atPool = ListenerConstant.ATP_EVENT_LISTENER;

	private final InvokeSignature mouseDragged;
	private final boolean mouseDraggedWithEvent;
	private final InvokeSignature mouseMoved;
	private final boolean mouseMovedWithEvent;

	public LtMouseMotionAdapter(final InvokeSignature mouseDragged, final InvokeSignature mouseMoved) {
		if (mouseDragged != null) {
			if (!mouseDragged.matchArguments()) {
				if (!mouseDragged.matchArguments(MouseEvent.class)) {
					throw new IllegalArgumentLtRtException("Argument '#0' has to sign a method with no argument or an argument of class '#1' to be called", "mouseDragged",
							MouseEvent.class);
				}
				this.mouseDraggedWithEvent = true;
			} else {
				this.mouseDraggedWithEvent = false;
			}
			this.mouseDragged = mouseDragged;
		} else {
			this.mouseDragged = null;
			this.mouseDraggedWithEvent = false;
		}
		if (mouseMoved != null) {
			if (!mouseMoved.matchArguments()) {
				if (!mouseMoved.matchArguments(MouseEvent.class)) {
					throw new IllegalArgumentLtRtException("Argument '#0' has to sign a method with no argument or an argument of class '#1' to be called", "mouseMoved",
							MouseEvent.class);
				}
				this.mouseMovedWithEvent = true;
			} else {
				this.mouseMovedWithEvent = false;
			}
			this.mouseMoved = mouseMoved;
		} else {
			this.mouseMoved = null;
			this.mouseMovedWithEvent = false;
		}
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if (this.mouseDragged != null) {
			try {
				if (this.mouseDraggedWithEvent) {
					atPool.workOn(new XThreadData(this.mouseDragged.getInvoke(e)));
				} else {
					atPool.workOn(new XThreadData(this.mouseDragged.getInvoke()));
				}
			} catch (final LtException ex) {
				LOG.error("#0", ex);
			}
		}
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		if (this.mouseMoved != null) {
			try {
				if (this.mouseMovedWithEvent) {
					atPool.workOn(new XThreadData(this.mouseMoved.getInvoke(e)));
				} else {
					atPool.workOn(new XThreadData(this.mouseMoved.getInvoke()));
				}
			} catch (final LtException ex) {
				LOG.error("#0", ex);
			}
		}
	}

}

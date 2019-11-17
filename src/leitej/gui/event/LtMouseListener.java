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
import java.awt.event.MouseListener;

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
public final class LtMouseListener implements MouseListener {

	private static final Logger LOG = Logger.getInstance();
	private static final PoolAgnosticThread atPool = ListenerConstant.ATP_EVENT_LISTENER;

	private final InvokeSignature mouseClicked;
	private final boolean mouseClickedWithEvent;
	private final InvokeSignature mouseEntered;
	private final boolean mouseEnteredWithEvent;
	private final InvokeSignature mouseExited;
	private final boolean mouseExitedWithEvent;
	private final InvokeSignature mousePressed;
	private final boolean mousePressedWithEvent;
	private final InvokeSignature mouseReleased;
	private final boolean mouseReleasedWithEvent;

	public LtMouseListener(final InvokeSignature mouseClicked) {
		this(mouseClicked, null, null, null, null);
	}

	public LtMouseListener(final InvokeSignature mouseClicked, final InvokeSignature mouseEntered,
			final InvokeSignature mouseExited, final InvokeSignature mousePressed,
			final InvokeSignature mouseReleased) {
		if (mouseClicked != null) {
			if (!mouseClicked.matchArguments()) {
				if (!mouseClicked.matchArguments(MouseEvent.class)) {
					throw new IllegalArgumentLtRtException("lt.GUIEventMethodWrongArgs", "mouseClicked",
							MouseEvent.class);
				}
				this.mouseClickedWithEvent = true;
			} else {
				this.mouseClickedWithEvent = false;
			}
			this.mouseClicked = mouseClicked;
		} else {
			this.mouseClicked = null;
			this.mouseClickedWithEvent = false;
		}
		if (mouseEntered != null) {
			if (!mouseEntered.matchArguments()) {
				if (!mouseEntered.matchArguments(MouseEvent.class)) {
					throw new IllegalArgumentLtRtException("lt.GUIEventMethodWrongArgs", "mouseEntered",
							MouseEvent.class);
				}
				this.mouseEnteredWithEvent = true;
			} else {
				this.mouseEnteredWithEvent = false;
			}
			this.mouseEntered = mouseEntered;
		} else {
			this.mouseEntered = null;
			this.mouseEnteredWithEvent = false;
		}
		if (mouseExited != null) {
			if (!mouseExited.matchArguments()) {
				if (!mouseExited.matchArguments(MouseEvent.class)) {
					throw new IllegalArgumentLtRtException("lt.GUIEventMethodWrongArgs", "mouseExited",
							MouseEvent.class);
				}
				this.mouseExitedWithEvent = true;
			} else {
				this.mouseExitedWithEvent = false;
			}
			this.mouseExited = mouseExited;
		} else {
			this.mouseExited = null;
			this.mouseExitedWithEvent = false;
		}
		if (mousePressed != null) {
			if (!mousePressed.matchArguments()) {
				if (!mousePressed.matchArguments(MouseEvent.class)) {
					throw new IllegalArgumentLtRtException("lt.GUIEventMethodWrongArgs", "mousePressed",
							MouseEvent.class);
				}
				this.mousePressedWithEvent = true;
			} else {
				this.mousePressedWithEvent = false;
			}
			this.mousePressed = mousePressed;
		} else {
			this.mousePressed = null;
			this.mousePressedWithEvent = false;
		}
		if (mousePressed != null) {
			if (!mouseReleased.matchArguments()) {
				if (!mouseReleased.matchArguments(MouseEvent.class)) {
					throw new IllegalArgumentLtRtException("lt.GUIEventMethodWrongArgs", "mouseReleased",
							MouseEvent.class);
				}
				this.mouseReleasedWithEvent = true;
			} else {
				this.mouseReleasedWithEvent = false;
			}
			this.mouseReleased = mouseReleased;
		} else {
			this.mouseReleased = null;
			this.mouseReleasedWithEvent = false;
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (this.mouseClicked != null) {
			try {
				if (this.mouseClickedWithEvent) {
					atPool.workOn(new XThreadData(this.mouseClicked.getInvoke(e)));
				} else {
					atPool.workOn(new XThreadData(this.mouseClicked.getInvoke()));
				}
			} catch (final LtException ex) {
				LOG.error("#0", ex);
			}
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		if (this.mouseEntered != null) {
			try {
				if (this.mouseEnteredWithEvent) {
					atPool.workOn(new XThreadData(this.mouseEntered.getInvoke(e)));
				} else {
					atPool.workOn(new XThreadData(this.mouseEntered.getInvoke()));
				}
			} catch (final LtException ex) {
				LOG.error("#0", ex);
			}
		}
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		if (this.mouseExited != null) {
			try {
				if (this.mouseExitedWithEvent) {
					atPool.workOn(new XThreadData(this.mouseExited.getInvoke(e)));
				} else {
					atPool.workOn(new XThreadData(this.mouseExited.getInvoke()));
				}
			} catch (final LtException ex) {
				LOG.error("#0", ex);
			}
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (this.mousePressed != null) {
			try {
				if (this.mousePressedWithEvent) {
					atPool.workOn(new XThreadData(this.mousePressed.getInvoke(e)));
				} else {
					atPool.workOn(new XThreadData(this.mousePressed.getInvoke()));
				}
			} catch (final LtException ex) {
				LOG.error("#0", ex);
			}
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (this.mouseReleased != null) {
			try {
				if (this.mouseReleasedWithEvent) {
					atPool.workOn(new XThreadData(this.mouseReleased.getInvoke(e)));
				} else {
					atPool.workOn(new XThreadData(this.mouseReleased.getInvoke()));
				}
			} catch (final LtException ex) {
				LOG.error("#0", ex);
			}
		}
	}

}

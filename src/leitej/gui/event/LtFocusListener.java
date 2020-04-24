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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

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
public final class LtFocusListener implements FocusListener {

	private static final Logger LOG = Logger.getInstance();
	private static final PoolAgnosticThread atPool = ListenerConstant.ATP_EVENT_LISTENER;

	private final InvokeSignature focusGained;
	private final boolean focusGainedWithEvent;
	private final InvokeSignature focusLost;
	private final boolean focusLostWithEvent;

	public LtFocusListener(final InvokeSignature focusGained, final InvokeSignature focusLost) {
		if (focusGained != null) {
			if (!focusGained.matchArguments()) {
				if (!focusGained.matchArguments(FocusEvent.class)) {
					throw new IllegalArgumentLtRtException("Argument '#0' has to sign a method with no argument or an argument of class '#1' to be called", "focusGained",
							FocusEvent.class);
				}
				this.focusGainedWithEvent = true;
			} else {
				this.focusGainedWithEvent = false;
			}
			this.focusGained = focusGained;
		} else {
			this.focusGained = null;
			this.focusGainedWithEvent = false;
		}
		if (focusLost != null) {
			if (!focusLost.matchArguments()) {
				if (!focusLost.matchArguments(FocusEvent.class)) {
					throw new IllegalArgumentLtRtException("Argument '#0' has to sign a method with no argument or an argument of class '#1' to be called", "focusLost", FocusEvent.class);
				}
				this.focusLostWithEvent = true;
			} else {
				this.focusLostWithEvent = false;
			}
			this.focusLost = focusLost;
		} else {
			this.focusLost = null;
			this.focusLostWithEvent = false;
		}
	}

	@Override
	public void focusGained(final FocusEvent e) {
		if (this.focusGained != null) {
			try {
				if (this.focusGainedWithEvent) {
					atPool.workOn(new XThreadData(this.focusGained.getInvoke(e)));
				} else {
					atPool.workOn(new XThreadData(this.focusGained.getInvoke()));
				}
			} catch (final LtException ex) {
				LOG.error("#0", ex);
			}
		}
	}

	@Override
	public void focusLost(final FocusEvent e) {
		if (this.focusLost != null) {
			try {
				if (this.focusLostWithEvent) {
					atPool.workOn(new XThreadData(this.focusLost.getInvoke(e)));
				} else {
					atPool.workOn(new XThreadData(this.focusLost.getInvoke()));
				}
			} catch (final LtException ex) {
				LOG.error("#0", ex);
			}
		}
	}

}

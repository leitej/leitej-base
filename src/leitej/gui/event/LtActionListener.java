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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
public final class LtActionListener implements ActionListener {

	private static final Logger LOG = Logger.getInstance();
	private static final PoolAgnosticThread atPool = ListenerConstant.ATP_EVENT_LISTENER;

	private final InvokeSignature actionPerformed;
	private final boolean actionPerformedWithEvent;

	public LtActionListener(final InvokeSignature actionPerformed) {
		if (actionPerformed != null) {
			if (!actionPerformed.matchArguments()) {
				if (!actionPerformed.matchArguments(ActionEvent.class)) {
					throw new IllegalArgumentLtRtException("lt.GUIEventMethodWrongArgs", "actionPerformed",
							ActionEvent.class);
				}
				this.actionPerformedWithEvent = true;
			} else {
				this.actionPerformedWithEvent = false;
			}
			this.actionPerformed = actionPerformed;
		} else {
			this.actionPerformed = null;
			this.actionPerformedWithEvent = false;
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (this.actionPerformed != null) {
			try {
				if (this.actionPerformedWithEvent) {
					atPool.workOn(new XThreadData(this.actionPerformed.getInvoke(e)));
				} else {
					atPool.workOn(new XThreadData(this.actionPerformed.getInvoke()));
				}
			} catch (final LtException ex) {
				LOG.error("#0", ex);
			}
		}
	}

}

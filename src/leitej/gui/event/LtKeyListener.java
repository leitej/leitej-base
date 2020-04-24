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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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
public final class LtKeyListener implements KeyListener {

	private static final Logger LOG = Logger.getInstance();
	private static final PoolAgnosticThread atPool = ListenerConstant.ATP_EVENT_LISTENER;

	private final InvokeSignature invokeKeyTyped;
	private final boolean invokeKeyTypedWithEvent;
	private final boolean justEnterKey;
	private final InvokeSignature invokeKeyPressed;
	private final boolean invokeKeyPressedWithEvent;
	private final InvokeSignature invokeKeyReleased;
	private final boolean invokeKeyReleasedWithEvent;

	public LtKeyListener(final InvokeSignature invokeKeyTyped) {
		this(invokeKeyTyped, null, null, false);
	}

	public LtKeyListener(final InvokeSignature invokeKeyTyped, final boolean justEnterKey) {
		this(invokeKeyTyped, null, null, justEnterKey);
	}

	public LtKeyListener(final InvokeSignature invokeKeyTyped, final InvokeSignature invokeKeyPressed,
			final InvokeSignature invokeKeyReleased) {
		this(invokeKeyTyped, invokeKeyPressed, invokeKeyReleased, false);
	}

	private LtKeyListener(final InvokeSignature invokeKeyTyped, final InvokeSignature invokeKeyPressed,
			final InvokeSignature invokeKeyReleased, final boolean justEnterKey) {
		if (invokeKeyTyped != null) {
			if (!invokeKeyTyped.matchArguments()) {
				if (!invokeKeyTyped.matchArguments(KeyEvent.class)) {
					throw new IllegalArgumentLtRtException("Argument '#0' has to sign a method with no argument or an argument of class '#1' to be called", "invokeKeyTyped",
							KeyEvent.class);
				}
				this.invokeKeyTypedWithEvent = true;
			} else {
				this.invokeKeyTypedWithEvent = false;
			}
			this.invokeKeyTyped = invokeKeyTyped;
		} else {
			this.invokeKeyTyped = null;
			this.invokeKeyTypedWithEvent = false;
		}
		if (invokeKeyPressed != null) {
			if (!invokeKeyPressed.matchArguments()) {
				if (!invokeKeyPressed.matchArguments(KeyEvent.class)) {
					throw new IllegalArgumentLtRtException("Argument '#0' has to sign a method with no argument or an argument of class '#1' to be called", "invokeKeyPressed",
							KeyEvent.class);
				}
				this.invokeKeyPressedWithEvent = true;
			} else {
				this.invokeKeyPressedWithEvent = false;
			}
			this.invokeKeyPressed = invokeKeyPressed;
		} else {
			this.invokeKeyPressed = null;
			this.invokeKeyPressedWithEvent = false;
		}
		if (invokeKeyReleased != null) {
			if (!invokeKeyReleased.matchArguments()) {
				if (!invokeKeyReleased.matchArguments(KeyEvent.class)) {
					throw new IllegalArgumentLtRtException("Argument '#0' has to sign a method with no argument or an argument of class '#1' to be called", "invokeKeyReleased",
							KeyEvent.class);
				}
				this.invokeKeyReleasedWithEvent = true;
			} else {
				this.invokeKeyReleasedWithEvent = false;
			}
			this.invokeKeyReleased = invokeKeyReleased;
		} else {
			this.invokeKeyReleased = null;
			this.invokeKeyReleasedWithEvent = false;

		}
		this.justEnterKey = justEnterKey;
	}

	@Override
	public void keyTyped(final KeyEvent e) {
		if (this.invokeKeyTyped != null && ((this.justEnterKey && e.getKeyChar() == '\n') || !this.justEnterKey)) {
			try {
				if (this.invokeKeyTypedWithEvent) {
					atPool.workOn(new XThreadData(this.invokeKeyTyped.getInvoke(e)));
				} else {
					atPool.workOn(new XThreadData(this.invokeKeyTyped.getInvoke()));
				}
			} catch (final LtException ex) {
				LOG.error("#0", ex);
			}
		}
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		if (this.invokeKeyPressed != null) {
			try {
				if (this.invokeKeyPressedWithEvent) {
					atPool.workOn(new XThreadData(this.invokeKeyPressed.getInvoke(e)));
				} else {
					atPool.workOn(new XThreadData(this.invokeKeyPressed.getInvoke()));
				}
			} catch (final LtException ex) {
				LOG.error("#0", ex);
			}
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		if (this.invokeKeyReleased != null) {
			try {
				if (this.invokeKeyReleasedWithEvent) {
					atPool.workOn(new XThreadData(this.invokeKeyReleased.getInvoke(e)));
				} else {
					atPool.workOn(new XThreadData(this.invokeKeyReleased.getInvoke()));
				}
			} catch (final LtException ex) {
				LOG.error("#0", ex);
			}
		}
	}

}

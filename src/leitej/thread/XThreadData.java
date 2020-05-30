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

package leitej.thread;

import java.util.Date;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.util.DateUtil;
import leitej.util.data.TimeTriggerImpl;
import leitej.util.data.TimeTrigger;
import leitej.util.data.InvokeItf;

/**
 * This class uses {@link leitej.util.data.TimeTrigger DateTimerItf} to give
 * the steps of execution.
 *
 * @author Julio Leite
 * @see leitej.thread.ThreadData
 * @see leitej.util.data.InvokeItf
 */
public final class XThreadData extends ThreadData {

	private static final long serialVersionUID = -6506827797853778130L;

	private TimeTrigger dateTimer = null;
	private Date doneDate = null;
	private boolean executed = true;

	/**
	 * Creates a new instance of XThreadData with only one step and now.
	 *
	 * @param invokeData {@link leitej.util.data.InvokeItf InvokeItf} to be executed
	 * @throws IllegalArgumentLtRtException if <code>invokeData</code> is null
	 */
	public XThreadData(final InvokeItf invokeData) {
		this(invokeData, TimeTriggerImpl.getSingleImmediateTrigger(), null, null); // Execute only once
	}

	/**
	 * Creates a new instance of XThreadData.
	 *
	 * @param invokeData {@link leitej.util.data.InvokeItf InvokeItf} to be executed
	 * @param dateTimer  with the steps of execution
	 * @throws IllegalArgumentLtRtException if <code>invokeData</code> is null
	 */
	public XThreadData(final InvokeItf invokeData, final TimeTrigger dateTimer) throws IllegalArgumentLtRtException {
		this(invokeData, dateTimer, null, null);
	}

	/**
	 * Creates a new instance of XThreadData with only one step and now.
	 *
	 * @param invokeData {@link leitej.util.data.InvokeItf InvokeItf} to be executed
	 * @param threadName partial name for the thread when executing
	 *                   <code>invokeData</code>
	 * @throws IllegalArgumentLtRtException if <code>invokeData</code> is null
	 */
	public XThreadData(final InvokeItf invokeData, final String threadName) throws IllegalArgumentLtRtException {
		this(invokeData, TimeTriggerImpl.getSingleImmediateTrigger(), threadName, null); // Execute only once
	}

	/**
	 * Creates a new instance of XThreadData with only one step and now.
	 *
	 * @param invokeData {@link leitej.util.data.InvokeItf InvokeItf} to be executed
	 * @param priority   of thread execution
	 * @throws IllegalArgumentLtRtException if <code>invokeData</code> is null
	 */
	public XThreadData(final InvokeItf invokeData, final ThreadPriorityEnum priority)
			throws IllegalArgumentLtRtException {
		this(invokeData, TimeTriggerImpl.getSingleImmediateTrigger(), null, priority); // Execute only once
	}

	/**
	 * Creates a new instance of XThreadData.
	 *
	 * @param invokeData {@link leitej.util.data.InvokeItf InvokeItf} to be executed
	 * @param dateTimer  with the steps of execution
	 * @param threadName partial name for the thread when executing
	 *                   <code>invokeData</code>
	 * @param priority   of thread execution
	 * @throws IllegalArgumentLtRtException if <code>invokeData</code> is null
	 */
	public XThreadData(final InvokeItf invokeData, final TimeTrigger dateTimer, final String threadName,
			final ThreadPriorityEnum priority) throws IllegalArgumentLtRtException {
		super(invokeData, threadName, priority);
		if (dateTimer == null) {
			this.dateTimer = new TimeTriggerImpl();
		} else {
			this.dateTimer = dateTimer;
		}
	}

	@Override
	public void done() {
		this.executed = true;
		super.done();
	}

	@Override
	public boolean isDone() {
		return this.dateTimer == null
				|| (super.isDone() && (this.executed || this.doneDate == null || (DateUtil.isFuture(this.doneDate))));
	}

	/**
	 * Defines if all steps has already been executed.
	 *
	 * @return boolean
	 */
	public boolean isXDone() {
		return this.dateTimer == null || nextExecTime() == null;
	}

	/**
	 * Defines if is to stop atypically the execution of the next steps.
	 *
	 * @return boolean
	 */
	public boolean isAtypicallyDone() {
		return this.dateTimer == null;
	}

	/**
	 * Call this method will prevent all the next steps execution of
	 * <code>getInvokeData()</code>.
	 */
	synchronized public void stopAtypically() {
		this.dateTimer = null;
	}

	/**
	 * Gives the date of the next execution.
	 *
	 * @return Date of next step
	 */
	synchronized Date nextExecTime() {
		if (this.executed) {
			if (this.dateTimer != null) {
				final Date nextStepTimer = this.dateTimer.nextTrigger();
				if (this.doneDate != null && this.doneDate == nextStepTimer) {
					new ImplementationLtRtException();
				}
				this.doneDate = nextStepTimer;
				if (this.doneDate == null) {
					super.done();
				}
			} else {
				this.doneDate = null;
				super.done();
			}
			this.executed = false;
		}
		return this.doneDate;
	}

}

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

package leitej.util.data;

import java.util.Date;

import leitej.util.DateUtil;

/**
 * This class makes use of {@link java.util.Calendar Calendar} to calculate the
 * periods.
 *
 * @author Julio Leite
 * @see java.util.Calendar
 */
public final class TimeTriggerImpl implements TimeTrigger {

	private static final long serialVersionUID = -5278534120277185092L;

	private final Date initiate; // data de inicio da tarefa
	private final DateFieldEnum periodType; // calender.field
	private final int periodFactor; // repeticao de tipoPeriodo, define o intervalo no caso de ter repeticoes
	private int numRepetitions; // numero de repeticoes a executar a tarefa
	private final Date finalize; // date de expiracao da tarefa

	private Date nextDate;

	/**
	 * Creates a new instance of DateTimer. Without steps.
	 */
	public TimeTriggerImpl() {
		this(null, null, 0, 0, null);
	}

	/**
	 * Creates a new instance of DateTimer.
	 *
	 * @param periodType   defines the type of period (ex: MINUTE, MONTH)
	 * @param periodFactor defines the factor for the period that determines the
	 *                     step
	 */
	public TimeTriggerImpl(final DateFieldEnum periodType, final int periodFactor) {
		this(null, periodType, periodFactor, -1, null);
	}

	/**
	 * Creates a new instance of DateTimer.
	 *
	 * @param periodType     defines the type of period (ex: MINUTE, MONTH)
	 * @param periodFactor   defines the factor for the period that determines the
	 *                       step
	 * @param numRepetitions number of steps to give
	 */
	public TimeTriggerImpl(final DateFieldEnum periodType, final int periodFactor, final int numRepetitions) {
		this(null, periodType, periodFactor, numRepetitions, null);
	}

	/**
	 * Creates a new instance of DateTimer.
	 *
	 * @param periodType   defines the type of period (ex: MINUTE, MONTH)
	 * @param periodFactor defines the factor for the period that determines the
	 *                     step
	 * @param finalize     Date that determines the end step
	 */
	public TimeTriggerImpl(final DateFieldEnum periodType, final int periodFactor, final Date finalize) {
		this(null, periodType, periodFactor, -1, finalize);
	}

	/**
	 * Creates a new instance of DateTimer.
	 *
	 * @param initiate       Date that determines the start step
	 * @param periodType     defines the type of period (ex: MINUTE, MONTH)
	 * @param periodFactor   defines the factor for the period that determines the
	 *                       step
	 * @param numRepetitions number of steps to give
	 * @param finalize       Date that determines the end step
	 */
	public TimeTriggerImpl(final Date initiate, final DateFieldEnum periodType, final int periodFactor,
			final int numRepetitions, final Date finalize) {
		this.initiate = initiate;
		this.periodType = periodType;
		this.periodFactor = periodFactor;
		this.numRepetitions = numRepetitions;
		this.finalize = finalize;
		this.nextDate = initiate;
	}

	private boolean hasStepper() {
		return (this.periodType != null && this.periodFactor > 0 && this.numRepetitions != 0
				&& (this.finalize == null || DateUtil.isFuture(this.finalize)));
	}

	@Override
	public Date nextTrigger() {
		if (this.initiate == null && this.nextDate == null && this.periodType == DateFieldEnum.MILLISECOND
				&& this.periodFactor == 1 && this.numRepetitions == 1 && this.finalize == null) {
			this.nextDate = DateUtil.now();
			this.numRepetitions = 0;
		} else if (this.nextDate == null || !DateUtil.isFuture(this.nextDate)) {
			if (hasStepper()) {
				Date init = this.initiate;
				if (init == null) {
					init = this.nextDate;
				}
				if (init == null) {
					init = DateUtil.now();
					DateUtil.zeroTill(init, this.periodType);
				}
				final Date now = DateUtil.now();
				while (now.compareTo(init) >= 0 && this.numRepetitions != 0
						&& (this.finalize == null || this.finalize.compareTo(init) >= 0)) {
					DateUtil.add(init, this.periodType, this.periodFactor);
					if (this.numRepetitions > 0) {
						this.numRepetitions--;
					}
				}
				if (now.compareTo(init) < 0) {
					this.nextDate = init;
				} else {
					this.nextDate = null;
					this.numRepetitions = 0;
				}
			} else if (this.nextDate != null) {
				this.nextDate = null;
				this.numRepetitions = 0;
			}
		}
		if (this.nextDate == null) {
			return null;
		} else {
			return new Date(this.nextDate.getTime());
		}
	}

	@Override
	public String toString() {
		return (new StringBuilder()).append(super.toString()).append("; initiate: ").append(this.initiate)
				.append("; nextDate: ").append(this.nextDate).append("; periodType: ").append(this.periodType)
				.append("; periodFactor: ").append(this.periodFactor).append("; numRepetitions: ")
				.append(this.numRepetitions).append("; finalize: ").append(this.finalize).toString();
	}

	/**
	 * Call this method to get the DateTimer instance.<br/>
	 * The new timer will have only one step and now.
	 *
	 * @return DateTimer instance
	 */
	public static TimeTriggerImpl getSingleImmediateTrigger() {
		return new TimeTriggerImpl(DateFieldEnum.MILLISECOND, 1, 1);
	}

}

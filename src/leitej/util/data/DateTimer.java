/*******************************************************************************
 * Copyright (C) 2011 Julio Leite
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
public final class DateTimer implements DateTimerItf {

	private static final long serialVersionUID = -5278534120277185092L;

	private Date initiate = null; // data de inicio da tarefa
	private DateFieldEnum periodType = null; // calender.field
	private int periodFactor = 0; // repeticao de tipoPeriodo, define o intervalo no caso de ter repeticoes
	private int numRepetitions = -1; // numero de repeticoes a executar a tarefa
	private Date finalize = null; // date de expiracao da tarefa

	private Date nextDate = null;

	/**
	 * Creates a new instance of DateTimer. Without steps.
	 */
	public DateTimer() {
	}

	/**
	 * Creates a new instance of DateTimer.
	 *
	 * @param periodType   defines the type of period (ex: MINUTE, MONTH)
	 * @param periodFactor defines the factor for the period that determines the
	 *                     step
	 */
	public DateTimer(final DateFieldEnum periodType, final int periodFactor) {
		this.periodType = periodType;
		this.periodFactor = periodFactor;
	}

	/**
	 * Creates a new instance of DateTimer.
	 *
	 * @param periodType     defines the type of period (ex: MINUTE, MONTH)
	 * @param periodFactor   defines the factor for the period that determines the
	 *                       step
	 * @param numRepetitions number of steps to give
	 */
	public DateTimer(final DateFieldEnum periodType, final int periodFactor, final int numRepetitions) {
		this.periodType = periodType;
		this.periodFactor = periodFactor;
		this.numRepetitions = numRepetitions;
	}

	/**
	 * Creates a new instance of DateTimer.
	 *
	 * @param periodType   defines the type of period (ex: MINUTE, MONTH)
	 * @param periodFactor defines the factor for the period that determines the
	 *                     step
	 * @param finalize     Date that determines the end step
	 */
	public DateTimer(final DateFieldEnum periodType, final int periodFactor, final Date finalize) {
		this.periodType = periodType;
		this.periodFactor = periodFactor;
		this.finalize = finalize;
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
	public DateTimer(final Date initiate, final DateFieldEnum periodType, final int periodFactor,
			final int numRepetitions, final Date finalize) {
		this.initiate = initiate;
		this.nextDate = initiate;
		this.periodType = periodType;
		this.periodFactor = periodFactor;
		this.numRepetitions = numRepetitions;
		this.finalize = finalize;
	}

	@Override
	public boolean hasStarted() {
		return (this.initiate != null && !DateUtil.isFuture(this.initiate));
	}

	@Override
	public boolean hasNextStep() {
		return DateUtil.isFuture(this.nextDate) || hasStepper();
	}

	private boolean hasStepper() {
		return (this.periodType != null && this.periodFactor > 0 && this.numRepetitions != 0
				&& (this.finalize == null || DateUtil.isFuture(this.finalize)));
	}

	@Override
	public Date nextStepTimer() {
		if (this.initiate == null && this.nextDate == null && this.periodType == DateFieldEnum.MILLISECOND
				&& this.periodFactor == 1 && this.numRepetitions == 1 && this.finalize == null) {
			this.nextDate = DateUtil.now();
			this.numRepetitions--;
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
				while (!DateUtil.isFuture(init) && this.numRepetitions != 0
						&& (this.finalize == null || this.finalize.compareTo(init) >= 0)) {
					DateUtil.add(init, this.periodType, this.periodFactor);
					if (this.numRepetitions > 0) {
						this.numRepetitions--;
					}
				}
				if (DateUtil.isFuture(init)) {
					this.nextDate = init;
				} else {
					this.nextDate = null;
					this.numRepetitions = 0;
				}
			} else if (this.nextDate != null) {
				this.nextDate = null;
			}
		}
		if (this.nextDate == null) {
			return null;
		} else {
			return new Date(this.nextDate.getTime());
		}
	}

	@Override
	public Long milliSecondsRemainingToStep() {
		final Date nextDate = nextStepTimer();
		if (nextDate == null) {
			return null;
		}
		return nextDate.getTime() - DateUtil.nowTime();
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
	public static DateTimer getUniqueImmediateTimer() {
		return new DateTimer(DateFieldEnum.MILLISECOND, 1, 1);
	}

}

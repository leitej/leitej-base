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

import java.io.Serializable;
import java.util.Date;

/**
 * Objects implementing this interface gives responses like a timer.
 *
 * @author Julio Leite
 */
public interface DateTimerItf extends Serializable {
	/**
	 * Verifies if the timer has already started.
	 *
	 * @return boolean
	 */
	public boolean hasStarted();

	/**
	 * Verifies if the timer has an other step in the future.
	 *
	 * @return boolean
	 */
	public boolean hasNextStep();

	/**
	 * Gives the next step of the timer.
	 *
	 * @return Date of the step or null if don't have it
	 */
	public Date nextStepTimer();

	/**
	 * Gives the remaining time to the next step.
	 *
	 * @return Long milliseconds
	 */
	public Long milliSecondsRemainingToStep();
}

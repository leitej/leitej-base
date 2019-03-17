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

import java.util.Calendar;

/**
 * Enumerates all the possible values for DateFieldEnum field.
 *
 * @author Julio Leite
 */
public enum DateFieldEnum {
	MILLISECOND, SECOND, MINUTE, WEEK_OF_YEAR, HOUR_OF_DAY, DAY_OF_MONTH, MONTH, YEAR;

	/**
	 * Gives the corresponding constant of DateFieldEnum used by
	 * {@link java.util.Calendar Calendar}.
	 *
	 * @return int
	 */
	public int getCalendarField() {
		switch (this) {
		case MILLISECOND:
			return Calendar.MILLISECOND;
		case SECOND:
			return Calendar.SECOND;
		case MINUTE:
			return Calendar.MINUTE;
		case HOUR_OF_DAY:
			return Calendar.HOUR_OF_DAY;
		case WEEK_OF_YEAR:
			return Calendar.WEEK_OF_YEAR;
		case DAY_OF_MONTH:
			return Calendar.DAY_OF_MONTH;
		case MONTH:
			return Calendar.MONTH;
		case YEAR:
			return Calendar.YEAR;
		default:
			return -1;
		}
	}

}

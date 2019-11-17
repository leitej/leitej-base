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

package leitej.log;

import leitej.exception.ImplementationLtRtException;
import leitej.util.DateUtil;
import leitej.util.data.DateFieldEnum;

/**
 * Object used exclusively to get and set configuration of
 * {@link leitej.log.Logger Logger}, owing to XMLOM concept.
 *
 * @author Julio Leite
 */
public enum ConfigDynFileName {
	NONE, HOURLY, WEEKLY, DAILY, MONTHLY, YEARLY;

	DateFieldEnum getDatePeriodType() {
		switch (this) {
		case NONE:
			return null;
		case HOURLY:
			return DateFieldEnum.HOUR_OF_DAY;
		case WEEKLY:
			return DateFieldEnum.WEEK_OF_YEAR;
		case DAILY:
			return DateFieldEnum.DAY_OF_MONTH;
		case MONTHLY:
			return DateFieldEnum.MONTH;
		case YEARLY:
			return DateFieldEnum.YEAR;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
	}

	String getDateFormat() {
		switch (this) {
		case NONE:
			return null;
		case HOURLY:
			return DateUtil.KEY_FORMAT_YEAR_COMPACT + DateUtil.KEY_FORMAT_MONTH + DateUtil.KEY_FORMAT_DAY_IN_MONTH
					+ DateUtil.KEY_FORMAT_HOUR;
		case WEEKLY:
			return DateUtil.KEY_FORMAT_YEAR_COMPACT + DateUtil.KEY_FORMAT_WEEK_IN_YEAR;
		case DAILY:
			return DateUtil.KEY_FORMAT_YEAR_COMPACT + DateUtil.KEY_FORMAT_MONTH + DateUtil.KEY_FORMAT_DAY_IN_MONTH;
		case MONTHLY:
			return DateUtil.KEY_FORMAT_YEAR_COMPACT + DateUtil.KEY_FORMAT_MONTH;
		case YEARLY:
			return DateUtil.KEY_FORMAT_YEAR_COMPACT;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
	}
}

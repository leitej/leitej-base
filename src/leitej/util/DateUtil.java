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

package leitej.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import leitej.util.data.DateFieldEnum;

/**
 * An useful class to help in date manipulation.
 *
 * @author Julio Leite
 */
public final class DateUtil {

	public static final long ONE_SECOND_IN_MS = 1000;
	public static final long ONE_MINUTE_IN_MS = 60 * ONE_SECOND_IN_MS;
	public static final long ONE_HOUR_IN_MS = 60 * ONE_MINUTE_IN_MS;
	public static final long ONE_DAY_IN_MS = 24 * ONE_HOUR_IN_MS;
	public static final long ONE_WEEK_IN_MS = 7 * ONE_DAY_IN_MS;
	public static final long ONE_MONTH_AROUND_IN_MS = Math.round(4.3 * ONE_WEEK_IN_MS);
	public static final long ONE_YEAR_AROUND_IN_MS = 365 * ONE_DAY_IN_MS;
	public static final long FOUR_YEARS_IN_MS = 4 * ONE_YEAR_AROUND_IN_MS + 1;

	private static final int FIRST_MILLISECOND = 0;
	private static final int FIRST_SECOND = 0;
	private static final int FIRST_MINUTE = 0;
	private static final int FIRST_HOUR = 0;
	private static final int FIRST_DAY = 1;
//	private static final int FIRST_WEEK = 1;
	private static final int FIRST_MONTH = 1;
//	private static final int FIRST_YEAR = 1970;

	public static final String KEY_FORMAT_YEAR_COMPACT = "yy";
	public static final String KEY_FORMAT_YEAR = "yyyy";
	public static final String KEY_FORMAT_MONTH = "MM";
	public static final String KEY_FORMAT_WEEK_IN_YEAR = "ww";
	public static final String KEY_FORMAT_DAY_IN_MONTH = "dd";
	public static final String KEY_FORMAT_DAY_IN_YEAR = "DDD";
	public static final String KEY_FORMAT_HOUR = "HH";
	public static final String KEY_FORMAT_MINUTE = "mm";
	public static final String KEY_FORMAT_SECOND = "ss";
	public static final String KEY_FORMAT_MILLI_SECOND = "SSS";
	public static final String KEY_FORMAT_TIME_ZONE = "Z";

	private static final Map<Long, Calendar> cm = Collections.synchronizedMap(new HashMap<Long, Calendar>());
	private static final Map<Long, SimpleDateFormat> sm = Collections
			.synchronizedMap(new HashMap<Long, SimpleDateFormat>());

	private static Long LOCK_UNIQUE_NUMBER = System.currentTimeMillis();

	/**
	 * Creates a new instance of DateUtil.
	 */
	private DateUtil() {
	}

	/**
	 * Returns the current system time in milliseconds. Note that while the unit of
	 * time of the return value is a millisecond, the granularity of the value
	 * depends on the underlying operating system and may be larger. For example,
	 * many operating systems measure time in units of tens of milliseconds.
	 *
	 * @return the difference, measured in milliseconds, between the current time
	 *         and midnight, January 1, 1970 UTC
	 */
	public static long nowTime() {
		return System.currentTimeMillis();
	}

	/**
	 * Allocates a Date object and initialises it so that it represents the time at
	 * which it was allocated, measured to the nearest millisecond.
	 *
	 * @return Date
	 */
	public static Date now() {
		return new Date();
	}

	/**
	 * Allocates a Date object and initialises it so that it represents the time at
	 * argument.
	 *
	 * @param year  (1970..)
	 * @param month (1..12)
	 * @param day   (1..31)
	 * @return Date
	 */
	public static Date newDate(final int year, final int month, final int day) {
		final Calendar c = getCalendar();
		c.clear();
		c.set(year, month - 1, day);
		return new Date(c.getTimeInMillis());
	}

	/**
	 * Allocates a Date object and initialises it so that it represents the time at
	 * argument.
	 *
	 * @param year
	 * @param month
	 * @param day
	 * @param hourOfDay
	 * @param minute
	 * @param second
	 * @return Date
	 */
	public static Date newDate(final int year, final int month, final int day, final int hourOfDay, final int minute,
			final int second) {
		final Calendar c = getCalendar();
		c.clear();
		c.set(year, month - 1, day, hourOfDay, minute, second);
		return new Date(c.getTimeInMillis());
	}

	/**
	 * Formats a Date into a date/time string.
	 *
	 * @param date
	 * @param pattern
	 * @return formated date
	 * @throws NullPointerException     If the given pattern is null
	 * @throws IllegalArgumentException If the given pattern is invalid
	 * @see java.text.SimpleDateFormat
	 */
	public static String format(final Date date, final String pattern)
			throws NullPointerException, IllegalArgumentException {
		final SimpleDateFormat sdf = getSimpleDateFormat();
		sdf.applyPattern(pattern);
		return sdf.format(date);
	}

	/**
	 * Verifies if a date represents a time in the future.
	 *
	 * @param date to compare
	 * @return boolean
	 */
	public static boolean isFuture(final Date date) {
		if (date == null) {
			return false;
		}
		return (date.compareTo(now()) > 0);
	}

	/**
	 * Verifies if a time is in the future.
	 *
	 * @param time to compare
	 * @return boolean
	 */
	public static boolean isFuture(final long time) {
		return (time > nowTime());
	}

	/**
	 * Returns the value of the given date field.
	 *
	 * @param date  to get the value
	 * @param field the given date field
	 * @return the value for the given calendar field
	 */
	public static int get(final Date date, final DateFieldEnum field) {
		final Calendar c = getCalendar();
		c.setTime(date);
		int normalize = 0;
		if (field.equals(DateFieldEnum.MONTH)) {
			normalize = 1;
		}
		if (field.equals(DateFieldEnum.WEEK_OF_YEAR) && c.get(Calendar.MONTH) == Calendar.DECEMBER
				&& c.get(Calendar.WEEK_OF_YEAR) == 1) {
			do {
				c.add(Calendar.DAY_OF_MONTH, -1);
			} while (c.get(Calendar.WEEK_OF_YEAR) == 1);
			normalize = 1;
		}
		return c.get(field.getCalendarField()) + normalize;
	}

	/**
	 * Adds or subtracts the specified amount of time to the given calendar field,
	 * based on the calendar's rules.
	 *
	 * @param date   to change
	 * @param field  the date field
	 * @param amount the amount of date or time to be added to the field
	 * @return date changed by specifications
	 */
	public static Date add(final Date date, final DateFieldEnum field, final int amount) {
		final Calendar c = getCalendar();
		c.setTime(date);
		c.add(field.getCalendarField(), amount);
		date.setTime(c.getTimeInMillis());
		return date;
	}

	/**
	 * Sets the given date field to the given value.
	 *
	 * @param date  to change
	 * @param field the given date field
	 * @param value to be set for the given date field
	 * @return date changed by specifications
	 */
	public static Date set(final Date date, final DateFieldEnum field, final int value) {
		final Calendar c = getCalendar();
		c.setTime(date);
		int normalize = 0;
		if (field.equals(DateFieldEnum.MONTH)) {
			normalize = 1;
		}
		c.set(field.getCalendarField(), value - normalize);
		date.setTime(c.getTimeInMillis());
		return date;
	}

	/**
	 * Clear fields till a given date field.
	 *
	 * @param date  to change
	 * @param field the given date field
	 * @return date changed by specifications
	 */
	public static Date zeroTill(final Date date, final DateFieldEnum field) {
		if (field.ordinal() > DateFieldEnum.MILLISECOND.ordinal()) {
			set(date, DateFieldEnum.MILLISECOND, FIRST_MILLISECOND);
		}
		if (field.ordinal() > DateFieldEnum.SECOND.ordinal()) {
			set(date, DateFieldEnum.SECOND, FIRST_SECOND);
		}
		if (field.ordinal() > DateFieldEnum.MINUTE.ordinal()) {
			set(date, DateFieldEnum.MINUTE, FIRST_MINUTE);
		}
		if (field.ordinal() > DateFieldEnum.HOUR_OF_DAY.ordinal()) {
			set(date, DateFieldEnum.HOUR_OF_DAY, FIRST_HOUR);
		}
		if (field.ordinal() > DateFieldEnum.DAY_OF_MONTH.ordinal()) {
			set(date, DateFieldEnum.DAY_OF_MONTH, FIRST_DAY);
		}
		if (field.ordinal() > DateFieldEnum.MONTH.ordinal()) {
			set(date, DateFieldEnum.MONTH, FIRST_MONTH);
		}
		return date;
	}

	private static Calendar getCalendar() {
		final Long threadId = Thread.currentThread().getId();
		Calendar result = cm.get(threadId);
		if (result == null) {
			result = Calendar.getInstance();
			cm.put(threadId, result);
		}
		return result;
	}

	private static SimpleDateFormat getSimpleDateFormat() {
		final Long threadId = Thread.currentThread().getId();
		SimpleDateFormat result = sm.get(threadId);
		if (result == null) {
			result = new SimpleDateFormat();
			sm.put(threadId, result);
		}
		return result;
	}

	/**
	 * Generates a unique number for witch call, for the running JVM.
	 *
	 * @return unique number
	 */
	public static long generateUniqueNumberPerJVM() {
		long result;
		synchronized (LOCK_UNIQUE_NUMBER) {
			result = LOCK_UNIQUE_NUMBER;
			LOCK_UNIQUE_NUMBER++;
			while (LOCK_UNIQUE_NUMBER > System.currentTimeMillis()) {
				try {
					Thread.sleep(15);
				} catch (final InterruptedException e) {
					/* ignored */}
			}
		}
		return result;
	}

}

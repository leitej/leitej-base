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

import java.lang.reflect.Array;
import java.util.Date;
import java.util.Map;

import leitej.Constant;
import leitej.locale.message.Messages;
import leitej.util.DateUtil;
import leitej.util.StringUtil;

/**
 * Appender
 *
 * @author Julio Leite
 */
abstract class AbstractAppender {

	private static final Messages MESSAGES = Messages.getInstance();

	private static String[] getHierarchicalSignLog(final String logSignClean) {
		String[] result = null;
		if (!StringUtil.isNullOrEmpty(logSignClean)) {
			final String[] pack = logSignClean.split("\\.");
			result = (String[]) Array.newInstance(String.class, pack.length);
			if (pack.length > 0) {
				final StringBuilder sb = new StringBuilder(pack[0]);
				result[pack.length - 1] = pack[0];
				for (int i = 1; i < pack.length; i++) {
					result[pack.length - i - 1] = sb.append(".").append(pack[i]).toString();
				}
			}
		}
		return result;
	}

	private final String sdFormat;
	private final LevelEnum defaultLevel;
	private final Map<String, LevelEnum> packageLogLevel;

	AbstractAppender(final Config lp) {
		this.sdFormat = (lp.getDateFormat() != null) ? lp.getDateFormat() : Constant.DEFAULT_SIMPLE_DATE_FORMAT;
		this.defaultLevel = (lp.getLogLevel() != null) ? lp.getLogLevel() : Constant.DEFAULT_LOG_LEVEL;
		this.packageLogLevel = (lp.getPackageLogLevel() != null) ? lp.getPackageLogLevel() : null;
	}

	final LevelEnum getMaxLogLevel(final String signClass) {
		final LevelEnum result;
		if (this.packageLogLevel != null) {
			LevelEnum level = this.packageLogLevel.get(signClass);
			if (level == null) {
				final String[] signs = getHierarchicalSignLog(signClass);
				int i = 0;
				i++;
				for (; i < signs.length && level == null; i++) {
					level = this.packageLogLevel.get(signs[i]);
				}
				i -= 2;
				if (level == null) {
					level = this.defaultLevel;
				}
				for (; i > 0; i--) {
					this.packageLogLevel.put(signs[i], level);
				}
				this.packageLogLevel.put(signClass, level);
			}
			result = level;
		} else {
			result = this.defaultLevel;
		}
		return result;
	}

	final void print(final LevelEnum level, final String threadName, final String signLog, final Date date,
			final String msg, final Object... args) {
		outPrint(true, DateUtil.format(date, this.sdFormat));
		printLog(level, threadName, signLog, msg, args);
	}

	private void printLog(final LevelEnum level, final String threadName, final String signLog, final String plainLog,
			final Object... args) {
		outPrint(false, " ");
		outPrint(false, level.toString());
		outPrint(false, " [");
		outPrint(false, threadName);
		outPrint(false, "] ");
		outPrint(false, signLog);
		outPrint(false, " - ");
		outPrint(false, plainLog);
		outPrint(false, Constant.DEFAULT_LINE_SEPARATOR);
		if ((level.ordinal() < LevelEnum.WARN.ordinal() || level.ordinal() > LevelEnum.DEBUG.ordinal()) && args != null
				&& args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (Exception.class.isInstance(args[i])) {
					printOutputException(((Exception) args[i]));
				}
			}
		}
	}

	private void printOutputException(final Exception e) {
		StackTraceElement[] elements = e.getStackTrace();
		printOutputStackTrace(elements);
		Throwable throwable = e.getCause();
		while (throwable != null) {
			outPrint(false, MESSAGES.get("Caused by: #0", throwable));
			outPrint(false, Constant.DEFAULT_LINE_SEPARATOR);
			elements = throwable.getStackTrace();
			printOutputStackTrace(elements);
			throwable = throwable.getCause();
		}
	}

	private void printOutputStackTrace(final StackTraceElement[] elements) {
		for (int j = 0; j < elements.length; j++) {
			outPrint(false, "\t");
			outPrint(false, MESSAGES.get("at"));
			outPrint(false, " ");
			outPrint(false, elements[j].toString());
			outPrint(false, Constant.DEFAULT_LINE_SEPARATOR);
		}
	}

	@Override
	public final void finalize() throws Throwable {
		close();
		super.finalize();
	}

	abstract void outPrint(final boolean newRecord, final String txt);

	abstract void close();

}

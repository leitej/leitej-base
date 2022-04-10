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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Julio Leite
 *
 */
final class JavaLogging extends Handler {

	private static final int OFF = Level.OFF.intValue();
	private static final int WARNING = Level.WARNING.intValue();
	private static final int INFO = Level.INFO.intValue();
	private static final int FINE = Level.FINE.intValue();
	private static final int ALL = Level.ALL.intValue();

	static void grab(final Logger log) {
		final java.util.logging.Logger lGlobal = java.util.logging.Logger.getGlobal();
		final Handler[] handlers = lGlobal.getHandlers();
		for (int i = 0; i < handlers.length; i++) {
			lGlobal.removeHandler(handlers[i]);
		}
		lGlobal.addHandler(new JavaLogging(log));
	}

	private final Logger log;

	JavaLogging(final Logger log) {
		this.log = log;
	}

	@Override
	public void publish(final LogRecord record) {
		final int level = record.getLevel().intValue();
		if (OFF < level && level <= WARNING) {
			this.log.warn("#0 - #1", record.getLoggerName(), record.getMessage());
		} else if (WARNING < level && level <= INFO) {
			this.log.info("#0 - #1", record.getLoggerName(), record.getMessage());
		} else if (INFO < level && level <= FINE) {
			this.log.debug("#0 - #1", record.getLoggerName(), record.getMessage());
		} else if (FINE < level && level <= ALL) {
			this.log.trace("#0 - #1", record.getLoggerName(), record.getMessage());
		}
	}

	@Override
	public void flush() {
		// empty
	}

	@Override
	public void close() throws SecurityException {
		// empty
	}

}

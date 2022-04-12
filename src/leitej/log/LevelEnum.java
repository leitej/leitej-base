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

import java.util.logging.Level;

import leitej.exception.ImplementationLtRtException;

/**
 * Level enumerator to be used by {@link leitej.log.Logger Logger}.
 *
 * @author Julio Leite
 * @see leitej.log.Logger
 */
public enum LevelEnum {
	NONE, ERROR, WARN, INFO, DEBUG, TRACE, ALL;

	static final LevelEnum fromJavaLoggingLevel(final int level) {
		if (Level.ALL.intValue() == level) {
			return ALL;
		} else if (Level.ALL.intValue() > level && level < Level.FINEST.intValue()) {
			return TRACE;
		} else if (Level.FINEST.intValue() >= level && level < Level.FINE.intValue()) {
			return TRACE;
		} else if (Level.FINE.intValue() >= level && level < Level.INFO.intValue()) {
			return DEBUG;
		} else if (Level.INFO.intValue() >= level && level < Level.WARNING.intValue()) {
			return INFO;
		} else if (Level.WARNING.intValue() >= level && level < Level.SEVERE.intValue()) {
			return WARN;
		} else if (Level.SEVERE.intValue() >= level && level < Level.OFF.intValue()) {
			return ERROR;
		} else {
			return NONE;
		}
	}

	final String getJavaLoggingLevel() {
		switch (this) {
		case NONE:
			return "OFF";
		case ERROR:
			return "SEVERE";
		case WARN:
			return "WARNING";
		case INFO:
			return "CONFIG";
		case DEBUG:
			return "FINER";
		case TRACE:
			return "FINEST";
		case ALL:
			return "ALL";
		default:
			throw new ImplementationLtRtException("miss: #0", this);
		}
	}
}

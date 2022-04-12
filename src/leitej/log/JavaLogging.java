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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import leitej.Constant;
import leitej.util.stream.ChunkInputStream;

/**
 * @author Julio Leite
 *
 */
public final class JavaLogging extends Handler {

	private static Logger LOG;

	static void grab(final Logger log, final Config config) {
		LOG = log;
		final LogManager lm = LogManager.getLogManager();
		log.trace("previous default handler: #0", lm.getProperty("handlers"));
		LevelEnum logLevel;
		Map<String, LevelEnum> packageLogLevel;
		if (config == null) {
			logLevel = Constant.DEFAULT_LOG_LEVEL;
			packageLogLevel = null;
		} else {
			logLevel = config.getLogLevel();
			packageLogLevel = config.getPackageLogLevel();
		}
		final InputStream is = new JavaLoggingConfigInputStream(logLevel, packageLogLevel);
		lm.reset();
		try {
			lm.readConfiguration(is);
			is.close();
			LOG.debug("Java Logging Set");
		} catch (final IOException e) {
			LOG.error("#0", e);
			LOG.warn("Java Logging write SUPPRESS!");
		}
	}

	@Override
	public void publish(final LogRecord record) {
		LOG.appendJavaLogging(record);
	}

	@Override
	public void flush() {
		// no-op
	}

	@Override
	public void close() throws SecurityException {
		// no-op
	}

	private static class JavaLoggingConfigInputStream extends ChunkInputStream {

		private final Iterator<Entry<String, LevelEnum>> packageLogLevel;

		public JavaLoggingConfigInputStream(final LevelEnum logLevel, final Map<String, LevelEnum> packageLogLevel) {
			if (packageLogLevel == null) {
				this.packageLogLevel = null;
			} else {
				this.packageLogLevel = packageLogLevel.entrySet().iterator();
			}
			feed("handlers= leitej.log.JavaLogging\n".getBytes());
			feed((".level= " + logLevel.getJavaLoggingLevel() + "\n").getBytes());
		}

		@Override
		protected void waitFeed() {
			if (this.packageLogLevel == null) {
				close();
			} else {
				if (this.packageLogLevel.hasNext()) {
					final Entry<String, LevelEnum> entry = this.packageLogLevel.next();
					feed((entry.getKey().trim() + ".level = " + entry.getValue().getJavaLoggingLevel() + "\n")
							.getBytes());
				} else {
					close();
				}
			}
		}

	}

}

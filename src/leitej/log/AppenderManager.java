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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leitej.exception.LtException;
import leitej.locale.message.Messages;
import leitej.util.DateUtil;
import leitej.util.data.XmlomUtil;
import leitej.util.stream.FileUtil;

/**
 * AppenderManager
 *
 * @author Julio Leite
 */
final class AppenderManager {

	private static final Messages MESSAGES = Messages.getInstance();

	private static final AbstractAppender[] APPENDERS = loadAppenders();
	private static volatile boolean CLOSED = false;

	private static AbstractAppender[] loadAppenders() {
		final Config[] props = readPropertiesFile();
		final List<AbstractAppender> list = new ArrayList<>();
		if (props != null) {
			for (int i = 0; i < props.length; i++) {
				try {
					list.add(newLogAppender(props[i]));
				} catch (final UnsupportedEncodingException | FileNotFoundException e) {
					(new LtException(e, "lt.LogAppendErrorOpen")).printStackTrace();
				}
			}
		}
		return list.toArray(new AbstractAppender[list.size()]);
	}

	private static AbstractAppender newLogAppender(final Config propLog)
			throws UnsupportedEncodingException, FileNotFoundException {
		AbstractAppender logAppender = null;
		if (propLog.isConsole()) {
			logAppender = new AppenderConsole(propLog);
		} else if (propLog.getFile() != null && propLog.getFile().getDynName() != null
				&& !propLog.getFile().getDynName().equals(ConfigDynFileName.NONE)) {
			logAppender = new AppenderDynFileName(propLog);
		} else {
			logAppender = new AppenderFile(propLog);
		}
		return logAppender;
	}

	private static Config[] readPropertiesFile() {
		Config[] result = null;
		try {
			result = XmlomUtil.getObjectsFromFileUTF8(Config.class, Logger.DEFAULT_LOG_PROPERTIES_FILE_NAME)
					.toArray((Config[]) Array.newInstance(Config.class, 1));
		} catch (final FileNotFoundException e) {
			result = defaultProperties();
		} catch (final NullPointerException | IllegalArgumentException | SecurityException | IOException
				| LtException e) {
			System.err.println(e.getMessage());
		}
		return result;
	}

	private static Config[] defaultProperties() {
		final Config[] result = (Config[]) Array.newInstance(Config.class, 1);
		final Config pl = XmlomUtil.newXmlObjectModelling(Config.class);
		final ConfigFile plf = XmlomUtil.newXmlObjectModelling(ConfigFile.class);
		plf.setFileName("app.log");
		plf.setAppendFile(Boolean.FALSE);
		pl.setFile(plf);
		pl.setConsole(false);
		pl.setLogLevel(Logger.DEFAULT_LOG_LEVEL);
		pl.setDateFormat(Logger.DEFAULT_LOG_SIMPLE_DATE_FORMAT);
		final Map<String, LevelEnum> pll = new HashMap<>();
		pll.put("leitej", LevelEnum.INFO);
		pl.setPackageLogLevel(pll);
		result[0] = pl;
		if (!FileUtil.exists(Logger.DEFAULT_LOG_PROPERTIES_FILE_NAME)) {
			// Create the config file with default properties created above
			try {
				XmlomUtil.sendToFileUTF8(Logger.DEFAULT_LOG_PROPERTIES_FILE_NAME, result);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	static void close() {
		if (!CLOSED) {
			CLOSED = true;
			for (int i = 0; i < APPENDERS.length; i++) {
				APPENDERS[i].close();
			}
		}
	}

	private final String signLog;
	private final LevelEnum signLogLevelGlobal;
	private final LevelEnum[] signLogLevelPerAppender;

	AppenderManager(final String signClass) {
		this.signLog = signClass;
		this.signLogLevelPerAppender = new LevelEnum[APPENDERS.length];
		LevelEnum signLogLevel = LevelEnum.NONE;
		LevelEnum appendLevel;
		for (int i = 0; i < APPENDERS.length; i++) {
			appendLevel = APPENDERS[i].getMaxLogLevel(this.signLog);
			if (appendLevel.ordinal() > signLogLevel.ordinal()) {
				signLogLevel = appendLevel;
			}
			this.signLogLevelPerAppender[i] = appendLevel;
		}
		this.signLogLevelGlobal = signLogLevel;
	}

	void print(final LevelEnum level, final String threadName, final String msg, final Object... args) {
		if (level.ordinal() <= this.signLogLevelGlobal.ordinal()) {
			final String signLog;
			if (LevelEnum.INFO.compareTo(level) < 0) {
				// sign with more information as a new throwable can give (like method invoked)
				signLog = (new Throwable()).getStackTrace()[3].toString();
			} else {
				// simple sign
				signLog = this.signLog;
			}
			synchronized (APPENDERS) {
				final Date date = DateUtil.now();
				final String plainLog = MESSAGES.get(msg, args);
				for (int i = 0; i < APPENDERS.length; i++) {
					if (level.ordinal() <= this.signLogLevelPerAppender[i].ordinal()) {
						APPENDERS[i].print(level, threadName, signLog, date, plainLog, args);
					}
				}
			}
		}
	}

}

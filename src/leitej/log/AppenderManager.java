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
import java.util.logging.LogRecord;

import leitej.Constant;
import leitej.exception.LtException;
import leitej.exception.XmlInvalidLtException;
import leitej.util.DateUtil;
import leitej.util.StringUtil;
import leitej.xml.om.Xmlom;

/**
 * AppenderManager
 *
 * @author Julio Leite
 */
final class AppenderManager {

	private static final AbstractAppender[] APPENDERS = loadAppenders();
	static Config JAVA_LOGGING_CONFIG;

	private static Config[] defaultConfig() {
		final Config[] result = (Config[]) Array.newInstance(Config.class, 2);
		final Config pl = Xmlom.newInstance(Config.class);
		final ConfigFile plf = Xmlom.newInstance(ConfigFile.class);
		plf.setFileName("app.log");
		plf.setAppendFile(Boolean.TRUE);
		plf.setDynName(ConfigDynFileName.DAILY);
		plf.setPath(".");
		plf.setCharsetName(Constant.UTF8_CHARSET_NAME);
		pl.setFile(plf);
		pl.setConsole(true);
		pl.setLogLevel(Constant.DEFAULT_LOG_LEVEL);
		pl.setDateFormat(Constant.DEFAULT_SIMPLE_DATE_FORMAT);
		final Map<String, LevelEnum> pll = new HashMap<>();
		pll.put("leitej", LevelEnum.INFO);
		pl.setPackageLogLevel(pll);
		result[0] = pl;
		final Config javaLog = Xmlom.newInstance(Config.class);
		javaLog.setJavaLoggingLevelDefine(true);
		javaLog.setLogLevel(Constant.DEFAULT_LOG_LEVEL);
		final Map<String, LevelEnum> pllJLog = new HashMap<>();
		pllJLog.put("com.xyz.foo.level", LevelEnum.ERROR);
		javaLog.setPackageLogLevel(pllJLog);
		result[1] = javaLog;
		return result;
	}

	private static AbstractAppender newLogAppender(final Config config)
			throws UnsupportedEncodingException, FileNotFoundException {
		AbstractAppender logAppender = null;
		if (config.isConsole()) {
			logAppender = new AppenderConsole(config);
		} else if (config.getFile() != null && config.getFile().getDynName() != null
				&& !config.getFile().getDynName().equals(ConfigDynFileName.NONE)) {
			logAppender = new AppenderDynFileName(config);
		} else {
			logAppender = new AppenderFile(config);
		}
		return logAppender;
	}

	private static AbstractAppender[] loadAppenders() {
		final List<AbstractAppender> list = new ArrayList<>();
		try {
			final List<Config> props = Xmlom.getConfig(Config.class, defaultConfig());
			if (props != null) {
				for (final Config config : props) {
					try {
						if (config.getJavaLoggingLevelDefine() == null || !config.getJavaLoggingLevelDefine()) {
							list.add(newLogAppender(config));
						} else {
							JAVA_LOGGING_CONFIG = config;
						}
					} catch (final UnsupportedEncodingException | FileNotFoundException e) {
						(new LtException(e, "Can't open an appender log")).printStackTrace();
					}
				}
			}
		} catch (NullPointerException | SecurityException | XmlInvalidLtException | IOException e) {
			e.printStackTrace();
		}
		return list.toArray(new AbstractAppender[list.size()]);
	}

	static void close() {
		for (int i = 0; i < APPENDERS.length; i++) {
			try {
				APPENDERS[i].close();
			} catch (final IOException e) {
				e.printStackTrace();
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

	void print(final LogRecord record, final LevelEnum level, final String threadName, final String msg,
			final Object... args) {
		if (level.ordinal() <= this.signLogLevelGlobal.ordinal()) {
			final String signLog;
			if (record != null) {
				if (LevelEnum.INFO.compareTo(level) < 0) {
					signLog = record.getSourceClassName() + "." + record.getSourceMethodName();
				} else {
					signLog = record.getSourceClassName();
				}
			} else if (LevelEnum.INFO.compareTo(level) < 0) {
				// sign with more information as a new throwable can give (like method invoked)
				signLog = (new Throwable()).getStackTrace()[3].toString();
			} else {
				// simple sign
				signLog = this.signLog;
			}
			synchronized (APPENDERS) {
				final Date date = DateUtil.now();
				final String plainLog = StringUtil.insertObjects(msg, args);
				for (int i = 0; i < APPENDERS.length; i++) {
					if (level.ordinal() <= this.signLogLevelPerAppender[i].ordinal()) {
						APPENDERS[i].print(level, threadName, signLog, date, plainLog, args);
					}
				}
			}
		}
	}

}

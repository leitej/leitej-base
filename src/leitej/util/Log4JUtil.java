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

package leitej.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import leitej.Constant;
import leitej.log.Logger;

/**
 * An useful class to help configure the Log4J.
 *
 * @author Julio Leite
 */
public final class Log4JUtil {

	private static final Logger LOG = Logger.getInstance();

	private static final String DEFAULT_LOG4J_PROPERTIES_FILE_NAME = "log4j.properties";
	private static final String SILENT_LOG4J_PROPERTIES = "log4j.rootLogger=OFF\n";

	/**
	 * Creates a new instance of Log4JUtil.
	 */
	private Log4JUtil() {
	}

	/**
	 * Creates a silent properties file for log4j.
	 */
	public static void createSilentPropertiesFile() {
		if (!(new File(DEFAULT_LOG4J_PROPERTIES_FILE_NAME)).exists()) {
			try {
				final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(new File(DEFAULT_LOG4J_PROPERTIES_FILE_NAME), false),
						Constant.DEFAULT_CHARSET_NAME));
				bw.write(SILENT_LOG4J_PROPERTIES);
				bw.flush();
				bw.close();
			} catch (final UnsupportedEncodingException e) {
				LOG.error("#0", e);
			} catch (final FileNotFoundException e) {
				LOG.error("#0", e);
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
			LOG.trace("lt.FileCreated", DEFAULT_LOG4J_PROPERTIES_FILE_NAME);
		}
	}
}

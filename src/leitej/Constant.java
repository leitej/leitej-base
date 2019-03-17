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

package leitej;

import java.math.BigInteger;

import leitej.log.LevelEnum;
import leitej.util.stream.FileUtil;

/**
 * Constants
 *
 * @author Julio Leite
 */
public final class Constant {

	private Constant() {
	}

	public static final String LEITEJ = "leitej";

	public static final boolean DEBUG_ACTIVE = false;

	public static final int KILO = 1024;
	public static final int MEGA = KILO * KILO;
	public static final int GIGA = MEGA * KILO;
	public static final long TERA = ((long) GIGA) * KILO;
	public static final long PETA = TERA * KILO;
	public static final long EXA = PETA * KILO;
	public static final BigInteger ZETTA = BigInteger.valueOf(EXA).multiply(BigInteger.valueOf(KILO));
	public static final BigInteger YOTTA = ZETTA.multiply(BigInteger.valueOf(KILO));

	public static final BigInteger BIGINTEGER_MAX_LONG_VALUE = BigInteger.valueOf(Long.MAX_VALUE);

	public static final String UTF8_CHARSET_NAME = "UTF-8";
	public static final String DEFAULT_CHARSET_NAME = UTF8_CHARSET_NAME;

	public static final String DEFAULT_FILE_SEPARATOR = System.getProperties().getProperty("file.separator", "/");
	public final static String DEFAULT_LINE_SEPARATOR = System.getProperties().getProperty("line.separator", "\n");
	public final static String DEFAULT_PATH_SEPARATOR = System.getProperties().getProperty("path.separator", ":");

	public static final String GET_PREFIX = "get";
	public static final String IS_PREFIX = "is";
	public static final String SET_PREFIX = "set";
	public static final String VALUEOF_METHOD_NAME = "valueOf";
	public static final String ALIAS_CLASS_NAME_METHOD_NAME = "aliasClassName";

	public static final String DEFAULT_PROPERTIES_FILE_DIR = "meta-inf" + DEFAULT_FILE_SEPARATOR;
	public static final String DEFAULT_DATA_FILE_DIR = "data" + DEFAULT_FILE_SEPARATOR;

	public static final String DEFAULT_PROPERTIES_XML_FILE_EXT = ".prop.lt.xml";
	public final static String DEFAULT_BACKUP_EXTENSION = ".bkp";

	public static final String BIG_BINARY_TEMPORARY_DIRECTORY = DEFAULT_DATA_FILE_DIR + "bigBinary"
			+ DEFAULT_FILE_SEPARATOR;
	public static final int FRACTION_INPUT_STREAM_REFRESH_WAIT_IO = 2000; // 2 seconds
	public static final int IO_BUFFER_SIZE = 2 * KILO; // 2 KB

	public static final String DEFAULT_LOG_PROPERTIES_FILE_NAME = FileUtil.propertieRelativePath4FileName("logger");
	public static final LevelEnum DEFAULT_LOG_LEVEL = LevelEnum.WARN;
	public static final String DEFAULT_LOG_SIMPLE_DATE_FORMAT = "yyMMdd.HHmm.ssSSS";

}

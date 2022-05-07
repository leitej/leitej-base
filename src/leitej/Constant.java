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

package leitej;

import java.io.File;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import leitej.log.LevelEnum;

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

	public static final String DEFAULT_SIMPLE_DATE_FORMAT = "yyMMdd.HHmm.ssSSS";
	public static LevelEnum DEFAULT_LOG_LEVEL = LevelEnum.WARN;

	public static final int KILO = 1024;
	public static final int MEGA = KILO * KILO;
	public static final int GIGA = MEGA * KILO;
	public static final long TERA = ((long) GIGA) * KILO;
	public static final long PETA = TERA * KILO;
	public static final long EXA = PETA * KILO;
	public static final BigInteger ZETTA = BigInteger.valueOf(EXA).multiply(BigInteger.valueOf(KILO));
	public static final BigInteger YOTTA = ZETTA.multiply(BigInteger.valueOf(KILO));

	public static final boolean IS_BIG_ENDIAN_NATIVE = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

	public static final BigInteger BIGINTEGER_MAX_LONG_VALUE = BigInteger.valueOf(Long.MAX_VALUE);

	public static final String UTF8_CHARSET_NAME = "UTF-8";
	public static final Charset UTF8_CHARSET = Charset.forName(UTF8_CHARSET_NAME);

	public static final String DEFAULT_FILE_SEPARATOR = System.getProperties().getProperty("file.separator", "/");
	public final static String DEFAULT_LINE_SEPARATOR = System.getProperties().getProperty("line.separator", "\n");
	public final static String DEFAULT_PATH_SEPARATOR = System.getProperties().getProperty("path.separator", ":");

	public static final String GET_PREFIX = "get";
	public static final String IS_PREFIX = "is";
	public static final String SET_PREFIX = "set";
	public static final String VALUEOF_METHOD_NAME = "valueOf";

	// TODO pass the String to File to all representatives directories
	public static final String DEFAULT_PROPERTIES_FILE_DIR = "meta-inf" + DEFAULT_FILE_SEPARATOR;
	public static final String DEFAULT_DATA_FILE_DIR = ".data" + DEFAULT_FILE_SEPARATOR;

	public static final String DEFAULT_PROPERTIES_XML_FILE_EXT = ".xml";
	public final static String DEFAULT_BACKUP_EXTENSION = ".bkp";
	public final static String DEFAULT_EXAMPLE_EXTENSION = ".example";

	public static final int FRACTION_INPUT_STREAM_REFRESH_WAIT_IO = 2000; // 2 seconds
	public static final int IO_BUFFER_SIZE = 2 * KILO; // 2 KB

	public static final String LTM_DIR = DEFAULT_DATA_FILE_DIR + "ltm" + DEFAULT_FILE_SEPARATOR; // long term memory
	public static final String LTM_DBNAME_DIR = LTM_DIR + "rdb"; // relational data base
	public static final File LTM_STREAM_DIR = new File(LTM_DIR, "jbs"); // leitej binary stream

}

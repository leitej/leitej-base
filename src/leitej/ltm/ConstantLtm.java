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

package leitej.ltm;

import java.lang.reflect.Array;

import leitej.Constant;

/**
 *
 * @author Julio Leite
 */
final class ConstantLtm {

	private ConstantLtm() {
	}

	static final String DEFAULT_LTM_DIR = Constant.DEFAULT_DATA_FILE_DIR + "/ltm"; // long term memory
	static final String DEFAULT_DBNAME = DEFAULT_LTM_DIR + "/rdb"; // relational data base
	static final String DEFAULT_STREAM_DIR = DEFAULT_LTM_DIR + "/rdb.jbs"; // leitej binary stream

	static final Class<?> CLASS_ARRAY_BOOLEAN = Array.newInstance(Boolean.class, 0).getClass();
	static final Class<?> CLASS_ARRAY_BYTE = Array.newInstance(Byte.class, 0).getClass();
	static final Class<?> CLASS_ARRAY_SHORT = Array.newInstance(Short.class, 0).getClass();
	static final Class<?> CLASS_ARRAY_INTEGER = Array.newInstance(Integer.class, 0).getClass();
	static final Class<?> CLASS_ARRAY_LONG = Array.newInstance(Long.class, 0).getClass();

	static final boolean ID_DEFAULT_UNIQUE = true;
	static final boolean ID_DEFAULT_NULLABLE = false;
	static final boolean ID_DEFAULT_INSERTABLE = true;
	static final boolean ID_DEFAULT_UPDATABLE = false;
	static final int ID_DEFAULT_LENGTH = 255;
	static final int ID_DEFAULT_PRECISION = 0;
	static final int ID_DEFAULT_SCALE = 0;
	static final SequenceGeneratorEnum ID_DEFAULT_SEQUENCE_GENERATOR = SequenceGeneratorEnum.JAVA_UNIQUE;

	static final boolean STREAM_DEFAULT_UNIQUE = true;
	static final boolean STREAM_DEFAULT_NULLABLE = true;
	static final boolean STREAM_DEFAULT_INSERTABLE = true;
	static final boolean STREAM_DEFAULT_UPDATABLE = true;
	static final int STREAM_DEFAULT_LENGTH = 255;
	static final int STREAM_DEFAULT_PRECISION = 0;
	static final int STREAM_DEFAULT_SCALE = 0;

}

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

import java.sql.Array;
import java.sql.SQLException;

import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
final class SqlParser {

	private SqlParser() {
	}

	/**
	 *
	 * @param javaArray
	 * @param sqlArray
	 * @return
	 * @throws SQLException if an error occurs while attempting to access the array
	 */
	static Object parseArray(final Class<?> javaArray, final Array sqlArray) throws SQLException {
		Object result = sqlArray.getArray();
		if (!javaArray.isInstance(result)) {
			if (javaArray.getComponentType().equals(Byte.class)) {
				final Object[] objTmpArray = (Object[]) result;
				final Byte[] byteTmpArray = new Byte[objTmpArray.length];
				for (int i = 0; i < objTmpArray.length; i++) {
					if (objTmpArray[i] != null) {
						byteTmpArray[i] = Number.class.cast(objTmpArray[i]).byteValue();
					}
				}
				result = byteTmpArray;
			} else {
				throw new ImplementationLtRtException();
			}
		}
		return result;
	}

}

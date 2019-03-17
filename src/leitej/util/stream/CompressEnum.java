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

package leitej.util.stream;

import java.util.zip.Deflater;

/**
 * Enumerates all the possible values for CompressEnum field.
 *
 * @author Julio Leite
 */
public enum CompressEnum {
	DEFAULT_COMPRESSION, NO_COMPRESSION, BEST_SPEED, BEST_COMPRESSION;

	/**
	 * Gives the corresponding constant of CompressEnum used by
	 * {@link java.util.zip.Deflater Deflater}.
	 *
	 * @return int
	 */
	int deflaterValue() {
		switch (this) {
		case DEFAULT_COMPRESSION:
			return Deflater.DEFAULT_COMPRESSION;
		case NO_COMPRESSION:
			return Deflater.NO_COMPRESSION;
		case BEST_SPEED:
			return Deflater.BEST_SPEED;
		case BEST_COMPRESSION:
			return Deflater.BEST_COMPRESSION;
		default:
			throw new IllegalStateException(this.toString());
		}
	}

}

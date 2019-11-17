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

package leitej.util.stream;

import leitej.exception.ImplementationLtRtException;

/**
 * Enumerates all the possible values for FilePiecesAccessEnum field.
 *
 * @author Julio Leite
 */
public enum RandomAccessModeEnum {
	R, // Open for reading only. Invoking any of the write methods of the resulting
		// object will cause an java.io.IOException to be thrown.
	RW, // Open for reading and writing. If the file does not already exist then an
		// attempt will be made to create it.
	RWS, // Open for reading and writing, as with "rw", and also require that every
			// update to the file's content or metadata be written synchronously to the
			// underlying storage device.
	RWD; // Open for reading and writing, as with "rw", and also require that every
			// update to the file's content be written synchronously to the underlying
			// storage device.

	String getRandomAccessFileMode() {
		switch (this) {
		case R:
			return "r";
		case RW:
			return "rw";
		case RWS:
			return "rws";
		case RWD:
			return "rwd";
		default:
			throw new ImplementationLtRtException(this.toString());
		}
	}

	boolean isSynchronously() {
		switch (this) {
		case R:
			return false;
		case RW:
			return false;
		case RWS:
			return true;
		case RWD:
			return true;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
	}
}

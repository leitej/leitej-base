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

package leitej.util;

/**
 *
 *
 * @author Julio Leite
 */
public final class BinaryUtil {

	public static final int LONG_BYTE_LENGTH = 8;

	private BinaryUtil() {
	}

	public static long readLong64bit(final byte[] buffer) {
		if (buffer == null || buffer.length < LONG_BYTE_LENGTH) {
			throw new IllegalArgumentException();
		}
		return ((long) ((int) (buffer[0] & 0xFF) << 24) + ((int) (buffer[1] & 0xFF) << 16)
				+ ((int) (buffer[2] & 0xFF) << 8) + ((int) (buffer[3] & 0xFF) << 0) << 32)
				+ (((int) (buffer[4] & 0xFF) << 24) + ((int) (buffer[5] & 0xFF) << 16) + ((int) (buffer[6] & 0xFF) << 8)
						+ ((int) (buffer[7] & 0xFF) << 0) & 0xFFFFFFFFL);
	}

	public static void writeLong64bit(final byte[] buffer, final long v) {
		if (buffer == null || buffer.length < LONG_BYTE_LENGTH) {
			throw new IllegalArgumentException();
		}
		buffer[0] = (byte) ((v >>> 56) & 0xFF);
		buffer[1] = (byte) ((v >>> 48) & 0xFF);
		buffer[2] = (byte) ((v >>> 40) & 0xFF);
		buffer[3] = (byte) ((v >>> 32) & 0xFF);
		buffer[4] = (byte) ((v >>> 24) & 0xFF);
		buffer[5] = (byte) ((v >>> 16) & 0xFF);
		buffer[6] = (byte) ((v >>> 8) & 0xFF);
		buffer[7] = (byte) ((v >>> 0) & 0xFF);
	}

	@Deprecated
	public static byte[] convert(final Byte[] array) throws NullPointerException {
		if (array == null) {
			return null;
		}
		return convert(array, array.length);
	}

	@Deprecated
	public static byte[] convert(final Byte[] array, final int length) throws NullPointerException {
		if (array == null) {
			return null;
		}
		final byte[] result = new byte[length];
//		System.arraycopy(array, 0, result, 0, length);
		for (int i = 0; i < length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	@Deprecated
	public static Byte[] convert(final byte[] array) throws NullPointerException {
		if (array == null) {
			return null;
		}
		return convert(array, array.length);
	}

	@Deprecated
	public static Byte[] convert(final byte[] array, final int length) throws NullPointerException {
		if (array == null) {
			return null;
		}
		final Byte[] result = new Byte[length];
//		System.arraycopy(array, 0, result, 0, length);
		for (int i = 0; i < length; i++) {
			result[i] = array[i];
		}
		return result;
	}

}

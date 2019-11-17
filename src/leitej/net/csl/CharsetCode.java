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

package leitej.net.csl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import leitej.Constant;
import leitej.exception.ClosedLtRtException;
import leitej.exception.IllegalArgumentLtRtException;

/**
 *
 *
 * @author Julio Leite
 */
final class CharsetCode {

	// 8 bits code

	static final byte UTF8 = 0x01;

	private CharsetCode() {
	}

	/**
	 *
	 * @param csc byte code
	 * @return charset name for the byte code
	 * @throws IllegalArgumentLtRtException if byte code is not defined
	 */
	private static String getCharsetName(final byte csc) throws IllegalArgumentLtRtException {
		switch (csc) {
		case UTF8:
			return Constant.UTF8_CHARSET_NAME;
		default:
			throw new IllegalArgumentLtRtException("lt.CSLCharsetCodeNotDefined", csc);
		}
	}

	/**
	 *
	 * @param charsetName the name of the requested charset; may be either a
	 *                    canonical name or an alias
	 * @return byte code for the charsetName
	 * @throws IllegalArgumentLtRtException if the charset name is not defined
	 */
	private static byte getCharsetCode(final String charsetName) throws IllegalArgumentLtRtException {
		final Charset cs = Charset.forName(charsetName);
		if (cs.toString().equals(Constant.UTF8_CHARSET_NAME)) {
			return UTF8;
		}
		throw new IllegalArgumentLtRtException("lt.CSLCharsetNameNotDefined", charsetName);
	}

	/**
	 *
	 * @param os          OutputStream to be written
	 * @param charsetName the name of the requested charset; may be either a
	 *                    canonical name or an alias
	 * @throws IOException                  if an I/O error occurs
	 * @throws IllegalArgumentLtRtException if the charset name is not defined
	 */
	static void writeCharsetCode(final OutputStream os, final String charsetName)
			throws IllegalArgumentLtRtException, IOException {
		os.write(CharsetCode.getCharsetCode(charsetName));
		os.flush();
	}

	/**
	 *
	 * @param is InputStream to be read
	 * @return charset name
	 * @throws IOException                  if an I/O error occurs
	 * @throws IllegalArgumentLtRtException if byte code read is not defined
	 */
	static String readCharsetCode(final InputStream is) throws IllegalArgumentLtRtException, IOException {
		final int csc = is.read();
		if (csc == -1) {
			throw new IOException(new ClosedLtRtException("lt.CSLEndStream"));
		}
		return CharsetCode.getCharsetName((byte) (csc & 0xff));
	}

}

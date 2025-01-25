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
	 * @return charset for the byte code
	 * @throws IllegalArgumentLtRtException if byte code is not defined
	 */
	private static Charset getCharsetName(final byte csc) throws IllegalArgumentLtRtException {
		switch (csc) {
		case UTF8:
			return Constant.UTF8_CHARSET;
		default:
			throw new IllegalArgumentLtRtException("Charset code '#0' not defined", csc);
		}
	}

	/**
	 *
	 * @param charset
	 * @return byte code for the charset
	 * @throws IllegalArgumentLtRtException if the charset is not defined
	 */
	private static byte getCharsetCode(final Charset charset) throws IllegalArgumentLtRtException {
		if (Constant.UTF8_CHARSET.equals(charset)) {
			return UTF8;
		}
		throw new IllegalArgumentLtRtException("Charset name '#0' not defined", charset);
	}

	/**
	 *
	 * @param os      OutputStream to be written
	 * @param charset
	 * @throws IOException                  if an I/O error occurs
	 * @throws IllegalArgumentLtRtException if the charset is not defined
	 */
	static void writeCharsetCode(final OutputStream os, final Charset charset)
			throws IllegalArgumentLtRtException, IOException {
		os.write(CharsetCode.getCharsetCode(charset));
		os.flush();
	}

	/**
	 *
	 * @param is InputStream to be read
	 * @return charset
	 * @throws IOException                  if an I/O error occurs
	 * @throws IllegalArgumentLtRtException if byte code read is not defined
	 */
	static Charset readCharsetCode(final InputStream is) throws IllegalArgumentLtRtException, IOException {
		final int csc = is.read();
		if (csc == -1) {
			throw new IOException(new ClosedLtRtException("Unexpected end of stream"));
		}
		return CharsetCode.getCharsetName((byte) (csc & 0xff));
	}

}

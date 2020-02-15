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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import leitej.Constant;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.util.HexaUtil;

/**
 *
 *
 * @author Julio Leite
 */
public final class StreamUtil {

	private static final int BUFFER_SIZE = Constant.IO_BUFFER_SIZE; // 2 KB
	private static final int FLUSH_INTERVAL = Constant.MEGA; // 1 MB

	/**
	 * Creates a new instance of StreamUtil.
	 */
	private StreamUtil() {
	}

	/**
	 *
	 * @param in
	 * @param out
	 * @param doFlush
	 * @return
	 * @throws IOException If some other I/O error occurs
	 */
	public static long pipe(final InputStream in, final OutputStream out, final boolean doFlush) throws IOException {
		long result;
		if (doFlush) {
			result = pipe(in, out, FLUSH_INTERVAL);
		} else {
			result = pipe(in, out, 0);
		}
		return result;
	}

	/**
	 *
	 * @param in
	 * @param out
	 * @param flushInterval if greater then zero, activate the call of the flush
	 * @return
	 * @throws IOException If some other I/O error occurs
	 */
	public static long pipe(final InputStream in, final OutputStream out, final int flushInterval) throws IOException {
		final byte[] buffer = new byte[BUFFER_SIZE];
		long result = 0;
		int numRead;
		int flushCount = 0;
		final boolean doFlush = flushInterval > 0;
		while ((numRead = in.read(buffer)) >= 0) {
			out.write(buffer, 0, numRead);
			result += numRead;
			if (doFlush) {
				flushCount += numRead;
				if (flushInterval < flushCount) {
					flushCount = 0;
					out.flush();
				}
			}
		}
		if (doFlush) {
			out.flush();
		}
		return result;
	}

	/**
	 *
	 * @param in
	 * @param out
	 * @param doFlush
	 * @return
	 * @throws IOException If some other I/O error occurs
	 */
	public static long pipeReadable(final Reader in, final Writer out, final boolean doFlush) throws IOException {
		long result;
		if (doFlush) {
			result = pipeReadable(in, out, FLUSH_INTERVAL);
		} else {
			result = pipeReadable(in, out, 0);
		}
		return result;
	}

	/**
	 *
	 * @param in
	 * @param out
	 * @param flushInterval if greater then zero, activate the call of the flush
	 * @return
	 * @throws IOException If some other I/O error occurs
	 */
	public static long pipeReadable(final Reader in, final Writer out, final int flushInterval) throws IOException {
		final char[] cBuffer = new char[BUFFER_SIZE];
		long result = 0;
		int numRead;
		int flushCount = 0;
		final boolean doFlush = flushInterval > 0;
		while ((numRead = in.read(cBuffer)) >= 0) {
			out.write(cBuffer, 0, numRead);
			result += numRead;
			if (doFlush) {
				flushCount += numRead;
				if (flushInterval < flushCount) {
					flushCount = 0;
					out.flush();
				}
			}
		}
		if (doFlush) {
			out.flush();
		}
		return result;
	}

	/**
	 *
	 * @param in
	 * @param out
	 * @param doFlush
	 * @return
	 * @throws IOException If some other I/O error occurs
	 */
	public static long pipeToHex(final InputStream in, final Writer out, final boolean doFlush) throws IOException {
		long result;
		if (doFlush) {
			result = pipeToHex(in, out, FLUSH_INTERVAL);
		} else {
			result = pipeToHex(in, out, 0);
		}
		return result;
	}

	/**
	 *
	 * @param in
	 * @param out
	 * @param flushInterval if greater then zero, activate the call of the flush
	 * @return
	 * @throws IOException If some other I/O error occurs
	 */
	public static long pipeToHex(final InputStream in, final Writer out, final int flushInterval) throws IOException {
		final byte[] buffer = new byte[BUFFER_SIZE];
		final StringBuilder sb = new StringBuilder();
		long result = 0;
		int numRead;
		int flushCount = 0;
		final boolean doFlush = flushInterval > 0;
		while ((numRead = in.read(buffer)) >= 0) {
			sb.setLength(0);
			HexaUtil.toHex(sb, buffer, 0, numRead);
			out.append(sb);
			result += numRead;
			if (doFlush) {
				flushCount += numRead;
				if (flushInterval < flushCount) {
					flushCount = 0;
					out.flush();
				}
			}
		}
		if (doFlush) {
			out.flush();
		}
		return result;
	}

	/**
	 *
	 * @param in
	 * @param out
	 * @param doFlush
	 * @return
	 * @throws IOException If some other I/O error occurs
	 */
	public static long pipeFromHex(final Reader in, final OutputStream out, final boolean doFlush) throws IOException {
		long result;
		if (doFlush) {
			result = pipeFromHex(in, out, FLUSH_INTERVAL);
		} else {
			result = pipeFromHex(in, out, 0);
		}
		return result;
	}

	/**
	 *
	 * @param in
	 * @param out
	 * @param flushInterval if greater then zero, activate the call of the flush
	 * @return
	 * @throws IOException If some other I/O error occurs
	 */
	public static long pipeFromHex(final Reader in, final OutputStream out, final int flushInterval)
			throws IOException {
		final char[] cBuffer = new char[BUFFER_SIZE];
		final byte[] buffer = new byte[(BUFFER_SIZE / 2) + 1];
		final StringBuilder sb = new StringBuilder();
		long result = 0;
		int numRead;
		int numWrite;
		int flushCount = 0;
		final boolean doFlush = flushInterval > 0;
		int odd = 0;
		while ((numRead = in.read(cBuffer)) >= 0) {
			sb.append(cBuffer, 0, numRead);
			odd = ((numRead & 1) + odd) & 1;
			numWrite = HexaUtil.toByte(buffer, sb, 0, sb.length() - odd);
			out.write(buffer, 0, numWrite);
			result += numRead;
			if (numRead != 0) {
				sb.setLength(0);
				if (odd != 0) {
					sb.append(cBuffer[numRead - 1]);
				}
			}
			if (doFlush) {
				flushCount += numWrite;
				if (flushInterval < flushCount) {
					flushCount = 0;
					out.flush();
				}
			}
		}
		if (odd != 0) {
			throw new IllegalArgumentLtRtException("buffered_reader has an invalid hex value");
		}
		if (doFlush) {
			out.flush();
		}
		return result;
	}

	/**
	 * Calculates the MD5 of a input stream.<br/>
	 * Closes input stream at the end of read.
	 *
	 * @param in input stream
	 * @return the array of bytes for the resulting hash value
	 * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi
	 *                                  implementation for the specified algorithm
	 * @throws IOException              if the first byte cannot be read for any
	 *                                  reason other than the end of the file, if
	 *                                  the input stream has been closed, or if some
	 *                                  other I/O error occurs
	 */
	public static byte[] md5(final InputStream in) throws NoSuchAlgorithmException, IOException {
		final MessageDigest md = MessageDigest.getInstance("MD5");
		final byte[] buffer = new byte[BUFFER_SIZE];
		int tmp = 0;
		try {
			while (tmp != -1) {
				tmp = in.read(buffer);
				if (tmp != -1) {
					md.update(buffer, 0, tmp);
				}
			}
		} finally {
			in.close();
		}
		return md.digest();
	}

}

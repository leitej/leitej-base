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

package leitej.crypto.symmetric.stream;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.spec.IvParameterSpec;

import leitej.crypto.symmetric.CircBlockCipher;
import leitej.exception.IllegalArgumentLtRtException;

/**
 *
 * @author Julio Leite
 */
public final class CircInputStream extends InputStream {

	private final InputStream in;
	private final CircBlockCipher blockCipher;
	private final byte[] byteArray;
	private final byte[] byteArrayOut;
	private final int blockByteSize;

	/**
	 *
	 * @param is
	 * @param cipherAlgorithm
	 * @throws IllegalArgumentLtRtException if <code>is</code> is null or
	 *                                      <code>cipher</code> is null
	 */
	public CircInputStream(final InputStream is, final String cipherAlgorithm) throws IllegalArgumentLtRtException {
		super();
		if (is == null || cipherAlgorithm == null) {
			throw new IllegalArgumentLtRtException();
		}
		this.in = is;
		this.blockCipher = new CircBlockCipher(cipherAlgorithm);
		this.byteArray = new byte[1];
		this.blockByteSize = this.blockCipher.getBlockByteSize();
		this.byteArrayOut = new byte[this.blockByteSize];
	}

	/**
	 *
	 * @param forEncryption
	 * @param key
	 * @param ivSpec
	 * @throws InvalidKeyException
	 * @throws IllegalArgumentLtRtException if <code>ivSpec</code> is null or has a
	 *                                      size different from the block size
	 */
	public final void cipherInit(final boolean forEncryption, final Key key, final IvParameterSpec ivSpec)
			throws InvalidKeyException, IllegalArgumentLtRtException {
		this.blockCipher.init(forEncryption, key, ivSpec);
	}

	public final int ivByteSize() {
		return this.blockByteSize;
	}

	@Override
	public int read() throws IOException {
		final int read = this.in.read();
		if (read == -1) {
			return -1;
		}
		this.byteArray[0] = (byte) (read & 0xff);
		while (this.blockCipher.process(this.byteArray, 0, 1, this.byteArrayOut, 0) == 0) {
		}
		return this.byteArrayOut[0];
	}

	@Override
	public int read(final byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(final byte b[], final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		int count = 0;
		int countStep;
		int i;
		while (count < len) {
			countStep = this.in.read(this.byteArrayOut, 0, Math.min(len - count, this.blockByteSize));
			if (countStep == 0) {
				return count;
			}
			if (countStep == -1) {
				if (count == 0) {
					return -1;
				}
				return count;
			}
			i = 0;
			while (i < countStep) {
				i += this.blockCipher.process(this.byteArrayOut, i, countStep - i, b, off + count + i);
			}
			count += countStep;
		}
		return count;
	}

	@Override
	public long skip(long n) throws IOException {
		long i = 0;
		final int available = available();
		if (available < n) {
			n = available;
		}
		// TODO: optimise to array?
		while ((i < n) && (read() != -1)) {
			i++;
		}
		return i;
	}

	@Override
	public int available() throws IOException {
		return this.in.available();
	}

	@Override
	public void close() throws IOException {
		this.in.close();
	}

	@Override
	public synchronized void mark(final int readlimit) {
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}

	@Override
	public boolean markSupported() {
		return false;
	}

}

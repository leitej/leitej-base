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
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.spec.IvParameterSpec;

import leitej.crypto.symmetric.CipherEnum;
import leitej.crypto.symmetric.CircBlockCipher;
import leitej.exception.IllegalArgumentLtRtException;

/**
 *
 * @author Julio Leite
 */
public final class CircOutputStream extends OutputStream {

	private final OutputStream out;
	private final CircBlockCipher blockCipher;
	private final byte[] byteArray;
	private final byte[] byteArrayOut;
	private final int blockByteSize;

	/**
	 *
	 * @param os
	 * @param cipherAlgorithm
	 * @throws IllegalArgumentLtRtException if <code>os</code> is null or
	 *                                      <code>cipher</code> is null
	 */
	public CircOutputStream(final OutputStream os, final CipherEnum cipherAlgorithm) throws IllegalArgumentLtRtException {
		super();
		if (os == null || cipherAlgorithm == null) {
			throw new IllegalArgumentLtRtException();
		}
		this.out = os;
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
	public void write(final int b) throws IOException {
		this.byteArray[0] = (byte) (b & 0xff);
		while (this.blockCipher.process(this.byteArray, 0, 1, this.byteArrayOut, 0) == 0) {
		}
		this.out.write(this.byteArrayOut, 0, 1);
	}

	@Override
	public void write(final byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		int count = 0;
		int countStep;
		while (count < len) {
			countStep = this.blockCipher.process(b, off + count, Math.min(len - count, this.blockByteSize), this.byteArrayOut,
					0);
			this.out.write(this.byteArrayOut, 0, countStep);
			count += countStep;
		}
	}

	@Override
	public void flush() throws IOException {
		this.out.flush();
	}

	@Override
	public void close() throws IOException {
		this.out.close();
	}

}

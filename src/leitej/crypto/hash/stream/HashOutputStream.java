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

package leitej.crypto.hash.stream;

import java.io.OutputStream;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.ShortBufferException;

import leitej.crypto.Cryptography;
import leitej.crypto.hash.MessageDigestEnum;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
public class HashOutputStream extends AbstractHashOutputStream {

	private final MessageDigest hash;

	/**
	 *
	 * @param out
	 * @param digestAlgorithm
	 * @param stepLength      auto verify digest at <code>stepLength</code> length
	 *                        of data
	 * @throws IllegalArgumentLtRtException if <code>os</code> is null,
	 *                                      <code>digestAlgorithm</code> is null or
	 *                                      <code>stepLength</code> is less then 1
	 */
	public HashOutputStream(final OutputStream out, final MessageDigestEnum digestAlgorithm, final int stepLength)
			throws IllegalArgumentLtRtException {
		super(out, stepLength);
		if (digestAlgorithm == null) {
			throw new IllegalArgumentLtRtException();
		}
		try {
			this.hash = Cryptography.getMessageDigest(digestAlgorithm);
		} catch (final NoSuchAlgorithmException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchProviderException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	@Override
	protected final int digestLength() {
		return this.hash.getDigestLength();
	}

	@Override
	protected void update(final byte[] input, final int offset, final int len) {
		this.hash.update(input, offset, len);
	}

	@Override
	protected final void digest(final byte[] buf, final int offset) throws DigestException, ShortBufferException {
		if (buf.length < this.hash.getDigestLength() + offset) {
			throw new ShortBufferException();
		}
		this.hash.digest(buf, offset, this.hash.getDigestLength());
	}

}

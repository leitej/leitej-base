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

import java.io.InputStream;
import java.security.DigestException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Mac;
import javax.crypto.ShortBufferException;

import leitej.crypto.Cryptography;
import leitej.crypto.hash.HMacEnum;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
public class HMacInputStream extends AbstractHashInputStream {

	private final Mac hashMac;

	/**
	 * @param is
	 * @param hmacAlgorithm
	 * @param key
	 * @param maxStepLength maximum amount of untrusted data received before digest
	 *                      verify
	 * @throws IllegalArgumentLtRtException if <code>is</code> is null,
	 *                                      <code>hmacAlgorithm</code> is null or
	 *                                      <code>maxStepLength</code> is less then
	 *                                      1
	 * @throws InvalidKeyException
	 */
	public HMacInputStream(final InputStream is, final HMacEnum hmacAlgorithm, final Key key, final int maxStepLength)
			throws IllegalArgumentLtRtException, InvalidKeyException {
		super(is, maxStepLength);
		if (hmacAlgorithm == null) {
			throw new IllegalArgumentLtRtException();
		}
		try {
			this.hashMac = Cryptography.getHmac(hmacAlgorithm);
		} catch (final NoSuchAlgorithmException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchProviderException e) {
			throw new ImplementationLtRtException(e);
		}
		this.hashMac.init(key);
	}

	@Override
	protected int digestLength() {
		return this.hashMac.getMacLength();
	}

	@Override
	protected void update(final byte[] input, final int offset, final int len) {
		this.hashMac.update(input, offset, len);
	}

	@Override
	protected void digest(final byte[] buf, final int offset) throws DigestException, ShortBufferException {
		this.hashMac.doFinal(buf, offset);
	}

}

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

package leitej.crypto.keyStore;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.log.Logger;

/**
 *
 * @author Julio Leite
 */
public final class Password {

	private static final Logger LOG = Logger.getInstance();

	private static transient final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final int INTEGER_SIZE_IN_BYTES = Integer.SIZE >>> 3;

	/**
	 * Writes the zero character at all positions of the array.
	 *
	 * @param chars
	 */
	public static final void erase(final char[] chars) {
		for (int i = 0; i < chars.length; i++) {
			chars[i] = '\0';
		}
	}

	/**
	 * Writes random characters at the array.
	 *
	 * @param chars
	 * @param random
	 */
	public static final void erase(final char[] chars, final Random random) {
		final byte[] buffer = new byte[INTEGER_SIZE_IN_BYTES];
		if (buffer.length != 4) {
			throw new ImplementationLtRtException();
		}
		for (int i = 0; i < chars.length; i += 2) {
			random.nextBytes(buffer);
			chars[i] = (char) (((buffer[0] & 0xff) << 8) + (buffer[1]));
			if (i + 1 < chars.length) {
				chars[i + 1] = (char) (((buffer[2] & 0xff) << 8) + (buffer[3]));
			}
		}
	}

	private transient final Random random;
	private transient volatile int counter;
	private transient volatile Map<Integer, byte[]> a;
	private transient volatile Integer[] b;
	private transient volatile byte[] c;
	private transient volatile char[] d;

	/**
	 * <br/>
	 * Argument <code>password</code> is filled with garbage.
	 *
	 * @param password
	 * @throws IllegalArgumentLtRtException if <code>password</code> is null
	 */
	public Password(final char[] password) throws IllegalArgumentLtRtException {
		if (password == null) {
			throw new IllegalArgumentLtRtException();
		}
		this.random = new Random();
		this.counter = 0;
		final byte[] buffer = new byte[INTEGER_SIZE_IN_BYTES];
		if (buffer.length != 4) {
			throw new ImplementationLtRtException();
		}
		final int passwordByteSize = password.length << 1;
		this.a = new HashMap<>(passwordByteSize);
		this.b = new Integer[passwordByteSize];
		this.c = new byte[passwordByteSize];
		this.d = new char[password.length];
		synchronized (SECURE_RANDOM) {
			int count;
			int count2;
			int jLimit;
			for (int i = 0; i < password.length; i += (INTEGER_SIZE_IN_BYTES >>> 1)) {
				SECURE_RANDOM.nextBytes(buffer);
				count = 0;
				jLimit = (i + (INTEGER_SIZE_IN_BYTES >>> 1)) << 1;
				for (int j = i << 1; j < passwordByteSize && j < jLimit; j += 2) {
					count2 = count << 1;
					do {
						this.b[j] = Integer.valueOf(SECURE_RANDOM.nextInt());
					} while (this.a.get(this.b[j]) != null);
					this.a.put(this.b[j], new byte[] { buffer[count2] });
					this.c[j] = (byte) ((password[i + count] & 0xff) ^ buffer[count2]);
					count2++;
					do {
						this.b[j + 1] = Integer.valueOf(SECURE_RANDOM.nextInt());
					} while (this.a.get(this.b[j + 1]) != null);
					this.a.put(this.b[j + 1], new byte[] { buffer[count2] });
					this.c[j + 1] = (byte) (((password[i + count] & 0xff00) >>> 8) ^ buffer[count2]);
					count++;
				}
			}
		}
		this.random.nextBytes(buffer);
		erase(password, this.random);
	}

	/**
	 * Retrieves the plain password. <br/>
	 * <i>Invoke the <code>erasePassword()</code> as soon as possible for each time
	 * this method is invoked. <br/>
	 * And ensure that the result is not copied</i>.
	 *
	 * @return
	 * @throws IllegalStateLtRtException if already destroyed
	 */
	final synchronized char[] getPassword() throws IllegalStateLtRtException {
		if (this.d == null) {
			throw new IllegalStateLtRtException();
		}
		this.counter++;
		if (this.counter == 1) {
			int j;
			for (int i = 0; i < this.d.length; i++) {
				j = i << 1;
				this.d[i] = (char) ((((this.c[j + 1] ^ this.a.get(this.b[j + 1])[0]) & 0xff) << 8)
						+ ((this.c[j] ^ this.a.get(this.b[j])[0]) & 0xff));
			}
		}
		return this.d;
	}

	/**
	 * Erases as soon as possible plain password from memory.
	 */
	final synchronized void erasePassword() {
		if (this.d == null) {
			return;
		}
		if (this.counter == 0) {
			throw new IllegalStateLtRtException();
		}
		if (this.d != null && this.counter == 1) {
			erase(this.d, this.random);
		}
		this.counter--;
	}

	/**
	 * Clears the password material permanently.
	 */
	public synchronized final void destroy() {
		if (this.d != null) {
			LOG.debug("initialized");
			this.random.nextBytes(this.c);
			this.c = null;
			if (this.counter > 1) {
				this.counter = 1;
			}
			if (this.counter != 0) {
				erasePassword();
			}
			this.d = null;
		}
	}

	@Override
	public final boolean equals(final Object object) {
		if (object == null || !Password.class.isInstance(object)) {
			return false;
		}
		final Password other = Password.class.cast(object);
		if (this.c == null || other.c == null || this.c.length != other.c.length) {
			return false;
		}
		boolean result = true;
		getPassword();
		other.getPassword();
		for (int i = 0; i < this.d.length && result; i++) {
			if (this.d[i] != other.d[i]) {
				result = false;
			}
		}
		erasePassword();
		other.erasePassword();
		return result;
	}

	@Override
	public final Object clone() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void finalize() throws Throwable {
		destroy();
		super.finalize();
	}

}

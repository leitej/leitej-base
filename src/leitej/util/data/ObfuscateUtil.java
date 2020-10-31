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

package leitej.util.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.Random;

import leitej.Constant;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.UnsupportedDataTypeLtRtException;
import leitej.log.Logger;
import leitej.util.HexaUtil;
import leitej.util.NetUtil;
import leitej.util.machine.UserUtil;

/**
 * @author Julio Leite
 *
 */
public final class ObfuscateUtil {

	private static final String OBFUSCATE_PREFIX = "{o}";
	private static final int ENTROPY_GEN_STEP_SIZE = 128;
	private static final Random RANDOM;
	private static byte[] ENTROPY;
	private static byte[] USERNAME_SALT;
	private static byte[] HOSTNAME_SALT;

	static {
		long seed = 0;
		try {
			final File SEED_FILE = new File(Constant.DEFAULT_PROPERTIES_FILE_DIR,
					Obfuscate.class.getCanonicalName() + ".seed");
			if (SEED_FILE.exists()) {
				ObjectInputStream ois = null;
				try {
					ois = new ObjectInputStream(new FileInputStream(SEED_FILE));
					seed = ois.readLong();
				} finally {
					if (ois != null) {
						ois.close();
					}
				}
			} else {
				seed = (new Random()).nextLong();
				ObjectOutputStream oos = null;
				try {
					oos = new ObjectOutputStream(new FileOutputStream(SEED_FILE));
					oos.writeLong(seed);
					oos.flush();
				} finally {
					if (oos != null) {
						oos.close();
					}
				}
			}
		} catch (final IOException e) {
			Logger.getInstance().fatal("#0", e);
		}
		RANDOM = new Random(seed);
		ENTROPY = new byte[ENTROPY_GEN_STEP_SIZE];
		RANDOM.nextBytes(ENTROPY);
		USERNAME_SALT = null;
		HOSTNAME_SALT = null;
	}

	private static void incrementEntropy() {
		final byte[] firstBytes = ENTROPY;
		ENTROPY = new byte[firstBytes.length + ENTROPY_GEN_STEP_SIZE];
		final byte[] moreBytes = new byte[ENTROPY_GEN_STEP_SIZE];
		RANDOM.nextBytes(moreBytes);
		System.arraycopy(firstBytes, 0, ENTROPY, 0, firstBytes.length);
		System.arraycopy(moreBytes, 0, ENTROPY, firstBytes.length, ENTROPY_GEN_STEP_SIZE);
	}

	private static void xor(final byte[] bytes, final byte[] salt, final boolean fullSalt) {
		int iLimit;
		if (fullSalt && salt.length > bytes.length) {
			iLimit = salt.length;
		} else {
			iLimit = bytes.length;
		}
		int bPos = 0;
		int sPos = 0;
		for (int i = 0; i < iLimit; i++) {
			bytes[bPos] = (byte) (0xff & ((int) bytes[bPos] ^ (int) salt[sPos]));
			bPos++;
			sPos++;
			if (bPos >= bytes.length) {
				bPos = 0;
			}
			if (sPos >= salt.length) {
				sPos = 0;
			}
		}
	}

	private static void usernameSalt(final byte[] bytes) {
		if (USERNAME_SALT == null) {
			if (UserUtil.USER_NAME == null || UserUtil.USER_NAME.length() < 1) {
				throw new IllegalStateException();
			}
			USERNAME_SALT = UserUtil.USER_NAME.getBytes(Constant.UTF8_CHARSET);
		}
		xor(bytes, USERNAME_SALT, true);
	}

	private static void hostnameSalt(final byte[] bytes) {
		if (HOSTNAME_SALT == null) {
			final String localHostname;
			try {
				localHostname = NetUtil.localHostName();
			} catch (final UnknownHostException e) {
				throw new IllegalStateLtRtException(e);
			}
			if (localHostname == null || localHostname.length() < 1) {
				throw new IllegalStateLtRtException();
			}
			HOSTNAME_SALT = localHostname.getBytes(Constant.UTF8_CHARSET);
		}
		xor(bytes, HOSTNAME_SALT, true);
	}

	public static <O> O hide(final O plain) {
		return hide(plain, false, false);
	}

	public static <O> O hide(final Obfuscate annot, final O plain) {
		return hide(plain, annot.usernameSalt(), annot.hostnameSalt());
	}

	@SuppressWarnings("unchecked")
	public static <O> O hide(final O value, final boolean useUsernameSalt, final boolean useHostnameSalt) {
		if (!(value instanceof String)) {
			throw new UnsupportedDataTypeLtRtException("#0", value.getClass().getCanonicalName());
		}
		final O result;
		final String plain = String.class.cast(value);
		if (plain == null) {
			result = null;
		} else {
			if (plain.startsWith(OBFUSCATE_PREFIX)) {
				result = value;
			} else {
				final byte[] bytes = plain.getBytes(Constant.UTF8_CHARSET);
				while (bytes.length > ENTROPY.length) {
					incrementEntropy();
				}
				if (useUsernameSalt) {
					usernameSalt(bytes);
				}
				if (useHostnameSalt) {
					hostnameSalt(bytes);
				}
				xor(bytes, ENTROPY, false);
				result = (O) (OBFUSCATE_PREFIX + HexaUtil.toHex(bytes));
			}
		}
		return result;
	}

	public static <O> O unHide(final O obfuscated) {
		return unHide(obfuscated, false, false);
	}

	public static <O> O unHide(final Obfuscate annot, final O obfuscated) {
		return unHide(obfuscated, annot.usernameSalt(), annot.hostnameSalt());
	}

	@SuppressWarnings("unchecked")
	public static <O> O unHide(final O value, final boolean useUsernameSalt, final boolean useHostnameSalt) {
		if (!(value instanceof String)) {
			throw new UnsupportedDataTypeLtRtException("#0", value.getClass().getCanonicalName());
		}
		final O result;
		final String obfuscated = String.class.cast(value);
		if (obfuscated == null) {
			result = null;
		} else {
			if (!obfuscated.startsWith(OBFUSCATE_PREFIX)) {
				result = value;
			} else {
				final byte[] bytes = HexaUtil
						.toByte(obfuscated.subSequence(OBFUSCATE_PREFIX.length(), obfuscated.length()));
				while (bytes.length > ENTROPY.length) {
					incrementEntropy();
				}
				if (useUsernameSalt) {
					usernameSalt(bytes);
				}
				if (useHostnameSalt) {
					hostnameSalt(bytes);
				}
				xor(bytes, ENTROPY, false);
				result = (O) new String(bytes, Constant.UTF8_CHARSET);
			}
		}
		return result;
	}

}

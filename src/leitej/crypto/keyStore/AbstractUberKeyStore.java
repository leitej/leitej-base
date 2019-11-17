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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.crypto.SecretKey;

import leitej.crypto.ProviderEnum;
import leitej.crypto.exception.KeyStoreLtException;
import leitej.exception.ImplementationLtRtException;
import leitej.util.data.Cache;
import leitej.util.data.CacheWeak;

/**
 *
 * @author Julio Leite
 */
abstract class AbstractUberKeyStore extends AbstractKeyStore {

	private static final KeyStoreEnum KEYSTORE_ENUM = KeyStoreEnum.UBER;
	private static final ProviderEnum PROVIDER_ENUM = ProviderEnum.BC;

	private transient volatile Password password;
	private transient volatile Cache<String, PrivateKey> privateKeyLightCache;
	private transient volatile Cache<String, SecretKey> secretKeyLightCache;

	/**
	 *
	 * @param is       if null creates an empty keystore
	 * @param password the password used to check the integrity of the keystore, the
	 *                 password used to unlock the keystore, or <code>null</code>
	 * @throws IOException         if there is an I/O or format problem with the
	 *                             keystore data, if a password is required but not
	 *                             given, or if the given password was incorrect. If
	 *                             the error is due to a wrong password, the
	 *                             {@link Throwable#getCause cause} of the
	 *                             <code>IOException</code> should be an
	 *                             <code>UnrecoverableKeyException</code>
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause KeyStoreException if a KeyStoreSpi
	 *                             implementation for the specified type is not
	 *                             available from the specified provider <br/>
	 *                             +Cause NoSuchProviderException if the specified
	 *                             provider is not registered in the security
	 *                             provider list <br/>
	 *                             +Cause NoSuchAlgorithmException if the algorithm
	 *                             used to check the integrity of the keystore
	 *                             cannot be found <br/>
	 *                             +Cause CertificateException if any of the
	 *                             certificates in the keystore could not be loaded
	 */
	protected AbstractUberKeyStore(final InputStream is, final Password password)
			throws KeyStoreLtException, IOException {
		super(KEYSTORE_ENUM, PROVIDER_ENUM, is, password);
		this.password = password;
		this.privateKeyLightCache = new CacheWeak<>();
		this.secretKeyLightCache = new CacheWeak<>();
	}

	/**
	 *
	 * @param os
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause NoSuchAlgorithmException if the
	 *                             appropriate data integrity algorithm could not be
	 *                             found <br/>
	 *                             +Cause CertificateException if any of the
	 *                             certificates included in the keystore data could
	 *                             not be stored
	 * @throws IOException         If there was an I/O problem with data
	 */
	protected final void store(final OutputStream os) throws KeyStoreLtException, IOException {
		super.store(os, this.password);
		os.flush();
	}

	protected abstract byte[] getKeyStoredData() throws IOException;

	@Override
	public final boolean changePassword(final Password oldPassword, final Password newPassword)
			throws KeyStoreLtException, IOException {
		boolean result = false;
		synchronized (this.keyStore) {
			if ((this.password == null && oldPassword == null)
					|| (this.password != null && this.password.equals(oldPassword))) {
				persist();
				final InputStream is = new ByteArrayInputStream(getKeyStoredData());
				try {
					final Enumeration<String> aliases = aliases();
					String alias;
					while (aliases.hasMoreElements()) {
						alias = aliases.nextElement();
						if (isKeyEntry(alias)) {
							if (getPrivateKey(alias) != null) {
								super.setPrivateKeyEntry(alias, getPrivateKey(alias), newPassword,
										getKeyCertificateChain(alias));
							} else if (getSecretKey(alias) != null) {
								super.setSecretKeyEntry(alias, getSecretKey(alias), newPassword);
							} else {
								throw new ImplementationLtRtException();
							}
						}
					}
					final Password tmp = this.password;
					try {
						this.password = newPassword;
						persist();
						result = true;
					} finally {
						if (!result) {
							this.password = tmp;
						}
					}
				} finally {
					if (!result) {
						super.load(is, this.password);
					} else {
						clearCache();
					}
					is.close();
				}
			}
		}
		return result;
	}

	@Override
	public final PrivateKey getPrivateKey(final String alias) throws KeyStoreLtException {
		PrivateKey key = this.privateKeyLightCache.get(alias);
		if (key == null) {
			key = super.getPrivateKey(alias, this.password);
			if (key != null) {
				this.privateKeyLightCache.set(alias, key);
			}
		}
		return key;
	}

	@Override
	public final SecretKey getSecretKey(final String alias) throws KeyStoreLtException {
		SecretKey key = this.secretKeyLightCache.get(alias);
		if (key == null) {
			key = super.getSecretKey(alias, this.password);
			if (key != null) {
				this.secretKeyLightCache.set(alias, key);
			}
		}
		return key;
	}

	@Override
	public final void setPrivateKeyEntry(final String alias, final PrivateKey key, final X509Certificate[] chain)
			throws KeyStoreLtException {
		super.setPrivateKeyEntry(alias, key, this.password, chain);
		if (key != null) {
			this.privateKeyLightCache.set(alias, key);
		} else {
			this.privateKeyLightCache.remove(alias);
		}
	}

	@Override
	public final void setSecretKeyEntry(final String alias, final SecretKey secretKey) throws KeyStoreLtException {
		super.setSecretKeyEntry(alias, secretKey, this.password);
		if (secretKey != null) {
			this.secretKeyLightCache.set(alias, secretKey);
		} else {
			this.secretKeyLightCache.remove(alias);
		}
	}

	@Override
	public final void deleteEntry(final String alias) throws KeyStoreLtException {
		super.deleteEntry(alias);
		this.privateKeyLightCache.remove(alias);
		this.secretKeyLightCache.remove(alias);
	}

	@Override
	public final void deleteAllEntries() throws KeyStoreLtException {
		super.deleteAllEntries();
		clearCache();
	}

	private final void clearCache() {
		this.privateKeyLightCache.clear();
		this.secretKeyLightCache.clear();
	}

}

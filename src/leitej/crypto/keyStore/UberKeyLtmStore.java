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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import leitej.exception.KeyStoreLtException;
import leitej.exception.LtmLtRtException;
import leitej.ltm.LargeMemory;
import leitej.ltm.LongTermMemory;
import leitej.ltm.LtmFilter;
import leitej.ltm.LtmFilter.OPERATOR;
import leitej.ltm.LtmFilter.OPERATOR_JOIN;

/**
 *
 * @author Julio Leite
 */
public final class UberKeyLtmStore extends AbstractUberKeyStore {

	private static final LongTermMemory LTM = LongTermMemory.getInstance();
	private static final Map<String, UberKeyLtmStore> INSTANCE_MAP = new HashMap<>();

	/**
	 *
	 * @param alias the alias name
	 * @return if the keystore exists
	 * @throws IOException if there is an I/O or format problem with the keystore
	 *                     data
	 */
	public static final boolean exists(final String alias) throws IOException {
		boolean result = false;
		synchronized (INSTANCE_MAP) {
			final UberKeyLtmStore keyLtmStore = INSTANCE_MAP.get(alias);
			if (keyLtmStore == null) {
				try {
					final LtmFilter<KeyLtmStore> filter = new LtmFilter<>(KeyLtmStore.class, OPERATOR_JOIN.AND);
					filter.prepare(OPERATOR.EQUAL).setAlias(alias);
					final Iterator<KeyLtmStore> found = LTM.search(filter);
					if (found.hasNext()) {
						result = true;
					}
				} catch (final LtmLtRtException e) {
					throw new IOException(e);
				}
			} else {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Constructs a new object with a new <code>KeyStore</code> created, the
	 * password will be used when persist keystore to protects its integrity.<br/>
	 * <br/>
	 * If a password is not given for integrity, then integrity checking is not
	 * performed.
	 *
	 * @param alias    the alias name
	 * @param password the password used to check the integrity of the keystore, the
	 *                 password used to unlock the keystore, or <code>null</code>
	 * @return a new UberKeyLtmStore
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
	 *                             cannot be found
	 */
	public static final UberKeyLtmStore create(final String alias, final Password password)
			throws KeyStoreLtException, IOException {
		return newUberKeyLtmStore(true, alias, password);
	}

	/**
	 * Constructs a new object with a new <code>KeyStore</code> loaded.<br/>
	 * <br/>
	 * A password may be given to unlock the keystore (e.g. the keystore resides on
	 * a hardware token device), or to check the integrity of the keystore data. If
	 * a password is not given for integrity checking, then integrity checking is
	 * not performed.
	 *
	 * @param alias    the alias name
	 * @param password the password used to check the integrity of the keystore, the
	 *                 password used to unlock the keystore, or <code>null</code>
	 * @return a new UberKeyLtmStore
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
	public static final UberKeyLtmStore load(final String alias, final Password password)
			throws KeyStoreLtException, IOException {
		return newUberKeyLtmStore(false, alias, password);
	}

	/**
	 *
	 * @param create   if true creates an empty keystore
	 * @param alias    the alias name
	 * @param password the password used to check the integrity of the keystore, the
	 *                 password used to unlock the keystore, or <code>null</code>
	 * @return a new UberKeyLtmStore
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
	private static final UberKeyLtmStore newUberKeyLtmStore(final boolean create, final String alias,
			final Password password) throws KeyStoreLtException, IOException {
		LargeMemory ltmStore;
		synchronized (INSTANCE_MAP) {
			UberKeyLtmStore keyLtmStore = INSTANCE_MAP.get(alias);
			if (keyLtmStore == null) {
				try {
					final LtmFilter<KeyLtmStore> filter = new LtmFilter<>(KeyLtmStore.class, OPERATOR_JOIN.AND);
					filter.prepare(OPERATOR.EQUAL).setAlias(alias);
					final Iterator<KeyLtmStore> found = LTM.search(filter);
					if (!found.hasNext()) {
						final KeyLtmStore createLtmStore = LTM.newRecord(KeyLtmStore.class);
						createLtmStore.setAlias(alias);
						ltmStore = createLtmStore.getKeyStore();
					} else {
						ltmStore = found.next().getKeyStore();
						if (create) {
							ltmStore.open();
							ltmStore.setLength(0);
							ltmStore.close();
						}
					}
				} catch (final LtmLtRtException e) {
					throw new IOException(e);
				}
				ltmStore.open();
				final InputStream is = ((create) ? null : ltmStore.newInputStream());
				try {
					keyLtmStore = new UberKeyLtmStore(is, ltmStore, password);
				} finally {
					if (is != null) {
						is.close();
						ltmStore.close();
					}
				}
				INSTANCE_MAP.put(alias, keyLtmStore);
			} else {
				if (create) {
					keyLtmStore.deleteAllEntries();
					keyLtmStore.persist();
				}
			}
			return keyLtmStore;
		}
	}

	private final LargeMemory ltmStore;

	/**
	 * @param is
	 * @param password
	 * @throws KeyStoreLtException
	 * @throws IOException
	 */
	private UberKeyLtmStore(final InputStream is, final LargeMemory ltmStore, final Password password)
			throws KeyStoreLtException, IOException {
		super(is, password);
		this.ltmStore = ltmStore;
	}

	@Override
	protected byte[] getKeyStoredData() throws IOException {
		this.ltmStore.open();
		final byte[] result = new byte[(int) this.ltmStore.length()];
		this.ltmStore.readFully(0, result);
		this.ltmStore.close();
		return result;
	}

	@Override
	public void persist() throws KeyStoreLtException, IOException {
		OutputStream os = null;
		try {
			this.ltmStore.open();
			this.ltmStore.setLength(0);
			os = this.ltmStore.newOutputStream();
			super.store(os);
		} finally {
			if (os != null) {
				os.close();
			}
			this.ltmStore.close();
		}
	}

}

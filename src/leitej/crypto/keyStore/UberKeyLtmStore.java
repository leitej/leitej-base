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
import java.util.Map;

import leitej.exception.KeyStoreLtException;
import leitej.exception.LtmLtRtException;
import leitej.ltm.LongTermMemory;
import leitej.ltm.Query;
import leitej.ltm.QueryEntrance;
import leitej.ltm.QueryResult;
import leitej.ltm.lixo.LtmBinary;

/**
 *
 * @author Julio Leite
 */
public final class UberKeyLtmStore extends AbstractUberKeyStore {

	private static final LongTermMemory LTM = LongTermMemory.getInstance();
	private static final QueryResult<KeyLtmStore> FIND_KEYSTORE;
	static {
		LTM.registry(KeyLtmStore.class);
		final QueryEntrance<KeyLtmStore> entranceIV = Query.newTableEntrance(KeyLtmStore.class);
		FIND_KEYSTORE = Query.find(entranceIV,
				Query.filterBy(Query.expression(Query.field(entranceIV, KeyLtmStore.FIELD_ALIAS), Query.equal(),
						Query.dynamicParameter())));
		FIND_KEYSTORE.setFetchScale(1);
	}
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
				synchronized (FIND_KEYSTORE) {
					FIND_KEYSTORE.setParams(alias);
					try {
						if (FIND_KEYSTORE.size() == 1) {
							result = true;
						}
					} catch (final LtmLtRtException e) {
						throw new IOException(e);
					}
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
		LtmBinary ltmStore;
		synchronized (INSTANCE_MAP) {
			UberKeyLtmStore keyLtmStore = INSTANCE_MAP.get(alias);
			if (keyLtmStore == null) {
				synchronized (FIND_KEYSTORE) {
					FIND_KEYSTORE.setParams(alias);
					try {
						if (FIND_KEYSTORE.size() != 1) {
							final KeyLtmStore createLtmStore = LTM.newObject(KeyLtmStore.class);
							createLtmStore.setAlias(alias);
							createLtmStore.save();
							ltmStore = createLtmStore.getKeyStore();
						} else {
							ltmStore = FIND_KEYSTORE.get(0).getKeyStore();
							if (create) {
								ltmStore.access().setLength(0);
								ltmStore.access().close();
							}
						}
					} catch (final LtmLtRtException e) {
						throw new IOException(e);
					}
				}
				final InputStream is = ((create) ? null : ltmStore.access().newInputStream());
				try {
					keyLtmStore = new UberKeyLtmStore(is, ltmStore, password);
				} finally {
					if (is != null) {
						is.close();
						ltmStore.access().close();
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

	private final LtmBinary ltmStore;

	/**
	 * @param is
	 * @param password
	 * @throws KeyStoreLtException
	 * @throws IOException
	 */
	private UberKeyLtmStore(final InputStream is, final LtmBinary ltmStore, final Password password)
			throws KeyStoreLtException, IOException {
		super(is, password);
		this.ltmStore = ltmStore;
	}

	@Override
	protected byte[] getKeyStoredData() throws IOException {
		final byte[] result = new byte[(int) this.ltmStore.access().length()];
		this.ltmStore.access().readFully(0, result);
		return result;
	}

	@Override
	public void persist() throws KeyStoreLtException, IOException {
		OutputStream os = null;
		try {
			this.ltmStore.access().setLength(0);
			os = this.ltmStore.access().newOutputStream();
			super.store(os);
		} finally {
			if (os != null) {
				os.close();
			}
			this.ltmStore.access().close();
		}
	}

}

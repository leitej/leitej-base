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

import leitej.exception.ImplementationLtRtException;
import leitej.exception.KeyStoreLtException;
import leitej.util.stream.FileUtil;

/**
 *
 * @author Julio Leite
 */
public final class UberKeyStore extends AbstractDefaultKeyStore {

	/**
	 * Constructs a new object with a new <code>KeyStore</code> created, the
	 * password will be used when persist keystore to protects its integrity.<br/>
	 * <br/>
	 * If a password is not given for integrity, then integrity checking is not
	 * performed.
	 * 
	 * @param filename a pathname string of the keystore file
	 * @param password the password used to check the integrity of the keystore, the
	 *                 password used to unlock the keystore, or <code>null</code>
	 * @return a new UBERKeyStore
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
	 */
	public static final UberKeyStore create(final String filename, final Password password) throws KeyStoreLtException {
		try {
			return newUberKeyStore(true, filename, password);
		} catch (final IOException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 * Constructs a new object with a new <code>KeyStore</code> loaded from the
	 * given <code>filename</code> argument.<br/>
	 * <br/>
	 * A password may be given to unlock the keystore (e.g. the keystore resides on
	 * a hardware token device), or to check the integrity of the keystore data. If
	 * a password is not given for integrity checking, then integrity checking is
	 * not performed.
	 *
	 * @param filename a pathname string of the keystore file
	 * @param password the password used to check the integrity of the keystore, the
	 *                 password used to unlock the keystore, or <code>null</code>
	 * @return a new UBERKeyStore
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
	public static final UberKeyStore load(final String filename, final Password password)
			throws KeyStoreLtException, IOException {
		return newUberKeyStore(false, filename, password);
	}

	/**
	 *
	 * @param create   if true creates an empty keystore
	 * @param filename a pathname string of the keystore file
	 * @param password the password used to check the integrity of the keystore, the
	 *                 password used to unlock the keystore, or <code>null</code>
	 * @return a new UBERKeyStore
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
	private static final UberKeyStore newUberKeyStore(final boolean create, final String filename,
			final Password password) throws KeyStoreLtException, IOException {
		final InputStream is = ((create) ? null : FileUtil.openFileBinaryInputStream(filename));
		try {
			return new UberKeyStore(is, filename, password);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	private final String filename;

	/**
	 *
	 * @param is       if null creates an empty keystore
	 * @param filename a pathname string of the keystore file
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
	private UberKeyStore(final InputStream is, final String filename, final Password password)
			throws KeyStoreLtException, IOException {
		super(is, password);
		this.filename = filename;
	}

	@Override
	public final void persist() throws KeyStoreLtException, IOException {
		OutputStream os = null;
		try {
			os = FileUtil.openFileBinaryOutputStream(this.filename, false);
			super.store(os);
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

	@Override
	protected byte[] getKeyStoredData() throws IOException {
		return FileUtil.readAllAtOnce(this.filename);
	}

}

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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.crypto.SecretKey;

import leitej.crypto.Cryptography;
import leitej.crypto.ProviderEnum;
import leitej.crypto.exception.KeyStoreLtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
abstract class AbstractKeyStore {

	// The KeyStore API supports the persisting of three types of entries:
	// -Private keys.
	// --Private keys can be saved with their associated certificate chains.
	// --In most cases they can also be individually password protected.
	// -Symmetric keys.
	// --Although the API now supports this explicitly some KeyStore types do not.
	// --Where the saving of symmetric keys does work they can be individually
	// password protected.
	// -Trusted certificates.
	// --These are the certificates used to create TrustAnchor objects when you need
	// them.
	// --Ordinarily you will have obtained them from a third party and verified
	// their authenticity
	// --through channels other than those you use for validating certificates that
	// exist within certificate paths.

	protected transient volatile KeyStore keyStore;

	/**
	 * Constructs a new object with a new <code>KeyStore</code> loaded from the
	 * given input stream.<br/>
	 * <br/>
	 * A password may be given to unlock the keystore (e.g. the keystore resides on
	 * a hardware token device), or to check the integrity of the keystore data. If
	 * a password is not given for integrity checking, then integrity checking is
	 * not performed.<br/>
	 * <br/>
	 * In order to create an empty keystore, or if the keystore cannot be
	 * initialized from a stream, pass <code>null</code> as the <code>stream</code>
	 * argument.
	 * 
	 * @param stream   the input stream from which the keystore is loaded, or
	 *                 <code>null</code>
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
	protected AbstractKeyStore(final KeyStoreEnum keyStoreEnum, final ProviderEnum providerEnum,
			final InputStream stream, final Password password) throws KeyStoreLtException, IOException {
		this.keyStore = Cryptography.getKeyStore(keyStoreEnum, providerEnum);
		// This line is important; if it is missing, you will get a KeyStoreException
		// when the entries are added.
		load(stream, password);
	}

	/**
	 * Stores the keystore, and protects its integrity with the given password
	 *
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause NoSuchAlgorithmException if the
	 *                             appropriate data integrity algorithm could not be
	 *                             found <br/>
	 *                             +Cause CertificateException if any of the
	 *                             certificates included in the keystore data could
	 *                             not be stored
	 * @throws IOException         If there was an I/O problem with data
	 */
	public abstract void persist() throws KeyStoreLtException, IOException;

	/**
	 * Changes the password used to protects the integrity of keystore.
	 *
	 * @param oldPassword
	 * @param newPassword
	 * @return true when successful change
	 */
	protected abstract boolean changePassword(Password oldPassword, Password newPassword)
			throws KeyStoreLtException, IOException;

	/**
	 * Returns the private key associated with the given alias.
	 * 
	 * @param alias the alias name
	 * @return the requested key, or null if the given alias does not exist or does
	 *         not identify a key-related entry
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause UnrecoverableKeyException if the key
	 *                             cannot be recovered (e.g., the given password is
	 *                             wrong) <br/>
	 *                             +Cause NoSuchAlgorithmException if the algorithm
	 *                             for recovering the key cannot be found
	 */
	protected abstract PrivateKey getPrivateKey(String alias) throws KeyStoreLtException;

	/**
	 * Returns the secret key associated with the given alias.
	 * 
	 * @param alias the alias name
	 * @return the requested key, or null if the given alias does not exist or does
	 *         not identify a key-related entry
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause UnrecoverableKeyException if the key
	 *                             cannot be recovered (e.g., the given password is
	 *                             wrong) <br/>
	 *                             +Cause NoSuchAlgorithmException if the algorithm
	 *                             for recovering the key cannot be found
	 */
	protected abstract SecretKey getSecretKey(String alias) throws KeyStoreLtException;

	/**
	 * Assigns the given private key to the given alias.<br/>
	 * <br/>
	 * It must be accompanied by a certificate chain certifying the corresponding
	 * public key.<br/>
	 * <br/>
	 * If the given alias already exists, the keystore information associated with
	 * it is overridden.
	 *
	 * @param alias      the alias name
	 * @param privateKey the private key to be associated with the alias
	 * @param chain      the certificate chain for the corresponding public key
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause KeyStoreException if the given key cannot
	 *                             be protected, or this operation fails for some
	 *                             other reason
	 */
	protected abstract void setPrivateKeyEntry(String alias, PrivateKey privateKey, X509Certificate[] chain)
			throws KeyStoreLtException;

	/**
	 * Assigns the given secret key to the given alias.<br/>
	 * <br/>
	 * <br/>
	 * If the given alias already exists, the keystore information associated with
	 * it is overridden.
	 *
	 * @param alias     the alias name
	 * @param secretKey the secret key to be associated with the alias
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause KeyStoreException if the given key cannot
	 *                             be protected, or this operation fails for some
	 *                             other reason
	 */
	protected abstract void setSecretKeyEntry(String alias, SecretKey secretKey) throws KeyStoreLtException;

	/**
	 * Loads this KeyStore from the given input stream.
	 *
	 * <p>
	 * A password may be given to unlock the keystore (e.g. the keystore resides on
	 * a hardware token device), or to check the integrity of the keystore data. If
	 * a password is not given for integrity checking, then integrity checking is
	 * not performed.
	 *
	 * <p>
	 * In order to create an empty keystore, or if the keystore cannot be
	 * initialized from a stream, pass <code>null</code> as the <code>stream</code>
	 * argument.
	 *
	 * <p>
	 * Note that if this keystore has already been loaded, it is reinitialized and
	 * loaded again from the given input stream.
	 *
	 * @param stream   the input stream from which the keystore is loaded, or
	 *                 <code>null</code>
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
	 *                             +Cause NoSuchAlgorithmException if the algorithm
	 *                             used to check the integrity of the keystore
	 *                             cannot be found <br/>
	 *                             +Cause CertificateException if any of the
	 *                             certificates in the keystore could not be loaded
	 */
	protected final void load(final InputStream stream, final Password password)
			throws KeyStoreLtException, IOException {
		try {
			this.keyStore.load(stream, ((password == null || stream == null) ? null : password.getPassword()));
		} catch (final IllegalStateLtRtException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchAlgorithmException e) {
			throw new KeyStoreLtException(e);
		} catch (final CertificateException e) {
			throw new KeyStoreLtException(e);
		} finally {
			if (password != null && stream != null) {
				password.erasePassword();
			}
		}
	}

	/**
	 * Stores this keystore to the given output stream, and protects its integrity
	 * with the given password.
	 *
	 * @param stream   the output stream to which this keystore is written
	 * @param password the password to generate the keystore integrity check
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause NoSuchAlgorithmException if the
	 *                             appropriate data integrity algorithm could not be
	 *                             found <br/>
	 *                             +Cause CertificateException if any of the
	 *                             certificates included in the keystore data could
	 *                             not be stored
	 * @throws IOException         If there was an I/O problem with data
	 */
	protected final void store(final OutputStream stream, final Password password)
			throws KeyStoreLtException, IOException {
		try {
			synchronized (this.keyStore) {
				this.keyStore.store(stream, ((password == null) ? null : password.getPassword()));
			}
		} catch (final KeyStoreException e) {
			throw new ImplementationLtRtException(e);
		} catch (final IllegalStateLtRtException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchAlgorithmException e) {
			throw new KeyStoreLtException(e);
		} catch (final CertificateException e) {
			throw new KeyStoreLtException(e);
		} finally {
			if (password != null) {
				password.erasePassword();
			}
		}
	}

	/**
	 * Lists all the alias names of this keystore.
	 *
	 * @return enumeration of the alias names
	 */
	protected final Enumeration<String> aliases() {
		try {
			synchronized (this.keyStore) {
				return this.keyStore.aliases();
			}
		} catch (final KeyStoreException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 * Checks if the given alias exists in this keystore.
	 *
	 * @param alias the alias name
	 * @return true if the alias exists, false otherwise
	 */
	public final boolean containsAlias(final String alias) {
		try {
			synchronized (this.keyStore) {
				return this.keyStore.containsAlias(alias);
			}
		} catch (final KeyStoreException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 * Deletes the entry identified by the given alias from this keystore.
	 *
	 * @param alias the alias name
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause KeyStoreException If the entry cannot be
	 *                             removed
	 */
	protected void deleteEntry(final String alias) throws KeyStoreLtException {
		try {
			synchronized (this.keyStore) {
				this.keyStore.deleteEntry(alias);
			}
		} catch (final KeyStoreException e) {
			throw new KeyStoreLtException(e);
		}
	}

	/**
	 * Deletes all entries from this keystore.
	 *
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause KeyStoreException If the entry cannot be
	 *                             removed
	 */
	protected void deleteAllEntries() throws KeyStoreLtException {
		try {
			synchronized (this.keyStore) {
				final Enumeration<String> aliases = aliases();
				while (aliases.hasMoreElements()) {
					this.keyStore.deleteEntry(aliases.nextElement());
				}
			}
		} catch (final KeyStoreException e) {
			throw new KeyStoreLtException(e);
		}
	}

	/**
	 * Returns the first element of the key certificate chain associated with the
	 * given alias.
	 * 
	 * @param alias the alias name
	 * @return the certificate, or null if the given alias does not exist or does
	 *         not contain a certificate
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause ClassCastException if the keystore is
	 *                             holding a non X509Certificate
	 */
	public final X509Certificate getKeyCertificate(final String alias) throws KeyStoreLtException {
		try {
			synchronized (this.keyStore) {
				if (!isKeyEntry(alias)) {
					return null;
				}
				try {
					return X509Certificate.class.cast(this.keyStore.getCertificate(alias));
				} catch (final ClassCastException e) {
					throw new KeyStoreLtException(e);
				}
			}
		} catch (final KeyStoreException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 * Returns the trusted certificate associated with the given alias.
	 * 
	 * @param alias the alias name
	 * @return the certificate, or null if the given alias does not exist or does
	 *         not contain a certificate
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause ClassCastException if the keystore is
	 *                             holding a non X509Certificate
	 */
	public final X509Certificate getTrustedCertificate(final String alias) throws KeyStoreLtException {
		try {
			synchronized (this.keyStore) {
				if (!isTrustedCertificateEntry(alias)) {
					return null;
				}
				try {
					return X509Certificate.class.cast(this.keyStore.getCertificate(alias));
				} catch (final ClassCastException e) {
					throw new KeyStoreLtException(e);
				}
			}
		} catch (final KeyStoreException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 * Returns the (alias) name of the first keystore entry whose certificate
	 * matches the given certificate.<br/>
	 * <br/>
	 * This method attempts to match the given certificate with each keystore entry.
	 * If the entry being considered was created by a call to
	 * <code>setTrustedCertificateEntry</code>, then the given certificate is
	 * compared to that entry's certificate.<br/>
	 * <br/>
	 * If the entry being considered was created by a call to
	 * <code>setPrivateKeyEntry</code>, then the given certificate is compared to
	 * the first element of that entry's certificate chain.
	 * 
	 * @param cert the certificate to match with.
	 * @return the alias name of the first entry with a matching certificate, or
	 *         null if no such entry exists in this keystore
	 */
	protected final String getCertificateAlias(final X509Certificate cert) {
		try {
			synchronized (this.keyStore) {
				return this.keyStore.getCertificateAlias(cert);
			}
		} catch (final KeyStoreException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 * Returns the certificate chain associated with the given alias. The
	 * certificate chain must have been associated with the alias by a call to
	 * <code>setPrivateKeyEntry</code>.
	 *
	 * @param alias the alias name
	 * @return the certificate chain (ordered with the user's certificate first and
	 *         the root certificate authority last), or null if the given alias does
	 *         not exist or does not contain a certificate chain
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause ClassCastException if the keystore is
	 *                             holding a non X509Certificate
	 */
	public final X509Certificate[] getKeyCertificateChain(final String alias) throws KeyStoreLtException {
		try {
			synchronized (this.keyStore) {
				final Certificate[] tmp = this.keyStore.getCertificateChain(alias);
				if (tmp == null) {
					return null;
				}
				final X509Certificate[] result = new X509Certificate[tmp.length];
				try {
					for (int i = 0; i < result.length; i++) {
						result[i] = X509Certificate.class.cast(tmp[i]);
					}
				} catch (final ClassCastException e) {
					throw new KeyStoreLtException(e);
				}
				return result;
			}
		} catch (final KeyStoreException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 * Returns the key associated with the given alias, using the given password to
	 * recover it. The key must have been associated with the alias by a call to
	 * <code>setPrivateKeyEntry</code>.
	 * 
	 * @param alias    the alias name
	 * @param password the password for recovering the key
	 * @return the requested key, or null if the given alias does not exist or does
	 *         not identify a key-related entry
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause UnrecoverableKeyException if the key
	 *                             cannot be recovered (e.g., the given password is
	 *                             wrong) <br/>
	 *                             +Cause NoSuchAlgorithmException if the algorithm
	 *                             for recovering the key cannot be found
	 */
	protected final PrivateKey getPrivateKey(final String alias, final Password password) throws KeyStoreLtException {
		try {
			synchronized (this.keyStore) {
				return PrivateKey.class
						.cast(this.keyStore.getKey(alias, ((password == null) ? null : password.getPassword())));
			}
		} catch (final ClassCastException e) {
			return null;
		} catch (final KeyStoreException e) {
			throw new ImplementationLtRtException(e);
		} catch (final UnrecoverableKeyException e) {
			throw new KeyStoreLtException(e);
		} catch (final IllegalStateLtRtException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchAlgorithmException e) {
			throw new KeyStoreLtException(e);
		} finally {
			if (password != null) {
				password.erasePassword();
			}
		}
	}

	/**
	 * Returns the key associated with the given alias, using the given password to
	 * recover it. The key must have been associated with the alias by a call to
	 * <code>setSecretKeyEntry</code>.
	 * 
	 * @param alias    the alias name
	 * @param password the password for recovering the key
	 * @return the requested key, or null if the given alias does not exist or does
	 *         not identify a key-related entry
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause UnrecoverableKeyException if the key
	 *                             cannot be recovered (e.g., the given password is
	 *                             wrong) <br/>
	 *                             +Cause NoSuchAlgorithmException if the algorithm
	 *                             for recovering the key cannot be found
	 */
	protected final SecretKey getSecretKey(final String alias, final Password password) throws KeyStoreLtException {
		try {
			synchronized (this.keyStore) {
				return SecretKey.class
						.cast(this.keyStore.getKey(alias, ((password == null) ? null : password.getPassword())));
			}
		} catch (final ClassCastException e) {
			return null;
		} catch (final KeyStoreException e) {
			throw new ImplementationLtRtException(e);
		} catch (final UnrecoverableKeyException e) {
			throw new KeyStoreLtException(e);
		} catch (final IllegalStateLtRtException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchAlgorithmException e) {
			throw new KeyStoreLtException(e);
		} finally {
			if (password != null) {
				password.erasePassword();
			}
		}
	}

	/**
	 * Returns true if the entry identified by the given alias was created by a call
	 * to <code>setTrustedCertificateEntry</code>.
	 *
	 * @param alias the alias for the keystore entry to be checked
	 * @return true if the entry identified by the given alias contains a trusted
	 *         certificate, false otherwise
	 */
	public final boolean isTrustedCertificateEntry(final String alias) {
		try {
			synchronized (this.keyStore) {
				return this.keyStore.isCertificateEntry(alias);
			}
		} catch (final KeyStoreException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 * Returns true if the entry identified by the given alias was created by a call
	 * to <code>setPrivateKeyEntry</code>, or created by a call to
	 * <code>setSecretKeyEntry</code>.
	 *
	 * @param alias the alias for the keystore entry to be checked
	 * @return true if the entry identified by the given alias is a key-related
	 *         entry, false otherwise
	 */
	public final boolean isKeyEntry(final String alias) {
		try {
			synchronized (this.keyStore) {
				return this.keyStore.isKeyEntry(alias);
			}
		} catch (final KeyStoreException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 * Assigns the given trusted certificate to the given alias. <br/>
	 * <br/>
	 * If the given alias identifies an existing entry created by a call to
	 * <code>setTrustedCertificateEntry</code>, the trusted certificate in the
	 * existing entry is overridden by the given certificate.
	 *
	 * @param alias the alias name
	 * @param cert  the certificate
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause KeyStoreException if the given alias
	 *                             already exists and does not identify an entry
	 *                             containing a trusted certificate, or this
	 *                             operation fails for some other reason
	 */
	public final void setTrustedCertificateEntry(final String alias, final X509Certificate cert)
			throws KeyStoreLtException {
		try {
			synchronized (this.keyStore) {
				this.keyStore.setCertificateEntry(alias, cert);
			}
		} catch (final KeyStoreException e) {
			throw new KeyStoreLtException(e);
		}
	}

	/**
	 * Assigns the given private key to the given alias, protecting it with the
	 * given password.<br/>
	 * <br/>
	 * It must be accompanied by a certificate chain certifying the corresponding
	 * public key.<br/>
	 * <br/>
	 * If the given alias already exists, the keystore information associated with
	 * it is overridden.
	 *
	 * @param alias      the alias name
	 * @param privateKey the private key to be associated with the alias
	 * @param password   the password to protect the key
	 * @param chain      the certificate chain for the corresponding public key
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause KeyStoreException if the given key cannot
	 *                             be protected, or this operation fails for some
	 *                             other reason
	 */
	protected final void setPrivateKeyEntry(final String alias, final PrivateKey privateKey, final Password password,
			final X509Certificate[] chain) throws KeyStoreLtException {
		try {
			synchronized (this.keyStore) {
				this.keyStore.setKeyEntry(alias, privateKey, ((password == null) ? null : password.getPassword()),
						chain);
			}
		} catch (final KeyStoreException e) {
			throw new KeyStoreLtException(e);
		} finally {
			if (password != null) {
				password.erasePassword();
			}
		}
	}

	/**
	 * Assigns the given secret key to the given alias, protecting it with the given
	 * password.<br/>
	 * <br/>
	 * If the given alias already exists, the keystore information associated with
	 * it is overridden.
	 *
	 * @param alias     the alias name
	 * @param secretKey the secret key to be associated with the alias
	 * @param password  the password to protect the key
	 * @throws KeyStoreLtException <br/>
	 *                             +Cause KeyStoreException if the given key cannot
	 *                             be protected, or this operation fails for some
	 *                             other reason
	 */
	protected final void setSecretKeyEntry(final String alias, final SecretKey secretKey, final Password password)
			throws KeyStoreLtException {
		try {
			synchronized (this.keyStore) {
				this.keyStore.setKeyEntry(alias, secretKey, ((password == null) ? null : password.getPassword()), null);
			}
		} catch (final KeyStoreException e) {
			throw new KeyStoreLtException(e);
		} finally {
			if (password != null) {
				password.erasePassword();
			}
		}
	}

}

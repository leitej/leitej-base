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

package leitej.crypto.vault;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import leitej.crypto.Cryptography;
import leitej.crypto.asymmetric.certificate.CertificateStreamUtil;
import leitej.crypto.asymmetric.certificate.CertificateUtil;
import leitej.crypto.keyStore.Password;
import leitej.crypto.keyStore.UberKeyLtmStore;
import leitej.exception.CertificateLtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.KeyStoreLtException;
import leitej.exception.LtmLtRtException;
import leitej.ltm.LongTermMemory;
import leitej.ltm.LtmFilter;
import leitej.ltm.LtmFilter.OPERATOR;
import leitej.ltm.LtmFilter.OPERATOR_JOIN;
import leitej.util.data.Cache;
import leitej.util.data.CacheSoft;
import leitej.util.data.CacheWeak;

/**
 *
 *
 * @author Julio Leite
 */
public final class LtVault {

	// hold: IV Public certificate chains
	// (for the certificates chains, the same alias is correspondent to the exact
	// same certificate)

	private static final String VAULT_INTERNAL_IV_SECRET_KEY_ENTRY_ALIAS = "__VAULT_INTERNAL_IV_SECRET_KEY_ENTRY_ALIAS__";
	private static final String VAULT_INTERNAL_CERTIFICATE_SECRET_KEY_ENTRY_ALIAS = "__VAULT_INTERNAL_CERTIFICATE_SECRET_KEY_ENTRY_ALIAS__";
	private static final String VAULT_INTERNAL_CERTIFICATE_SECRET_IV_ENTRY_ALIAS = "__VAULT_INTERNAL_CERTIFICATE_SECRET_IV_ENTRY_ALIAS__";

	private static final LongTermMemory LTM = LongTermMemory.getInstance();
	private static final Map<String, LtVault> INSTANCE_MAP = new HashMap<>();

	/**
	 *
	 * @param alias the alias name
	 * @return if the vault exists
	 * @throws IOException if there is an I/O or format problem with the keystore
	 *                     data
	 */
	public static final boolean exists(final String alias) throws IOException {
		return UberKeyLtmStore.exists(alias);
	}

	/**
	 * Constructs a new object with a new <code>LtVault</code> created, the password
	 * will be used to protects its integrity.<br/>
	 * <br/>
	 * If a password is not given for integrity, then integrity checking is not
	 * performed.
	 *
	 * @param alias    the alias name
	 * @param password the password used to check the integrity of the keystore, the
	 *                 password used to unlock the keystore, or <code>null</code>
	 * @return a new LtVault
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
	public static final LtVault create(final String alias, final Password password)
			throws KeyStoreLtException, IOException {
		return newLtVault(true, alias, password);
	}

	/**
	 * Constructs a new object with a new <code>LtVault</code> loaded.<br/>
	 * <br/>
	 * A password may be given to unlock the keystore (e.g. the keystore resides on
	 * a hardware token device), or to check the integrity of the keystore data. If
	 * a password is not given for integrity checking, then integrity checking is
	 * not performed.
	 *
	 * @param alias    the alias name
	 * @param password the password used to check the integrity of the keystore, the
	 *                 password used to unlock the keystore, or <code>null</code>
	 * @return a new LtVault
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
	public static final LtVault load(final String alias, final Password password)
			throws KeyStoreLtException, IOException {
		return newLtVault(false, alias, password);
	}

	private static final LtVault newLtVault(final boolean create, final String alias, final Password password)
			throws KeyStoreLtException, IOException {
		synchronized (INSTANCE_MAP) {
			LtVault result = INSTANCE_MAP.get(alias);
			if (result == null) {
				if (create) {
					result = new LtVault(alias, UberKeyLtmStore.create(alias, password));
					result.deleteAllEntries();
				} else {
					result = new LtVault(alias, UberKeyLtmStore.load(alias, password));
				}
			} else {
				if (create) {
					result.deleteAllEntries();
				}
			}
			return result;
		}
	}

	private final Cache<String, VaultSecretIV> vaultSecretIVLightCache;
	private final Cache<String, IvParameterSpec> secretIVCache;

	private final Map<String, VaultCertificate> vaultCertificateIssuerMap;
	private final Cache<String, VaultCertificate> vaultCertificateLightCache;
	private final Cache<String, X509Certificate> certificateCache;

	private final String keyLtmStoreAlias;
	private final UberKeyLtmStore keyLtmStore;

	private LtVault(final String keyLtmStoreAlias, final UberKeyLtmStore keyLtmStore)
			throws KeyStoreLtException, IOException {
		this.keyLtmStoreAlias = keyLtmStoreAlias;
		this.keyLtmStore = keyLtmStore;
		this.vaultSecretIVLightCache = new CacheWeak<>();
		this.secretIVCache = new CacheSoft<>();
		this.vaultCertificateIssuerMap = new HashMap<>();
		this.vaultCertificateLightCache = new CacheWeak<>();
		this.certificateCache = new CacheSoft<>();
		// assert vault has the internal secret key for iv_s
		if (this.keyLtmStore.getSecretKey(VAULT_INTERNAL_IV_SECRET_KEY_ENTRY_ALIAS) == null) {
			try {
				this.keyLtmStore.setSecretKeyEntry(VAULT_INTERNAL_IV_SECRET_KEY_ENTRY_ALIAS, Cryptography.keyGenerate(
						Cryptography.getDefaultSymmetricCipher(), Cryptography.getDefaultSymmetricKeyBitSize()));
			} catch (final NoSuchAlgorithmException e) {
				throw new KeyStoreLtException(e);
			} catch (final NoSuchProviderException e) {
				throw new KeyStoreLtException(e);
			}
			this.keyLtmStore.persist();
		}
		// assert vault has the internal secret key for certificates
		if (this.keyLtmStore.getSecretKey(VAULT_INTERNAL_CERTIFICATE_SECRET_KEY_ENTRY_ALIAS) == null) {
			try {
				this.keyLtmStore.setSecretKeyEntry(VAULT_INTERNAL_CERTIFICATE_SECRET_KEY_ENTRY_ALIAS,
						Cryptography.keyGenerate(Cryptography.getDefaultSymmetricCipher(),
								Cryptography.getDefaultSymmetricKeyBitSize()));
			} catch (final NoSuchAlgorithmException e) {
				throw new KeyStoreLtException(e);
			} catch (final NoSuchProviderException e) {
				throw new KeyStoreLtException(e);
			}
			this.keyLtmStore.persist();
		}
		// assert vault has the internal secret iv for certificates
		final LtmFilter<VaultSecretIV> filter = new LtmFilter<>(VaultSecretIV.class, OPERATOR_JOIN.AND);
		filter.prepare(OPERATOR.EQUAL).setKeyLtmStoreAlias(this.keyLtmStoreAlias);
		filter.prepare(OPERATOR.EQUAL).setAlias(VAULT_INTERNAL_CERTIFICATE_SECRET_IV_ENTRY_ALIAS);
		final Iterator<VaultSecretIV> found = LTM.search(filter);
		if (!found.hasNext()) {
			setSecretIV(VAULT_INTERNAL_CERTIFICATE_SECRET_IV_ENTRY_ALIAS,
					Cryptography.ivGenerate(Cryptography.getDefaultSymmetricIvBitSize()));
		}
	}

	/*
	 * iv
	 */

	public final IvParameterSpec getSecretIV(final String alias) throws LtmLtRtException {
		synchronized (this.secretIVCache) {
			IvParameterSpec result = this.secretIVCache.get(alias);
			if (result == null) {
				final VaultSecretIV vaultSecretIV = fetchSecretIv(alias);
				if (vaultSecretIV != null) {
					// TODO: desencriptar iv
					result = Cryptography.ivProduce(vaultSecretIV.getIv());
					this.secretIVCache.set(alias, result);
				}
			}
			return result;
		}
	}

	private final VaultSecretIV fetchSecretIv(final String alias) throws LtmLtRtException {
		VaultSecretIV result = this.vaultSecretIVLightCache.get(alias);
		if (result == null) {
			final LtmFilter<VaultSecretIV> filter = new LtmFilter<>(VaultSecretIV.class, OPERATOR_JOIN.AND);
			filter.prepare(OPERATOR.EQUAL).setKeyLtmStoreAlias(this.keyLtmStoreAlias);
			filter.prepare(OPERATOR.EQUAL).setAlias(alias);
			final Iterator<VaultSecretIV> found = LTM.search(filter);
			if (found.hasNext()) {
				result = found.next();
				this.vaultSecretIVLightCache.set(alias, result);
			}
		}
		return result;
	}

	public final void setSecretIV(final String alias, final IvParameterSpec iv) throws LtmLtRtException {
		synchronized (this.secretIVCache) {
			VaultSecretIV vaultSecretIV = fetchSecretIv(alias);
			if (vaultSecretIV == null) {
				vaultSecretIV = LTM.newRecord(VaultSecretIV.class);
				vaultSecretIV.setKeyLtmStoreAlias(this.keyLtmStoreAlias);
				vaultSecretIV.setAlias(alias);
			}
			// TODO: encriptar iv
			vaultSecretIV.setIv(iv.getIV());
			this.secretIVCache.set(alias, iv);
		}
	}

	/*
	 * certificate
	 */

	public final X509Certificate getCertificate(final String alias) throws LtmLtRtException, IOException {
		synchronized (this.certificateCache) {
			X509Certificate result = this.certificateCache.get(alias);
			if (result == null) {
				final VaultCertificate vaultCertificate = fetchCertificate(alias);
				if (vaultCertificate != null) {
					try {
						// TODO: desencriptar certificate
						try {
							result = CertificateStreamUtil
									.readX509Certificate(vaultCertificate.getCertificate().newInputStream());
						} catch (final CertificateLtException e) {
							throw new IOException(e);
						}
						this.certificateCache.set(alias, result);
					} finally {
						vaultCertificate.getCertificate().close();
					}
				}
			}
			return result;
		}
	}

	public final X509Certificate[] getCertificateChain(final String alias) throws LtmLtRtException, IOException {
		final List<X509Certificate> result = new ArrayList<>();
		synchronized (this.certificateCache) {
			result.add(getCertificate(alias));
			VaultCertificate issuer;
			String aliasForIssuer = alias;
			do {
				issuer = this.vaultCertificateIssuerMap.get(aliasForIssuer);
				if (issuer == null) {
					issuer = fetchCertificate(aliasForIssuer).getIssuer();
					this.vaultCertificateIssuerMap.put(aliasForIssuer, issuer);
				}
				if (issuer != null) {
					result.add(getCertificate(issuer.getAlias()));
					aliasForIssuer = issuer.getAlias();
				}
			} while (issuer != null);
		}
		return result.toArray(new X509Certificate[result.size()]);
	}

	private final VaultCertificate fetchCertificate(final String alias) throws LtmLtRtException {
		VaultCertificate result = this.vaultCertificateLightCache.get(alias);
		if (result == null) {
			final LtmFilter<VaultCertificate> filter = new LtmFilter<>(VaultCertificate.class, OPERATOR_JOIN.AND);
			filter.prepare(OPERATOR.EQUAL).setKeyLtmStoreAlias(this.keyLtmStoreAlias);
			filter.prepare(OPERATOR.EQUAL).setAlias(alias);
			final Iterator<VaultCertificate> found = LTM.search(filter);
			if (found.hasNext()) {
				result = found.next();
				this.vaultCertificateLightCache.set(alias, result);
			}
		}
		return result;
	}

	public final void setCertificateChain(final String alias, final X509Certificate[] chain)
			throws LtmLtRtException, IOException, CertificateEncodingException {
		synchronized (this.certificateCache) {
			VaultCertificate issuer = null;
			String aliasIssuer;
			for (int i = chain.length - 1; i > 0; i--) {
				aliasIssuer = CertificateUtil.getAliasFrom(chain[i]);
				this.vaultCertificateIssuerMap.put(aliasIssuer, issuer);
				issuer = setCertificate(aliasIssuer, chain[i], issuer);
			}
			this.vaultCertificateIssuerMap.put(alias, issuer);
			setCertificate(alias, chain[0], issuer);
		}
	}

	private final VaultCertificate setCertificate(final String alias, final X509Certificate certificate,
			final VaultCertificate issuer) throws LtmLtRtException, IOException, CertificateEncodingException {
		VaultCertificate vaultCertificate = fetchCertificate(alias);
		if (vaultCertificate == null) {
			vaultCertificate = LTM.newRecord(VaultCertificate.class);
			vaultCertificate.setKeyLtmStoreAlias(this.keyLtmStoreAlias);
			vaultCertificate.setAlias(alias);
			vaultCertificate.setIssuer(issuer);
			// TODO: encriptar certificate
			try {
				CertificateStreamUtil.writeX509CertificatesPEM(
						new PrintWriter(vaultCertificate.getCertificate().newOutputStream()), certificate);
			} finally {
				vaultCertificate.getCertificate().close();
			}
			this.certificateCache.set(alias, certificate);
		}
		return vaultCertificate;
	}

	/*
	 * keystore
	 */

	public final void persist() throws KeyStoreLtException, IOException {
		this.keyLtmStore.persist();
	}

	public final PrivateKey getPrivateKey(final String alias) throws KeyStoreLtException {
		return this.keyLtmStore.getPrivateKey(alias);
	}

	public final SecretKey getSecretKey(final String alias) throws KeyStoreLtException {
		return this.keyLtmStore.getSecretKey(alias);
	}

	public final void setPrivateKeyEntry(final String alias, final PrivateKey key, final X509Certificate[] chain)
			throws KeyStoreLtException {
		this.keyLtmStore.setPrivateKeyEntry(alias, key, chain);
	}

	public final void setSecretKeyEntry(final String alias, final SecretKey secretKey) throws KeyStoreLtException {
		this.keyLtmStore.setSecretKeyEntry(alias, secretKey);
	}

	public final void deleteEntry(final String alias) throws KeyStoreLtException {
		// TODO:
		throw new ImplementationLtRtException();
	}

	private final void deleteAllEntries() {
		// TODO:
		// throw new ImplementationLtRtException();
	}

	public final boolean containsAlias(final String alias) throws LtmLtRtException, IOException {
		boolean result = this.keyLtmStore.containsAlias(alias);
		// TODO: optimise - query to find any - and size() equal to one
		if (!result) {
			result = (getSecretIV(alias) != null);
		}
		if (!result) {
			result = (getCertificate(alias) != null);
		}
		return result;
	}

	public final X509Certificate getKeyCertificate(final String alias) throws KeyStoreLtException {
		return this.keyLtmStore.getKeyCertificate(alias);
	}

	public final X509Certificate getTrustedCertificate(final String alias) throws KeyStoreLtException {
		return this.keyLtmStore.getTrustedCertificate(alias);
	}

	public final X509Certificate[] getKeyCertificateChain(final String alias) throws KeyStoreLtException {
		return this.keyLtmStore.getKeyCertificateChain(alias);
	}

	public final void setTrustedCertificateEntry(final String alias, final X509Certificate cert)
			throws KeyStoreLtException {
		this.keyLtmStore.setTrustedCertificateEntry(alias, cert);
	}

}

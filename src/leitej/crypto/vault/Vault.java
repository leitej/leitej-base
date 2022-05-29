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
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.cert.X509CertificateHolder;

import leitej.crypto.Cryptography;
import leitej.crypto.asymmetric.certificate.CertificateUtil;
import leitej.crypto.keyStore.LtmKeyStore;
import leitej.crypto.keyStore.Password;
import leitej.exception.CertificateLtException;
import leitej.exception.KeyStoreLtException;
import leitej.exception.LtmLtRtException;
import leitej.ltm.LongTermMemory;
import leitej.ltm.LtmFilter;
import leitej.ltm.LtmFilter.OPERATOR;
import leitej.ltm.LtmFilter.OPERATOR_JOIN;
import leitej.util.data.Cache;
import leitej.util.data.CacheSoft;

/**
 *
 *
 * @author Julio Leite
 */
public final class Vault {

	// hold: IV Public certificate chains
	// (for the certificates chains, the same alias is correspondent to the exact
	// same certificate)

	private static final LongTermMemory LTM = LongTermMemory.getInstance();
	private static final Map<String, Vault> INSTANCE_MAP = new HashMap<>();

	/**
	 *
	 * @param alias the alias name
	 * @return if the vault exists
	 * @throws IOException if there is an I/O or format problem with the keystore
	 *                     data
	 */
	public static final boolean exists(final String alias) throws IOException {
		return LtmKeyStore.exists(alias);
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
	public static final Vault create(final String alias, final Password password)
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
	public static final Vault load(final String alias, final Password password) throws KeyStoreLtException, IOException {
		return newLtVault(false, alias, password);
	}

	private static final Vault newLtVault(final boolean create, final String alias, final Password password)
			throws KeyStoreLtException, IOException {
		synchronized (INSTANCE_MAP) {
			Vault result = INSTANCE_MAP.get(alias);
			if (result == null) {
				if (create) {
					result = new Vault(alias, LtmKeyStore.create(alias, password));
					result.clear();
				} else {
					result = new Vault(alias, LtmKeyStore.load(alias, password));
				}
			} else {
				if (create) {
					result.clear();
				}
			}
			return result;
		}
	}

	private final Cache<String, IvParameterSpec> secretIVCache;

	private final Cache<String, X509CertificateHolder> certificateCache;

	private final String ltmKeyStoreAlias;
	private final LtmKeyStore ltmKeyStore;

	private Vault(final String ltmKeyStoreAlias, final LtmKeyStore ltmKeyStore) throws KeyStoreLtException, IOException {
		this.ltmKeyStoreAlias = ltmKeyStoreAlias;
		this.ltmKeyStore = ltmKeyStore;
		this.secretIVCache = new CacheSoft<>();
		this.certificateCache = new CacheSoft<>();
	}

	/*
	 * iv
	 */

	public final IvParameterSpec getSecretIV(final String alias) throws LtmLtRtException {
		synchronized (this.secretIVCache) {
			IvParameterSpec result = this.secretIVCache.get(alias);
			if (result == null) {
				final VaultSecretIV vaultSecretIV = searchVaultSecretIv(alias);
				if (vaultSecretIV != null) {
					result = Cryptography.ivProduce(Base64.getDecoder().decode(vaultSecretIV.getIv()));
					this.secretIVCache.set(alias, result);
				}
			}
			return result;
		}
	}

	private final VaultSecretIV searchVaultSecretIv(final String alias) throws LtmLtRtException {
		VaultSecretIV result;
		final LtmFilter<VaultSecretIV> filter = new LtmFilter<>(VaultSecretIV.class, OPERATOR_JOIN.AND);
		filter.append(OPERATOR.EQUAL).setLtmKeyStoreAlias(this.ltmKeyStoreAlias);
		filter.append(OPERATOR.EQUAL).setAlias(alias);
		final Iterator<VaultSecretIV> found = LTM.search(filter);
		if (found.hasNext()) {
			result = found.next();
		} else {
			result = null;
		}
		return result;
	}

	public final void setSecretIV(final String alias, final IvParameterSpec iv) throws LtmLtRtException {
		synchronized (this.secretIVCache) {
			VaultSecretIV vaultSecretIV = searchVaultSecretIv(alias);
			if (vaultSecretIV == null) {
				vaultSecretIV = LTM.newRecord(VaultSecretIV.class);
				vaultSecretIV.setLtmKeyStoreAlias(this.ltmKeyStoreAlias);
				vaultSecretIV.setAlias(alias);
			}
			vaultSecretIV.setIv(Base64.getEncoder().encodeToString(iv.getIV()));
			this.secretIVCache.set(alias, iv);
		}
	}

	public final void deleteSecretIV(final String alias) {
		synchronized (this.secretIVCache) {
			final VaultSecretIV vaultSecretIV = searchVaultSecretIv(alias);
			if (vaultSecretIV != null) {
				LTM.forgets(vaultSecretIV);
			}
			this.secretIVCache.remove(alias);
		}
	}

	public final void deleteAllSecretIV() {
		synchronized (this.secretIVCache) {
			final LtmFilter<VaultSecretIV> filter = new LtmFilter<>(VaultSecretIV.class, OPERATOR_JOIN.AND);
			filter.append(OPERATOR.EQUAL).setLtmKeyStoreAlias(this.ltmKeyStoreAlias);
			final Iterator<VaultSecretIV> found = LTM.search(filter);
			while (found.hasNext()) {
				LTM.forgets(found.next());
			}
			this.secretIVCache.clear();
		}
	}

	/*
	 * certificate
	 */

	private final X509CertificateHolder getCertificate(final String alias, final VaultCertificate vaultCertificate)
			throws LtmLtRtException, CertificateLtException {
		X509CertificateHolder result = this.certificateCache.get(alias);
		if (result == null) {
			VaultCertificate vc = vaultCertificate;
			if (vc == null) {
				vc = searchVaultCertificate(alias);
			}
			if (vc != null) {
				try {
					result = new X509CertificateHolder(Base64.getDecoder().decode(vc.getCertificate()));
				} catch (final IOException e) {
					throw new CertificateLtException(e);
				}
				this.certificateCache.set(vc.getAlias(), result);
			}
		}
		return result;
	}

	public final X509CertificateHolder getCertificate(final String alias)
			throws LtmLtRtException, CertificateLtException {
		synchronized (this.certificateCache) {
			return getCertificate(alias, null);
		}
	}

	public final X509CertificateHolder[] getCertificateChain(final String alias)
			throws LtmLtRtException, CertificateLtException {
		final List<X509CertificateHolder> result = new ArrayList<>();
		synchronized (this.certificateCache) {
			VaultCertificate vaultCertificate = searchVaultCertificate(alias);
			while (vaultCertificate != null) {
				result.add(getCertificate(vaultCertificate.getAlias(), vaultCertificate));
				vaultCertificate = vaultCertificate.getIssuer();
			}
		}
		return result.toArray(new X509CertificateHolder[result.size()]);
	}

	private final VaultCertificate searchVaultCertificate(final String alias) throws LtmLtRtException {
		VaultCertificate result;
		final LtmFilter<VaultCertificate> filter = new LtmFilter<>(VaultCertificate.class, OPERATOR_JOIN.AND);
		filter.append(OPERATOR.EQUAL).setLtmKeyStoreAlias(this.ltmKeyStoreAlias);
		filter.append(OPERATOR.EQUAL).setAlias(alias);
		final Iterator<VaultCertificate> found = LTM.search(filter);
		if (found.hasNext()) {
			result = found.next();
		} else {
			result = null;
		}
		return result;
	}

	/**
	 *
	 * @param chain
	 * @return alias
	 * @throws LtmLtRtException
	 * @throws CertificateLtException
	 */
	public final String setCertificateChain(final X509CertificateHolder[] chain)
			throws LtmLtRtException, CertificateLtException {
		synchronized (this.certificateCache) {
			VaultCertificate issuer = null;
			for (int i = chain.length - 1; i >= 0; i--) {
				issuer = setCertificate(chain[i], issuer);
			}
			return issuer.getAlias();
		}
	}

	private final VaultCertificate setCertificate(final X509CertificateHolder certificate, final VaultCertificate issuer)
			throws LtmLtRtException, CertificateLtException {
		String alias;
		try {
			alias = CertificateUtil.getAlias(certificate);
		} catch (final IOException e) {
			throw new CertificateLtException(e);
		}
		VaultCertificate vaultCertificate = searchVaultCertificate(alias);
		if (vaultCertificate == null) {
			String certEncoded;
			try {
				certEncoded = Base64.getEncoder().encodeToString(certificate.getEncoded());
			} catch (final IOException e) {
				throw new CertificateLtException(e);
			}
			vaultCertificate = LTM.newRecord(VaultCertificate.class);
			vaultCertificate.setLtmKeyStoreAlias(this.ltmKeyStoreAlias);
			vaultCertificate.setAlias(alias);
			vaultCertificate.setCertificate(certEncoded);
			vaultCertificate.setIssuer(issuer);
			this.certificateCache.set(alias, certificate);
		}
		return vaultCertificate;
	}

	public final void deleteCertificate(final String alias) {
		synchronized (this.certificateCache) {
			final VaultCertificate vaultCertificate = searchVaultCertificate(alias);
			if (vaultCertificate != null) {
				LTM.forgets(vaultCertificate);
			}
			this.certificateCache.remove(alias);
		}
	}

	public final void deleteAllCertificate() {
		synchronized (this.certificateCache) {
			final LtmFilter<VaultCertificate> filter = new LtmFilter<>(VaultCertificate.class, OPERATOR_JOIN.AND);
			filter.append(OPERATOR.EQUAL).setLtmKeyStoreAlias(this.ltmKeyStoreAlias);
			final Iterator<VaultCertificate> found = LTM.search(filter);
			while (found.hasNext()) {
				LTM.forgets(found.next());
			}
			this.certificateCache.clear();
		}
	}

	/*
	 * keystore
	 */

	public final PrivateKey getPrivateKey(final String alias) throws KeyStoreLtException {
		return this.ltmKeyStore.getPrivateKey(alias);
	}

	public final X509CertificateHolder getKeyCertificate(final String alias)
			throws KeyStoreLtException, CertificateLtException {
		return this.ltmKeyStore.getKeyCertificate(alias);
	}

	public final X509CertificateHolder[] getKeyCertificateChain(final String alias)
			throws KeyStoreLtException, CertificateLtException {
		return this.ltmKeyStore.getKeyCertificateChain(alias);
	}

	public final void setPrivateKeyEntry(final String alias, final PrivateKey key, final X509CertificateHolder[] chain)
			throws KeyStoreLtException, CertificateLtException {
		this.ltmKeyStore.setPrivateKeyEntry(alias, key, chain);
	}

	public final SecretKey getSecretKey(final String alias) throws KeyStoreLtException {
		return this.ltmKeyStore.getSecretKey(alias);
	}

	public final void setSecretKeyEntry(final String alias, final SecretKey secretKey) throws KeyStoreLtException {
		this.ltmKeyStore.setSecretKeyEntry(alias, secretKey);
	}

	public final X509CertificateHolder getTrustedCertificate(final String alias)
			throws KeyStoreLtException, CertificateLtException {
		return this.ltmKeyStore.getTrustedCertificate(alias);
	}

	public final void setTrustedCertificateEntry(final String alias, final X509CertificateHolder cert)
			throws KeyStoreLtException, CertificateLtException {
		this.ltmKeyStore.setTrustedCertificateEntry(alias, cert);
	}

	/**
	 * Only required to be called when changed privateKeys or secretKey or
	 * trustedCertificate, to be persisted.
	 *
	 * @throws KeyStoreLtException
	 * @throws IOException
	 */
	public final void persist() throws KeyStoreLtException, IOException {
		this.ltmKeyStore.persist();
	}

	/**
	 * Deletes privateKey or secretKey or trustedCertificate referenced by alias.
	 *
	 * @param alias
	 * @throws KeyStoreLtException
	 */
	public final void deleteEntry(final String alias) throws KeyStoreLtException {
		this.ltmKeyStore.deleteEntry(alias);
	}

	/*
	 * Global
	 */

	public final boolean containsAlias(final String alias) throws LtmLtRtException, CertificateLtException {
		boolean result = this.ltmKeyStore.containsAlias(alias);
		if (!result) {
			result = (getSecretIV(alias) != null);
		}
		if (!result) {
			result = (getCertificate(alias) != null);
		}
		return result;
	}

	/**
	 * Deletes any data referenced by alias.
	 *
	 * @param alias
	 * @throws KeyStoreLtException
	 */
	public final void delete(final String alias) throws KeyStoreLtException {
		deleteEntry(alias);
		deleteSecretIV(alias);
		deleteCertificate(alias);
	}

	/**
	 * Deletes all data in this vault.
	 *
	 * @throws KeyStoreLtException
	 * @throws IOException
	 */
	public final void clear() throws KeyStoreLtException, IOException {
		this.ltmKeyStore.deleteAllEntries();
		persist();
		deleteAllSecretIV();
		deleteAllCertificate();
	}

}

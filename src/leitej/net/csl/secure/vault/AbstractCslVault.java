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

package leitej.net.csl.secure.vault;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bouncycastle.cert.X509CertificateHolder;

import leitej.crypto.ConstantCrypto;
import leitej.crypto.asymmetric.certificate.CertificateUtil;
import leitej.crypto.keyStore.Password;
import leitej.crypto.vault.Vault;
import leitej.exception.CertificateChainLtException;
import leitej.exception.CertificateLtException;
import leitej.exception.ExpiredDataLtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.KeyStoreLtException;
import leitej.exception.LtmLtRtException;
import leitej.log.Logger;
import leitej.ltm.LongTermMemory;
import leitej.ltm.LtmFilter;
import leitej.ltm.LtmFilter.OPERATOR;
import leitej.ltm.LtmFilter.OPERATOR_JOIN;
import leitej.util.data.Cache;
import leitej.util.data.CacheSoft;
import leitej.util.data.CacheWeak;

/**
 *
 * @author Julio Leite
 */
public abstract class AbstractCslVault implements CslVaultItf {

	private static final String CSL_VAULT_ALAIS = "__csl" + ConstantCrypto.KEYSTORE_FILE_EXTENSION;

	private static final Logger LOG = Logger.getInstance();

	private static final String CSL_PRIVATE_KEY_ENTRY_ALIAS = "__CSL_PRIVATE_KEY_ENTRY_ALIAS__";

	private static final LongTermMemory LTM = LongTermMemory.getInstance();

	private final Map<String, CslCertificate> cslCertificateIssuerMap;
	private final Cache<String, Cadastre> cadastreCache;
	private final Cache<String, CslCertificate> endPointCslCertificateMap;

	protected final Vault vault;

	/**
	 *
	 * @param password
	 * @throws KeyStoreLtException    if the keystore cannot be loaded
	 * @throws IOException            if there is an I/O or format problem with the
	 *                                keystore data, if a password is required but
	 *                                not given, or if the given password was
	 *                                incorrect. If the error is due to a wrong
	 *                                password, the {@link Throwable#getCause cause}
	 *                                of the <code>IOException</code> should be an
	 *                                <code>UnrecoverableKeyException</code>
	 * @throws ExpiredDataLtException if at the first load of the vault, the primary
	 *                                application trusted anchor does not pass
	 *                                verification procedure
	 * @throws CertificateLtException
	 */
	protected AbstractCslVault(final Password password)
			throws KeyStoreLtException, IOException, ExpiredDataLtException, CertificateLtException {
		// load vault
		if (Vault.exists(CSL_VAULT_ALAIS)) {
			this.vault = Vault.load(CSL_VAULT_ALAIS, password);
		} else {
			this.vault = Vault.create(CSL_VAULT_ALAIS, password);
		}
		// initialise maps
		this.cslCertificateIssuerMap = new HashMap<>();
		this.cadastreCache = new CacheWeak<>();
		this.endPointCslCertificateMap = new CacheSoft<>();
		// initiate trusted anchor
		if (!this.vault.containsAlias(TrustedAnchor.ALIAS)) {
			this.vault.setTrustedCertificateEntry(TrustedAnchor.ALIAS, TrustedAnchor.CERTIFICATE);
			if (!this.isTrustedAnchor(TrustedAnchor.CERTIFICATE)) {
				throw new ExpiredDataLtException();
			}
		}
	}

	/**
	 *
	 * @return true only if has my key, otherwise false
	 * @throws KeyStoreLtException if the key cannot be recovered
	 */
	public final boolean hasCslKey() throws KeyStoreLtException {
		return this.vault.getPrivateKey(CSL_PRIVATE_KEY_ENTRY_ALIAS) != null;
	}

	/**
	 *
	 * @param key
	 * @param certificates
	 * @throws KeyStoreLtException    if the given key cannot be protected, or this
	 *                                operation fails for some other reason
	 * @throws IOException            if there was an I/O problem with data
	 * @throws CertificateLtException
	 */
	protected final synchronized void setCslKey(final PrivateKey key, final X509CertificateHolder[] certificates)
			throws KeyStoreLtException, IOException, CertificateLtException {
		this.vault.setPrivateKeyEntry(CSL_PRIVATE_KEY_ENTRY_ALIAS, key, certificates);
		this.vault.persist();
	}

	@Override
	public final PrivateKey getCslPrivateKey() throws KeyStoreLtException {
		return this.vault.getPrivateKey(CSL_PRIVATE_KEY_ENTRY_ALIAS);
	}

	@Override
	public final X509CertificateHolder[] getCslChainCertificate() throws KeyStoreLtException, CertificateLtException {
		return this.vault.getKeyCertificateChain(CSL_PRIVATE_KEY_ENTRY_ALIAS);
	}

	@Override
	public final X509CertificateHolder getCslCertificate() throws KeyStoreLtException, CertificateLtException {
		return this.vault.getKeyCertificate(CSL_PRIVATE_KEY_ENTRY_ALIAS);
	}

	/**
	 *
	 * @param certificate
	 * @throws KeyStoreLtException    if the created alias from certificate already
	 *                                exists and does not identify an entry
	 *                                containing a trusted certificate, or this
	 *                                operation fails for some other reason
	 * @throws IOException            if there was an I/O problem with data
	 * @throws CertificateLtException
	 */
	protected final synchronized void setTrustedAnchor(final X509CertificateHolder certificate)
			throws KeyStoreLtException, IOException, CertificateLtException {
		this.vault.setTrustedCertificateEntry(CertificateUtil.getAlias(certificate), certificate);
		this.vault.persist();
	}

	@Override
	public final boolean isTrustedAnchor(final X509CertificateHolder certificate)
			throws KeyStoreLtException, CertificateLtException, IOException {
		final String alias = CertificateUtil.getAlias(certificate);
		final X509CertificateHolder trustedCertificate = this.vault.getTrustedCertificate(alias);
		if (trustedCertificate == null) {
			// TODO: verificar possivel nova trusted anchor assinada pelos rootLinks
			return false;
		}
		try {
			return CertificateUtil.isValidX509(certificate, trustedCertificate);
		} catch (final CertificateLtException e) {
			LOG.debug("#0", e.toString());
			return false;
		}
	}

	@Override
	public final void verifyEndPointCertificate(final X509CertificateHolder certificate)
			throws CertificateLtException, LtmLtRtException, IOException {
		final String alias = CertificateUtil.getAlias(certificate);
		final Cadastre cadastre = getEndPointCadastre(alias);
		synchronized (cadastre) {
			CslCertificate cslCertificate;
			try {
				cslCertificate = getEndPointCslCertificateMap(cadastre);
			} catch (final IOException e) {
				throw new LtmLtRtException(e);
			}
			if (!CertificateUtil.isValidX509(certificate, cslCertificate.getIssuer())) {
				throw new CertificateLtException();
			}
			cslCertificate.check();
		}
	}

	private final CslCertificate getEndPointCslCertificateMap(final Cadastre cadastre)
			throws CertificateLtException, IOException {
		synchronized (this.endPointCslCertificateMap) {
			CslCertificate result = this.endPointCslCertificateMap.get(cadastre.getAlias());
			if (result == null) {
				result = new CslCertificate(this.vault.getCertificate(cadastre.getAlias()), getRooterCslCertificate(cadastre));
				this.endPointCslCertificateMap.set(cadastre.getAlias(), result);
			}
			return result;
		}
	}

	private final CslCertificate getRooterCslCertificate(final Cadastre cadastre)
			throws CertificateLtException, IOException {
		CslCertificate result = this.cslCertificateIssuerMap.get(cadastre.getAlias());
		if (result == null) {
			final CadastreIssuer cadastreIssuer = cadastre.getIssuer();
			if (cadastreIssuer != null) {
				result = new CslCertificate(this.vault.getCertificate(cadastreIssuer.getAlias()),
						getCslCertificateIssuer(cadastreIssuer));
				this.cslCertificateIssuerMap.put(cadastre.getAlias(), result);
			}
		}
		return result;
	}

	private final CslCertificate getCslCertificateIssuer(final CadastreIssuer cadastre)
			throws CertificateLtException, IOException {
		if (cadastre.getIssuer() == null) {
			return null;
		}
		CslCertificate result = this.cslCertificateIssuerMap.get(cadastre.getAlias());
		if (result == null) {
			final CadastreIssuer cadastreIssuer = cadastre.getIssuer();
			result = new CslCertificate(this.vault.getCertificate(cadastreIssuer.getAlias()),
					getCslCertificateIssuer(cadastreIssuer));
			this.cslCertificateIssuerMap.put(cadastre.getAlias(), result);
		}
		return result;
	}

	@Override
	public final void addEndPointChain(final X509CertificateHolder[] chain) throws CertificateChainLtException,
			LtmLtRtException, IOException, KeyStoreLtException, CertificateEncodingException, CertificateLtException {
		if (!isTrustedAnchor(chain[chain.length - 1])) {
			throw new CertificateChainLtException();
		}
		try {
			if (!CertificateUtil.isValidChain(chain)) {
				throw new CertificateChainLtException();
			}
		} catch (final CertificateLtException e) {
			throw new CertificateChainLtException(e);
		}
		synchronized (this.endPointCslCertificateMap) {
			final String alias = CertificateUtil.getAlias(chain[0]);
			Cadastre cadastre;
			try {
				cadastre = getEndPointCadastre(alias);
			} catch (final CertificateLtException e) {
				this.vault.setCertificateChain(chain);
				cadastre = LTM.newRecord(Cadastre.class);
				synchronized (cadastre) {
					cadastre.setAlias(alias);
					cadastre.setSaltIn(new byte[48]);
					cadastre.setSaltOut(new byte[48]);
					cadastre.setIssuer(addCadastreIssuer(chain, 1));
					synchronized (this.cadastreCache) {
						this.cadastreCache.set(alias, cadastre);
					}
				}
			}
		}
	}

	private final CadastreIssuer addCadastreIssuer(final X509CertificateHolder[] chain, final int position)
			throws LtmLtRtException, IOException {
		if (position == chain.length) {
			return null;
		}
		final String alias = CertificateUtil.getAlias(chain[position]);
		CadastreIssuer result;
		final LtmFilter<CadastreIssuer> filter = new LtmFilter<>(CadastreIssuer.class, OPERATOR_JOIN.AND);
		filter.append(OPERATOR.EQUAL).setAlias(alias);
		final Iterator<CadastreIssuer> found = LTM.search(filter);
		if (found.hasNext()) {
			result = found.next();
		} else {
			result = LTM.newRecord(CadastreIssuer.class);
			result.setAlias(alias);
			result.setIssuer(addCadastreIssuer(chain, position + 1));
		}
		return result;
	}

	@Override
	public final void saltIn(final byte[] remoteHalfStateKeyBlock, final X509CertificateHolder clientCertificate)
			throws LtmLtRtException, IOException {
		try {
			final Cadastre cadastre = getEndPointCadastre(clientCertificate);
			byte[] salt;
			synchronized (cadastre) {
				salt = cadastre.getSaltIn();
			}
			for (int i = 0; i < salt.length; i++) {
				remoteHalfStateKeyBlock[i] = (byte) ((remoteHalfStateKeyBlock[i] ^ salt[i]) & 0xff);
			}
		} catch (final CertificateLtException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	@Override
	public final void saltOut(final byte[] myHalfStateKeyBlock, final X509CertificateHolder clientCertificate)
			throws LtmLtRtException, IOException {
		try {
			final Cadastre cadastre = getEndPointCadastre(clientCertificate);
			byte[] salt;
			synchronized (cadastre) {
				salt = cadastre.getSaltOut();
			}
			for (int i = 0; i < salt.length; i++) {
				myHalfStateKeyBlock[i] = (byte) ((myHalfStateKeyBlock[i] ^ salt[i]) & 0xff);
			}
		} catch (final CertificateLtException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	@Override
	public final void updateSalt(final byte[] remoteKeyBlock, final byte[] myKeyBlock,
			final X509CertificateHolder clientCertificate) throws LtmLtRtException, IOException {
		try {
			final Cadastre cadastre = getEndPointCadastre(clientCertificate);
			synchronized (cadastre) {
				cadastre.setSaltIn(remoteKeyBlock);
				cadastre.setSaltOut(myKeyBlock);
			}
		} catch (final CertificateLtException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 *
	 * @param aliasClientCertificate
	 * @return the cadastre
	 * @throws CertificateLtException if cadastre is not present for the argument
	 *                                certificate alias
	 * @throws LtmLtRtException       if encountered a problem accessing the long
	 *                                term memory
	 */
	private final Cadastre getEndPointCadastre(final String aliasClientCertificate)
			throws CertificateLtException, LtmLtRtException {
		synchronized (this.cadastreCache) {
			Cadastre result = this.cadastreCache.get(aliasClientCertificate);
			if (result == null) {
				final LtmFilter<Cadastre> filter = new LtmFilter<>(Cadastre.class, OPERATOR_JOIN.AND);
				filter.append(OPERATOR.EQUAL).setAlias(aliasClientCertificate);
				final Iterator<Cadastre> found = LTM.search(filter);
				if (!found.hasNext()) {
					throw new CertificateLtException();
				}
				result = found.next();
				this.cadastreCache.set(aliasClientCertificate, result);
			}
			return result;
		}
	}

	/**
	 *
	 * @param clientCertificate
	 * @return the cadastre
	 * @throws CertificateLtException if cadastre is not present for the argument
	 *                                certificate
	 * @throws LtmLtRtException       if encountered a problem accessing the long
	 *                                term memory
	 * @throws IOException
	 */
	private final Cadastre getEndPointCadastre(final X509CertificateHolder clientCertificate)
			throws CertificateLtException, LtmLtRtException, IOException {
		final String alias = CertificateUtil.getAlias(clientCertificate);
		return getEndPointCadastre(alias);
	}

}

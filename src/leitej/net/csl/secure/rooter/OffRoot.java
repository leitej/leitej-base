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

package leitej.net.csl.secure.rooter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.jce.PKCS10CertificationRequest;

import leitej.Constant;
import leitej.crypto.ConstantCrypto;
import leitej.crypto.Cryptography;
import leitej.crypto.asymmetric.certificate.CertificateChainUtil;
import leitej.crypto.asymmetric.certificate.CertificateIoUtil;
import leitej.crypto.asymmetric.certificate.CertificateUtil;
import leitej.crypto.asymmetric.signature.SignatureEnum;
import leitej.crypto.keyStore.Password;
import leitej.crypto.keyStore.UberKeyStore;
import leitej.exception.CertificateAuthorityLtException;
import leitej.exception.CertificateLtException;
import leitej.exception.CertificationRequestLtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.KeyStoreLtException;
import leitej.log.Logger;
import leitej.util.DateUtil;
import leitej.util.data.DateFieldEnum;
import leitej.util.stream.FileUtil;

/**
 *
 * @author Julio Leite
 */
public final class OffRoot {

	private static final Logger LOG = Logger.getInstance();

	private static final String KEYSTORE_FILENAME = Constant.DEFAULT_DATA_FILE_DIR + "/rooter"
			+ ConstantCrypto.DEFAULT_UBER_KEYSTORE_FILE_EXTENSION;
	static final String REPOSITORY_FILE_DIR = "repository";
	public static final int COMMUNICATION_PATH_LENGTH = 6;

	private static final String MY_CERTIFICATE_FILENAME = REPOSITORY_FILE_DIR + "/offroot"
			+ ConstantCrypto.CERTIFICATE_FILE_EXTENSION;
	private static final String MY_CERTIFICATE_CONTINGENCY_FILENAME = REPOSITORY_FILE_DIR + "/offroot.contingency"
			+ ConstantCrypto.CERTIFICATE_FILE_EXTENSION;
	private static final String MY_CERTIFICATE_ISSUER = "leitej.offline.root";
	private static final int RSA_KEY_BIT_SIZE = 8192;
	private static final int VALIDITY_FOR_TIME_YEARS = 32;
	private static final int VALIDITY_ACTIVE_TIME_YEARS = VALIDITY_FOR_TIME_YEARS / 2;

	private static final String MY_ENTRY_KEY_ALIAS = "__MY_ENTRY_KEY_ALIAS__";
	private static final String MY_ENTRY_KEY_OLD_ALIAS = "__MY_ENTRY_KEY_OLD_ALIAS__";
	private static final String MY_CONTINGENCY_ENTRY_KEY_ALIAS = "__MY_CONTINGENCY_ENTRY_KEY_ALIAS__";

	private final UberKeyStore keystore;

	public OffRoot(final Password password) throws KeyStoreLtException, IOException {
		if (!FileUtil.exists(KEYSTORE_FILENAME)) {
			LOG.warn("Creating keystore: #0", KEYSTORE_FILENAME);
			this.keystore = UberKeyStore.create(KEYSTORE_FILENAME, password);
		} else {
			LOG.info("Loading keystore: #0", KEYSTORE_FILENAME);
			this.keystore = UberKeyStore.load(KEYSTORE_FILENAME, password);
		}
		updateVault();
	}

	private final void updateVault() throws KeyStoreLtException, IOException {
		boolean mod = false;
		final Date now = DateUtil.zeroTill(DateUtil.now(), DateFieldEnum.DAY_OF_MONTH);
		if (this.keystore.getPrivateKey(MY_ENTRY_KEY_ALIAS) == null) {
			final Date valid = DateUtil.add((Date) now.clone(), DateFieldEnum.YEAR, VALIDITY_FOR_TIME_YEARS);
			newKeys(MY_ENTRY_KEY_ALIAS, MY_CERTIFICATE_FILENAME, now, valid);
			newKeys(MY_CONTINGENCY_ENTRY_KEY_ALIAS, MY_CERTIFICATE_CONTINGENCY_FILENAME, now, valid);
			mod = true;
		}
		final Date expireActiveTime = DateUtil.add(
				(Date) this.keystore.getKeyCertificate(MY_ENTRY_KEY_ALIAS).getNotBefore().clone(), DateFieldEnum.YEAR,
				VALIDITY_ACTIVE_TIME_YEARS);
		if (now.getTime() > expireActiveTime.getTime()) {
			// TODO: rever o sitema para a contigencia
			upgradeContingencyKey();
			newKeys(MY_CONTINGENCY_ENTRY_KEY_ALIAS, MY_CERTIFICATE_CONTINGENCY_FILENAME, expireActiveTime,
					DateUtil.add((Date) expireActiveTime.clone(), DateFieldEnum.YEAR, VALIDITY_FOR_TIME_YEARS));
			mod = true;
		}
		if (mod) {
			this.keystore.persist();
		}
	}

	private final void upgradeContingencyKey() throws KeyStoreLtException, IOException {
		if (DateUtil.isFuture(this.keystore.getKeyCertificate(MY_CONTINGENCY_ENTRY_KEY_ALIAS).getNotBefore())) {
			throw new ImplementationLtRtException();
		}
		LOG.info("Upgrading contingency key");
		this.keystore.setPrivateKeyEntry(MY_ENTRY_KEY_OLD_ALIAS, this.keystore.getPrivateKey(MY_ENTRY_KEY_ALIAS),
				new X509Certificate[] { this.keystore.getKeyCertificate(MY_ENTRY_KEY_ALIAS) });
		this.keystore.setPrivateKeyEntry(MY_ENTRY_KEY_ALIAS,
				this.keystore.getPrivateKey(MY_CONTINGENCY_ENTRY_KEY_ALIAS),
				new X509Certificate[] { this.keystore.getKeyCertificate(MY_CONTINGENCY_ENTRY_KEY_ALIAS) });
		if (!FileUtil.renameToBackup(MY_CERTIFICATE_FILENAME, true)) {
			throw new IOException();
		}
		if (!FileUtil.rename(MY_CERTIFICATE_CONTINGENCY_FILENAME, MY_CERTIFICATE_FILENAME)) {
			throw new IOException();
		}
	}

	private final void newKeys(final String aliasName, final String certificateFilename, final Date validAfter,
			final Date validTill) throws KeyStoreLtException, IOException {
		LOG.info("Generating new key: #0", RSA_KEY_BIT_SIZE);
		try {
			final KeyPair keys = Cryptography.RSA.keyPairGenerate(RSA_KEY_BIT_SIZE);
			final X509Certificate certificate = CertificateUtil.generateX509CertificateV3SelfSigned(
					MY_CERTIFICATE_ISSUER, keys, SignatureEnum.SHA512WithRSAAndMGF1,
					CertificateUtil.generateSerialNumber(), validAfter, validTill, COMMUNICATION_PATH_LENGTH);
			this.keystore.setPrivateKeyEntry(aliasName, keys.getPrivate(), new X509Certificate[] { certificate });
			CertificateIoUtil.writeX509CertificatesPEM(certificateFilename, false, Constant.UTF8_CHARSET_NAME,
					certificate);
		} catch (final IllegalArgumentLtRtException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchAlgorithmException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchProviderException e) {
			throw new ImplementationLtRtException(e);
		} catch (final CertificateLtException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NullPointerException e) {
			throw new ImplementationLtRtException(e);
		} catch (final UnsupportedEncodingException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	public final void updateRepository()
			throws CertificationRequestLtException, CertificateLtException, KeyStoreLtException, IOException {
		final File repositoryDir = new File(REPOSITORY_FILE_DIR);
		for (final File sub : repositoryDir.listFiles()) {
			if (sub.isFile() && sub.getName().matches(".*\\" + ConstantCrypto.REQUEST_FILE_EXTENSION)) {
				sign(sub);
			}
		}
	}

	/**
	 *
	 * @param fileRequest
	 * @throws IOException                     if an I/O error occurred
	 * @throws CertificationRequestLtException if the certification request in the
	 *                                         input stream is not of type
	 *                                         PKCS10CertificationRequest or some
	 *                                         irregular information found in the
	 *                                         request
	 * @throws CertificateLtException          if some exception occurred at
	 *                                         invocation to generate the
	 *                                         certificate
	 * @throws KeyStoreLtException             if some exception occurred while
	 *                                         getting information from own keystore
	 */
	private final void sign(final File fileRequest)
			throws CertificationRequestLtException, IOException, CertificateLtException, KeyStoreLtException {
		LOG.info("Signing request: #0", fileRequest.getName());
		final Date now = DateUtil.zeroTill(DateUtil.now(), DateFieldEnum.DAY_OF_MONTH);
		PKCS10CertificationRequest request;
		try {
			request = CertificateIoUtil.readPKCS10CertificationRequestPEM(fileRequest.getAbsolutePath(),
					Constant.UTF8_CHARSET_NAME);
		} catch (final NullPointerException e) {
			throw new ImplementationLtRtException(e);
		} catch (final UnsupportedEncodingException e) {
			throw new ImplementationLtRtException(e);
		} catch (final FileNotFoundException e) {
			throw new ImplementationLtRtException(e);
		}
		X509Certificate[] certificates;
		try {
			certificates = CertificateChainUtil.addLink(this.keystore.getKeyCertificateChain(MY_ENTRY_KEY_ALIAS),
					(PrivateKey) this.keystore.getPrivateKey(MY_ENTRY_KEY_ALIAS), SignatureEnum.SHA512WithRSAAndMGF1,
					CertificateUtil.generateSerialNumber(), now,
					DateUtil.add((Date) now.clone(), DateFieldEnum.YEAR, VALIDITY_ACTIVE_TIME_YEARS), request);
		} catch (final CertificateAuthorityLtException e) {
			throw new ImplementationLtRtException(e);
		}
		final String filenameRequest = fileRequest.getAbsolutePath();
		final String filenameCertificate = filenameRequest.substring(0, filenameRequest.length() - 4)
				+ ConstantCrypto.CERTIFICATE_FILE_EXTENSION;
		try {
			CertificateIoUtil.writeX509CertificatesPEM(filenameCertificate, false, Constant.UTF8_CHARSET_NAME,
					certificates);
		} catch (final NullPointerException e) {
			throw new ImplementationLtRtException(e);
		} catch (final UnsupportedEncodingException e) {
			throw new ImplementationLtRtException(e);
		} catch (final FileNotFoundException e) {
			throw new ImplementationLtRtException(e);
		}
	}

}

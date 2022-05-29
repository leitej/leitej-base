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
import java.io.PrintWriter;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.util.Date;
import java.util.Scanner;

import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;

import leitej.Constant;
import leitej.crypto.ConstantCrypto;
import leitej.crypto.Cryptography;
import leitej.crypto.asymmetric.certificate.CertificateStreamUtil;
import leitej.crypto.asymmetric.certificate.CertificateUtil;
import leitej.crypto.keyStore.FileKeyStore;
import leitej.crypto.keyStore.Password;
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

	private static final File KEYSTORE_FILE = new File(Constant.DEFAULT_DATA_FILE_DIR,
			"rooter" + ConstantCrypto.KEYSTORE_FILE_EXTENSION);
	static final File REPOSITORY_FILE_DIR = new File(Constant.DEFAULT_DATA_FILE_DIR, "rooter");
	public static final int COMMUNICATION_PATH_LENGTH = 6;
	static {
		REPOSITORY_FILE_DIR.mkdirs();
	}

	private static final File MY_CERTIFICATE_FILE = new File(REPOSITORY_FILE_DIR,
			"offroot" + ConstantCrypto.CERTIFICATE_FILE_EXTENSION);
	private static final File MY_CERTIFICATE_CONTINGENCY_FILE = new File(REPOSITORY_FILE_DIR,
			"offroot.contingency" + ConstantCrypto.CERTIFICATE_FILE_EXTENSION);
	private static final String MY_CERTIFICATE_ISSUER = "leitej.offline.root";
	private static final int RSA_KEY_BIT_SIZE = 8192;
	private static final int VALIDITY_FOR_TIME_YEARS = 32;
	private static final int VALIDITY_ACTIVE_TIME_YEARS = VALIDITY_FOR_TIME_YEARS / 2;

	private static final String MY_ENTRY_KEY_ALIAS = "__MY_ENTRY_KEY_ALIAS__";
	private static final String MY_ENTRY_KEY_OLD_ALIAS = "__MY_ENTRY_KEY_OLD_ALIAS__";
	private static final String MY_CONTINGENCY_ENTRY_KEY_ALIAS = "__MY_CONTINGENCY_ENTRY_KEY_ALIAS__";

	private final FileKeyStore keystore;

	public OffRoot(final Password password) throws KeyStoreLtException, IOException, CertificateLtException {
		if (!KEYSTORE_FILE.exists()) {
			LOG.warn("Creating keystore: #0", KEYSTORE_FILE);
			this.keystore = FileKeyStore.create(KEYSTORE_FILE, password);
		} else {
			LOG.info("Loading keystore: #0", KEYSTORE_FILE);
			this.keystore = FileKeyStore.load(KEYSTORE_FILE, password);
		}
		updateVault();
	}

	private final void updateVault() throws KeyStoreLtException, IOException, CertificateLtException {
		boolean mod = false;
		final Date now = DateUtil.zeroTill(DateUtil.now(), DateFieldEnum.DAY_OF_MONTH);
		if (this.keystore.getPrivateKey(MY_ENTRY_KEY_ALIAS) == null) {
			final Date valid = DateUtil.add((Date) now.clone(), DateFieldEnum.YEAR, VALIDITY_FOR_TIME_YEARS);
			newKeys(MY_ENTRY_KEY_ALIAS, MY_CERTIFICATE_FILE, now, valid);
			newKeys(MY_CONTINGENCY_ENTRY_KEY_ALIAS, MY_CERTIFICATE_CONTINGENCY_FILE, now, valid);
			mod = true;
		}
		final Date expireActiveTime = DateUtil.add(
				(Date) this.keystore.getKeyCertificate(MY_ENTRY_KEY_ALIAS).getNotBefore().clone(), DateFieldEnum.YEAR,
				VALIDITY_ACTIVE_TIME_YEARS);
		if (now.getTime() > expireActiveTime.getTime()) {
			// TODO: rever o sitema para a contigencia
			upgradeContingencyKey();
			newKeys(MY_CONTINGENCY_ENTRY_KEY_ALIAS, MY_CERTIFICATE_CONTINGENCY_FILE, expireActiveTime,
					DateUtil.add((Date) expireActiveTime.clone(), DateFieldEnum.YEAR, VALIDITY_FOR_TIME_YEARS));
			mod = true;
		}
		if (mod) {
			this.keystore.persist();
		}
	}

	private final void upgradeContingencyKey() throws KeyStoreLtException, IOException, CertificateLtException {
		if (DateUtil.isFuture(this.keystore.getKeyCertificate(MY_CONTINGENCY_ENTRY_KEY_ALIAS).getNotBefore())) {
			throw new ImplementationLtRtException();
		}
		LOG.info("Upgrading contingency key");
		this.keystore.setPrivateKeyEntry(MY_ENTRY_KEY_OLD_ALIAS, this.keystore.getPrivateKey(MY_ENTRY_KEY_ALIAS),
				new X509CertificateHolder[] { this.keystore.getKeyCertificate(MY_ENTRY_KEY_ALIAS) });
		this.keystore.setPrivateKeyEntry(MY_ENTRY_KEY_ALIAS, this.keystore.getPrivateKey(MY_CONTINGENCY_ENTRY_KEY_ALIAS),
				new X509CertificateHolder[] { this.keystore.getKeyCertificate(MY_CONTINGENCY_ENTRY_KEY_ALIAS) });
		if (!FileUtil.renameToBackup(MY_CERTIFICATE_FILE, true)) {
			throw new IOException();
		}
		if (!MY_CERTIFICATE_CONTINGENCY_FILE.renameTo(MY_CERTIFICATE_FILE)) {
			throw new IOException();
		}
	}

	private final void newKeys(final String aliasName, final File certificateFile, final Date validAfter,
			final Date validTill) throws KeyStoreLtException, IOException {
		LOG.info("Generating new key: #0", RSA_KEY_BIT_SIZE);
		try {
			final KeyPair keys = Cryptography.RSA.keyPairGenerate(RSA_KEY_BIT_SIZE);
			final X509CertificateHolder certificate = CertificateUtil.generateX509CertificateV3SelfSignedRootChain(
					new X500Name("CN=" + MY_CERTIFICATE_ISSUER), keys, validAfter, validTill, COMMUNICATION_PATH_LENGTH);
			this.keystore.setPrivateKeyEntry(aliasName, keys.getPrivate(), new X509CertificateHolder[] { certificate });
			final PrintWriter writer = new PrintWriter(certificateFile);
			CertificateStreamUtil.writeX509CertificatesPEM(writer, certificate);
			writer.close();
		} catch (final IllegalArgumentLtRtException | NoSuchAlgorithmException | NoSuchProviderException
				| CertificateLtException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	public final void updateRepository()
			throws CertificationRequestLtException, CertificateLtException, KeyStoreLtException, IOException {
		for (final File sub : REPOSITORY_FILE_DIR.listFiles()) {
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
		final Scanner scanner = new Scanner(fileRequest.getAbsolutePath());
		final Date now = DateUtil.zeroTill(DateUtil.now(), DateFieldEnum.DAY_OF_MONTH);
		CertificationRequest request;
		try {
			request = CertificateStreamUtil.readCertificationRequestPEM(scanner);
			scanner.close();
		} catch (final NullPointerException e) {
			throw new ImplementationLtRtException(e);
		}
		X509CertificateHolder[] certificates;
		certificates = CertificateUtil.addLink(this.keystore.getKeyCertificateChain(MY_ENTRY_KEY_ALIAS),
				(PrivateKey) this.keystore.getPrivateKey(MY_ENTRY_KEY_ALIAS), now,
				DateUtil.add((Date) now.clone(), DateFieldEnum.YEAR, VALIDITY_ACTIVE_TIME_YEARS), request);
		final String filenameRequest = fileRequest.getAbsolutePath();
		final String filenameCertificate = filenameRequest.substring(0, filenameRequest.length() - 4)
				+ ConstantCrypto.CERTIFICATE_FILE_EXTENSION;
		try {
			final PrintWriter writer = new PrintWriter(filenameCertificate);
			CertificateStreamUtil.writeX509CertificatesPEM(writer, certificates);
			writer.close();
		} catch (final NullPointerException e) {
			throw new ImplementationLtRtException(e);
		} catch (final FileNotFoundException e) {
			throw new ImplementationLtRtException(e);
		}
	}

}

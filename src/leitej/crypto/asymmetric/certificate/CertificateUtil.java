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

package leitej.crypto.asymmetric.certificate;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;

import leitej.crypto.Cryptography;
import leitej.exception.CertificateChainLtException;
import leitej.exception.CertificateLtException;
import leitej.util.DateUtil;
import sun.security.pkcs10.PKCS10;
import sun.security.x509.AlgorithmId;
import sun.security.x509.BasicConstraintsExtension;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.KeyUsageExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 *
 * @author Julio Leite
 */
public final class CertificateUtil {

	private CertificateUtil() {
	}

	// Distinguished Name X.500
	// example: 'CN=www.bouncycastle.org, OU=Bouncy Castle, O=Legions, C=AU'
	// CN - commonName, OID '2.5.4.3', limited to 64 characters
	// OU - organizationalUnitName, OID '2.5.4.11', limited to 64 characters
	// O - organizationName, OID '2.5.4.10', limited to 64 characters
	// C - country, OID '2.5.4.6', limited to 2 characters
	// L - localityName, OID '2.5.4.7', limited to 64 characters
	// ST - stateOrProvinceName, OID '2.5.4.8', limited to 64 characters

	/*
	 * CA (certificate authority) the entity responsible for issuing the
	 * certificates
	 */

	public static final String getAliasFrom(final X509Certificate certificate) {
		return certificate.getSerialNumber().toString(Character.MAX_RADIX)
				+ certificate.getSubjectX500Principal().getName();
	}

	/**
	 * Generates a unique number for witch call, for the running JVM.
	 *
	 * @return unique number
	 */
	public static BigInteger generateSerialNumber() {
		return BigInteger.valueOf(DateUtil.generateUniqueNumberPerJVM());
	}

	/*
	 * Generate X509 certificates
	 */

	/**
	 * Generates a X.509 certificate version 3 - self signed (root for a chain of
	 * certificates).
	 *
	 * @param issuerDN           common name
	 * @param keys
	 * @param signatureAlgorithm the name of the signature algorithm used
	 * @param validAfter         the date and time after which the certificate is
	 *                           valid
	 * @param expireDate         the date and time after which the certificate
	 *                           expires
	 * @param pathLenConstraint  specifies the depth of the certification path (if
	 *                           >-1 create as certificate authority)
	 * @return generated certificate
	 * @throws CertificateLtException <br/>
	 *                                +Cause IOException on other errors <br/>
	 *                                +Cause CertificateException on invalid
	 *                                attributes or encoding errors <br/>
	 *                                +Cause NoSuchAlgorithmException if no Provider
	 *                                supports a Signature implementation for the
	 *                                specified algorithm <br/>
	 *                                +Cause SignatureException on signature
	 *                                handling errors <br/>
	 *                                +Cause NoSuchProviderException if there's no
	 *                                default provider <br/>
	 *                                +Cause InvalidKeyException on incorrect key
	 */
	public static X509Certificate generateX509CertificateV3SelfSignedRootChain(final X500Name issuerDN,
			final KeyPair keys, final Date validAfter, final Date expireDate, final int pathLenConstraint)
			throws CertificateLtException {
		return generateX509CertificateV3(issuerDN, Cryptography.getDefaultCertificateSignatureAlgorithm(),
				keys.getPrivate(), generatePKCS10(keys.getPublic(), issuerDN,
						Cryptography.getDefaultCertificateSignatureAlgorithm(), keys.getPrivate()),
				generateSerialNumber(), validAfter, expireDate, pathLenConstraint);
	}

	/**
	 * Generates a X.509 certificate version 3.
	 *
	 * @param issuerDN           distinguished name
	 * @param signatureAlgorithm the name of the signature algorithm used
	 * @param issuerPrivateKey   key used to sign
	 * @param csr                certificate sign request
	 * @param serialNumber       the serial number for the certificate
	 * @param validAfter         the date and time after which the certificate is
	 *                           valid
	 * @param expireDate         the date and time after which the certificate
	 *                           expires
	 * @param pathLenConstraint  specifies the depth of the certification path (if
	 *                           >-1 create as certificate authority)
	 * @return generated certificate
	 * @throws CertificateLtException <br/>
	 *                                +Cause IOException on other errors <br/>
	 *                                +Cause CertificateException on invalid
	 *                                attributes or encoding errors <br/>
	 *                                +Cause NoSuchAlgorithmException on unsupported
	 *                                signature algorithms <br/>
	 *                                +Cause SignatureException on signature errors
	 *                                <br/>
	 *                                +Cause NoSuchProviderException if there's no
	 *                                default provider <br/>
	 *                                +Cause InvalidKeyException on incorrect key
	 */
	private static X509Certificate generateX509CertificateV3(final X500Name issuerDN, final String signatureAlgorithm,
			final PrivateKey issuerPrivateKey, final PKCS10 csr, final BigInteger serialNumber, final Date validAfter,
			final Date expireDate, final int pathLenConstraint) throws CertificateLtException {
		try {
			final X509CertInfo certInfo = new X509CertInfo();
			certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
			certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(serialNumber));
			certInfo.set(X509CertInfo.VALIDITY, new CertificateValidity(validAfter, expireDate));
			certInfo.set(X509CertInfo.KEY, new CertificateX509Key(csr.getSubjectPublicKeyInfo()));
			certInfo.set(X509CertInfo.SUBJECT, csr.getSubjectName());
			certInfo.set(X509CertInfo.ISSUER, issuerDN);
			certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(AlgorithmId.get(signatureAlgorithm)));
			// x509v3 extensions
			final CertificateExtensions extensions = new CertificateExtensions();
			// KeyUsage ::= BIT STRING {
			// digitalSignature (0),
			// nonRepudiation (1),
			// keyEncipherment (2),
			// dataEncipherment (3),
			// keyAgreement (4),
			// keyCertSign (5),
			// cRLSign (6),
			// encipherOnly (7),
			// decipherOnly (8) }
			final boolean[] keyUsagePolicies = new boolean[9];
			if (pathLenConstraint > -1) {
				keyUsagePolicies[5] = true; // keyCertSign
			} else {
				keyUsagePolicies[0] = true; // digitalSignature
				keyUsagePolicies[2] = true; // keyEncipherment
			}
			final KeyUsageExtension keyUsageExtension = new KeyUsageExtension(true,
					(new KeyUsageExtension(keyUsagePolicies)).getExtensionValue());
			extensions.set(KeyUsageExtension.NAME, keyUsageExtension);
			// basic constraints
			final BasicConstraintsExtension bce;
			if (pathLenConstraint > -1) {
				bce = new BasicConstraintsExtension(true, true, pathLenConstraint);
			} else {
				bce = new BasicConstraintsExtension(true, false, -1);
			}
			extensions.set(BasicConstraintsExtension.NAME, bce);
			// add extensions
			certInfo.set(X509CertInfo.EXTENSIONS, extensions);
			// create certificate
			final X509CertImpl certificate = new X509CertImpl(certInfo);
			certificate.sign(issuerPrivateKey, signatureAlgorithm);
			return certificate;
		} catch (CertificateException | IOException | NoSuchAlgorithmException | InvalidKeyException
				| NoSuchProviderException | SignatureException e) {
			throw new CertificateLtException(e);
		}
	}

	/*
	 * Certificate Sign Request
	 */

	/**
	 * Constructs a certificate sign request.
	 *
	 * @param publicKey          the public key that should be placed into the
	 *                           certificate generated by the CA
	 * @param distinguishedName  X.500 names to identify entity
	 * @param signatureAlgorithm signature algorithm
	 * @param privateKey         the private key of the identity whose signature is
	 *                           going to be generated
	 * @return pkcs10 certificate request
	 * @throws CertificateLtException <br/>
	 *                                +Cause NoSuchAlgorithmException if no Provider
	 *                                supports a Signature implementation for the
	 *                                specified algorithm <br/>
	 *                                +Cause InvalidKeyException if the privateKey
	 *                                is invalid <br/>
	 *                                +Cause IOException on errors <br/>
	 *                                +Cause SignatureException on signature
	 *                                handling errors <br/>
	 *                                +Cause CertificateException on certificate
	 *                                handling errors
	 */
	public static PKCS10 generatePKCS10(final PublicKey publicKey, final X500Name distinguishedName,
			final String signatureAlgorithm, final PrivateKey privateKey) throws CertificateLtException {
		try {
			final Signature signature = Signature.getInstance(signatureAlgorithm);
			signature.initSign(privateKey);
			final PKCS10 pkcs10 = new PKCS10(publicKey);
			pkcs10.encodeAndSign(distinguishedName, signature);
			return pkcs10;
		} catch (NoSuchAlgorithmException | InvalidKeyException | CertificateException | SignatureException
				| IOException e) {
			throw new CertificateLtException(e);
		}
	}

	/*
	 * Verify
	 */

	/**
	 * Verifies that the certificate was signed using the private key that
	 * corresponds to the specified public key. <br/>
	 * Also verifies that is type of X.509 and if the current date and time are
	 * within the validity period given in the certificate.
	 *
	 * @param certificate
	 * @param pbcKeyTrust the trusted public key used to carry out the verification
	 * @throws CertificateLtException <br/>
	 *                                +Cause NoSuchAlgorithmException on unsupported
	 *                                signature algorithms <br/>
	 *                                +Cause InvalidKeyException on incorrect key
	 *                                <br/>
	 *                                +Cause NoSuchProviderException on incorrect
	 *                                provider <br/>
	 *                                +Cause SignatureException on signature errors
	 *                                <br/>
	 *                                +Cause CertificateException on encoding errors
	 *                                or it's type is not X.509 <br/>
	 *                                +Cause CertificateExpiredException if the
	 *                                certificate has expired <br/>
	 *                                +Cause CertificateNotYetValidException if the
	 *                                certificate is not yet valid
	 */
	public static void verify(final Certificate certificate, final PublicKey pbcKeyTrust)
			throws CertificateLtException {
		try {
			certificate.verify(pbcKeyTrust);
		} catch (final InvalidKeyException e) {
			throw new CertificateLtException(e);
		} catch (final CertificateException e) {
			throw new CertificateLtException(e);
		} catch (final NoSuchAlgorithmException e) {
			throw new CertificateLtException(e);
		} catch (final NoSuchProviderException e) {
			throw new CertificateLtException(e);
		} catch (final SignatureException e) {
			throw new CertificateLtException(e);
		}
		verifyTypeX509(certificate);
		try {
			X509Certificate.class.cast(certificate).checkValidity();
		} catch (final CertificateExpiredException e) {
			throw new CertificateLtException(e);
		} catch (final CertificateNotYetValidException e) {
			throw new CertificateLtException(e);
		}
	}

	/**
	 *
	 * @param certificate
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateException if it's type is
	 *                                not X.509
	 */
	public static void verifyTypeX509(final Certificate certificate) throws CertificateLtException {
		if (!X509Certificate.class.isInstance(certificate) || !TypeEnum.X509.getName().equals(certificate.getType())) {
			throw new CertificateLtException(new CertificateException(), "Invalid type: #0", certificate.getType());
		}
	}

	private static boolean isCA(final X509Certificate certificate) {
		return certificate.getBasicConstraints() > -1 && certificate.getKeyUsage()[5];
	}

	/**
	 * Verifies that the root certificate in the chain is self-signed.<br/>
	 * And all consecutive certificates were signed using the private key that
	 * corresponds to the public key in anterior certificate.<br/>
	 * Also verifies that all are of type X.509 and if the current date and time are
	 * within the validity period given in all the certificates. <br/>
	 *
	 * @param chain with the certificates to verify
	 * @throws CertificateLtException <br/>
	 *                                +Cause NoSuchAlgorithmException on unsupported
	 *                                signature algorithms <br/>
	 *                                +Cause InvalidKeyException on incorrect key
	 *                                <br/>
	 *                                +Cause NoSuchProviderException on incorrect
	 *                                provider <br/>
	 *                                +Cause SignatureException on signature errors
	 *                                <br/>
	 *                                +Cause CertificateException on encoding errors
	 *                                or it's type is not X.509 <br/>
	 *                                +Cause CertificateExpiredException if the
	 *                                certificate has expired <br/>
	 *                                +Cause CertificateNotYetValidException if the
	 *                                certificate is not yet valid <br/>
	 *                                +Cause CertificateChainLtException if the
	 *                                chain doesn't have certificate authority in
	 *                                the correct position or has an invalid path
	 *                                length constraint or the chain is empty
	 */
	public static void verifyChain(final Certificate[] chain) throws CertificateLtException {
		if (chain.length > 0) {
			final Certificate rootCertificate = chain[chain.length - 1];
			PublicKey publicKey = rootCertificate.getPublicKey();
			int issuerPathLength = Integer.MAX_VALUE;
			Certificate certificate;
			X509Certificate x509;
			int x509PathLength;
			boolean isCA;
			for (int i = chain.length - 1; i >= 0; i--) {
				certificate = chain[i];
				verify(certificate, publicKey);
				x509 = X509Certificate.class.cast(certificate);
				isCA = isCA(x509);
				if (i != 0 && !isCA) {
					throw new CertificateLtException(
							new CertificateChainLtException("Expected have root and intermediates as CA"));
				}
				x509PathLength = x509.getBasicConstraints();
				if (x509PathLength >= issuerPathLength) {
					throw new CertificateLtException(new CertificateChainLtException("Invalid path length"));
				}
				publicKey = certificate.getPublicKey();
				issuerPathLength = x509PathLength;
			}
		} else {
			throw new CertificateLtException(new CertificateChainLtException("Empty chain"));
		}
	}

	/*
	 * Certificate chain
	 */

	private static X509Certificate[] addEndCertificate(final X509Certificate certificate,
			final X509Certificate[] chain) {
		final X509Certificate[] result = new X509Certificate[chain.length + 1];
		result[0] = certificate;
		for (int i = 0; i < chain.length; i++) {
			result[i + 1] = chain[i];
		}
		return result;
	}

	/**
	 * Creates a new certificate from the <code>requestLink</code> and adds it to
	 * the chain.
	 *
	 * @param chain
	 * @param chain0PrivateKey the private key correspondent to the certificate in
	 *                         index 0 (will be used to sign the new link)
	 * @param validAfter
	 * @param expireDate
	 * @param requestLink
	 * @return a new array with the new link
	 * @throws CertificateLtException <br/>
	 *                                +Cause IOException on other errors <br/>
	 *                                +Cause CertificateException on invalid
	 *                                attributes or encoding errors <br/>
	 *                                +Cause NoSuchAlgorithmException on unsupported
	 *                                signature algorithms <br/>
	 *                                +Cause SignatureException on signature errors
	 *                                <br/>
	 *                                +Cause NoSuchProviderException if there's no
	 *                                default provider <br/>
	 *                                +Cause InvalidKeyException on incorrect key
	 *                                <br/>
	 *                                +Cause CertificateChainLtException if issuer
	 *                                is not a CA or valid date interval is invalid
	 *                                for the issuer or pathLenConstraint is invalid
	 */
	public static X509Certificate[] addLink(final X509Certificate[] chain, final PrivateKey chain0PrivateKey,
			final Date validAfter, final Date expireDate, final PKCS10 requestLink) throws CertificateLtException {
		final X509Certificate newCertificate = generateLinkedX509CertificateV3(requestLink, chain[0], chain0PrivateKey,
				validAfter, expireDate);
		return addEndCertificate(newCertificate, chain);
	}

	/**
	 * Creates a new certificate from the <code>requestLink</code> and adds it to
	 * the chain.
	 *
	 * @param chain
	 * @param chain0PrivateKey   the private key correspondent to the certificate in
	 *                           index 0 (will be used to sign the new link)
	 * @param signatureAlgorithm
	 * @param serialNumber
	 * @param validAfter
	 * @param expireDate
	 * @param requestLink
	 * @param pathLenConstraint  specifies the depth of the certification path (if
	 *                           >-1 create as certificate authority)
	 * @return a new array with the new link
	 * @throws CertificateLtException <br/>
	 *                                +Cause IOException on other errors <br/>
	 *                                +Cause CertificateException on invalid
	 *                                attributes or encoding errors <br/>
	 *                                +Cause NoSuchAlgorithmException on unsupported
	 *                                signature algorithms <br/>
	 *                                +Cause SignatureException on signature errors
	 *                                <br/>
	 *                                +Cause NoSuchProviderException if there's no
	 *                                default provider <br/>
	 *                                +Cause InvalidKeyException on incorrect key
	 *                                <br/>
	 *                                +Cause CertificateChainLtException if issuer
	 *                                is not a CA or valid date interval is invalid
	 *                                for the issuer or pathLenConstraint is invalid
	 */
	public static X509Certificate[] addLink(final X509Certificate[] chain, final PrivateKey chain0PrivateKey,
			final String signatureAlgorithm, final BigInteger serialNumber, final Date validAfter,
			final Date expireDate, final PKCS10 requestLink, final int pathLenConstraint)
			throws CertificateLtException {
		final X509Certificate newCertificate = generateLinkedX509CertificateV3(requestLink, chain[0], chain0PrivateKey,
				signatureAlgorithm, serialNumber, validAfter, expireDate, pathLenConstraint);
		return addEndCertificate(newCertificate, chain);
	}

	/**
	 * Generates a X.509 certificate version 3, signed by CA issuer.
	 *
	 * @param csr               certificate sign request
	 * @param issuerCertificate CA certificate
	 * @param issuerPrivateKey  key used to sign
	 * @param validAfter        the date and time after which the certificate is
	 *                          valid
	 * @param expireDate        the date and time after which the certificate
	 *                          expires
	 * @return the new certificate
	 * @throws CertificateLtException <br/>
	 *                                +Cause IOException on other errors <br/>
	 *                                +Cause CertificateException on invalid
	 *                                attributes or encoding errors <br/>
	 *                                +Cause NoSuchAlgorithmException on unsupported
	 *                                signature algorithms <br/>
	 *                                +Cause SignatureException on signature errors
	 *                                <br/>
	 *                                +Cause NoSuchProviderException if there's no
	 *                                default provider <br/>
	 *                                +Cause InvalidKeyException on incorrect key
	 *                                <br/>
	 *                                +Cause CertificateChainLtException if issuer
	 *                                is not a CA or valid date interval is invalid
	 *                                for the issuer or pathLenConstraint is invalid
	 */
	public static X509Certificate generateLinkedX509CertificateV3(final PKCS10 csr,
			final X509Certificate issuerCertificate, final PrivateKey issuerPrivateKey, final Date validAfter,
			final Date expireDate) throws CertificateLtException {
		return generateLinkedX509CertificateV3(csr, issuerCertificate, issuerPrivateKey,
				Cryptography.getDefaultCertificateSignatureAlgorithm(), generateSerialNumber(), validAfter, expireDate,
				issuerCertificate.getBasicConstraints() - 1);
	}

	/**
	 * Generates a X.509 certificate version 3, signed by CA issuer.
	 *
	 * @param csr                certificate sign request
	 * @param issuerCertificate  CA certificate
	 * @param issuerPrivateKey   key used to sign
	 * @param signatureAlgorithm the name of the signature algorithm used
	 * @param serialNumber       the serial number for the certificate
	 * @param validAfter         the date and time after which the certificate is
	 *                           valid
	 * @param expireDate         the date and time after which the certificate
	 *                           expires
	 * @param pathLenConstraint  specifies the depth of the certification path (if
	 *                           >-1 create as certificate authority)
	 * @return the new certificate
	 * @throws CertificateLtException <br/>
	 *                                +Cause IOException on other errors <br/>
	 *                                +Cause CertificateException on invalid
	 *                                attributes or encoding errors <br/>
	 *                                +Cause NoSuchAlgorithmException on unsupported
	 *                                signature algorithms <br/>
	 *                                +Cause SignatureException on signature errors
	 *                                <br/>
	 *                                +Cause NoSuchProviderException if there's no
	 *                                default provider <br/>
	 *                                +Cause InvalidKeyException on incorrect key
	 *                                <br/>
	 *                                +Cause CertificateChainLtException if issuer
	 *                                is not a CA or valid date interval is invalid
	 *                                for the issuer or pathLenConstraint is invalid
	 */
	public static X509Certificate generateLinkedX509CertificateV3(final PKCS10 csr,
			final X509Certificate issuerCertificate, final PrivateKey issuerPrivateKey, final String signatureAlgorithm,
			final BigInteger serialNumber, final Date validAfter, final Date expireDate, final int pathLenConstraint)
			throws CertificateLtException {
		if (!isCA(issuerCertificate)) {
			throw new CertificateLtException(new CertificateChainLtException("Issuer has to be a CA"));
		}
		if (pathLenConstraint > -1 && issuerCertificate.getBasicConstraints() <= pathLenConstraint) {
			throw new CertificateLtException(new CertificateChainLtException("Invalid path length"));
		}
		if (issuerCertificate.getNotBefore().compareTo(validAfter) > 0
				|| issuerCertificate.getNotAfter().compareTo(expireDate) < 0) {
			throw new CertificateLtException(
					new CertificateChainLtException("New validation interval date is out of CA interval"));
		}
		try {
			return generateX509CertificateV3(new X500Name(issuerCertificate.getSubjectX500Principal().getName()),
					signatureAlgorithm, issuerPrivateKey, csr, serialNumber, validAfter, expireDate, pathLenConstraint);
		} catch (final IOException e) {
			throw new CertificateLtException(e);
		}
	}

}

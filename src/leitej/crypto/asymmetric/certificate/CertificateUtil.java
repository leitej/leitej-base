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
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentVerifierProviderBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;

import leitej.crypto.Cryptography;
import leitej.crypto.ProviderEnum;
import leitej.crypto.asymmetric.signature.SignatureEnum;
import leitej.exception.CertificateChainLtException;
import leitej.exception.CertificateLtException;
import leitej.log.Logger;
import leitej.util.DateUtil;
import leitej.util.HexaUtil;

/**
 *
 * @author Julio Leite
 */
public final class CertificateUtil {

	private static final Logger LOG = Logger.getInstance();

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

	/**
	 * Generates unique alias per issuer and serialNumber.
	 *
	 * @param certificate
	 * @return alias
	 * @throws IOException on encoding error
	 */
	public static final String getAlias(final Certificate certificate) throws IOException {
		return certificate.getSerialNumber().getValue().toString(Character.MAX_RADIX)
				+ HexaUtil.toHex(certificate.getIssuer().getEncoded());
	}

	/**
	 * Generates unique alias per issuer and serialNumber.
	 *
	 * @param certificate
	 * @return alias
	 * @throws IOException on encoding error
	 */
	public static final String getAlias(final X509CertificateHolder certificate) throws IOException {
		return certificate.getSerialNumber().toString(Character.MAX_RADIX)
				+ HexaUtil.toHex(certificate.getIssuer().getEncoded());
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
	 * Converters
	 */

	/**
	 * Convert.
	 *
	 * @param certificate
	 * @return
	 */
	public static X509CertificateHolder convert(final Certificate certificate) {
		return new X509CertificateHolder(certificate);
	}

	/**
	 * Convert.
	 *
	 * @param certificate
	 * @return
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateEncodingException if an
	 *                                encoding error occurs<br/>
	 *                                +Cause IOException in the event of corrupted
	 *                                data, or an incorrect structure
	 */
	public static X509CertificateHolder convert(final java.security.cert.Certificate certificate)
			throws CertificateLtException {
		try {
			return new X509CertificateHolder(certificate.getEncoded());
		} catch (CertificateEncodingException | IOException e) {
			throw new CertificateLtException(e);
		}
	}

	/**
	 * Convert.
	 *
	 * @param certificates
	 * @return
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateEncodingException if an
	 *                                encoding error occurs<br/>
	 *                                +Cause IOException in the event of corrupted
	 *                                data, or an incorrect structure
	 */
	public static X509CertificateHolder[] convert(final java.security.cert.Certificate[] certificates)
			throws CertificateLtException {
		try {
			final X509CertificateHolder[] result = new X509CertificateHolder[certificates.length];
			for (int i = 0; i < result.length; i++) {
				result[i] = new X509CertificateHolder(certificates[i].getEncoded());
			}
			return result;
		} catch (CertificateEncodingException | IOException e) {
			throw new CertificateLtException(e);
		}
	}

	/**
	 * Convert.
	 *
	 * @param certificate
	 * @return
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateException if the conversion
	 *                                is unable to be made
	 */
	public static X509Certificate convert(final X509CertificateHolder certificate) throws CertificateLtException {
		try {
			return new JcaX509CertificateConverter().setProvider(ProviderEnum.BC.getName()).getCertificate(certificate);
		} catch (final CertificateException e) {
			throw new CertificateLtException(e);
		}
	}

	/**
	 * Convert.
	 *
	 * @param certificates
	 * @return
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateException if the conversion
	 *                                is unable to be made
	 */
	public static X509Certificate[] convert(final X509CertificateHolder[] certificates) throws CertificateLtException {
		try {
			final JcaX509CertificateConverter converter = new JcaX509CertificateConverter()
					.setProvider(ProviderEnum.BC.getName());
			final X509Certificate[] result = new X509Certificate[certificates.length];
			for (int i = 0; i < result.length; i++) {
				result[i] = converter.getCertificate(certificates[i]);
			}
			return result;
		} catch (final CertificateException e) {
			throw new CertificateLtException(e);
		}
	}

	/**
	 * Convert.
	 *
	 * @param publicKey
	 * @return
	 * @throws CertificateLtException <br/>
	 *                                +Cause IOException on an error decoding the
	 *                                key
	 */
	public static AsymmetricKeyParameter convert(final PublicKey publicKey) throws CertificateLtException {
		try {
			return PublicKeyFactory.createKey(publicKey.getEncoded());
		} catch (final IOException e) {
			throw new CertificateLtException(e);
		}
	}

	/*
	 * Extract
	 */

	/**
	 * Extract public key from X509CertificateHolder.
	 *
	 * @param certificate
	 * @return
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateException if the conversion
	 *                                is unable to be made
	 */
	public static PublicKey extract(final X509CertificateHolder certificate) throws CertificateLtException {
		return convert(certificate).getPublicKey();
	}

	/*
	 * Generate X509 certificates
	 */

	/**
	 * Generates a X.509 certificate version 3 - self signed (root for a chain of
	 * certificates).
	 *
	 * @param issuerDN          common name
	 * @param keys
	 * @param validAfter        the date and time after which the certificate is
	 *                          valid
	 * @param expireDate        the date and time after which the certificate
	 *                          expires
	 * @param pathLenConstraint specifies the depth of the certification path (if
	 *                          &#62;-1 create as certificate authority)
	 * @return generated certificate
	 * @throws CertificateLtException <br/>
	 *                                +Cause OperatorCreationException
	 */
	public static X509CertificateHolder generateX509CertificateV3SelfSignedRootChain(final X500Name issuerDN,
			final KeyPair keys, final Date validAfter, final Date expireDate, final int pathLenConstraint)
			throws CertificateLtException {
		return generateX509CertificateV3(issuerDN, Cryptography.getDefaultCertificateSignatureAlgorithm(),
				keys.getPrivate(), generateCSR(keys.getPublic(), issuerDN,
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
	 *                           &#62;-1 create as certificate authority)
	 * @return generated certificate
	 * @throws CertificateLtException <br/>
	 *                                +Cause OperatorCreationException
	 */
	private static X509CertificateHolder generateX509CertificateV3(final X500Name issuerDN,
			final SignatureEnum signatureAlgorithm, final PrivateKey issuerPrivateKey, final CertificationRequest csr,
			final BigInteger serialNumber, final Date validAfter, final Date expireDate, final int pathLenConstraint)
			throws CertificateLtException {
		try {
			// The issuer information (CA) and the serial number serve to uniquely identify
			// the certificate
			final X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(issuerDN, serialNumber,
					validAfter, expireDate, csr.getCertificationRequestInfo().getSubject(),
					csr.getCertificationRequestInfo().getSubjectPublicKeyInfo());
			// x509v3 extensions
			if (pathLenConstraint > -1) {
				certificateBuilder.addExtension(Extension.keyUsage, true,
						new X509KeyUsage(X509KeyUsage.keyCertSign | X509KeyUsage.cRLSign));
			} else {
				certificateBuilder.addExtension(Extension.keyUsage, true, new X509KeyUsage(
						X509KeyUsage.keyEncipherment | X509KeyUsage.digitalSignature | X509KeyUsage.nonRepudiation));
			}
			// basic constraints
			if (pathLenConstraint > -1) {
				certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(pathLenConstraint));
			} else {
				certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
			}
			// create certificate
			final ContentSigner signer = new JcaContentSignerBuilder(signatureAlgorithm.getName())
					.setProvider(ProviderEnum.BC.getName()).build(issuerPrivateKey);
			return certificateBuilder.build(signer);
		} catch (final OperatorCreationException | CertIOException e) {
			throw new CertificateLtException(e);
		}
	}

	/*
	 * Generate Certificate Sign Request
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
	 *                                +Cause OperatorCreationException
	 */
	public static CertificationRequest generateCSR(final PublicKey publicKey, final X500Name distinguishedName,
			final SignatureEnum signatureAlgorithm, final PrivateKey privateKey) throws CertificateLtException {
		try {
			final PKCS10CertificationRequestBuilder csrBuilder = new PKCS10CertificationRequestBuilder(distinguishedName,
					SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(PublicKeyFactory.createKey(publicKey.getEncoded())));
			// create certificate request
			final ContentSigner signer = new JcaContentSignerBuilder(signatureAlgorithm.getName())
					.setProvider(ProviderEnum.BC.getName()).build(privateKey);
			return csrBuilder.build(signer).toASN1Structure();
		} catch (final OperatorCreationException | IOException e) {
			throw new CertificateLtException(e);
		}
	}

	/*
	 * Verify
	 */

	/**
	 * Verifies that the certificate was signed using the private key that
	 * corresponds to the specified public key. <br/>
	 * Also verifies the current date and time are within the validity period given
	 * in the certificate.
	 *
	 * @param certificate X509
	 * @param keyTrust    the trusted public key used to carry out the verification
	 * @return true if is valid
	 * @throws CertificateLtException <br/>
	 *                                +Cause OperatorCreationException <br/>
	 *                                +Cause CertException - if the signature cannot
	 *                                be processed or is inappropriate
	 */
	public static boolean isValidX509(final X509CertificateHolder certificate, final AsymmetricKeyParameter keyTrust)
			throws CertificateLtException {
		try {
			return certificate.isValidOn(DateUtil.now()) && certificate.isSignatureValid(
					new BcRSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder()).build(keyTrust));
		} catch (OperatorCreationException | CertException e) {
			throw new CertificateLtException(e);
		}
	}

	/**
	 * Verifies that the certificate was signed using the private key that
	 * corresponds to the specified public key from issuer certificate. <br/>
	 * Also verifies the current date and time are within the validity period given
	 * in the certificate.
	 *
	 * @param certificate       X509
	 * @param issuerCertificate the trusted public key used to carry out the
	 *                          verification
	 * @return true if is valid
	 * @throws CertificateLtException <br/>
	 *                                +Cause OperatorCreationException <br/>
	 *                                +Cause CertException - if the signature cannot
	 *                                be processed or is inappropriate
	 */
	public static boolean isValidX509(final X509CertificateHolder certificate,
			final X509CertificateHolder issuerCertificate) throws CertificateLtException {
		try {
			return certificate.isValidOn(DateUtil.now()) && certificate
					.isSignatureValid(new BcRSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder())
							.build(issuerCertificate));
		} catch (OperatorCreationException | CertException e) {
			throw new CertificateLtException(e);
		}
	}

	private static boolean isCA(final X509CertificateHolder certificate) {
		final BasicConstraints bConstraints = BasicConstraints.fromExtensions(certificate.getExtensions());
		if (bConstraints == null) {
			return false;
		}
		final BigInteger pathLenConstraint = bConstraints.getPathLenConstraint();
		if (pathLenConstraint == null) {
			return false;
		}
		final KeyUsage kUsage = KeyUsage.fromExtensions(certificate.getExtensions());
		if (kUsage == null) {
			return false;
		}
		return pathLenConstraint.intValueExact() > -1 && kUsage.hasUsages(X509KeyUsage.keyCertSign);
	}

	/**
	 * Verifies that the root certificate in the chain is self-signed.<br/>
	 * And all consecutive certificates were signed using the private key that
	 * corresponds to the public key in anterior certificate.<br/>
	 * Also verifies if the current date and time are within the validity period
	 * given in all the certificates. <br/>
	 *
	 * @param chain with the certificates to verify
	 * @return true if is valid
	 * @throws CertificateLtException <br/>
	 *                                +Cause OperatorCreationException <br/>
	 *                                +Cause CertException - if the signature cannot
	 *                                be processed or is inappropriate
	 */
	public static boolean isValidChain(final X509CertificateHolder[] chain) throws CertificateLtException {
		if (chain.length > 0) {
			final X509CertificateHolder rootCertificate = chain[chain.length - 1];
			X509CertificateHolder issuerCertificate = rootCertificate;
			int issuerPathLength = Integer.MAX_VALUE;
			X509CertificateHolder certificate;
			boolean isCA;
			BigInteger pathLenConstraint;
			for (int i = chain.length - 1; i >= 0; i--) {
				certificate = chain[i];
				if (!isValidX509(certificate, issuerCertificate)) {
					LOG.debug("Invalid certificate with serialNumber: #0", certificate.getSerialNumber());
					return false;
				}
				isCA = isCA(certificate);
				if (i != 0 && !isCA) {
					LOG.debug("Expected have root and intermediates as CA");
					return false;
				}
				if (isCA) {
					pathLenConstraint = BasicConstraints.fromExtensions(certificate.getExtensions()).getPathLenConstraint();
					if (pathLenConstraint == null || pathLenConstraint.intValueExact() >= issuerPathLength) {
						LOG.debug("Invalid sequence of pathLength");
						return false;
					}
					issuerPathLength = pathLenConstraint.intValueExact();
				}
				issuerCertificate = certificate;
			}
			return true;
		} else {
			LOG.debug("Empty chain");
			return false;
		}
	}

	/*
	 * Certificate chain
	 */

	private static X509CertificateHolder[] addEndCertificate(final X509CertificateHolder certificate,
			final X509CertificateHolder[] chain) {
		final X509CertificateHolder[] result = new X509CertificateHolder[chain.length + 1];
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
	 *                                +Cause CertificateChainLtException if issuer
	 *                                is not a CA or valid date interval is invalid
	 *                                for the issuer or pathLenConstraint is invalid
	 *                                <br/>
	 *                                +Cause OperatorCreationException
	 */
	public static X509CertificateHolder[] addLink(final X509CertificateHolder[] chain, final PrivateKey chain0PrivateKey,
			final Date validAfter, final Date expireDate, final CertificationRequest requestLink)
			throws CertificateLtException {
		final X509CertificateHolder newCertificate = generateLinkedX509CertificateV3(requestLink, chain[0],
				chain0PrivateKey, validAfter, expireDate);
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
	 *                           &#62;-1 create as certificate authority)
	 * @return a new array with the new link
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateChainLtException if issuer
	 *                                is not a CA or valid date interval is invalid
	 *                                for the issuer or pathLenConstraint is invalid
	 *                                <br/>
	 *                                +Cause OperatorCreationException
	 */
	public static X509CertificateHolder[] addLink(final X509CertificateHolder[] chain, final PrivateKey chain0PrivateKey,
			final SignatureEnum signatureAlgorithm, final BigInteger serialNumber, final Date validAfter,
			final Date expireDate, final CertificationRequest requestLink, final int pathLenConstraint)
			throws CertificateLtException {
		final X509CertificateHolder newCertificate = generateLinkedX509CertificateV3(requestLink, chain[0],
				chain0PrivateKey, signatureAlgorithm, serialNumber, validAfter, expireDate, pathLenConstraint);
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
	 *                                +Cause CertificateChainLtException if issuer
	 *                                is not a CA or valid date interval is invalid
	 *                                for the issuer or pathLenConstraint is invalid
	 *                                <br/>
	 *                                +Cause OperatorCreationException
	 */
	public static X509CertificateHolder generateLinkedX509CertificateV3(final CertificationRequest csr,
			final X509CertificateHolder issuerCertificate, final PrivateKey issuerPrivateKey, final Date validAfter,
			final Date expireDate) throws CertificateLtException {
		return generateLinkedX509CertificateV3(csr, issuerCertificate, issuerPrivateKey,
				Cryptography.getDefaultCertificateSignatureAlgorithm(), generateSerialNumber(), validAfter, expireDate,
				BasicConstraints.fromExtensions(issuerCertificate.getExtensions()).getPathLenConstraint().intValueExact() - 1);
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
	 *                           &#62;-1 create as certificate authority)
	 * @return the new certificate
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateChainLtException if issuer
	 *                                is not a CA or valid date interval is invalid
	 *                                for the issuer or pathLenConstraint is invalid
	 *                                <br/>
	 *                                +Cause OperatorCreationException
	 */
	public static X509CertificateHolder generateLinkedX509CertificateV3(final CertificationRequest csr,
			final X509CertificateHolder issuerCertificate, final PrivateKey issuerPrivateKey,
			final SignatureEnum signatureAlgorithm, final BigInteger serialNumber, final Date validAfter,
			final Date expireDate, final int pathLenConstraint) throws CertificateLtException {
		if (!isCA(issuerCertificate)) {
			throw new CertificateLtException(new CertificateChainLtException("Issuer has to be a CA"));
		}
		if (pathLenConstraint > -1 && BasicConstraints.fromExtensions(issuerCertificate.getExtensions())
				.getPathLenConstraint().intValueExact() <= pathLenConstraint) {
			throw new CertificateLtException(new CertificateChainLtException("Invalid path length"));
		}
		if (issuerCertificate.getNotBefore().compareTo(validAfter) > 0
				|| issuerCertificate.getNotAfter().compareTo(expireDate) < 0) {
			throw new CertificateLtException(
					new CertificateChainLtException("New validation interval date is out of CA interval"));
		}
		return generateX509CertificateV3(issuerCertificate.getSubject(), signatureAlgorithm, issuerPrivateKey, csr,
				serialNumber, validAfter, expireDate, pathLenConstraint);
	}

}

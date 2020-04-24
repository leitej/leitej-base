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

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import leitej.crypto.ProviderEnum;
import leitej.crypto.asymmetric.signature.SignatureEnum;
import leitej.crypto.exception.CertificateLtException;
import leitej.util.DateUtil;

/**
 *
 * @author Julio Leite
 */
public final class CertificateUtil {

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

	private CertificateUtil() {
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

	@SuppressWarnings("unused")
	@Deprecated
	/**
	 * Generates a X.509 certificate version 1 - self signed (root for a chain of
	 * certificates).
	 *
	 * @param name
	 * @param expirationDate
	 * @param keyPair
	 * @return generated certificate
	 * @throws CertificateLtException if some exception occurred at invocation to
	 *                                generate the certificate <br/>
	 *                                +Cause CertificateEncodingException <br/>
	 *                                +Cause InvalidKeyException <br/>
	 *                                +Cause IllegalStateException <br/>
	 *                                +Cause NoSuchProviderException <br/>
	 *                                +Cause NoSuchAlgorithmException <br/>
	 *                                +Cause SignatureException
	 */
	private static X509Certificate generateX509CertificateV1SelfSignedRoot(final String issuer, final KeyPair keys,
			final Date expireDate) throws CertificateLtException {
		final X509V1CertificateGenerator certifiacteGenerator = new X509V1CertificateGenerator();
		certifiacteGenerator.setSerialNumber(generateSerialNumber());
		certifiacteGenerator.setIssuerDN(new X500Principal("CN=".concat(issuer)));
		certifiacteGenerator.setNotBefore(DateUtil.now());
		certifiacteGenerator.setNotAfter(expireDate);
		certifiacteGenerator.setSubjectDN(new X500Principal("CN=".concat(issuer)));
		certifiacteGenerator.setPublicKey(keys.getPublic());
		certifiacteGenerator.setSignatureAlgorithm(SignatureEnum.SHA512WithRSAAndMGF1.getName());
		try {
			return certifiacteGenerator.generate(keys.getPrivate(), ProviderEnum.BC.getName());
		} catch (final CertificateEncodingException e) {
			throw new CertificateLtException(e);
		} catch (final InvalidKeyException e) {
			throw new CertificateLtException(e);
		} catch (final IllegalStateException e) {
			throw new CertificateLtException(e);
		} catch (final NoSuchProviderException e) {
			throw new CertificateLtException(e);
		} catch (final NoSuchAlgorithmException e) {
			throw new CertificateLtException(e);
		} catch (final SignatureException e) {
			throw new CertificateLtException(e);
		}
	}

	/**
	 * Generates a X.509 certificate version 3 - self signed (root for a chain of
	 * certificates). <br/>
	 * <br/>
	 * Any value passed to <code>pathLengthCA</code> positive will generate a
	 * certificate authority that can have <code>pathLengthCA</code> links, zero
	 * value or negative will generate an end-entity (not CA).
	 *
	 * @param issuer
	 * @param keys
	 * @param expireDate
	 * @param pathLengthCA defines in the extension information as critical if the
	 *                     generated certificate can be used as certification
	 *                     authority and how many links can be attached to it in a
	 *                     chain
	 * @return generated certificate
	 * @throws CertificateLtException if some exception occurred at invocation to
	 *                                generate the certificate <br/>
	 *                                +Cause CertificateEncodingException <br/>
	 *                                +Cause InvalidKeyException <br/>
	 *                                +Cause IllegalStateException <br/>
	 *                                +Cause NoSuchProviderException <br/>
	 *                                +Cause NoSuchAlgorithmException <br/>
	 *                                +Cause SignatureException
	 */
	public static X509Certificate generateX509CertificateV3SelfSigned(final String issuer, final KeyPair keys,
			final Date expireDate, final int pathLengthCA) throws CertificateLtException {
		return generateX509CertificateV3SelfSigned(issuer, keys, SignatureEnum.SHA512WithRSAAndMGF1,
				generateSerialNumber(), DateUtil.now(), expireDate, pathLengthCA);
	}

	/**
	 * Generates a X.509 certificate version 3 - self signed (root for a chain of
	 * certificates). <br/>
	 * <br/>
	 * Any value passed to <code>pathLengthCA</code> positive will generate a
	 * certificate authority that can have <code>pathLengthCA</code> links, zero
	 * value or negative will generate an end-entity (not CA).
	 *
	 * @param issuer
	 * @param keys
	 * @param signatureAlgorithm
	 * @param serialNumber
	 * @param validAfter
	 * @param expireDate
	 * @param pathLengthCA       defines in the extension information as critical if
	 *                           the generated certificate can be used as
	 *                           certification authority and how many links can be
	 *                           attached to it in a chain
	 * @return generated certificate
	 * @throws CertificateLtException if some exception occurred at invocation to
	 *                                generate the certificate <br/>
	 *                                +Cause CertificateEncodingException <br/>
	 *                                +Cause InvalidKeyException <br/>
	 *                                +Cause IllegalStateException <br/>
	 *                                +Cause NoSuchProviderException <br/>
	 *                                +Cause NoSuchAlgorithmException <br/>
	 *                                +Cause SignatureException
	 */
	public static X509Certificate generateX509CertificateV3SelfSigned(final String issuer, final KeyPair keys,
			final SignatureEnum signatureAlgorithm, final BigInteger serialNumber, final Date validAfter,
			final Date expireDate, final int pathLengthCA) throws CertificateLtException {
		return generateX509CertificateV3(new X500Principal("CN=".concat(issuer)), keys.getPrivate(), null,
				signatureAlgorithm, serialNumber, validAfter, expireDate, issuer, keys.getPublic(), null, pathLengthCA);
	}

	/**
	 * Generates a X.509 certificate version 3. <br/>
	 * <br/>
	 * Any value passed to <code>pathLengthCA</code> positive will generate a
	 * certificate authority that can have <code>pathLengthCA</code> links, zero
	 * value or negative will generate an end-entity (not CA).
	 *
	 * @param issuer
	 * @param issuerPrivateKey
	 * @param issuerAlternativeName  can be null, or add some extension information
	 *                               for the issuer like his email contact
	 * @param signatureAlgorithm
	 * @param serialNumber
	 * @param validAfter
	 * @param expireDate
	 * @param subject
	 * @param subjectPublicKey
	 * @param subjectAlternativeName can be null, or add some extension information
	 *                               for the subject like his email contact
	 * @param pathLengthCA           defines in the extension information as
	 *                               critical if the generated certificate can be
	 *                               used as certification authority and how many
	 *                               links can be attached to it in a chain
	 * @return generated certificate
	 * @throws CertificateLtException if some exception occurred at invocation to
	 *                                generate the certificate <br/>
	 *                                +Cause CertificateEncodingException <br/>
	 *                                +Cause InvalidKeyException <br/>
	 *                                +Cause IllegalStateException <br/>
	 *                                +Cause NoSuchProviderException <br/>
	 *                                +Cause NoSuchAlgorithmException <br/>
	 *                                +Cause SignatureException
	 */
	private static X509Certificate generateX509CertificateV3(final X500Principal issuer,
			final PrivateKey issuerPrivateKey, final String issuerAlternativeName,
			final SignatureEnum signatureAlgorithm, final BigInteger serialNumber, final Date validAfter,
			final Date expireDate, final String subject, final PublicKey subjectPublicKey,
			final String subjectAlternativeName, final int pathLengthCA) throws CertificateLtException {
		final X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();

		certificateGenerator.setSerialNumber(serialNumber);
		// The issuer information (CA) and the serial number serve to uniquely identify
		// the certificate
		certificateGenerator.setIssuerDN(issuer);

		certificateGenerator.setNotBefore(validAfter);
		certificateGenerator.setNotAfter(expireDate);
		// represents the owner of the public key in the generated certificate
		certificateGenerator.setSubjectDN(new X500Principal("CN=".concat(subject)));
		certificateGenerator.setPublicKey(subjectPublicKey);
		certificateGenerator.setSignatureAlgorithm(signatureAlgorithm.getName());

		// extensions
		if (pathLengthCA > 0) {
			certificateGenerator.addExtension(X509Extensions.BasicConstraints, true,
					new BasicConstraints(pathLengthCA - 1));
		} else {
			certificateGenerator.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
		}
		if (issuerAlternativeName != null) {
			certificateGenerator.addExtension(X509Extensions.IssuerAlternativeName, false,
					new GeneralNames(new GeneralName(GeneralName.rfc822Name, issuerAlternativeName)));
		}
		if (subjectAlternativeName != null) {
			certificateGenerator.addExtension(X509Extensions.SubjectAlternativeName, false,
					new GeneralNames(new GeneralName(GeneralName.rfc822Name, subjectAlternativeName)));
		}

		try {
			return certificateGenerator.generate(issuerPrivateKey, ProviderEnum.BC.getName());
		} catch (final CertificateEncodingException e) {
			throw new CertificateLtException(e);
		} catch (final InvalidKeyException e) {
			throw new CertificateLtException(e);
		} catch (final IllegalStateException e) {
			throw new CertificateLtException(e);
		} catch (final NoSuchProviderException e) {
			throw new CertificateLtException(e);
		} catch (final NoSuchAlgorithmException e) {
			throw new CertificateLtException(e);
		} catch (final SignatureException e) {
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
	 * @param pbcKeyTrust the PublicKey used to carry out the verification
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
			certificate.verify(pbcKeyTrust, ProviderEnum.BC.getName());
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
	static void verifyTypeX509(final Certificate certificate) throws CertificateLtException {
		if (!X509Certificate.class.isInstance(certificate) || !TypeEnum.X509.getName().equals(certificate.getType())) {
			throw new CertificateLtException(new CertificateException(), "Invalid type: #0",
					certificate.getType());
		}
	}

}

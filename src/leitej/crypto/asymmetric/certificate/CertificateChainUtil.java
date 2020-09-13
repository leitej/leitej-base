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
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import leitej.crypto.ConstantCrypto;
import leitej.crypto.ProviderEnum;
import leitej.crypto.asymmetric.signature.SignatureEnum;
import leitej.exception.CertificateAuthorityLtException;
import leitej.exception.CertificateChainLtException;
import leitej.exception.CertificateLtException;
import leitej.exception.CertificationRequestLtException;

/**
 *
 * @author Julio Leite
 */
public final class CertificateChainUtil {

	private CertificateChainUtil() {
	}

	/*
	 * Generate request
	 */

	/**
	 * Creates a PKCS10 certification request.
	 *
	 * @param subject
	 * @param keyPair
	 * @param subjectAlternativeName
	 * @param signatureAlgorithm
	 * @return
	 * @throws CertificationRequestLtException if some exception occurred at
	 *                                         invocation to generate the
	 *                                         certification request <br/>
	 *                                         +Cause InvalidKeyException <br/>
	 *                                         +Cause NoSuchAlgorithmException <br/>
	 *                                         +Cause NoSuchProviderException <br/>
	 *                                         +Cause SignatureException
	 */
	public static PKCS10CertificationRequest generatePKCS10CertificationRequest(final String subject,
			final KeyPair keyPair, final String subjectAlternativeName, final SignatureEnum signatureAlgorithm)
			throws CertificationRequestLtException {
		ASN1Set requestExtension;
		if (subjectAlternativeName == null) {
			requestExtension = null;
		} else {
			// create a SubjectAlternativeName extension value
			final GeneralNames subjectAltName = new GeneralNames(
					new GeneralName(GeneralName.rfc822Name, subjectAlternativeName));
			// create the extensions object and add it as an attribute
			final Vector<DERObjectIdentifier> oids = new Vector<>();
			final Vector<X509Extension> values = new Vector<>();
			oids.add(X509Extensions.SubjectAlternativeName);
			values.add(new X509Extension(false, new DEROctetString(subjectAltName)));
			final X509Extensions extensions = new X509Extensions(oids, values);
			final Attribute attribute = new Attribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
					new DERSet(extensions));
			requestExtension = new DERSet(attribute);
		}
		try {
			return new PKCS10CertificationRequest(signatureAlgorithm.getName(),
					new X500Principal("CN=".concat(subject)), keyPair.getPublic(), requestExtension,
					keyPair.getPrivate());
		} catch (final InvalidKeyException e) {
			throw new CertificationRequestLtException(e);
		} catch (final NoSuchAlgorithmException e) {
			throw new CertificationRequestLtException(e);
		} catch (final NoSuchProviderException e) {
			throw new CertificationRequestLtException(e);
		} catch (final SignatureException e) {
			throw new CertificationRequestLtException(e);
		}
	}

	/*
	 * Certificate chain
	 */

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
	 * @return a new array with the new link
	 * @throws CertificateAuthorityLtException <br/>
	 *                                         +Cause CertificateChainLtException if
	 *                                         is not a certificate authority <br/>
	 *                                         +Cause CertificateExpiredException if
	 *                                         the certificate has expired <br/>
	 *                                         +Cause
	 *                                         CertificateNotYetValidException if
	 *                                         the certificate is not yet valid
	 * @throws CertificationRequestLtException if some irregular information found
	 *                                         in the request <br/>
	 *                                         +Cause InvalidKeyException <br/>
	 *                                         +Cause NoSuchAlgorithmException <br/>
	 *                                         +Cause NoSuchProviderException <br/>
	 *                                         +Cause SignatureException
	 * @throws CertificateLtException          if some exception occurred at
	 *                                         invocation to generate the
	 *                                         certificate <br/>
	 *                                         +Cause CertificateEncodingException
	 *                                         <br/>
	 *                                         +Cause InvalidKeyException <br/>
	 *                                         +Cause IllegalStateException <br/>
	 *                                         +Cause NoSuchProviderException <br/>
	 *                                         +Cause NoSuchAlgorithmException <br/>
	 *                                         +Cause SignatureException
	 */
	public static X509Certificate[] addLink(final X509Certificate[] chain, final PrivateKey chain0PrivateKey,
			final SignatureEnum signatureAlgorithm, final BigInteger serialNumber, final Date validAfter,
			final Date expireDate, final PKCS10CertificationRequest requestLink)
			throws CertificateAuthorityLtException, CertificationRequestLtException, CertificateLtException {
		final X509Certificate newCertificate = generateLinkedX509CertificateV3(requestLink, chain[0], chain0PrivateKey,
				signatureAlgorithm, serialNumber, validAfter, expireDate);
		return addEndCertificate(newCertificate, chain);
	}

	/**
	 *
	 * @param request
	 * @param issuerCertificate
	 * @param issuerPrivateKey
	 * @param signatureAlgorithm
	 * @param serialNumber
	 * @param validAfter
	 * @param expireDate
	 * @return the new certificate
	 * @throws CertificateAuthorityLtException <br/>
	 *                                         +Cause CertificateParsingException if
	 *                                         a extension cannot be decoded <br/>
	 *                                         +Cause CertificateChainLtException if
	 *                                         is not a certificate authority <br/>
	 *                                         +Cause CertificateExpiredException if
	 *                                         the certificate has expired <br/>
	 *                                         +Cause
	 *                                         CertificateNotYetValidException if
	 *                                         the certificate is not yet valid
	 * @throws CertificationRequestLtException if some irregular information found
	 *                                         in the request <br/>
	 *                                         +Cause InvalidKeyException <br/>
	 *                                         +Cause NoSuchAlgorithmException <br/>
	 *                                         +Cause NoSuchProviderException <br/>
	 *                                         +Cause SignatureException
	 * @throws CertificateLtException          if some exception occurred at
	 *                                         invocation to generate the
	 *                                         certificate <br/>
	 *                                         +Cause CertificateEncodingException
	 *                                         <br/>
	 *                                         +Cause InvalidKeyException <br/>
	 *                                         +Cause IllegalStateException <br/>
	 *                                         +Cause NoSuchProviderException <br/>
	 *                                         +Cause NoSuchAlgorithmException <br/>
	 *                                         +Cause SignatureException
	 */
	private static X509Certificate generateLinkedX509CertificateV3(final PKCS10CertificationRequest request,
			final X509Certificate issuerCertificate, final PrivateKey issuerPrivateKey,
			final SignatureEnum signatureAlgorithm, final BigInteger serialNumber, final Date validAfter,
			final Date expireDate)
			throws CertificateAuthorityLtException, CertificationRequestLtException, CertificateLtException {
		verifyCertificateAuthority(issuerCertificate, validAfter, expireDate);
		verifyPKCS10CertificationRequest(request);

		final X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();

		// Set the information for the new certificate
		certificateGenerator.setSerialNumber(serialNumber);
		certificateGenerator.setIssuerDN(issuerCertificate.getSubjectX500Principal());

		certificateGenerator.setNotBefore(validAfter);
		certificateGenerator.setNotAfter(expireDate);
		// represents the owner of the public key in the generated certificate
		certificateGenerator.setSubjectDN(request.getCertificationRequestInfo().getSubject());
		try {
			certificateGenerator.setPublicKey(request.getPublicKey(ProviderEnum.BC.getName()));
		} catch (final InvalidKeyException e) {
			throw new CertificationRequestLtException(e);
		} catch (final NoSuchAlgorithmException e) {
			throw new CertificationRequestLtException(e);
		} catch (final NoSuchProviderException e) {
			throw new CertificationRequestLtException(e);
		}
		certificateGenerator.setSignatureAlgorithm(signatureAlgorithm.getName());

		// extensions
		// add basic constrain
		int pathLengthCA = -1;
		final Set<String> criticalExtensionOIDs = issuerCertificate.getCriticalExtensionOIDs();
		if (criticalExtensionOIDs != null) {
			for (final String oid : criticalExtensionOIDs) {
				if (ConstantCrypto.BASIC_CONSTRAIN_OID.equals(oid)) {
					pathLengthCA = issuerCertificate.getBasicConstraints();
				}
			}
		}
		if (pathLengthCA == -1) {
			throw new CertificateAuthorityLtException(new CertificateChainLtException());
		}
		if (pathLengthCA > 0) {
			certificateGenerator.addExtension(X509Extensions.BasicConstraints, true,
					new BasicConstraints(pathLengthCA - 1));
		} else {
			certificateGenerator.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
		}
		// add issuer alternative names
		try {
			final Collection<List<?>> issuerAlternativeNames = issuerCertificate.getSubjectAlternativeNames();
			if (issuerAlternativeNames != null) {
				Integer tmp;
				String issuerAlternativeName;
				for (final List<?> value : issuerAlternativeNames) {
					tmp = Integer.class.cast(value.get(0));
					// only pass to the new certificate the issuerAlternativeName with type
					// rfc822Name
					if (tmp != null && tmp == GeneralName.rfc822Name) {
						issuerAlternativeName = String.class.cast(value.get(1));
						if (issuerAlternativeName != null) {
							certificateGenerator.addExtension(X509Extensions.IssuerAlternativeName, false,
									new GeneralNames(new GeneralName(GeneralName.rfc822Name, issuerAlternativeName)));
						}
					}
				}
			}
		} catch (final CertificateParsingException e) {
			throw new CertificateAuthorityLtException(e);
		}
		// add the extensions from request attribute
		final ASN1Set attributes = request.getCertificationRequestInfo().getAttributes();
		if (attributes != null) {
			Attribute attribute;
			for (int i = 0; i < attributes.size(); i++) {
				attribute = Attribute.getInstance(attributes.getObjectAt(i));
				// unique type of extensions allowed
				if (attribute.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
					if (attribute.getAttrValues().size() != 0) {
						final X509Extensions extensions = X509Extensions
								.getInstance(attribute.getAttrValues().getObjectAt(0));
						final Enumeration<?> oidEnumeration = extensions.oids();
						DERObjectIdentifier oid;
						X509Extension extension;
						while (oidEnumeration.hasMoreElements()) {
							oid = DERObjectIdentifier.class.cast(oidEnumeration.nextElement());
							extension = extensions.getExtension(oid);
							if (extension != null) {
								certificateGenerator.addExtension(oid, extension.isCritical(),
										extension.getValue().getOctets());
							}
						}
					}
				}
			}
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

	private static X509Certificate[] addEndCertificate(final X509Certificate certificate,
			final X509Certificate[] chain) {
		final X509Certificate[] result = new X509Certificate[chain.length + 1];
		result[0] = certificate;
		for (int i = 0; i < chain.length; i++) {
			result[i + 1] = chain[i];
		}
		return result;
	}

	/*
	 * Verify certificate authority
	 */

	/**
	 * Verifies if is a certificate authority and if can sign a new certificate with
	 * the specified dates in parameters.
	 *
	 * @param certificate
	 * @throws CertificateAuthorityLtException <br/>
	 *                                         +Cause CertificateChainLtException if
	 *                                         is not a certificate authority <br/>
	 *                                         +Cause CertificateExpiredException if
	 *                                         the certificate has expired <br/>
	 *                                         +Cause
	 *                                         CertificateNotYetValidException if
	 *                                         the certificate is not yet valid
	 */
	private static void verifyCertificateAuthority(final X509Certificate certificate, final Date validAfter,
			final Date expireDate) throws CertificateAuthorityLtException {
		try {
			verifyChainForExtension(certificate, 1);
		} catch (final CertificateChainLtException e) {
			throw new CertificateAuthorityLtException(e);
		}
		if (certificate.getNotBefore().getTime() > validAfter.getTime()) {
			throw new CertificateAuthorityLtException(new CertificateNotYetValidException());
		}
		if (certificate.getNotAfter().getTime() < expireDate.getTime()) {
			throw new CertificateAuthorityLtException(new CertificateExpiredException());
		}
	}

	/*
	 * Verify certification request
	 */

	/**
	 * Verifies signature of the request and validates the information extension to
	 * be added to the new certificate.
	 *
	 * @param request
	 * @throws CertificationRequestLtException if some irregular information found
	 *                                         in the request <br/>
	 *                                         +Cause InvalidKeyException <br/>
	 *                                         +Cause NoSuchAlgorithmException <br/>
	 *                                         +Cause NoSuchProviderException <br/>
	 *                                         +Cause SignatureException
	 */
	private static void verifyPKCS10CertificationRequest(final PKCS10CertificationRequest request)
			throws CertificationRequestLtException {
		try {
			if (!request.verify(ProviderEnum.BC.getName())) {
				throw new SignatureException();
			}
		} catch (final InvalidKeyException e) {
			throw new CertificationRequestLtException(e);
		} catch (final NoSuchAlgorithmException e) {
			throw new CertificationRequestLtException(e);
		} catch (final NoSuchProviderException e) {
			throw new CertificationRequestLtException(e);
		} catch (final SignatureException e) {
			throw new CertificationRequestLtException(e);
		}
		// extract the extension request attribute for validation
		final ASN1Set attributes = request.getCertificationRequestInfo().getAttributes();
		if (attributes != null) {
			Attribute attribute;
			for (int i = 0; i < attributes.size(); i++) {
				attribute = Attribute.getInstance(attributes.getObjectAt(i));
				// unique type of extensions allowed
				if (!attribute.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
					throw new CertificationRequestLtException();
				}
				if (attribute.getAttrValues().size() == 1) {
					final X509Extensions extensions = X509Extensions
							.getInstance(attribute.getAttrValues().getObjectAt(0));
					final Enumeration<?> oidEnumeration = extensions.oids();
					DERObjectIdentifier oid;
					X509Extension extension;
					while (oidEnumeration.hasMoreElements()) {
						try {
							oid = DERObjectIdentifier.class.cast(oidEnumeration.nextElement());
						} catch (final ClassCastException e) {
							// oid with a not expected object class
							throw new CertificationRequestLtException(e);
						}
						// only allow extension for a SubjectAlternativeName
						if (!Arrays.equals(X509Extensions.SubjectAlternativeName.getDEREncoded(),
								oid.getDEREncoded())) {
							throw new CertificationRequestLtException();
						}
						extension = extensions.getExtension(oid);
						if (extension != null) {
							// and can not a be critical extension
							if (extension.isCritical()) {
								throw new CertificationRequestLtException();
							}
							try {
								// TODO: verify that the value of oid SubjectAlternativeName is in
								// GeneralName.rfc822Name
								// ? is this enough
								DEROctetString.getInstance(extension.getValue());
							} catch (final IllegalArgumentException e) {
								throw new CertificationRequestLtException();
							}
						}
					}
				} else if (attribute.getAttrValues().size() != 0) {
					// if have more than one element
					throw new CertificationRequestLtException();
				}
			}
		}
	}

	/*
	 * Verify chain
	 */

	/**
	 * Verifies that the root certificate in the chain was signed using the private
	 * key that corresponds to the specified public key. <br/>
	 * And all consecutive certificates were signed using the private key that
	 * corresponds to the public key in anterior certificate. <br/>
	 * Also verifies that all are of type X.509 and if the current date and time are
	 * within the validity period given in all the certificates.
	 *
	 * @param chain       with the certificates to verify
	 * @param pbcKeyTrust the PublicKey used to carry out the verification of the
	 *                    root certificate of the chain
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
	public static void verifyChain(final Certificate[] chain, final PublicKey pbcKeyTrust)
			throws CertificateLtException {
		verifyChain(chain, pbcKeyTrust, -1);
	}

	/**
	 * Verifies that the root certificate in the chain was signed using the private
	 * key that corresponds to the specified public key. <br/>
	 * And all consecutive certificates were signed using the private key that
	 * corresponds to the public key in anterior certificate. <br/>
	 * Also verifies that all are of type X.509 and if the current date and time are
	 * within the validity period given in all the certificates. <br/>
	 * <br/>
	 * The chain length will not be asserted if <code>secureChainLength</code> is
	 * less then zero.
	 *
	 * @param chain             with the certificates to verify
	 * @param pbcKeyTrust       the PublicKey used to carry out the verification of
	 *                          the root certificate of the chain
	 * @param secureChainLength the length for the chain be considered secure
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
	 *                                or it's type is not X.509 or the chain doesn't
	 *                                respect the <code>secureChainLength</code>
	 *                                <br/>
	 *                                +Cause CertificateChainLtException if the
	 *                                chain doesn't respect the
	 *                                <code>secureChainLength</code> or if a
	 *                                certificate has a wrong position in the chain
	 *                                <br/>
	 *                                +Cause CertificateExpiredException if the
	 *                                certificate has expired <br/>
	 *                                +Cause CertificateNotYetValidException if the
	 *                                certificate is not yet valid
	 */
	public static void verifyChain(final Certificate[] chain, final PublicKey pbcKeyTrust, final int secureChainLength)
			throws CertificateLtException {
		if (secureChainLength > -1 && chain.length != secureChainLength) {
			throw new CertificateLtException(new CertificateChainLtException("The chain length is #0 and expected to be #1",
					chain.length, secureChainLength));
		}
		PublicKey pbcKeyTmp = pbcKeyTrust;
		for (int i = chain.length - 1; i >= 0; i--) {
			CertificateUtil.verify(chain[i], pbcKeyTmp);
			try {
				verifyChainForExtension(X509Certificate.class.cast(chain[i]), i + 1);
			} catch (final CertificateChainLtException e) {
				throw new CertificateLtException(e);
			}
			pbcKeyTmp = chain[i].getPublicKey();
		}
	}

	/**
	 * Verifies if the extensions in the certificate allows have the
	 * <code>linkPosition</code> in a chain.
	 *
	 * @param certificate
	 * @param linkPosition
	 * @throws CertificateChainLtException if the certificate has a wrong position
	 *                                     in the chain
	 */
	private static void verifyChainForExtension(final X509Certificate certificate, final int linkPosition)
			throws CertificateChainLtException {
		final Set<String> criticalExtensionOIDs = certificate.getCriticalExtensionOIDs();
		if (criticalExtensionOIDs != null) {
			for (final String oid : criticalExtensionOIDs) {
				if (ConstantCrypto.BASIC_CONSTRAIN_OID.equals(oid)) {
					// basicConstrain OID
					final int pathLength = certificate.getBasicConstraints();
					if (pathLength == -1) {
						// its not a CA
						if (linkPosition != 0) {
							throw new CertificateChainLtException("The chain can not have an end-entity (not CA) at #0 position", linkPosition);
						}
					} else {
						// its a CA
						if (pathLength < linkPosition - 1) {
							throw new CertificateChainLtException("Certificate at index #0 in the chain can only have #1 links bellow", linkPosition,
									(pathLength + 1));
						}
					}
				}
			}
		}
	}

}

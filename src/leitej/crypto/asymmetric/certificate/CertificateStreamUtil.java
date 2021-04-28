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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;
import java.util.Scanner;

import leitej.exception.CertificateLtException;
import sun.security.pkcs10.PKCS10;
import sun.security.provider.X509Factory;

/**
 *
 * @author Julio Leite
 */
public final class CertificateStreamUtil {

	private static final String MIME_LINE_FEED = "\r\n";

	private CertificateStreamUtil() {
	}

	/*
	 * Write certificates
	 */

	/**
	 * Writes the encoded form (ASN.1 DER) of the certificates to the output stream
	 * <code>os</code>.
	 *
	 * @param os
	 * @param certificates
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateEncodingException if an
	 *                                encoding error occurs
	 * @throws IOException            if an I/O error occurs
	 */
	public static void writeX509Certificates(final OutputStream os, final X509Certificate... certificates)
			throws CertificateLtException, IOException {
		try {
			for (final Certificate certificate : certificates) {
				os.write(certificate.getEncoded());
				os.flush();
			}
		} catch (final CertificateEncodingException e) {
			throw new CertificateLtException(e);
		}
	}

	/*
	 * Write certificates in PEM format
	 */

	/**
	 * Writes the certificates in PEM format to the <code>writer</code>.
	 *
	 * @param writer
	 * @param certificates
	 * @throws IOException                  if an I/O error occurred
	 * @throws CertificateEncodingException if an encoding error occurs
	 */
	public static void writeX509CertificatesPEM(final Writer writer, final X509Certificate... certificates)
			throws IOException, CertificateEncodingException {
		final Base64.Encoder encoder = Base64.getMimeEncoder(64, MIME_LINE_FEED.getBytes());
		for (final Certificate cert : certificates) {
			writer.write(X509Factory.BEGIN_CERT);
			writer.write(MIME_LINE_FEED);
			writer.write(encoder.encodeToString(cert.getEncoded()));
			writer.write(MIME_LINE_FEED);
			writer.write(X509Factory.END_CERT);
			writer.write(MIME_LINE_FEED);
			writer.flush();
		}
	}

	/*
	 * Write certification request in PEM format
	 */

	/**
	 * Writes the certification request in PEM format to the <code>writer</code>.
	 *
	 * @param writer
	 * @param certificationRequests
	 * @throws IOException If an I/O error occurred
	 */
	public static void writePKCS10CertificationRequestPEM(final Writer writer, final PKCS10... certificationRequests)
			throws IOException {
		final Base64.Encoder encoder = Base64.getMimeEncoder(64, MIME_LINE_FEED.getBytes());
		for (final PKCS10 request : certificationRequests) {
			writer.write("-----BEGIN CERTIFICATE REQUEST-----");
			writer.write(MIME_LINE_FEED);
			writer.write(encoder.encodeToString(request.getEncoded()));
			writer.write(MIME_LINE_FEED);
			writer.write("-----END CERTIFICATE REQUEST-----");
			writer.write(MIME_LINE_FEED);
			writer.flush();
		}
	}

	/*
	 * Read certificate
	 */

	/**
	 * Generates a certificate object and initialises it with the data read from the
	 * input stream <code>is</code>.<br/>
	 * This method uses {@link java.security.cert.CertificateFactory
	 * CertificateFactory}.<br/>
	 * <br/>
	 * The certificate provided in <code>is</code> must be DER-encoded and may be
	 * supplied in binary or printable (Base64) encoding. If the certificate is
	 * provided in Base64 encoding, it must be bounded at the beginning by
	 * -----BEGIN CERTIFICATE-----, and must be bounded at the end by -----END
	 * CERTIFICATE-----.<br/>
	 * <br/>
	 * Note that if the given input stream does not support
	 * {@link java.io.InputStream#mark(int) mark} and
	 * {@link java.io.InputStream#reset() reset}, this method will consume the
	 * entire input stream. Otherwise, each call to this method consumes one
	 * certificate and the read position of the input stream is positioned to the
	 * next available byte after the inherent end-of-certificate marker. If the data
	 * in the input stream does not contain an inherent end-of-certificate marker
	 * (other than EOF) and there is trailing data after the certificate is parsed,
	 * a <code>CertificateException</code> is thrown.
	 *
	 * @param is an input stream with the X509 certificate data
	 * @return the X509 certificate object initialised with the data from the input
	 *         stream
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateException if it's type is
	 *                                not X.509 or a CertificateFactorySpi
	 *                                implementation for the specified algorithm is
	 *                                not available from the specified provider or
	 *                                on parsing errors
	 */
	public static X509Certificate readX509Certificate(final InputStream is) throws CertificateLtException {
		try {
			// create the certificate factory
			CertificateFactory fact;
			fact = CertificateFactory.getInstance(TypeEnum.X509.getName());
			// read certificate
			final Certificate certificate = fact.generateCertificate(is);
			CertificateUtil.verifyTypeX509(certificate);
			return X509Certificate.class.cast(certificate);
		} catch (final CertificateException e) {
			throw new CertificateLtException(e);
		}
	}

	/*
	 * Read certificate collection
	 */

	/**
	 * Returns an array of the X509 certificates read from the given input stream
	 * <code>is</code>.<br/>
	 * This method uses {@link java.security.cert.CertificateFactory
	 * CertificateFactory}.<br/>
	 * <br/>
	 * The <code>is</code> may contain a sequence of DER-encoded certificates in the
	 * formats described for {@link #generateCertificate(java.io.InputStream)
	 * generateCertificate}. In addition, <code>is</code> may contain a PKCS#7
	 * certificate chain. This is a PKCS#7 <i>SignedData</i> object, with the only
	 * significant field being <i>certificates</i>. In particular, the signature and
	 * the contents are ignored. This format allows multiple certificates to be
	 * downloaded at once. If no certificates are present, an empty collection is
	 * returned.<br/>
	 * <br/>
	 * Note that if the given input stream does not support
	 * {@link java.io.InputStream#mark(int) mark} and
	 * {@link java.io.InputStream#reset() reset}, this method will consume the
	 * entire input stream.
	 *
	 * @param is the input stream with the X509 certificates
	 * @return an array of java.security.cert.X509Certificate objects initialised
	 *         with the data from the input stream
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateException if any of the
	 *                                certificate is not a X.509 type or a
	 *                                CertificateFactorySpi implementation for the
	 *                                specified algorithm is not available from the
	 *                                specified provider or on parsing errors
	 */
	public static X509Certificate[] readX509Certificates(final InputStream is) throws CertificateLtException {
		try {
			// create the certificate factory
			final CertificateFactory fact = CertificateFactory.getInstance(TypeEnum.X509.getName());
			X509Certificate[] result;
			// read certificates
			final Collection<? extends Certificate> tmp = fact.generateCertificates(is);
			if (tmp == null) {
				return new X509Certificate[0];
			}
			result = new X509Certificate[tmp.size()];
			int i = 0;
			for (final Certificate certificate : tmp) {
				CertificateUtil.verifyTypeX509(certificate);
				result[i++] = X509Certificate.class.cast(certificate);
			}
			return result;
		} catch (final CertificateException e) {
			throw new CertificateLtException(e);
		}
	}

	/*
	 * Read certification request in PEM format
	 */

	/**
	 * Reads a certification request in PEM format from the <code>scanner</code>.
	 *
	 * @param scanner
	 * @return the certification request
	 * @throws IOException            for low level errors reading the data
	 * @throws CertificateLtException <br/>
	 *                                +Cause SignatureException when the signature
	 *                                is invalid <br/>
	 *                                +Cause NoSuchAlgorithmException when the
	 *                                signature algorithm is not supported in this
	 *                                environment <br/>
	 *                                +Cause CertificateEncodingException when
	 *                                invalid syntax
	 */
	public static PKCS10 readPKCS10CertificationRequestPEM(final Scanner scanner)
			throws IOException, CertificateLtException {
		final Base64.Decoder decoder = Base64.getMimeDecoder();
		scanner.useDelimiter(".");
		final String linePattern = ".*" + MIME_LINE_FEED;
		final String header = scanner.findWithinHorizon(linePattern, 128);
		if (header == null || !header.matches("-----BEGIN.+CERTIFICATE REQUEST-----" + MIME_LINE_FEED)) {
			throw new CertificateLtException(new CertificateEncodingException("Invalid syntax"));
		}
		final StringBuilder sb = new StringBuilder();
		String line = scanner.findWithinHorizon(linePattern, 128);
		while (line != null && !line.startsWith("-----")) {
			sb.append(line);
			sb.setLength(sb.length() - MIME_LINE_FEED.length());
			line = scanner.findWithinHorizon(linePattern, 128);
		}
		if (line == null || !line.matches("-----END.+CERTIFICATE REQUEST-----" + MIME_LINE_FEED)) {
			throw new CertificateLtException(new CertificateEncodingException("Invalid syntax"));
		}
		try {
			return new PKCS10(decoder.decode(sb.toString()));
		} catch (SignatureException | NoSuchAlgorithmException e) {
			throw new CertificateLtException(e);
		}
	}

}

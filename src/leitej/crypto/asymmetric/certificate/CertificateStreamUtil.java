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
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;

import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import leitej.exception.CertificateLtException;

/**
 *
 * @author Julio Leite
 */
public final class CertificateStreamUtil {

	private static final String MIME_LINE_FEED = "\r\n";
	private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
	private static final String END_CERT = "-----END CERTIFICATE-----";

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
	 *                                +Cause IOException if an encoding cannot be
	 *                                generated
	 */
	public static void writeX509Certificates(final OutputStream os, final X509CertificateHolder... certificates)
			throws CertificateLtException, IOException {
		try {
			for (final X509CertificateHolder certificate : certificates) {
				os.write(certificate.getEncoded());
				os.flush();
			}
		} catch (final IOException e) {
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
	public static void writeX509CertificatesPEM(final Writer writer, final X509CertificateHolder... certificates)
			throws CertificateLtException {
		final Base64.Encoder encoder = Base64.getMimeEncoder(64, MIME_LINE_FEED.getBytes());
		try {
			for (final X509CertificateHolder cert : certificates) {
				writer.write(BEGIN_CERT);
				writer.write(MIME_LINE_FEED);
				writer.write(encoder.encodeToString(cert.getEncoded()));
				writer.write(MIME_LINE_FEED);
				writer.write(END_CERT);
				writer.write(MIME_LINE_FEED);
				writer.flush();
			}
		} catch (final IOException e) {
			throw new CertificateLtException(e);
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
	public static void writeCertificationRequestPEM(final Writer writer,
			final CertificationRequest... certificationRequests) throws IOException {
		final Base64.Encoder encoder = Base64.getMimeEncoder(64, MIME_LINE_FEED.getBytes());
		for (final CertificationRequest request : certificationRequests) {
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
	 * <br/>
	 * The certificate provided in <code>is</code> must be DER-encoded and may be
	 * supplied in binary or printable (Base64) encoding. If the certificate is
	 * provided in Base64 encoding, it must be bounded at the beginning by
	 * -----BEGIN CERTIFICATE-----, and must be bounded at the end by -----END
	 * CERTIFICATE-----.
	 *
	 * @param is an input stream with the X509 certificate data
	 * @return the X509 certificate object initialised with the data from the input
	 *         stream
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateException on parsing or
	 *                                encoding errors<br/>
	 *                                +Cause IOException in the event of corrupted
	 *                                data, or an incorrect structure
	 */
	public static X509CertificateHolder readX509Certificate(final InputStream is) throws CertificateLtException {
		try {
			// create the certificate factory, read certificate, convert
			return new X509CertificateHolder((new CertificateFactory()).engineGenerateCertificate(is).getEncoded());
		} catch (final CertificateException | IOException e) {
			throw new CertificateLtException(e);
		}
	}

	/*
	 * Read certificate collection
	 */

	/**
	 * Returns an array of the X509 certificates read from the given input stream
	 * <code>is</code>.
	 *
	 * @param is the input stream with the X509 certificates
	 * @return an array of X509 certificate initialized with the data from the input
	 *         stream
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateException on parsing or
	 *                                encoding errors<br/>
	 *                                +Cause IOException in the event of corrupted
	 *                                data, or an incorrect structure
	 */
	public static X509CertificateHolder[] readX509Certificates(final InputStream is) throws CertificateLtException {
		try {
			// read certificates
			final Collection<?> certificates = (new CertificateFactory()).engineGenerateCertificates(is);
			final X509CertificateHolder[] result = new X509CertificateHolder[certificates.size()];
			int i = 0;
			for (final Iterator<?> iterator = certificates.iterator(); iterator.hasNext(); i++) {
				result[i] = CertificateUtil.convert(Certificate.class.cast(iterator.next()));
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
	 * @throws CertificateLtException <br/>
	 *                                +Cause IOException in the event of corrupted
	 *                                data, or an incorrect structure
	 */
	public static CertificationRequest readCertificationRequestPEM(final Scanner scanner) throws CertificateLtException {
		final Base64.Decoder decoder = Base64.getMimeDecoder();
		scanner.useDelimiter(".");
		final String linePattern = ".*" + MIME_LINE_FEED;
		final String header = scanner.findWithinHorizon(linePattern, 128);
		if (header == null || !header.matches("-----BEGIN.+CERTIFICATE REQUEST-----" + MIME_LINE_FEED)) {
			throw new CertificateLtException(new IOException("Invalid syntax"));
		}
		final StringBuilder sb = new StringBuilder();
		String line = scanner.findWithinHorizon(linePattern, 128);
		while (line != null && !line.startsWith("-----")) {
			sb.append(line);
			sb.setLength(sb.length() - MIME_LINE_FEED.length());
			line = scanner.findWithinHorizon(linePattern, 128);
		}
		if (line == null || !line.matches("-----END.+CERTIFICATE REQUEST-----" + MIME_LINE_FEED)) {
			throw new CertificateLtException(new IOException("Invalid syntax"));
		}
		try {
			final PKCS10CertificationRequest csrBuilder = new PKCS10CertificationRequest(decoder.decode(sb.toString()));
			return csrBuilder.toASN1Structure();
		} catch (final IOException e) {
			throw new CertificateLtException(e);
		}
	}

}

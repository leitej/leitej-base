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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;

import leitej.crypto.ProviderEnum;
import leitej.exception.CertificateLtException;
import leitej.exception.CertificationRequestLtException;
import leitej.log.Logger;
import leitej.util.stream.FileUtil;

/**
 *
 * @author Julio Leite
 */
public final class CertificateIoUtil {

	private static final Logger LOG = Logger.getInstance();

	private CertificateIoUtil() {
	}

	/*
	 * Write certificates
	 */

	/**
	 * Writes the encoded form (ASN.1 DER) of the certificates to the file
	 * <code>filename</code>.
	 *
	 * @param filename     a pathname string
	 * @param append       if true, then bytes will be written to the end of the
	 *                     file rather than the beginning
	 * @param certificates
	 * @throws NullPointerException   If the <code>filename</code> argument is
	 *                                <code>null</code>
	 * @throws FileNotFoundException  If the file exists but is a directory rather
	 *                                than a regular file, does not exist but cannot
	 *                                be created, or cannot be opened for any other
	 *                                reason
	 * @throws IOException            If an I/O error occurred
	 * @throws SecurityException      If a security manager exists and its
	 *                                <code>{@link
	 *          java.lang.SecurityManager#checkRead(java.lang.String) checkRead}</code>
	 *                                method does not permit verification of the
	 *                                existence of the named directory and all
	 *                                necessary parent directories; or if the
	 *                                <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                                method does not permit the named directory and
	 *                                all necessary parent directories to be
	 *                                created; or if a security manager exists and
	 *                                its <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                                method denies write access to the file
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateEncodingException if an
	 *                                encoding error occurs
	 */
	public static void writeX509Certificates(final String filename, final boolean append,
			final X509Certificate... certificates)
			throws NullPointerException, FileNotFoundException, SecurityException, IOException, CertificateLtException {
		OutputStream os = null;
		try {
			os = FileUtil.openFileBinaryOutputStream(filename, append);
			writeX509Certificates(os, certificates);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (final IOException e) {
					LOG.error("#0", e);
				}
			}
		}
	}

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
			}
		} catch (final CertificateEncodingException e) {
			throw new CertificateLtException(e);
		}
	}

	/*
	 * Write certificates in PEM format
	 */

	/**
	 * Writes the certificates in PEM format to the file <code>filename</code>.
	 *
	 * @param filename a pathname string
	 * @param append   if true, then bytes will be written to the end of the file
	 *                 rather than the beginning
	 * @param charset  the name of a supported {@link java.nio.charset.Charset
	 *                 </code>charset<code>}
	 * &#64;param certificates
	 * @throws NullPointerException If the <code>filename</code> argument is
	 *                                      <code>null</code>
	 * @throws FileNotFoundException        If the file exists but is a directory
	 *                                      rather than a regular file, does not
	 *                                      exist but cannot be created, or cannot
	 *                                      be opened for any other reason
	 * @throws IOException                  If an I/O error occurred
	 * @throws SecurityException            If a security manager exists and its
	 *                                      <code>{@link
	 *          java.lang.SecurityManager#checkRead(java.lang.String) checkRead}</code>
	 *                                      method does not permit verification of
	 *                                      the existence of the named directory and
	 *                                      all necessary parent directories; or if
	 *                                      the <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                                      method does not permit the named
	 *                                      directory and all necessary parent
	 *                                      directories to be created; or if a
	 *                                      security manager exists and its
	 *                                      <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                                      method denies write access to the file
	 * @throws UnsupportedEncodingException If the charset encoding is not supported
	 */
	public static void writeX509CertificatesPEM(final String filename, final boolean append, final String charset,
			final X509Certificate... certificates) throws NullPointerException, FileNotFoundException,
			SecurityException, UnsupportedEncodingException, IOException {
		OutputStream os = null;
		try {
			os = FileUtil.openFileBinaryOutputStream(filename, append);
			writeX509CertificatesPEM(os, charset, certificates);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (final IOException e) {
					LOG.error("#0", e);
				}
			}
		}
	}

	/**
	 * Writes the certificates in PEM format to the output stream <code>os</code>.
	 *
	 * @param os
	 * @param charset      the name of a supported {@link java.nio.charset.Charset
	 *                     </code>charset<code>}
	 * @param certificates
	 * @throws UnsupportedEncodingException If the charset encoding is not supported
	 * @throws IOException                  If an I/O error occurred
	 */
	public static void writeX509CertificatesPEM(final OutputStream os, final String charset,
			final X509Certificate... certificates) throws UnsupportedEncodingException, IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final PEMWriter pemWrt = new PEMWriter(new OutputStreamWriter(bos, charset), ProviderEnum.BC.getName());
		for (final Certificate cert : certificates) {
			pemWrt.writeObject(cert);
			pemWrt.flush();
			bos.writeTo(os);
			bos.reset();
		}
		pemWrt.close();
	}

	/*
	 * Write certification request in PEM format
	 */

	/**
	 * Writes the certification request in PEM format to the file
	 * <code>filename</code>.
	 *
	 * @param filename a pathname string
	 * @param append   if true, then bytes will be written to the end of the file
	 *                 rather than the beginning
	 * @param charset  the name of a supported {@link java.nio.charset.Charset
	 *                 </code>charset<code>}
	 * &#64;param certificationRequests
	 * @throws NullPointerException If the <code>filename</code> argument is
	 *                                      <code>null</code>
	 * @throws FileNotFoundException        If the file exists but is a directory
	 *                                      rather than a regular file, does not
	 *                                      exist but cannot be created, or cannot
	 *                                      be opened for any other reason
	 * @throws IOException                  If an I/O error occurred
	 * @throws SecurityException            If a security manager exists and its
	 *                                      <code>{@link
	 *          java.lang.SecurityManager#checkRead(java.lang.String) checkRead}</code>
	 *                                      method does not permit verification of
	 *                                      the existence of the named directory and
	 *                                      all necessary parent directories; or if
	 *                                      the <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                                      method does not permit the named
	 *                                      directory and all necessary parent
	 *                                      directories to be created; or if a
	 *                                      security manager exists and its
	 *                                      <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                                      method denies write access to the file
	 * @throws UnsupportedEncodingException If the charset encoding is not supported
	 */
	public static void writePKCS10CertificationRequestPEM(final String filename, final boolean append,
			final String charset, final PKCS10CertificationRequest... certificationRequests)
			throws NullPointerException, FileNotFoundException, SecurityException, UnsupportedEncodingException,
			IOException {
		OutputStream os = null;
		try {
			os = FileUtil.openFileBinaryOutputStream(filename, append);
			writePKCS10CertificationRequestPEM(os, charset, certificationRequests);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (final IOException e) {
					LOG.error("#0", e);
				}
			}
		}
	}

	/**
	 * Writes the certification request in PEM format to the output stream
	 * <code>os</code>.
	 *
	 * @param os
	 * @param charset               the name of a supported
	 *                              {@link java.nio.charset.Charset
	 *                              </code>charset<code>}
	 * @param certificationRequests
	 * @throws UnsupportedEncodingException If the charset encoding is not supported
	 * @throws IOException                  If an I/O error occurred
	 */
	public static void writePKCS10CertificationRequestPEM(final OutputStream os, final String charset,
			final PKCS10CertificationRequest... certificationRequests)
			throws UnsupportedEncodingException, IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final PEMWriter pemWrt = new PEMWriter(new OutputStreamWriter(bos, charset), ProviderEnum.BC.getName());
		for (final PKCS10CertificationRequest request : certificationRequests) {
			pemWrt.writeObject(request);
			pemWrt.flush();
			bos.writeTo(os);
			bos.reset();
		}
		pemWrt.close();
	}

	/*
	 * Read certificate
	 */

	/**
	 * Generates a certificate object and initialises it with the data read from the
	 * file <code>filename</code>.<br/>
	 * This method uses {@link java.security.cert.CertificateFactory
	 * CertificateFactory}.<br/>
	 * <br/>
	 * The certificate provided in <code>is</code> must be DER-encoded and may be
	 * supplied in binary or printable (Base64) encoding. If the certificate is
	 * provided in Base64 encoding, it must be bounded at the beginning by
	 * -----BEGIN CERTIFICATE-----, and must be bounded at the end by -----END
	 * CERTIFICATE-----.<br/>
	 * <br/>
	 * If the data in the file does not contain an inherent end-of-certificate
	 * marker (other than EOF) and there is trailing data after the certificate is
	 * parsed, a <code>CertificateException</code> is thrown.
	 *
	 * @param filename a pathname string with the X509 certificate data
	 * @return the X509 certificate object initialised with the data from the file
	 * @throws NullPointerException   If the <code>filename</code> argument is
	 *                                <code>null</code>
	 * @throws SecurityException      If a security manager exists and its
	 *                                <code>checkRead</code> method denies read
	 *                                access to the file
	 * @throws FileNotFoundException  If the file does not exist, is a directory
	 *                                rather than a regular file, or for some other
	 *                                reason cannot be opened for reading
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateException if it's type is
	 *                                not X.509 or a CertificateFactorySpi
	 *                                implementation for the specified algorithm is
	 *                                not available from the specified provider or
	 *                                on parsing errors <br/>
	 *                                +Cause NoSuchProviderException If the
	 *                                specified provider is not registered in the
	 *                                security provider list
	 */
	public static X509Certificate readX509Certificate(final String filename)
			throws NullPointerException, SecurityException, FileNotFoundException, CertificateLtException {
		InputStream is = null;
		X509Certificate result = null;
		try {
			is = FileUtil.openFileBinaryInputStream(filename);
			result = readX509Certificate(is);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
		}
		return result;
	}

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
	 *                                on parsing errors <br/>
	 *                                +Cause NoSuchProviderException If the
	 *                                specified provider is not registered in the
	 *                                security provider list
	 */
	public static X509Certificate readX509Certificate(final InputStream is) throws CertificateLtException {
		try {
			// create the certificate factory
			CertificateFactory fact;
			fact = CertificateFactory.getInstance(TypeEnum.X509.getName(), ProviderEnum.BC.getName());
			// read certificate
			final Certificate certificate = fact.generateCertificate(is);
			CertificateUtil.verifyTypeX509(certificate);
			return X509Certificate.class.cast(certificate);
		} catch (final CertificateException e) {
			throw new CertificateLtException(e);
		} catch (final NoSuchProviderException e) {
			throw new CertificateLtException(e);
		}
	}

	/*
	 * Read certificate collection
	 */

	/**
	 * Returns an array of the X509 certificates read from the given file
	 * <code>filename</code>.<br/>
	 * This method uses {@link java.security.cert.CertificateFactory
	 * CertificateFactory}.<br/>
	 * <br/>
	 * The <code>filename</code> may contain a sequence of DER-encoded certificates
	 * in the formats described for {@link #generateCertificate(java.io.InputStream)
	 * generateCertificate}. In addition, <code>filename</code> may contain a PKCS#7
	 * certificate chain. This is a PKCS#7 <i>SignedData</i> object, with the only
	 * significant field being <i>certificates</i>. In particular, the signature and
	 * the contents are ignored. This format allows multiple certificates to be
	 * downloaded at once. If no certificates are present, an empty collection is
	 * returned.
	 *
	 * @param filename a pathname string with the X509 certificates data
	 * @return an array of java.security.cert.X509Certificate objects initialised
	 *         with the data from the file
	 * @throws NullPointerException   If the <code>filename</code> argument is
	 *                                <code>null</code>
	 * @throws SecurityException      If a security manager exists and its
	 *                                <code>checkRead</code> method denies read
	 *                                access to the file
	 * @throws FileNotFoundException  If the file does not exist, is a directory
	 *                                rather than a regular file, or for some other
	 *                                reason cannot be opened for reading
	 * @throws CertificateLtException <br/>
	 *                                +Cause CertificateException if any of the
	 *                                certificate is not a X.509 type or a
	 *                                CertificateFactorySpi implementation for the
	 *                                specified algorithm is not available from the
	 *                                specified provider or on parsing errors <br/>
	 *                                +Cause NoSuchProviderException If the
	 *                                specified provider is not registered in the
	 *                                security provider list
	 */
	public static X509Certificate[] readX509Certificates(final String filename)
			throws NullPointerException, SecurityException, FileNotFoundException, CertificateLtException {
		InputStream is = null;
		X509Certificate[] result = null;
		try {
			is = FileUtil.openFileBinaryInputStream(filename);
			result = readX509Certificates(is);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
		}
		return result;
	}

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
	 *                                specified provider or on parsing errors <br/>
	 *                                +Cause NoSuchProviderException If the
	 *                                specified provider is not registered in the
	 *                                security provider list
	 */
	public static X509Certificate[] readX509Certificates(final InputStream is) throws CertificateLtException {
		try {
			// create the certificate factory
			final CertificateFactory fact = CertificateFactory.getInstance(TypeEnum.X509.getName(),
					ProviderEnum.BC.getName());
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
		} catch (final NoSuchProviderException e) {
			throw new CertificateLtException(e);
		}
	}

	/*
	 * Read certification request in PEM format
	 */

	/**
	 * Reads the certification request in PEM format from the file
	 * <code>filename</code>.
	 *
	 * @param filename a pathname string
	 * @param charset  the name of a supported {@link java.nio.charset.Charset
	 *                 </code>charset<code>}
	 * @throws NullPointerException If the <code>filename</code> argument is
	 *                                         <code>null</code>
	 * @throws SecurityException               If a security manager exists and its
	 *                                         <code>checkRead</code> method denies
	 *                                         read access to the file
	 * @throws FileNotFoundException           If the file does not exist, is a
	 *                                         directory rather than a regular file,
	 *                                         or for some other reason cannot be
	 *                                         opened for reading
	 * @throws UnsupportedEncodingException    if the named charset is not supported
	 * @throws IOException                     if an I/O error occurred
	 * @throws CertificationRequestLtException if the certification request in the
	 *                                         input stream is not of type
	 *                                         PKCS10CertificationRequest
	 */
	public static PKCS10CertificationRequest readPKCS10CertificationRequestPEM(final String filename,
			final String charset) throws NullPointerException, SecurityException, UnsupportedEncodingException,
			FileNotFoundException, IOException, CertificationRequestLtException {
		InputStream is = null;
		PKCS10CertificationRequest result = null;
		try {
			is = FileUtil.openFileBinaryInputStream(filename);
			result = readPKCS10CertificationRequestPEM(is, charset);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
		}
		return result;
	}

	/**
	 * Reads a certification request in PEM format from the input stream
	 * <code>is</code>.<br/>
	 * Input stream <code>is</code> is not closed.
	 *
	 * @param is
	 * @param charset the name of a supported {@link java.nio.charset.Charset
	 *                </code>charset<code>}
	 * @return the certification request
	 * @throws UnsupportedEncodingException    if the named charset is not supported
	 * @throws IOException                     if an I/O error occurred
	 * @throws CertificationRequestLtException if the certification request in the
	 *                                         input stream is not of type
	 *                                         PKCS10CertificationRequest
	 */
	private static PKCS10CertificationRequest readPKCS10CertificationRequestPEM(final InputStream is,
			final String charset) throws UnsupportedEncodingException, IOException, CertificationRequestLtException {
		@SuppressWarnings("resource")
		final PEMReader pemReader = new PEMReader(new InputStreamReader(is, charset), null, ProviderEnum.BC.getName());
		try {
			return PKCS10CertificationRequest.class.cast(pemReader.readObject());
		} catch (final ClassCastException e) {
			throw new CertificationRequestLtException(e);
		}
	}

}

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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.bouncycastle.cert.X509CertificateHolder;

import leitej.crypto.asymmetric.certificate.CertificateStreamUtil;
import leitej.crypto.asymmetric.certificate.CertificateUtil;
import leitej.exception.CertificateLtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.SeppukuLtRtException;

/**
 *
 * @author Julio Leite
 */
final class TrustedAnchor {

	private static final String DATA = new String(
			"-----BEGIN CERTIFICATE-----\n" + "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\n" + "gXUriDE3Shj5AO2cK5y5TPqZhMRipc/Z\n"
					+ "-----END CERTIFICATE-----");

	static final X509CertificateHolder CERTIFICATE;
	static {
		try {
			CERTIFICATE = CertificateStreamUtil.readX509Certificate(new ByteArrayInputStream(DATA.getBytes()));
		} catch (final CertificateLtException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	static final String ALIAS;
	static {
		try {
			ALIAS = CertificateUtil.getAlias(CERTIFICATE);
		} catch (final IOException e) {
			throw new SeppukuLtRtException(e);
		}
	}

	private TrustedAnchor() {
	}

}

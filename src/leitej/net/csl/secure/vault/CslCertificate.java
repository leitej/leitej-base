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

import org.bouncycastle.cert.X509CertificateHolder;

import leitej.exception.CertificateLtException;
import leitej.util.DateUtil;

/**
 *
 * @author Julio Leite
 */
final class CslCertificate {

	private final X509CertificateHolder certificate;
	private final CslCertificate issuerCslCertificate;
	private final long checkedTill;

	CslCertificate(final X509CertificateHolder certificate, final CslCertificate issuerCslCertificate) {
		this.certificate = certificate;
		this.issuerCslCertificate = issuerCslCertificate;
		this.checkedTill = Long.MAX_VALUE;
	}

//	final X509Certificate getCertificate() {
//		return certificate;
//	}

	final X509CertificateHolder getIssuer() {
		return this.issuerCslCertificate.certificate;
	}

	final void check() throws CertificateLtException {
		if (DateUtil.nowTime() > this.checkedTill) {
			// TODO: verificar o certificado
			// TODO: CRL verify
		}
		if (this.issuerCslCertificate != null) {
			this.issuerCslCertificate.check();
		}
	}

}

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

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import leitej.exception.CertificateChainLtException;
import leitej.exception.CertificateLtException;
import leitej.exception.KeyStoreLtException;
import leitej.exception.LtmLtRtException;

/**
 *
 * @author Julio Leite
 */
public interface CslVaultItf {

	/**
	 *
	 * @param certificate
	 * @return
	 * @throws KeyStoreLtException if a trusted certificate cannot be recovered
	 */
	public boolean isTrustedAnchor(X509Certificate certificate) throws KeyStoreLtException;

	/**
	 *
	 * @return key
	 * @throws KeyStoreLtException if the key cannot be recovered
	 */
	public PrivateKey getCslPrivateKey() throws KeyStoreLtException;

	/**
	 *
	 * @return certificate chain
	 * @throws KeyStoreLtException if the certificate chain cannot be recovered
	 */
	public X509Certificate[] getCslChainCertificate() throws KeyStoreLtException;

	/**
	 *
	 * @return certificate
	 * @throws KeyStoreLtException if the certificate cannot be recovered
	 */
	public X509Certificate getCslCertificate() throws KeyStoreLtException;

	/**
	 *
	 * @param certificate
	 * @throws CertificateLtException if cadastre is not present for the argument
	 *                                certificate
	 * @throws LtmLtRtException       if encountered a problem accessing the long
	 *                                term memory
	 */
	public void verifyEndPointCertificate(X509Certificate certificate) throws CertificateLtException, LtmLtRtException;

	public void addEndPointChain(X509Certificate[] chain) throws CertificateChainLtException, LtmLtRtException,
			IOException, KeyStoreLtException, CertificateEncodingException;

	/**
	 * @param remoteHalfStateKeyBlock
	 * @param clientCertificate
	 */
	public void saltIn(byte[] remoteHalfStateKeyBlock, X509Certificate clientCertificate);

	/**
	 * @param myHalfStateKeyBlock
	 * @param clientCertificate
	 */
	public void saltOut(byte[] myHalfStateKeyBlock, X509Certificate clientCertificate);

	/**
	 * @param remoteKeyBlock
	 * @param myKeyBlock
	 * @param clientCertificate
	 * @throws LtmLtRtException if encountered a problem accessing the long term
	 *                          memory
	 */
	public void updateSalt(byte[] remoteKeyBlock, byte[] myKeyBlock, X509Certificate clientCertificate)
			throws LtmLtRtException;

}

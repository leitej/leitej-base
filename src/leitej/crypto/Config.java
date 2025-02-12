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

package leitej.crypto;

import leitej.crypto.asymmetric.signature.SignatureEnum;
import leitej.crypto.symmetric.CipherEnum;
import leitej.xml.om.XmlObjectModelling;

/**
 * @author Julio Leite
 *
 */
public abstract interface Config extends XmlObjectModelling {

	public abstract SignatureEnum getDefaultCertificateSignatureAlgorithm();

	public abstract void setDefaultCertificateSignatureAlgorithm(SignatureEnum defaultCertificateSignatureAlgorithm);

	public abstract CipherEnum getDefaultSymmetricCipher();

	public abstract void setDefaultSymmetricCipher(CipherEnum defaultSymmetricCipher);

	public abstract int getDefaultSymmetricKeyBitSize();

	public abstract void setDefaultSymmetricKeyBitSize(int defaultSymmetricKeyBitSize);

	public abstract int getDefaultSymmetricIvBitSize();

	public abstract void setDefaultSymmetricIvBitSize(int defaultSymmetricIvBitSize);

}

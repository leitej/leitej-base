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

package leitej.net.csl.secure;

import leitej.crypto.symmetric.CipherEnum;
import leitej.xml.om.XmlObjectModelling;

/**
 * @author Julio Leite
 *
 */
public abstract interface Config extends XmlObjectModelling {

	abstract leitej.net.csl.Config getCSL();

	abstract void setCSL(leitej.net.csl.Config csl);

	abstract CipherEnum getSymmetricCipher();

	abstract void setSymmetricCipher(CipherEnum symmetricCipher);

	abstract int getSymmetricKeyBitSize();

	abstract void setSymmetricKeyBitSize(int symmetricKeyBitSize);

	abstract int getSymmetricIvBitSize();

	abstract void setSymmetricIvBitSize(int symmetricIvBitSize);

}

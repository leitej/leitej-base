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

package leitej.crypto.symmetric;

import leitej.exception.ImplementationLtRtException;

/*
 * ARC4.	ARC4, apparently based on RSA Security's RC4 cipher.
 * 			Is probably the most widely used stream cipher on the net.
 */
/**
 * Stream ciphers do not have modes or require padding, they will always produce
 * output the same length as the input.<br/>
 * The idea is for the cipher to create a stream of bits that are then XORed
 * with the plain-text to produce the cipher-text.<br/>
 * Stream ciphers are basically just ciphers that, by design, behave like block
 * ciphers using the streaming modes.
 *
 * @author Julio Leite
 */
public enum StreamCipherEnum {
	ARC4;

	public String getName() {
		String result;
		switch (this) {
		case ARC4:
			result = "ARC4";
			break;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
		return result;
	}

	public int bestKeyBitSize() {
		// TODO:
//		int result;
		switch (this) {
		case ARC4:
			throw new ImplementationLtRtException(this.toString());
//			result = 128;
//			break;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
//		return result;
	}

}

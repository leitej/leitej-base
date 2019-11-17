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
 * ECB. 		Electronic CodeBook mode describes the use of a symmetric cipher in its rawest form.
 * 				The problem with ECB mode is that if there are patterns in your data, there will be
 * 				patterns in your encrypted data as well. A pattern, in this case, is any block of
 * 				bytes that contains the same values as another block of bytes. This is more common
 * 				than you might imagine, especially if you are processing data that is structured.
 * CBC. 		Cipher Block Chaining mode reduces the likelihood of patterns appearing in the cipher-text
 * 				by XORing the block of data to be encrypted with the last block of cipher-text produced
 * 				and then applying the raw cipher to produce the next block of cipher-text.
 * 				(Needs the initialisation vector)
 * CTS. 		Cipher Text Stealing is defined in RFC 2040 and combines the use of CBC mode with some
 * 				additional XOR operations on the final encrypted block of the data being processed to
 * 				produce encrypted data that is the same length as the input data. In some ways, it is
 * 				almost a padding mechanism more than a mode, and as it is based around CBC mode, it still
 * 				requires the data to be processed in discrete blocks.
 * 				(Streaming block cipher - should be used with NoPadding)
 * 				(Needs the initialisation vector)
 * SIC (CTR). 	Segmented Integer Counter mode (CounTeR mode) has been around for quite a while but has
 * 				finally been standardised by NIST in SP 800-38a and in RFC 3686.
 * 				(Streaming block cipher - should be used with NoPadding)
 * 				(Needs the initialisation vector)
 * OFB. 		Output Feedback Mode is like SIC (CTR) mode, OFB mode works by using the raw block cipher
 * 				to produce a stream of pseudo-random bits, which are then XORed with the input message to
 * 				produce the encrypted message. The actual input message is never used. With OFB mode, rather
 * 				than considering part of the IV to be a counter, you just load the IV into a state array,
 * 				encrypt the state array, and save the result back to the state array, using the bits you
 * 				generated to XOR with the next block of input and generate the cipher-text.
 * 				(Streaming block cipher - should be used with NoPadding)
 * 				(Needs the initialisation vector)
 * CFB. 		Cipher Feedback Mode is like OFB mode and CTR mode, CFB mode produces a stream of
 * 				pseudo-random bits that are then used to encrypt the input. Unlike the others, CFB mode uses
 * 				the plain-text as part of the process of generating the stream of bits. In this case, CFB
 * 				starts with the IV, encrypts it using the raw cipher and saves it in a state array. As you
 * 				encrypt a block of data, you XOR it with the state array to get the cipher-text and store the
 * 				resulting cipher-text back in our state array.
 * 				(Streaming block cipher - should be used with NoPadding)
 * 				(Needs the initialisation vector)
 * OpenPGPCFB. 	Variation on CFB mode defined in OpenPGP
 * GOFB. 		OFB mode defined for the GOST-28147 encryption algorithm
 */
/**
 *
 * @author Julio Leite
 */
public enum ModeEnum {
	ECB, CBC, CTS, SIC_CTR, OFB, CFB, OpenPGPCFB, GOFB;

	public String getName() {
		String result;
		switch (this) {
		case ECB:
			result = "ECB";
			break;
		case CBC:
			result = "CBC";
			break;
		case CTS:
			result = "CTS";
			break;
		case SIC_CTR:
			result = "SIC";
			break;
		case OFB:
			result = "OFB";
			break;
		case CFB:
			result = "CFB";
			break;
		case OpenPGPCFB:
			result = "OpenPGPCFB";
			break;
		case GOFB:
			result = "GOFB";
			break;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
		return result;
	}
}

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

/**
 * 
 * @author Julio Leite
 */
public final class CertificateRevocationUtil {
	
	/*
	 * ReasonFlags ::= BIT STRING {     - for CRL
	 * unused                   (0),
	 * keyCompromise            (1),
	 * cACompromise             (2),
	 * affiliationChanged       (3),
	 * superseded               (4),
	 * cessationOfOperation     (5),
	 * certificateHold          (6),
	 * privilegeWithdrawn       (7),
	 * aACompromise             (8) }
	 * 
	 * 
	 * OID "2.5.29.21" (id-ce-cRLReason) - for extensions
	 * reasonCode ::= { CRLReason }
	 * CRLReason ::= ENUMERATED {
	 * unspecified             (0),
	 * keyCompromise           (1),
	 * cACompromise            (2),
	 * affiliationChanged      (3),
	 * superseded              (4),
	 * cessationOfOperation    (5),
	 * certificateHold         (6),
	 * removeFromCRL           (8),
	 * privilegeWithdrawn      (9),
	 * aACompromise            (10) }
	 * 
	 * OID "2.5.29.29" (id-ce-certificateIssuer)
	 * certificateIssuer ::= GeneralNames
	 * 
	 * serialNumber         CertificateSerialNumber
	 * CertificateSerialNumber ::= INTEGER
	 * 
	 */
	
	private CertificateRevocationUtil(){}

}

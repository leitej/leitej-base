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

package leitej.xml;

import leitej.exception.XmlInvalidLtException;
import leitej.util.stream.FileUtil;

/**
 * XML - Tools
 *
 * @author Julio Leite
 */
final class XmlTools {

	static final String KEY_XML_ELEMENT_NAME = "xml";
	static final String[] CDATA_WRAP = { "<![CDATA[", "]]>" };
	static final String[] HDATA_WRAP = { "<![HDATA[", "]]>" };

	static final char ATTRIB_EQUAL = '=';
	static final char END_TAG_CHARACTER = '/';
	static final char SPACE_CHARACTER = ' ';
	static final char IDENT_CHARACTER = '\t';
	static final char META_DATA_CHARACTER_INIT = '?';
	static final char META_DATA_CHARACTER_END = '?';
	static final char COMMENT_CHARACTER_INIT_FIRST = '!';
	static final char COMMENT_CHARACTER_INIT_SECOND_THIRD = '-';
	static final char COMMENT_CHARACTER_END_FIRST_SECOND = '-';
	static final char SEMICOLON_CHARACTER = ';';
	private static final char DATA_INIT_FIRST = CDATA_WRAP[0].charAt(0);
	static final char DATA_INIT_LAST = CDATA_WRAP[0].charAt(CDATA_WRAP[0].length() - 1);
	static final char DATA_END_FIRST = CDATA_WRAP[1].charAt(0);

	static final char KEY_LESS_THAN = '<';
	static final char KEY_GREATER_THAN = '>';
	static final char KEY_AMPERSAND = '&';
	static final char KEY_APOSTROPHE = '\'';
	static final char KEY_QUOTATION_MARK = '\"';

	static final String KEY_LESS_THAN_ENTITY_REFERENCE = KEY_AMPERSAND + "lt" + SEMICOLON_CHARACTER;
	static final String KEY_GREATER_THAN_ENTITY_REFERENCE = KEY_AMPERSAND + "gt" + SEMICOLON_CHARACTER;
	static final String KEY_AMPERSAND_ENTITY_REFERENCE = KEY_AMPERSAND + "amp" + SEMICOLON_CHARACTER;
	static final String KEY_APOSTROPHE_ENTITY_REFERENCE = KEY_AMPERSAND + "apos" + SEMICOLON_CHARACTER;
	static final String KEY_QUOTATION_MARK_ENTITY_REFERENCE = KEY_AMPERSAND + "quot" + SEMICOLON_CHARACTER;

	static final String LINE_SEPARATOR = FileUtil.LINE_SEPARATOR;

	private static final Object[] RESERVED_KEY_MAP = { KEY_LESS_THAN, KEY_LESS_THAN_ENTITY_REFERENCE, KEY_GREATER_THAN,
			KEY_GREATER_THAN_ENTITY_REFERENCE, KEY_AMPERSAND, KEY_AMPERSAND_ENTITY_REFERENCE, KEY_APOSTROPHE,
			KEY_APOSTROPHE_ENTITY_REFERENCE, KEY_QUOTATION_MARK, KEY_QUOTATION_MARK_ENTITY_REFERENCE };

	private static final String RESERVED_KEYS = "" + KEY_LESS_THAN + KEY_GREATER_THAN + KEY_AMPERSAND + KEY_APOSTROPHE
			+ KEY_QUOTATION_MARK;
	private static final String[] RESERVED_ENTITIES_REFERENCE = { KEY_LESS_THAN_ENTITY_REFERENCE,
			KEY_GREATER_THAN_ENTITY_REFERENCE, KEY_AMPERSAND_ENTITY_REFERENCE, KEY_APOSTROPHE_ENTITY_REFERENCE,
			KEY_QUOTATION_MARK_ENTITY_REFERENCE };

	private XmlTools() {
	}

	/**
	 * Encodes value of element with the rules of XML.
	 *
	 * @param value to be encoded
	 * @throws XmlInvalidLtException If value has an invalid CDATA
	 */
	static void encod(final StringBuilder dest, final CharSequence value) throws XmlInvalidLtException {
		if (value != null) {
			char ci;
			int isCData = 0;
			int reservePos;
			for (int i = 0; i < value.length(); i++) {
				ci = value.charAt(i);
				if (isCData > 0) {
					if (DATA_INIT_FIRST == ci && containsAt(value, i, CDATA_WRAP[0])) {
						dest.append(CDATA_WRAP[0]);
						isCData++;
						i += (CDATA_WRAP[0].length() - 1);
					} else if (DATA_END_FIRST == ci && containsAt(value, i, CDATA_WRAP[1])) {
						dest.append(CDATA_WRAP[1]);
						isCData--;
						i += (CDATA_WRAP[1].length() - 1);
					} else {
						dest.append(ci);
					}
				} else {
					if (DATA_INIT_FIRST == ci && containsAt(value, i, CDATA_WRAP[0])) {
						dest.append(CDATA_WRAP[0]);
						isCData++;
						i += (CDATA_WRAP[0].length() - 1);
					} else {
						reservePos = RESERVED_KEYS.indexOf(ci);
						if (reservePos == -1) {
							dest.append(ci);
						} else {
							dest.append(RESERVED_ENTITIES_REFERENCE[reservePos]);
						}
					}
				}
			}
			if (isCData != 0) {
				throw new XmlInvalidLtException("invalid CDATA value");
			}
		}
	}

	/**
	 * Check if the subsequence starts at <code>off + 1</code> and ends at
	 * <code>off + object.length() - 1</code> from <code>charSequence</code> is
	 * equals to <code>object</code>.
	 *
	 * @param charSequence
	 * @param off          initial position (less one) of charSequence to compare
	 * @param object       to compare (except the first character)
	 * @return true if object is equals to the subsequence
	 */
	private static boolean containsAt(final CharSequence charSequence, final int off, final CharSequence object) {
		final boolean result;
		int j = 1;
		while (j < object.length() && j < (charSequence.length() - off)
				&& object.charAt(j) == charSequence.charAt(off + j)) {
			j++;
		}
		result = CDATA_WRAP[0].length() == j;
		return result;
	}

	/**
	 * Decodes value of element with the rules of XML.
	 *
	 * @param value to be decoded
	 */
	static void decod(final StringBuilder dest, final CharSequence value) {
		if (value != null) {
			char c;
			int initRef = -1;
			int endRef = 0;
			int initPlain = 0;
			int endPlain = 0;
			for (int i = 0; i < value.length(); i++) {
				c = value.charAt(i);
				if (initRef != -1) {
					endRef++;
					if (c == SEMICOLON_CHARACTER || i + 1 == value.length()) {
						c = convertEntityReference(value.subSequence(initRef, endRef));
						if (c != -1) {
							dest.append(c);
						} else {
							dest.append(value.subSequence(initRef, endRef));
						}
						initRef = -1;
						initPlain = i + 1;
						endPlain = i + 1;
					} else if (c == KEY_AMPERSAND) {
						dest.append(value.subSequence(initRef, endRef - 1));
						initRef = i;
						endRef = i + 1;
					}
				} else {
					if (c == KEY_AMPERSAND && i + 1 != value.length()) {
						dest.append(value.subSequence(initPlain, endPlain));
						initRef = i;
						endRef = i + 1;
					} else {
						endPlain++;
					}
				}
			}
			dest.append(value.subSequence(initPlain, endPlain));
		}
	}

	private static char convertEntityReference(final CharSequence eReference) {
		for (int i = 1; i < RESERVED_KEY_MAP.length; i = i + 2) {
			if (String.class.cast(RESERVED_KEY_MAP[i]).contentEquals(eReference)) {
				return Character.class.cast(RESERVED_KEY_MAP[i - 1]);
			}
		}
		return (char) -1;
	}

	/**
	 * Validates a name to be a element name according to XML rules.
	 *
	 * <p>
	 * <blockquote>
	 *
	 * <pre>
	 * XML elements must follow these naming rules:
	 *	Names can contain letters, numbers, and other characters
	 *	Names cannot start with a number or punctuation character
	 *	Names cannot start with the letters xml (or XML, or Xml, etc)
	 *	Names cannot contain spaces
	 * Any name can be used, no words are reserved.
	 * </pre>
	 *
	 * </blockquote>
	 * <p>
	 *
	 * @param elementName the name to be validated
	 * @throws XmlInvalidLtException If is a invalid tag or an invalid name element
	 */
	static void validatesElementName(final CharSequence elementName) throws XmlInvalidLtException {
		if (elementName == null || elementName.length() == 0) {
			throw new XmlInvalidLtException("Invalid XML element name '#0'", elementName);
		}
		if (!Character.isLetter(elementName.charAt(0))) {
			throw new XmlInvalidLtException("Invalid XML element name '#0'", elementName);
		}
		if (elementName.length() > 2
				&& XmlTools.KEY_XML_ELEMENT_NAME.charAt(0) == Character.toLowerCase(elementName.charAt(0))
				&& XmlTools.KEY_XML_ELEMENT_NAME.charAt(1) == Character.toLowerCase(elementName.charAt(1))
				&& XmlTools.KEY_XML_ELEMENT_NAME.charAt(2) == Character.toLowerCase(elementName.charAt(2))) {
			throw new XmlInvalidLtException("Invalid XML element name '#0'", elementName);
		}
		for (int i = 1; i < elementName.length(); i++) {
			if (Character.isWhitespace(elementName.charAt(i))) {
				throw new XmlInvalidLtException("Invalid XML element name '#0'", elementName);
			}
		}
	}

}

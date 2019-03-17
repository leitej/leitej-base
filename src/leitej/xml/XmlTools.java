/*******************************************************************************
 * Copyright (C) 2011 Julio Leite
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
	static final char 
		KEY_LESS_THAN = '<',
		KEY_GREATER_THAN = '>',
		KEY_AMPERSAND = '&',
		KEY_SEMICOLON = ';',
		KEY_APOSTROPHE = '\'',
		KEY_QUOTATION_MARK = '\"';
	static final String 
		KEY_LESS_THAN_ENTITY_REFERENCE = KEY_AMPERSAND + "lt" + KEY_SEMICOLON,
		KEY_GREATER_THAN_ENTITY_REFERENCE = KEY_AMPERSAND + "gt" + KEY_SEMICOLON,
		KEY_AMPERSAND_ENTITY_REFERENCE = KEY_AMPERSAND + "amp" + KEY_SEMICOLON,
		KEY_APOSTROPHE_ENTITY_REFERENCE = KEY_AMPERSAND + "apos" + KEY_SEMICOLON,
		KEY_QUOTATION_MARK_ENTITY_REFERENCE = KEY_AMPERSAND + "quot" + KEY_SEMICOLON;
	static final String LINE_SEPARATOR = FileUtil.LINE_SEPARATOR;
	static final char 
		ATTRIB_EQUAL = '=',
		END_TAG_CHARACTER = '/',
		SPACE_CHARACTER = ' ',
		IDENT_CHARACTER = '\t',
		META_DATA_CHARACTER_INIT = '?',
		META_DATA_CHARACTER_END = '?',
		COMMENT_CHARACTER_INIT_FIRST = '!',
		COMMENT_CHARACTER_INIT_SECOND_THIRD = '-',
		COMMENT_CHARACTER_END_FIRST_SECOND = '-';
	private static final Object[] reservedKey = {KEY_LESS_THAN, KEY_LESS_THAN_ENTITY_REFERENCE,
												 KEY_GREATER_THAN, KEY_GREATER_THAN_ENTITY_REFERENCE,
												 KEY_AMPERSAND, KEY_AMPERSAND_ENTITY_REFERENCE,
												 KEY_APOSTROPHE, KEY_APOSTROPHE_ENTITY_REFERENCE,
												 KEY_QUOTATION_MARK, KEY_QUOTATION_MARK_ENTITY_REFERENCE};

	private XmlTools() {
	}

	/**
	 * Encodes value of element with the rules of XML.
	 *
	 * @param value to be encoded
	 */
	static void encod(final StringBuilder dest, final CharSequence value) {
		if (value != null) {
			for (int i = 0; i < value.length(); i++) {
				dest.append(convertReservedKey(value.charAt(i)));
			}
		}
	}

	private static Object convertReservedKey(final char c) {
		for (int i = 0; i < reservedKey.length; i = i + 2) {
			if (((Character) reservedKey[i]) == c) {
				return reservedKey[i + 1];
			}
		}
		return c;
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
					if (c == KEY_SEMICOLON || i + 1 == value.length()) {
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
		for (int i = 1; i < reservedKey.length; i = i + 2) {
			if (String.class.cast(reservedKey[i]).contentEquals(eReference)) {
				return Character.class.cast(reservedKey[i - 1]);
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
			throw new XmlInvalidLtException("lt.XmlInvalidElementName", elementName);
		}
		if (!Character.isLetter(elementName.charAt(0))) {
			throw new XmlInvalidLtException("lt.XmlInvalidElementName", elementName);
		}
		if (elementName.length() > 2
				&& XmlTools.KEY_XML_ELEMENT_NAME.charAt(0) == Character.toLowerCase(elementName.charAt(0))
				&& XmlTools.KEY_XML_ELEMENT_NAME.charAt(1) == Character.toLowerCase(elementName.charAt(1))
				&& XmlTools.KEY_XML_ELEMENT_NAME.charAt(2) == Character.toLowerCase(elementName.charAt(2))) {
			throw new XmlInvalidLtException("lt.XmlInvalidElementName", elementName);
		}
		for (int i = 1; i < elementName.length(); i++) {
			if (Character.isWhitespace(elementName.charAt(i))) {
				throw new XmlInvalidLtException("lt.XmlInvalidElementName", elementName);
			}
		}
	}

}

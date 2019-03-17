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

/**
 * XML - Producer Tools
 * <p>
 * Methods to facilitate the generation of elements.
 *
 * @author Julio Leite
 * @see leitej.xml.XmlProducer
 */
final class XmlProducerTools {

	private final StringBuilder VERSION = new StringBuilder("1.0");
	private final StringBuilder NAME_VERSION = new StringBuilder("version");
	private final StringBuilder NAME_ENCODING = new StringBuilder("encoding");

	XmlProducerTools() {
	}

	/**
	 * Generates the meta data.
	 *
	 * @param encoding used on writing the XML
	 * @return Meta data well formatted
	 */
	void genMetaData(final StringBuilder dest, final CharSequence encoding) {
		dest.append(XmlTools.KEY_LESS_THAN);
		dest.append(XmlTools.META_DATA_CHARACTER_INIT);
		dest.append(XmlTools.KEY_XML_ELEMENT_NAME);
		genAttribute(dest, this.NAME_VERSION, this.VERSION);
		if (encoding != null && encoding.length() > 0) {
			genAttribute(dest, this.NAME_ENCODING, encoding);
		}
		dest.append(XmlTools.META_DATA_CHARACTER_END);
		dest.append(XmlTools.KEY_GREATER_THAN);
	}

	/**
	 * Generates an attribute.
	 *
	 * @param name  Attribute name
	 * @param value Attribute value
	 * @return Attribute well formatted to be inserted on a open tag
	 */
	void genAttribute(final StringBuilder dest, final CharSequence name, final CharSequence value) {
		dest.append(XmlTools.SPACE_CHARACTER);
		dest.append(name);
		dest.append(XmlTools.ATTRIB_EQUAL);
		dest.append(XmlTools.KEY_QUOTATION_MARK);
		XmlTools.encod(dest, value);
		dest.append(XmlTools.KEY_QUOTATION_MARK);
	}

	/**
	 * Generates an opening tag.
	 *
	 * @param indent         Number of tabs to indent this tag
	 * @param elementName    tag name
	 * @param generatedAttrs the attributes already formated
	 * @throws XmlInvalidLtException If is generating a tag with a invalid element
	 *                               name
	 */
	void genElementTagOpen(final StringBuilder dest, final Integer indent, final CharSequence elementName,
			final CharSequence generatedAttrs) throws XmlInvalidLtException {
		genElementTagOpen(dest, indent, false, elementName, generatedAttrs);
	}

	/**
	 * Generates an element without value (null value).
	 *
	 * @param indent         Number of tabs to indent this tag
	 * @param elementName    tag name
	 * @param generatedAttrs the attributes already formated
	 * @throws XmlInvalidLtException If is generating a tag with a invalid element
	 *                               name
	 */
	void genElementTagOpenClose(final StringBuilder dest, final Integer indent, final CharSequence elementName,
			final CharSequence generatedAttrs) throws XmlInvalidLtException {
		genElementTagOpen(dest, indent, true, elementName, generatedAttrs);
	}

	private void genElementTagOpen(final StringBuilder dest, final Integer indent, final boolean openClose,
			final CharSequence elementName, final CharSequence generatedAttrs) throws XmlInvalidLtException {
		XmlTools.validatesElementName(elementName);
		for (int i = 0; i < indent.intValue(); i++) {
			dest.append(XmlTools.IDENT_CHARACTER);
		}
		dest.append(XmlTools.KEY_LESS_THAN);
		dest.append(elementName);
		if (generatedAttrs != null) {
			dest.append(generatedAttrs);
		}
		if (openClose) {
			dest.append(XmlTools.END_TAG_CHARACTER);
		}
		dest.append(XmlTools.KEY_GREATER_THAN);
	}

	/**
	 * Generates a closing tag.
	 *
	 * @param indent      Number of tabs to indent this tag
	 * @param elementName tag name
	 * @throws XmlInvalidLtException If is generating a tag with a invalid element
	 *                               name
	 */
	void genElementTagClose(final StringBuilder dest, final Integer indent, final CharSequence elementName)
			throws XmlInvalidLtException {
		XmlTools.validatesElementName(elementName);
		for (int i = 0; i < indent.intValue(); i++) {
			dest.append(XmlTools.IDENT_CHARACTER);
		}
		dest.append(XmlTools.KEY_LESS_THAN);
		dest.append(XmlTools.END_TAG_CHARACTER);
		dest.append(elementName);
		dest.append(XmlTools.KEY_GREATER_THAN);
	}

	/**
	 * Generates a comment tag.
	 *
	 * @param indent  Number of tabs to indent this tag
	 * @param comment the comment
	 */
	void genCommentTag(final StringBuilder dest, final Integer indent, final CharSequence comment) {
		for (int i = 0; i < indent.intValue(); i++) {
			dest.append(XmlTools.IDENT_CHARACTER);
		}
		dest.append(XmlTools.KEY_LESS_THAN);
		dest.append(XmlTools.COMMENT_CHARACTER_INIT_FIRST);
		dest.append(XmlTools.COMMENT_CHARACTER_INIT_SECOND_THIRD);
		dest.append(XmlTools.COMMENT_CHARACTER_INIT_SECOND_THIRD);
		XmlTools.encod(dest, comment);
		dest.append(XmlTools.COMMENT_CHARACTER_END_FIRST_SECOND);
		dest.append(XmlTools.COMMENT_CHARACTER_END_FIRST_SECOND);
		dest.append(XmlTools.KEY_GREATER_THAN);
	}

	void encod(final StringBuilder dest, final CharSequence value) {
		XmlTools.encod(dest, value);
	}

}

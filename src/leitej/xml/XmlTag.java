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

import java.util.Map;
import java.util.TreeMap;

import leitej.exception.XmlInvalidLtException;

/**
 *
 * @author Julio Leite
 */
final class XmlTag {

	private final StringBuilder tag;
	private boolean isInvalid;
	private XmlTagType type;
	private CharSequence name;
	private CharSequence comment;
	private final Map<String, Object> attribMap;

	/**
	 * Instantiates a new empty tag.
	 */
	XmlTag() {
		this.tag = new StringBuilder();
		this.attribMap = new TreeMap<>();
		init();
	}

	/**
	 * Initiates the state of this instance to make a new tag.
	 */
	void init() {
		this.isInvalid = true;
		this.type = null;
		this.name = null;
		this.comment = null;
		this.attribMap.clear();
		this.tag.setLength(0);
	}

	/**
	 * Appends the string representation of the {@code char} argument to this
	 * sequence.
	 * <p>
	 * The argument is appended to the contents of this sequence. The length of this
	 * sequence increases by {@code 1}.
	 * <p>
	 * The overall effect is exactly as if the argument were converted to a string
	 * by the method {@link String#valueOf(char)}, and the character in that string
	 * were then {@link #append(String) appended} to this character sequence.
	 *
	 * @param c a {@code char}.
	 */
	void append(final char c) {
		this.tag.append(c);
	}

	/**
	 * Loads and validates the appended tag data.
	 *
	 * @return false if is an invalid tag
	 */
	boolean load() {
		this.isInvalid = !XmlConsumerTools.validatesTag(this.tag);
		return !this.isInvalid;
	}

	/**
	 * Obtain the type of this tag.
	 *
	 * @return tag type or null if fail to recognize it
	 */
	XmlTagType getXmlTagType() {
		if (this.type == null && this.tag.length() > 0) {
			this.type = XmlConsumerTools.parseXmlTagType(this.tag);
		}
		return this.type;
	}

	/**
	 * Get the name of the tag.
	 *
	 * @return the name
	 * @throws XmlInvalidLtException If is not a valid tag or is an invalid name
	 *                               element
	 */
	CharSequence getName() throws XmlInvalidLtException {
		if (this.isInvalid) {
			throw new XmlInvalidLtException("Invalid xml tag: #0", this.tag);
		}
		if (this.name == null && (getXmlTagType().equals(XmlTagType.OPEN) || getXmlTagType().equals(XmlTagType.CLOSE)
				|| getXmlTagType().equals(XmlTagType.OPEN_CLOSE))) {
			this.name = XmlConsumerTools.getElementName(this.tag);
		}
		return this.name;
	}

	/**
	 * Obtains the comment of the tag.
	 *
	 * @param dest to write the comment
	 * @throws XmlInvalidLtException If is not a valid tag
	 */
	void getComment(final StringBuilder dest) throws XmlInvalidLtException {
		if (this.isInvalid) {
			throw new XmlInvalidLtException("Invalid xml tag: #0", this.tag);
		}
		if (XmlTagType.COMMENT.equals(getXmlTagType())) {
			if (this.comment == null) {
				this.comment = XmlConsumerTools.getElementComment(this.tag);
			}
			XmlTools.decod(dest, this.comment);
		}
	}

	/**
	 * Obtains the attribute value by tag.
	 *
	 * @param dest               to write the value
	 * @param attributeNameToGet the name of attribute
	 * @return false if does not exists
	 * @throws XmlInvalidLtException If is an invalid tag
	 */
	boolean getElementAttributeValue(final StringBuilder dest, final String attributeNameToGet)
			throws XmlInvalidLtException {
		if (this.isInvalid) {
			throw new XmlInvalidLtException("Invalid xml tag: #0", this.tag);
		}
		boolean result = false;
		if (getXmlTagType().equals(XmlTagType.OPEN) || getXmlTagType().equals(XmlTagType.OPEN_CLOSE)) {
			Object value = this.attribMap.get(attributeNameToGet);
			if (value == null) {
				value = XmlConsumerTools.getElementAttributeValue(this.tag, attributeNameToGet);
				if (value == null) {
					value = Boolean.FALSE;
				}
				this.attribMap.put(attributeNameToGet, value);
			}
			if (value != null && CharSequence.class.isInstance(value)) {
				XmlTools.decod(dest, CharSequence.class.cast(value));
				result = true;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return this.tag.toString();
	}

}

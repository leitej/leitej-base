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
	private Boolean isMetaData;
	private Boolean isComment;
	private Boolean isOpen;
	private Boolean isClose;
	private Boolean isOpenClose;
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
	final void init() {
		this.isInvalid = true;
		this.isMetaData = null;
		this.isComment = null;
		this.isOpen = null;
		this.isClose = null;
		this.isOpenClose = null;
		this.name = null;
		this.comment = null;
		this.attribMap.clear();
		this.tag.setLength(0);
	}

	/**
	 * Appends the string representation of a subarray of the <code>char</code>
	 * array argument to this tag.
	 * <p>
	 * Characters of the <code>char</code> array <code>str</code>, starting at index
	 * <code>offset</code>, are appended, in order, to the contents of this
	 * sequence. The length of this sequence increases by the value of
	 * <code>len</code>.
	 *
	 * @param str    the characters to be appended.
	 * @param offset the index of the first <code>char</code> to append.
	 * @param len    the number of <code>char</code>s to append.
	 */
	final void append(final char[] str, final int offset, final int len) {
		this.tag.append(str, offset, len);
	}

	/**
	 * @throws XmlInvalidLtException If is not a valid tag
	 */
	private final void validatesTag() throws XmlInvalidLtException {
		XmlConsumerTools.validatesTag(this.tag);
		this.isInvalid = false;
	}

	/**
	 *
	 * @return true if is a metadata tag
	 * @throws XmlInvalidLtException If is not a valid tag
	 */
	final boolean isMetaData() throws XmlInvalidLtException {
		if (this.isInvalid) {
			validatesTag();
		}
		if (this.isMetaData == null) {
			this.isMetaData = Boolean.valueOf(XmlConsumerTools.isTagMetaData(this.tag));
		}
		return this.isMetaData.booleanValue();
	}

	/**
	 *
	 * @return true if is a comment tag
	 * @throws XmlInvalidLtException If is not a valid tag
	 */
	final boolean isComment() throws XmlInvalidLtException {
		if (this.isInvalid) {
			validatesTag();
		}
		if (this.isComment == null) {
			this.isComment = Boolean.valueOf(XmlConsumerTools.isTagComment(this.tag));
		}
		return this.isComment.booleanValue();
	}

	/**
	 *
	 * @return true if is an open tag
	 * @throws XmlInvalidLtException If is not a valid tag
	 */
	final boolean isOpen() throws XmlInvalidLtException {
		if (this.isInvalid) {
			validatesTag();
		}
		if (this.isOpen == null) {
			this.isOpen = Boolean.valueOf(XmlConsumerTools.isElementTagOpen(this.tag));
		}
		return this.isOpen.booleanValue();
	}

	/**
	 *
	 * @return true if is a close tag
	 * @throws XmlInvalidLtException If is not a valid tag
	 */
	final boolean isClose() throws XmlInvalidLtException {
		if (this.isInvalid) {
			validatesTag();
		}
		if (this.isClose == null) {
			this.isClose = Boolean.valueOf(XmlConsumerTools.isElementTagClose(this.tag));
		}
		return this.isClose.booleanValue();
	}

	/**
	 *
	 * @return true if is an open_close tag
	 * @throws XmlInvalidLtException If is not a valid tag
	 */
	final boolean isOpenClose() throws XmlInvalidLtException {
		if (this.isInvalid) {
			validatesTag();
		}
		if (this.isOpenClose == null) {
			this.isOpenClose = Boolean.valueOf(XmlConsumerTools.isElementTagOpenClose(this.tag));
		}
		return this.isOpenClose.booleanValue();
	}

	/**
	 * Get the name of the tag.
	 *
	 * @return the name
	 * @throws XmlInvalidLtException If is not a valid tag or is an invalid name
	 *                               element
	 */
	final CharSequence getName() throws XmlInvalidLtException {
		if (this.isInvalid) {
			validatesTag();
		}
		if (this.name == null) {
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
	final void getComment(final StringBuilder dest) throws XmlInvalidLtException {
		// the next line has the validation - if(isInvalid) validatesTag();
		if (isComment()) {
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
	final boolean getElementAttributeValue(final StringBuilder dest, final String attributeNameToGet)
			throws XmlInvalidLtException {
		if (this.isInvalid) {
			validatesTag();
		}
		Object value = this.attribMap.get(attributeNameToGet);
		if (value == null) {
			value = XmlConsumerTools.getElementAttributeValue(this.tag, attributeNameToGet);
			if (value == null) {
				value = Boolean.FALSE;
			}
			this.attribMap.put(attributeNameToGet, value);
		}
		boolean result;
		if (CharSequence.class.isInstance(value)) {
			XmlTools.decod(dest, CharSequence.class.cast(value));
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	@Override
	public String toString() {
		return this.tag.toString();
	}

}

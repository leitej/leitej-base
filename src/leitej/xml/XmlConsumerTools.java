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
import leitej.util.StringUtil;

/**
 * XML - Consumer Tools
 * <p>
 * Methods to facilitate the parsing of elements.
 *
 * @author Julio Leite
 * @see leitej.xml.XmlConsumer
 */
final class XmlConsumerTools {

	private XmlConsumerTools() {
	}

	/**
	 * Validates if is a tag. (Only checks if has the symbols of open and close tag
	 * at the right position)
	 *
	 * @param tag to be verified
	 * @return true if is checked OK
	 */
	static final boolean validatesTag(final CharSequence tag) {
		return (tag != null && tag.length() > 3 && XmlTools.KEY_LESS_THAN == tag.charAt(0)
				&& (XmlTools.KEY_GREATER_THAN == tag.charAt(tag.length() - 1)) || tag.equals(XmlTools.CDATA_WRAP[0])
				|| tag.equals(XmlTools.HDATA_WRAP[0]));
	}

	/**
	 *
	 * Validates if is a meta-data tag. (Only checks the initial symbol)
	 *
	 * Validates if is a comment tag. (Only checks the initial symbol)
	 *
	 * Validates if is a close tag. (Only checks the initial symbol)
	 *
	 * Validates if is an open_close tag. (Only checks the final symbol and if is a
	 * open tag at same time)
	 *
	 * Validates if is a CDATA tag. (expects tag.equals(XmlTools.CDATA_WRAP[0]))
	 *
	 * Validates if is a HDATA tag. (expects tag.equals(XmlTools.HDATA_WRAP[0]))
	 *
	 * @param tag the meta-data tag to be verified
	 * @return null if is not defined
	 */
	static XmlTagType parseXmlTagType(final CharSequence tag) {
		final XmlTagType result;
		if (XmlTools.META_DATA_CHARACTER_INIT == tag.charAt(1)) {
			result = XmlTagType.META_DATA;
		} else if (XmlTools.COMMENT_CHARACTER_INIT_FIRST == tag.charAt(1)) {
			if (XmlTools.COMMENT_CHARACTER_INIT_SECOND_THIRD == tag.charAt(2)
					&& XmlTools.COMMENT_CHARACTER_INIT_SECOND_THIRD == tag.charAt(3)) {
				result = XmlTagType.COMMENT;
			} else if (tag.equals(XmlTools.CDATA_WRAP[0])) {
				result = XmlTagType.CDATA;
			} else if (tag.equals(XmlTools.HDATA_WRAP[0])) {
				result = XmlTagType.HDATA;
			} else {
				result = null;
			}
		} else if (XmlTools.END_TAG_CHARACTER == tag.charAt(1)) {
			result = XmlTagType.CLOSE;
		} else if (isElementTagOpen(tag)) {
			if (XmlTools.END_TAG_CHARACTER == tag.charAt(tag.length() - 2)) {
				result = XmlTagType.OPEN_CLOSE;
			} else {
				result = XmlTagType.OPEN;
			}
		} else {
			result = null;
		}
		return result;
	}

	/**
	 * Validates if is an open tag.
	 *
	 * @param tag the open tag to be verified
	 * @return boolean validation
	 */
	private static final boolean isElementTagOpen(final CharSequence tag) {
		Boolean result = null;
		for (int i = 1; i < tag.length() - 1 && result == null; i++) {
			if (Character.isLetter(tag.charAt(i))) {
				result = Boolean.TRUE;
			}
			if (!Character.isLetter(tag.charAt(i)) && !Character.isWhitespace(tag.charAt(i))) {
				result = Boolean.FALSE;
			}
		}
		return result.booleanValue();
	}

	/**
	 * Get the name of the element of <code>tag</code>.
	 *
	 * @param tag
	 * @return tag name
	 * @throws XmlInvalidLtException If is an invalid name element
	 */
	static final CharSequence getElementName(final CharSequence tag) throws XmlInvalidLtException {
		int pos = ((XmlTools.END_TAG_CHARACTER == tag.charAt(1)) ? 2 : 1);
		while (Character.isWhitespace(tag.charAt(pos))) {
			pos++;
		}
		final int init = pos;
		while (XmlTools.KEY_GREATER_THAN != tag.charAt(pos) && !Character.isWhitespace(tag.charAt(pos))) {
			pos++;
		}
		final CharSequence result = tag.subSequence(init, pos);
		XmlTools.validatesElementName(result);
		return result;
	}

	/**
	 * Get the raw comment of <code>commentTag</code>.
	 *
	 * @param commentTag
	 * @return tag comment without decode
	 */
	static final CharSequence getElementComment(final CharSequence commentTag) {
		final int init = 4;
		int pos = init;
		while (XmlTools.KEY_GREATER_THAN != commentTag.charAt(pos)) {
			pos++;
		}
		return commentTag.subSequence(init, pos - 2);
	}

	/**
	 * Obtains the attribute raw value in the <code>tag<code>.
	 *
	 * @param tag
	 * @param attributeName the name of attribute
	 * @return the attribute value without decode or null if does not exist
	 */
	static final CharSequence getElementAttributeValue(final StringBuilder tag, final String attributeName) {
		CharSequence result = null;
		if (!StringUtil.isNullOrEmpty(attributeName)) {
			// find end position of name
			int endNameTagPos = ((XmlTools.END_TAG_CHARACTER == tag.charAt(1)) ? 2 : 1);
			while (Character.isWhitespace(tag.charAt(endNameTagPos))) {
				endNameTagPos++;
			}
			while (XmlTools.KEY_GREATER_THAN != tag.charAt(endNameTagPos)
					&& !Character.isWhitespace(tag.charAt(endNameTagPos))) {
				endNameTagPos++;
			}
			// find attribute
			boolean expectEquals = false;
			boolean expectValue = false;
			boolean fakeMatch = false;
			int index = endNameTagPos;
			int valueIndexEnd = 0;
			char attributeValueDelimiter;
			while (result == null && index != -1) {
				index = tag.indexOf(attributeName, index);
				if (index != -1 && Character.isWhitespace(tag.charAt(index - 1))) {
					expectEquals = true;
					expectValue = false;
					fakeMatch = false;
					index += attributeName.length();
					for (int i = index; i < tag.length() && !fakeMatch && result == null; i++) {
						while (Character.isWhitespace(tag.charAt(i))) {
							i++;
						}
						if (expectEquals && XmlTools.ATTRIB_EQUAL == tag.charAt(i)) {
							expectEquals = false;
							expectValue = true;
						} else if (expectValue && (XmlTools.KEY_QUOTATION_MARK == tag.charAt(i)
								|| XmlTools.KEY_APOSTROPHE == tag.charAt(i))) {
							attributeValueDelimiter = tag.charAt(i); // the KEY_QUOTATION_MARK or KEY_APOSTROPHE
							valueIndexEnd = i + 1;
							while (valueIndexEnd != tag.length()
									&& attributeValueDelimiter != tag.charAt(valueIndexEnd)) {
								valueIndexEnd++;
							}
							if (valueIndexEnd != tag.length()) {
								result = tag.subSequence(i + 1, valueIndexEnd);
							} else {
								fakeMatch = true;
							}
						} else {
							fakeMatch = true;
						}
					}
				}
			}
		}
		return result;
	}

}

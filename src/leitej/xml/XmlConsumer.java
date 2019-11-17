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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;

import leitej.exception.IllegalStateLtRtException;
import leitej.exception.XmlInvalidLtException;

/**
 * XML Consumer
 *
 * @author Julio Leite
 * @see leitej.xml.XmlProducer
 */
public final class XmlConsumer {

	private static final char EOF_CHAR = (char) -1;
	private static final int BUFFERED_READER_SIZE = 32;

	private BufferedReader in;
	private final char[] buffer;
	private final Stack<String> tagTrack;
	private XmlTag currTag;
	private final StringBuilder betweenTagRawValue;
	private XmlTag nextTag;
	private boolean consumeNextTag;
	private boolean hitStartTagElementRoot;

	/**
	 * Creates a new instance of XMLConsumer.
	 *
	 * @param isr the underlying input stream
	 */
	public XmlConsumer(final InputStreamReader isr) {
		this.in = new BufferedReader(isr, BUFFERED_READER_SIZE);
		if (!this.in.markSupported()) {
			throw new IllegalStateLtRtException();
		}
		this.buffer = new char[BUFFERED_READER_SIZE];
		this.tagTrack = new Stack<>();
		this.currTag = new XmlTag();
		this.betweenTagRawValue = new StringBuilder();
		this.nextTag = new XmlTag();
		this.consumeNextTag = true;
		this.hitStartTagElementRoot = false;
	}

	/**
	 * Closes the stream XML.
	 *
	 * @throws IOException If an I/O error occurs
	 */
	public synchronized void close() throws IOException {
		if (this.in != null) {
			this.in.close();
			this.in = null;
		}
	}

	/**
	 * Checks that have already consumed the closing tag of the root.
	 *
	 * @return boolean
	 */
	public synchronized boolean isAlreadyConsumedEndTagElementRoot() {
		return this.hitStartTagElementRoot && this.tagTrack.isEmpty();
	}

	/**
	 * Get the element value corresponding to the last consumed tag.
	 *
	 * @param dest to write the value
	 * @throws XmlInvalidLtException If encounter an invalid syntax
	 * @throws IOException           If an I/O error occurs
	 */
	public synchronized void getElementValue(final StringBuilder dest) throws XmlInvalidLtException, IOException {
		updateNextTag();
		XmlTools.decod(dest, this.betweenTagRawValue);
	}

	/**
	 * Takes the next tag.
	 *
	 * @throws XmlInvalidLtException If encounter an invalid syntax
	 * @throws IOException           If an I/O error occurs
	 */
	public synchronized void consumeTag() throws XmlInvalidLtException, IOException {
		updateNextTag();
		if (!isAlreadyConsumedEndTagElementRoot()) {
			if (isNextTagClose()) {
				if (!this.tagTrack.pop().contentEquals(this.nextTag.getName())) {
					throw new XmlInvalidLtException("lt.XmlInvalidEndTag", this.nextTag);
				}
			} else if (isNextTagOpen() && !isNextTagOpenClose()) {
				this.tagTrack.push(this.nextTag.getName().toString());
			}
			// verify that started the root element
			if (!this.hitStartTagElementRoot && this.nextTag.isOpen()) {
				this.hitStartTagElementRoot = true;
			}
			final XmlTag tmp = this.currTag;
			this.currTag = this.nextTag;
			this.nextTag = tmp;
			this.betweenTagRawValue.setLength(0);
			this.nextTag.init();
			this.consumeNextTag = true;
		}
	}

	/**
	 *
	 * @throws XmlInvalidLtException If encounter an invalid syntax
	 * @throws IOException           If an I/O error occurs
	 */
	private void updateNextTag() throws IOException, XmlInvalidLtException {
		if (this.consumeNextTag) {
			if (!isAlreadyConsumedEndTagElementRoot()) {
				readBetweenTagRawValue();
				readNextTag();
			}
			this.consumeNextTag = false;
		}
	}

	/**
	 *
	 * @throws XmlInvalidLtException If encounter an invalid syntax
	 * @throws IOException           If an I/O error occurs
	 */
	private void readNextTag() throws XmlInvalidLtException, IOException {
		int count = 0;
		int cicle = 0;
		int numRead;
		int len;
		char ch = Character.MIN_VALUE;
		while (XmlTools.KEY_GREATER_THAN != ch && EOF_CHAR != ch) {
			if (count == 0) {
				if (cicle == 0) {
					this.in.mark(BUFFERED_READER_SIZE);
					if (XmlTools.KEY_LESS_THAN != ((char) this.in.read())) {
						throw new XmlInvalidLtException("lt.XmlInvalid");
					} else {
						count++;
					}
				} else {
					this.in.reset();
					while ((numRead = this.in.read(this.buffer, count, BUFFERED_READER_SIZE - count)) >= 0
							&& count < BUFFERED_READER_SIZE) {
						count += numRead;
					}
					if (count < BUFFERED_READER_SIZE) {
						throw new XmlInvalidLtException("lt.XmlInvalidStreamEnd");
					} else {
						this.nextTag.append(this.buffer, 0, BUFFERED_READER_SIZE);
						count = 0;
						this.in.mark(BUFFERED_READER_SIZE);
					}
				}
			}
			ch = (char) this.in.read();
			if (XmlTools.KEY_LESS_THAN == ch) {
				throw new XmlInvalidLtException("lt.XmlInvalid");
			} else {
				count++;
				if (count == BUFFERED_READER_SIZE) {
					count = 0;
					cicle++;
				}
			}
		}
		if (EOF_CHAR == ch) {
			throw new XmlInvalidLtException("lt.XmlInvalidStreamEnd");
		} else {
			if (count == 0) {
				len = BUFFERED_READER_SIZE;
			} else {
				len = count;
				count = 0;
			}
			this.in.reset();
			while ((numRead = this.in.read(this.buffer, count, len - count)) >= 0 && count < len) {
				count += numRead;
			}
			if (count < len) {
				throw new XmlInvalidLtException("lt.XmlInvalidStreamEnd");
			} else {
				this.nextTag.append(this.buffer, 0, len);
			}
		}
	}

	/**
	 *
	 * @throws XmlInvalidLtException If encounter an invalid syntax
	 * @throws IOException           If an I/O error occurs
	 */
	private void readBetweenTagRawValue() throws XmlInvalidLtException, IOException {
		int count = 0;
		int cicle = 0;
		int numRead;
		int len;
		char ch = Character.MIN_VALUE;
		while (XmlTools.KEY_LESS_THAN != ch && EOF_CHAR != ch) {
			if (count == 0) {
				if (cicle != 0) {
					this.in.reset();
					while ((numRead = this.in.read(this.buffer, count, BUFFERED_READER_SIZE - count)) >= 0
							&& count < BUFFERED_READER_SIZE) {
						count += numRead;
					}
					if (count < BUFFERED_READER_SIZE) {
						throw new XmlInvalidLtException("lt.XmlInvalidStreamEnd");
					} else {
						this.betweenTagRawValue.append(this.buffer, 0, BUFFERED_READER_SIZE);
						count = 0;
					}
				}
				this.in.mark(BUFFERED_READER_SIZE);
			}
			ch = (char) this.in.read();
			if (XmlTools.KEY_GREATER_THAN == ch) {
				throw new XmlInvalidLtException("lt.XmlInvalid");
			} else {
				count++;
				if (count == BUFFERED_READER_SIZE) {
					count = 0;
					cicle++;
				}
			}
		}
		if (count == 0) {
			len = BUFFERED_READER_SIZE;
		} else {
			len = count;
			count = 0;
		}
		if (XmlTools.KEY_LESS_THAN == ch) {
			len--;
		}
		this.in.reset();
		while ((numRead = this.in.read(this.buffer, count, len - count)) >= 0 && count < len) {
			count += numRead;
		}
		if (count < len) {
			throw new XmlInvalidLtException("lt.XmlInvalidStreamEnd");
		} else {
			this.betweenTagRawValue.append(this.buffer, 0, len);
		}
	}

	/**
	 * Checks whether the next tag is a meta-data tag.
	 *
	 * @return boolean
	 * @throws XmlInvalidLtException If encounter an invalid syntax
	 * @throws IOException           If an I/O error occurs
	 */
	public synchronized boolean isNextTagMetaData() throws XmlInvalidLtException, IOException {
		updateNextTag();
		return this.nextTag.isMetaData();
	}

	/**
	 * Checks whether the next tag is a comment tag.
	 *
	 * @return boolean
	 * @throws XmlInvalidLtException If encounter an invalid syntax
	 * @throws IOException           If an I/O error occurs
	 */
	public synchronized boolean isNextTagComment() throws XmlInvalidLtException, IOException {
		updateNextTag();
		return this.nextTag.isComment();
	}

	/**
	 * Checks whether the next tag is a close tag.
	 *
	 * @return boolean
	 * @throws XmlInvalidLtException If encounter an invalid syntax
	 * @throws IOException           If an I/O error occurs
	 */
	public synchronized boolean isNextTagClose() throws XmlInvalidLtException, IOException {
		updateNextTag();
		return this.nextTag.isClose();
	}

	/**
	 * Checks whether the next tag is an open tag.
	 *
	 * @return boolean
	 * @throws XmlInvalidLtException If encounter an invalid syntax
	 * @throws IOException           If an I/O error occurs
	 */
	public synchronized boolean isNextTagOpen() throws XmlInvalidLtException, IOException {
		updateNextTag();
		return this.nextTag.isOpen();
	}

	/**
	 * Checks whether the next tag is an open and close tag (element without value).
	 *
	 * @return boolean
	 * @throws XmlInvalidLtException If encounter an invalid syntax
	 * @throws IOException           If an I/O error occurs
	 */
	public synchronized boolean isNextTagOpenClose() throws XmlInvalidLtException, IOException {
		updateNextTag();
		return this.nextTag.isOpenClose();
	}

	/**
	 * Checks whether the current tag is an open tag.
	 *
	 * @return boolean
	 * @throws XmlInvalidLtException If is not a valid tag
	 */
	public synchronized boolean isTagOpen() throws XmlInvalidLtException {
		return this.currTag.isOpen();
	}

	/**
	 * Checks whether the current tag is a close tag.
	 *
	 * @return boolean
	 * @throws XmlInvalidLtException If is not a valid tag
	 */
	public synchronized boolean isTagClose() throws XmlInvalidLtException {
		return this.currTag.isClose();
	}

	/**
	 * Checks whether the current tag is an open and close tag (element without
	 * value).
	 *
	 * @return boolean
	 * @throws XmlInvalidLtException If is not a valid tag
	 */
	public synchronized boolean isTagOpenClose() throws XmlInvalidLtException {
		return this.currTag.isOpenClose();
	}

	/**
	 * Get the name of the current tag.
	 *
	 * @throws XmlInvalidLtException If is not a valid tag or is an invalid name
	 *                               element
	 */
	public synchronized CharSequence getElementName() throws XmlInvalidLtException {
		return this.currTag.getName();
	}

	/**
	 * Get the comment of the current tag.
	 *
	 * @param dest to write the comment
	 * @throws XmlInvalidLtException If is not a valid tag
	 */
	public synchronized void getComment(final StringBuilder dest) throws XmlInvalidLtException {
		this.currTag.getComment(dest);
	}

	/**
	 * Obtains the attribute value of the current tag.
	 *
	 * @param dest               to write the value
	 * @param attributeNameToGet the name of attribute
	 * @return false if does not exists
	 * @throws XmlInvalidLtException If is an invalid tag
	 */
	public synchronized boolean getElementAttributeValue(final StringBuilder dest, final String attributeNameToGet)
			throws XmlInvalidLtException {
		return this.currTag.getElementAttributeValue(dest, attributeNameToGet);
	}

	/**
	 * Exposes the current tag.
	 */
	@Override
	public String toString() {
		return this.currTag.toString();
	}

}

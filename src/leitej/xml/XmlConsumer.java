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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Stack;

import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.XmlInvalidLtException;
import leitej.util.stream.StreamUtil;

/**
 * XML Consumer
 *
 * @author Julio Leite
 * @see leitej.xml.XmlProducer
 *
 */
public final class XmlConsumer implements Closeable {

	private static final char EOF_CHAR = (char) -1;

	private BufferedReader in;
	private final Stack<CharSequence> tagTrack;
	private XmlTag curTag;
	private final StringBuilder curData;
	private XmlTag nextTag;
	private Writer osComment;
	private Writer osCData;
	private OutputStream osHData;

	/**
	 * Creates a new instance of XMLConsumer.
	 *
	 * @param isr the underlying input stream
	 */
	public XmlConsumer(final InputStreamReader isr) {
		this.in = new BufferedReader(isr);
		this.tagTrack = new Stack<>();
		this.curTag = new XmlTag();
		this.curData = new StringBuilder();
		this.nextTag = new XmlTag();
		this.osComment = null;
		this.osCData = null;
		this.osHData = null;
	}

	/**
	 *
	 * @return false if do not have next or is invalid
	 * @throws IOException If an I/O error occurs
	 */
	public synchronized boolean nextElement() throws IOException {
		boolean result = false;
		// prepare to read the stream
		final XmlTag tmpNextTag = this.curTag;
		this.curTag = this.nextTag;
		tmpNextTag.init();
		if (this.in != null) {
			// read COMMENT, CDATA or HDATA
			if (this.curTag.getXmlTagType() != null) {
				switch (this.curTag.getXmlTagType()) {
				case COMMENT:
					final Reader commentStream = new DetectCommentEndInputStream(this.in);
					if (this.osComment == null) {
						StreamUtil.pipeReadable(commentStream, StreamUtil.VOID_WRITER, false);
					} else {
						StreamUtil.pipeReadable(commentStream, this.osComment, true);
						this.osComment = null;
					}
					commentStream.close();
					break;

				case CDATA:
					final Reader cDataStream = new DetectDataEndInputStream(this.in);
					if (this.osCData == null) {
						StreamUtil.pipeReadable(cDataStream, StreamUtil.VOID_WRITER, false);
					} else {
						StreamUtil.pipeReadable(cDataStream, this.osCData, true);
						this.osCData = null;
					}
					cDataStream.close();
					break;

				case HDATA:
					final Reader hDataStream = new DetectDataEndInputStream(this.in);
					if (this.osHData == null) {
						StreamUtil.pipeReadable(hDataStream, StreamUtil.VOID_WRITER, false);
					} else {
						StreamUtil.pipeFromHex(hDataStream, this.osHData, true);
						this.osHData = null;
					}
					hDataStream.close();
					break;

				default:
					break;
				}
			}
			char c;
			// read data
			this.curData.setLength(0);
			while ((c = (char) this.in.read()) != EOF_CHAR && c != XmlTools.KEY_LESS_THAN) {
				this.curData.append(c);
			}
			if (c != EOF_CHAR) {
				// read tag
				tmpNextTag.append(c);
				c = (char) this.in.read();
				if (c == EOF_CHAR) {
					throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
				} else if (c == XmlTools.COMMENT_CHARACTER_INIT_FIRST) {
					// its COMMENT, CDATA or HDATA
					tmpNextTag.append(c);
					c = (char) this.in.read();
					if (c == EOF_CHAR) {
						throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
					} else if (c == XmlTools.COMMENT_CHARACTER_INIT_SECOND_THIRD) {
						// its COMMENT
						tmpNextTag.append(c);
						c = (char) this.in.read();
						if (c == EOF_CHAR) {
							throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
						} else {
							tmpNextTag.append(c);
						}
					} else if (c == XmlTools.DATA_INIT_SECOND) {
						// its CDATA or HDATA
						tmpNextTag.append(c);
						for (int i = 3; i < XmlTools.CDATA_WRAP[0].length()
								&& (c = (char) this.in.read()) != EOF_CHAR; i++) {
							tmpNextTag.append(c);
						}
						if (c == EOF_CHAR) {
							throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
						}
					} else {
						throw new IOException(new XmlInvalidLtException("Invalid xml reading tag: #0", tmpNextTag));
					}
				} else {
					do {
						tmpNextTag.append(c);
					} while ((c = (char) this.in.read()) != EOF_CHAR && c != XmlTools.KEY_GREATER_THAN);
					if (c == XmlTools.KEY_GREATER_THAN) {
						tmpNextTag.append(c);
					}
				}
				// load
				if (c != EOF_CHAR) {
					result = tmpNextTag.load();
				} else {
					throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
				}
			} else {
				close();
			}
		}
		this.nextTag = tmpNextTag;
		if (result) {
			// validate open close
			try {
				if (XmlTagType.OPEN.equals(this.nextTag.getXmlTagType())) {
					this.tagTrack.push(this.nextTag.getName());
				} else if (XmlTagType.CLOSE.equals(this.nextTag.getXmlTagType())) {
					if (!this.tagTrack.pop().equals(this.nextTag.getName())) {
						throw new IOException(
								new XmlInvalidLtException("Unexpected close element name: #0", this.nextTag.getName()));
					}
				}
			} catch (final XmlInvalidLtException e) {
				throw new IOException(e);
			}
		}
		return result;
	}

	/**
	 *
	 * @return
	 */
	public synchronized boolean isEnded() {
		return this.in == null;
	}

	/**
	 *
	 * @return
	 * @throws XmlInvalidLtException if tag is invalid
	 */
	public synchronized XmlTagType getTagType() throws XmlInvalidLtException {
		return this.curTag.getXmlTagType();
	}

	/**
	 *
	 * @return
	 * @throws XmlInvalidLtException if tag is invalid
	 */
	public synchronized XmlTagType peekNextTagType() throws XmlInvalidLtException {
		return this.nextTag.getXmlTagType();
	}

	/**
	 * Get the element name.
	 *
	 * @return element name
	 * @throws XmlInvalidLtException if tag is invalid
	 */
	public synchronized CharSequence getElementName() throws XmlInvalidLtException {
		return this.curTag.getName();
	}

	/**
	 * Get the element value.
	 *
	 * @param dest to write the value
	 */
	public synchronized void getElementValue(final StringBuilder dest) {
		XmlTools.decod(dest, this.curData);
	}

	/**
	 * Obtains the attribute value by tag.
	 *
	 * @param dest               to write the value
	 * @param attributeNameToGet the name of attribute
	 * @return false if does not exists
	 * @throws XmlInvalidLtException If is an invalid tag
	 */
	public synchronized boolean getElementAttributeValue(final StringBuilder dest, final String attributeNameToGet)
			throws XmlInvalidLtException {
		return this.curTag.getElementAttributeValue(dest, attributeNameToGet);
	}

	/**
	 * Prepares consumer to write Comment content to the object in argument.
	 *
	 * @param osComment destination
	 * @throws XmlInvalidLtException if tag is invalid
	 */
	public synchronized void setWriteNextCommentTo(final Writer osComment) throws XmlInvalidLtException {
		if (!XmlTagType.COMMENT.equals(peekNextTagType())) {
			throw new IllegalStateLtRtException("Expected Comment tag, but it is: #0", peekNextTagType());
		}
		this.osComment = osComment;
	}

	/**
	 * Prepares consumer to write CDATA content to the object in argument.
	 *
	 * @param osCData destination
	 * @throws XmlInvalidLtException if tag is invalid
	 */
	public synchronized void setWriteNextCDataTo(final Writer osCData) throws XmlInvalidLtException {
		if (!XmlTagType.CDATA.equals(peekNextTagType())) {
			throw new IllegalStateLtRtException("Expected CDATA tag, but it is: #0", peekNextTagType());
		}
		this.osCData = osCData;
	}

	/**
	 * Prepares consumer to write HDATA content to the object in argument.
	 *
	 * @param osHData destination
	 * @throws XmlInvalidLtException if tag is invalid
	 */
	public synchronized void setWriteNextHDataTo(final OutputStream osHData) throws XmlInvalidLtException {
		if (!XmlTagType.HDATA.equals(peekNextTagType())) {
			throw new IllegalStateLtRtException("Expected HDATA tag, but it is: #0", peekNextTagType());
		}
		this.osHData = osHData;
	}

	/**
	 * Closes the stream XML.
	 *
	 * @throws IOException If an I/O error occurs
	 */
	@Override
	public synchronized void close() throws IOException {
		if (this.in != null) {
			this.in.close();
			this.in = null;
		}
	}

	/**
	 * Exposes the current tag.
	 */
	@Override
	public synchronized String toString() {
		return super.toString() + " - " + this.curTag.toString();
	}

	private class DetectDataEndInputStream extends Reader {

		private final BufferedReader reader;
		private boolean endedData = false;

		private DetectDataEndInputStream(final BufferedReader reader) {
			this.reader = reader;
		}

		@Override
		public int read(final char[] cbuf, final int off, final int len) throws IOException {
			if (cbuf == null) {
				throw new NullPointerException();
			} else if (off < 0 || len < 0 || len > cbuf.length - off) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return 0;
			} else if (len < 4) {
				throw new UnsupportedOperationException(
						new ImplementationLtRtException("Not implemented to receive a 0 < len < 4"));
			}
			if (this.endedData) {
				return -1;
			}
			final int readLimit = off + (len - 3);
			int count = off;
			char c = '#';
			while (count < readLimit && (c = (char) this.reader.read()) != EOF_CHAR && c != XmlTools.DATA_END_FIRST) {
				cbuf[count++] = c;
			}
			if (count < readLimit) {
				if (c == XmlTools.DATA_END_FIRST) {
					if ((c = (char) this.reader.read()) != EOF_CHAR && c != XmlTools.CDATA_WRAP[1].charAt(1)) {
						cbuf[count++] = XmlTools.DATA_END_FIRST;
						cbuf[count++] = c;
					} else {
						if ((c = (char) this.reader.read()) != EOF_CHAR && c != XmlTools.CDATA_WRAP[1].charAt(2)) {
							cbuf[count++] = XmlTools.DATA_END_FIRST;
							cbuf[count++] = XmlTools.CDATA_WRAP[1].charAt(1);
							cbuf[count++] = c;
						} else {
							if (c == XmlTools.CDATA_WRAP[1].charAt(2)) {
								this.endedData = true;
							} else {
								throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
							}
						}
					}
				} else {
					throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
				}
			}
			return count - off;
		}

		@Override
		public void close() throws IOException {
			this.endedData = true;
		}

	}

	private class DetectCommentEndInputStream extends Reader {

		private final BufferedReader reader;
		private boolean endedData = false;
		private Reader dataStream = null;
		private boolean consumedFirstEnd = false;

		private DetectCommentEndInputStream(final BufferedReader reader) {
			this.reader = reader;
		}

		@Override
		public int read(final char[] cbuf, final int off, final int len) throws IOException {
			if (this.dataStream != null) {
				int count = this.dataStream.read(cbuf, off, len);
				if (count < 0) {
					this.dataStream = null;
					count = XmlTools.CDATA_WRAP[1].length();
					for (int i = 0; i < count; i++) {
						cbuf[off + i] = XmlTools.CDATA_WRAP[1].charAt(i);
					}
				}
				return count;
			}
			if (cbuf == null) {
				throw new NullPointerException();
			} else if (off < 0 || len < 0 || len > cbuf.length - off) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return 0;
			} else if (len < 10) {
				throw new UnsupportedOperationException(
						new ImplementationLtRtException("Not implemented to receive a 0 < len < 10"));
			}
			if (this.endedData) {
				return -1;
			}
			final int readLimit = off + (len - 9);
			int count = off;
			char c = '#';
			if (this.consumedFirstEnd) {
				c = XmlTools.COMMENT_CHARACTER_END_FIRST_SECOND;
				this.consumedFirstEnd = false;
			} else {
				while (count < readLimit && (c = (char) this.reader.read()) != EOF_CHAR
						&& c != XmlTools.COMMENT_CHARACTER_END_FIRST_SECOND && c != XmlTools.DATA_INIT) {
					cbuf[count++] = c;
				}
			}
			if (count < readLimit) {
				if (c == XmlTools.COMMENT_CHARACTER_END_FIRST_SECOND) {
					// check end of comment
					if ((c = (char) this.reader.read()) != EOF_CHAR
							&& c != XmlTools.COMMENT_CHARACTER_END_FIRST_SECOND) {
						cbuf[count++] = XmlTools.COMMENT_CHARACTER_END_FIRST_SECOND;
						cbuf[count++] = c;
					} else {
						if ((c = (char) this.reader.read()) != EOF_CHAR && c != XmlTools.KEY_GREATER_THAN) {
							cbuf[count++] = XmlTools.COMMENT_CHARACTER_END_FIRST_SECOND;
							cbuf[count++] = XmlTools.COMMENT_CHARACTER_END_FIRST_SECOND;
							cbuf[count++] = c;
						} else {
							if (c == XmlTools.KEY_GREATER_THAN) {
								this.endedData = true;
							} else {
								throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
							}
						}
					}
				} else if (c == XmlTools.DATA_INIT) {
					// check initiation of CDATA or HDATA
					cbuf[count++] = c;
					if ((c = (char) this.reader.read()) != EOF_CHAR && c != XmlTools.DATA_INIT_FIRST) {
						if (c == XmlTools.COMMENT_CHARACTER_END_FIRST_SECOND) {
							this.consumedFirstEnd = true;
						} else {
							cbuf[count++] = c;
						}
					} else {
						if (c == XmlTools.DATA_INIT_FIRST) {
							cbuf[count++] = c;
							if ((c = (char) this.reader.read()) != EOF_CHAR && c != XmlTools.DATA_INIT_SECOND) {
								if (c == XmlTools.COMMENT_CHARACTER_END_FIRST_SECOND) {
									this.consumedFirstEnd = true;
								} else {
									cbuf[count++] = c;
								}
							} else {
								if (c == XmlTools.DATA_INIT_SECOND) {
									cbuf[count++] = c;
									if ((c = (char) this.reader.read()) == EOF_CHAR) {
										throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
									} else {
										if (c == XmlTools.COMMENT_CHARACTER_END_FIRST_SECOND) {
											this.consumedFirstEnd = true;
										} else {
											cbuf[count++] = c;
											boolean isDataVariant = false;
											for (int i = 0; i < XmlTools.DATA_INIT_VARIANT.length; i++) {
												if (c == XmlTools.DATA_INIT_VARIANT[i]) {
													isDataVariant = true;
												}
											}
											if (isDataVariant) {
												int dataTagPos = 4;
												while (dataTagPos < XmlTools.CDATA_WRAP[0].length()
														&& (c = (char) this.reader.read()) == XmlTools.CDATA_WRAP[0]
																.charAt(dataTagPos)) {
													cbuf[count++] = c;
													dataTagPos++;
												}
												if (dataTagPos == XmlTools.CDATA_WRAP[0].length()) {
													// data tag inside comment tag
													this.dataStream = new DetectDataEndInputStream(this.reader);
												} else {
													if (c == EOF_CHAR) {
														throw new IOException(
																new XmlInvalidLtException("Unexpected end of stream"));
													} else if (c == XmlTools.COMMENT_CHARACTER_END_FIRST_SECOND) {
														this.consumedFirstEnd = true;
													} else {
														cbuf[count++] = c;
													}
												}
											}
										}
									}
								} else {
									throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
								}
							}
						} else {
							throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
						}
					}
				} else {
					throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
				}
			}
			return count - off;
		}

		@Override
		public void close() throws IOException {
			this.endedData = true;
		}

	}

}

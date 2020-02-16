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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
public final class XmlConsumer {

	private static final char EOF_CHAR = (char) -1;

	private BufferedReader in;
	private final Stack<CharSequence> tagTrack;
	private XmlTag curTag;
	private final StringBuilder curData;
	private XmlTag nextTag;
	private BufferedWriter osCData;
	private StringBuilder sbCData;
	private BufferedOutputStream osHData;

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
		this.osCData = null;
		this.sbCData = null;
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
			char c = '#';
			// read CDATA and HDATA
			if (this.curTag.getXmlTagType() != null) {
				switch (this.curTag.getXmlTagType()) {
				case CDATA:
					if (this.sbCData != null) {
						final int initPos = this.sbCData.length();
						boolean endedCData = false;
						while (!endedCData) {
							while ((c = (char) this.in.read()) != EOF_CHAR && c != XmlTools.DATA_END_FIRST) {
								this.sbCData.append(c);
							}
							if (c == XmlTools.DATA_END_FIRST) {
								if ((c = (char) this.in.read()) != EOF_CHAR && c != XmlTools.CDATA_WRAP[1].charAt(1)) {
									this.sbCData.append(XmlTools.DATA_END_FIRST);
									this.sbCData.append(c);
								} else {
									if ((c = (char) this.in.read()) != EOF_CHAR
											&& c != XmlTools.CDATA_WRAP[1].charAt(2)) {
										this.sbCData.append(XmlTools.DATA_END_FIRST);
										this.sbCData.append(XmlTools.CDATA_WRAP[1].charAt(1));
										this.sbCData.append(c);
									} else {
										endedCData = true;
									}
								}
							}
							if (c == EOF_CHAR) {
								throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
							}
						}
						if (this.osCData != null) {
							this.osCData.append(this.sbCData, initPos, this.sbCData.length() - initPos);
						}
						this.sbCData = null;
						this.osCData = null;
					} else if (this.osCData != null) {
						final Reader ris = new DetectDataEndInputStream(this.in);
						StreamUtil.pipeReadable(ris, this.osCData, true);
						ris.close();
						this.osCData = null;
					} else {
						boolean endedCData = false;
						while (!endedCData) {
							while ((c = (char) this.in.read()) != EOF_CHAR && c != XmlTools.DATA_END_FIRST) {
							}
							if (c == XmlTools.DATA_END_FIRST) {
								if ((c = (char) this.in.read()) != EOF_CHAR && c != XmlTools.CDATA_WRAP[1].charAt(1)) {
								} else {
									if ((c = (char) this.in.read()) != EOF_CHAR
											&& c != XmlTools.CDATA_WRAP[1].charAt(2)) {
									} else {
										endedCData = true;
									}
								}
							}
							if (c == EOF_CHAR) {
								throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
							}
						}
					}
					break;

				case HDATA:
					if (this.osHData != null) {
						final Reader ris = new DetectDataEndInputStream(this.in);
						StreamUtil.pipeFromHex(ris, this.osHData, true);
						ris.close();
						this.osHData = null;
					} else {
						boolean endedCData = false;
						while (!endedCData) {
							while ((c = (char) this.in.read()) != EOF_CHAR && c != XmlTools.DATA_END_FIRST) {
							}
							if (c == XmlTools.DATA_END_FIRST) {
								if ((c = (char) this.in.read()) != EOF_CHAR && c != XmlTools.HDATA_WRAP[1].charAt(1)) {
								} else {
									if ((c = (char) this.in.read()) != EOF_CHAR
											&& c != XmlTools.HDATA_WRAP[1].charAt(2)) {
									} else {
										endedCData = true;
									}
								}
							}
							if (c == EOF_CHAR) {
								throw new IOException(new XmlInvalidLtException("Unexpected end of stream"));
							}
						}
					}
					break;

				default:
					break;
				}
			}
			this.curData.setLength(0);
			// read data
			if (c != EOF_CHAR) {
				while ((c = (char) this.in.read()) != EOF_CHAR && c != XmlTools.KEY_LESS_THAN) {
					this.curData.append(c);
				}
			}
			if (c != EOF_CHAR) {
				// read tag
				do {
					tmpNextTag.append(c);
				} while (!(c == XmlTools.DATA_INIT_LAST && (XmlTagType.CDATA.equals(tmpNextTag.getXmlTagType())
						|| XmlTagType.HDATA.equals(tmpNextTag.getXmlTagType())))
						&& (c = (char) this.in.read()) != EOF_CHAR && c != XmlTools.KEY_GREATER_THAN);
				if (c == XmlTools.KEY_GREATER_THAN) {
					tmpNextTag.append(c);
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
	 * Get the content of the comment.
	 *
	 * @param dest to write the comment
	 * @throws XmlInvalidLtException if tag is invalid
	 */
	public synchronized void getComment(final StringBuilder dest) throws XmlInvalidLtException {
		this.curTag.getComment(dest);
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
	 * Prepares consumer to write CDATA content to the object in argument.
	 *
	 * @param osCData destination
	 * @throws XmlInvalidLtException if tag is invalid
	 */
	public synchronized void setWriteNextCDataTo(final BufferedWriter osCData) throws XmlInvalidLtException {
		if (XmlTagType.CDATA.equals(peekNextTagType())) {
			throw new IllegalStateLtRtException("Expected CDATA tag, but it is: #0", peekNextTagType());
		}
		this.osCData = osCData;
	}

	/**
	 * Prepares consumer to write CDATA content to the object in argument.
	 *
	 * @param sbCData destination
	 * @throws XmlInvalidLtException if tag is invalid
	 */
	public synchronized void setWriteNextCDataTo(final StringBuilder sbCData) throws XmlInvalidLtException {
		if (XmlTagType.CDATA.equals(peekNextTagType())) {
			throw new IllegalStateLtRtException("Expected CDATA tag, but it is: #0", peekNextTagType());
		}
		this.sbCData = sbCData;
	}

	/**
	 * Prepares consumer to write HDATA content to the object in argument.
	 *
	 * @param osHData destination
	 * @throws XmlInvalidLtException if tag is invalid
	 */
	public synchronized void setWriteNextHDataTo(final BufferedOutputStream osHData) throws XmlInvalidLtException {
		if (XmlTagType.HDATA.equals(peekNextTagType())) {
			throw new IllegalStateLtRtException("Expected HDATA tag, but it is: #0", peekNextTagType());
		}
		this.osHData = osHData;
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
	 * Exposes the current tag.
	 */
	@Override
	public synchronized String toString() {
		return super.toString() + " - " + this.curTag.toString();
	}

	private class DetectDataEndInputStream extends Reader {

		private final BufferedReader ddeis;
		private boolean endedCData = false;

		private DetectDataEndInputStream(final BufferedReader ddeis) {
			this.ddeis = ddeis;
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
			if (this.endedCData) {
				return -1;
			}
			final int readLimit = off + (len - 3);
			int count = off;
			char c = '#';
			while (count < readLimit && (c = (char) this.ddeis.read()) != EOF_CHAR && c != XmlTools.DATA_END_FIRST) {
				cbuf[count++] = c;
			}
			if (count < readLimit) {
				if (c == XmlTools.DATA_END_FIRST) {
					if ((c = (char) this.ddeis.read()) != EOF_CHAR && c != XmlTools.CDATA_WRAP[1].charAt(1)) {
						cbuf[count++] = XmlTools.DATA_END_FIRST;
						cbuf[count++] = c;
					} else {
						if ((c = (char) this.ddeis.read()) != EOF_CHAR && c != XmlTools.CDATA_WRAP[1].charAt(2)) {
							cbuf[count++] = XmlTools.DATA_END_FIRST;
							cbuf[count++] = XmlTools.CDATA_WRAP[1].charAt(1);
							cbuf[count++] = c;
						} else {
							if (c == XmlTools.CDATA_WRAP[1].charAt(2)) {
								this.endedCData = true;
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
		}

	}

}

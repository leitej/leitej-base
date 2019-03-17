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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Stack;

import leitej.exception.XmlInvalidLtException;

/**
 * XML Producer
 *
 * @author Julio Leite
 * @see leitej.xml.XmlConsumer
 */
public final class XmlProducer {

	private final XmlProducerTools xmlProdTools = new XmlProducerTools();
	private final Stack<String> tagTrack = new Stack<>();
	private BufferedWriter out = null;
	private final boolean hReadable;
	private final StringBuilder encoding = new StringBuilder();
	private final StringBuilder sbTmp = new StringBuilder();
	private boolean endRootElement = false;

	/**
	 * Creates a new instance of XMLProducer. With a minified XML configured.
	 *
	 * @param osr the underlying output stream to be written
	 */
	public XmlProducer(final OutputStreamWriter osr) {
		this(osr, true);
	}

	/**
	 * Creates a new instance of XMLProducer.
	 *
	 * @param osr      the underlying output stream to be written
	 * @param minified when false produces a human readable XML, other wise outputs
	 *                 a clean strait line
	 */
	public XmlProducer(final OutputStreamWriter osr, final boolean minified) {
		this.encoding.append(osr.getEncoding());
		this.out = new BufferedWriter(osr);
		this.hReadable = !minified;
	}

	/**
	 * Closes the stream XML.
	 *
	 * @throws IOException If an I/O error occurs
	 */
	public synchronized void close() throws IOException {
		if (this.out != null) {
			this.out.flush();
			this.out.close();
			this.out = null;
		}
	}

	/**
	 * Flushes the stream.
	 *
	 * @throws IOException If an I/O error occurs
	 */
	public synchronized void flush() throws IOException {
		if (this.out != null) {
			this.out.flush();
		}
	}

	/**
	 * Writes to stream.
	 *
	 * @param txt to be written
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is writing a corrupted XML
	 */
	private void write(final CharSequence txt) throws IOException, XmlInvalidLtException {
		if (this.endRootElement) {
			throw new XmlInvalidLtException("lt.XmlInvalidElementAfterRootEnd");
		}
		this.out.append(txt);
	}

	private void println() throws IOException {
		if (this.hReadable) {
			this.out.write(XmlTools.LINE_SEPARATOR);
		}
	}

	private Integer ident() {
		return ((this.hReadable) ? this.tagTrack.size() : 0);
	}

	/**
	 * Write a default meta data.
	 *
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is writing a corrupted XML
	 */
	public synchronized void printMetaData() throws IOException, XmlInvalidLtException {
		this.sbTmp.setLength(0);
		this.xmlProdTools.genMetaData(this.sbTmp, this.encoding);
		write(this.sbTmp);
		println();
	}

	/**
	 * Writes the opening tag.
	 *
	 * @param elementName    tag name
	 * @param generatedAttrs the attributes already formated
	 * @throws XmlInvalidLtException If is writing a corrupted XML
	 * @throws IOException           If an I/O error occurs
	 */
	public synchronized void printTagOpen(final CharSequence elementName, final CharSequence generatedAttrs)
			throws XmlInvalidLtException, IOException {
		printTagOpen(elementName, generatedAttrs, true);
	}

	private void printTagOpen(final CharSequence elementName, final CharSequence generatedAttrs, final boolean newLine)
			throws XmlInvalidLtException, IOException {
		this.sbTmp.setLength(0);
		this.xmlProdTools.genElementTagOpen(this.sbTmp, ident(), elementName, generatedAttrs);
		write(this.sbTmp);
		this.tagTrack.push(elementName.toString());
		if (newLine) {
			println();
		}
	}

	/**
	 * Writes the closing tag.
	 *
	 * @param elementName tag name
	 * @throws XmlInvalidLtException If is writing a corrupted XML
	 * @throws IOException           If an I/O error occurs
	 */
	public synchronized void printTagClose(final CharSequence elementName) throws XmlInvalidLtException, IOException {
		printTagClose(elementName, true);
		println();
	}

	private void printTagClose(final CharSequence elementName, final boolean ident)
			throws XmlInvalidLtException, IOException {
		if (!this.tagTrack.pop().contentEquals(elementName)) {
			throw new XmlInvalidLtException("lt.XmlInvalidElementConstruct", elementName);
		}
		this.sbTmp.setLength(0);
		this.xmlProdTools.genElementTagClose(this.sbTmp, ((ident) ? ident() : 0), elementName);
		write(this.sbTmp);
		if (this.tagTrack.size() == 0) {
			this.endRootElement = true;
		}
	}

	/**
	 * Writes a element without value (null value).
	 *
	 * @param elementName    element name
	 * @param generatedAttrs the attributes already formated
	 * @throws XmlInvalidLtException If is writing a corrupted XML
	 * @throws IOException           If an I/O error occurs
	 */
	public synchronized void printTagOpenClose(final CharSequence elementName, final CharSequence generatedAttrs)
			throws XmlInvalidLtException, IOException {
		this.sbTmp.setLength(0);
		this.xmlProdTools.genElementTagOpenClose(this.sbTmp, ident(), elementName, generatedAttrs);
		write(this.sbTmp);
		println();
	}

	/**
	 * Writes a element. The value is encoded with the rules of XML.
	 *
	 * @param elementName    element name
	 * @param elementValue   the element value
	 * @param generatedAttrs the attributes already formated
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is writing a corrupted XML
	 */
	public synchronized void printElement(final CharSequence elementName, final CharSequence elementValue,
			final CharSequence generatedAttrs) throws IOException, XmlInvalidLtException {
		if (elementValue != null) {
			printTagOpen(elementName, generatedAttrs, false);
			this.sbTmp.setLength(0);
			this.xmlProdTools.encod(this.sbTmp, elementValue);
			write(this.sbTmp);
			printTagClose(elementName, false);
		} else {
			printTagOpenClose(elementName, generatedAttrs);
		}
		println();
	}

	/**
	 * Writes a comment. The comment is encoded with the rules of XML.
	 *
	 * @param comment the comment
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is writing a corrupted XML
	 */
	public synchronized void printComment(final CharSequence comment) throws IOException, XmlInvalidLtException {
		if (comment != null) {
			this.sbTmp.setLength(0);
			this.xmlProdTools.genCommentTag(this.sbTmp, ident(), comment);
			write(this.sbTmp);
			println();
		}
	}

	/**
	 * Generates an attribute.
	 *
	 * @param dest  to write the attribute
	 * @param name  attribute name
	 * @param value attribute value
	 * @return attribute well formatted to be inserted on a open tag
	 */
	public synchronized void genAttribute(final StringBuilder dest, final CharSequence name, final CharSequence value) {
		this.xmlProdTools.genAttribute(dest, name, value);
	}

}

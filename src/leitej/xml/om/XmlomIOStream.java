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

package leitej.xml.om;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import leitej.Constant;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.XmlInvalidLtException;
import leitej.util.stream.FileUtil;

/**
 * An useful class to help in XML Object Modelling stream.
 *
 * @author Julio Leite
 * @see leitej.xml.om.XmlomOutputStream
 * @see leitej.xml.om.XmlomInputStream
 */
public final class XmlomIOStream {

	/**
	 * Creates a new instance of XMLOMUtil.
	 */
	private XmlomIOStream() {
	}

	public static <I extends XmlObjectModelling> I newXmlObjectModelling(final Class<I> interfaceClass) {
		return XmlomOutputStream.newXmlObjectModelling(interfaceClass);
	}

	public static <I extends XmlObjectModelling> void registry(final Class<I> interfaceClass)
			throws IllegalArgumentLtRtException {
		XmlomInputStream.registry(interfaceClass);
	}

	/**
	 * Saves the object to a file with the
	 * <code>Constant.UTF8_CHARSET_NAME</code>.<br/>
	 * This method will try to create the file if not exists.
	 *
	 * @param fileName a pathname string
	 * @param obj      object to be saved
	 * @throws FileNotFoundException If the file exists but is a directory rather
	 *                               than a regular file, does not exist but cannot
	 *                               be created, or cannot be opened for any other
	 *                               reason.
	 * @throws NullPointerException  If the <code>filename</code> argument is
	 *                               <code>null</code>
	 * @throws IOException           If an I/O error occurred
	 * @throws SecurityException     If a security manager exists and its
	 *                               <code>{@link
	 *          java.lang.SecurityManager#checkRead(java.lang.String) checkRead}</code>
	 *                               method does not permit verification of the
	 *                               existence of the named directory and all
	 *                               necessary parent directories; or if the
	 *                               <code>{@link 
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                               method does not permit the named directory and
	 *                               all necessary parent directories to be created;
	 *                               or if a security manager exists and its
	 *                               <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                               method denies write access to the file
	 */
	public static <I extends XmlObjectModelling> void sendToFileUTF8(final String fileName, final I obj)
			throws NullPointerException, FileNotFoundException, SecurityException, IOException {
		sendToFile(fileName, Constant.UTF8_CHARSET_NAME, false, obj);
	}

	/**
	 * Saves the objects to a file with the
	 * <code>Constant.UTF8_CHARSET_NAME</code>.<br/>
	 * This method will try to create the file if not exists.
	 *
	 * @param fileName a pathname string
	 * @param objs     objects to be saved
	 * @throws FileNotFoundException If the file exists but is a directory rather
	 *                               than a regular file, does not exist but cannot
	 *                               be created, or cannot be opened for any other
	 *                               reason.
	 * @throws NullPointerException  If the <code>filename</code> argument is
	 *                               <code>null</code>
	 * @throws IOException           If an I/O error occurred
	 * @throws SecurityException     If a security manager exists and its
	 *                               <code>{@link
	 *          java.lang.SecurityManager#checkRead(java.lang.String) checkRead}</code>
	 *                               method does not permit verification of the
	 *                               existence of the named directory and all
	 *                               necessary parent directories; or if the
	 *                               <code>{@link 
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                               method does not permit the named directory and
	 *                               all necessary parent directories to be created;
	 *                               or if a security manager exists and its
	 *                               <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                               method denies write access to the file
	 */
	public static <I extends XmlObjectModelling> void sendToFileUTF8(final String fileName, final I[] objs)
			throws NullPointerException, FileNotFoundException, SecurityException, IOException {
		sendToFile(fileName, Constant.UTF8_CHARSET_NAME, false, objs);
	}

	/**
	 * Saves the objects to a file.<br/>
	 * This method will try to create the file if not exists.
	 *
	 * @param fileName    a pathname string
	 * @param charsetName name of a supported {@link java.nio.charset.Charset
	 *                    </code>charset<code>}
	 * &#64;param minified when false produces a human readable XML, other wise outputs a clean strait line
	 * &#64;param objs objects to be saved
	 * &#64;throws FileNotFoundException If the file exists but is a directory
	 * 			rather than a regular file, does not exist but cannot
	 * 			be created, or cannot be opened for any other reason.
	 * @throws NullPointerException If the <code>filename</code> argument is
	 *                                      <code>null</code>
	 * @throws IOException                  If an I/O error occurred
	 * @throws SecurityException            If a security manager exists and its
	 *                                      <code>{@link
	 *          java.lang.SecurityManager#checkRead(java.lang.String) checkRead}</code>
	 *                                      method does not permit verification of
	 *                                      the existence of the named directory and
	 *                                      all necessary parent directories; or if
	 *                                      the <code>{@link 
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                                      method does not permit the named
	 *                                      directory and all necessary parent
	 *                                      directories to be created; or if a
	 *                                      security manager exists and its
	 *                                      <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                                      method denies write access to the file
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 */
	public static <I extends XmlObjectModelling> void sendToFile(final String fileName, final String charsetName,
			final boolean minified, final I[] objs) throws UnsupportedEncodingException, NullPointerException,
			FileNotFoundException, SecurityException, IOException {
		FileUtil.createFile(fileName);
		final OutputStream os = new FileOutputStream(fileName, false);
		sendToStream(true, os, charsetName, minified, objs);
	}

	/**
	 * Saves the object to a file.<br/>
	 * This method will try to create the file if not exists.
	 *
	 * @param fileName    a pathname string
	 * @param charsetName name of a supported {@link java.nio.charset.Charset
	 *                    </code>charset<code>}
	 * &#64;param minified when false produces a human readable XML, other wise outputs a clean strait line
	 * &#64;param obj object to be saved
	 * &#64;throws FileNotFoundException If the file exists but is a directory
	 * 			rather than a regular file, does not exist but cannot
	 * 			be created, or cannot be opened for any other reason.
	 * @throws NullPointerException If the <code>filename</code> argument is
	 *                                      <code>null</code>
	 * @throws IOException                  If an I/O error occurred
	 * @throws SecurityException            If a security manager exists and its
	 *                                      <code>{@link
	 *          java.lang.SecurityManager#checkRead(java.lang.String) checkRead}</code>
	 *                                      method does not permit verification of
	 *                                      the existence of the named directory and
	 *                                      all necessary parent directories; or if
	 *                                      the <code>{@link 
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                                      method does not permit the named
	 *                                      directory and all necessary parent
	 *                                      directories to be created; or if a
	 *                                      security manager exists and its
	 *                                      <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                                      method denies write access to the file
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 */
	public static <I extends XmlObjectModelling> void sendToFile(final String fileName, final String charsetName,
			final boolean minified, final I obj) throws UnsupportedEncodingException, NullPointerException,
			FileNotFoundException, SecurityException, IOException {
		FileUtil.createFile(fileName);
		final OutputStream os = new FileOutputStream(fileName, false);
		sendToStream(true, os, charsetName, minified, obj);
	}

	/**
	 * Writes the <code>objs</code> to the stream with the
	 * <code>Constant.UTF8_CHARSET_NAME</code>. And don't closes it.
	 *
	 * @param os       an OutputStream
	 * @param minified when false produces a human readable XML, other wise outputs
	 *                 a clean strait line
	 * @param objs     to write to the stream
	 * @throws IOException If an I/O error occurs
	 */
	public static <I extends XmlObjectModelling> void sendToStreamUTF8(final OutputStream os, final boolean minified,
			final I[] objs) throws IOException {
		sendToStream(os, Constant.UTF8_CHARSET_NAME, minified, objs);
	}

	/**
	 * Writes the <code>objs</code> to the stream. And don't closes it.
	 *
	 * @param os          an OutputStream
	 * @param charsetName the name of a supported {@link java.nio.charset.Charset
	 *                    charset}
	 * @param minified    when false produces a human readable XML, other wise
	 *                    outputs a clean strait line
	 * @param objs        to write to the stream
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 * @throws IOException                  If an I/O error occurs
	 */
	public static <I extends XmlObjectModelling> void sendToStream(final OutputStream os, final String charsetName,
			final boolean minified, final I[] objs) throws UnsupportedEncodingException, IOException {
		sendToStream(false, os, charsetName, minified, objs);
	}

	/**
	 * Writes the <code>obj</code> to the stream.
	 *
	 * @param withClose   defines if is to close the stream at the end of write
	 * @param os          an OutputStream
	 * @param charsetName the name of a supported {@link java.nio.charset.Charset
	 *                    charset}
	 * @param minified    when false produces a human readable XML, other wise
	 *                    outputs a clean strait line
	 * @param obj         to write to the stream
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 * @throws IOException                  If an I/O error occurs
	 */
	private static <I extends XmlObjectModelling> void sendToStream(final boolean withClose, final OutputStream os,
			final String charsetName, final boolean minified, final I obj)
			throws UnsupportedEncodingException, IOException {
		final XmlomOutputStream out = new XmlomOutputStream(os, charsetName, minified);
		try {
			out.write(obj);
			out.doFinal();
		} finally {
			if (withClose) {
				out.close();
			}
		}
	}

	/**
	 * Writes the <code>objs</code> to the stream.
	 *
	 * @param withClose   defines if is to close the stream at the end of write
	 * @param os          an OutputStream
	 * @param charsetName the name of a supported {@link java.nio.charset.Charset
	 *                    charset}
	 * @param minified    when false produces a human readable XML, other wise
	 *                    outputs a clean strait line
	 * @param objs        to write to the stream
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 * @throws IOException                  If an I/O error occurs
	 */
	private static <I extends XmlObjectModelling> void sendToStream(final boolean withClose, final OutputStream os,
			final String charsetName, final boolean minified, final I[] objs)
			throws UnsupportedEncodingException, IOException {
		final XmlomOutputStream out = new XmlomOutputStream(os, charsetName, minified);
		try {
			out.write(objs);
			out.doFinal();
		} finally {
			if (withClose) {
				out.close();
			}
		}
	}

	/**
	 * Reads all objects from the file in <code>Constant.UTF8_CHARSET_NAME</code>.
	 *
	 * @param interfaceClass type of object to be ridden
	 * @param fileName       a pathname string
	 * @return all the ridden objects
	 * @throws FileNotFoundException If the file does not exist, is a directory
	 *                               rather than a regular file, or for some other
	 *                               reason cannot be opened for reading
	 * @throws SecurityException     If a security manager exists and its
	 *                               <code>checkRead</code> method denies read
	 *                               access to the file.
	 * @throws NullPointerException  If the pathname argument is null; or if
	 *                               trustClass has a class null
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 * @throws IOException           If an I/O error occurs
	 */
	public static <I extends XmlObjectModelling> List<I> getObjectsFromFileUTF8(final Class<I> interfaceClass,
			final String fileName)
			throws FileNotFoundException, SecurityException, NullPointerException, XmlInvalidLtException, IOException {
		return getObjectsFromFile(interfaceClass, fileName, Constant.UTF8_CHARSET_NAME);
	}

	/**
	 * Reads all objects from the file.
	 *
	 * @param interfaceClass type of object to be ridden
	 * @param fileName       a pathname string
	 * @param charsetName    the name of a supported {@link java.nio.charset.Charset
	 *                       charset}
	 * @return all the ridden objects
	 * @throws FileNotFoundException        If the file does not exist, is a
	 *                                      directory rather than a regular file, or
	 *                                      for some other reason cannot be opened
	 *                                      for reading
	 * @throws SecurityException            If a security manager exists and its
	 *                                      <code>checkRead</code> method denies
	 *                                      read access to the file.
	 * @throws UnsupportedEncodingException If the named charset is not supported
	 * @throws NullPointerException         If the pathname argument is null; or if
	 *                                      trustClass has a class null
	 * @throws XmlInvalidLtException        If is reading a corrupted XML
	 * @throws IOException                  If an I/O error occurs
	 */
	public static <I extends XmlObjectModelling> List<I> getObjectsFromFile(final Class<I> interfaceClass,
			final String fileName, final String charsetName) throws FileNotFoundException, SecurityException,
			UnsupportedEncodingException, NullPointerException, XmlInvalidLtException, IOException {
		return getObjectsFromStream(interfaceClass, true, new FileInputStream(new File(fileName)), charsetName);
	}

	/**
	 * Reads all objects from the stream with the
	 * <code>Constant.UTF8_CHARSET_NAME</code>. And don't closes it.
	 *
	 * @param interfaceClass type of object to be ridden
	 * @param is             an InputStream
	 * @return all the ridden objects
	 * @throws NullPointerException  If trustClass has a class null
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 * @throws IOException           If an I/O error occurs
	 */
	public static <I extends XmlObjectModelling> List<I> getObjectsFromStreamUTF8(final Class<I> interfaceClass,
			final InputStream is) throws NullPointerException, XmlInvalidLtException, IOException {
		return getObjectsFromStream(interfaceClass, is, Constant.UTF8_CHARSET_NAME);
	}

	/**
	 * Reads all objects from the stream. And don't closes it.
	 *
	 * @param interfaceClass type of object to be ridden
	 * @param is             an InputStream
	 * @param charsetName    the name of a supported {@link java.nio.charset.Charset
	 *                       charset}
	 * @return all the ridden objects
	 * @throws UnsupportedEncodingException If the named charset is not supported
	 * @throws NullPointerException         If trustClass has a class null
	 * @throws XmlInvalidLtException        If is reading a corrupted XML
	 * @throws IOException                  If an I/O error occurs
	 */
	public static <I extends XmlObjectModelling> List<I> getObjectsFromStream(final Class<I> interfaceClass,
			final InputStream is, final String charsetName)
			throws UnsupportedEncodingException, NullPointerException, XmlInvalidLtException, IOException {
		return getObjectsFromStream(interfaceClass, false, is, charsetName);
	}

	/**
	 * Reads all objects from the stream.
	 *
	 * @param interfaceClass type of object to be ridden
	 * @param withClose      defines if is to close the stream at the end of write
	 * @param is             an InputStream
	 * @param charsetName    the name of a supported {@link java.nio.charset.Charset
	 *                       charset}
	 * @return all the ridden objects
	 * @throws UnsupportedEncodingException If the named charset is not supported
	 * @throws NullPointerException         If trustClass has a class null
	 * @throws XmlInvalidLtException        If is reading a corrupted XML
	 * @throws IOException                  If an I/O error occurs
	 */
	private static <I extends XmlObjectModelling> List<I> getObjectsFromStream(final Class<I> interfaceClass,
			final boolean withClose, final InputStream is, final String charsetName)
			throws UnsupportedEncodingException, NullPointerException, XmlInvalidLtException, IOException {
		List<I> result = null;
		final XmlomInputStream in = new XmlomInputStream(is, charsetName);
		try {
			result = in.readAll(interfaceClass);
		} finally {
			if (withClose) {
				in.close();
			}
		}
		return result;
	}

}

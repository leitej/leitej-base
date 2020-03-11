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

package leitej.util.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import leitej.Constant;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.XmlInvalidLtException;
import leitej.util.stream.FileUtil;
import leitej.xml.om.XmlObjectModelling;
import leitej.xml.om.XmlomReader;
import leitej.xml.om.XmlomWriter;

/**
 * An useful class to help in XML Object Modelling stream.
 *
 * @author Julio Leite
 * @see leitej.xml.om.XmlomWriter
 * @see leitej.xml.om.XmlomReader
 */
public final class XmlomUtil {

	/**
	 * Creates a new instance of XMLOMUtil.
	 */
	private XmlomUtil() {
	}

	/**
	 * //TODO
	 *
	 * @param interfaceClass
	 * @return
	 */
	public static <I extends XmlObjectModelling> I newXmlObjectModelling(final Class<I> interfaceClass) {
		return XmlomWriter.newXmlObjectModelling(interfaceClass);
	}

	/**
	 * //TODO
	 *
	 * @param interfaceClass
	 * @throws IllegalArgumentLtRtException
	 */
	public static <I extends XmlObjectModelling> void registry(final Class<I> interfaceClass)
			throws IllegalArgumentLtRtException {
		XmlomReader.registry(interfaceClass);
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
	public static <I extends XmlObjectModelling> void sendToFile(final String fileName, final I obj)
			throws NullPointerException, FileNotFoundException, SecurityException, IOException {
		sendToFile(fileName, Charset.forName(Constant.UTF8_CHARSET_NAME), false, obj);
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
	public static <I extends XmlObjectModelling> void sendToFile(final String fileName, final I[] objs)
			throws NullPointerException, FileNotFoundException, SecurityException, IOException {
		sendToFile(fileName, Charset.forName(Constant.UTF8_CHARSET_NAME), false, objs);
	}

	/**
	 * Saves the objects to a file.<br/>
	 * This method will try to create the file if not exists.
	 *
	 * @param fileName a pathname string
	 * @param charset
	 * @param minified when false produces a human readable XML, other wise outputs
	 *                 a clean strait line
	 * @param objs     objects to be saved
	 * @throws FileNotFoundException        If the file exists but is a directory
	 *                                      rather than a regular file, does not
	 *                                      exist but cannot be created, or cannot
	 *                                      be opened for any other reason.
	 * @throws NullPointerException         If the <code>filename</code> argument is
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
	public static <I extends XmlObjectModelling> void sendToFile(final String fileName, final Charset charset,
			final boolean minified, final I[] objs) throws UnsupportedEncodingException, NullPointerException,
			FileNotFoundException, SecurityException, IOException {
		FileUtil.createFile(fileName);
		final OutputStream os = new FileOutputStream(fileName, false);
		sendToStream(true, os, charset, minified, objs);
	}

	/**
	 * Saves the object to a file.<br/>
	 * This method will try to create the file if not exists.
	 *
	 * @param fileName a pathname string
	 * @param charset
	 * @param minified when false produces a human readable XML, other wise outputs
	 *                 a clean strait line
	 * @param obj      object to be saved
	 * @throws FileNotFoundException        If the file exists but is a directory
	 *                                      rather than a regular file, does not
	 *                                      exist but cannot be created, or cannot
	 *                                      be opened for any other reason.
	 * @throws NullPointerException         If the <code>filename</code> argument is
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
	public static <I extends XmlObjectModelling> void sendToFile(final String fileName, final Charset charset,
			final boolean minified, final I obj) throws UnsupportedEncodingException, NullPointerException,
			FileNotFoundException, SecurityException, IOException {
		final OutputStream os = new FileOutputStream(FileUtil.createFile(fileName), false);
		sendToStream(true, os, charset, minified, obj);
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
	public static <I extends XmlObjectModelling> void sendToStream(final OutputStream os, final boolean minified,
			final I[] objs) throws IOException {
		sendToStream(os, Charset.forName(Constant.UTF8_CHARSET_NAME), minified, objs);
	}

	/**
	 * Writes the <code>objs</code> to the stream. And don't closes it.
	 *
	 * @param os       an OutputStream
	 * @param charset
	 * @param minified when false produces a human readable XML, other wise outputs
	 *                 a clean strait line
	 * @param objs     to write to the stream
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 * @throws IOException                  If an I/O error occurs
	 */
	public static <I extends XmlObjectModelling> void sendToStream(final OutputStream os, final Charset charset,
			final boolean minified, final I[] objs) throws UnsupportedEncodingException, IOException {
		sendToStream(false, os, charset, minified, objs);
	}

	/**
	 * Writes the <code>obj</code> to the stream.
	 *
	 * @param withClose defines if is to close the stream at the end of write
	 * @param os        an OutputStream
	 * @param charset
	 * @param minified  when false produces a human readable XML, other wise outputs
	 *                  a clean strait line
	 * @param obj       to write to the stream
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 * @throws IOException                  If an I/O error occurs
	 */
	private static <I extends XmlObjectModelling> void sendToStream(final boolean withClose, final OutputStream os,
			final Charset charset, final boolean minified, final I obj)
			throws UnsupportedEncodingException, IOException {
		final XmlomWriter out = new XmlomWriter(os, charset, minified);
		try {
			out.write(obj);
		} finally {
			if (withClose) {
				out.close();
			}
		}
	}

	/**
	 * Writes the <code>objs</code> to the stream.
	 *
	 * @param withClose defines if is to close the stream at the end of write
	 * @param os        an OutputStream
	 * @param charset
	 * @param minified  when false produces a human readable XML, other wise outputs
	 *                  a clean strait line
	 * @param objs      to write to the stream
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 * @throws IOException                  If an I/O error occurs
	 */
	private static <I extends XmlObjectModelling> void sendToStream(final boolean withClose, final OutputStream os,
			final Charset charset, final boolean minified, final I[] objs)
			throws UnsupportedEncodingException, IOException {
		final XmlomWriter out = new XmlomWriter(os, charset, minified);
		try {
			out.write(objs);
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
	public static <I extends XmlObjectModelling> List<I> getObjectsFromFile(final Class<I> interfaceClass,
			final String fileName)
			throws FileNotFoundException, SecurityException, NullPointerException, XmlInvalidLtException, IOException {
		return getObjectsFromFile(interfaceClass, fileName, Charset.forName(Constant.UTF8_CHARSET_NAME));
	}

	/**
	 * Reads all objects from the file.
	 *
	 * @param interfaceClass type of object to be ridden
	 * @param fileName       a pathname string
	 * @param charset
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
			final String fileName, final Charset charset) throws FileNotFoundException, SecurityException,
			UnsupportedEncodingException, NullPointerException, XmlInvalidLtException, IOException {
		return getObjectsFromStream(interfaceClass, true, new FileInputStream(new File(fileName)), charset);
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
	public static <I extends XmlObjectModelling> List<I> getObjectsFromStream(final Class<I> interfaceClass,
			final InputStream is) throws NullPointerException, XmlInvalidLtException, IOException {
		return getObjectsFromStream(interfaceClass, is, Charset.forName(Constant.UTF8_CHARSET_NAME));
	}

	/**
	 * Reads all objects from the stream. And don't closes it.
	 *
	 * @param interfaceClass type of object to be ridden
	 * @param is             an InputStream
	 * @param charset
	 * @return all the ridden objects
	 * @throws UnsupportedEncodingException If the named charset is not supported
	 * @throws NullPointerException         If trustClass has a class null
	 * @throws XmlInvalidLtException        If is reading a corrupted XML
	 * @throws IOException                  If an I/O error occurs
	 */
	public static <I extends XmlObjectModelling> List<I> getObjectsFromStream(final Class<I> interfaceClass,
			final InputStream is, final Charset charset)
			throws UnsupportedEncodingException, NullPointerException, XmlInvalidLtException, IOException {
		return getObjectsFromStream(interfaceClass, false, is, charset);
	}

	/**
	 * Reads all objects from the stream.
	 *
	 * @param interfaceClass type of object to be ridden
	 * @param withClose      defines if is to close the stream at the end of write
	 * @param is             an InputStream
	 * @param charset
	 * @return all the ridden objects
	 * @throws UnsupportedEncodingException If the named charset is not supported
	 * @throws NullPointerException         If trustClass has a class null
	 * @throws XmlInvalidLtException        If is reading a corrupted XML
	 * @throws IOException                  If an I/O error occurs
	 */
	private static <I extends XmlObjectModelling> List<I> getObjectsFromStream(final Class<I> interfaceClass,
			final boolean withClose, final InputStream is, final Charset charset)
			throws UnsupportedEncodingException, NullPointerException, XmlInvalidLtException, IOException {
		final List<I> result = new ArrayList<>();
		I tmp;
		final XmlomReader in = new XmlomReader(is, charset);
		try {
			while ((tmp = in.read(interfaceClass)) != null) {
				result.add(tmp);
			}
		} finally {
			if (withClose) {
				in.close();
			}
		}
		return result;
	}

	/**
	 * Reads the content of configuration file in default file for the
	 * interfaceClass. With charset <code>Constant.UTF8_CHARSET_NAME</code>. If does
	 * not exist, write a standard one as an example, and return null.
	 *
	 * @param interfaceClass
	 * @return
	 * @throws NullPointerException If the default filename for interfaceClass
	 *                              results in <code>null</code>
	 * @throws IOException          If an I/O error occurred
	 * @throws SecurityException    If a security manager exists and it denies read
	 *                              or write
	 */
	public static <I extends XmlObjectModelling> List<I> getConfig(final Class<I> interfaceClass)
			throws NullPointerException, SecurityException, IOException {
		return getConfig(interfaceClass, Charset.forName(Constant.UTF8_CHARSET_NAME), true);
	}

	/**
	 * Reads the content of configuration file in default file for the
	 * interfaceClass. If does not exist, write a standard one as an example, and
	 * return null.
	 *
	 * @param interfaceClass
	 * @param charset
	 * @param standardContent
	 * @return
	 * @throws NullPointerException If the default filename for interfaceClass
	 *                              results in <code>null</code>
	 * @throws IOException          If an I/O error occurred
	 * @throws SecurityException    If a security manager exists and it denies read
	 *                              or write
	 */
	public static <I extends XmlObjectModelling> List<I> getConfig(final Class<I> interfaceClass, final Charset charset,
			final boolean standardContent) throws NullPointerException, SecurityException, IOException {
		return getConfig(interfaceClass, FileUtil.defaultPropertyClassFilename(interfaceClass), charset,
				standardContent);
	}

	/**
	 * Reads the content of configuration file. If does not exist, write a standard
	 * one as an example, and return null.
	 *
	 * @param interfaceClass
	 * @param fromFile
	 * @param charset
	 * @param standardContent
	 * @return
	 */
	public static <I extends XmlObjectModelling> List<I> getConfig(final Class<I> interfaceClass, final File fromFile,
			final Charset charset, final boolean standardContent) {
		final List<I> defaultContent;
		if (standardContent) {
			defaultContent = new ArrayList<>(1);
			defaultContent.add(createExample(interfaceClass));
		} else {
			defaultContent = null;
		}
		return getConfig(interfaceClass, fromFile, charset, defaultContent);
	}

	private static <I extends XmlObjectModelling> I createExample(final Class<I> interfaceClass) {
		final I result = newXmlObjectModelling(interfaceClass);
		// TODO implement a new xmlom with all fields fulfilled as example
		// (attention to loops)
		return result;
	}

	/**
	 * Reads the content of configuration file in default file for the
	 * interfaceClass. With charset <code>Constant.UTF8_CHARSET_NAME</code>. If does
	 * not exist, write the default one, and gives it as return.
	 *
	 * @param interfaceClass
	 * @param defaultContent
	 * @return
	 * @throws NullPointerException If the default filename for interfaceClass
	 *                              results in <code>null</code>
	 * @throws IOException          If an I/O error occurred
	 * @throws SecurityException    If a security manager exists and it denies read
	 *                              or write
	 */
	public static <I extends XmlObjectModelling> List<I> getConfig(final Class<I> interfaceClass,
			final List<I> defaultContent) throws NullPointerException, SecurityException, IOException {
		return getConfig(interfaceClass, Charset.forName(Constant.UTF8_CHARSET_NAME), defaultContent);
	}

	/**
	 * Reads the configuration file in default file for the interfaceClass.If does
	 * not exist, write the default one, and gives it as return.
	 *
	 * @param interfaceClass
	 * @param charset
	 * @param defaultContent
	 * @return
	 * @throws NullPointerException If the default filename for interfaceClass
	 *                              results in <code>null</code>
	 * @throws IOException          If an I/O error occurred
	 * @throws SecurityException    If a security manager exists and it denies read
	 *                              or write
	 */
	public static <I extends XmlObjectModelling> List<I> getConfig(final Class<I> interfaceClass, final Charset charset,
			final List<I> defaultContent) throws NullPointerException, SecurityException, IOException {
		return getConfig(interfaceClass, FileUtil.defaultPropertyClassFilename(interfaceClass), charset,
				defaultContent);
	}

	/**
	 * Reads the content of configuration file. If does not exist, write the default
	 * one, and gives it as return.
	 *
	 * @param interfaceClass
	 * @param fromFile
	 * @param charset
	 * @param defaultContent
	 * @return
	 */
	public static <I extends XmlObjectModelling> List<I> getConfig(final Class<I> interfaceClass, final File fromFile,
			final Charset charset, final List<I> defaultContent) {
		// TODO implement
		return null;
	}

}
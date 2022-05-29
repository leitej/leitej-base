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
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import leitej.Constant;
import leitej.exception.XmlInvalidLtException;
import leitej.util.stream.FileUtil;

/**
 * An useful class to help in XML Object Modelling stream.
 *
 * @author Julio Leite
 * @see leitej.xml.om.XmlomWriter
 * @see leitej.xml.om.XmlomReader
 */
public final class Xmlom {

	/**
	 * Creates a new instance of XMLOMUtil.
	 */
	private Xmlom() {
	}

	/**
	 * Instantiates a new xmlom object.
	 *
	 * @param interfaceClass to interact to this new xmlom object
	 * @return the new xmlom object
	 */
	public static <I extends XmlObjectModelling> I newInstance(final Class<I> interfaceClass) {
		return XmlomWriter.newXmlObjectModelling(interfaceClass);
	}

	/**
	 * Gives the xmlom interface used in <code>proxy</code> instantiation.
	 *
	 * @param proxy xmlom object
	 * @return xmlom interface of argument
	 */
	public static <I extends XmlObjectModelling> Class<I> getInterface(final I proxy) {
		return DataProxyHandler.class.cast(Proxy.getInvocationHandler(proxy)).getInterface();
	}

	/**
	 * Check data in <code>obj</code> with obfuscated annotation if is in obfuscated
	 * state. If no data has to be obfuscated this method returns true.<br>
	 * <br>
	 * Attention: this method iterates through all linked
	 * <code>XmlObjectModelling</code> recursively starting from <code>obj</code>,
	 * it is protected to not check the same link twice.
	 *
	 * @param obj xmlom to check
	 * @return false if there is data that has be obfuscated and was load in plain
	 *         state
	 */
	public static <I extends XmlObjectModelling> boolean isSerializationObfustated(final I obj) {
		return DataProxyHandler.class.cast(Proxy.getInvocationHandler(obj)).isSerializationObfustated();
	}

	/**
	 * Saves the object to a file with the
	 * <code>Constant.UTF8_CHARSET_NAME</code>.<br/>
	 * This method will try to create the file if not exists.
	 *
	 * @param file
	 * @param obj  object to be saved
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
	public static <I extends XmlObjectModelling> void sendToFile(final File file, final I obj)
			throws NullPointerException, FileNotFoundException, SecurityException, IOException {
		sendToFile(file, Charset.forName(Constant.UTF8_CHARSET_NAME), false, obj);
	}

	/**
	 * Saves the objects to a file with the
	 * <code>Constant.UTF8_CHARSET_NAME</code>.<br/>
	 * This method will try to create the file if not exists.
	 *
	 * @param file
	 * @param objs objects to be saved
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
	public static <I extends XmlObjectModelling> void sendToFile(final File file, final I[] objs)
			throws NullPointerException, FileNotFoundException, SecurityException, IOException {
		sendToFile(file, Charset.forName(Constant.UTF8_CHARSET_NAME), false, objs);
	}

	/**
	 * Saves the objects to a file.<br/>
	 * This method will try to create the file if not exists.
	 *
	 * @param file
	 * @param charset
	 * @param minified when false produces a human readable XML, other wise outputs
	 *                 a clean strait line
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
	public static <I extends XmlObjectModelling> void sendToFile(final File file, final Charset charset,
			final boolean minified, final I[] objs)
			throws NullPointerException, FileNotFoundException, SecurityException, IOException {
		FileUtil.createFile(file);
		final OutputStream os = new FileOutputStream(file, false);
		sendToStream(true, os, charset, minified, objs);
	}

	/**
	 * Saves the object to a file.<br/>
	 * This method will try to create the file if not exists.
	 *
	 * @param file
	 * @param charset
	 * @param minified when false produces a human readable XML, other wise outputs
	 *                 a clean strait line
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
	public static <I extends XmlObjectModelling> void sendToFile(final File file, final Charset charset,
			final boolean minified, final I obj)
			throws NullPointerException, FileNotFoundException, SecurityException, IOException {
		final OutputStream os = new FileOutputStream(FileUtil.createFile(file), false);
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
	 * @throws IOException If an I/O error occurs
	 */
	public static <I extends XmlObjectModelling> void sendToStream(final OutputStream os, final Charset charset,
			final boolean minified, final I[] objs) throws IOException {
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
	 * @throws IOException If an I/O error occurs
	 */
	private static <I extends XmlObjectModelling> void sendToStream(final boolean withClose, final OutputStream os,
			final Charset charset, final boolean minified, final I obj) throws IOException {
		final XmlomWriter out = new XmlomWriter(os, charset, minified);
		try {
			out.write(obj);
			out.flush();
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
	 * @throws IOException If an I/O error occurs
	 */
	private static <I extends XmlObjectModelling> void sendToStream(final boolean withClose, final OutputStream os,
			final Charset charset, final boolean minified, final I[] objs) throws IOException {
		final XmlomWriter out = new XmlomWriter(os, charset, minified);
		try {
			out.write(objs);
			out.flush();
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
	 * @param file
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
			final File file)
			throws FileNotFoundException, SecurityException, NullPointerException, XmlInvalidLtException, IOException {
		return getObjectsFromFile(interfaceClass, file, Charset.forName(Constant.UTF8_CHARSET_NAME));
	}

	/**
	 * Reads all objects from the file.
	 *
	 * @param interfaceClass type of object to be ridden
	 * @param file
	 * @param charset
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
			final File file, final Charset charset)
			throws FileNotFoundException, SecurityException, NullPointerException, XmlInvalidLtException, IOException {
		return getObjectsFromStream(interfaceClass, true, new FileInputStream(file), charset);
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
	 * @throws NullPointerException  If trustClass has a class null
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 * @throws IOException           If an I/O error occurs
	 */
	public static <I extends XmlObjectModelling> List<I> getObjectsFromStream(final Class<I> interfaceClass,
			final InputStream is, final Charset charset) throws NullPointerException, XmlInvalidLtException, IOException {
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
	 * @throws NullPointerException  If trustClass has a class null
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 * @throws IOException           If an I/O error occurs
	 */
	private static <I extends XmlObjectModelling> List<I> getObjectsFromStream(final Class<I> interfaceClass,
			final boolean withClose, final InputStream is, final Charset charset)
			throws NullPointerException, XmlInvalidLtException, IOException {
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
	 * not exist, write a standard one as an example, and return null.<br>
	 * If configuration file has obfuscated parameters unprotected, it will be
	 * rewritten to obfuscate it.
	 *
	 * @param interfaceClass
	 * @return
	 * @throws FileNotFoundException If default filename results in a directory
	 *                               rather than a regular file, or for some other
	 *                               reason cannot be opened for reading
	 * @throws IOException           If an I/O error occurred
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 * @throws NullPointerException  If the default filename for interfaceClass
	 *                               results in <code>null</code>
	 * @throws SecurityException     If a security manager exists and it denies read
	 *                               or write
	 */
	public static <I extends XmlObjectModelling> List<I> getConfig(final Class<I> interfaceClass)
			throws FileNotFoundException, NullPointerException, SecurityException, IOException, XmlInvalidLtException {
		return getConfig(interfaceClass, Charset.forName(Constant.UTF8_CHARSET_NAME), true);
	}

	/**
	 * Reads the content of configuration file in default file for the
	 * interfaceClass. If does not exist, write a standard one as an example, and
	 * return null.<br>
	 * If configuration file has obfuscated parameters unprotected, it will be
	 * rewritten to obfuscate it.
	 *
	 * @param interfaceClass
	 * @param charset
	 * @param standardContent
	 * @return
	 * @throws FileNotFoundException If default filename results in a directory
	 *                               rather than a regular file, or for some other
	 *                               reason cannot be opened for reading
	 * @throws IOException           If an I/O error occurred
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 * @throws NullPointerException  If the default filename for interfaceClass
	 *                               results in <code>null</code>
	 * @throws SecurityException     If a security manager exists and it denies read
	 *                               or write
	 */
	public static <I extends XmlObjectModelling> List<I> getConfig(final Class<I> interfaceClass, final Charset charset,
			final boolean standardContent)
			throws FileNotFoundException, NullPointerException, SecurityException, IOException, XmlInvalidLtException {
		return getConfig(interfaceClass, FileUtil.defaultPropertyClassFilename(interfaceClass), charset, true,
				standardContent);
	}

	/**
	 * Reads the content of configuration file. If does not exist, write a standard
	 * one as an example, and return null.
	 *
	 * @param interfaceClass
	 * @param fromFile
	 * @param charset
	 * @param rewriteIfNotObfuscated
	 * @param standardContent
	 * @return
	 * @throws FileNotFoundException If <code>fromFile</code> is a directory rather
	 *                               than a regular file, or for some other reason
	 *                               cannot be opened for reading
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 * @throws NullPointerException  If any of arguments is null
	 * @throws SecurityException     If a security manager exists and it denies read
	 *                               or write
	 */
	public static <I extends XmlObjectModelling> List<I> getConfig(final Class<I> interfaceClass, final File fromFile,
			final Charset charset, final boolean rewriteIfNotObfuscated, final boolean standardContent)
			throws FileNotFoundException, SecurityException, NullPointerException, XmlInvalidLtException, IOException {
		return getConfig(interfaceClass, fromFile, charset, true, null, createExample(interfaceClass));
	}

	private static <I extends XmlObjectModelling> I createExample(final Class<I> interfaceClass) {
		final I result = newInstance(interfaceClass);
		// TODO implement a new xmlom with all fields fulfilled as example
		// (attention to loops)
		return result;
	}

	/**
	 * Reads the content of configuration file in default file for the
	 * interfaceClass. With charset <code>Constant.UTF8_CHARSET_NAME</code>. If does
	 * not exist, write the default one, and gives it as return.<br>
	 * If configuration file has obfuscated parameters unprotected, it will be
	 * rewritten to obfuscate it.
	 *
	 * @param interfaceClass
	 * @param defaultContent
	 * @return
	 * @throws FileNotFoundException If default location is a directory rather than
	 *                               a regular file, or for some other reason cannot
	 *                               be opened for reading
	 * @throws NullPointerException  If <code>interfaceClass</code> is null
	 * @throws IOException           If an I/O error occurred
	 * @throws SecurityException     If a security manager exists and it denies read
	 *                               or write
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 */
	public static <I extends XmlObjectModelling> List<I> getConfig(final Class<I> interfaceClass,
			final I[] defaultContent)
			throws FileNotFoundException, NullPointerException, SecurityException, IOException, XmlInvalidLtException {
		return getConfig(interfaceClass, Charset.forName(Constant.UTF8_CHARSET_NAME), defaultContent);
	}

	/**
	 * Reads the configuration file in default file for the interfaceClass.If does
	 * not exist, write the default one, and gives it as return.<br>
	 * If configuration file has obfuscated parameters unprotected, it will be
	 * rewritten to obfuscate it.
	 *
	 * @param interfaceClass
	 * @param charset
	 * @param defaultContent
	 * @return
	 * @throws FileNotFoundException If default location is a directory rather than
	 *                               a regular file, or for some other reason cannot
	 *                               be opened for reading
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 * @throws NullPointerException  If any of arguments is null, except
	 *                               <code>defaultContent</code>
	 * @throws SecurityException     If a security manager exists and it denies read
	 *                               or write
	 */
	public static <I extends XmlObjectModelling> List<I> getConfig(final Class<I> interfaceClass, final Charset charset,
			final I[] defaultContent)
			throws FileNotFoundException, NullPointerException, SecurityException, IOException, XmlInvalidLtException {
		return getConfig(interfaceClass, FileUtil.defaultPropertyClassFilename(interfaceClass), charset, true,
				defaultContent);
	}

	/**
	 * Reads the content of configuration file. If does not exist, write the default
	 * one, and gives it as return.
	 *
	 * @param interfaceClass
	 * @param fromFile
	 * @param charset
	 * @param rewriteIfNotObfuscated
	 * @param defaultContent
	 * @return
	 * @throws FileNotFoundException If <code>fromFile</code> is a directory rather
	 *                               than a regular file, or for some other reason
	 *                               cannot be opened for reading
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 * @throws NullPointerException  If any of arguments is null, except
	 *                               <code>defaultContent</code>
	 * @throws SecurityException     If a security manager exists and it denies read
	 *                               or write
	 */
	public static <I extends XmlObjectModelling> List<I> getConfig(final Class<I> interfaceClass, final File fromFile,
			final Charset charset, final boolean rewriteIfNotObfuscated, final I[] defaultContent)
			throws FileNotFoundException, SecurityException, NullPointerException, XmlInvalidLtException, IOException {
		return getConfig(interfaceClass, fromFile, charset, rewriteIfNotObfuscated, defaultContent, null);
	}

	/**
	 * Reads the content of configuration file. If does not exist, write the example
	 * and the default one, and gives the default as return.
	 *
	 * @param interfaceClass
	 * @param fromFile
	 * @param charset
	 * @param rewriteIfNotObfuscated
	 * @param defaultContent
	 * @param exampleContent
	 * @return
	 * @throws FileNotFoundException If <code>fromFile</code> is a directory rather
	 *                               than a regular file, or for some other reason
	 *                               cannot be opened for reading
	 * @throws IOException           If an I/O error occurs
	 * @throws XmlInvalidLtException If is reading a corrupted XML
	 * @throws NullPointerException  If any of arguments is null, except
	 *                               <code>defaultContent</code> and
	 *                               <code>exampleContent</code>
	 * @throws SecurityException     If a security manager exists and it denies read
	 *                               or write
	 */
	public static <I extends XmlObjectModelling> List<I> getConfig(final Class<I> interfaceClass, final File fromFile,
			final Charset charset, final boolean rewriteIfNotObfuscated, final I[] defaultContent, final I exampleContent)
			throws FileNotFoundException, SecurityException, NullPointerException, XmlInvalidLtException, IOException {
		List<I> result = null;
		try {
			result = Xmlom.getObjectsFromFile(interfaceClass, fromFile, charset);
		} catch (final FileNotFoundException e1) {
//			System.err.println("xmlom util - " + e1.getMessage());
			if (exampleContent != null) {
				final File example = new File(fromFile.getAbsolutePath() + Constant.DEFAULT_EXAMPLE_EXTENSION);
				if (!example.exists()) {
//					System.err.println("xmlom util - config - writing - " + example);
					Xmlom.sendToFile(example, charset, false, exampleContent);
				}
			}
			if (defaultContent != null && !fromFile.exists()) {
//				System.err.println("xmlom util - config - writing - " + filename);
				Xmlom.sendToFile(fromFile, charset, false, defaultContent);
			}
			result = Arrays.asList(defaultContent);
		}
		if (rewriteIfNotObfuscated) {
			boolean obfuscated = true;
			for (final I i : result) {
				obfuscated &= isSerializationObfustated(i);
			}
			if (!obfuscated) {
				Xmlom.sendToFile(fromFile, charset, false, result.toArray(new XmlObjectModelling[result.size()]));
			}
		}
		return result;
	}

}

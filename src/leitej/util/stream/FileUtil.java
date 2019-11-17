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

package leitej.util.stream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;

import leitej.Constant;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;

/**
 * An useful class to help in file stream.
 *
 * @author Julio Leite
 */
public final class FileUtil {

	public final static String LINE_SEPARATOR = Constant.DEFAULT_LINE_SEPARATOR;
	public final static String FILE_SEPARATOR = Constant.DEFAULT_FILE_SEPARATOR;
	public final static String PATH_SEPARATOR = Constant.DEFAULT_PATH_SEPARATOR;

	public final static String BACKUP_EXTENSION = Constant.DEFAULT_BACKUP_EXTENSION;

	/**
	 * Creates a new instance of FileUtil.
	 */
	private FileUtil() {
	}

	/**
	 * Creates a new File instance by converting the given pathname string into an
	 * abstract pathname. If the given string is the empty string, then the result
	 * is the empty abstract pathname.
	 *
	 * @param filename a pathname string
	 * @return File (if created or already existed) or <code>null</code> if could
	 *         not create for any reason
	 * @throws NullPointerException If the <code>filename</code> argument is
	 *                              <code>null</code>
	 * @throws IOException          If an I/O error occurred
	 * @throws SecurityException    If a security manager exists and its
	 *                              <code>{@link
	 *          java.lang.SecurityManager#checkRead(java.lang.String) checkRead}</code>
	 *                              method does not permit verification of the
	 *                              existence of the named directory and all
	 *                              necessary parent directories; or if the
	 *                              <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                              method does not permit the named directory and
	 *                              all necessary parent directories to be created;
	 *                              or if a security manager exists and its
	 *                              <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                              method denies write access to the file
	 */
	public static File createFile(final String filename) throws NullPointerException, SecurityException, IOException {
		File result = null;
		boolean pass = true;
		final File file = new File(filename);
		if (!file.exists()) {
			if (pass && file.getParentFile() != null && !file.getParentFile().exists()) {
				pass = file.getParentFile().mkdirs();
			}
			if (pass) {
				pass = file.createNewFile();
			}
		}
		if (pass) {
			result = file;
		}
		return result;
	}

	/**
	 * Creates a new File instance by converting the given pathname string into an
	 * abstract pathname. If the given string is the empty string, then the result
	 * is the empty abstract pathname.<br/>
	 * The file will be created with the <code>length</code> size.<br/>
	 * This method only changes FileSystem if the pathname given does not exists.
	 *
	 * @param filename a pathname string
	 * @param length   the size of the file
	 * @return True if could create the file with the specific length
	 * @throws IllegalArgumentException If the <code>length</code> argument is less
	 *                                  then <code>zero</code>
	 * @throws NullPointerException     If the <code>filename</code> argument is
	 *                                  <code>null</code>
	 * @throws IOException              If an I/O error occurred
	 * @throws SecurityException        If a security manager exists and its
	 *                                  <code>{@link
	 *          java.lang.SecurityManager#checkRead(java.lang.String) checkRead}</code>
	 *                                  method does not permit verification of the
	 *                                  existence of the named directory and all
	 *                                  necessary parent directories; or if the
	 *                                  <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                                  method does not permit the named directory
	 *                                  and all necessary parent directories to be
	 *                                  created; or if a security manager exists and
	 *                                  its <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                                  method denies write access to the file
	 */
	public static boolean createFile(final String filename, final long length)
			throws IllegalArgumentException, NullPointerException, SecurityException, IOException {
		if (length < 0) {
			throw new IllegalArgumentLtRtException("lt.NegativeValue", "length");
		}
		boolean result;
		final File file = new File(filename);
		if (file.getParentFile() != null && !file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if (!file.exists()) {
			final RandomAccessFile raFile = new RandomAccessFile(file, "rw");
			try {
				if (length > 0) {
					raFile.setLength(length);
				}
			} finally {
				raFile.close();
			}
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * Gives a buffered character-output stream that uses a default-sized output
	 * buffer.<br/>
	 * This method uses <code>createFile(String)</code> to try to create if doesn't
	 * exist.
	 *
	 * @param filename a pathname string
	 * @param append   if true, then bytes will be written to the end of the file
	 *                 rather than the beginning
	 * @param charset  name of a supported {@link java.nio.charset.Charset
	 *                 </code>charset<code>}
	 * &#64;return BufferedWriter
	 * &#64;throws UnsupportedEncodingException If the named encoding is not supported
	 * &#64;throws FileNotFoundException If the file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened for any other reason
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
	 */
	public static BufferedWriter openFileOutputWriter(final String filename, final boolean append, final String charset)
			throws SecurityException, UnsupportedEncodingException, FileNotFoundException, NullPointerException,
			IOException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(createFile(filename), append), charset));
	}

	/**
	 * Just verifies if the path to <code>file</code> exists, if not tries make it.
	 *
	 * @param file An abstract representation of file and directory pathnames
	 * @return boolean true if exists or had been created
	 * @throws SecurityException If a security manager exists and its <code>{@link
	 *          java.lang.SecurityManager#checkRead(java.lang.String) checkRead}</code>
	 *                           method does not permit verification of the
	 *                           existence of the named directory and all necessary
	 *                           parent directories; or if the <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String) checkWrite}</code>
	 *                           method does not permit the named directory and all
	 *                           necessary parent directories to be created
	 */
	public static boolean createPathFromFile(final File file) throws SecurityException {
		boolean result = true;
		if (file.getParentFile() != null && !file.getParentFile().exists()) {
			result = file.getParentFile().mkdirs();
		}
		return result;
	}

	/**
	 * Gives a new buffered output stream to write data to the specified underlying
	 * output stream created from parameters.
	 *
	 * @param filename a pathname string
	 * @param append   if true, then bytes will be written to the end of the file
	 *                 rather than the beginning
	 * @return BufferedOutputStream
	 * @throws NullPointerException  If the <code>filename</code> argument is
	 *                               <code>null</code>
	 * @throws FileNotFoundException If the file exists but is a directory rather
	 *                               than a regular file, does not exist but cannot
	 *                               be created, or cannot be opened for any other
	 *                               reason
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
	public static BufferedOutputStream openFileBinaryOutputStream(final String filename, final boolean append)
			throws NullPointerException, FileNotFoundException, SecurityException, IOException {
		return new BufferedOutputStream(new FileOutputStream(createFile(filename), append));
	}

	/**
	 * Gives a new buffered input stream to read data to the specified underlying
	 * input stream created from parameters.
	 *
	 * @param filename a pathname string
	 * @return BufferedInputStream
	 * @throws NullPointerException  If the <code>filename</code> argument is
	 *                               <code>null</code>
	 * @throws SecurityException     If a security manager exists and its
	 *                               <code>checkRead</code> method denies read
	 *                               access to the file
	 * @throws FileNotFoundException If the file does not exist, is a directory
	 *                               rather than a regular file, or for some other
	 *                               reason cannot be opened for reading
	 */
	public static BufferedInputStream openFileBinaryInputStream(final String filename)
			throws NullPointerException, SecurityException, FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(new File(filename)));
	}

	/**
	 * Writes binary data to a file, all at once.
	 *
	 * @param bytes    the data to write
	 * @param filename a pathname string
	 * @param append   if true, then bytes will be written to the end of the file
	 *                 rather than the beginning
	 * @throws NullPointerException  If the <code>filename</code> argument is
	 *                               <code>null</code>
	 * @throws FileNotFoundException If the file exists but is a directory rather
	 *                               than a regular file, does not exist but cannot
	 *                               be created, or cannot be opened for any other
	 *                               reason
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
	public static void writeAllAtOnce(final byte[] bytes, final String filename, final boolean append)
			throws NullPointerException, FileNotFoundException, SecurityException, IOException {
		final OutputStream bufferedOutputStream = openFileBinaryOutputStream(filename, append);
		try {
			bufferedOutputStream.write(bytes);
			bufferedOutputStream.flush();
		} finally {
			bufferedOutputStream.close();
		}
	}

	/**
	 * Reads binary data from a file, all at once.<br/>
	 * The content can not have more then <code>Integer.MAX_VALUE</code> of length.
	 *
	 * @param file an abstract representation of file and directory pathnames
	 * @return the content of the file
	 * @throws FileNotFoundException If the file does not exist, is a directory
	 *                               rather than a regular file, or for some other
	 *                               reason cannot be opened for reading.
	 * @throws SecurityException     If a security manager exists and its
	 *                               <code>checkRead</code> method denies read
	 *                               access to the file
	 * @throws IOException           If the first byte cannot be read for any reason
	 *                               other than end of file, or if the input stream
	 *                               has been closed, or if some other I/O error
	 *                               occurs
	 */
	public static byte[] readAllAtOnce(final File file) throws FileNotFoundException, SecurityException, IOException {
		if (file.length() > Integer.MAX_VALUE) {
			throw new IllegalArgumentLtRtException("lt.FileLengthTooBig", file.getAbsoluteFile());
		}
		final InputStream is = new FileInputStream(file);
		byte[] result;
		final int arrayLength = (int) file.length();
		result = (byte[]) Array.newInstance(byte.class, arrayLength);
		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		try {
			while (offset < arrayLength
					&& (numRead = is.read(result, offset, Math.min(arrayLength - offset, 512 * Constant.KILO))) >= 0) {
				offset += numRead;
			}
			// Ensure all the bytes have been read in
			if (offset < arrayLength) {
				throw new IOException(new IllegalStateLtRtException("lt.IncompleteReadFile", file.getAbsoluteFile()));
			}
		} finally {
			is.close();
		}
		return result;
	}

	/**
	 * Reads binary data from a file, all at once.<br/>
	 * The content can not have more then <code>Integer.MAX_VALUE</code> of length.
	 *
	 * @param filename a pathname string
	 * @return the content of the file
	 * @throws NullPointerException  If the <code>filename</code> argument is
	 *                               <code>null</code>
	 * @throws FileNotFoundException If the file does not exist, is a directory
	 *                               rather than a regular file, or for some other
	 *                               reason cannot be opened for reading.
	 * @throws SecurityException     If a security manager exists and its
	 *                               <code>checkRead</code> method denies read
	 *                               access to the file
	 * @throws IOException           If the first byte cannot be read for any reason
	 *                               other than end of file, or if the input stream
	 *                               has been closed, or if some other I/O error
	 *                               occurs
	 */
	public static byte[] readAllAtOnce(final String filename)
			throws NullPointerException, FileNotFoundException, SecurityException, IOException {
		return readAllAtOnce(new File(filename));
	}

	/**
	 * Makes the property path file with extension for a simple name.<br/>
	 * The path file will have a relative reference to the directory META-INF.
	 *
	 * @param filename to make the path
	 * @return String path
	 */
	public static String propertieRelativePath4FileName(final String filename) {
		return Constant.DEFAULT_PROPERTIES_FILE_DIR + filename + Constant.DEFAULT_PROPERTIES_XML_FILE_EXT;
	}

	/**
	 * Verifies whether the file or directory denoted by this abstract pathname
	 * exists.
	 *
	 * @param filename a pathname string
	 * @return true If and only if the file or directory denoted by this abstract
	 *         pathname exists; false otherwise
	 * @throws NullPointerException If the <code>filename</code> argument is
	 *                              <code>null</code>
	 * @throws SecurityException    If a security manager exists and its
	 *                              <code>{@link java.lang.SecurityManager#checkRead(java.lang.String) checkRead}</code>
	 *                              method denies read access to the file or
	 *                              directory
	 */
	public static boolean exists(final String filename) throws NullPointerException, SecurityException {
		return (new File(filename)).exists();
	}

	/**
	 * Returns the length of the file denoted by this abstract pathname. The return
	 * value is unspecified if this pathname denotes a directory.
	 *
	 * @param filename a pathname string
	 * @return the length, in bytes, of the file denoted by this abstract pathname,
	 *         or 0L if the file does not exist. Some operating systems may return
	 *         0L for pathnames denoting system-dependent entities such as devices
	 *         or pipes
	 * @throws NullPointerException If the <code>filename</code> argument is
	 *                              <code>null</code>
	 * @throws SecurityException    If a security manager exists and its
	 *                              <code>{@link java.lang.SecurityManager#checkRead(java.lang.String) checkRead}</code>
	 *                              method denies read access to the file
	 */
	public static long getFileSize(final String filename) throws NullPointerException, SecurityException {
		return (new File(filename)).length();
	}

	public static boolean setFileSize(final String filename, final long length)
			throws IllegalArgumentException, NullPointerException, SecurityException, IOException {
		if (length < 0) {
			return false;
		}
		final File file = new File(filename);
		if (!file.exists()) {
			return createFile(filename, length);
		} else {
			final RandomAccessFile raFile = new RandomAccessFile(file, "rw");
			try {
				raFile.setLength(length);
			} finally {
				raFile.close();
			}
			return true;
		}
	}

	/**
	 * Calculates the MD5 of a file.
	 *
	 * @param file the file to be opened for reading
	 * @return the array of bytes for the resulting hash value
	 * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi
	 *                                  implementation for the specified algorithm
	 * @throws FileNotFoundException    if the file does not exist, is a directory
	 *                                  rather than a regular file, or for some
	 *                                  other reason cannot be opened for reading
	 * @throws SecurityException        if a security manager exists and its
	 *                                  <code>checkRead</code> method denies read
	 *                                  access to the file
	 * @throws IOException              if the first byte cannot be read for any
	 *                                  reason other than the end of the file, if
	 *                                  the input stream has been closed, or if some
	 *                                  other I/O error occurs
	 */
	public static byte[] md5(final File file)
			throws SecurityException, NoSuchAlgorithmException, FileNotFoundException, IOException {
		return StreamUtil.md5(new BufferedInputStream(new FileInputStream(file)));
	}

	/**
	 * Calculates the MD5 of a file.
	 *
	 * @param filename a pathname string
	 * @return the array of bytes for the resulting hash value
	 * @throws NullPointerException     if the <code>filename</code> argument is
	 *                                  <code>null</code>
	 * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi
	 *                                  implementation for the specified algorithm
	 * @throws FileNotFoundException    if the file does not exist, is a directory
	 *                                  rather than a regular file, or for some
	 *                                  other reason cannot be opened for reading
	 * @throws SecurityException        if a security manager exists and its
	 *                                  <code>checkRead</code> method denies read
	 *                                  access to the file
	 * @throws IOException              if the first byte cannot be read for any
	 *                                  reason other than the end of the file, if
	 *                                  the input stream has been closed, or if some
	 *                                  other I/O error occurs
	 */
	public static byte[] md5(final String filename) throws NullPointerException, SecurityException,
			NoSuchAlgorithmException, FileNotFoundException, IOException {
		return md5(new File(filename));
	}

	/**
	 * Renames the file denoted by this abstract pathname <code>fromFilename</code>
	 * to abstract pathname <code>toFilename</code>. <br/>
	 * <br/>
	 * Many aspects of the behavior of this method are inherently
	 * platform-dependent: The rename operation might not be able to move a file
	 * from one filesystem to another, it might not be atomic, and it might not
	 * succeed if a file with the destination abstract pathname already exists.
	 * <br/>
	 * The return value should always be checked to make sure that the rename
	 * operation was successful.
	 *
	 * @param fromFilename the abstract pathname of the named file to rename
	 * @param toFilename   the new abstract pathname for the named file
	 * @return <code>true</code> if and only if the renaming succeeded;
	 *         <code>false</code> otherwise
	 * @throws SecurityException    if a security manager exists and its
	 *                              <code>{@link java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
	 *                              method denies write access to either the old or
	 *                              new pathnames
	 * @throws NullPointerException if parameter <code>fromFilename</code> or
	 *                              <code>toFilename</code> is <code>null</code>
	 */
	public static boolean rename(final String fromFilename, final String toFilename)
			throws SecurityException, NullPointerException {
		return rename(new File(fromFilename), toFilename);
	}

	/**
	 * Renames the file denoted by <code>fromFile</code> to abstract pathname
	 * <code>toFilename</code>. <br/>
	 * <br/>
	 * Many aspects of the behavior of this method are inherently
	 * platform-dependent: The rename operation might not be able to move a file
	 * from one filesystem to another, it might not be atomic, and it might not
	 * succeed if a file with the destination abstract pathname already exists.
	 * <br/>
	 * The return value should always be checked to make sure that the rename
	 * operation was successful.
	 *
	 * @param fromFile   the file to rename
	 * @param toFilename the new abstract pathname for the named file
	 * @return <code>true</code> if and only if the renaming succeeded;
	 *         <code>false</code> otherwise
	 * @throws SecurityException    if a security manager exists and its
	 *                              <code>{@link java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
	 *                              method denies write access to either the old or
	 *                              new pathnames
	 * @throws NullPointerException if parameter <code>fromFile</code> or
	 *                              <code>toFilename</code> is <code>null</code>
	 */
	public static boolean rename(final File fromFile, final String toFilename)
			throws SecurityException, NullPointerException {
		return fromFile.renameTo((new File(toFilename)));
	}

	public static boolean renameToBackup(final String fromFilename, final boolean override) {
		return renameToBackup(new File(fromFilename), override);
	}

	public static boolean renameToBackup(final File file, final boolean override) {
		boolean result;
		final String filenameBackup = file.getAbsolutePath() + BACKUP_EXTENSION;
		if (override) {
			if (exists(filenameBackup)) {
				if (delete(filenameBackup)) {
					result = rename(file, filenameBackup);
				} else {
					result = false;
				}
			} else {
				result = rename(file, filenameBackup);
			}
		} else {
			if (exists(filenameBackup)) {
				result = false;
			} else {
				result = rename(file, filenameBackup);
			}
		}
		return result;
	}

	public static void copy(final String fromFilename, final String toFilename) {
		copy(new File(fromFilename), new File(toFilename));
	}

	public static void copy(final File fromFile, final String toFilename) {
		copy(fromFile, new File(toFilename));
	}

	public static void copy(final File fromFile, final File toFile) {
		throw new ImplementationLtRtException();
	}

	/**
	 * Deletes the file or directory denoted by this abstract pathname. If this
	 * pathname denotes a directory, then the directory must be empty in order to be
	 * deleted.
	 *
	 * @param file the file to be deleted
	 * @return <code>true</code> if and only if the file or directory is
	 *         successfully deleted; <code>false</code> otherwise
	 * @throws SecurityException If a security manager exists and its
	 *                           <code>{@link java.lang.SecurityManager#checkDelete}</code>
	 *                           method denies delete access to the file
	 */
	public static boolean delete(final File file) throws SecurityException {
		return file.delete();
	}

	/**
	 * Deletes the file or directory denoted by <code>filename</code> pathname. If
	 * this pathname denotes a directory, then the directory must be empty in order
	 * to be deleted.
	 *
	 * @param filename a pathname string
	 * @return <code>true</code> if and only if the file or directory is
	 *         successfully deleted; <code>false</code> otherwise
	 * @throws NullPointerException if the <code>filename</code> argument is
	 *                              <code>null</code>
	 * @throws SecurityException    If a security manager exists and its
	 *                              <code>{@link java.lang.SecurityManager#checkDelete}</code>
	 *                              method denies delete access to the file
	 */
	public static boolean delete(final String filename) throws NullPointerException, SecurityException {
		return delete(new File(filename));
	}

}

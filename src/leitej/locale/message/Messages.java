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

package leitej.locale.message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import leitej.Constant;
import leitej.util.StringUtil;
import leitej.util.stream.FileUtil;

/**
 * This class aims to facilitate the translation of information output from the
 * application.<br />
 * You can get an object to interact via the method <code>getInstance()</code>
 * or <code>getInstance(Locale)</code>.<br />
 * The getInstance will return a different object per Locale. The new instance
 * will load the file called 'messages' with extension
 * <code>locale.getISO3Language()</code> on the directory 'meta-inf'.<br />
 * <p>
 * Example of a 'messages':<br />
 * <code>
 * # Comment<br />
 * app.WelcomeMessage	=Welcome #0. At #1<br />
 * app.End				=Application has finished<br />
 * </code>
 * </p>
 * <p>
 * Sample code to use:<br />
 * <code>
 * ...<br />
 * private static final Messages MESSAGES = Messages.getInstance();<br />
 * ...<br />
 * method(){<br />
 * println(MESSAGES.get("app.WelcomeMessage", username, (new Date())));<br />
 * ...<br />
 * println(MESSAGES.get("app.End"));<br />
 * }<br />
 * ...<br />
 * </code>
 * </p>
 *
 * @author Julio Leite
 */
public final class Messages {

	private static final String DEFAULT_FILE_NAME = Constant.DEFAULT_PROPERTIES_FILE_DIR + "messages.";
	private static final Map<Locale, Messages> ALL_MESSAGES = new HashMap<>();
	private static final Locale DEFAULT_LANG = Locale.ENGLISH;
	private static final Locale DEFAULT_RUNTIME_LANG = Locale.getDefault();

	private final Map<String, String> messages = Collections.synchronizedMap(new HashMap<String, String>());

	private Messages(final Locale locale) {
		super();
		// load project file messages
		try {
			loadFile(DEFAULT_FILE_NAME + locale.getISO3Language(), Constant.UTF8_CHARSET_NAME);
		} catch (final IOException e) {
			if (!(e instanceof FileNotFoundException)) {
				System.err.println(e.getMessage());
			}
			try {
				loadFile(DEFAULT_FILE_NAME + DEFAULT_LANG.getISO3Language(), Constant.UTF8_CHARSET_NAME);
			} catch (final IOException e2) {
				if (!(e2 instanceof FileNotFoundException)) {
					System.err.println(e2.getMessage());
				}
				// write sample file
				try {
					final BufferedWriter bw = FileUtil.openFileOutputWriter(
							DEFAULT_FILE_NAME + DEFAULT_LANG.getISO3Language(), false, Constant.UTF8_CHARSET_NAME);
					for (int i = 0; i < 5; i++) {
						bw.append(MessagesEng.MESSAGES[i]);
						bw.append('\n');
					}
					bw.flush();
					bw.close();
				} catch (final SecurityException | NullPointerException | MissingResourceException | IOException e1) {
					if (!(e1 instanceof FileNotFoundException)) {
						System.err.println(e1.getMessage());
					}
				}
			}
		}
		// load internal(leitej-base) messages
		if (Locale.ENGLISH.equals(locale)) {
			loadArrayString(MessagesEng.MESSAGES);
		} else {
			loadArrayString(MessagesEng.MESSAGES);
		}
	}

	/**
	 * Loads from an array for translation.<br/>
	 * Each array element is the association of the name value.<br/>
	 * You should only load once for each Locale language instance.
	 *
	 * @param msgs Array with the associations of the name value
	 * @return number of entries placed in the message table
	 *
	 * @throws IOException If the named charset is not supported, a security manager
	 *                     exists and its checkRead method denies read access to the
	 *                     file, or an I/O error occurs
	 */
	public int loadArrayString(final String[] msgs) {
		int c = 0;
		for (final String msg : msgs) {
			if (addLine(msg)) {
				c++;
			}
		}
		return c;
	}

	/**
	 * Loads from an inputstream for translation.<br/>
	 * You should only load once for each Locale language instance.
	 *
	 * @param inputstream The inputstream
	 * @param charsetName The name of a supported {@link java.nio.charset.Charset
	 *                    charset}
	 * @return number of entries placed in the message table
	 *
	 * @throws IOException If the named charset is not supported, or an I/O error
	 *                     occurs
	 */
	public int loadStream(final InputStream inputstream, final String charsetName) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream, charsetName));
		int c = 0;
		String tmp;
		do {
			tmp = reader.readLine();
			if (addLine(tmp)) {
				c++;
			}
		} while (tmp != null);
		reader.close();
		return c;
	}

	/**
	 * Loads a file for translation.<br/>
	 * You should only load once for each Locale language instance.
	 *
	 * @param pathname    A pathname string
	 * @param charsetName The name of a supported {@link java.nio.charset.Charset
	 *                    charset}
	 * @return number of entries placed in the message table
	 *
	 * @throws IOException If the named charset is not supported, a security manager
	 *                     exists and its checkRead method denies read access to the
	 *                     file, or an I/O error occurs
	 */
	public int loadFile(final String pathname, final String charsetName) throws IOException {
		return loadStream(new FileInputStream(new File(pathname)), charsetName);
	}

	private boolean addLine(final String line) {
		boolean b = false;
		if (line != null && line.length() > 0 && line.charAt(0) != '#') {
			final String[] aux = line.split("=", 2);
			if (aux.length == 2) {
				this.messages.put(aux[0].trim(), aux[1]);
				b = true;
			}
		}
		return b;
	}

	/**
	 * Translates the name to the message.
	 *
	 * @param messageName name String
	 * @param objects     to use if needed to build the result
	 * @return message
	 */
	public String get(final String messageName, final Object... objects) {
		String result = this.messages.get(messageName);
		if (result == null) {
			result = messageName;
		}
		return StringUtil.insertObjects(result, objects);
	}

	/**
	 * Get the instance in system language (Locale.getDefault()).
	 *
	 * @return the messages instance
	 */
	public static Messages getInstance() {
		return getInstance(DEFAULT_RUNTIME_LANG);
	}

	/**
	 * Get the instance for the argument Locale language.
	 *
	 * @param locale for messages
	 * @return the messages instance
	 */
	public static Messages getInstance(final Locale locale) {
		synchronized (Messages.class) {
			Messages result = ALL_MESSAGES.get(locale);
			if (result == null) {
				result = new Messages(locale);
				ALL_MESSAGES.put(locale, result);
			}
			return result;
		}
	}

}

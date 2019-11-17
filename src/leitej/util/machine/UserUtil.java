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

package leitej.util.machine;

import java.util.Locale;

/**
 * An useful class to help get user information.
 *
 * @author Julio Leite
 */
public final class UserUtil {

	/**
	 * Creates a new instance of UserUtil.
	 */
	private UserUtil() {
	}

	/**
	 * User's account name
	 */
	public static final String USER_NAME = System.getProperties().getProperty("user.name");
	/**
	 * User's home directory
	 */
	public static final String USER_HOME = System.getProperties().getProperty("user.home");
	/**
	 * User's current working directory
	 */
	public static final String USER_DIR = System.getProperties().getProperty("user.dir");

	/**
	 * Default temporary file path
	 */
	public static final String DEFAULT_TMPDIR = System.getProperties().getProperty("java.io.tmpdir");

	/**
	 * User's language
	 */
	public static final String USER_LANG_ISO3 = Locale.getDefault().getISO3Language();

	/**
	 * User's country
	 */
	public static final String USER_COUNTRY_ISO3 = Locale.getDefault().getISO3Country();

}

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

package leitej.util;

/**
 * An useful class to help interact with the main method.
 *
 * @author Julio Leite
 */
public final class MainUtil {

	/**
	 * Creates a new instance of MainUtil.
	 */
	private MainUtil() {
	}

	/**
	 * Parse from args an option value.
	 *
	 * @param args   with all options
	 * @param option to be catched
	 * @return the value on this option
	 */
	public static String getOption(final String[] args, final String option) {
		String result = null;
		if (StringUtil.isNullOrEmpty(option)) {
			return result;
		}
		if (args != null) {
			for (int i = 0; i < args.length && result == null; i++) {
				if (args[i].startsWith(option)) {
					result = args[i].substring(option.length());
				}
			}
		}
		return result;
	}

}

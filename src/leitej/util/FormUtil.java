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
 *
 * @author Julio Leite
 */
public final class FormUtil {

	public static final boolean isValideNIF(final String nif) {
		boolean result = false;
		final String number = nif.replaceAll("\\W", "");
		if (number.length() == 9) {
			boolean invalid = false;
			final int[] pos = new int[9];
			for (int i = 0; i < pos.length; i++) {
				pos[i] = number.charAt(i) - '0';
				if (pos[i] < 0 || pos[i] > 9) {
					invalid = true;
				}
			}
			if (!invalid) {
				int factor;
				int suma = 0;
				for (int i = 0; i < pos.length - 1; i++) {
					factor = 9 - i;
					suma += factor * pos[i];
				}
				int checkDigit;
				final int restDiv = suma % 11;
				if (restDiv == 0 || restDiv == 1) {
					checkDigit = 0;
				} else {
					checkDigit = 11 - restDiv;
				}
				result = checkDigit == pos[8];
			}
		}
		return result;
	}

	private FormUtil() {
	}

}

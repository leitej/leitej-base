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

package leitej.thread;

/**
 * Enumerates all the possible values for ThreadPriorityEnum field.
 *
 * @author Julio Leite
 */
public enum ThreadPriorityEnum {
	MINIMUM, NORMAL, MAXIMUM;

	/**
	 * Gives the corresponding constant used by {@link java.lang.Thread Thread}.
	 *
	 * @return int
	 */
	public int getSystemThreadPriority() {
		switch (this) {
		case MINIMUM:
			return Thread.MIN_PRIORITY;
		case NORMAL:
			return Thread.NORM_PRIORITY;
		case MAXIMUM:
			return Thread.MAX_PRIORITY;
		default:
			return Thread.NORM_PRIORITY;
		}
	}

}

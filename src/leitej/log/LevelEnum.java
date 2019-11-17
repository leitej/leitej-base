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

package leitej.log;

/**
 * Level enumerator to be used by {@link leitej.log.Logger Logger}.
 *
 * @author Julio Leite
 * @see leitej.log.Logger
 */
public enum LevelEnum {
	NONE, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL;
}

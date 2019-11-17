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

/**
 * Objects implements this interface to be used by
 * {@link leitej.locale.message.Messages Messages}.
 *
 * Objectivo futuro: carregar automaticamente todas as classes que implementem
 * esta ;)
 *
 * @author Julio Leite
 * @see leitej.locale.message.Messages
 */
public abstract interface MessagesItf {
	public abstract String[] getMessages();
}

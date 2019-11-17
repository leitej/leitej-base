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

package leitej.net.ftp;

/**
 *
 * @author Julio Leite
 */
enum ConnectionTypeEnum {

	// The most common types are ASCII and Binary
	// Other Types may or may not be supported by your server

	ASCII, // A - ASCII
	BINARY, // I - Image(Binary)
	L8, // L <byte size> - Local byte Byte size
	EBCDIC; // E - EBCDIC (no longer widely used, but may exist on some older systems)
}

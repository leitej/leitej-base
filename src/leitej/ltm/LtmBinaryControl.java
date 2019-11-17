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

package leitej.ltm;

import leitej.ltm.annotation.LongTermMemory;

/**
 *
 * @author Julio Leite
 */
@LongTermMemory(name = "__LTM_BINARY_CONTORL__")
abstract interface LtmBinaryControl extends LtmObjectModelling {

	abstract LtmBinary getBinaryStream();

	abstract void setBinaryStream(LtmBinary binaryStream);

}

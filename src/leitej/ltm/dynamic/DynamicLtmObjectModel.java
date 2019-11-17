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

package leitej.ltm.dynamic;

import java.util.Set;

import leitej.ltm.LtmBinary;
import leitej.ltm.LtmObjectModelling;
import leitej.ltm.annotation.LongTermMemory;

/**
 *
 * @author Julio Leite
 */
@LongTermMemory
public abstract interface DynamicLtmObjectModel extends LtmObjectModelling {

	public abstract Data get();

	public abstract void put(Data data);

	public abstract <T extends LtmObjectModelling> T getLink(String field);

	public abstract <T extends LtmObjectModelling> void putLink(String field, T dlom);

	public abstract <T extends LtmObjectModelling> Set<T> getLinkSet(String field);

	public abstract LtmBinary getBinaryStream(String field);

}

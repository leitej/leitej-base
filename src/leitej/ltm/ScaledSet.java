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

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import leitej.log.Logger;

/**
 * @author Julio Leite
 *
 */
public final class ScaledSet<T> extends AbstractSet<T> implements Set<T>, Serializable {

	private static final long serialVersionUID = 5098023082686280992L;

	private static final Logger LOG = Logger.getInstance();

	private static final LongTermMemory DATA_PROXY = LongTermMemory.getInstance();

	static final <T extends LtmObjectModelling> void delete(final ScaledSet<T> set) {
		// TODO when deleting remember to delete also cache on DATA_PROXY

	}

	// FIXME override more methods to do not force iterate all elements
	// from AbstractSet and AbstractCollection

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}
}

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

package leitej.util.fs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Julio Leite
 *
 */
public abstract class AbstractQualified<E extends AbstractEntity> {
	// TODO:

	private final List<E> entityList = new ArrayList<>();

	protected AbstractQualified(final Collection<E> c) {
		this.entityList.addAll(c);
	}

	synchronized final boolean has(final E entity) {
		return this.entityList.contains(entity);
	}

	protected abstract boolean persistAdd(final E entity);

	synchronized final boolean add(final E entity) {
		return persistAdd(entity) && this.entityList.add(entity);
	}

	protected abstract boolean persistRemove(final E entity);

	synchronized final boolean remove(final E entity) {
		return persistRemove(entity) && this.entityList.remove(entity);
	}

}

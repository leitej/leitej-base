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

package leitej.util.data;

import java.lang.ref.WeakReference;

/**
 * This is multi-thread protected
 *
 * @author Julio Leite
 */
public final class CacheWeak<K, V> extends AbstractCache<K, V, WeakReference<V>> {

	private static final long serialVersionUID = 83296759332470214L;

	public CacheWeak(final int initialCapacity, final float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public CacheWeak(final int initialCapacity) {
		super(initialCapacity);
	}

	public CacheWeak() {
		super();
	}

	@Override
	protected WeakReference<V> newReference(final V referent) {
		return new WeakReference<>(referent);
	}

}

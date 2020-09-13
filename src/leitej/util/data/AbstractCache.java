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

package leitej.util.data;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import leitej.exception.AgnosticThreadLtException;
import leitej.exception.ImplementationLtRtException;
import leitej.thread.XAgnosticThread;
import leitej.thread.XThreadData;
import leitej.util.AgnosticUtil;

/**
 * This is multi-thread protected
 *
 * @author Julio Leite
 */
abstract class AbstractCache<K, V, R extends Reference<V>> implements Cache<K, V> {

	private static final long serialVersionUID = -2521342152261153474L;
	private static final List<WeakReference<AbstractCache<?, ?, ?>>> AUTO_REINDEX_LIST = new ArrayList<>();
	private static final XAgnosticThread AUTO_REINDEX_EXEC = new XAgnosticThread("cache_reindex_auto", true);
	static {
		AUTO_REINDEX_EXEC.setDaemon(true);
		AUTO_REINDEX_EXEC.start();
		try {
			AUTO_REINDEX_EXEC.workOn(new XThreadData(
					new Invoke(AbstractCache.class, AgnosticUtil.getMethod(AbstractCache.class, "autoReIndex")),
					new TimeTriggerImpl(DateFieldEnum.HOUR_OF_DAY, 1)));
		} catch (AgnosticThreadLtException | NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	public static final void autoReIndex() {
		WeakReference<AbstractCache<?, ?, ?>> weakReference;
		AbstractCache<?, ?, ?> tmp;
		for (final Iterator<WeakReference<AbstractCache<?, ?, ?>>> iterator = AUTO_REINDEX_LIST.iterator(); iterator
				.hasNext();) {
			weakReference = iterator.next();
			tmp = weakReference.get();
			if (tmp == null) {
				AUTO_REINDEX_LIST.remove(weakReference);
			} else {
				tmp.reindex();
			}
		}
	}

	private static void activaAutoReIndex(final AbstractCache<?, ?, ?> cache) {
		AUTO_REINDEX_LIST.add(new WeakReference<AbstractCache<?, ?, ?>>(cache));
	}

	private final Map<K, R> cache;

	protected AbstractCache(final int initialCapacity, final float loadFactor) {
		this.cache = new HashMap<>(initialCapacity, loadFactor);
		activaAutoReIndex(this);
	}

	protected AbstractCache(final int initialCapacity) {
		this.cache = new HashMap<>(initialCapacity);
		activaAutoReIndex(this);
	}

	protected AbstractCache() {
		this.cache = new HashMap<>();
		activaAutoReIndex(this);
	}

	@Override
	public synchronized final V get(final K key) {
		final R result = this.cache.get(key);
		return (result == null) ? null : result.get();
	}

	protected abstract R newReference(V referent);

	@Override
	public synchronized final V set(final K key, final V value) {
		if (key == null) {
			throw new IllegalArgumentException();
		}
		final R result;
		if (value == null) {
			result = this.cache.remove(key);
		} else {
			final R newReference = newReference(value);
			result = this.cache.put(key, newReference);
		}
		return (result == null) ? null : result.get();
	}

	@Override
	public final V remove(final K key) {
		return set(key, null);
	}

	@Override
	public final void setAll(final Map<K, V> m) {
		if (m == null) {
			throw new IllegalArgumentException();
		}
		Entry<K, V> tmp;
		for (final Iterator<Entry<K, V>> iterator = m.entrySet().iterator(); iterator.hasNext();) {
			tmp = iterator.next();
			set(tmp.getKey(), tmp.getValue());
		}
	}

	@Override
	public synchronized final void clear() {
		this.cache.clear();
	}

	@Override
	public synchronized final int reindex() {
		int result = 0;
		Entry<K, R> tmp;
		for (final Iterator<Entry<K, R>> iterator = this.cache.entrySet().iterator(); iterator.hasNext();) {
			tmp = iterator.next();
			if (tmp.getValue().get() == null) {
				iterator.remove();
				result++;
			}
		}
		return result;
	}

}

/*******************************************************************************
 * Copyright (C) 2018 Julio Leite
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

import java.io.Serializable;
import java.util.Map;

/**
 * Useful interface to defines expected interaction with a cache object.
 *
 * @author julio
 *
 */
public abstract interface Cache<K, V> extends Serializable {

	/**
	 * Get object mapped to a key.
	 *
	 * @param key
	 * @return cached object, if null missed in cache
	 */
	public abstract V get(K key);

	/**
	 * Saves an object to cache, mapping to the key. When value equal to null, will
	 * remove indexed key from cache
	 *
	 * @param key
	 * @param value
	 * @return cached object if any otherwise null
	 */
	public abstract V set(K key, V value);

	/**
	 * Removes the key value pair from cache.
	 *
	 * @param key
	 * @return cached object if any otherwise null
	 */
	public abstract V remove(K key);

	/**
	 * Saves all key value pairs in argument to the cache.
	 *
	 * @param m
	 */
	public abstract void setAll(Map<K, V> m);

	/**
	 * Removes every key and values from cache.
	 */
	public abstract void clear();

	/**
	 * Optimize mapping from key to value.
	 *
	 * @return number of removed entries
	 */
	public abstract int reindex();

}

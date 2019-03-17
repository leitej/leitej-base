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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Julio Leite
 */
public final class Bag<E> implements Collection<E>, Serializable {

	private static final long serialVersionUID = 406142976396299108L;

	public static enum PeakStrategy {
		sequence
	}

	private static final PeakStrategy DEFAULT_ATRATEGY = PeakStrategy.sequence;

	private final PeakStrategy strategy;
	private final List<E> bag;
	private int pointer = 0;

	public Bag() {
		this(DEFAULT_ATRATEGY);
	}

	public Bag(final PeakStrategy strategy) {
		this.bag = new ArrayList<>();
		this.strategy = strategy;
	}

	public Bag(final int initialCapacity) {
		this(DEFAULT_ATRATEGY, initialCapacity);
	}

	public Bag(final PeakStrategy strategy, final int initialCapacity) {
		this.bag = new ArrayList<>(initialCapacity);
		this.strategy = strategy;
	}

	@Override
	public synchronized boolean add(final E e) {
		return this.bag.add(e);
	}

	@Override
	public synchronized boolean remove(final Object e) {
		return this.bag.remove(e);
	}

	@Override
	public synchronized void clear() {
		this.bag.clear();
	}

	@Override
	public synchronized int size() {
		return this.bag.size();
	}

	@Override
	public synchronized boolean isEmpty() {
		return this.bag.isEmpty();
	}

	@Override
	public synchronized boolean contains(final Object o) {
		return this.bag.contains(o);
	}

	@Override
	public synchronized Iterator<E> iterator() {
		return this.bag.iterator();
	}

	@Override
	public synchronized Object[] toArray() {
		return this.bag.toArray();
	}

	@Override
	public synchronized <T> T[] toArray(final T[] a) {
		return this.bag.toArray(a);
	}

	@Override
	public synchronized boolean containsAll(final Collection<?> c) {
		return this.bag.containsAll(c);
	}

	@Override
	public synchronized boolean addAll(final Collection<? extends E> c) {
		return this.bag.addAll(c);
	}

	@Override
	public synchronized boolean removeAll(final Collection<?> c) {
		return this.bag.removeAll(c);
	}

	@Override
	public synchronized boolean retainAll(final Collection<?> c) {
		return this.bag.retainAll(c);
	}

	public synchronized E takeOne() throws IllegalStateException {
		E result;
		switch (this.strategy) {
		case sequence:
			if (this.pointer >= this.bag.size()) {
				this.pointer = 0;
			}
			try {
				result = this.bag.get(this.pointer++);
			} catch (final IndexOutOfBoundsException e) {
				throw new IllegalStateException(e);
			}
			break;
		default:
			throw new IllegalStateException();
		}
		return result;
	}

}

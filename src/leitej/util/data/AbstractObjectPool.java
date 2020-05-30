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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import leitej.exception.ClosedLtRtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.ObjectPoolLtException;

/**
 * Useful abstract class to extend when implementing pools.
 *
 * @author Julio Leite
 */
public abstract class AbstractObjectPool<E> implements Serializable {

	private static final long serialVersionUID = -1943465152813039473L;

	private final int maxObjects;
	private final List<E> objectList;
	private final QueueBlockingFIFO<E> queue;
	private volatile boolean closed = false;

	/**
	 *
	 * @param maxObjects is the maximum capacity of the pool
	 * @throws IllegalArgumentException if maxObjects is less than 1
	 */
	protected AbstractObjectPool(final int maxObjects) throws IllegalArgumentException {
		this.maxObjects = maxObjects;
		this.queue = new QueueBlockingFIFO<>(this.maxObjects);
		this.objectList = Collections.synchronizedList(new ArrayList<E>(this.maxObjects));
	}

	/**
	 * Instantiates new object to the pool.
	 *
	 * @return instance
	 * @throws ObjectPoolLtException if is there any reason to abort the
	 *                               instantiation
	 */
	protected abstract E newObject() throws ObjectPoolLtException;

	/**
	 * Assert if the <code>obj</code> in parameter is inactive.
	 *
	 * @param obj to assert
	 * @return assertion
	 */
	protected abstract boolean isInactive(E obj);

	/**
	 * Deactivate the <code>obj</code>.
	 *
	 * @param obj to deactivate
	 * @throws ObjectPoolLtException if is there any reason to abort the deactivate
	 *                               of an element
	 */
	protected abstract void deactivate(E obj) throws ObjectPoolLtException;

	/**
	 * Polls element.
	 *
	 * @return polled or new element
	 * @throws ClosedLtRtException   if pool already closed
	 * @throws ObjectPoolLtException if is there any reason to abort the
	 *                               instantiation of new element
	 * @throws InterruptedException  if interrupted while waiting
	 */
	protected E poll() throws ClosedLtRtException, ObjectPoolLtException, InterruptedException {
		E result = null;
		while (!this.closed && result == null) {
			if (this.queue.isEmpty()) {
				offerNewObject();
			}
			result = this.queue.poll();
			if (isInactive(result)) {
				this.objectList.remove(result);
				result = null;
			}
		}
		if (this.closed) {
			throw new ClosedLtRtException();
		}
		return result;
	}

	/**
	 *
	 * @throws ObjectPoolLtException if is there any reason to abort the
	 *                               instantiation of new element
	 * @throws ClosedLtRtException   if pool already closed
	 * @throws InterruptedException  if interrupted while waiting
	 */
	private void offerNewObject() throws ObjectPoolLtException, ClosedLtRtException, InterruptedException {
		synchronized (this.objectList) {
			if (this.maxObjects > this.objectList.size()) {
				final E tmp = newObject();
				this.objectList.add(tmp);
				try {
					offer(tmp);
				} catch (final InterruptedException e) {
					deactivate(tmp);
					this.objectList.remove(tmp);
					throw e;
				} catch (final IllegalArgumentException e) {
					throw new ImplementationLtRtException(e);
				}
			}
		}
	}

	/**
	 * Offers element to the pool.
	 *
	 * @param obj to offer
	 * @throws IllegalArgumentException if the element offered is not registered
	 * @throws InterruptedException     if interrupted while waiting
	 */
	protected void offer(final E obj) throws InterruptedException, IllegalArgumentException {
		if (!this.objectList.contains(obj)) {
			throw new IllegalArgumentException();
		}
		if (!this.closed) {
			try {
				this.queue.offer(obj);
			} catch (final ClassCastException e) {
				throw new IllegalStateLtRtException(e);
			} catch (final NullPointerException e) {
				throw new IllegalStateLtRtException(e);
			} catch (final IllegalArgumentException e) {
				throw new IllegalStateLtRtException(e);
			} catch (final ClosedLtRtException e) {
				/* ignored */
			}
		}
	}

	/**
	 * Offers an inactive element to the pool.<br/>
	 * Will unregister the element from the pool. Its useful if the element became
	 * inactive while out of the pool.
	 *
	 * @param obj inactive to offer
	 */
	protected void offerInactive(final E obj) {
		this.objectList.remove(obj);
	}

	/**
	 * Closes the pool and deactivate all registered element.
	 *
	 * @throws ObjectPoolLtException if is there any reason to abort the deactivate
	 *                               of an element
	 */
	protected synchronized void close() throws ObjectPoolLtException {
		if (!this.closed) {
			synchronized (this.objectList) {
				for (final E obj : this.objectList) {
					deactivate(obj);
				}
				this.objectList.clear();
			}
			this.queue.close();
			this.closed = true;
		}
	}

}

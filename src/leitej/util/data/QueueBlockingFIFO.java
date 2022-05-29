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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import leitej.exception.ClosedLtRtException;

/**
 * An useful {@link java.util.concurrent.BlockingQueue BlockingQueue} adapted to
 * block the current thread indefinitely when calling offer or pull until the
 * operations can succeed or the queue has been closed.<br/>
 * The queue accesses for threads blocked on insertion or removal, are processed
 * in FIFO order.
 *
 * @author Julio Leite
 */
public final class QueueBlockingFIFO<E> implements Serializable {

	private static final long serialVersionUID = -5153742418415443929L;

	private static final long TIMEOUT = 2;
	private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

	private BlockingQueue<E> queue = null;
	private volatile boolean closed = false;

	/**
	 * Creates a new instance of FIFOBlockingQueue.
	 *
	 * @param capacity the capacity of this queue
	 * @throws IllegalArgumentException If capacity is less than 1
	 */
	public QueueBlockingFIFO(final int capacity) throws IllegalArgumentException {
		super();
		this.queue = new ArrayBlockingQueue<>(capacity, true);
	}

	/**
	 * Inserts the specified element into this queue, blocking until the necessary
	 * space become available.
	 *
	 * @param obj the element to add
	 * @throws InterruptedException     If interrupted while waiting
	 * @throws ClosedLtRtException      If the queue is closed
	 * @throws ClassCastException       If the class of the specified element
	 *                                  prevents it from being added to this queue
	 * @throws NullPointerException     If the specified element is null
	 * @throws IllegalArgumentException If some property of the specified element
	 *                                  prevents it from being added to this queue
	 */
	public void offer(final E obj) throws InterruptedException, ClassCastException, NullPointerException,
			IllegalArgumentException, ClosedLtRtException {
		if (this.closed) {
			throw new ClosedLtRtException("Already closed - can't offer!");
		}
		boolean added = this.queue.offer(obj, TIMEOUT, TIMEOUT_UNIT);
		while (!added && !this.closed) {
			added = this.queue.offer(obj, TIMEOUT, TIMEOUT_UNIT);
		}
		if (!added) {
			throw new ClosedLtRtException("Already closed - can't offer!");
		}
	}

	/**
	 * Retrieves and removes the head of this queue, blocking until an element
	 * become available.
	 *
	 * @return the head of this queue
	 * @throws InterruptedException If interrupted while waiting
	 * @throws ClosedLtRtException  If the queue is closed
	 */
	public E poll() throws InterruptedException, ClosedLtRtException {
		if (this.closed && this.queue.size() == 0) {
			throw new ClosedLtRtException("Already closed - can't offer!");
		}
		E result = this.queue.poll(TIMEOUT, TIMEOUT_UNIT);
		while (result == null && !this.closed) {
			result = this.queue.poll(TIMEOUT, TIMEOUT_UNIT);
		}
		if (result == null) {
			throw new ClosedLtRtException("Already closed - can't offer!");
		}
		return result;
	}

	/**
	 * Closes the queue.
	 */
	public synchronized void close() {
		if (!this.closed) {
			this.closed = true;
		}
	}

	/**
	 * Verifies if this queue is closed.
	 *
	 * @return boolean
	 */
	public boolean isClosed() {
		return this.closed;
	}

	/**
	 * Returns the number of elements in this collection. If this collection
	 * contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
	 *
	 * @return number of elements
	 */
	public int size() {
		return this.queue.size();
	}

	/**
	 * Verifies if this queue is empty.
	 *
	 * @return boolean
	 */
	public boolean isEmpty() {
		return this.queue.isEmpty();
	}

	/**
	 * Returns an array containing all of the elements in this queue. This method
	 * return the elements in the same order.<br/>
	 * <br/>
	 * The returned array will be "safe" in that no references to it are maintained
	 * by this queue. (In other words, this method must allocate a new array even if
	 * this collection is backed by an array). The caller is thus free to modify the
	 * returned array.<br/>
	 * <br/>
	 * This method acts as bridge between array-based and collection-based APIs
	 *
	 * @return an array containing all of the elements in this queue
	 */
	public Object[] toArray() {
		return this.queue.toArray();
	}

	/**
	 *
	 *
	 * @param a the array into which the elements of this queue are to be stored, if
	 *          it is big enough; otherwise, a new array of the same runtime type is
	 *          allocated for this purpose
	 * @return an array containing all of the elements in this queue
	 * @throws ArrayStoreException  If the runtime type of the specified array is
	 *                              not a supertype of the runtime type of every
	 *                              element in this queue
	 * @throws NullPointerException If the specified array is null
	 */
	public <T> T[] toArray(final T[] a) throws ArrayStoreException, NullPointerException {
		return this.queue.toArray(a);
	}

}

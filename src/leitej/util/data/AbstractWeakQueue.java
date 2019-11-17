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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * This is multi-thread protected
 *
 * @author Julio Leite
 */
public abstract class AbstractWeakQueue<E> implements Serializable {

	private static final long serialVersionUID = -4529816994885501524L;

	private final List<WeakReference<E>> weakList;

	public AbstractWeakQueue(final int initialCapacity) {
		this.weakList = new ArrayList<>(initialCapacity);
	}

	public AbstractWeakQueue() {
		this.weakList = new ArrayList<>();
	}

	protected abstract E newObject();

	public final synchronized boolean offer(final E e) {
		if (e == null) {
			return false;
		}
		return this.weakList.add(new WeakReference<>(e));
	}

	public final synchronized E poll() {
		E result = null;
		WeakReference<E> tmp;
		while (result == null) {
			if (this.weakList.isEmpty()) {
				result = newObject();
				if (result == null) {
					throw new IllegalStateException();
				}
			} else {
				tmp = this.weakList.remove(this.weakList.size() - 1);
				result = tmp.get();
			}
		}
		return result;
	}

	public final synchronized int size() {
		return this.weakList.size();
	}

}

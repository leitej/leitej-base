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

package leitej.net.dtp;

import java.io.IOException;
import java.net.InetAddress;

import leitej.exception.ClosedLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.util.data.AbstractObjectPool;

/**
 *
 * @author Julio Leite
 */
final class RawDataListenerBindedPool extends AbstractObjectPool<RawDataListener> {

	private static final long serialVersionUID = -6669716044907859290L;

	private final InetAddress bindAddr;

	protected RawDataListenerBindedPool(final int maxObjects, final InetAddress bindAddr)
			throws IllegalArgumentException {
		super(maxObjects);
		this.bindAddr = bindAddr;
	}

	@Override
	protected RawDataListener newObject() throws ObjectPoolLtException {
		try {
			return new RawDataListener(this, this.bindAddr);
		} catch (final IOException e) {
			throw new ObjectPoolLtException(e);
		}
	}

	@Override
	protected boolean isInactive(final RawDataListener obj) {
		return obj.isInactive();
	}

	@Override
	protected void deactivate(final RawDataListener obj) {
		obj.close();
	}

	@Override
	protected RawDataListener poll() throws ClosedLtRtException, ObjectPoolLtException, InterruptedException {
		return super.poll();
	}

	@Override
	protected void offer(final RawDataListener obj) throws InterruptedException, IllegalArgumentException {
		super.offer(obj);
	}

	@Override
	protected void offerInactive(final RawDataListener obj) {
		super.offerInactive(obj);
	}

	@Override
	protected void close() {
		super.close();
	}

}

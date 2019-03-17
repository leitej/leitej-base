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

package leitej.util.machine;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.Collection;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.log.Logger;

/**
 * An useful class to help control heap memory.
 *
 * @author Julio Leite
 */
public final class HeapMemoryUsageWarn {

	private static final Logger LOG = Logger.getInstance();

	private static final double defaultPercentageUsageThreshold = 0.8;
	private static final MemoryPoolMXBean tenuredGenPool = getTenuredGenPool();
	private static final Collection<Listener> listeners = new ArrayList<>();

	static {
		final MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		final NotificationEmitter emitter = (NotificationEmitter) mbean;
		emitter.addNotificationListener(new NotificationListener() {
			@Override
			public void handleNotification(final Notification n, final Object hb) {
				if (n.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
					HeapMemoryUsageWarn.notifyMemoryUsageLow(tenuredGenPool.getUsage().getUsed(),
							tenuredGenPool.getUsage().getMax());
				}
			}
		}, null, null);
		setPercentageUsageThreshold(defaultPercentageUsageThreshold);
	}

	private static MemoryPoolMXBean getTenuredGenPool() {
		for (final MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
			if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
				return pool;
			}
		}
		throw new ImplementationLtRtException("lt.CNFTenuredSpace");
	}

	/**
	 * Set the percentage threshold that triggers the warning.
	 *
	 * @param percentage threshold
	 */
	public static void setPercentageUsageThreshold(final double percentage) {
		if (percentage <= 0.0 || percentage > 1.0) {
			throw new IllegalArgumentLtRtException("lt.HMUWWrongPercentage", percentage);
		}
		tenuredGenPool.setUsageThreshold((long) (tenuredGenPool.getUsage().getMax() * percentage));
		LOG.debug("lt.CNFTenuredSpaceSet", percentage);
	}

	private static void notifyMemoryUsageLow(final long usedMemory, final long maxMemory) {
		LOG.debug("lt.CNFTenuredSpaceWarn", listeners.size());
		for (final Listener listener : listeners) {
			listener.memoryUsageLow(usedMemory, maxMemory);
		}
	}

	/**
	 * Add a listener to receive the warn.
	 *
	 * @param listener to add
	 * @return if has been added
	 */
	public static boolean addListener(final Listener listener) {
		return listeners.add(listener);
	}

	/**
	 * Remove a listener.
	 *
	 * @param listener to remove
	 * @return if has been removed
	 */
	public static boolean removeListener(final Listener listener) {
		return listeners.remove(listener);
	}

	private HeapMemoryUsageWarn() {
	}

	/**
	 * Interface that listener from HeapMemoryUsageWarn needs to implement.
	 */
	public abstract interface Listener {
		public abstract void memoryUsageLow(long usedMemory, long maxMemory);
	}

}

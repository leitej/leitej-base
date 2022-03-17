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

package leitej.ltm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import leitej.Constant;
import leitej.exception.SeppukuLtRtException;
import leitej.log.Logger;
import leitej.util.DateUtil;
import leitej.util.machine.VMMonitor;

/**
 * @author Julio Leite
 *
 */
final class CompactMemory {

	private static final Logger LOG = Logger.getInstance();

	private static final File TIME_FILE = new File(Constant.DEFAULT_PROPERTIES_FILE_DIR,
			CompactMemory.class.getCanonicalName() + ".time");
	private static final boolean IS_TO_COMPACT;

	static {
		final String forceCompactArg = "-LTM.ForceCompact";
		if (VMMonitor.javaArguments().contains(forceCompactArg)) {
			LOG.info("Force compact memory is active by jvm argument: #0", forceCompactArg);
			IS_TO_COMPACT = true;
		} else if (DataMemoryPool.CONFIG.getShutdownCompactMemoryEveryNDays() < 0) {
			LOG.info("Compact memory is disabled by configuration");
			IS_TO_COMPACT = false;
		} else {
			final long interval = Long.valueOf(DataMemoryPool.CONFIG.getShutdownCompactMemoryEveryNDays()) * 24 * 60
					* 60 * 1000;
			long lastCompactTime = 0;
			try {
				if (TIME_FILE.exists()) {
					ObjectInputStream ois = null;
					try {
						ois = new ObjectInputStream(new FileInputStream(TIME_FILE));
						lastCompactTime = ois.readLong();
					} finally {
						if (ois != null) {
							ois.close();
						}
					}
				} else {
					compactDone();
				}
			} catch (final IOException e) {
				throw new SeppukuLtRtException(e);
			}
			final long nextTime = lastCompactTime + interval;
			IS_TO_COMPACT = DateUtil.nowTime() > nextTime;
			if (!IS_TO_COMPACT) {
				LOG.info("To force compact memory data from #0 set jvm argument: #1",
						LongTermMemory.class.getSimpleName(), forceCompactArg);
				LOG.debug("Next scheduled after #0", DateUtil.format(new Date(nextTime), "YYYY-MM-dd"));
			}
		}
		LOG.info("Compact memory at shutdown: #0", IS_TO_COMPACT);
	}

	static boolean isToCompact() {
		return IS_TO_COMPACT;
	}

	static void compactDone() throws IOException {
		LOG.info("Compact memory done");
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(TIME_FILE));
			oos.writeLong(DateUtil.nowTime());
			oos.flush();
		} finally {
			if (oos != null) {
				oos.close();
			}
		}
	}

}

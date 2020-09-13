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

package leitej.util.machine;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * An useful class to help get VM information.
 *
 * @author Julio Leite
 * @see java.lang.management.RuntimeMXBean
 * @see java.lang.management.OperatingSystemMXBean
 * @see java.lang.management.MemoryMXBean
 * @see java.lang.management.ThreadMXBean
 */
public final class VMMonitor {

	private static final VMMonitor INSTANCE = new VMMonitor();

	private final File f = (new File((new File("leitej.txt")).getAbsolutePath())).getParentFile();
	private final OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();
	private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
	private final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
	private final ThreadMXBean thread = ManagementFactory.getThreadMXBean();

	/**
	 * Creates a new instance of VMMonitor.
	 */
	private VMMonitor() {
	}

	/**
	 * Operating system architecture
	 *
	 * @return name
	 */
	public static String osArch() {
		return INSTANCE.operatingSystem.getArch();
	}

	public static int availableProcessors() {
		return INSTANCE.operatingSystem.getAvailableProcessors();
	}

	/**
	 * Operating system name
	 *
	 * @return INSTANCE.name
	 */
	public static String osName() {
		return INSTANCE.operatingSystem.getName();
	}

	public static double systemLoadAverage() {
		return INSTANCE.operatingSystem.getSystemLoadAverage();
	}

	/**
	 * Operating system version
	 *
	 * @return INSTANCE.name
	 */
	public static String osVersion() {
		return INSTANCE.operatingSystem.getVersion();
	}

	public static String bootClassPath() {
		return INSTANCE.runtime.getBootClassPath();
	}

	public static String classPath() {
		return INSTANCE.runtime.getClassPath();
	}

	public static List<String> javaArguments() {
		return INSTANCE.runtime.getInputArguments();
	}

	public static String libraryPath() {
		return INSTANCE.runtime.getLibraryPath();
	}

	public static String managementSpecVersion() {
		return INSTANCE.runtime.getManagementSpecVersion();
	}

	public static String name() {
		return INSTANCE.runtime.getName();
	}

	public static String specName() {
		return INSTANCE.runtime.getSpecName();
	}

	public static String specVendor() {
		return INSTANCE.runtime.getSpecVendor();
	}

	public static String specVersion() {
		return INSTANCE.runtime.getSpecVersion();
	}

	public static long startTime() {
		return INSTANCE.runtime.getStartTime();
	}

	public static Map<String, String> systemProperties() {
		return INSTANCE.runtime.getSystemProperties();
	}

	public static long upTime() {
		return INSTANCE.runtime.getUptime();
	}

	public static String vmName() {
		return INSTANCE.runtime.getVmName();
	}

	public static String vmVendor() {
		return INSTANCE.runtime.getVmVendor();
	}

	public static String vmVersion() {
		return INSTANCE.runtime.getVmVersion();
	}

	public static boolean isBootClassPathSupported() {
		return INSTANCE.runtime.isBootClassPathSupported();
	}

	public static int threadCount() {
		return INSTANCE.thread.getThreadCount();
	}

	public static long currentThreadCpuTimeNano() {
		return INSTANCE.thread.getCurrentThreadCpuTime();
	}

	public static long currentThreadUserTimeNano() {
		return INSTANCE.thread.getCurrentThreadUserTime();
	}

	public static MemoryUsage heapMemoryUsage() {
		return INSTANCE.memory.getHeapMemoryUsage();
	}

	public static MemoryUsage nonHeapMemoryUsage() {
		return INSTANCE.memory.getNonHeapMemoryUsage();
	}

	public static long freeSpace() {
		return INSTANCE.f.getFreeSpace();
	}

	public static long totalSpace() {
		return INSTANCE.f.getTotalSpace();
	}

	public static long usableSpace() {
		return INSTANCE.f.getUsableSpace();
	}

	public static String locale() {
		return Locale.getDefault().toString();
	}

	public static String fileEncoding() {
		return System.getProperty("file.encoding");
	}

}

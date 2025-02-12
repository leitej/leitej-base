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

package leitej.log;

import java.util.logging.LogRecord;

import leitej.exception.ImplementationLtRtException;
import leitej.util.AgnosticUtil;
import leitej.util.DateUtil;
import leitej.util.data.Cache;
import leitej.util.data.CacheWeak;
import leitej.util.data.Invoke;
import leitej.util.data.InvokeItf;
import leitej.util.machine.ShutdownHookUtil;
import leitej.util.machine.VMMonitor;

/**
 * <p>
 * Logging messages for a specific system or application component.
 * </p>
 * <p>
 * Logger object is obtained by calling static method getInstace(). For example
 * 'private static final Logger LOG = Logger.getInstace();' in the class that
 * uses the log. Each obtained instance is to be used inside the class that
 * invoked it, for performance and output coherence.
 * </p>
 * <p>
 * The log is configured based on the properties from the logging configuration
 * file 'logger.prop.lt.xml' on the directory 'meta-inf'.
 * </p>
 * <p>
 * Logger is multi-thread safe.
 * </p>
 * <p>
 * The Logger class will put a call to close it self in the
 * {@link leitej.util.machine.ShutdownHookUtil#addToLast(InvokeItf)
 * ShutdownHookUtil.addToLast(InvokeItf)}.
 * </p>
 *
 * @author Julio Leite
 */
public final class Logger {

	private static final String METHOD_CLOSE = "close";
	private static volatile boolean CLOSED = false;

	private static final InvokeItf CLOSE_AT_JVM_SHUTDOWN;
	private static final Cache<String, Logger> ISTANCES;
	private static final Logger LOG;

	static {
		ISTANCES = new CacheWeak<>();
		LOG = new Logger(Logger.class.getCanonicalName());
		try {
			CLOSE_AT_JVM_SHUTDOWN = new Invoke(Logger.class, AgnosticUtil.getMethod(Logger.class, METHOD_CLOSE));
			ShutdownHookUtil.addToLast(CLOSE_AT_JVM_SHUTDOWN);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		}
		try {
			LOG.info("osArch: #0", VMMonitor.osArch());
			LOG.info("osName: #0", VMMonitor.osName());
			LOG.info("osVersion: #0", VMMonitor.osVersion());
			LOG.info("vmName: #0", VMMonitor.vmName());
			LOG.info("vmVendor: #0", VMMonitor.vmVendor());
			LOG.info("vmVersion: #0", VMMonitor.vmVersion());
			LOG.trace("startTime: #0", VMMonitor.startTime());
			LOG.info("time: #0", DateUtil.format(DateUtil.now(), "yyyy-MM-dd HH:mm:ss.SSS 'GMT' Z z (zzzz)"));
			LOG.info("locale: #0", VMMonitor.locale());
			LOG.info("fileEncoding: #0", VMMonitor.fileEncoding());
			LOG.trace("bootClassPath: #0", VMMonitor.bootClassPath());
			LOG.trace("classPath: #0", VMMonitor.classPath());
			LOG.info("javaArguments: #0", VMMonitor.javaArguments());
			LOG.trace("libraryPath: #0", VMMonitor.libraryPath());
			LOG.debug("systemProperties: #0", VMMonitor.systemProperties());
			LOG.debug("availableProcessors: #0", VMMonitor.availableProcessors());
			LOG.debug("threadCount: #0", VMMonitor.threadCount());
			LOG.debug("heapMemoryUsage: #0", VMMonitor.heapMemoryUsage());
			LOG.debug("nonHeapMemoryUsage: #0", VMMonitor.nonHeapMemoryUsage());
			LOG.trace("totalSpace: #0", VMMonitor.totalSpace());
			LOG.trace("freeSpace: #0", VMMonitor.freeSpace());
		} catch (final Exception e) {
			LOG.debug("when trying to show some info, received an exception: #0", e.getMessage());
		}
		try {
			// grab Java logging
			JavaLogging.grab(new Logger("java.log"), AppenderManager.JAVA_LOGGING_CONFIG);
		} catch (final Exception e) {
			LOG.warn("Ignoring Java Logging Grab - error: #0", e.getMessage());
		}
	}

	public static final Logger getInstance() {
		return getInstance((new Throwable()).getStackTrace()[1].getClassName());
	}

	public static final Logger getInstance(final Class<?> signClass) {
		return getInstance(signClass.getCanonicalName());
	}

	private static final Logger getInstance(final String signClass) {
		Logger result;
		synchronized (ISTANCES) {
			result = ISTANCES.get(signClass);
			if (result == null) {
				result = new Logger(signClass);
				ISTANCES.set(signClass, result);
			}
		}
		return result;
	}

	/**
	 * Closes the logger (appenders).<br/>
	 * Before close it sends to debug log information about the use of JVM at this
	 * point.
	 */
	public static synchronized void close() {
		if (!CLOSED) {
			removeCloseAsyncInvokeFromShutdownHook();
			LOG.debug("threadCount: #0", VMMonitor.threadCount());
			LOG.debug("heapMemoryUsage: #0", VMMonitor.heapMemoryUsage());
			LOG.debug("nonHeapMemoryUsage: #0", VMMonitor.nonHeapMemoryUsage());
			LOG.info("Logger closing");
			CLOSED = true;
			AppenderManager.close();
			ISTANCES.clear();
		}
	}

	private static void removeCloseAsyncInvokeFromShutdownHook() {
		if (CLOSE_AT_JVM_SHUTDOWN != null && !ShutdownHookUtil.isActive()) {
			ShutdownHookUtil.remove(CLOSE_AT_JVM_SHUTDOWN);
		}
	}

	private final AppenderManager appenderMng;

	/**
	 * Creates a new instance of Logger.<br/>
	 * <br/>
	 * A call to {@link leitej.log.Logger#close() close()} is put in the
	 * {@link leitej.util.machine.ShutdownHookUtil#addToLast(InvokeItf)
	 * ShutdownHookUtil.addToLast(InvokeItf)}.
	 */
	private Logger(final String signClass) {
		this.appenderMng = new AppenderManager(signClass);
	}

	/**
	 * Adds log at error level.<br/>
	 * If any object in args argument has an instance of exception this method will
	 * also send a stack trace of this.
	 *
	 * @param msg  text of the message to log
	 * @param args objects to compose the text message
	 */
	public void error(final String msg, final Object... args) {
		append(LevelEnum.ERROR, msg, args);
	}

	/**
	 * Adds log at warn level.
	 *
	 * @param msg  text of the message to log
	 * @param args objects to compose the text message
	 */
	public void warn(final String msg, final Object... args) {
		append(LevelEnum.WARN, msg, args);
	}

	/**
	 * Adds log at info level.
	 *
	 * @param msg  text of the message to log
	 * @param args objects to compose the text message
	 */
	public void info(final String msg, final Object... args) {
		append(LevelEnum.INFO, msg, args);
	}

	/**
	 * Adds log at debug level.
	 *
	 * @param msg  text of the message to log
	 * @param args objects to compose the text message
	 */
	public void debug(final String msg, final Object... args) {
		append(LevelEnum.DEBUG, msg, args);
	}

	/**
	 * Adds log at trace level.<br/>
	 * If any object in args argument has an instance of exception this method will
	 * also send a stack trace of this.
	 *
	 * @param msg  text of the message to log
	 * @param args objects to compose the text message
	 */
	public void trace(final String msg, final Object... args) {
		append(LevelEnum.TRACE, msg, args);
	}

	/**
	 *
	 */
	private void append(final LevelEnum level, final String msg, final Object... args) {
		if (!CLOSED) {
			this.appenderMng.print(null, level, Thread.currentThread().getName(), msg, args);
		} else {
			System.err.println("LOG_CLOSED - " + level + " - " + Thread.currentThread().getName() + " - " + msg);
		}
	}

	void appendJavaLogging(final LogRecord record) {
		if (!CLOSED) {
			this.appenderMng.print(record, LevelEnum.fromJavaLoggingLevel(record.getLevel().intValue()),
					"Thread-" + record.getThreadID(), record.getMessage());
		} else {
			System.err.println("LOG_CLOSED - " + LevelEnum.fromJavaLoggingLevel(record.getLevel().intValue())
					+ " - thread-" + record.getThreadID() + " - " + record.getMessage());
		}
	}

}

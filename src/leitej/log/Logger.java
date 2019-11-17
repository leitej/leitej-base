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

import java.util.Locale;

import leitej.exception.ImplementationLtRtException;
import leitej.exception.SeppukuLtRtException;
import leitej.util.AgnosticUtil;
import leitej.util.DateUtil;
import leitej.util.data.Cache;
import leitej.util.data.CacheWeak;
import leitej.util.data.Invoke;
import leitej.util.data.InvokeItf;
import leitej.util.machine.ShutdownHookUtil;
import leitej.util.machine.VMMonitor;
import leitej.util.stream.FileUtil;

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

	private static volatile boolean CLOSED = false;

	static LevelEnum DEFAULT_LOG_LEVEL = LevelEnum.WARN;
	static final String DEFAULT_LOG_PROPERTIES_FILE_NAME = FileUtil.propertieRelativePath4FileName("logger");
	static final String DEFAULT_LOG_SIMPLE_DATE_FORMAT = "yyMMdd.HHmm.ssSSS";

	private static final String METHOD_CLOSE = "close";
	private static final InvokeItf CLOSE_AT_JVM_SHUTDOWN;
	private static final Cache<String, Logger> ISTANCES = new CacheWeak<>();
	private static final Logger MY_LOG = new Logger(Logger.class.getCanonicalName());

	static {
		MY_LOG.info("osArch: #0", VMMonitor.osArch());
		MY_LOG.info("osName: #0", VMMonitor.osName());
		MY_LOG.info("osVersion: #0", VMMonitor.osVersion());
		MY_LOG.info("vmName: #0", VMMonitor.vmName());
		MY_LOG.info("vmVendor: #0", VMMonitor.vmVendor());
		MY_LOG.info("vmVersion: #0", VMMonitor.vmVersion());
		MY_LOG.info("time: #0", DateUtil.format(DateUtil.now(), "yyyy-MM-dd HH:mm:ss.SSS 'GMT' Z z (zzzz)"));
		MY_LOG.info("locale: #0", Locale.getDefault());
		MY_LOG.debug("javaArguments: #0", VMMonitor.javaArguments());
		MY_LOG.debug("availableProcessors: #0", VMMonitor.availableProcessors());
		MY_LOG.debug("threadCount: #0", VMMonitor.threadCount());
		MY_LOG.debug("heapMemoryUsage: #0", VMMonitor.heapMemoryUsage());
		MY_LOG.debug("nonHeapMemoryUsage: #0", VMMonitor.nonHeapMemoryUsage());
		try {
			CLOSE_AT_JVM_SHUTDOWN = new Invoke(Logger.class, AgnosticUtil.getMethod(Logger.class, METHOD_CLOSE));
			ShutdownHookUtil.addToLast(CLOSE_AT_JVM_SHUTDOWN);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
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
			MY_LOG.debug("threadCount: #0", VMMonitor.threadCount());
			MY_LOG.debug("heapMemoryUsage: #0", VMMonitor.heapMemoryUsage());
			MY_LOG.debug("nonHeapMemoryUsage: #0", VMMonitor.nonHeapMemoryUsage());
			MY_LOG.info("lt.LogClose");
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
		this.trace("lt.NewInstance");
	}

	/**
	 * Adds log at fatal level.<br/>
	 * If any object in args argument has an instance of exception this method will
	 * also send a stack trace of this.<br/>
	 * After add the log this method throws a
	 * {@link leitej.exception.SeppukuLtRtException SeppukuLtRtException} with exit
	 * code 707, null cause and the same message as to log.
	 *
	 * @param msg  text of the message to log
	 * @param args objects to compose the text message
	 * @throws SeppukuLtRtException always
	 * @see leitej.exception.SeppukuLtRtException
	 */
	public void fatal(final String msg, final Object... args) throws SeppukuLtRtException {
		append(LevelEnum.FATAL, msg, args);
		throw new SeppukuLtRtException(707, null, msg, args);
	}

	/**
	 * Adds log at fatal level.<br/>
	 * If any object in args argument has an instance of exception this method will
	 * also send a stack trace of this.<br/>
	 * After add the log this method throws a
	 * {@link leitej.exception.SeppukuLtRtException SeppukuLtRtException} with the
	 * same message to log.
	 *
	 * @param exitStatus Exit status
	 * @param cause      The nested exception
	 * @param msg        text of the message to log
	 * @param args       objects to compose the text message
	 * @throws SeppukuLtRtException always
	 * @see leitej.exception.SeppukuLtRtException
	 */
	public void fatal(final int exitStatus, final Throwable cause, final String msg, final Object... args)
			throws SeppukuLtRtException {
		append(LevelEnum.FATAL, msg, args);
		throw new SeppukuLtRtException(exitStatus, cause, msg, args);
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
			this.appenderMng.print(level, Thread.currentThread().getName(), msg, args);
		} else {
			System.err.println(level + " - " + Thread.currentThread().getName() + " - Fail print: " + msg);
		}
	}

}

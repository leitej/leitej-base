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
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import leitej.locale.message.Messages;
import leitej.util.DateUtil;
import leitej.util.StringUtil;

/**
 * An useful stopwatch class.
 *
 * @author Julio Leite
 */
public final class Stopwatch implements Serializable {

	private static final long serialVersionUID = -5680151515624212060L;

	private static final Messages MESSAGES = Messages.getInstance();

	private static final String DEFAULT_STOPWATCH_NAME = "default";
	private static final String DEFAULT_DESC = null;
	private static final String DEFAULT_STARTDESC_NAME = "start";
	private static final String DEFAULT_STOPDESC_NAME = "stop";

	private static final Map<String, Stopwatch> INSTANCE_MAP = new HashMap<>();

	private final String name;
	private final boolean nano;
	private final Vector<TimerStruct> stepTime = new Vector<>();
	private boolean stopped = false;

	/**
	 * Creates a new instance of Stopwatch.
	 *
	 * @param name of stopwatch
	 * @param nano precision
	 */
	private Stopwatch(final String name, final boolean nano) {
		this.name = name;
		this.nano = nano;
	}

	/**
	 * Call this method to get the stopwatch instance, without nano precision.
	 *
	 * @return the stopwatch instance
	 */
	public static Stopwatch getInstance() {
		return getInstance(DEFAULT_STOPWATCH_NAME, false);
	}

	/**
	 * Call this method to get the stopwatch instance, without nano precision.
	 *
	 * @param name of stopwatch
	 * @return the stopwatch instance
	 */
	public static Stopwatch getInstance(final String name) {
		return getInstance(name, false);
	}

	/**
	 * Call this method to get the stopwatch instance.
	 *
	 * @param name of stopwatch
	 * @param nano precision
	 * @return the stopwatch instance
	 */
	public static Stopwatch getInstance(final String name, final boolean nano) {
		Stopwatch result;
		synchronized (INSTANCE_MAP) {
			result = INSTANCE_MAP.get(name);
			if (result == null) {
				result = new Stopwatch(name, nano);
				INSTANCE_MAP.put(name, result);
			}
		}
		return result;
	}

	private long now() {
		if (this.nano) {
			return System.nanoTime();
		} else {
			return DateUtil.nowTime();
		}
	}

	private TimerStruct getTimeStruct(final String desc) {
		return new TimerStruct(desc, now());
	}

	private void addStepRegist(final String desc) {
		this.stepTime.add(getTimeStruct(desc));
	}

	/**
	 * Start the stopwatch.
	 */
	public void start() {
		start(DEFAULT_STARTDESC_NAME);
	}

	/**
	 * Start the stopwatch.
	 *
	 * @param desc description
	 */
	public void start(final String desc) {
		if (this.stepTime.size() != 0) {
			reset();
		}
		addStepRegist(desc);
	}

	/**
	 * Mark a step at stopwatch.
	 *
	 * @return the variation from start or last step to this step
	 */
	public Long step() {
		return step(DEFAULT_DESC);
	}

	/**
	 * Mark a step at stopwatch.
	 *
	 * @param desc description
	 * @return the variation from start or last step to this step
	 */
	public Long step(final String desc) {
		if (this.stepTime.size() == 0) {
			start();
		}
		final Long lastStep = this.stepTime.lastElement().getTime();
		addStepRegist(desc);
		final Long result = this.stepTime.lastElement().getTime() - lastStep;
		return result;
	}

	/**
	 * Stop the stopwatch.
	 *
	 * @return the variation from start to stop
	 */
	public Long stop() {
		return stop(DEFAULT_STOPDESC_NAME);
	}

	/**
	 * Stop the stopwatch.
	 *
	 * @param desc description
	 * @return the variation from start to stop
	 */
	public Long stop(final String desc) {
		if (this.stepTime.size() == 0) {
			start();
		}
		final Long firstStep = this.stepTime.firstElement().getTime();
		addStepRegist(desc);
		final Long result = this.stepTime.lastElement().getTime() - firstStep;
		this.stopped = true;
		return result;
	}

	private void reset() {
		this.stepTime.clear();
		this.stopped = false;
	}

	/**
	 * Gives null if does not started; the variation from start to now if started
	 * and not stopped; or from start to stop.
	 *
	 * @return the variation
	 */
	public Long peekTotalElapsedTime() {
		Long result = null;
		if (!this.stepTime.isEmpty()) {
			final long time = (this.stopped) ? this.stepTime.lastElement().getTime() : now();
			result = time - this.stepTime.firstElement().getTime();
		}
		return result;
	}

	/**
	 * Gives null if does not started or already stop; the variation from start or
	 * last step to now if started and not stopped.
	 *
	 * @return the variation
	 */
	public Long peekLastStepElapsedTime() {
		Long result = null;
		if (!this.stepTime.isEmpty() && !this.stopped) {
			result = now() - this.stepTime.lastElement().getTime();
		}
		return result;
	}

	/**
	 * Table with all changes made to stopwatch.
	 *
	 * @return string with multiple lines
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(MESSAGES.get("lt.SWatch"));
		sb.append(" '");
		sb.append(this.name);
		sb.append("' ");
		sb.append(MESSAGES.get("lt.SWatchTable"));
		sb.append(": ");
		if (this.stepTime.size() == 0) {
			sb.append(MESSAGES.get("lt.SWatchNone"));
		} else {
			final String unidade = ((this.nano) ? "(nanos)" : "(ms)");
			Long tr = this.stepTime.firstElement().getTime();
			for (int i = 0; i < this.stepTime.size(); i++) {
				sb.append("\n ");
				sb.append(i);
				sb.append("> ");
				if (!StringUtil.isNullOrEmpty(this.stepTime.elementAt(i).getDesc())) {
					sb.append(this.stepTime.elementAt(i).getDesc());
					sb.append(", ");
				}
				sb.append(this.stepTime.elementAt(i).getTime() - tr);
				sb.append(unidade);
				tr = this.stepTime.elementAt(i).getTime();
			}
			sb.append("\n ");
			sb.append(MESSAGES.get("lt.SWatchTotal"));
			sb.append("> ");
			sb.append(peekTotalElapsedTime());
			sb.append(unidade);
		}
		return sb.toString();
	}

	private final class TimerStruct implements Serializable {
		private static final long serialVersionUID = -131612033327589600L;

		final String desc;
		final Long time;

		public TimerStruct(String desc, final Long time) {
			if (desc == null) {
				desc = DEFAULT_DESC;
			}
			this.desc = desc;
			this.time = time;
		}

		public String getDesc() {
			return this.desc;
		}

		public Long getTime() {
			return this.time;
		}

		@Override
		public String toString() {
			String result = null;
			if (this.desc.equals(DEFAULT_DESC)) {
				result = this.time.toString();
			} else {
				result = (new StringBuilder()).append("(").append(this.desc).append(",").append(this.time.toString())
						.append(")").toString();
			}
			return result;
		}
	}

}

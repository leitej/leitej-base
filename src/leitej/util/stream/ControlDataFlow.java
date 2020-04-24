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

package leitej.util.stream;

import leitej.exception.DataOverflowLtException;
import leitej.util.DateUtil;

/**
 *
 * @author Julio Leite
 */
final class ControlDataFlow {

	// 1000ms -> 1s
	// 125ms -> 1/8s
	// >>> 3 -> /8

	private static final long ZERO_LONG = 0L;
	private static final long EIGTH_OF_SECOND = 125L;
	private static final long[] FRACTION_OF_SECOND;
	static {
		FRACTION_OF_SECOND = new long[35];
		long p = EIGTH_OF_SECOND;
		for (int i = 0; i < 35; i++) {
			FRACTION_OF_SECOND[i] = p;
			p = p << 1;
		}
	}

	private boolean controlMaxBytePerStep;
	private long maxBytePerStep;
	private long sizeStep;

	private boolean controlBytePerSecond;
	private long[] fractionOfBytePerSecond;

	private long startGapDelimiter;
	private long stopGapDelimiter;
	private long gapDataFlow;

	synchronized void setBytePerSecond(final int bytePerSecond) {
		this.controlBytePerSecond = (bytePerSecond > 0);
		if (this.controlBytePerSecond) {
			this.fractionOfBytePerSecond = new long[35];
			this.fractionOfBytePerSecond[0] = bytePerSecond >>> 3;
			this.fractionOfBytePerSecond[1] = bytePerSecond >>> 2;
			this.fractionOfBytePerSecond[2] = bytePerSecond >>> 1;
			long v = bytePerSecond;
			for (int i = 0; i < 32; i++) {
				this.fractionOfBytePerSecond[i + 3] = v;
				v = v << 1;
			}
		}
	}

	synchronized void setMaxBytePerStep(final long maxBytePerStep) {
		this.controlMaxBytePerStep = (maxBytePerStep > ZERO_LONG);
		if (this.controlMaxBytePerStep) {
			this.maxBytePerStep = maxBytePerStep;
			this.sizeStep = ZERO_LONG;
		}
	}

	ControlDataFlow(final int bytePerSecond, final long maxBytePerStep) {
		setMaxBytePerStep(maxBytePerStep);
		setBytePerSecond(bytePerSecond);
		this.startGapDelimiter = ZERO_LONG;
		this.stopGapDelimiter = ZERO_LONG;
		this.gapDataFlow = ZERO_LONG;
	}

	void changeStep() {
		this.sizeStep = ZERO_LONG;
	}

	void initTrans() throws InterruptedException {
		if (this.controlBytePerSecond) {
			if (DateUtil.nowTime() - this.stopGapDelimiter > FRACTION_OF_SECOND[3]
					|| this.gapDataFlow > Integer.MAX_VALUE) {
				spendTime();
			}
		}
	}

	void endTrans(final int bytecount) throws InterruptedException, DataOverflowLtException {
		if (bytecount < 0) {
			close();
		}
		if (this.controlMaxBytePerStep) {
			this.sizeStep += bytecount;
			if (this.sizeStep > this.maxBytePerStep) {
				throw new DataOverflowLtException("Passed the limit imposed on the volume of data");
			}
		}
		if (this.controlBytePerSecond) {
			this.gapDataFlow += bytecount;
			spendTime();
		}
	}

	private void spendTime() throws InterruptedException {
		this.stopGapDelimiter = DateUtil.nowTime();
		int control = 0;
		long tmp = this.stopGapDelimiter - this.startGapDelimiter;
		while (control < 35
				&& (this.gapDataFlow > this.fractionOfBytePerSecond[control] || tmp > FRACTION_OF_SECOND[control])) {
			control++;
		}
		if (control > 34) {
			spendExactTime();
		} else if (control > 0) {
			control--;
			this.startGapDelimiter += FRACTION_OF_SECOND[control];
			tmp = this.startGapDelimiter - this.stopGapDelimiter;
			if (tmp > 0) {
				Thread.sleep(tmp);
				this.stopGapDelimiter = this.startGapDelimiter;
				this.gapDataFlow -= this.fractionOfBytePerSecond[control];
			} else {
				if (tmp < -FRACTION_OF_SECOND[3]) {
					this.startGapDelimiter = this.stopGapDelimiter;
					this.gapDataFlow = ZERO_LONG;
				}
			}
		}
	}

	void close() throws DataOverflowLtException, InterruptedException {
		spendExactTime();
		this.controlMaxBytePerStep = false;
		this.controlBytePerSecond = false;
	}

	private void spendExactTime() throws InterruptedException {
		if (this.controlBytePerSecond) {
			final long sleep = ((this.gapDataFlow * 1000L) / this.fractionOfBytePerSecond[3])
					- (this.stopGapDelimiter - this.startGapDelimiter);
			if (sleep > 0) {
				Thread.sleep(sleep);
			}
			this.startGapDelimiter = DateUtil.nowTime();
			this.stopGapDelimiter = DateUtil.nowTime();
			this.gapDataFlow = ZERO_LONG;
		}
	}

}

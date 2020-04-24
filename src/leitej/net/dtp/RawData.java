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
/*
 *
 *
 */

package leitej.net.dtp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.log.Logger;
import leitej.net.ConstantNet;
import leitej.net.exception.DtpLtException;
import leitej.util.DateUtil;
import leitej.util.data.Cache;
import leitej.util.data.CacheWeak;
import leitej.util.stream.FractionInputStream;

/**
 *
 * @author Julio Leite
 */
public final class RawData extends InputStream implements Serializable {

	private static final long serialVersionUID = 1607307704254633948L;

	private static final Logger LOG = Logger.getInstance();

	private static final Cache<Long, List<RawData>> TMP_POINT_MAP = new CacheWeak<>();

	// TODO: integrar no factory as configs das sockect a usar na raw data. (control
	// data flow)

	private static List<RawData> myList() {
		final Long threadId = Long.valueOf(Thread.currentThread().getId());
		List<RawData> result;
		synchronized (TMP_POINT_MAP) {
			result = TMP_POINT_MAP.get(threadId);
			if (result == null) {
				result = new ArrayList<>(2);
				TMP_POINT_MAP.set(threadId, result);
			}
		}
		return result;
	}

	static void initRequest() {
		LOG.trace("initialized");
		myList().clear();
	}

	static List<RawData> endRequest() {
		final List<RawData> result = myList();
		LOG.trace("#0", result.size());
		return result;
	}

	static void initResponse() {
		LOG.trace("initialized");
		myList().clear();
	}

	static List<RawData> endResponse() {
		final List<RawData> result = myList();
		LOG.trace("#0", result.size());
		return result;
	}

	public static RawData valueOf(final String dataItf) throws IllegalArgumentLtRtException {
		LOG.debug("#0", dataItf);
		final RawData result = new RawData(dataItf);
		myList().add(result);
		return result;
	}

	public static String aliasClassName() {
		return "RawData";
	}

	private final long value;
	private final String stringValue;

	private RawDataListener rawDataListener;
	private Socket socket;
	private volatile InputStream in;

	private final List<Thread> threadsBlocked = new ArrayList<>(2);

	public RawData(final InputStream in) throws IllegalArgumentLtRtException {
		if (in == null) {
			throw new IllegalArgumentLtRtException();
		}
		this.value = DateUtil.generateUniqueNumberPerJVM();
		this.stringValue = String.valueOf(this.value);
		this.rawDataListener = null;
		this.socket = null;
		this.in = in;
	}

	private RawData(final String stringValue) throws IllegalArgumentLtRtException {
		try {
			this.value = Long.valueOf(stringValue);
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentLtRtException(e);
		}
		this.stringValue = stringValue;
		this.rawDataListener = null;
		this.socket = null;
		this.in = null;
	}

	void setInputStream(final RawDataListener rawDataListener) throws IOException {
		LOG.trace("initialized");
		if (this.in != null) {
			throw new ImplementationLtRtException();
		}
		this.rawDataListener = rawDataListener;
		this.in = new BufferedInputStream(rawDataListener.getSocket().getInputStream());
		interruptTreadsBlocked();
	}

	void setInputStream(final Socket socket) throws IOException {
		LOG.trace("initialized");
		if (this.in != null) {
			throw new ImplementationLtRtException();
		}
		this.socket = socket;
		this.in = new BufferedInputStream(socket.getInputStream());
		interruptTreadsBlocked();
	}

	private void interruptTreadsBlocked() {
		synchronized (this.threadsBlocked) {
			final Iterator<Thread> it = this.threadsBlocked.iterator();
			while (it.hasNext()) {
				try {
					it.next().interrupt();
				} catch (final SecurityException e) {
					LOG.error("#0", e);
				}
			}
			this.threadsBlocked.clear();
		}
	}

	long getId() {
		return this.value;
	}

	@Override
	public String toString() {
		LOG.debug("#0", this.stringValue);
		myList().add(this);
		return this.stringValue;
	}

	// input stream

	@Override
	public int read() throws IOException {
		waitSetIn();
		if (FractionInputStream.class.isInstance(this.in)) {
			return FractionInputStream.class.cast(this.in).readFractionReferenced();
		}
		return this.in.read();
	}

	@Override
	public int read(final byte b[]) throws IOException {
		waitSetIn();
		if (FractionInputStream.class.isInstance(this.in)) {
			return FractionInputStream.class.cast(this.in).readFractionReferenced(b);
		}
		return this.in.read(b);
	}

	@Override
	public int read(final byte b[], final int off, final int len) throws IOException {
		waitSetIn();
		if (FractionInputStream.class.isInstance(this.in)) {
			return FractionInputStream.class.cast(this.in).readFractionReferenced(b, off, len);
		}
		return this.in.read(b, off, len);
	}

	@Override
	public long skip(final long n) throws IOException {
		waitSetIn();
		return this.in.skip(n);
	}

	@Override
	public int available() throws IOException {
		waitSetIn();
		return this.in.available();
	}

	@Override
	public void close() throws IOException {
		waitSetIn();
		try {
			this.in.close();
		} finally {
			if (this.socket != null) {
				this.socket.close();
			}
			if (this.rawDataListener != null) {
				this.rawDataListener.endReceiveInputStream();
			}
		}
	}

	@Override
	public void mark(final int readlimit) {
		try {
			waitSetIn();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		this.in.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		waitSetIn();
		this.in.reset();
	}

	@Override
	public boolean markSupported() {
		try {
			waitSetIn();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return this.in.markSupported();
	}

	private void waitSetIn() throws IOException {
		if (this.in == null) {
			synchronized (this.threadsBlocked) {
				this.threadsBlocked.add(Thread.currentThread());
			}
			try {
				if (this.in == null) {
					Thread.sleep(ConstantNet.RAW_DATA_SOCKET_TIME_OUT);
				}
				if (this.in == null) {
					throw new IOException(new DtpLtException());
				}
			} catch (final InterruptedException e) {
				if (this.in == null) {
					throw new IOException(e);
				}
			} finally {
				synchronized (this.threadsBlocked) {
					this.threadsBlocked.remove(Thread.currentThread());
					Thread.interrupted();
				}
			}
		}
	}

}

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

package leitej.net.csl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;

import leitej.Constant;
import leitej.exception.ConnectionLtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.XmlInvalidLtException;
import leitej.exception.XmlomInvalidLtException;
import leitej.log.Logger;
import leitej.util.stream.ControlDataInputStream;
import leitej.util.stream.ControlDataOutputStream;
import leitej.xml.om.XmlObjectModelling;
import leitej.xml.om.Xmlom;
import leitej.xml.om.XmlomReader;
import leitej.xml.om.XmlomWriter;

/**
 * Communication Session Layer
 *
 * @author Julio Leite
 */
public abstract class AbstractCommunicationSession<F extends AbstractCommunicationFactory<F, L, H, G>, L extends AbstractCommunicationListener<F, L, H, G>, H extends AbstractCommunicationSession<F, L, H, G>, G extends AbstractCommunicationSession<F, L, H, G>> {
	// <initiate session>
	// | -- initiateBasicProtection()
	// |(><) initiateCommunication() - subClasse
	// | > 1byte to define the char set
	// | < confirmation
	// |(--) streamWrapped() - subClasse
	// |(><) initiateWrappedCommunication() - subClasse
	// | >< XMLOMs
	// <end session>

	private static final Logger LOG = Logger.getInstance();

	private static final Flush FLUSH_ELEMENT = Xmlom.newInstance(Flush.class);

	private final F factory;
	private final Socket socket;
	private final Charset charset;
	private final ControlDataInputStream cdis;
	private volatile boolean stepClosed;
	private final XmlomWriter xos;
	private final XmlomReader xis;

	/**
	 * Connects and initiates session from guest side.
	 *
	 * @param factory  with settings to apply
	 * @param endpoint the SocketAddress
	 * @param charset
	 * @throws SocketException              if there is an error in the underlying
	 *                                      protocol, such as a TCP error
	 * @throws IllegalArgumentException     if endpoint is null or is a
	 *                                      SocketAddress subclass not supported by
	 *                                      this socket
	 * @throws IllegalArgumentLtRtException if the charset is not defined
	 * @throws ConnectionLtException        <br/>
	 *                                      +Cause IOException if an error occurs
	 *                                      during the connection
	 */
	protected AbstractCommunicationSession(final F factory, final SocketAddress endpoint, final Charset charset)
			throws SocketException, IllegalArgumentException, IllegalArgumentLtRtException, ConnectionLtException {
		boolean pass = false;
		try {
			this.factory = factory;
			this.socket = new Socket();
			this.socket.setKeepAlive(false);
			if (this.factory.getConfig().getInitCommTimeOutMs() > 0) {
				this.socket.setSoTimeout(this.factory.getConfig().getInitCommTimeOutMs());
			} else {
				this.socket.setSoTimeout(0);
			}
			this.socket.connect(endpoint);
			OutputStream out = initiateBasicProtection(new BufferedOutputStream(this.socket.getOutputStream()));
			InputStream in = initiateBasicProtection(new BufferedInputStream(this.socket.getInputStream()));
			if (ControlDataInputStream.class.isInstance(in)) {
				this.cdis = ControlDataInputStream.class.cast(in);
			} else {
				this.cdis = null;
			}
			initiateCommunication(in, out);
			if (this.factory.getConfig().getTimeOutMs() > 0) {
				this.socket.setSoTimeout(this.factory.getConfig().getTimeOutMs());
			} else {
				this.socket.setSoTimeout(0);
			}
			this.charset = (charset == null) ? Constant.UTF8_CHARSET : charset;
			CharsetCode.writeCharsetCode(out, this.charset);
			out = getOutputStreamWrapped(out);
			in = getInputStreamWrapped(in);
			initiateWrappedCommunication(in, out);
			this.xos = new XmlomWriter(out, this.charset);
			this.flush();
			this.xis = new XmlomReader(in, this.charset);
			pass = true;
		} catch (final SocketException e) {
			throw e;
		} catch (final IllegalArgumentException e) {
			throw e;
		} catch (final IllegalArgumentLtRtException e) {
			throw e;
		} catch (final UnsupportedEncodingException e) {
			throw new ConnectionLtException(e);
		} catch (final IOException e) {
			throw new ConnectionLtException(e);
		} catch (final XmlomInvalidLtException e) {
			throw new ConnectionLtException(e);
		} catch (final XmlInvalidLtException e) {
			throw new ConnectionLtException(e);
		} finally {
			if (!pass) {
				try {
					close();
				} catch (final ConnectionLtException e1) {
					LOG.debug("#0", e1);
				}
			}
		}
	}

	/**
	 * initiates session from host side.
	 *
	 * @param factory with settings to apply
	 * @param socket  connected to the guest
	 * @throws SocketException       if there is an error in the underlying
	 *                               protocol, such as a TCP error
	 * @throws ConnectionLtException <br/>
	 *                               +Cause IOException if an error occurs during
	 *                               the connection <br/>
	 *                               +Cause IllegalArgumentLtRtException if the
	 *                               charset read from socket is not defined on host
	 */
	protected AbstractCommunicationSession(final F factory, final Socket socket)
			throws SocketException, ConnectionLtException {
		boolean pass = false;
		try {
			this.factory = factory;
			this.socket = socket;
			this.socket.setKeepAlive(false);
			if (this.factory.getConfig().getInitCommTimeOutMs() > 0) {
				this.socket.setSoTimeout(this.factory.getConfig().getInitCommTimeOutMs());
			} else {
				this.socket.setSoTimeout(0);
			}
			OutputStream out = initiateBasicProtection(new BufferedOutputStream(this.socket.getOutputStream()));
			InputStream in = initiateBasicProtection(new BufferedInputStream(this.socket.getInputStream()));
			if (ControlDataInputStream.class.isInstance(in)) {
				this.cdis = ControlDataInputStream.class.cast(in);
			} else {
				this.cdis = null;
			}
			initiateCommunication(in, out);
			if (this.factory.getConfig().getTimeOutMs() > 0) {
				this.socket.setSoTimeout(this.factory.getConfig().getTimeOutMs());
			} else {
				this.socket.setSoTimeout(0);
			}
			this.charset = CharsetCode.readCharsetCode(in);
			out = getOutputStreamWrapped(out);
			in = getInputStreamWrapped(in);
			initiateWrappedCommunication(in, out);
			this.xos = new XmlomWriter(out, this.charset);
			this.flush();
			this.xis = new XmlomReader(in, this.charset);
			pass = true;
		} catch (final SocketException e) {
			throw e;
		} catch (final IllegalArgumentLtRtException e) {
			throw new ConnectionLtException(e);
		} catch (final IOException e) {
			throw new ConnectionLtException(e);
		} catch (final XmlomInvalidLtException e) {
			throw new ConnectionLtException(e);
		} catch (final XmlInvalidLtException e) {
			throw new ConnectionLtException(e);
		} finally {
			if (!pass) {
				try {
					close();
				} catch (final ConnectionLtException e1) {
					LOG.debug("#0", e1);
				}
			}
		}
	}

	private final OutputStream initiateBasicProtection(OutputStream out) {
		if (this.factory.getConfig().getVelocity() > 0) {
			out = new ControlDataOutputStream(out, this.factory.getConfig().getVelocity());
		}
		return out;
	}

	private final InputStream initiateBasicProtection(InputStream in) {
		if (this.factory.getConfig().getVelocity() > 0 || this.factory.getConfig().getSizePerSentence() > 0) {
			in = new ControlDataInputStream(in, this.factory.getConfig().getVelocity(),
					this.factory.getConfig().getSizePerSentence());
		}
		return in;
	}

	/**
	 *
	 * @param in
	 * @param out
	 * @throws ConnectionLtException if any exception is raised due to the
	 *                               initialisation, that exception should be put in
	 *                               the cause of the ConnectionLtException
	 */
	protected abstract void initiateCommunication(InputStream in, OutputStream out) throws ConnectionLtException;

	/**
	 *
	 * @param in
	 * @param out
	 * @throws ConnectionLtException if any exception is raised due to the
	 *                               initialisation, that exception should be put in
	 *                               the cause of the ConnectionLtException
	 */
	protected abstract void initiateWrappedCommunication(InputStream in, OutputStream out) throws ConnectionLtException;

	/**
	 *
	 * @param out
	 * @return
	 * @throws ConnectionLtException if any exception is raised due to the wrapping
	 *                               stream, that exception should be put in the
	 *                               cause of the ConnectionLtException
	 */
	protected OutputStream getOutputStreamWrapped(final OutputStream out) throws ConnectionLtException {
		return out;
	}

	/**
	 *
	 * @param in
	 * @return
	 * @throws ConnectionLtException if any exception is raised due to the wrapping
	 *                               stream, that exception should be put in the
	 *                               cause of the ConnectionLtException
	 */
	protected InputStream getInputStreamWrapped(final InputStream in) throws ConnectionLtException {
		return in;
	}

	protected final F getFactory() {
		return this.factory;
	}

	public final <I extends XmlObjectModelling> void write(final I objs) {
		if (!this.stepClosed && this.cdis != null) {
			this.stepClosed = true;
			this.cdis.changeStep();
		}
		this.xos.write(objs);
	}

	/**
	 *
	 * @throws ConnectionLtException <br/>
	 *                               +Cause IOException If an I/O error occurs
	 */
	public final void flush() throws ConnectionLtException {
		try {
			this.xos.write(FLUSH_ELEMENT);
			this.xos.flush();
		} catch (final IOException e) {
			throw new ConnectionLtException(e);
		}
	}

	/**
	 *
	 * @param interfaceClass
	 * @return
	 * @throws ConnectionLtException <br/>
	 *                               +Cause IOException If an I/O error occurs <br/>
	 *                               +Cause XmlInvalidLtException If is reading a
	 *                               corrupted XML
	 */
	public final <I extends XmlObjectModelling> I read(final Class<I> interfaceClass) throws ConnectionLtException {
		if (this.stepClosed) {
			this.stepClosed = false;
		}
		try {
			I result;
			do {
				result = this.xis.read(interfaceClass);
			} while (Flush.class.isInstance(result));
			return result;
		} catch (final XmlInvalidLtException e) {
			throw new ConnectionLtException(e);
		} catch (final IOException e) {
			throw new ConnectionLtException(e);
		}
	}

	/**
	 * Returns the connection state of the socket.
	 *
	 * @return true if the socket successfully connected
	 */
	public final boolean isConnected() {
		return (this.socket != null && this.socket.isConnected());
	}

	public final boolean isClosed() {
		return (this.socket == null || this.socket.isClosed());
	}

	public final SocketAddress getRemoteSocketAddress() {
		return this.socket.getRemoteSocketAddress();
	}

	public final InetAddress getRemoteInetAddress() {
		return this.socket.getInetAddress();
	}

	/**
	 *
	 * @throws ConnectionLtException <br/>
	 *                               +Cause IOException if an I/O error occurs when
	 *                               closing this socket
	 */
	public final void close() throws ConnectionLtException {
		try {
			if (this.xos != null) {
				this.xos.close();
			}
			if (this.xis != null) {
				this.xis.close();
			}
		} catch (final IOException e) {
			LOG.debug("#0", e);
		} finally {
			if (this.socket != null) {
				try {
					this.socket.close();
				} catch (final IOException e) {
					throw new ConnectionLtException(e);
				}
			}
		}
	}

}

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

package leitej.net.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import leitej.Constant;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.LtException;
import leitej.locale.message.Messages;
import leitej.log.Logger;
import leitej.net.exception.FtpLtException;
import leitej.thread.PoolAgnosticThread;
import leitej.thread.XThreadData;
import leitej.util.AgnosticUtil;
import leitej.util.data.Invoke;
import leitej.util.fs.Path;

/**
 *
 * @author Julio Leite
 */
public abstract class AbstractFtpServerHandler {

	private static final Logger LOG = Logger.getInstance();
	private static final Messages MESSAGES = Messages.getInstance();

	private PoolAgnosticThread poolAThread;
	private AbstractFtpServer<?> server;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;

	private SessionData sessionData;

	protected AbstractFtpServerHandler() {
		this.sessionData = new SessionData();
	}

	final <H extends AbstractFtpServerHandler> void startAsync(final PoolAgnosticThread poolAThread,
			final AbstractFtpServer<H> server, final Socket socket) {
		try {
			this.poolAThread = poolAThread;
			this.server = server;
			this.socket = socket;
			this.sessionData.setLocalInetAddress(socket.getLocalAddress());
			this.sessionData.setLocalSocketAddress(socket.getLocalSocketAddress());
			this.sessionData.setRemoteSocketAddress(socket.getRemoteSocketAddress());
			this.reader = new BufferedReader(
					new InputStreamReader(encapsulate(socket.getInputStream()), Constant.UTF8_CHARSET_NAME));
			this.writer = new PrintWriter(
					new OutputStreamWriter(encapsulate(socket.getOutputStream()), Constant.UTF8_CHARSET_NAME), false);
			final XThreadData xtd = new XThreadData(new Invoke(this, AgnosticUtil.getMethod(this, METHOD_DEAL)),
					ConstantFtp.FTP_HANDLER_THREAD_NAME + socket.getRemoteSocketAddress());
			poolAThread.workOn(xtd);
		} catch (final SecurityException e) {
			LOG.error("#0", e);
		} catch (final NoSuchMethodException e) {
			LOG.error("#0", e);
		} catch (final LtException e) {
			LOG.error("#0", e);
		} catch (final IOException e) {
			LOG.error("#0", e);
		}
	}

	protected InputStream encapsulate(final InputStream is) {
		return is;
	}

	protected OutputStream encapsulate(final OutputStream os) {
		return os;
	}

	// Connection Establishment
	// -- 120
	// ---- 220
	// -- 220
	// -- 421
	private static final String METHOD_DEAL = "deal";

	public synchronized final void deal() {
		if (!PoolAgnosticThread.isCurrentThreadFrom(this.poolAThread)) {
			throw new IllegalStateLtRtException("lt.FTPWrongCall");
		}
		try {
			send(CodeEnum._220);
			String line;
			String[] cmdArgs = null;
			CommandEnum command = null;
			CodeEnum response;
			final StringBuilder args = new StringBuilder();
			while (this.socket != null && !this.socket.isClosed() && (line = this.reader.readLine()) != null) {
				try {
					cmdArgs = line.split("\\s", 2);
					try {
						command = Enum.valueOf(CommandEnum.class, cmdArgs[0].toUpperCase());
					} catch (final IllegalArgumentException e1) {
						command = null;
						LOG.debug("#0", line);
					}
					args.setLength(0);
					if (cmdArgs.length == 2) {
						args.append(cmdArgs[1]);
					}
					if (command != null) {
						if (CommandEnum.PASS.ordinal() == command.ordinal()) {
							LOG.debug(CommandEnum.PASS + " ********");
						} else {
							LOG.debug("#0", line);
						}
						switch (command) {
						/* ACCESS CONTROL */
						case USER:
							response = handleUSER(args);
							break;
						case PASS:
							response = handlePASS(args);
							break;
						case ACCT:
							response = handleACCT(args);
							break;
						case CWD:
							response = handleCWD(args);
							break;
						case CDUP:
							response = handleCDUP(args);
							break;
						case SMNT:
							response = handleSMNT(args);
							break;
						case REIN:
							response = handleREIN(args);
							break;
						case QUIT:
							response = handleQUIT(args);
							break;
						/* TRANSFER PARAMETERS */
						case PORT:
							response = handlePORT(args);
							break;
						case PASV:
							response = handlePASV(args);
							break;
						case TYPE:
							response = handleTYPE(args);
							break;
						case STRU:
							response = handleSTRU(args);
							break;
						case MODE:
							response = handleMODE(args);
							break;
						/* SERVICE COMMANDS */
						case RETR:
							response = handleRETR(args);
							break;
						case STOR:
							response = handleSTOR(args);
							break;
						case STOU:
							response = handleSTOU(args);
							break;
						case APPE:
							response = handleAPPE(args);
							break;
						case ALLO:
							response = handleALLO(args);
							break;
						case REST:
							response = handleREST(args);
							break;
						case RNFR:
							response = handleRNFR(args);
							break;
						case RNTO:
							response = handleRNTO(args);
							break;
						case ABOR:
							response = handleABOR(args);
							break;
						case DELE:
							response = handleDELE(args);
							break;
						case RMD:
							response = handleRMD(args);
							break;
						case MKD:
							response = handleMKD(args);
							break;
						case PWD:
							response = handlePWD(args);
							break;
						case LIST:
							response = handleLIST(args);
							break;
						case NLST:
							response = handleNLST(args);
							break;
						case SITE:
							response = handleSITE(args);
							break;
						case SYST:
							response = handleSYST(args);
							break;
						case STAT:
							response = handleSTAT(args);
							break;
						case HELP:
							response = handleHELP(args);
							break;
						case NOOP:
							response = handleNOOP(args);
							break;
						/* SERVICE COMMANDS */
						case OPTS:
							response = handleOPTS(args);
							break;
						default:
							throw new IllegalStateException();
						}
					} else {
						response = handleSpecialCMD(cmdArgs[0], args);
					}
					if (response == null) {
						send(CodeEnum._500);
					} else if (response.ordinal() == CodeEnum._221.ordinal()) {
						internalClose();
					}
				} catch (final FtpLtException e) {
					LOG.debug("#0", e);
					send(e.getCode());
				}
				this.sessionData.setLastCommandReceived(command);
				if (cmdArgs.length == 2) {
					this.sessionData.setLastArgsReceived(cmdArgs[1]);
				} else {
					this.sessionData.setLastArgsReceived(null);
				}
			}
		} catch (final SocketException e) {
			LOG.debug("#0", e);
		} catch (final Exception e) {
			LOG.error("#0", e);
		} finally {
			try {
				internalClose();
			} catch (final IOException e) {
				LOG.error("#0", e);
			} catch (final FtpLtException e) {
				LOG.error("#0", e);
			}
		}
	}

	protected CodeEnum handleSpecialCMD(final String command, final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		return null;
	}

	protected CodeEnum handleUSER(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		this.sessionData.setUserName(args.toString());
		this.sessionData.setPassword(null);
		return send(CodeEnum._331);
	}

	protected CodeEnum handlePASS(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		if (!CodeEnum._331.equals(this.sessionData.getLastCodeSent())
				|| !CommandEnum.USER.equals(this.sessionData.getLastCommandReceived())) {
			throw new FtpLtException(CodeEnum._503_luf);
		}
		final char[] password = new char[args.length()];
		args.getChars(0, args.length(), password, 0);
		this.sessionData.setPassword(password);
		this.sessionData.setAuth(auth(this.sessionData));
		return ((this.sessionData.isAuth()) ? send(CodeEnum._230) : send(CodeEnum._530));
	}

	protected abstract boolean auth(SessionData session);

	protected CodeEnum handleACCT(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		return null;
	}

	protected CodeEnum handleCWD(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		if (args.length() != 0 && args.charAt(args.length() - 1) != Path.SEPARATOR.charAt(0)) {
			args.append(Path.SEPARATOR);
		}
		final int r = changeWorkingDirectory(args);
		if (r == 1) {
			throw new FtpLtException(CodeEnum._550_ned);
		}
		if (r == 2) {
			throw new FtpLtException(CodeEnum._550_ind);
		}
		if (r != 0) {
			throw new FtpLtException(CodeEnum._550);
		}
		return send(CodeEnum._250);
	}

	/**
	 * CHANGE WORKING DIRECTORY
	 *
	 * @return 0 -> success<br/>
	 *         1 -> fail - directory does not exist<br/>
	 *         2 -> fail - is not a directory<br/>
	 *         other -> fail
	 */
	protected abstract int changeWorkingDirectory(StringBuilder args);

	protected CodeEnum handleCDUP(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		if (changeToParentDirectory() != 0) {
			throw new FtpLtException(CodeEnum._550);
		}
		return send(CodeEnum._250);
	}

	/**
	 * CHANGE TO PARENT DIRECTORY
	 *
	 * @return 0 -> success<br/>
	 *         other -> fail
	 */
	protected abstract int changeToParentDirectory();

	protected CodeEnum handleSMNT(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		return null;
	}

	protected CodeEnum handleREIN(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		if (this.sessionData.isAuth()) {
			this.sessionData.setPassword(null);
			this.sessionData = new SessionData();
		}
		reset();
		return send(CodeEnum._220);
	}

	protected abstract void reset();

	protected CodeEnum handleQUIT(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		if (this.sessionData.isAuth()) {
			this.sessionData.setPassword(null);
			this.sessionData = new SessionData();
		}
		return send(CodeEnum._221);
	}

	protected CodeEnum handlePORT(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		final StringTokenizer tmp = new StringTokenizer(args.toString(), ",");
		try {
			final String h1 = tmp.nextToken();
			final String h2 = tmp.nextToken();
			final String h3 = tmp.nextToken();
			final String h4 = tmp.nextToken();
			final int p1 = Integer.parseInt(tmp.nextToken());
			final int p2 = Integer.parseInt(tmp.nextToken());
			final String dataHost = h1 + "." + h2 + "." + h3 + "." + h4;
			final int dataPort = (p1 << 8) | p2;
			this.sessionData.setClientDtpIpPort(dataHost, dataPort);
		} catch (final NumberFormatException e) {
			throw new FtpLtException(CodeEnum._501, e);
		}
		return send(CodeEnum._200);
	}

	protected CodeEnum handlePASV(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		final String pasv = this.sessionData.setPassiv();
		if (pasv == null) {
			return send(CodeEnum._425);
		} else {
			return send(CodeEnum._227, (Object) pasv);
		}
	}

	protected CodeEnum handleTYPE(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		if (args.length() != 1) {
			throw new FtpLtException(CodeEnum._501);
		}
		switch (args.charAt(0)) {
		case 'i':
		case 'I':
			this.sessionData.setConnectionType(ConnectionTypeEnum.BINARY);
			break;
		case 'a':
		case 'A':
			this.sessionData.setConnectionType(ConnectionTypeEnum.ASCII);
			break;
		default:
			throw new FtpLtException(CodeEnum._501);
		}
		return send(CodeEnum._200);
	}

	protected CodeEnum handleSTRU(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		if (args.length() != 1) {
			throw new FtpLtException(CodeEnum._501);
		}
		switch (args.charAt(0)) {
		case 'f':
		case 'F':
			break;
		default:
			throw new FtpLtException(CodeEnum._501);
		}
		return send(CodeEnum._200);
	}

	protected CodeEnum handleMODE(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		if (args.length() != 1) {
			throw new FtpLtException(CodeEnum._501);
		}
		switch (args.charAt(0)) {
		case 's':
		case 'S':
			break;
		default:
			throw new FtpLtException(CodeEnum._501);
		}
		return send(CodeEnum._200);
	}

	protected CodeEnum handleRETR(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		Socket dataSocket = null;
		int isFile = isFile(args);
		if (isFile == 1) {
			throw new FtpLtException(CodeEnum._550_npf);
		}
		if (isFile == 2) {
			throw new FtpLtException(CodeEnum._550_nef);
		}
		if (isFile != 0) {
			throw new FtpLtException(CodeEnum._550);
		}
		send(CodeEnum._150);
		try {
			Long offset = null;
			if (CodeEnum._350.equals(this.sessionData.getLastCodeSent())
					&& CommandEnum.REST.equals(this.sessionData.getLastCommandReceived())) {
				try {
					offset = Long.valueOf(this.sessionData.getLastArgsReceived());
				} catch (final NumberFormatException e) {
					LOG.error("#0", e);
					throw new FtpLtException(CodeEnum._451);
				}
			}
			dataSocket = getDataSocketClient();
			final BufferedOutputStream out = new BufferedOutputStream(dataSocket.getOutputStream());
			isFile = sendBinaryFile(out, args, offset);
			if (isFile == 1) {
				throw new FtpLtException(CodeEnum._550_npf);
			}
			if (isFile == 2) {
				throw new FtpLtException(CodeEnum._550_nef);
			}
			if (isFile != 0) {
				throw new FtpLtException(CodeEnum._550);
			}
			out.flush();
			send(CodeEnum._226);
			out.close();
		} catch (final IOException e) {
			throw new FtpLtException(CodeEnum._425);
		} finally {
			try {
				if (dataSocket != null) {
					dataSocket.close();
				}
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
		}
		return CodeEnum._226;
	}

	/**
	 * IS FILE
	 *
	 * @return 0 -> success<br/>
	 *         1 -> fail - not a plain file<br/>
	 *         2 -> fail - nonexistent file<br/>
	 *         other -> fail
	 */
	protected abstract int isFile(StringBuilder args);

	/**
	 * SEND BINARY FILE<br/>
	 * Does not close argument <code>out</code>.
	 *
	 * @return 0 -> success<br/>
	 *         1 -> fail - not a plain file<br/>
	 *         2 -> fail - nonexistent file<br/>
	 *         other -> fail
	 */
	protected abstract int sendBinaryFile(BufferedOutputStream out, StringBuilder args, Long offset);

	protected CodeEnum handleSTOR(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		Socket dataSocket = null;
		final int isDirectory = isDirectory(args);
		if (isDirectory == 0) {
			throw new FtpLtException(CodeEnum._550);
		}
		send(CodeEnum._150);
		try {
			Long offset = null;
			if (CodeEnum._350.equals(this.sessionData.getLastCodeSent())
					&& CommandEnum.REST.equals(this.sessionData.getLastCommandReceived())) {
				try {
					offset = Long.valueOf(this.sessionData.getLastArgsReceived());
				} catch (final NumberFormatException e) {
					LOG.error("#0", e);
					throw new FtpLtException(CodeEnum._451);
				}
			}
			dataSocket = getDataSocketClient();
			final BufferedInputStream in = new BufferedInputStream(dataSocket.getInputStream());
			if (receiveBinaryFile(in, args, offset) != 0) {
				throw new FtpLtException(CodeEnum._550);
			}
			send(CodeEnum._226);
		} catch (final IOException e) {
			throw new FtpLtException(CodeEnum._550_cwf);
		} finally {
			try {
				if (dataSocket != null) {
					dataSocket.close();
				}
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
		}
		return CodeEnum._226;
	}

	/**
	 * IS DIRECTORY
	 *
	 * @return 0 -> success<br/>
	 *         1 -> fail - not a directory<br/>
	 *         2 -> fail - nonexistent directory<br/>
	 *         other -> fail
	 */
	protected abstract int isDirectory(StringBuilder args);

	/**
	 * RECEIVE BINARY FILE<br/>
	 * Closes argument <code>in</code> at the end of execution.
	 *
	 * @return 0 -> success<br/>
	 *         other -> fail
	 */
	protected abstract int receiveBinaryFile(BufferedInputStream in, StringBuilder args, Long offset);

	protected CodeEnum handleSTOU(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		// TODO:
		return null;
	}

	protected CodeEnum handleAPPE(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		// TODO:
		return null;
	}

	protected CodeEnum handleALLO(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		// TODO:
		return null;
	}

	protected CodeEnum handleREST(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		return send(CodeEnum._350);
	}

	protected CodeEnum handleRNFR(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		return send(CodeEnum._350);
	}

	protected CodeEnum handleRNTO(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		if (!CodeEnum._350.equals(this.sessionData.getLastCodeSent())
				|| !CommandEnum.RNFR.equals(this.sessionData.getLastCommandReceived())) {
			throw new FtpLtException(CodeEnum._503_luf);
		}
		final int r = rename(this.sessionData.getLastArgsReceived(), args);
		if (r == 1) {
			throw new FtpLtException(CodeEnum._550_nef);
		}
		if (r != 0) {
			throw new FtpLtException(CodeEnum._550);
		}
		return send(CodeEnum._250);
	}

	/**
	 * RENAME FILE
	 *
	 * @return 0 -> success<br/>
	 *         1 -> fail - nonexistent file<br/>
	 *         other -> fail
	 */
	protected abstract int rename(String from, StringBuilder to);

	protected CodeEnum handleABOR(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		// TODO:
		return null;
	}

	protected CodeEnum handleDELE(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		final int r = delete(args);
		if (r == 1) {
			throw new FtpLtException(CodeEnum._550_nef);
		}
		if (r == 2) {
			throw new FtpLtException(CodeEnum._550_cndf);
		}
		if (r != 0) {
			throw new FtpLtException(CodeEnum._550);
		}
		return send(CodeEnum._250);
	}

	/**
	 * DELETE
	 *
	 * @return 0 -> success<br/>
	 *         1 -> fail - file does not exist<br/>
	 *         2 -> fail - could not delete the file<br/>
	 *         other -> fail
	 */
	protected abstract int delete(StringBuilder args);

	protected CodeEnum handleRMD(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		if (args.length() != 0 && args.charAt(args.length() - 1) != Path.SEPARATOR.charAt(0)) {
			args.append(Path.SEPARATOR);
		}
		final int r = removeDirectory(args);
		if (r == 1) {
			throw new FtpLtException(CodeEnum._550_ned);
		}
		if (r == 2) {
			throw new FtpLtException(CodeEnum._550_ind);
		}
		if (r == 3) {
			throw new FtpLtException(CodeEnum._550_cnrd);
		}
		if (r != 0) {
			throw new FtpLtException(CodeEnum._550);
		}
		return send(CodeEnum._250);
	}

	/**
	 * REMOVE DIRECTORY
	 *
	 * @return 0 -> success<br/>
	 *         1 -> fail - directory does not exist<br/>
	 *         2 -> fail - is not a directory<br/>
	 *         3 -> fail - could not remove the directory<br/>
	 *         other -> fail
	 */
	protected abstract int removeDirectory(StringBuilder args);

	protected CodeEnum handleMKD(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		if (args.length() != 0 && args.charAt(args.length() - 1) != Path.SEPARATOR.charAt(0)) {
			args.append(Path.SEPARATOR);
		}
		final int r = makeDirectory(args);
		if (r == 1) {
			throw new FtpLtException(CodeEnum._550_fe);
		}
		if (r == 2) {
			throw new FtpLtException(CodeEnum._550_dcnc);
		}
		if (r != 0) {
			throw new FtpLtException(CodeEnum._550);
		}
		return send(CodeEnum._257);
	}

	/**
	 * MAKE DIRECTORY
	 *
	 * @return 0 -> success<br/>
	 *         1 -> fail - file exist<br/>
	 *         2 -> fail - directory could not be created<br/>
	 *         other -> fail
	 */
	protected abstract int makeDirectory(StringBuilder args);

	protected CodeEnum handlePWD(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		return send(CodeEnum._257, "\"" + pwd() + "\"");
	}

	protected abstract String pwd();

	protected CodeEnum handleLIST(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		Socket dataSocket = null;
		if (args.length() != 0 && args.charAt(0) == '-') {
			LOG.debug("'#0' -> ''", args);
			args.setLength(0);
		}
		if (args.length() != 0 && args.charAt(args.length() - 1) != Path.SEPARATOR.charAt(0)) {
			args.append(Path.SEPARATOR);
		}
		Object[] fileList = getList(args);
		if (fileList == null && args.length() != 0) {
			fileList = getList(new StringBuilder());
		}
		if (fileList == null) {
			throw new FtpLtException(CodeEnum._550_ned);
		}
		send(CodeEnum._150);
		try {
			dataSocket = getDataSocketClient();
			final PrintWriter out = new PrintWriter(new OutputStreamWriter(dataSocket.getOutputStream(), "US-ASCII"),
					true);
			writeFileList(out, fileList);
			out.flush();
			send(CodeEnum._226);
			out.close();
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		} catch (final IOException e) {
			throw new FtpLtException(CodeEnum._425);
		} finally {
			try {
				if (dataSocket != null) {
					dataSocket.close();
				}
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
		}
		return CodeEnum._226;
	}

	protected abstract Object[] getList(StringBuilder args);

	protected abstract void writeFileList(PrintWriter out, Object[] fileArray);

	protected CodeEnum handleNLST(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		Socket dataSocket = null;
		if (args.length() != 0 && args.charAt(args.length() - 1) != Path.SEPARATOR.charAt(0)) {
			args.append(Path.SEPARATOR);
		}
		final Object[] fileList = getList(args);
		if (fileList == null) {
			throw new FtpLtException(CodeEnum._550_ned);
		}
		send(CodeEnum._150);
		try {
			dataSocket = getDataSocketClient();
			final PrintWriter out = new PrintWriter(new OutputStreamWriter(dataSocket.getOutputStream(), "US-ASCII"),
					true);
			writeFileNameList(out, fileList);
			out.flush();
			send(CodeEnum._226);
			out.close();
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		} catch (final IOException e) {
			throw new FtpLtException(CodeEnum._425);
		} finally {
			try {
				if (dataSocket != null) {
					dataSocket.close();
				}
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
		}
		return CodeEnum._226;
	}

	protected abstract void writeFileNameList(PrintWriter out, Object[] fileArray);

	protected CodeEnum handleSITE(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		return send(CodeEnum._202);
	}

	protected CodeEnum handleSYST(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		final String systemId = systemId();
		return ((systemId != null) ? send(CodeEnum._215, systemId) : send(CodeEnum._502));
	}

	protected String systemId() {
		return null;
	}

	protected CodeEnum handleSTAT(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		// TODO:
		return null;
	}

	protected CodeEnum handleHELP(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		// TODO:
		return null;
	}

	protected CodeEnum handleNOOP(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		checkLogin();
		return send(CodeEnum._200);
	}

	private CodeEnum handleOPTS(final StringBuilder args) throws FtpLtException {
		LOG.trace("lt.Init");
		final StringTokenizer st = new StringTokenizer(args.toString(), " ");
		if (!st.hasMoreTokens()) {
			return send(CodeEnum._501);
		}
		if (!st.nextToken().equalsIgnoreCase("utf8")) {
			return send(CodeEnum._501);
		}
		if (!st.hasMoreTokens()) {
			return send(CodeEnum._501);
		}
		if (!st.nextToken().equalsIgnoreCase("on")) {
			return send(CodeEnum._502);
		}
		return send(CodeEnum._200);
	}

	private void checkLogin() throws FtpLtException {
		if (!this.sessionData.isAuth()) {
			throw new FtpLtException(CodeEnum._530);
		}
	}

	protected final CodeEnum send(final CodeEnum code, final Object... args) {
		final String txt = MESSAGES.get(code.getMessageKey(), args);
		this.writer.println(txt);
		this.writer.flush();
		this.sessionData.setLastCodeSent(code);
		LOG.debug("#0", txt);
		return code;
	}

	protected final CodeEnum send(final CodeEnum code, final String personalizedText) {
		final String txt = code.getCode() + " " + personalizedText;
		this.writer.println(txt);
		this.writer.flush();
		this.sessionData.setLastCodeSent(code);
		LOG.debug("#0", txt);
		return code;
	}

	private void internalClose() throws IOException, FtpLtException {
		LOG.trace("lt.Init");
		waitFilesTransaction();
		if (this.socket != null) {
			handleQUIT(null);
			LOG.debug("end");
			this.server.endHandler(this);
			if (!this.socket.isClosed()) {
				this.socket.close();
			}
			this.socket = null;
		}
	}

	final void close() {
		LOG.trace("lt.Init");
		waitFilesTransaction();
		if (this.socket != null) {
			try {
				handleQUIT(null);
			} catch (final FtpLtException e) {
				LOG.error("#0", e);
			}
			LOG.debug("end");
			try {
				if (!this.socket.isClosed()) {
					this.socket.close();
				}
				this.socket = null;
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
		}
	}

	private void waitFilesTransaction() {
		// TODO: wait all files end transaction
	}

	private Socket getDataSocketClient() throws FtpLtException {
		LOG.trace("lt.Init");
		if (TransferModeEnum.ACTIVE.equals(this.sessionData.getTransferMode())) {
			try {
				return new Socket(InetAddress.getByName(this.sessionData.getClientDtpIp()),
						this.sessionData.getClientDtpPort(), this.socket.getLocalAddress(),
						this.socket.getLocalPort() - 1);
			} catch (final UnknownHostException e) {
				throw new FtpLtException(CodeEnum._425);
			} catch (final IOException e) {
				throw new FtpLtException(CodeEnum._425);
			}
		} else if (TransferModeEnum.PASSIV.equals(this.sessionData.getTransferMode())) {
			return this.sessionData.getSocketDtp();
		} else {
			throw new FtpLtException(CodeEnum._451);
		}
	}

}

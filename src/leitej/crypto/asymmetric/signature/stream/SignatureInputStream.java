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

package leitej.crypto.asymmetric.signature.stream;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import leitej.Constant;
import leitej.crypto.Cryptography;
import leitej.crypto.asymmetric.signature.SignatureEnum;
import leitej.crypto.exception.TamperproofLtException;
import leitej.exception.DataOverflowLtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.util.data.BinaryConcat;

/**
 *
 * @author Julio Leite
 */
public final class SignatureInputStream extends InputStream {

	private static final int SKIP_BUFFER_SIZE = Constant.IO_BUFFER_SIZE;
	private static final int BYTE_CONTROL_STEP = SignatureOutputStream.BYTE_CONTROL_STEP;

	private final transient Signature signature;
	private final InputStream in;
	private int controlStepCount;
	private int controlStepLength;
	private final byte[] readBuffer;
	private final int secureStepLength;
	private int secureStepCount;
	private boolean signatureNextTime;
	private boolean signatureTime;
	private final BinaryConcat signatureBytes;
	private boolean untrust;
	private boolean ended;

	/**
	 *
	 * @param is
	 * @param secureStepLength   the considered amount of data that can be processed
	 *                           by <code>signatureAlgorithm</code> without lose
	 *                           security
	 * @param publicKey
	 * @param signatureAlgorithm
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public SignatureInputStream(final InputStream is, final int secureStepLength, final PublicKey publicKey,
			final SignatureEnum signatureAlgorithm)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
		if (is == null || secureStepLength <= BYTE_CONTROL_STEP || publicKey == null) {
			throw new IllegalArgumentLtRtException();
		}
		this.signature = Cryptography.getSignature(signatureAlgorithm);
		this.signature.initVerify(publicKey);
		this.in = is;
		this.controlStepCount = BYTE_CONTROL_STEP;
		this.controlStepLength = BYTE_CONTROL_STEP;
		this.readBuffer = new byte[BYTE_CONTROL_STEP];
		this.secureStepLength = secureStepLength;
		this.secureStepCount = 0;
		this.signatureNextTime = false;
		this.signatureTime = false;
		this.signatureBytes = new BinaryConcat();
		this.untrust = false;
		this.ended = false;
	}

	@Override
	public synchronized int read() throws IOException {
		final int readCount = read(this.readBuffer, 0, 1);
		if (readCount == 0) {
			throw new ImplementationLtRtException();
		}
		if (readCount == -1) {
			return -1;
		}
		return this.readBuffer[0];
	}

	@Override
	public int read(final byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public synchronized int read(final byte[] b, final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		if (this.untrust) {
			throw new IOException(new TamperproofLtException());
		}
		if (this.ended) {
			return -1;
		}
		try {
			return readAux(b, off, len);
		} catch (final TamperproofLtException e) {
			throw new IOException(e);
		} catch (final IOException e) {
			internalClose(false);
			throw e;
		}
	}

	private int readAux(final byte[] b, final int off, final int len) throws IOException, TamperproofLtException {
		int bPointer = 0;
		int readStep;
		int readCount;
		while (bPointer < len) {
			if (this.controlStepCount == BYTE_CONTROL_STEP) {
				// read control byte
				int bc;
				if (b == this.readBuffer) {
					bc = this.in.read();
				} else {
					bc = this.in.read(this.readBuffer, 0, 1);
					if (bc == 0) {
						updateSignature(b, off, bPointer);
						return bPointer;
					}
					if (bc == 1) {
						bc = this.readBuffer[0];
					}
				}
				if (bc == -1) {
					if (this.signatureTime && !this.signatureNextTime) {
						// the correct point to end the stream
						internalClose(true);
						return ((bPointer != 0) ? bPointer : -1);
					} else {
						// its not supposed to end at this point
						internalClose(false);
						throw new TamperproofLtException();
					}
				} else {
					this.signatureTime = this.signatureNextTime;
					this.signatureNextTime = (bc & 0x01) == 1;
					if (!this.signatureTime && this.signatureNextTime
							|| this.signatureTime && !this.signatureNextTime) {
						this.controlStepLength = BYTE_CONTROL_STEP - ((bc & 0xfe) >>> 1);
					} else {
						this.controlStepLength = BYTE_CONTROL_STEP;
					}
					this.controlStepCount = 0;
				}
			}
			if (this.signatureTime) {
				readStep = this.controlStepLength - this.controlStepCount;
				if (readStep > 0) {
					// read signature
					readCount = this.in.read(this.readBuffer, 0, readStep);
					if (readCount == -1) {
						// its not supposed to end at this point
						internalClose(false);
						throw new TamperproofLtException();
					}
					if (readCount == 0) {
						if (b == this.readBuffer) {
							final int readByte = this.in.read();
							if (readByte == -1) {
								// its not supposed to end at this point
								internalClose(false);
								throw new TamperproofLtException();
							}
							this.signatureBytes.add(readByte);
							this.controlStepCount++;
						} else {
							return bPointer;
						}
					} else {
						this.signatureBytes.add(this.readBuffer, 0, readCount);
						this.controlStepCount += readCount;
					}
				} else {
					// consume sign padding
					readCount = this.in.read(this.readBuffer, 0, BYTE_CONTROL_STEP - this.controlStepCount);
					if (readCount == -1) {
						// its not supposed to end at this point
						internalClose(false);
						throw new TamperproofLtException();
					}
					if (readCount == 0) {
						if (b == this.readBuffer) {
							if (this.in.read() == -1) {
								// its not supposed to end at this point
								internalClose(false);
								throw new TamperproofLtException();
							}
							this.controlStepCount++;
						} else {
							return bPointer;
						}
					} else {
						this.controlStepCount += readCount;
					}
				}
				if (this.controlStepCount == BYTE_CONTROL_STEP && !this.signatureNextTime) {
					// verify sign
					if (!verifySignature(this.signatureBytes.resetSuppress())) {
						// wrong signature
						internalClose(false);
						throw new TamperproofLtException();
					}
				}
			} else {
				readStep = this.controlStepLength - this.controlStepCount;
				if (readStep > 0) {
					// read data
					readStep = Math.min(len - bPointer, readStep);
					if (readStep == 0) {
						updateSignature(b, off, bPointer);
						return bPointer;
					}
					readCount = this.in.read(b, off + bPointer, readStep);
					if (readCount == -1) {
						// its not supposed to end at this point
						internalClose(false);
						throw new TamperproofLtException();
					} else if (readCount == 0) {
						if (b == this.readBuffer) {
							final int readByte = this.in.read();
							if (readByte == -1) {
								// its not supposed to end at this point
								internalClose(false);
								throw new TamperproofLtException();
							}
							b[off + bPointer] = (byte) (readByte & 0xff);
							bPointer++;
							this.controlStepCount++;
							this.secureStepCount++;
						} else {
							updateSignature(b, off, bPointer);
							return bPointer;
						}
					} else {
						bPointer += readCount;
						this.controlStepCount += readCount;
						this.secureStepCount += readCount;
					}
					if (this.secureStepLength < this.secureStepCount) {
						// its not supposed this amount of data pass through the stream without a
						// signature in the middle
						internalClose(false);
						throw new TamperproofLtException(new DataOverflowLtException());
					}
				} else {
					// read data padding
					readCount = this.in.read(this.readBuffer, 0, BYTE_CONTROL_STEP - this.controlStepCount);
					if (readCount == -1) {
						// its not supposed to end at this point
						internalClose(false);
						throw new TamperproofLtException();
					}
					if (readCount == 0) {
						if (b == this.readBuffer) {
							if (this.in.read() == -1) {
								// its not supposed to end at this point
								internalClose(false);
								throw new TamperproofLtException();
							}
							this.controlStepCount++;
						} else {
							updateSignature(b, off, bPointer);
							return bPointer;
						}
					} else {
						this.controlStepCount += readCount;
					}
				}
				if (this.controlStepCount == BYTE_CONTROL_STEP && this.signatureNextTime) {
					updateSignature(b, off, bPointer);
					return bPointer;
				}
			}
		}
		updateSignature(b, off, bPointer);
		return bPointer;
	}

	private void updateSignature(final byte[] data, final int off, final int len) {
		if (len != 0) {
			try {
				this.signature.update(data, off, len);
			} catch (final SignatureException e) {
				throw new ImplementationLtRtException(e);
			}
		}
	}

	private boolean verifySignature(final byte[] sign) {
		try {
			return this.signature.verify(sign);
		} catch (final SignatureException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	private void internalClose(final boolean trust) throws IOException {
		this.untrust = !trust;
		this.controlStepCount = BYTE_CONTROL_STEP;
		this.ended = true;
		this.in.close();
	}

	@Override
	public synchronized long skip(final long n) throws IOException {

		long remaining = n;
		int nr;

		if (n <= 0) {
			return 0;
		}

		final int size = (int) Math.min(SKIP_BUFFER_SIZE, remaining);
		final byte[] skipBuffer = new byte[size];

		while (remaining > 0) {
			nr = read(skipBuffer, 0, (int) Math.min(size, remaining));

			if (nr < 0) {
				break;
			}
			remaining -= nr;
		}

		return n - remaining;
	}

	@Override
	public synchronized int available() throws IOException {
		if (this.untrust) {
			throw new IOException(new TamperproofLtException());
		}
		return Math.min(this.controlStepLength - this.controlStepCount, this.in.available());
	}

	@Override
	public synchronized void close() throws IOException {
		if (!this.ended) {
			if (read() != -1) {
				internalClose(false);
			}
		}
		if (this.untrust) {
			throw new IOException(new TamperproofLtException());
		}
	}

	@Override
	public synchronized void mark(final int readlimit) {
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}

	@Override
	public boolean markSupported() {
		return false;
	}

}

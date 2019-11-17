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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import leitej.log.Logger;
import leitej.util.BinaryUtil;
import leitej.util.data.BigBinaryData;
import leitej.util.stream.BinaryFile;
import leitej.util.stream.FileUtil;
import leitej.util.stream.RandomAccessModeEnum;

/**
 *
 *
 * @author Julio Leite
 */
public final class LtmBinary implements Serializable {

	private static final long serialVersionUID = -5925496438079430896L;

	private static final Logger LOG = Logger.getInstance();

	private static final String FILE_BAK_NAME = new File(ConstantLtm.DEFAULT_STREAM_DIR, ".ltm_stm.lt")
			.getAbsolutePath();
	private static final List<LtmBinary> FILES_TO_DELETE = new ArrayList<>();
	private static final BinaryFile FILE_BAK;
	private static final List<Boolean> INDEX_FILE_BAK = new ArrayList<>();
	private static long BYTE_FILE_TO_WRITE = 0;
	private static final int pieceSize = 8;
	private static final int pieceSizeDouble = pieceSize * 2;
	private static final byte[] buffer = new byte[pieceSize];
	static {
		BinaryFile tmp = null;
		try {
			FileUtil.createFile(FILE_BAK_NAME);
			tmp = new BinaryFile(FILE_BAK_NAME, RandomAccessModeEnum.RWS);
			if (tmp.length() > 0L) {
				// FIXME: atencao caso se apaga o file colocar o long64 a zero
				// e colocar o INDEX_FILE_BAK a apontar para o fim dos longs caso nao consiga
				// apagar todos os que ainda estavam no ficheiro
				// para que volte a tentar apagar
				// TODO: verificar novamente este sistema ao detalhe para ver as falhas
				LOG.warn("Recovering from improper end!");
				long id;
				long idReverse;
				int countRead;
				if ((tmp.length() % pieceSizeDouble) == 0) {
					for (int i = 0; i < tmp.length(); i += pieceSizeDouble) {
						countRead = 0;
						while ((countRead += tmp.read(i + countRead, buffer, countRead,
								pieceSize - countRead)) < pieceSize) {
						}
						id = BinaryUtil.readLong64bit(buffer);
						countRead = 0;
						while ((countRead += tmp.read(i + pieceSize + countRead, buffer, countRead,
								pieceSize - countRead)) < pieceSize) {
						}
						idReverse = BinaryUtil.readLong64bit(buffer);
						if (id != 0L && id == Long.reverse(idReverse)) {
							LOG.debug("Restored call to remove id '#0'", id);
							BigBinaryData.delete(ConstantLtm.DEFAULT_STREAM_DIR, id);
						}
					}
				} else {
					LOG.error("Invalid recovering file '#0' !?", FILE_BAK_NAME);
				}
				tmp.setLength(0);
			}
		} catch (final IOException e) {
			LOG.error("#0", e);
		} catch (final SecurityException e) {
			LOG.error("#0", e);
		} finally {
			FILE_BAK = tmp;
		}
	}

	static synchronized void fileToDelete(final LtmBinary bs) {
		if (bs == null || FILES_TO_DELETE.contains(bs)) {
			return;
		}
		FILES_TO_DELETE.add(bs);
		writeBak(bs.getId());
		writeBak(Long.reverse(bs.getId()));
	}

	private static void writeBak(final long v) {
		BinaryUtil.writeLong64bit(buffer, v);
		try {
			FILE_BAK.write(BYTE_FILE_TO_WRITE, buffer);
		} catch (final IOException e) {
			LOG.error("#0", e);
		}
		INDEX_FILE_BAK.add(Boolean.TRUE);
		BYTE_FILE_TO_WRITE += pieceSize;
	}

	static synchronized void ignoreFileToDelete(final LtmBinary bs) {
		int index;
		long off = 0;
		if (bs == null || (index = FILES_TO_DELETE.indexOf(bs)) == -1) {
			return;
		}
		FILES_TO_DELETE.remove(bs);
		for (int i = 0; i < INDEX_FILE_BAK.size() && index > -1; i++) {
			if (INDEX_FILE_BAK.get(i).booleanValue()) {
				index--;
				off += pieceSizeDouble;
			}
			if (index == -1) {
				BinaryUtil.writeLong64bit(buffer, 0L);
				try {
					FILE_BAK.write(off, buffer);
					off += pieceSize;
					FILE_BAK.write(off, buffer);
				} catch (final IOException e) {
					LOG.fatal("#0", e);
				}
				INDEX_FILE_BAK.set(i, Boolean.FALSE);
			}
		}
	}

	static synchronized void execDelete() {
		LOG.debug("deleting #0 files", FILES_TO_DELETE.size());
		for (final LtmBinary bs : FILES_TO_DELETE) {
			LOG.debug("delete: #0", bs.id);
			BigBinaryData.delete(ConstantLtm.DEFAULT_STREAM_DIR, bs.id);
		}
		FILES_TO_DELETE.clear();
		try {
			FILE_BAK.setLength(0);
		} catch (final IOException e) {
			LOG.error("#0", e);
		}
	}

	static boolean deleteAll() {
		return BigBinaryData.deleteAll(ConstantLtm.DEFAULT_STREAM_DIR);
	}

	private final long id;

	LtmBinary(final long id) throws IOException {
		this.id = id;
	}

	long getId() {
		return this.id;
	}

	public BigBinaryData access() throws IOException {
		return BigBinaryData.valueOf(ConstantLtm.DEFAULT_STREAM_DIR, this.id);
	}

//	public long copyTo(LtmBinary other) throws IOException {
//		BigBinary otherBinary = other.open();
//		otherBinary.setLength(0);
//		OutputStream otherOs = otherBinary.newOutputStream(0);
//		InputStream in = open().newInputStream(0);
//		long result = StreamUtil.pipe(in, otherOs);
//		in.close();
//		otherOs.close();
//		otherBinary.release();
//		return result;
//	}

	@Override
	public boolean equals(final Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (!LtmBinary.class.isInstance(obj)) {
			return false;
		}
		return this.id == LtmBinary.class.cast(obj).id;
	}
}

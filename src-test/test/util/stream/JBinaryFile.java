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

package test.util.stream;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.util.stream.BinaryFile;
import leitej.util.stream.FileUtil;
import leitej.util.stream.RandomAccessModeEnum;

public class JBinaryFile {

	private static String filename = "JFilePieces.lixo.dat";
	private static String filename2 = "JFilePieces2.lixo.dat";
	private static int pieceSize = 1024;
	private static int pieces = 1024;

	@Before
	public void setUp() throws Exception {
		FileUtil.createFile(filename, pieces * pieceSize);
		FileUtil.createFile(filename2, pieces * pieceSize);
	}

	@After
	public void tearDown() throws Exception {
		(new File(filename)).delete();
		(new File(filename2)).delete();
	}

	@Test
	public void test() throws IllegalArgumentException, SecurityException, NullPointerException, IOException {
		final BinaryFile fp = new BinaryFile(filename, RandomAccessModeEnum.RW);
		final byte[] buff = new byte[pieceSize];
		final Random rnd = new Random();
		for (int piece = 0; piece < pieces; piece++) {
			rnd.nextBytes(buff);
			fp.write(piece * pieceSize, buff);
		}

		final BinaryFile fp2 = new BinaryFile(filename2, RandomAccessModeEnum.RW);
		for (int piece = pieces - 1; piece >= 0; piece--) {
			fp.read(piece * pieceSize, buff);
			fp2.write(piece * pieceSize, buff);
		}

		fp.close();
		fp2.close();

		verifyFiles();
	}

	private void verifyFiles() throws NullPointerException, FileNotFoundException, SecurityException, IOException {
		assertArrayEquals(FileUtil.readAllAtOnce(filename), FileUtil.readAllAtOnce(filename2));
	}

}

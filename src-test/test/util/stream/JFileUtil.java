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

import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.util.stream.FileUtil;

public class JFileUtil {

	private String filename_md5;
	private String filename_create;
	private String filename_allAtOnce;
	private String filename_inputFile;

	@Before
	public void setUp() throws Exception {
		this.filename_md5 = "testMD5_JFileUtil.lixo.dat";
		this.filename_create = "testCreate_JFileUtil.lixo.dat";
		this.filename_allAtOnce = "testfilename_AllAtOnce_JFileUtil.lixo.dat";
		this.filename_inputFile = "testInputFile_JFileUtil.lixo.dat";
	}

	@After
	public void tearDown() throws Exception {
		(new File(this.filename_md5)).delete();
		(new File(this.filename_create)).delete();
		(new File(this.filename_allAtOnce)).delete();
		(new File(this.filename_inputFile)).delete();
	}

	@Test
	public void test_md5() throws NullPointerException, SecurityException, IOException, NoSuchAlgorithmException {
		final BufferedOutputStream bos = FileUtil.openFileBinaryOutputStream(this.filename_md5, false);
		final byte[] b = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		for (int i = 0; i < 100; i++) {
			bos.write(b);
		}
		bos.close();
		final byte[] calc = FileUtil.md5(this.filename_md5);
		final byte[] res = { -121, -43, -15, -13, 12, 90, 61, -90, 98, 85, 1, 24, 62, 78, 118, 90 };
		assertTrue(Arrays.equals(calc, res));
	}

	@Test
	public void test_create() throws NullPointerException, SecurityException, IOException {
		assertTrue(!FileUtil.exists(this.filename_create));
		FileUtil.createFile(this.filename_create);
		assertTrue(FileUtil.exists(this.filename_create));
		assertTrue(FileUtil.getFileSize(this.filename_create) == 0L);
		assertTrue(!FileUtil.createFile(this.filename_create, 1024));
		(new File(this.filename_create)).delete();
		assertTrue(!FileUtil.exists(this.filename_create));
		assertTrue(FileUtil.createFile(this.filename_create, 1024));
		assertTrue(FileUtil.exists(this.filename_create));
		assertTrue(FileUtil.getFileSize(this.filename_create) == 1024L);
	}

	@Test
	public void test_AllAtOnce() throws NullPointerException, FileNotFoundException, SecurityException, IOException {
		final byte[] input = new byte[2 * 1024];
		(new Random()).nextBytes(input);
		FileUtil.writeAllAtOnce(input, this.filename_allAtOnce, false);
		assertTrue(Arrays.equals(input, FileUtil.readAllAtOnce(this.filename_allAtOnce)));
		final byte[] input2 = new byte[1024];
		(new Random()).nextBytes(input2);
		FileUtil.writeAllAtOnce(input2, this.filename_allAtOnce, false);
		assertTrue(Arrays.equals(input2, FileUtil.readAllAtOnce(this.filename_allAtOnce)));
	}

	@Test(expected = FileNotFoundException.class)
	public void test_inputFile() throws NullPointerException, SecurityException, FileNotFoundException {
		(new File(this.filename_inputFile)).delete();
		FileUtil.openFileBinaryInputStream(this.filename_inputFile);
	}

}

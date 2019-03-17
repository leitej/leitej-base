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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.util.stream.BinaryFileFractionOutputStream;
import leitej.util.stream.FileUtil;

public class JBinaryFileFraction {

	private static String filename = "JFileFraction.lixo.dat";

	@Before
	public void setUp() throws Exception {
		FileUtil.createFile(filename, 100);
	}

	@After
	public void tearDown() throws Exception {
		(new File(filename)).delete();
	}

	@Test
	public void test() throws IOException {
		final BinaryFileFractionOutputStream out = new BinaryFileFractionOutputStream(filename, 0, false);
		final BinaryFileFractionOutputStream out2 = new BinaryFileFractionOutputStream(filename, 10, false);
		out.write(2);
		out2.write(1);
		out.close();
		out2.close();
		System.out.println(Arrays.toString(FileUtil.readAllAtOnce(filename)));
		assertTrue(true);
	}

}

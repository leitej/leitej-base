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

package test.util;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.util.HexaUtil;
import leitej.util.data.BinaryConcat;

public class JBinaryConcat {

	private BinaryConcat bin;

	@Before
	public void setUp() throws Exception {
		this.bin = new BinaryConcat();
	}

	@After
	public void tearDown() throws Exception {
		this.bin = null;
	}

	@Test
	public void test() {
		assertTrue(this.bin.bitSize() == 0);
		this.bin.add(1);
		assertTrue(this.bin.bitSize() == 8);
		this.bin.add(0, 1, 1);
		assertTrue(this.bin.bitSize() == 9);
		this.bin.add(0x8f, 0, 7);
		assertTrue(this.bin.bitSize() == 16);
		this.bin.add(0, 0, 3);
		assertTrue(this.bin.bitSize() == 19);
		this.bin.add(1, 0, 1);
		this.bin.add(0xf);
		assertTrue(HexaUtil.toHex(this.bin.resetExcess()).equals("010F10F0"));
	}

	@Test
	public void test2() {
		this.bin.resetExcess();
		this.bin.add(new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff });
		assertTrue(HexaUtil.toHex(this.bin.resetExcess()).equals("FFFFFFFFFF"));
		this.bin.add(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 });
		assertTrue(HexaUtil.toHex(this.bin.resetExcess()).equals("0000000000"));
	}

}

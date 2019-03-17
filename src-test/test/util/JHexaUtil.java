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

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.util.HexaUtil;
import leitej.util.data.BinaryConcat;
import leitej.util.data.Stopwatch;

public class JHexaUtil {

	private static final String out = "000102030405060708090a0b0c0d0e0f" + "101112131415161718191a1b1c1d1e1f"
			+ "202122232425262728292a2b2c2d2e2f" + "303132333435363738393a3b3c3d3e3f"
			+ "404142434445464748494a4b4c4d4e4f" + "505152535455565758595a5b5c5d5e5f"
			+ "606162636465666768696a6b6c6d6e6f" + "707172737475767778797a7b7c7d7e7f"
			+ "808182838485868788898a8b8c8d8e8f" + "909192939495969798999a9b9c9d9e9f"
			+ "a0a1a2a3a4a5a6a7a8a9aaabacadaeaf" + "b0b1b2b3b4b5b6b7b8b9babbbcbdbebf"
			+ "c0c1c2c3c4c5c6c7c8c9cacbcccdcecf" + "d0d1d2d3d4d5d6d7d8d9dadbdcdddedf"
			+ "e0e1e2e3e4e5e6e7e8e9eaebecedeeef" + "f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		final BinaryConcat bc = new BinaryConcat();
		for (int i = 0; i < 256; i++) {
			bc.add(i);
		}
		final byte[] in = bc.resetSuppress();
		assertTrue("".equals(HexaUtil.toHex(new byte[] {})));
		assertTrue(out.toUpperCase().equals(HexaUtil.toHex(in)));
		assertTrue(Arrays.equals(in, HexaUtil.toByte(out)));
	}

	@Test
	public void test2() {
		assertTrue("FFFFFFFFFF".equals(
				HexaUtil.toHex(new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff })));
		assertTrue("0000000000".equals(
				HexaUtil.toHex(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 })));
	}

	@Test
	public void testCharge() {
		final Stopwatch sw = Stopwatch.getInstance();
		sw.start();
		final BinaryConcat bc = new BinaryConcat();
		sw.step("new BinaryConcat");
		for (int mB = 0; mB < 24; mB++) {
			for (int kB = 0; kB < 1024; kB++) {
				for (int j = 0; j < 4; j++) {
					for (int i = 0; i < 256; i++) {
						bc.add(i);
					}
				}
			}
		}
		sw.step("add " + bc.size() + " Bytes");
		System.out.println(bc.size() + " Bytes");
		System.out.println(bc.size() / 1024 + " KBytes");
		System.out.println(bc.size() / 1024 / 1024 + " MBytes");
		System.out.println(bc.size() * 8 / 1024 / 1024 + " Mbites");
		sw.step("System.out.println");
		final byte[] in = bc.resetSuppress();
		sw.step("bc.resetSuppress");
		final StringBuilder buffer = new StringBuilder(in.length * 2);
		sw.step("new StringBuilder(in.length*2)");
		HexaUtil.toHex(buffer, in);
		sw.step("toHex");
		final byte[] out = HexaUtil.toByte(buffer);
		sw.step("toByte");
		assertTrue(Arrays.equals(in, out));
		sw.stop("assertTrue");
		System.out.println(sw.toString());
	}

}

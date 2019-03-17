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

import leitej.util.BinaryUtil;

public class JBinaryUtil {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
//		BinaryUtil.convert(new Byte[1]);
//		BinaryUtil.convert(new byte[1]);
//		assertTrue(true);
	}

	@Test
	public void test_long_byteArray() {
		final byte[] buffer = new byte[BinaryUtil.LONG_BYTE_LENGTH];
		BinaryUtil.writeLong64bit(buffer, 0L);
		final byte[] tmp = new byte[BinaryUtil.LONG_BYTE_LENGTH];
		for (int i = 0; i < buffer.length; i++) {
			assertTrue(buffer[i] == tmp[i]);
		}
		BinaryUtil.writeLong64bit(buffer, 1L);
		tmp[BinaryUtil.LONG_BYTE_LENGTH - 1] = 0x01;
		for (int i = 0; i < BinaryUtil.LONG_BYTE_LENGTH; i++) {
			assertTrue(buffer[i] == tmp[i]);
		}
		BinaryUtil.writeLong64bit(buffer, 2L);
		tmp[BinaryUtil.LONG_BYTE_LENGTH - 1] = 0x02;
		for (int i = 0; i < BinaryUtil.LONG_BYTE_LENGTH; i++) {
			assertTrue(buffer[i] == tmp[i]);
		}
		BinaryUtil.writeLong64bit(buffer, Long.MIN_VALUE);
		tmp[0] = (byte) 0x80;
		tmp[BinaryUtil.LONG_BYTE_LENGTH - 1] = 0x00;
		for (int i = 0; i < BinaryUtil.LONG_BYTE_LENGTH; i++) {
			assertTrue(buffer[i] == tmp[i]);
		}
		BinaryUtil.writeLong64bit(buffer, -1L);
		for (int i = 0; i < BinaryUtil.LONG_BYTE_LENGTH; i++) {
			tmp[i] = (byte) 0xff;
		}
		for (int i = 0; i < BinaryUtil.LONG_BYTE_LENGTH; i++) {
			assertTrue(buffer[i] == tmp[i]);
		}
		BinaryUtil.writeLong64bit(buffer, -2L);
		tmp[BinaryUtil.LONG_BYTE_LENGTH - 1] = (byte) 0xfe;
		for (int i = 0; i < BinaryUtil.LONG_BYTE_LENGTH; i++) {
			assertTrue(buffer[i] == tmp[i]);
		}
	}

}

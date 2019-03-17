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

import org.junit.Test;

import leitej.util.ColorUtil;

/**
 *
 * @author Julio Leite
 */
public class JColorUtil {

	@Test
	public void test() {
		assertTrue(ColorUtil.webColor(0, 0, 0).equals("#000000"));
		assertTrue(ColorUtil.webColor(0, 0, 255).equals("#0000FF"));
		assertTrue(ColorUtil.webColor(0, 255, 0).equals("#00FF00"));
		assertTrue(ColorUtil.webColor(255, 0, 0).equals("#FF0000"));
		assertTrue(ColorUtil.webColor(128, 128, 128).equals("#808080"));
		assertTrue(ColorUtil.webColor(255, 255, 0).equals("#FFFF00"));
		assertTrue(ColorUtil.webColor(255, 255, 255).equals("#FFFFFF"));

		assertTrue(ColorUtil.rgbColor(0, 0, 0) == 0 * 65536 + 0 * 256 + 0);
		assertTrue(ColorUtil.rgbColor(0, 0, 255) == 0 * 65536 + 0 * 256 + 255);
		assertTrue(ColorUtil.rgbColor(0, 255, 0) == 0 * 65536 + 255 * 256 + 0);
		assertTrue(ColorUtil.rgbColor(255, 0, 0) == 255 * 65536 + 0 * 256 + 0);
		assertTrue(ColorUtil.rgbColor(128, 128, 128) == 128 * 65536 + 128 * 256 + 128);
		assertTrue(ColorUtil.rgbColor(255, 255, 0) == 255 * 65536 + 255 * 256 + 0);
		assertTrue(ColorUtil.rgbColor(255, 255, 255) == 255 * 65536 + 255 * 256 + 255);

		assertTrue(ColorUtil.webColor(0 * 65536 + 0 * 256 + 0).equals("#000000"));
		assertTrue(ColorUtil.webColor(0 * 65536 + 0 * 256 + 255).equals("#0000FF"));
		assertTrue(ColorUtil.webColor(0 * 65536 + 255 * 256 + 0).equals("#00FF00"));
		assertTrue(ColorUtil.webColor(255 * 65536 + 0 * 256 + 0).equals("#FF0000"));
		assertTrue(ColorUtil.webColor(128 * 65536 + 128 * 256 + 128).equals("#808080"));
		assertTrue(ColorUtil.webColor(255 * 65536 + 255 * 256 + 0).equals("#FFFF00"));
		assertTrue(ColorUtil.webColor(255 * 65536 + 255 * 256 + 255).equals("#FFFFFF"));
	}

}

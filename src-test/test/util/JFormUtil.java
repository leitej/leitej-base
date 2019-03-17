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

import leitej.util.FormUtil;

/**
 *
 * @author Julio Leite
 */
public class JFormUtil {

	@Test
	public void test() {
		assertTrue(!FormUtil.isValideNIF("000000090"));
		assertTrue(FormUtil.isValideNIF("000000094"));
		assertTrue(FormUtil.isValideNIF("000000000"));
		assertTrue(!FormUtil.isValideNIF("000000001"));
		assertTrue(!FormUtil.isValideNIF("0000000000"));
		assertTrue(FormUtil.isValideNIF("000 000 000"));
		assertTrue(FormUtil.isValideNIF("000.000.000"));
	}

}

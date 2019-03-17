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

package test.util.machine;

import static org.junit.Assert.fail;

import org.junit.Test;

import leitej.util.machine.ConsoleUtil;

/**
 *
 * @author Julio Leite
 */
public class JConsoleUtil {

	@Test
	public void test() {
		ConsoleUtil.write("ola");
		ConsoleUtil.readPassword();
		fail("Not yet implemented");
	}

}

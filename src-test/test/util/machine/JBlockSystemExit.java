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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import leitej.log.Logger;
import leitej.util.AgnosticUtil;
import leitej.util.data.Invoke;
import leitej.util.machine.BlockSystemExit;

/**
 *
 * @author Julio Leite
 */
public class JBlockSystemExit {

	private static final Logger LOG = Logger.getInstance();

	@Test
	public void test() throws IllegalArgumentException, SecurityException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		BlockSystemExit
				.from(new Invoke(JBlockSystemExit.class, AgnosticUtil.getMethod(JBlockSystemExit.class, "exit")));
		assertTrue(true);
		System.exit(0);
	}

	public static void exit() {
		try {
			System.exit(0);
			fail("Not Correct");
		} catch (final SecurityException e) {
			LOG.info("Correct #0", e);
			throw e;
		}
	}

}

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.exception.IllegalStateLtRtException;
import leitej.log.Logger;
import leitej.util.AgnosticUtil;
import leitej.util.data.Invoke;
import leitej.util.machine.ShutdownHookUtil;

public class JShutdownHookUtil {

	private static final Logger LOG = Logger.getInstance();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws NullPointerException, IllegalArgumentException, IllegalStateException, SecurityException,
			NoSuchMethodException {
		ShutdownHookUtil.add(new Invoke(this, AgnosticUtil.getMethod(this, "sh")));
		ShutdownHookUtil.addToFirst(new Invoke(this, AgnosticUtil.getMethod(this, "shFirst")));
		ShutdownHookUtil.addToLast(new Invoke(this, AgnosticUtil.getMethod(this, "shLast")));
		assertTrue(true); // Assert at log the execution of the 'sh' method 3 times
	}

	// ###################################################################################
	// THIS METHOD HAS TO BE CALLED AT THE END OF EXECUTION OF JUNIT
	// ###################################################################################
	public void sh() throws NullPointerException, IllegalArgumentException, SecurityException, NoSuchMethodException {
		LOG.info("lt.Init");
		try {
			ShutdownHookUtil.add(new Invoke(this, AgnosticUtil.getMethod(this, "sh")));
			LOG.fatal("it should not pass here.");
		} catch (final IllegalStateLtRtException e) {
		}
		LOG.info(";)");
	}

	public void shFirst()
			throws NullPointerException, IllegalArgumentException, SecurityException, NoSuchMethodException {
		LOG.info("lt.Init");
		try {
			ShutdownHookUtil.add(new Invoke(this, AgnosticUtil.getMethod(this, "sh")));
			LOG.fatal("it should not pass here.");
		} catch (final IllegalStateLtRtException e) {
		}
		LOG.info(";) first");
	}

	public void shLast() throws NullPointerException, IllegalArgumentException, SecurityException,
			NoSuchMethodException, InterruptedException {
		LOG.info("lt.Init");
		try {
			ShutdownHookUtil.add(new Invoke(this, AgnosticUtil.getMethod(this, "sh")));
			LOG.fatal("it should not pass here.");
		} catch (final IllegalStateLtRtException e) {
		}
		LOG.info(";) last");
		Thread.sleep(2000);
	}

}

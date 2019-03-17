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

package test.exception;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import leitej.exception.IllegalStateLtRtException;
import leitej.exception.SeppukuLtRtException;
import leitej.log.Logger;

/**
 *
 * @author Julio Leite
 */
public class JException {

	private static final Logger LOG = Logger.getInstance();

	@Test
	public void test1() {
		LOG.info("#0", new IllegalStateLtRtException("lt.FaultDetected"));
		LOG.info("#0", new IllegalStateLtRtException("lt.FileCreated", "phantom"));
		LOG.trace("#0", new IllegalStateLtRtException(new IllegalStateLtRtException("lt.FaultDetected"),
				"lt.FileCreated", "phantom"));
		assertTrue(true);
	}

	@Test
	public void test2() {
		new SeppukuLtRtException(0, null);
		try {
			Thread.sleep(10000);
		} catch (final Exception e) {
			LOG.error("#0", e);
		}
	}

}

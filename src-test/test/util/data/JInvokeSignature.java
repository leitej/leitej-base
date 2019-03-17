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

package test.util.data;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.util.AgnosticUtil;
import leitej.util.data.Invoke;
import leitej.util.data.InvokeItf;
import leitej.util.data.InvokeSignature;

public class JInvokeSignature {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = IllegalArgumentLtRtException.class)
	public final void test() {
		new InvokeSignature(null, null);
	}

	@Test
	public final void test2() throws SecurityException, NoSuchMethodException {
		final InvokeSignature is = new InvokeSignature(null, AgnosticUtil.getMethod(this, "aux2", Object.class));
		assertTrue(!is.matchArguments());
		assertTrue(is.matchArguments(Object.class));
		assertTrue(is.matchArguments(JInvokeSignature.class));
	}

	public void aux2(final Object obj) {
	}

	@Test
	public final void test3() throws SecurityException, NoSuchMethodException {
		final InvokeSignature is = new InvokeSignature(null, AgnosticUtil.getMethod(this, "aux3"));
		assertTrue(is.matchArguments());
		assertTrue(!is.matchArguments(Object.class));
		assertTrue(!is.matchArguments(JInvokeSignature.class));
	}

	public void aux3() {
	}

	@Test
	public final void test4() throws SecurityException, NoSuchMethodException {
		final InvokeSignature is = new InvokeSignature(null, AgnosticUtil.getMethod(this, "aux4", InvokeItf.class));
		assertTrue(!is.matchArguments());
		assertTrue(!is.matchArguments(Object.class));
		assertTrue(is.matchArguments(InvokeItf.class));
		assertTrue(is.matchArguments(Invoke.class));
		assertTrue(!is.matchArguments(InvokeSignature.class));
	}

	public void aux4(final InvokeItf af) {
	}

}

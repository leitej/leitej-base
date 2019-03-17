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

package test.util.stream;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.util.stream.SniffInputStream;
import leitej.util.stream.SniffOutputStream;

public class JSniffStream {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws IOException {
		final byte[] msg = new byte[1024];
		final byte[] msg_in = new byte[1024];
		(new Random()).nextBytes(msg);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final ByteArrayOutputStream outSniff = new ByteArrayOutputStream();
		final OutputStream os = new SniffOutputStream(out, outSniff);
		os.write(msg);
		os.flush();
		os.close();
		final InputStream input = new ByteArrayInputStream(out.toByteArray());
		final ByteArrayOutputStream inputSniff = new ByteArrayOutputStream();
		final InputStream is = new SniffInputStream(input, inputSniff);
		is.read(msg_in);
		is.close();
		assertTrue(Arrays.equals(msg, msg_in));
		outSniff.close();
		inputSniff.close();
		assertTrue(Arrays.equals(msg, outSniff.toByteArray()));
		assertTrue(Arrays.equals(msg, inputSniff.toByteArray()));
	}

}

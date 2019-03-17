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

package test.locale;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.Constant;
import leitej.locale.message.Messages;
import leitej.util.stream.FileUtil;

public class JMessages {

	private static String FILE_NAME = "testJMessages.lixo.en";

	@Before
	public void setUp() throws Exception {
		final BufferedWriter localeFileAdd = FileUtil.openFileOutputWriter(FILE_NAME, false, "UTF8");
		localeFileAdd.write("#####################################\n" + "# Message file for english language #\n"
				+ "#####################################\n" + "\n" + "hi				=hello #0!\n" + "\n"
				+ "#EXCEPTION\n" + "Dahh			=Simple, not!!!!\n" + "\n");
		localeFileAdd.close();
	}

	@After
	public void tearDown() throws Exception {
		(new File(FILE_NAME)).delete();
	}

	@Test
	public final void testGet() {
		final Messages messages = Messages.getInstance();

		// (new File(Constant.DEFAULT_PROPERTIES_FILE_DIR + "messages." +
		// Locale.ENGLISH.getISO3Language())).delete();
		// assertTrue(!messages.getInitFailMsg().equals(""));
		// System.out.println("Fail message: '"+messages.getInitFailMsg()+"'");

		try {
			assertTrue(0 == messages.loadFile(FILE_NAME + "1", Constant.UTF8_CHARSET_NAME));
		} catch (final IOException e) {
		}

		assertTrue(messages.get("lt.NewInstance").equals("new instance"));
		assertTrue(messages.get("Dahh").equals("Dahh"));
		assertTrue(messages.get("Dahh", "MSG").equals("Dahh"));
		assertTrue(!messages.get("Dahh").equals("Dahh1"));
		assertTrue(messages.get("hi").equals("hi"));
		assertTrue(messages.get("hi", "MSG").equals("hi"));

		try {
			assertTrue(2 == messages.loadFile(FILE_NAME, Constant.UTF8_CHARSET_NAME));
		} catch (final IOException e) {
			e.printStackTrace();
		}

		assertTrue(messages.get("lt.NewInstance").equals("new instance"));
		assertTrue(!messages.get("Dahh").equals("Dahh"));
		assertTrue(messages.get("Dahh").equals("Simple, not!!!!"));
		assertTrue(messages.get("Dahh", "MSG").equals("Simple, not!!!!"));
		assertTrue(!messages.get("Dahh").equals("Dahh1"));
		assertTrue(messages.get("hi").equals("hello #0!"));
		assertTrue(messages.get("hi", "MSG").equals("hello MSG!"));
	}

}

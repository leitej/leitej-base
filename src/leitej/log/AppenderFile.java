/*******************************************************************************
 * Copyright Julio Leite
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

package leitej.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import leitej.Constant;
import leitej.locale.message.Messages;

/**
 * AppenderFile
 *
 * @author Julio Leite
 */
class AppenderFile extends AbstractAppender {

	private static final Messages MESSAGES = Messages.getInstance();
	private PrintStream out = null;

	AppenderFile(final Config lp) throws UnsupportedEncodingException, FileNotFoundException {
		super(lp);
		if (lp.getFile() == null) {
			throw new FileNotFoundException(MESSAGES.get("try open file 'null'"));
		} else {
			final boolean appendFile = (lp.getFile().getAppendFile() == null) ? true : lp.getFile().getAppendFile();
			final String charsetName = (lp.getFile().getCharsetName() != null) ? lp.getFile().getCharsetName()
					: Constant.DEFAULT_CHARSET_NAME;
			final String fileName = lp.getFile().getFileName();
			final String pathFile = lp.getFile().getPath();
			this.out = new PrintStream(new FileOutputStream(new File(pathFile, fileName), appendFile), false,
					charsetName);
		}
	}

	@Override
	void close() {
		if (this.out != null) {
			this.out.flush();
			this.out.close();
			this.out = null;
		}
	}

	@Override
	void outPrint(final boolean newRecord, final String txt) {
		if (this.out != null) {
			this.out.print(txt);
			// out.flush();
		}
	}

}

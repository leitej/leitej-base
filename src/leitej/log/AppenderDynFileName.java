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
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import leitej.Constant;
import leitej.util.DateUtil;
import leitej.util.data.DateFieldEnum;
import leitej.util.data.TimeTrigger;
import leitej.util.data.TimeTriggerImpl;

/**
 * AppenderDynFileName
 *
 * @author Julio Leite
 */
class AppenderDynFileName extends AbstractAppender {

	private final String pathFile;
	private final String staticFileName;
	private final String dynamicFileNameFormat;
	private final boolean appendFile;
	private final String charsetName;

	private final TimeTrigger dateTimer;
	private String lastError = null;
	private Date expireDate = null;
	private PrintStream out = null;

	AppenderDynFileName(final Config lp) throws UnsupportedEncodingException, FileNotFoundException {
		super(lp);
		if (lp.getFile() == null) {
			throw new FileNotFoundException("try open file 'null'");
		}
		if (lp.getFile().getAppendFile() != null) {
			this.appendFile = lp.getFile().getAppendFile();
		} else {
			this.appendFile = true;
		}
		if (lp.getFile().getCharsetName() != null) {
			this.charsetName = lp.getFile().getCharsetName();
		} else {
			this.charsetName = Constant.UTF8_CHARSET_NAME;
		}
		this.staticFileName = lp.getFile().getFileName();
		this.pathFile = lp.getFile().getPath();
		this.dynamicFileNameFormat = lp.getFile().getDynName().getDateFormat();
		final DateFieldEnum dateField = lp.getFile().getDynName().getDatePeriodType();
		this.expireDate = DateUtil.zeroTill(DateUtil.now(), dateField);
		this.dateTimer = new TimeTriggerImpl(dateField, 1);
	}

	@Override
	public void close() throws IOException {
		if (this.out != null) {
			this.out.flush();
			this.out.close();
			this.out = null;
		}
	}

	@Override
	void outPrint(final boolean newRecord, final String txt) {
		if (newRecord) {
			updateOut();
		}
		if (this.out != null) {
			this.out.print(txt);
			// out.flush();
		}
	}

	private void updateOut() {
		if (!DateUtil.isFuture(this.expireDate)) {
			try {
				close();
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
			final String fileName = DateUtil.format(DateUtil.now(), this.dynamicFileNameFormat) + this.staticFileName;
			try {
				this.out = new PrintStream(new FileOutputStream(new File(this.pathFile, fileName), this.appendFile),
						false, this.charsetName);
				this.expireDate = this.dateTimer.nextTrigger();
			} catch (final UnsupportedEncodingException | FileNotFoundException e) {
				if (this.lastError == null || !this.lastError.equals(fileName)) {
					e.printStackTrace();
					this.lastError = fileName;
				}
			}
		}
	}

}

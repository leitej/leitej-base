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

package leitej.log;

import java.io.PrintStream;

/**
 * AppenderConsole
 *
 * @author Julio Leite
 */
final class AppenderConsole extends AbstractAppender {

	private PrintStream out = null;

	AppenderConsole(final Config lp) {
		super(lp);
		this.out = System.out;
	}

	@Override
	void outPrint(final boolean newRecord, final String txt) {
		if (this.out != null) {
			this.out.print(txt);
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

}

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

package leitej.util.stream;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Julio Leite
 */
public abstract class FractionInputStream extends InputStream {

	public abstract int readFractionReferenced() throws IOException;

	public abstract int readFractionReferenced(byte b[]) throws IOException;

	public abstract int readFractionReferenced(byte b[], int off, int len) throws IOException;

	public abstract long length() throws IOException;

}

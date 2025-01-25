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

package leitej.xml.om;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Julio Leite
 *
 */
public abstract class AbstractRawHandler {

	/**
	 *
	 * @param asId unique id per JVM
	 * @return data to be handled out of the OM
	 * @throws IOException
	 */
	protected abstract InputStream read(long asId) throws IOException;

	/**
	 *
	 * @param asId    unique id per JVM
	 * @param rawData data to be handled out of the OM
	 * @throws IOException
	 */
	protected abstract void write(long asId, InputStream rawData) throws IOException;

	/**
	 * this method is called as soon as the producer or the parser is closed.
	 *
	 * @throws IOException
	 */
	protected abstract void omClosed() throws IOException;

}

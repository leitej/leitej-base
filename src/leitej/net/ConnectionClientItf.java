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

package leitej.net;

import leitej.net.exception.ConnectionLtException;
import leitej.xml.om.XmlObjectModelling;

/**
 *
 * @author Julio Leite
 */
public interface ConnectionClientItf {

	/**
	 * Sends <code>request</code> to server and waits for response casted to
	 * <code>responseClass</code>.<br/>
	 * <br/>
	 * Remember that you can release the <code>request</code> and response as soon
	 * as became garbage.
	 *
	 * @param responseClass expected class from server
	 * @param request       to send to server
	 * @return response from server
	 * @throws ConnectionLtException if an exception was raised and is related with
	 *                               the connection, that exception should be put in
	 *                               the cause of the ConnectionLtException
	 */
	public abstract <I extends XmlObjectModelling> I getResponse(Class<I> responseClass, XmlObjectModelling request)
			throws ConnectionLtException;

	/**
	 * Returns the connection state.
	 *
	 * @return true if successfully connected
	 */
	public abstract boolean isConnected();

	/**
	 * Closes the connection.
	 *
	 * @throws ConnectionLtException if an exception was raised and is related with
	 *                               the connection, that exception should be put in
	 *                               the cause of the ConnectionLtException
	 */
	public abstract void close() throws ConnectionLtException;
}

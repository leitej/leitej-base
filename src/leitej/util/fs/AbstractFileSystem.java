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

package leitej.util.fs;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Abstraction of a file system.
 *
 * @author Julio Leite
 */
public abstract class AbstractFileSystem<S extends AbstractFileSystem<S, N>, N extends AbstractNode<S, N>>
		implements Serializable {

	private static final long serialVersionUID = -4449917684220851058L;

	/**
	 *
	 * @return total space in bytes that this file system can use as storage
	 */
	public abstract BigInteger totalSpace();

	/**
	 *
	 * @return used space in bytes that this file system has stored
	 */
	public abstract BigInteger usedSpace();

	/**
	 *
	 * @return free space in bytes that has not be occupied in this file system
	 */
	public abstract BigInteger freeSpace();

	/**
	 *
	 * @return root node of the file system
	 */
	public abstract N rootNode();

	/**
	 *
	 * @param path
	 * @return a node for the <code>path</code>
	 */
	public abstract N node(Path path);

}

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

import java.io.InputStream;
import java.io.Serializable;

import leitej.exception.FileSystemLtRtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.PathLtException;

/**
 *
 * @author Julio Leite
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractNode<S extends AbstractFileSystem<S, N>, N extends AbstractNode<S, N>>
		implements Comparable<N>, Serializable {

	private static final long serialVersionUID = 5912722751018793765L;

	private final S fileSystem;
	private final Path path;

	/**
	 *
	 * @param path
	 * @throws IllegalArgumentLtRtException if <code>path</code> or
	 *                                      <code>fileSystem</code> argument is null
	 */
	public AbstractNode(final S fileSystem, final Path path) throws IllegalArgumentLtRtException {
		if (fileSystem == null || path == null) {
			throw new IllegalArgumentLtRtException();
		}
		this.fileSystem = fileSystem;
		this.path = path;
	}

	/**
	 * Retrieves the parent node.
	 *
	 * @return parent node
	 */
	public final N parent() {
		return this.fileSystem.node(this.path.parent());
	}

	/**
	 * Retrieves all existing sub nodes.
	 *
	 * @return all sub nodes
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public abstract N[] list() throws FileSystemLtRtException;

	/**
	 * Retrieves the sub node if exists.
	 *
	 * @param name - path simple name
	 * @return the sub node with the <code>name</code>
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public N sub(final String name) throws FileSystemLtRtException {
		N result = null;
		if (isGroup()) {
			final N[] list = list();
			for (int i = 0; i < list.length && result == null; i++) {
				if (list[i].path().getSimpleName().equals(name)) {
					result = list[i];
				}
			}
		}
		return result;
	}

	/**
	 * Retrieves a group sub node that not necessarily exists.
	 *
	 * @param name - path simple name
	 * @return the group sub node with the <code>name</code>
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public final N subGroup(final String name) throws FileSystemLtRtException {
		try {
			return this.fileSystem.node(this.path.subGroup(name));
		} catch (final PathLtException e) {
			throw new FileSystemLtRtException(e);
		}
	}

	/**
	 * Retrieves a leaf sub node that not necessarily exists.
	 *
	 * @param name - path simple name
	 * @return the leaf sub node with the <code>name</code>
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public final N subLeaf(final String name) throws FileSystemLtRtException {
		try {
			return this.fileSystem.node(this.path.subLeaf(name));
		} catch (final PathLtException e) {
			throw new FileSystemLtRtException(e);
		}
	}

	/**
	 *
	 * @return path
	 */
	public final Path path() {
		return this.path;
	}

	/**
	 * Stores node to the file system.
	 *
	 * @return true only if successful created the node
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public abstract boolean create() throws FileSystemLtRtException;

	/**
	 * Removes node from the file system.<br/>
	 * If <code>isGroup</code> also removes all sub nodes.
	 *
	 * @return true only if successful removed the node
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public abstract boolean remove() throws FileSystemLtRtException;

	/**
	 * Asserts this node is stored.
	 *
	 * @return true only if is stored in the file system
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public abstract boolean exists() throws FileSystemLtRtException;

	/**
	 *
	 * @return if is a group of nodes
	 */
	public final boolean isGroup() {
		return this.path.representGroup();
	}

	/**
	 *
	 * @return if is not a group of nodes
	 */
	public final boolean isLeaf() {
		return this.path.representLeaf();
	}

	/**
	 *
	 * @return if is the root of the file system
	 */
	public final boolean isRoot() {
		return this.path.isRoot();
	}

	/**
	 *
	 * @return node stored time
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public abstract long createTime() throws FileSystemLtRtException;

	/**
	 *
	 * @return node last change in storage
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public abstract long changeTime() throws FileSystemLtRtException;

	/**
	 *
	 * @return the node size in bytes occupying in storage
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public abstract long size() throws FileSystemLtRtException;

	/**
	 *
	 * @return md5
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public abstract byte[] md5() throws FileSystemLtRtException;

	/**
	 * Gives an input stream with the last stored information of the node.
	 *
	 * @return InputStream
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public InputStream read() throws FileSystemLtRtException {
		return read(null, null);
	}

	/**
	 * Gives an input stream with the last stored information of the node.
	 *
	 * @param offset of first byte to be read
	 * @return InputStream
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public InputStream read(final Long offset) throws FileSystemLtRtException {
		return read(offset, null);
	}

	/**
	 * Gives an input stream with the last stored information of the node.
	 *
	 * @param offset of first byte to be read
	 * @param length in bytes to be read
	 * @return InputStream
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public abstract InputStream read(Long offset, Long length) throws FileSystemLtRtException;

	/**
	 * Stores information in the node.<br/>
	 * The argument <code>in</code> is closed in a finally block at the end of
	 * execution.
	 *
	 * @param in input stream with the information to store
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public void write(final InputStream in) throws FileSystemLtRtException {
		write(in, null, null);
	}

	/**
	 * Stores information in the node.<br/>
	 * The argument <code>in</code> is closed in a finally block at the end of
	 * execution.
	 *
	 * @param in     input stream with the information to store
	 * @param offset of the first byte to start write at node
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public void write(final InputStream in, final Long offset) throws FileSystemLtRtException {
		write(in, offset, null);
	}

	/**
	 * Stores information in the node.<br/>
	 * The argument <code>in</code> is closed in a finally block at the end of
	 * execution.
	 *
	 * @param in     input stream with the information to store
	 * @param offset of the first byte to start write at node
	 * @param length in bytes to be written
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public abstract void write(InputStream in, Long offset, Long length) throws FileSystemLtRtException;

	/**
	 * Truncates information.
	 *
	 * @param offset of the first byte truncated
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public abstract void truncate(Long offset) throws FileSystemLtRtException;

	/**
	 * Copies the information of this node to a new one.<br/>
	 * If <code>isGroup</code> also copies all sub nodes.<br/>
	 * The <code>target</code> can not be a sub path.
	 *
	 * @param target path
	 * @return node that receives the copied information
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public N copyTo(final Path target) throws FileSystemLtRtException {
		if (!exists()) {
			throw new FileSystemLtRtException(this.toString());
		}
		if ((isGroup() && !target.representGroup()) || (isLeaf() && !target.representLeaf())
				|| this.path.isParent(target)) {
			throw new FileSystemLtRtException();
		}
		final N destNode = this.fileSystem.node(target);
		if (!path().equals(target)) {
			destNode.create();
			if (isGroup()) {
				try {
					for (final N node : list()) {
						if (node.isGroup()) {
							node.copyTo(target.subGroup(node.path().getSimpleName()));
						} else {
							node.copyTo(target.subLeaf(node.path().getSimpleName()));
						}
					}
				} catch (final PathLtException e) {
					throw new FileSystemLtRtException(e);
				}
			} else {
				destNode.truncate(0L);
				destNode.write(read());
			}
		}
		return destNode;
	}

	/**
	 * Moves the information of this node to a new one.<br/>
	 * If <code>isGroup</code> also moves all sub nodes.<br/>
	 * The destiny can not be a sub path.<br/>
	 * This node is removed from the file system if destination node is not equals
	 * to this one.
	 *
	 * @param target path
	 * @return node that receives the information
	 * @throws FileSystemLtRtException if some exception is raised while interacting
	 *                                 with file system, it should be at cause
	 */
	public N moveTo(final Path target) throws FileSystemLtRtException {
		final N destNode = copyTo(target);
		if (!equals(destNode)) {
			if (!remove()) {
				throw new FileSystemLtRtException(this.toString());
			}
		}
		return destNode;
	}

	@Override
	public int compareTo(final N o) {
		return this.path.compareTo(AbstractNode.class.cast(o).path);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !AbstractNode.class.isInstance(obj)) {
			return false;
		}
		final AbstractNode o = AbstractNode.class.cast(obj);
		return this.path.equals(o.path);
	}

	@Override
	public String toString() {
		return this.path.getName() + "><" + super.toString();
	}

}

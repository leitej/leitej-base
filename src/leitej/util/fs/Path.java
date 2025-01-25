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

package leitej.util.fs;

import java.io.Serializable;
import java.util.Stack;
import java.util.StringTokenizer;

import leitej.exception.PathLtException;
import leitej.util.StringUtil;

/**
 *
 * @author Julio Leite
 */
public final class Path<S extends AbstractFileSystem<S, ?>> implements Comparable<Path<S>>, Serializable {

	private static final long serialVersionUID = 2382425828263190884L;

	public static final String SEPARATOR = "/";

	/**
	 *
	 * @param absolutePath
	 * @return
	 * @throws PathLtException if absolutePath is null or empty or doesn't start
	 *                         with separator character
	 */
	private static String[] parse(final String absolutePath) throws PathLtException {
		if (absolutePath == null || absolutePath.length() == 0 || absolutePath.charAt(0) != SEPARATOR.charAt(0)) {
			throw new PathLtException();
		}
		final boolean isDirectory = absolutePath.charAt(absolutePath.length() - 1) == SEPARATOR.charAt(0);
		final StringTokenizer pathTokenizer = new StringTokenizer(absolutePath, SEPARATOR);
		final Stack<String> segments = new Stack<>();
		while (pathTokenizer.hasMoreTokens()) {
			final String segment = pathTokenizer.nextToken();
			if (segment.equals("..")) {
				if (!segments.empty()) {
					segments.pop();
				}
			} else if (segment.equals(".")) {
				// skip
			} else {
				if (segment.length() != 0) {
					segments.push(segment);
				}
			}
		}
		if (isDirectory) {
			segments.push(SEPARATOR);
		}
		return segments.toArray(new String[segments.size()]);
	}

	private static String resolve(final String[] absolutePathSegments) {
		final StringBuilder pathBuf = new StringBuilder();
		for (final String segmentsEn : absolutePathSegments) {
			pathBuf.append(SEPARATOR);
			pathBuf.append(segmentsEn);
		}
		if (SEPARATOR.equals(absolutePathSegments[absolutePathSegments.length - 1])) {
			pathBuf.setLength(pathBuf.length() - 1);
		}
		return pathBuf.toString();
	}

	private final S fileSystem;
	private final String[] pathSegments;
	private final String path;
	private final String name;
	private final String simpleName;
	private final String leafName;
	private final boolean representGroup;

	/**
	 * Root Path
	 */
	public Path(final S fileSystem) {
		this.fileSystem = fileSystem;
		this.pathSegments = new String[] { SEPARATOR };
		this.path = SEPARATOR;
		this.name = SEPARATOR;
		this.simpleName = SEPARATOR;
		this.leafName = null;
		this.representGroup = true;
	}

	/**
	 *
	 * @param fileSystem
	 * @param absolutePath
	 * @throws PathLtException if absolutePath is null or empty or doesn't start
	 *                         with separator character
	 */
	public Path(final S fileSystem, final String absolutePath) throws PathLtException {
		this(fileSystem, parse(absolutePath));
	}

	private Path(final S fileSystem, final String[] pathSegments) {
		this.fileSystem = fileSystem;
		this.pathSegments = pathSegments;
		final String completePath = resolve(pathSegments);
		this.representGroup = completePath.charAt(completePath.length() - 1) == SEPARATOR.charAt(0);
		if (this.representGroup) {
			this.path = completePath;
			if (completePath.length() == 1) {
				this.simpleName = SEPARATOR;
			} else {
				this.simpleName = pathSegments[pathSegments.length - 2];
			}
			this.leafName = null;
		} else {
			this.path = completePath.substring(0, completePath.lastIndexOf(SEPARATOR) + 1);
			this.simpleName = pathSegments[pathSegments.length - 1];
			this.leafName = this.simpleName;
		}
		this.name = completePath;
	}

	/**
	 *
	 * @return path representing the immediate parent group
	 */
	public Path<S> parent() {
		Path<S> result = null;
		if (this.representGroup) {
			if (this.pathSegments.length == 1) {
				result = this;
			} else {
				final String[] parentPathSegments = new String[this.pathSegments.length - 1];
				System.arraycopy(this.pathSegments, 0, parentPathSegments, 0, this.pathSegments.length - 2);
				parentPathSegments[parentPathSegments.length - 1] = SEPARATOR;
				result = new Path<>(this.fileSystem, parentPathSegments);
			}
		} else {
			final String[] parentPathSegments = new String[this.pathSegments.length];
			System.arraycopy(this.pathSegments, 0, parentPathSegments, 0, this.pathSegments.length - 1);
			parentPathSegments[parentPathSegments.length - 1] = SEPARATOR;
			result = new Path<>(this.fileSystem, parentPathSegments);
		}
		return result;
	}

	/**
	 *
	 * @param name of the sub group
	 * @return path representing a immediate sub group, or null if this path is not
	 *         a group
	 * @throws PathLtException if name has the separator, or null or empty
	 */
	public Path<S> subGroup(final String name) throws PathLtException {
		if (StringUtil.isNullOrEmpty(name) || name.indexOf(SEPARATOR) != -1) {
			throw new PathLtException();
		}
		Path<S> result = null;
		if (this.representGroup) {
			final String[] subPathSegments = new String[this.pathSegments.length + 1];
			System.arraycopy(this.pathSegments, 0, subPathSegments, 0, this.pathSegments.length - 1);
			subPathSegments[subPathSegments.length - 2] = name;
			subPathSegments[subPathSegments.length - 1] = SEPARATOR;
			result = new Path<>(this.fileSystem, subPathSegments);
		}
		return result;
	}

	/**
	 *
	 * @param name of the sub leaf
	 * @return path representing a immediate sub leaf, or null if this path is not a
	 *         group
	 * @throws PathLtException if name has the separator, or null or empty
	 */
	public Path<S> subLeaf(final String name) throws PathLtException {
		if (StringUtil.isNullOrEmpty(name) || name.indexOf(SEPARATOR) != -1) {
			throw new PathLtException();
		}
		Path<S> result = null;
		if (this.representGroup) {
			final String[] subPathSegments = new String[this.pathSegments.length];
			System.arraycopy(this.pathSegments, 0, subPathSegments, 0, this.pathSegments.length - 1);
			subPathSegments[subPathSegments.length - 1] = name;
			result = new Path<>(this.fileSystem, subPathSegments);
		}
		return result;
	}

	/**
	 *
	 * @return if this path represents a group
	 */
	public boolean representGroup() {
		return this.representGroup;
	}

	/**
	 *
	 * @return if this path represents a leaf
	 */
	public boolean representLeaf() {
		return !this.representGroup;
	}

	/**
	 *
	 * @return the leaf name, or null if is not a leaf
	 */
	public String getLeafName() {
		return this.leafName;
	}

	/**
	 *
	 * @return the absolute path which represents this group, if this is a leaf then
	 *         is the absolute path of parent group.
	 */
	public String getAbsolutePath() {
		return this.path;
	}

	/**
	 *
	 * @return the absolute name of this path
	 */
	public String getName() {
		return this.name;
	}

	/**
	 *
	 * @return the name of this group or leaf
	 */
	public String getSimpleName() {
		return this.simpleName;
	}

	/**
	 *
	 * @return if this path represents the root path
	 */
	public boolean isRoot() {
		return this.path.equals(SEPARATOR);
	}

	/**
	 * Asserts if this path is parent of <code>potencialSub</code> at any level.
	 *
	 * @param potencialSub
	 * @return true only if is parent node
	 */
	public boolean isParent(final Path<S> potencialSub) {
		if (potencialSub == null || !this.representGroup) {
			return false;
		}
		if (potencialSub.representGroup) {
			if (this.pathSegments.length <= potencialSub.pathSegments.length) {
				return false;
			}
		} else {
			if (this.pathSegments.length < potencialSub.pathSegments.length) {
				return false;
			}
		}
		for (int i = 0; i < this.pathSegments.length - 1; i++) {
			if (!this.pathSegments[i].equals(potencialSub.pathSegments[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Asserts if this path is a sub path of <code>potencialParent</code> at any
	 * level.
	 *
	 * @param potencialParent
	 * @return true only if is sub node
	 */
	public boolean isSub(final Path<S> potencialParent) {
		if (potencialParent == null) {
			return false;
		}
		return potencialParent.isParent(this);
	}

	@Override
	public int compareTo(final Path<S> o) {
		return this.name.compareTo(o.name);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !Path.class.isInstance(obj)) {
			return false;
		}
		final Path<?> o = Path.class.cast(obj);
		return this.name.equals(o.name);
	}

}

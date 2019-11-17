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
import leitej.log.Logger;

/**
 *
 * @author Julio Leite
 */
public final class Path implements Comparable<Path>, Serializable {

	private static final long serialVersionUID = 2382425828263190884L;

	private static final Logger LOG = Logger.getInstance();

	public static final String SEPARATOR = "/";
	public static final Path ROOT = new Path();

//	private static String build(boolean representGroup, String... pathNames) throws PathLtException{
//		StringBuilder pathBuf = new StringBuilder();
//		if(pathNames != null && pathNames.length > 0){
//			for(String name : pathNames){
//				if(name.indexOf(SEPARATOR) != -1) throw new PathLtException();
//				pathBuf.append(SEPARATOR);
//				pathBuf.append(name);
//			}
//			if(representGroup) pathBuf.append(SEPARATOR);
//		}else{
//			pathBuf.append(SEPARATOR);
//		}
//		return pathBuf.toString();
//	}

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

	private final String[] pathSegments;
	private final String path;
	private final String name;
	private final String simpleName;
	private final String leafName;
	private final boolean representGroup;

	private Path() {
		this.pathSegments = new String[] { SEPARATOR };
		this.path = SEPARATOR;
		this.name = SEPARATOR;
		this.simpleName = SEPARATOR;
		this.leafName = null;
		this.representGroup = true;
	}

	public Path(final String absolutePath) throws PathLtException {
		this(parse(absolutePath));
	}

	private Path(final String[] pathSegments) {
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
//	public Path(boolean representGroup, String... pathNames) throws PathLtException{
//		this(build(representGroup, pathNames));
//	}

	public Path parent() {
		Path result = null;
		try {
			if (this.representGroup) {
				if (this.pathSegments.length == 1) {
					result = this;
				} else {
					final String[] parentPathSegments = new String[this.pathSegments.length - 1];
					System.arraycopy(this.pathSegments, 0, parentPathSegments, 0, this.pathSegments.length - 2);
					parentPathSegments[parentPathSegments.length - 1] = SEPARATOR;
					result = new Path(parentPathSegments);
				}
			} else {
				final String[] parentPathSegments = new String[this.pathSegments.length];
				System.arraycopy(this.pathSegments, 0, parentPathSegments, 0, this.pathSegments.length - 1);
				parentPathSegments[parentPathSegments.length - 1] = SEPARATOR;
				result = new Path(this.path);
			}
		} catch (final PathLtException e) {
			LOG.debug("#0", e);
		}
		return result;
	}

	public Path subGroup(final String name) throws PathLtException {
		if (name.indexOf(SEPARATOR) != -1) {
			throw new PathLtException();
		}
		Path result = null;
		if (this.representGroup) {
			final String[] subPathSegments = new String[this.pathSegments.length + 1];
			System.arraycopy(this.pathSegments, 0, subPathSegments, 0, this.pathSegments.length - 1);
			subPathSegments[subPathSegments.length - 2] = name;
			subPathSegments[subPathSegments.length - 1] = SEPARATOR;
			result = new Path(subPathSegments);
		}
		return result;
	}

	public Path subLeaf(final String name) throws PathLtException {
		if (name.indexOf(SEPARATOR) != -1) {
			throw new PathLtException();
		}
		Path result = null;
		if (this.representGroup) {
			final String[] subPathSegments = new String[this.pathSegments.length];
			System.arraycopy(this.pathSegments, 0, subPathSegments, 0, this.pathSegments.length - 1);
			subPathSegments[subPathSegments.length - 1] = name;
			result = new Path(subPathSegments);
		}
		return result;
	}

	public boolean representGroup() {
		return this.representGroup;
	}

	public boolean representLeaf() {
		return !this.representGroup;
	}

	/**
	 * Nome do ficheiro
	 *
	 * @return
	 */
	public String getLeafName() {
		return this.leafName;
	}

	/**
	 * Caminho completo
	 *
	 * @return
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Caminho completo com o nome da directoria ou ficheiro
	 *
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Nome da directoria ou ficheiro
	 *
	 * @return
	 */
	public String getSimpleName() {
		return this.simpleName;
	}

	public boolean isRoot() {
		return this.equals(ROOT);
	}

	/**
	 * Asserts if this path is parent of <code>potencialSub</code> at any level.
	 *
	 * @param potencialSub
	 * @return true only if is parent node
	 */
	public boolean isParent(final Path potencialSub) {
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
	public boolean isSub(final Path potencialParent) {
		if (potencialParent == null) {
			return false;
		}
		return potencialParent.isParent(this);
	}

	@Override
	public int compareTo(final Path o) {
		int result = this.path.compareTo(o.path);
		if (result == 0 && this.representGroup != o.representGroup) {
			if (this.representGroup) {
				result = -1;
			} else {
				result = 1;
			}
		}
		if (result == 0 && !this.representGroup) {
			this.simpleName.compareTo(o.simpleName);
		}
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !Path.class.isInstance(obj)) {
			return false;
		}
		final Path o = Path.class.cast(obj);
		return this.path.equals(o.path) && this.representGroup == o.representGroup
				&& this.simpleName.equals(o.simpleName);
	}

}

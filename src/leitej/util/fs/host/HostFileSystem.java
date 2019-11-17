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

package leitej.util.fs.host;

import java.io.File;
import java.math.BigInteger;
import java.util.regex.Pattern;

import leitej.Constant;
import leitej.exception.FileSystemLtRtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.log.Logger;
import leitej.util.data.CacheSoft;
import leitej.util.data.CacheWeak;
import leitej.util.fs.AbstractFileSystem;
import leitej.util.fs.Path;

/**
 *
 * @author Julio Leite
 */
public final class HostFileSystem extends AbstractFileSystem<HostFileSystem, HostFileSystemNode> {

	private static final long serialVersionUID = 819202508900947511L;

	private static final Logger LOG = Logger.getInstance();

	public static final String NAME_PATTERN = "^\\w{3,24}$";

	private final CacheSoft<String, HostFileSystemNode> dirCache = new CacheSoft<>();
	private final CacheWeak<String, HostFileSystemNode> fileCache = new CacheWeak<>();
	private final HostFileSystemNode rootNode;
	private final String baseNameDir;

	public HostFileSystem(final String name) throws IllegalArgumentLtRtException, FileSystemLtRtException {
		if (name == null || !Pattern.matches(NAME_PATTERN, name)) {
			throw new IllegalArgumentLtRtException();
		}
		final File baseNameFile = new File(Constant.DEFAULT_DATA_FILE_DIR, name);
		this.baseNameDir = baseNameFile.getAbsolutePath();
		this.rootNode = node(Path.ROOT);
		this.rootNode.create();
	}

	@Override
	public BigInteger totalSpace() {
		return BigInteger.valueOf(this.rootNode.getHostFile().getTotalSpace());
	}

	@Override
	public BigInteger usedSpace() {
		return BigInteger.valueOf(this.rootNode.getHostFile().getUsableSpace());
	}

	@Override
	public BigInteger freeSpace() {
		return BigInteger.valueOf(this.rootNode.getHostFile().getFreeSpace());
	}

	@Override
	public HostFileSystemNode rootNode() {
		return this.rootNode;
	}

	@Override
	public HostFileSystemNode node(final Path path) throws IllegalArgumentLtRtException {
		return node(path, null);
	}

	HostFileSystemNode node(final Path path, final File hostFile) throws IllegalArgumentLtRtException {
		if (path == null) {
			throw new IllegalArgumentLtRtException();
		}
		HostFileSystemNode result = null;
		if (path.representGroup()) {
			synchronized (this.fileCache) {
				result = this.fileCache.get(path.getName());
				if (result == null) {
					result = new HostFileSystemNode(this, path, hostFile);
					this.fileCache.set(path.getName(), result);
				}
			}
		} else {
			synchronized (this.dirCache) {
				result = this.dirCache.get(path.getName());
				if (result == null) {
					result = new HostFileSystemNode(this, path, hostFile);
					this.dirCache.set(path.getName(), result);
				}
			}
		}
		LOG.trace("#0", result);
		return result;
	}

	final String getBaseNameDir() {
		return this.baseNameDir;
	}

}

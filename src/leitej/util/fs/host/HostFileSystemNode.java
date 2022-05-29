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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import leitej.exception.FileSystemLtRtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.PathLtException;
import leitej.log.Logger;
import leitej.util.data.AutoRecall;
import leitej.util.fs.AbstractNode;
import leitej.util.fs.Path;
import leitej.util.stream.BinaryFileFractionInputStream;
import leitej.util.stream.BinaryFileFractionOutputStream;
import leitej.util.stream.FileUtil;
import leitej.util.stream.StreamUtil;

/**
 *
 * @author Julio Leite
 */
public final class HostFileSystemNode extends AbstractNode<HostFileSystem, HostFileSystemNode> {

	private static final long serialVersionUID = 8602146867141537574L;

	private static final Logger LOG = Logger.getInstance();

	private static final long REFRESH_LIST_PERIOD_MS = 5 * 1000; // 5 seconds

	private final HostFileSystem hfs;
	private final String hostFileName;
	private final File hostFile;
	private volatile boolean exists;
	private HostFileSystemNode[] subNodes;
	private final AutoRecall refreshList;

	HostFileSystemNode(final HostFileSystem hfs, final Path path, final File hostFile)
			throws IllegalArgumentLtRtException {
		super(hfs, path);
		this.hfs = hfs;
		this.hostFileName = hfs.getBaseNameDir() + path.getName();
		if (hostFile == null) {
			this.hostFile = new File(this.hostFileName);
		} else {
			this.hostFile = hostFile;
		}
		this.exists = false;
		this.subNodes = null;
		this.refreshList = new AutoRecall(REFRESH_LIST_PERIOD_MS);
	}

	@Override
	public synchronized HostFileSystemNode[] list() throws FileSystemLtRtException {
		if (!exists()) {
			throw new FileSystemLtRtException(this.toString());
		}
		if (isGroup() && (this.subNodes == null || this.refreshList.recall())) {
			try {
				final File[] listFiles = this.hostFile.listFiles();
				this.subNodes = new HostFileSystemNode[listFiles.length];
				File file;
				Path path;
				for (int i = 0; i < listFiles.length; i++) {
					file = listFiles[i];
					if (file.isDirectory()) {
						path = path().subGroup(file.getName());
					} else {
						path = path().subLeaf(file.getName());
					}
					this.subNodes[i] = this.hfs.node(path, file);
				}
			} catch (final PathLtException e) {
				this.subNodes = null;
				throw new ImplementationLtRtException(e);
			}
		}
		return this.subNodes;
	}

	@Override
	public synchronized boolean create() throws FileSystemLtRtException {
		try {
			boolean result;
			if (isGroup()) {
				result = this.hostFile.mkdir();
			} else {
				result = this.hostFile.createNewFile();
			}
			this.refreshList.init();
			initRefreshParentList();
			return result;
		} catch (final IOException e) {
			throw new FileSystemLtRtException(e);
		}
	}

	@Override
	public synchronized boolean remove() throws FileSystemLtRtException {
		if (path().representGroup()) {
			for (final HostFileSystemNode node : list()) {
				node.remove();
			}
			if (path().isRoot()) {
				return false;
			}
		}
		final boolean result = this.hostFile.delete();
		this.refreshList.init();
		initRefreshParentList();
		return result;
	}

	private void initRefreshParentList() {
		this.parent().refreshList.init();
	}

	@Override
	public synchronized boolean exists() throws FileSystemLtRtException {
		if (this.hostFile.exists() != this.exists) {
			if (this.exists) {
				this.exists = false;
			} else {
				this.exists = ((path().representGroup() && this.hostFile.isDirectory())
						|| (path().representLeaf() && this.hostFile.isFile()));
			}
		}
		return this.exists;
	}

	@Override
	public synchronized long createTime() throws FileSystemLtRtException {
		if (!exists()) {
			throw new FileSystemLtRtException(this.toString());
		}
		return this.hostFile.lastModified();
	}

	@Override
	public synchronized long changeTime() throws FileSystemLtRtException {
		if (!exists()) {
			throw new FileSystemLtRtException(this.toString());
		}
		return this.hostFile.lastModified();
	}

	@Override
	public synchronized long size() throws FileSystemLtRtException {
		if (!exists()) {
			throw new FileSystemLtRtException(this.toString());
		}
		return this.hostFile.length();
	}

	@Override
	public synchronized byte[] md5() throws FileSystemLtRtException {
		if (!exists()) {
			throw new FileSystemLtRtException(this.toString());
		}
		try {
			return FileUtil.md5(this.hostFile);
		} catch (final SecurityException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchAlgorithmException e) {
			throw new ImplementationLtRtException(e);
		} catch (final FileNotFoundException e) {
			throw new ImplementationLtRtException(e);
		} catch (final IOException e) {
			throw new FileSystemLtRtException(e);
		}
	}

	@Override
	public synchronized InputStream read(final Long offset, final Long length) throws FileSystemLtRtException {
		if (!exists()) {
			throw new FileSystemLtRtException(this.toString());
		}
		InputStream result;
		final long offsetPrimitive = (offset == null) ? 0 : offset;
		try {
			if (length != null) {
				result = new BinaryFileFractionInputStream(this.hostFile, offsetPrimitive, length);
			} else {
				result = new BinaryFileFractionInputStream(this.hostFile, offsetPrimitive);
			}
		} catch (final IOException e) {
			throw new FileSystemLtRtException(e);
		}
		return result;
	}

	@Override
	public synchronized void write(final InputStream in, final Long offset, final Long length)
			throws FileSystemLtRtException {
		if (!exists()) {
			throw new FileSystemLtRtException(this.toString());
		}
		OutputStream os = null;
		try {
			final long offsetPrimitive = (offset == null) ? 0 : offset;
			if (length != null) {
				os = new BinaryFileFractionOutputStream(this.hostFile, offsetPrimitive, length, false);
			} else {
				os = new BinaryFileFractionOutputStream(this.hostFile, offsetPrimitive, false);
			}
			StreamUtil.pipe(in, os, true);
		} catch (final IOException e) {
			throw new FileSystemLtRtException(e);
		} finally {
			// TODO: procurar todos os 'finally' e colocar como este para o caso de haver
			// exception
			try {
				if (os != null) {
					os.close();
				}
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
				LOG.error("#0", e);
			}
		}
	}

	@Override
	public synchronized void truncate(final Long offset) throws FileSystemLtRtException {
		if (!exists()) {
			throw new FileSystemLtRtException(this.toString());
		}
		try {
			if (!FileUtil.setFileSize(this.hostFile, offset)) {
				throw new FileSystemLtRtException();
			}
		} catch (final IllegalArgumentException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NullPointerException e) {
			throw new ImplementationLtRtException(e);
		} catch (final SecurityException e) {
			throw new ImplementationLtRtException(e);
		} catch (final IOException e) {
			throw new FileSystemLtRtException(e);
		}
	}

	@Override
	public synchronized HostFileSystemNode moveTo(final Path dest) throws FileSystemLtRtException {
		// TODO: optimisation
		// fileutil.renameTo
//		hostFile.renameTo(dest);
		// if the rename return false then try by 'super.moveTo(dest)'
		return super.moveTo(dest);
	}
//	rename(String from, StringBuilder to) {
//		String renameFrom = convertToSysPath(resolvePath(from));
//		String renameTo = convertToSysPath(resolvePath(to.toString()));
//		File fileFrom = new File(renameFrom);
//		if(!fileFrom.exists()) return 1;
//		File fileTo = new File(renameTo);
//		if(fileTo.exists()) return 2;
//		fileFrom.renameTo(fileTo);
//		return 0;
//	}

	final File getHostFile() {
		return this.hostFile;
	}

}

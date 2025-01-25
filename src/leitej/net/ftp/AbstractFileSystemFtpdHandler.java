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

package leitej.net.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import leitej.exception.FileSystemLtRtException;
import leitej.exception.PathLtException;
import leitej.log.Logger;
import leitej.util.fs.AbstractFileSystem;
import leitej.util.fs.AbstractNode;
import leitej.util.fs.Path;
import leitej.util.stream.StreamUtil;

/**
 *
 * @author Julio Leite
 */
public abstract class AbstractFileSystemFtpdHandler<S extends AbstractFileSystem<S, N>, N extends AbstractNode<S, N>>
		extends AbstractFtpServerHandler {

	private static final Logger LOG = Logger.getInstance();

	private static final SimpleDateFormat SDF_DATE = new SimpleDateFormat("MM-dd-yy hh:mma");

	private final S fileSystem;
	private N currentNode;

	protected AbstractFileSystemFtpdHandler(final S fileSystem) {
		super();
		this.fileSystem = fileSystem;
		this.currentNode = fileSystem.rootNode();
	}

//	@Override
//	protected String systemId(){
//		return "UNIX Type: I";
//	}

	@Override
	protected int changeWorkingDirectory(final StringBuilder args) {
		try {
			if (args.length() == 0) {
				this.currentNode = this.fileSystem.rootNode();
			} else {
				this.currentNode = resolveNode(args);
			}
		} catch (final IOException e) {
			LOG.trace("#0", e);
			return 3;
		}
		LOG.trace("#0", this.currentNode);
		return 0;
	}

	@Override
	protected int changeToParentDirectory() {
		this.currentNode = this.currentNode.parent();
		LOG.trace("#0", this.currentNode);
		return 0;
	}

	@Override
	protected void reset() {
		this.currentNode = this.fileSystem.rootNode();
		LOG.trace("#0", this.currentNode);
	}

	@Override
	protected int isFile(final StringBuilder args) {
		N node;
		try {
			node = resolveNode(args);
			if (node.isRoot()) {
				return 1;
			}
			node = node.parent().sub(node.path().getSimpleName());
		} catch (final IOException e) {
			LOG.trace("#0", e);
			return 2;
		}
		if (node == null || !node.exists()) {
			return 2;
		}
		if (!node.isLeaf()) {
			return 1;
		}
		return 0;
	}

	@Override
	protected int sendBinaryFile(final BufferedOutputStream out, final StringBuilder args, final Long offset) {
		N node;
		try {
			node = resolveNode(args);
			LOG.trace("#0", node);
			if (!node.exists()) {
				return 2;
			}
			if (!node.isLeaf()) {
				return 1;
			}
		} catch (final FileSystemLtRtException e) {
			LOG.trace("#0", e);
			return 3;
		} catch (final IOException e) {
			LOG.trace("#0", e);
			return 3;
		}
		InputStream is = null;
		try {
			is = node.read(offset);
			StreamUtil.pipe(is, out, true);
		} catch (final FileSystemLtRtException e) {
			LOG.trace("#0", e);
			return 3;
		} catch (final IOException e) {
			LOG.trace("#0", e);
			return 3;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final IOException e) {
					LOG.error("#0", e);
				}
			}
		}
		return 0;
	}

	@Override
	protected int isDirectory(final StringBuilder args) {
		N node;
		try {
			node = resolveNode(args);
			if (node.isRoot()) {
				return 0;
			}
			node = node.parent().sub(node.path().getSimpleName());
		} catch (final IOException e) {
			LOG.trace("#0", e);
			return 2;
		}
		if (node == null || !node.exists()) {
			return 2;
		}
		if (!node.isGroup()) {
			return 1;
		}
		return 0;
	}

	@Override
	protected int receiveBinaryFile(final BufferedInputStream in, final StringBuilder args, final Long offset) {
		N node;
		try {
			node = resolveNode(args);
			LOG.trace("#0", node);
		} catch (final IOException e) {
			LOG.trace("#0", e);
			return 1;
		}
		try {
			if (!node.exists()) {
				node.create();
			}
			node.write(in, offset);
		} catch (final FileSystemLtRtException e) {
			LOG.trace("#0", e);
			return 2;
		}
		return 0;
	}

	@Override
	protected int rename(final String from, final StringBuilder to) {
		N nodeFrom;
		try {
			nodeFrom = resolveNode(from);
			if (nodeFrom.isRoot()) {
				return 2;
			}
			nodeFrom = nodeFrom.parent().sub(nodeFrom.path().getSimpleName());
			LOG.trace("#0", nodeFrom);
		} catch (final IOException e) {
			LOG.trace("#0", e);
			return 1;
		} catch (final FileSystemLtRtException e) {
			LOG.trace("#0", e);
			return 2;
		}
		try {
			if (nodeFrom == null || !nodeFrom.exists()) {
				return 1;
			}
			if (nodeFrom.isGroup()) {
				to.append(Path.SEPARATOR);
			}
			nodeFrom.moveTo(resolvePath(to.toString()));
		} catch (final PathLtException e) {
			LOG.trace("#0", e);
			return 2;
		} catch (final FileSystemLtRtException e) {
			LOG.trace("#0", e);
			return 2;
		}
		return 0;
	}

	@Override
	protected int delete(final StringBuilder args) {
		try {
			final N node = resolveNode(args);
			if (!node.exists()) {
				return 1;
			}
			if (!node.isLeaf()) {
				return 3;
			}
			if (!node.remove()) {
				return 2;
			}
			LOG.trace("#0", node);
		} catch (final FileSystemLtRtException e) {
			LOG.trace("#0", e);
			return 2;
		} catch (final IOException e) {
			LOG.trace("#0", e);
			return 1;
		}
		return 0;
	}

	@Override
	protected int removeDirectory(final StringBuilder args) {
		try {
			final N node = resolveNode(args);
			if (!node.exists()) {
				return 1;
			}
			if (!node.isGroup()) {
				return 2;
			}
			if (!node.remove()) {
				return 3;
			}
			LOG.trace("#0", node);
		} catch (final FileSystemLtRtException e) {
			LOG.trace("#0", e);
			return 3;
		} catch (final IOException e) {
			LOG.trace("#0", e);
			return 4;
		}
		return 0;
	}

	@Override
	protected int makeDirectory(final StringBuilder args) {
		try {
			final N node = resolveNode(args);
			if (node.exists()) {
				return 1;
			}
			if (!node.isGroup()) {
				return 1;
			}
			if (!node.create()) {
				return 2;
			}
			LOG.trace("#0", node);
		} catch (final FileSystemLtRtException e) {
			LOG.trace("#0", e);
			return 3;
		} catch (final IOException e) {
			LOG.trace("#0", e);
			return 3;
		}
		return 0;
	}

	@Override
	protected String pwd() {
		return this.currentNode.path().getName();
	}

	@Override
	protected Object[] getList(final StringBuilder args) {
		N[] result = null;
		try {
			result = resolveNode(args).list();
		} catch (final IOException e) {
			LOG.trace("#0", e);
		}
		LOG.trace("#0", result.length);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void writeFileList(final PrintWriter out, final Object[] fileArray) {
		// out.println("total " + ((fileArray != null)? fileArray.length : 0));
		N node;
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fileArray.length; i++) {
			sb.setLength(0);
			try {
				node = (N) fileArray[i];
				sb.append(SDF_DATE.format(node.changeTime()));
				sb.append(" ");
				sb.append(((node.isGroup()) ? "<DIR>" : " "));
				sb.append(" ");
				sb.append(((node.isGroup()) ? " " : node.size()));
				sb.append(" ");
				sb.append(node.path().getSimpleName());
				out.println(sb.toString());
			} catch (final FileSystemLtRtException e) {
				LOG.debug("#0", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void writeFileNameList(final PrintWriter out, final Object[] fileArray) {
		for (int i = 0; i < fileArray.length; i++) {
			out.println(((N) fileArray[i]).path().getSimpleName());
		}
	}

	private N resolveNode(final StringBuilder arg) throws IOException {
		return resolveNode(arg.toString());
	}

	private N resolveNode(final String arg) throws IOException {
		N result;
		if (arg.length() == 0) {
			result = this.currentNode;
		} else {
			try {
				result = this.fileSystem.node(resolvePath(arg));
			} catch (final PathLtException e) {
				LOG.trace("#0", e);
				throw new IOException(e);
			}
		}
		return result;
	}

	private Path<S> resolvePath(final String arg) throws PathLtException {
		Path<S> result;
		if (arg.length() == 0 || arg.charAt(0) != Path.SEPARATOR.charAt(0)) {
			result = new Path<>(this.fileSystem, this.currentNode.path().getAbsolutePath() + arg);
		} else {
			result = new Path<>(this.fileSystem, arg);
		}
		return result;
	}

}

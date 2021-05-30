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

package leitej.ltm;

import java.io.IOException;
import java.sql.SQLException;

import leitej.exception.ClosedLtRtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.exception.SeppukuLtRtException;
import leitej.exception.XmlInvalidLtException;
import leitej.util.AgnosticUtil;
import leitej.util.data.AbstractObjectPool;
import leitej.util.data.Invoke;
import leitej.util.machine.ShutdownHookUtil;
import leitej.xml.om.Xmlom;

/**
 * @author Julio Leite
 *
 */
public final class DataMemoryPool extends AbstractObjectPool<DataMemoryConnection> {

	private static final long serialVersionUID = 4645255425158742790L;

	static final DataMemoryConfig CONFIG;
	private static final DataMemoryPool INSTANCE;

	static {
		final DataMemoryConfig[] defaultContent = new DataMemoryConfig[] {
				Xmlom.newInstance(DataMemoryConfig.class) };
		defaultContent[0].setMaxConnections(20);
		defaultContent[0].setAutoForgetsInterfaceComponentMisses(false);
		try {
			CONFIG = Xmlom.getConfig(DataMemoryConfig.class, defaultContent).get(0);
		} catch (NullPointerException | SecurityException | XmlInvalidLtException | IOException e) {
			throw new SeppukuLtRtException(e);
		}
		//
		INSTANCE = new DataMemoryPool(CONFIG.getMaxConnections());
		try {
			ShutdownHookUtil.addToLast(new Invoke(INSTANCE, AgnosticUtil.getMethod(INSTANCE, "close")));
		} catch (IllegalStateLtRtException | IllegalArgumentLtRtException | NullPointerException
				| IllegalArgumentException | SecurityException | NoSuchMethodException e) {
			throw new SeppukuLtRtException(e);
		}
	}

	static DataMemoryPool getInstance() {
		return INSTANCE;
	}

	private DataMemoryPool(final int maxObjects) throws IllegalArgumentException {
		super(maxObjects);
	}

	@Override
	protected DataMemoryConnection poll() throws ClosedLtRtException, ObjectPoolLtException, InterruptedException {
		return super.poll();
	}

	@Override
	protected void offer(final DataMemoryConnection conn) throws IllegalArgumentException, InterruptedException {
		super.offer(conn);
	}

	@Override
	protected DataMemoryConnection newObject() throws ObjectPoolLtException {
		try {
			return new DataMemoryConnection();
		} catch (final SQLException e) {
			throw new ObjectPoolLtException(e);
		}
	}

	@Override
	protected boolean isInactive(final DataMemoryConnection obj) {
		return obj.isInactive();
	}

	@Override
	protected void deactivate(final DataMemoryConnection obj) throws ObjectPoolLtException {
		try {
			obj.close();
		} catch (final SQLException e) {
			throw new ObjectPoolLtException(e);

		}
	}

	@Override
	public void close() throws ObjectPoolLtException {
		// this method will be called on ShutdownHookUtil.addToLast
		super.close();
		try {
			DataMemoryConnection.hibernate();
		} catch (final SQLException e) {
			throw new ObjectPoolLtException(e);
		}
	}

}

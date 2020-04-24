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

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;

import leitej.exception.ClosedLtRtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.log.Logger;
import leitej.ltm.dynamic.DescriptorData;
import leitej.ltm.exception.LtmLtRtException;
import leitej.util.AgnosticUtil;
import leitej.util.data.Invoke;
import leitej.util.data.InvokeItf;
import leitej.util.machine.ShutdownHookUtil;

/**
 * Abstract manager for Long Term Memory (LTM) concept.
 *
 * <p>
 * The constructor will put a call to
 * {@link leitej.ltm.AbstractLongTermMemory#close() close()} in the
 * {@link leitej.util.machine.ShutdownHookUtil#addToLast(InvokeItf)
 * ShutdownHookUtil.addToLast(InvokeItf)}.
 * </p>
 *
 * @author Julio Leite
 */
abstract class AbstractLongTermMemory {

	private static final Logger LOG = Logger.getInstance();

	private static final DataProxy DATA_PROXY = DataProxy.getInstance();

	private final ConnectionPoolDB connectionPool;

	/**
	 * .<br/>
	 * <br/>
	 * A call to {@link leitej.ltm.AbstractLongTermMemory#close() close()} is put in
	 * the {@link leitej.util.machine.ShutdownHookUtil#addToLast(InvokeItf)
	 * ShutdownHookUtil.addToLast(InvokeItf)}.
	 *
	 * @param maxConnections allowed in parallel mode
	 * @throws IllegalArgumentException if <code>maxConnections</code> is less then
	 *                                  1
	 */
	protected AbstractLongTermMemory(final int maxConnections) throws IllegalArgumentException {
		this.connectionPool = new ConnectionPoolDB(this, maxConnections);
		try {
			// TODO: um metodo de x em x tempo fazer o shutdown da bd com compact
			ShutdownHookUtil.addToLast(new Invoke(this, AgnosticUtil.getMethod(this, METHOD_CLOSE)));
		} catch (final NullPointerException e) {
			throw new ImplementationLtRtException(e);
		} catch (final IllegalArgumentException e) {
			throw new ImplementationLtRtException(e);
		} catch (final SecurityException e) {
			throw new ImplementationLtRtException(e);
		} catch (final NoSuchMethodException e) {
			throw new ImplementationLtRtException(e);
		}
	}

	/**
	 * Attempts to establish a connection.
	 *
	 * @return a connection to the URL
	 * @throws SQLException if a database access error occurs
	 */
	protected abstract Connection getDriverConnection() throws SQLException;

	/**
	 * Approves if a determined exception given by a connection should invalidates
	 * it.
	 *
	 * @param e the exception to approve
	 * @return approbation
	 */
	protected abstract boolean invalidatesConnection(SQLException e);

	/**
	 * Gets a connection from the pool.
	 *
	 * @return connection
	 * @throws ClosedLtRtException   if already close
	 * @throws ObjectPoolLtException if can't instantiate a new connection
	 * @throws InterruptedException  if interrupted while waiting for the connection
	 */
	ConnectionDB getConnection() throws ClosedLtRtException, ObjectPoolLtException, InterruptedException {
		return this.connectionPool.poll();
	}

	/**
	 * Erases all memory.
	 * 
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close <br/>
	 *                          +Cause ObjectPoolLtException if can't instantiate a
	 *                          new connection <br/>
	 *                          +Cause InterruptedException if interrupted while
	 *                          waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection
	 */
	public synchronized void erase() throws LtmLtRtException {
		try {
			final ConnectionDB conn = getConnection();
			try {
				conn.addBatch(generateDropTables());
				conn.executeBatch();
				conn.release();
				LtmBinary.deleteAll();
			} catch (final SQLException e) {
				try {
					conn.releaseRollback();
				} catch (final SQLException e1) {
					LOG.debug("#0", e);
				}
				throw new LtmLtRtException(e);
			}
		} catch (final ClosedLtRtException e) {
			throw new LtmLtRtException(e);
		} catch (final ObjectPoolLtException e) {
			throw new LtmLtRtException(e);
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		}
		LOG.warn("Erased All Long Term Memory");
	}

	private static final String METHOD_CLOSE = "close";

	/**
	 * .<br/>
	 * <br/>
	 * This method can only be called from shutdown hook.
	 *
	 * @throws IllegalAccessException if the thread is not from shutdown hook
	 */
	public void close() throws IllegalAccessException {
		if (!ShutdownHookUtil.isCurrentThreadFrom()) {
			throw new IllegalAccessException();
		}
		// TODO: fazer uma verificacao parcial sequencial dos ficheiros criados pelo
		// binarystream no disco a ver se ainda sao referenciados
		LOG.debug("closing ltm manager");
		LtmBinary.execDelete();
		this.connectionPool.close();
	}

	/**
	 *
	 * @param cLtm
	 * @return a unique identifier to the singleton instance (that can be used when
	 *         calling 'fetch' or 'newObject')
	 * @throws IllegalArgumentLtRtException if the data in parameter represents a
	 *                                      configuration not allowed or already
	 *                                      registered data with the same name
	 * @throws LtmLtRtException             <br/>
	 *                                      +Cause ClosedLtRtException if long term
	 *                                      memory already close <br/>
	 *                                      +Cause ObjectPoolLtException if can't
	 *                                      instantiate a new connection <br/>
	 *                                      +Cause InterruptedException if
	 *                                      interrupted while waiting for a
	 *                                      connection <br/>
	 *                                      +Cause SQLException if a database access
	 *                                      error occurs, this method is called on a
	 *                                      closed connection, if one of the
	 *                                      commands sent to the database fails to
	 *                                      execute properly or attempts to return
	 *                                      an unexpected result
	 */
	public final <T extends LtmObjectModelling> String registry(final Class<T> cLtm)
			throws IllegalArgumentLtRtException, LtmLtRtException {
		try {
			return ElementTable.registry(cLtm);
		} catch (final ClosedLtRtException e) {
			throw new LtmLtRtException(e);
		} catch (final ObjectPoolLtException e) {
			throw new LtmLtRtException(e);
		} catch (final SQLException e) {
			throw new LtmLtRtException(e);
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		}
	}

	/**
	 *
	 * @param dData
	 * @return a unique identifier to use when calling 'fetch' or 'newObject' to get
	 *         the singleton instance
	 * @throws IllegalArgumentLtRtException if the data in parameter represents a
	 *                                      configuration not allowed or already
	 *                                      registered data with the same name
	 * @throws LtmLtRtException             <br/>
	 *                                      +Cause ClosedLtRtException if long term
	 *                                      memory already close <br/>
	 *                                      +Cause ObjectPoolLtException if can't
	 *                                      instantiate a new connection <br/>
	 *                                      +Cause InterruptedException if
	 *                                      interrupted while waiting for a
	 *                                      connection <br/>
	 *                                      +Cause SQLException if a database access
	 *                                      error occurs, this method is called on a
	 *                                      closed connection, if one of the
	 *                                      commands sent to the database fails to
	 *                                      execute properly or attempts to return
	 *                                      an unexpected result
	 */
	public final String registry(final DescriptorData dData) throws IllegalArgumentLtRtException, LtmLtRtException {
		try {
			return ElementTable.registry(dData);
		} catch (final ClosedLtRtException e) {
			throw new LtmLtRtException(e);
		} catch (final ObjectPoolLtException e) {
			throw new LtmLtRtException(e);
		} catch (final SQLException e) {
			throw new LtmLtRtException(e);
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		}
	}

	/**
	 *
	 * @param cLtm
	 * @param id
	 * @return
	 * @throws IllegalArgumentLtRtException if try to get a ElementTable not
	 *                                      registered
	 * @throws LtmLtRtException             <br/>
	 *                                      +Cause ClosedLtRtException if long term
	 *                                      memory already close <br/>
	 *                                      +Cause ObjectPoolLtException if can't
	 *                                      instantiate a new connection <br/>
	 *                                      +Cause InterruptedException if
	 *                                      interrupted while waiting for a
	 *                                      connection <br/>
	 *                                      +Cause SQLException if a database access
	 *                                      error occurs, this method is called on a
	 *                                      closed connection
	 */
	public final <T extends LtmObjectModelling> T fetch(final Class<T> cLtm, final Long id)
			throws LtmLtRtException, IllegalArgumentLtRtException {
		try {
			return DATA_PROXY.getTableInstance(ElementTable.getInstance(cLtm), id);
		} catch (final ClosedLtRtException e) {
			throw new LtmLtRtException(e);
		} catch (final ObjectPoolLtException e) {
			throw new LtmLtRtException(e);
		} catch (final SQLException e) {
			throw new LtmLtRtException(e);
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		}
	}

	/**
	 *
	 * @param dData
	 * @param id
	 * @return
	 * @throws IllegalArgumentLtRtException if try to get a ElementTable not
	 *                                      registered
	 * @throws LtmLtRtException             <br/>
	 *                                      +Cause ClosedLtRtException if long term
	 *                                      memory already close <br/>
	 *                                      +Cause ObjectPoolLtException if can't
	 *                                      instantiate a new connection <br/>
	 *                                      +Cause InterruptedException if
	 *                                      interrupted while waiting for a
	 *                                      connection <br/>
	 *                                      +Cause SQLException if a database access
	 *                                      error occurs, this method is called on a
	 *                                      closed connection
	 */
	public final <T extends LtmObjectModelling> T fetch(final String dData, final Long id)
			throws LtmLtRtException, IllegalArgumentLtRtException {
		try {
			return DATA_PROXY.getTableInstance(ElementTable.getInstance(dData), id);
		} catch (final ClosedLtRtException e) {
			throw new LtmLtRtException(e);
		} catch (final ObjectPoolLtException e) {
			throw new LtmLtRtException(e);
		} catch (final SQLException e) {
			throw new LtmLtRtException(e);
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		}
	}

	/**
	 *
	 * @param cLtm
	 * @return
	 */
	public final <T extends LtmObjectModelling> T newObject(final Class<T> cLtm) {
		return DATA_PROXY.newTableInstance(ElementTable.getInstance(cLtm));
	}

	/**
	 *
	 * @param cLtm
	 * @return
	 */
	public final <T extends LtmObjectModelling> T newObject(final String dData) {
		return DATA_PROXY.newTableInstance(ElementTable.getInstance(dData));
	}

	/**
	 *
	 * @param ltmObject
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close +Cause ObjectPoolLtException if can't
	 *                          instantiate a new connection +Cause
	 *                          InterruptedException if interrupted while waiting
	 *                          for lock or waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection
	 */
	public final <T extends LtmObjectModelling> void save(final T... ltmObject) throws LtmLtRtException {
		if (ltmObject != null && ltmObject.length > 0) {
			final DataProxyHandler[] tihArray = new DataProxyHandler[ltmObject.length];
			for (int i = 0; i < ltmObject.length; i++) {
				tihArray[i] = DATA_PROXY.getInvocationHandler(ltmObject[i]);
			}
			DataProxyHandler.save(tihArray);
		}
	}

	/**
	 *
	 * @param ltmObject
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close +Cause ObjectPoolLtException if can't
	 *                          instantiate a new connection +Cause
	 *                          InterruptedException if interrupted while waiting
	 *                          for lock or waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection
	 */
	public final <T extends LtmObjectModelling> void remove(final T... ltmObject) throws LtmLtRtException {
		if (ltmObject != null && ltmObject.length > 0) {
			final DataProxyHandler[] tihArray = new DataProxyHandler[ltmObject.length];
			for (int i = 0; i < ltmObject.length; i++) {
				tihArray[i] = DATA_PROXY.getInvocationHandler(ltmObject[i]);
			}
			DataProxyHandler.remove(tihArray);
		}
	}

	/**
	 *
	 * @param ltmObject
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close +Cause ObjectPoolLtException if can't
	 *                          instantiate a new connection +Cause
	 *                          InterruptedException if interrupted while waiting
	 *                          for lock or waiting for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection
	 */
	public final <T extends LtmObjectModelling> void refresh(final T... ltmObject) throws LtmLtRtException {
		if (ltmObject != null && ltmObject.length > 0) {
			final DataProxyHandler[] tihArray = new DataProxyHandler[ltmObject.length];
			for (int i = 0; i < ltmObject.length; i++) {
				tihArray[i] = DATA_PROXY.getInvocationHandler(ltmObject[i]);
			}
			DataProxyHandler.refresh(tihArray);
		}
	}

	abstract String byteAlias(String columnDefinition) throws IllegalArgumentLtRtException;

	abstract String shortAlias(String columnDefinition) throws IllegalArgumentLtRtException;

	abstract String integerAlias(String columnDefinition) throws IllegalArgumentLtRtException;

	abstract String longAlias(String columnDefinition) throws IllegalArgumentLtRtException;

	abstract String bigDecimalAlias(String columnDefinition, int precision, int scale)
			throws IllegalArgumentLtRtException;

	abstract String doubleAlias(String columnDefinition, int precision, int scale) throws IllegalArgumentLtRtException;

	abstract String booleanAlias(String columnDefinition) throws IllegalArgumentLtRtException;

	abstract String stringAlias(String columnDefinition, int length) throws IllegalArgumentLtRtException;

	abstract String byteArrayAlias(String columnDefinition, int maxArrayLength) throws IllegalArgumentLtRtException;

	abstract String shortArrayAlias(String columnDefinition, int maxArrayLength) throws IllegalArgumentLtRtException;

	abstract String integerArrayAlias(String columnDefinition, int maxArrayLength) throws IllegalArgumentLtRtException;

	abstract String longArrayAlias(String columnDefinition, int maxArrayLength) throws IllegalArgumentLtRtException;

	abstract String booleanArrayAlias(String columnDefinition, int maxArrayLength) throws IllegalArgumentLtRtException;

	abstract Array createArraySQL(ElementColumn ec, Object[] array) throws IllegalArgumentLtRtException;

	abstract int byteTypes();

	abstract int shortTypes();

	abstract int integerTypes();

	abstract int longTypes();

	abstract int bigDecimalTypes();

	abstract int doubleTypes();

	abstract int booleanTypes();

	abstract int stringTypes();

	abstract int arrayTypes();

	abstract String generateDropTables();

	/**
	 * ... This generation can only make references to the same table and can't make
	 * constrains to it or other
	 *
	 * @param elementTable
	 * @return
	 */
	abstract String[] generateCreateTableSQL(ElementTable elementTable);

	abstract String generateParameterizedSelectIdSQL(ElementTable elementTable);

	abstract String generateParameterizedSelectCountMappedIdSQL(ElementTable elementTable, String mappedColumn);

	abstract long castCountResult(Object countValue);

	abstract String generateParameterizedSelectLimitedMappedIdSQL(ElementTable elementTable, String mappedColumn);

	abstract boolean isParameterizedLimitArgsBeforeColumns();

	abstract int absoluteNumberOfFirstRow();

	abstract String generateParameterizedSelectCountHasIdAndMappedIdSQL(ElementTable elementTable, String mappedColumn);

	abstract String generateParameterizedInsertSQL(ElementTable elementTable);

	abstract String generateParameterizedUpdateIdSQL(ElementTable elementTable);

	abstract String generateParameterizedUpdateMappedIdListSQL(ElementTable elementTable, String mappedColumn);

	abstract String generateParameterizedRemoveIdSQL(ElementTable elementTable);

	abstract String generateParameterizedRemoveMappedIdSQL(ElementTable elementTable, String mappedColumn);

	abstract <T extends LtmObjectModelling> QueryResult<T> find(QueryEntrance<T> tableEntrance, QueryFilterBy where,
			QueryOrderBy orderBy);

	abstract String generateUniqueTableAlias();

}

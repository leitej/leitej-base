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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import leitej.exception.ClosedLtRtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.log.Logger;
import leitej.ltm.dynamic.Data;
import leitej.ltm.dynamic.Field;
import leitej.ltm.dynamic.Value;
import leitej.ltm.exception.LtmLtRtException;
import leitej.util.DateUtil;
import leitej.util.ThreadLock;
import leitej.util.data.AbstractDataProxyHandler;
import leitej.util.data.XmlomUtil;

/**
 *
 * @author Julio Leite
 */
final class DataProxyHandler extends AbstractDataProxyHandler<LtmObjectModelling>
		implements Comparable<LtmObjectModelling> {

	private static final long serialVersionUID = -7481755987901672218L;

	private static final Logger LOG = Logger.getInstance();
	private static final DataProxy DATA_PROXY = DataProxy.getInstance();
	private static final AbstractLongTermMemory LTM_MANAGER = LongTermMemory.getInstance();

	private static final String METHOD_NAME_COMPARE_TO = "compareTo";
	private static final String METHOD_NAME_IS_NEW = "isNew";
	private static final String METHOD_NAME_SAVE = "save";
	private static final String METHOD_NAME_REMOVE = "remove";
	private static final String METHOD_NAME_REFRESH = "refresh";

	private static final String DYN_METHOD_NAME_GET = "get";
	private static final String DYN_METHOD_NAME_PUT = "put";
	private static final String DYN_METHOD_NAME_GET_LINK = "getLink";
	private static final String DYN_METHOD_NAME_PUT_LINK = "putLink";
	private static final String DYN_METHOD_NAME_GET_LINK_SET = "getLinkSet";
	private static final String DYN_METHOD_NAME_GET_BINARY_STREAM = "getBinaryStream";

	// TODO: implement a lock only for the tables that the cascade obligates
	private static final ThreadLock DB_ACCESS = new ThreadLock();

	/**
	 *
	 * @param tihArray
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
	static void save(final DataProxyHandler... tihArray) throws LtmLtRtException {
		try {
			DB_ACCESS.lock();
			final ConnectionDB conn = LTM_MANAGER.getConnection();
			final List<Object> persistListLoop = new ArrayList<>();
			final List<Object> updateListLoop = new ArrayList<>();
			final List<Object> changeList = new ArrayList<>();
			try {
				for (int i = 0; i < tihArray.length; i++) {
					tihArray[i].save(conn, persistListLoop, null, changeList);
				}
				for (int i = 0; i < tihArray.length; i++) {
					tihArray[i].save(conn, null, updateListLoop, changeList);
				}
				if (!changeList.isEmpty()) {
					conn.executeParameterizedBatchGroup();
					conn.release();
				} else {
					conn.releaseRollback();
				}
			} catch (final SQLException e) {
				// TODO: this block has to be executed for other exceptions also, if not will
				// not unlocks some lists
				try {
					conn.releaseRollback();
				} catch (final SQLException e1) {
					LOG.debug("#0", e1);
				}
				for (final Object tih : changeList) {
					if (ScaledSet.class.isInstance(tih)) {
						ScaledSet.class.cast(tih).saveFail();
					} else if (DataProxyHandler.class.isInstance(tih)) {
						DataProxyHandler.class.cast(tih).saveFail();
					} else {
						throw new ImplementationLtRtException();
					}
				}
				throw new LtmLtRtException(e);
			}
			for (final Object tih : changeList) {
				if (ScaledSet.class.isInstance(tih)) {
					ScaledSet.class.cast(tih).saveSuccess();
				} else if (DataProxyHandler.class.isInstance(tih)) {
					DataProxyHandler.class.cast(tih).saveSuccess();
				} else {
					throw new ImplementationLtRtException();
				}
			}
		} catch (final ClosedLtRtException e) {
			throw new LtmLtRtException(e);
		} catch (final ObjectPoolLtException e) {
			throw new LtmLtRtException(e);
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		} finally {
			DB_ACCESS.unlock();
		}
	}

	/**
	 *
	 * @param tihArray
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
	static void remove(final DataProxyHandler... tihArray) throws LtmLtRtException {
		try {
			DB_ACCESS.lock();
			final ConnectionDB conn = LTM_MANAGER.getConnection();
			final List<Object> listLoop = new ArrayList<>();
			final List<Object> changeList = new ArrayList<>();
			try {
				for (int i = 0; i < tihArray.length; i++) {
					tihArray[i].remove(conn, listLoop, changeList);
				}
				if (!changeList.isEmpty()) {
					conn.executeParameterizedBatchGroup();
					conn.release();
				} else {
					conn.releaseRollback();
				}
			} catch (final SQLException e) {
				// TODO: this block has to be executed for other exceptions also, if not will
				// not unlocks some lists
				try {
					conn.releaseRollback();
				} catch (final SQLException e1) {
					LOG.debug("#0", e1);
				}
				for (final Object tih : changeList) {
					if (ScaledSet.class.isInstance(tih)) {
						ScaledSet.class.cast(tih).removeFail();
					} else if (DataProxyHandler.class.isInstance(tih)) {
						DataProxyHandler.class.cast(tih).removeFail();
					} else {
						throw new IllegalStateException();
					}
				}
				throw new LtmLtRtException(e);
			}
			for (final Object tih : changeList) {
				if (ScaledSet.class.isInstance(tih)) {
					ScaledSet.class.cast(tih).removeSuccess();
				} else if (DataProxyHandler.class.isInstance(tih)) {
					DataProxyHandler.class.cast(tih).removeSuccess();
				} else {
					throw new IllegalStateException();
				}
			}
		} catch (final ClosedLtRtException e) {
			throw new LtmLtRtException(e);
		} catch (final ObjectPoolLtException e) {
			throw new LtmLtRtException(e);
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		} finally {
			DB_ACCESS.unlock();
		}
	}

	/**
	 *
	 * @param tihArray
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
	static void refresh(final DataProxyHandler... tihArray) throws LtmLtRtException {
		try {
			DB_ACCESS.lock();
			final List<Object> listLoop = new ArrayList<>();
			for (int i = 0; i < tihArray.length; i++) {
				tihArray[i].refresh(listLoop);
			}
		} catch (final InterruptedException e) {
			throw new LtmLtRtException(e);
		} finally {
			DB_ACCESS.unlock();
		}
	}

	private final ElementTable elementTable;
	private Long id;
	private final Map<String, Object> data;
	private final Map<String, Object> joinData;
	private boolean needSave;

	/**
	 * .<br/>
	 * <br/>
	 *
	 * @param elementTable
	 * @param id
	 * @param data
	 * @throws IllegalArgumentLtRtException if <code>data</code> is null
	 */
	<T extends LtmObjectModelling> DataProxyHandler(final ElementTable elementTable, final Long id,
			final Map<String, Object> data) throws IllegalArgumentLtRtException {
		super(elementTable.getClassTable());
		if (data == null) {
			throw new IllegalArgumentLtRtException();
		}
		this.id = id;
		this.elementTable = elementTable;
		this.data = data;
		this.joinData = new HashMap<>();
		if (this.id == null) {
			synchronized (this.data) {
				this.data.put(elementTable.getColumnId().getJavaName(),
						SequenceGeneratorEnum.nextVal(elementTable.getColumnId().getSequenceGenerator()));
				for (final ElementColumn ec : elementTable.getColumns()) {
					if (ec.isStream()) {
						this.data.put(ec.getJavaName(), DateUtil.generateUniqueNumberPerJVM());
					}
				}
			}
			this.needSave = true;
		} else {
			this.needSave = false;
		}
	}

	@Override
	protected Object invokeSpecial(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final String methodName = method.getName();
		// compareTo
		if (methodName.equals(METHOD_NAME_COMPARE_TO) && args != null && args.length == 1
				&& (LtmObjectModelling.class.isInstance(args[0]) || args[0] == null)) {
			return this.compareTo(LtmObjectModelling.class.cast(args[0]));
		}
		// isNew
		if (methodName.equals(METHOD_NAME_IS_NEW) && args == null) {
			return this.isNew();
		}
		// save
		if (methodName.equals(METHOD_NAME_SAVE) && args == null) {
			this.save();
			return null;
		}
		// refresh
		if (methodName.equals(METHOD_NAME_REMOVE) && args == null) {
			this.remove();
			return null;
		}
		// remove
		if (methodName.equals(METHOD_NAME_REFRESH) && args == null) {
			this.refresh();
			return null;
		}
		// dyn get
		if (methodName.equals(DYN_METHOD_NAME_GET) && args == null) {
			return this.get();
		}
		// dyn put
		if (methodName.equals(DYN_METHOD_NAME_PUT) && args != null && args.length == 1 && args[0] != null
				&& Data.class.isAssignableFrom(args[0].getClass())) {
			this.put((Data) args[0]);
			return null;
		}
		// dyn get link
		if (methodName.equals(DYN_METHOD_NAME_GET_LINK) && args != null && args.length == 1 && args[0] != null
				&& String.class.equals(args[0].getClass())) {
			return this.getLink((String) args[0]);
		}
		// dyn put link
		if (methodName.equals(DYN_METHOD_NAME_PUT_LINK) && args != null && args.length == 2 && args[0] != null
				&& String.class.isInstance(args[0])) {
			this.putLink((String) args[0], args[1]);
			return null;
		}
		// dyn get link set
		if (methodName.equals(DYN_METHOD_NAME_GET_LINK_SET) && args != null && args.length == 1 && args[0] != null
				&& String.class.equals(args[0].getClass())) {
			return this.getLinkSet((String) args[0]);
		}
		// dyn get binary stream
		if (methodName.equals(DYN_METHOD_NAME_GET_BINARY_STREAM) && args != null && args.length == 1 && args[0] != null
				&& String.class.equals(args[0].getClass())) {
			return this.getBinaryStream((String) args[0]);
		}
		throw new NoSuchMethodException();
	}

	/**
	 *
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close +Cause ObjectPoolLtException if can't
	 *                          instantiate a new connection +Cause
	 *                          InterruptedException if interrupted while waiting
	 *                          for the connection <br/>
	 *                          +Cause SQLException if a database access error
	 *                          occurs, or this method is called on a closed
	 *                          connection
	 */
	@Override
	protected Object get(final String dataName) throws LtmLtRtException {
		Object result;
		final ElementColumn ec = this.elementTable.getColumn(dataName);
		synchronized (this.data) {
			synchronized (this.joinData) {
				if (!ec.isLink()) {
					if (ec.isStream()) {
						// stream
						if (!this.joinData.containsKey(dataName)) {
							try {
								result = new LtmBinary((Long) this.data.get(dataName));
								if (this.needSave && this.id == null) {
									LtmBinary.fileToDelete((LtmBinary) result);
								}
								this.joinData.put(dataName, result);
							} catch (final IOException e) {
								throw new IllegalStateException(e);
							}
						} else {
							result = this.joinData.get(dataName);
						}
					} else {
						// leaf
						result = this.data.get(dataName);
					}
				} else {
					if (this.joinData.containsKey(dataName)) {
						result = this.joinData.get(dataName);
					} else {
						try {
							result = fetchColumn(ec);
						} catch (final ClosedLtRtException e) {
							throw new LtmLtRtException(e);
						} catch (final ObjectPoolLtException e) {
							throw new LtmLtRtException(e);
						} catch (final InterruptedException e) {
							throw new LtmLtRtException(e);
						} catch (final SQLException e) {
							throw new LtmLtRtException(e);
						}
						if (ec.getListFromElementTable() == null && this.data.get(ec.getJavaName()) != null
								&& result == null) {
							set(dataName, null);
						} else {
							this.joinData.put(dataName, result);
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	protected void set(final String dataName, final Object value) {
		if (LtmObjectModelling.ID_DATA_NAME.equals(dataName)) {
			throw new IllegalAccessError();
		}
		final ElementColumn ec = this.elementTable.getColumn(dataName);
		if (value != null && !ec.getReturnType().isInstance(value)) {
			throw new IllegalArgumentException();
		}
		if (!ec.isLink()) {
			if (ec.isStream()) {
				throw new IllegalAccessError();
			}
			// leaf
			synchronized (this.data) {
				this.data.put(dataName, value);
				this.needSave = true;
			}
		} else {
			if (ec.getListFromElementTable() != null) {
				throw new IllegalAccessError();
			}
			setLink(ec, dataName, value);
		}
	}

	private void setLink(final ElementColumn ec, final String dataName, final Object value) {
		if (!ec.isMapped()) {
			// tableItf
			synchronized (this.data) {
				if (value == null) {
					this.data.put(dataName, null);
					this.needSave = true;
					synchronized (this.joinData) {
						this.joinData.put(dataName, null);
					}
				} else {
					this.data.put(dataName, ((LtmObjectModelling) value).getId());
					this.needSave = true;
					synchronized (this.joinData) {
						this.joinData.put(dataName, value);
					}
				}
			}
		} else {
			// tableItf mapped
			final Object tmp = get(dataName);
			if (tmp != null) {
				// FIXME: since this handler change the linked tmp(handler),
				// this tmp has to be saved when this handler has the method save called
				try {
					final DataProxyHandler mappedHandler = DATA_PROXY.getInvocationHandler((LtmObjectModelling) tmp);
					synchronized (mappedHandler.data) {
						mappedHandler.data.put(ec.getMappedByName(), null);
						mappedHandler.needSave = true;
						synchronized (mappedHandler.joinData) {
							mappedHandler.joinData.put(dataName, null);
						}
					}
				} catch (final IllegalArgumentException e) {
					throw new ImplementationLtRtException(e);
				} catch (final ClassCastException e) {
					throw new ImplementationLtRtException(e);
				}
			}
			if (value != null) {
				// FIXME: since this handler change the linked value(handler),
				// this value has to be saved when this handler has the method save called
				try {
					final DataProxyHandler mappedHandler = DATA_PROXY.getInvocationHandler((LtmObjectModelling) value);
					synchronized (mappedHandler.data) {
						mappedHandler.data.put(ec.getMappedByName(),
								(Long) this.data.get(LtmObjectModelling.ID_DATA_NAME));
						mappedHandler.needSave = true;
						synchronized (mappedHandler.joinData) {
							mappedHandler.joinData.put(dataName, this.getMyProxy());
						}
					}
				} catch (final IllegalArgumentException e) {
					throw new ImplementationLtRtException(e);
				} catch (final ClassCastException e) {
					throw new ImplementationLtRtException(e);
				}
			}
			synchronized (this.joinData) {
				this.joinData.put(dataName, value);
			}
		}
	}

	/**
	 *
	 * @param ec
	 * @return
	 * @throws ClosedLtRtException   if long term memory already close
	 * @throws ObjectPoolLtException if can't instantiate a new connection
	 * @throws InterruptedException  if interrupted while waiting for the connection
	 * @throws SQLException          if a database access error occurs, or this
	 *                               method is called on a closed connection
	 */
	private Object fetchColumn(final ElementColumn ec)
			throws ClosedLtRtException, ObjectPoolLtException, InterruptedException, SQLException {
		Object result = null;
		if (ec.isLink()) {
			if (ec.getListFromElementTable() != null) {
				result = new ScaledSet<>(getMyProxy(), ec.getRelatedElementTable(), ec.getMappedByName(),
						ec.getFetchScale());
			} else {
				if (!ec.isMapped()) {
					final Object tmp = this.data.get(ec.getJavaName());
					if (tmp != null) {
						result = DATA_PROXY.getTableInstance(ec.getRelatedElementTable(), (Long) tmp);
					}
				} else {
					final List<Map<String, Object>> tmpMap = ec.getRelatedElementTable().dbFetchMappedData(
							ec.getMappedByName(), (Long) this.data.get(ec.getJavaName()), null, 0, 1);
					if (tmpMap.size() != 0) {
						result = DATA_PROXY.getTableInstance(ec.getRelatedElementTable(),
								(Long) this.data.get(ec.getJavaName()), tmpMap.get(0));
					}
				}
			}
		}
		return result;
	}

	private Data get() {
		synchronized (this.data) {
			final Data result = XmlomUtil.newXmlObjectModelling(Data.class);
			if (!isNew()) {
				result.setId((Long) this.data.get(LtmObjectModelling.ID_DATA_NAME));
			}
			final Set<Field> fieldSet = new HashSet<>();
			Field tmpField;
			Value tmpValue;
			Object tmp;
			for (final ElementColumn ec : this.elementTable.getColumns()) {
				if (!ec.isLink() && !ec.isId() && !ec.isStream()) {
					tmp = this.data.get(ec.getJavaName());
					if (tmp != null) {
						tmpField = XmlomUtil.newXmlObjectModelling(Field.class);
						tmpField.setName(ec.getJavaName());
						tmpValue = XmlomUtil.newXmlObjectModelling(Value.class);
						if (!ec.isArray()) {
							if (Boolean.class.isAssignableFrom(ec.getReturnType())) {
								tmpValue.setBooleanValue((Boolean) tmp);
								tmpValue.setType(DataValueTypeEnum.BOOLEAN);
							} else if (Byte.class.isAssignableFrom(ec.getReturnType())) {
								tmpValue.setByteValue((Byte) tmp);
								tmpValue.setType(DataValueTypeEnum.BYTE);
							} else if (Short.class.isAssignableFrom(ec.getReturnType())) {
								tmpValue.setShortValue((Short) tmp);
								tmpValue.setType(DataValueTypeEnum.SHORT);
							} else if (Integer.class.isAssignableFrom(ec.getReturnType())) {
								tmpValue.setIntegerValue((Integer) tmp);
								tmpValue.setType(DataValueTypeEnum.INTEGER);
							} else if (Long.class.isAssignableFrom(ec.getReturnType())) {
								tmpValue.setLongValue((Long) tmp);
								tmpValue.setType(DataValueTypeEnum.LONG);
							} else if (Double.class.isAssignableFrom(ec.getReturnType())) {
								tmpValue.setDoubleValue((Double) tmp);
								tmpValue.setType(DataValueTypeEnum.DOUBLE);
							} else if (String.class.isAssignableFrom(ec.getReturnType())) {
								tmpValue.setStringValue((String) tmp);
								tmpValue.setType(DataValueTypeEnum.STRING);
							} else {
								throw new ImplementationLtRtException();
							}
						} else {
							if (ConstantLtm.CLASS_ARRAY_BOOLEAN.isAssignableFrom(ec.getReturnType())) {
								tmpValue.setBooleanArrayValue((Boolean[]) tmp);
								tmpValue.setType(DataValueTypeEnum.ARRAY_BOOLEAN);
							} else if (ConstantLtm.CLASS_ARRAY_BYTE.isAssignableFrom(ec.getReturnType())) {
								tmpValue.setByteArrayValue((Byte[]) tmp);
								tmpValue.setType(DataValueTypeEnum.ARRAY_BYTE);
							} else if (ConstantLtm.CLASS_ARRAY_SHORT.isAssignableFrom(ec.getReturnType())) {
								tmpValue.setShortArrayValue((Short[]) tmp);
								tmpValue.setType(DataValueTypeEnum.ARRAY_SHORT);
							} else if (ConstantLtm.CLASS_ARRAY_INTEGER.isAssignableFrom(ec.getReturnType())) {
								tmpValue.setIntegerArrayValue((Integer[]) tmp);
								tmpValue.setType(DataValueTypeEnum.ARRAY_INTEGER);
							} else if (ConstantLtm.CLASS_ARRAY_LONG.isAssignableFrom(ec.getReturnType())) {
								tmpValue.setLongArrayValue((Long[]) tmp);
								tmpValue.setType(DataValueTypeEnum.ARRAY_LONG);
							} else {
								throw new ImplementationLtRtException();
							}
						}
						tmpField.setValue(tmpValue);
						fieldSet.add(tmpField);
					}
				}
			}
			result.setFieldSet(fieldSet);
			return result;
		}
	}

	private void put(final Data dlom) {
		synchronized (this.data) {
			ElementColumn ec;
			Object tmp;
			for (final Field field : dlom.getFieldSet()) {
				ec = this.elementTable.getColumn(field.getName());
				if (ec == null || ec.isLink() || ec.isId() || ec.isStream()) {
					throw new IllegalAccessError();
				}
				switch (field.getValue().getType()) {
				case BOOLEAN:
					tmp = field.getValue().getBooleanValue();
					break;
				case BYTE:
					tmp = field.getValue().getByteValue();
					break;
				case SHORT:
					tmp = field.getValue().getShortValue();
					break;
				case INTEGER:
					tmp = field.getValue().getIntegerValue();
					break;
				case LONG:
					tmp = field.getValue().getLongValue();
					break;
				case DOUBLE:
					tmp = field.getValue().getDoubleValue();
					break;
				case STRING:
					tmp = field.getValue().getStringValue();
					break;
				case ARRAY_BOOLEAN:
					tmp = field.getValue().getBooleanArrayValue();
					break;
				case ARRAY_BYTE:
					tmp = field.getValue().getByteArrayValue();
					break;
				case ARRAY_SHORT:
					tmp = field.getValue().getShortArrayValue();
					break;
				case ARRAY_INTEGER:
					tmp = field.getValue().getIntegerArrayValue();
					break;
				case ARRAY_LONG:
					tmp = field.getValue().getLongArrayValue();
					break;
				case LINK_ONE_TO_ONE:
				case LINK_MANY_TO_ONE:
				case LINK_ONE_TO_MANY:
				case BINARY_STREAM:
					throw new IllegalArgumentLtRtException();
				default:
					throw new ImplementationLtRtException();
				}
				if (tmp != null && !ec.getReturnType().isInstance(tmp)) {
					throw new IllegalArgumentException();
				}
				this.data.put(field.getName(), tmp);
				this.needSave = true;
			}
		}
	}

	private Object getLink(final String field) {
		return get(field);
	}

	private void putLink(final String field, final Object value) {
		if (LtmObjectModelling.ID_DATA_NAME.equals(field)) {
			throw new IllegalAccessError();
		}
		final ElementColumn ec = this.elementTable.getColumn(field);
		if (ec == null || !ec.isLink() || ec.getListFromElementTable() != null) {
			throw new IllegalAccessError();
		}
		if (value != null && !ec.getReturnType().isInstance(value)) {
			throw new IllegalArgumentException();
		}
		setLink(ec, field, value);
	}

	private Object getLinkSet(final String field) {
		return get(field);
	}

	private Object getBinaryStream(final String field) {
		return get(field);
	}

	synchronized boolean isNew() {
		return this.id == null;
	}

	/**
	 *
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
	// Override
	public void save() throws LtmLtRtException {
		DataProxyHandler.save(this);
	}

	/**
	 *
	 * @param conn
	 * @param persistListLoop
	 * @param updateListLoop
	 * @param changeList
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection
	 */
	@SuppressWarnings("unchecked")
	void save(final ConnectionDB conn, final List<Object> persistListLoop, final List<Object> updateListLoop,
			final List<Object> changeList) throws SQLException {
		boolean firstPass = false;
		if (persistListLoop != null && !persistListLoop.contains(this)) {
			persistListLoop.add(this);
			if (this.needSave && this.id == null) {
				changeList.add(this);
				synchronized (this.data) {
					this.elementTable.persistStep(conn, this.data);
					this.needSave = false;
				}
			}
			firstPass = true;
		}
		if (updateListLoop != null && !updateListLoop.contains(this)) {
			updateListLoop.add(this);
//			synchronized (data) {
//				if(needSave || (id == null && data.get(elementTable.getColumnId().getJavaName()) != null)){
//					DataProxyHandler tih;
//					Object tihId;
//					synchronized (joinData) {
//						for(String key : joinData.keySet()){
//							if(joinData.get(key) != null && !BinaryStream.class.equals(joinData.get(key).getClass()) && data.containsKey(key)){
//								tih = (DataProxyHandler) Proxy.getInvocationHandler(joinData.get(key));
//								tihId = tih.data.get(tih.elementTable.getColumnId().getJavaName());
//								if(!tihId.equals(data.get(key))){
//									data.put(key, tihId);
//									if(!needSave) needSave = true;
//								}
//							}
//						}
//					}
			if (this.needSave) {
				synchronized (this.data) {
					if (this.id != null) {
						changeList.add(this);
					}
					this.elementTable.updateStep(conn, this.data);
					this.needSave = false;
				}
			}
//				}
//			}
			firstPass = true;
		}
		if (firstPass) {
			Object obj;
			for (final ElementColumn ec : this.elementTable.getColumns()) {
				if (ec.isCascadeAll() || ec.isCascadeSave()) {
					obj = get(ec.getJavaName());
					if (obj != null) {
						if (ScaledSet.class.isInstance(obj)) {
							ScaledSet.class.cast(obj).save(conn, persistListLoop, updateListLoop, changeList);
						} else {
							((DataProxyHandler) Proxy.getInvocationHandler(obj)).save(conn, persistListLoop,
									updateListLoop, changeList);
						}
					}
				}
			}
		}
	}

	private void saveFail() {
		synchronized (this.data) {
			this.needSave = true;
//			data.remove(elementTable.getColumnId().getJavaName());
		}
	}

	private void saveSuccess() {
		if (this.id == null) {
			synchronized (this) {
				this.id = (Long) this.data.get(this.elementTable.getColumnId().getJavaName());
				this.elementTable.getCache().put(getMyProxy());
				for (final String key : this.joinData.keySet()) {
					if (this.joinData.get(key) != null && LtmBinary.class.equals(this.joinData.get(key).getClass())) {
						LtmBinary.ignoreFileToDelete((LtmBinary) this.joinData.get(key));
					}
				}
			}
		}
	}

	/**
	 *
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
	// Override
	public void remove() throws LtmLtRtException {
		DataProxyHandler.remove(this);
	}

	/**
	 *
	 * @param conn
	 * @param listLoop
	 * @param changeList
	 * @throws SQLException if a database access error occurs, or this method is
	 *                      called on a closed connection
	 */
	@SuppressWarnings("unchecked")
	void remove(final ConnectionDB conn, final List<Object> listLoop, final List<Object> changeList)
			throws SQLException {
		if (!listLoop.contains(this)) {
			listLoop.add(this);
			if (this.id != null) {
				this.elementTable.removeStep(conn, this.id);
				synchronized (this) {
					this.id = null;
				}
				changeList.add(this);
			}
			Object obj;
			for (final ElementColumn ec : this.elementTable.getColumns()) {
				if (ec.isCascadeAll() || ec.isCascadeRemove()) {
					obj = get(ec.getJavaName());
					if (obj != null) {
						if (ScaledSet.class.isInstance(obj)) {
							ScaledSet.class.cast(obj).remove(conn, listLoop, changeList);
						} else {
							((DataProxyHandler) Proxy.getInvocationHandler(obj)).remove(conn, listLoop, changeList);
						}
					}
				}
			}
		}
	}

	private void removeFail() {
		synchronized (this) {
			this.id = (Long) this.data.get(this.elementTable.getColumnId().getJavaName());
		}
	}

	private void removeSuccess() {
		synchronized (this) {
			this.elementTable.getCache().remove(this.data.get(this.elementTable.getColumnId().getJavaName()));
			this.needSave = true;
			for (final String key : this.joinData.keySet()) {
				if (this.joinData.get(key) != null && LtmBinary.class.equals(this.joinData.get(key).getClass())) {
					LtmBinary.fileToDelete((LtmBinary) this.joinData.get(key));
				}
			}
		}
	}

	/**
	 *
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
	// Override
	public void refresh() throws LtmLtRtException {
		DataProxyHandler.refresh(this);
	}

	/**
	 *
	 * @param listLoop
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close +Cause ObjectPoolLtException if can't
	 *                          instantiate a new connection +Cause
	 *                          InterruptedException if interrupted while waiting
	 *                          for the connection +Cause SQLException if a database
	 *                          access error occurs, or this method is called on a
	 *                          closed connection
	 */
	void refresh(final List<Object> listLoop) throws LtmLtRtException {
		refresh(listLoop, null);
	}

	/**
	 *
	 * @param listLoop
	 * @param updatedData
	 * @throws LtmLtRtException <br/>
	 *                          +Cause ClosedLtRtException if long term memory
	 *                          already close +Cause ObjectPoolLtException if can't
	 *                          instantiate a new connection +Cause
	 *                          InterruptedException if interrupted while waiting
	 *                          for the connection +Cause SQLException if a database
	 *                          access error occurs, or this method is called on a
	 *                          closed connection
	 */
	@SuppressWarnings("unchecked")
	void refresh(final List<Object> listLoop, final Map<String, Object> updatedData) throws LtmLtRtException {
		if (!listLoop.contains(this)) {
			listLoop.add(this);
			if (this.id != null && this.needSave) {
				synchronized (this) {
					this.data.clear();
					if (updatedData != null) {
						if (this.id.equals(updatedData.get(this.elementTable.getColumnId().getJavaName()))) {
							this.data.putAll(updatedData);
						} else {
							throw new ImplementationLtRtException();
						}
					} else {
						try {
							this.data.putAll(this.elementTable.dbFetchData(this.id));
						} catch (final ClosedLtRtException e) {
							throw new LtmLtRtException(e);
						} catch (final ObjectPoolLtException e) {
							throw new LtmLtRtException(e);
						} catch (final InterruptedException e) {
							throw new LtmLtRtException(e);
						} catch (final SQLException e) {
							throw new LtmLtRtException(e);
						}
					}
//					joinData.clear();
					this.needSave = false;
				}
				;
			}
			Object obj;
			for (final ElementColumn ec : this.elementTable.getColumns()) {
				if (ec.isCascadeAll() || ec.isCascadeRefresh()) {
					obj = get(ec.getJavaName());
					if (obj != null) {
						if (ScaledSet.class.isInstance(obj)) {
							ScaledSet.class.cast(obj).refresh(listLoop);
						} else {
							((DataProxyHandler) Proxy.getInvocationHandler(obj)).refresh(listLoop);
						}
					}
				}
			}
		}
	}

	@Override
	public int compareTo(final LtmObjectModelling other) {
		if (other == null) {
			return -1;
		}
		if (this.id == null && other.getId() == null) {
			return 0;
		}
		if (this.id == null && other.getId() != null) {
			return 1;
		}
		if (this.id != null && other.getId() == null) {
			return -1;
		}
		return this.id.longValue() < other.getId().longValue() ? -1
				: (this.id.longValue() == other.getId().longValue() ? 0 : 1);
	}

	@Override
	public String toString() {
		return super.toString() + "_" + this.id;
	}

	@Override
	public boolean equals(final Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (!LtmObjectModelling.class.isInstance(obj)) {
			return false;
		}
		final LtmObjectModelling other = LtmObjectModelling.class.cast(obj);
		if (this.id == null || other.getId() == null) {
			return false;
		}
		return this.id.equals(other.getId());
	}

}

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

package leitej.xml.om;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import leitej.Constant;
import leitej.LtSystemOut;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.XmlInvalidLtException;
import leitej.exception.XmlomInvalidLtException;
import leitej.exception.XmlomSecurityLtException;
import leitej.util.AgnosticUtil;
import leitej.util.HexaUtil;
import leitej.util.StringUtil;
import leitej.xml.XmlConsumer;

/**
 * XML - Object Modeling - Parser
 *
 * @author Julio Leite
 * @see leitej.xml.om.Producer
 */
final class Parser {

	private static final DataProxy DATA_PROXY = DataProxy.getInstance();

	private static final Class<?> BYTE_ARRAY_CLASS = Array.newInstance(byte.class, 0).getClass();

	private XmlConsumer consumer;
	private Map<Integer, Object> trackLoopObjects;
	private final StringBuilder sbTmpComment;
	private final StringBuilder sbTmpVal;
	private final StringBuilder sbTmpElmName;
	private final StringBuilder sbTmpAttb;
	private boolean init;
	private boolean done;

	/**
	 *
	 * @param isr
	 * @param trustClass
	 */
	Parser(final InputStreamReader isr) {
		LtSystemOut.debug("lt.NewInstance");
		this.consumer = new XmlConsumer(isr);
		this.trackLoopObjects = new HashMap<>();
		this.sbTmpComment = new StringBuilder();
		this.sbTmpVal = new StringBuilder();
		this.sbTmpElmName = new StringBuilder();
		this.sbTmpAttb = new StringBuilder();
		this.init = false;
		this.done = false;
	}

	/**
	 *
	 * @throws XmlomInvalidLtException If do not find the root xmlom element
	 * @throws XmlInvalidLtException   If encounter an invalid syntax
	 * @throws IOException             If an I/O error occurs
	 */
	private void init() throws XmlomInvalidLtException, XmlInvalidLtException, IOException {
		if (!this.init) {
			try {
				readMetaData();
				readRootElementOpen();
			} finally {
				this.init = true;
			}
		}
	}

	/**
	 *
	 * @throws XmlInvalidLtException If encounter an invalid syntax
	 * @throws IOException           If an I/O error occurs
	 */
	private void readMetaData() throws XmlInvalidLtException, IOException {
		while (this.consumer.isNextTagMetaData()) {
			this.consumer.consumeTag();
		}
	}

	/**
	 *
	 * @throws XmlomInvalidLtException If do not find the open root xmlom element
	 * @throws XmlInvalidLtException   If encounter an invalid syntax
	 * @throws IOException             If an I/O error occurs
	 */
	private void readRootElementOpen() throws XmlomInvalidLtException, XmlInvalidLtException, IOException {
		this.consumer.consumeTag();
		if (!this.consumer.isTagOpen()) {
			throw new XmlomInvalidLtException("lt.XmlOmInvalidSyntaxRootInit", this.consumer);
		}
		LtSystemOut.debug("#0", this.consumer);
	}

	/**
	 *
	 * @throws XmlomInvalidLtException If do not find the close root xmlom element
	 * @throws XmlInvalidLtException   If is not a valid tag
	 */
	private void readRootElementClose() throws XmlomInvalidLtException, XmlInvalidLtException {
		if (!this.consumer.isTagClose()) {
			throw new XmlomInvalidLtException("lt.XmlOmInvalidSyntaxRootEnd", this.consumer);
		}
		LtSystemOut.debug("#0", this.consumer);
	}

	/**
	 *
	 * @return
	 * @throws XmlomSecurityLtException
	 * @throws XmlomInvalidLtException      If is reading an invalid xmlom
	 * @throws XmlInvalidLtException        If encounter an invalid syntax
	 * @throws IOException                  If an I/O error occurs
	 * @throws IllegalArgumentLtRtException if <code>interfaceClass</code> in
	 *                                      parameter is null or does not represents
	 *                                      a valid interface
	 */
	synchronized <I extends XmlObjectModelling> I read(final Class<I> interfaceClass)
			throws IllegalArgumentLtRtException, XmlomSecurityLtException, XmlomInvalidLtException,
			XmlInvalidLtException, IOException {
		TrustClassname.registry(interfaceClass);
		I result = null;
		if (!this.done) {
			init();
			try {
				result = interfaceClass.cast(readObject());
			} catch (final ClassCastException e) {
				throw new IOException(e);
			}
			if (result != null) {
				this.trackLoopObjects.clear();
			} else {
				this.trackLoopObjects = null;
			}
		}
		return result;
	}

	/**
	 *
	 * @throws IOException If an I/O error occurs
	 */
	synchronized void close() throws IOException {
		if (this.consumer != null) {
			this.done = true;
			this.consumer.close();
			this.consumer = null;
		}
	}

	/**
	 *
	 * @return
	 * @throws XmlomSecurityLtException
	 * @throws XmlomInvalidLtException
	 * @throws XmlInvalidLtException
	 * @throws IOException              If an I/O error occurs
	 */
	@SuppressWarnings("unchecked")
	private <I extends XmlObjectModelling> I readObject()
			throws XmlomSecurityLtException, XmlomInvalidLtException, XmlInvalidLtException, IOException {
		I obj = null;
		if (!this.consumer.isAlreadyConsumedEndTagElementRoot()) {
			List<String> comments = null;
			// while is comment read to array - to set on the next object read
			while (this.consumer.isNextTagComment()) {
				if (comments == null) {
					comments = new ArrayList<>();
				}
				this.consumer.consumeTag();
				this.sbTmpComment.setLength(0);
				this.consumer.getComment(this.sbTmpComment);
				comments.add(this.sbTmpComment.toString());
			}
			Object tmp = null;
			// read the next object
			if (this.consumer.isNextTagOpen() && !this.consumer.isNextTagOpenClose()) {
				this.consumer.consumeTag();
				tmp = readObjectAux(comments);
				if (tmp == null) {
					throw new ImplementationLtRtException();
				}
				if (!XmlObjectModelling.class.isInstance(tmp)) {
					if (BYTE_ARRAY_CLASS.isInstance(tmp)) {
						final XmlObjectModelling xom = XmlomIOStream.newXmlObjectModelling(XmlObjectModelling.class);
						DATA_PROXY.getInvocationHandler(xom).setByteArray((byte[]) tmp);
						obj = (I) xom;
					} else {
						throw new XmlomInvalidLtException(tmp.getClass().getName());
					}
				} else {
					obj = (I) tmp;
				}
				this.consumer.consumeTag();// close tag
			} else {
				// there is no next object
				if (comments != null) {
					throw new XmlomInvalidLtException("lt.XmlOmInvalidCommentSyntax", comments);
				}
				this.consumer.consumeTag();
				if (this.consumer.isAlreadyConsumedEndTagElementRoot()) {
					// already ended
					readRootElementClose();
					this.done = true;
				} else {
					// invalid xmlom
					throw new XmlomInvalidLtException("lt.XmlOmInvalidSyntax", this.consumer);
				}
			}
		}
		return obj;
	}

	/**
	 *
	 * @return
	 * @throws XmlomSecurityLtException If tries to load an untrusted class
	 * @throws XmlomInvalidLtException  If class was not found or invalid xmlom
	 *                                  element attributes
	 * @throws XmlInvalidLtException    If is an invalid tag
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private <I extends XmlObjectModelling> Object readObjectAux(final List<String> comments)
			throws XmlomSecurityLtException, XmlomInvalidLtException, XmlInvalidLtException, IOException {
		Object object = null;
		// parse the class of returning object
		Class<?> dataClass;
		this.sbTmpAttb.setLength(0);
		if (!this.consumer.getElementAttributeValue(this.sbTmpAttb, Producer.ATTRIBUTE_CLASS_NAME)
				|| this.sbTmpAttb.length() == 0) {
			throw new XmlomInvalidLtException("lt.XmlOmMissesAttribClass", this.consumer);
		}
		final String className = TypeVsClassname.getClassname(this.sbTmpAttb.toString());
		try {
			if (!TrustClassname.has(className)) {
				throw new XmlomSecurityLtException("lt.XmlOmUntrustedClass", className);
			}
			dataClass = AgnosticUtil.getClass(className);
		} catch (final ClassNotFoundException e) {
			throw new XmlomInvalidLtException(e, "lt.XmlOmInvalidAttribClass", this.consumer);
		}
		// parse object
		if (!this.consumer.isTagOpenClose()) {
			if (LeafElement.has(dataClass)) {
				if (comments != null) {
					throw new XmlomInvalidLtException("lt.XmlOmInvalidCommentLeaf");
				}
				this.sbTmpVal.setLength(0);
				this.consumer.getElementValue(this.sbTmpVal);
				object = convertFromElementValue(dataClass, this.sbTmpVal);
			} else if (ArrayElement.has(dataClass)) {
				if (comments != null) {
					throw new XmlomInvalidLtException("lt.XmlOmInvalidCommentArray");
				}
				object = readArrayObject(dataClass);
			} else {
				object = Pool.poolXmlObjectModelling((Class<I>) dataClass);
				final Integer id = getElementAttributeId();
				if (id != null) {
					this.trackLoopObjects.put(id, object);
				}
				if (!this.consumer.isNextTagClose()) {
					readObjectData((I) object);
				}
				((I) object).setComments(comments);
			}
		} else {
			final Integer id = getElementAttributeId();
			if (id != null) {
				if (comments != null) {
					throw new XmlomInvalidLtException("lt.XmlOmInvalidCommentLoopObj");
				}
				object = this.trackLoopObjects.get(id);
				if (object == null) {
					throw new XmlomInvalidLtException();
				}
			} else if (dataClass.isPrimitive()) {
				throw new XmlomInvalidLtException();
			}
		}
		return object;
	}

	/**
	 *
	 * @return id
	 * @throws XmlomInvalidLtException If the attribute id has an invalid value
	 * @throws XmlInvalidLtException   If is an invalid tag
	 */
	private Integer getElementAttributeId() throws XmlomInvalidLtException, XmlInvalidLtException {
		this.sbTmpAttb.setLength(0);
		this.consumer.getElementAttributeValue(this.sbTmpAttb, Producer.ATTRIBUTE_ID);
		if (this.sbTmpAttb.length() > 0) {
			try {
				return Integer.valueOf(this.sbTmpAttb.toString());
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "lt.XmlOmInvalidAttribId", this.consumer);
			}
		} else {
			return null;
		}
	}

	/**
	 *
	 *
	 * @param classValue defines the class to be instantiated
	 * @param valueSb    data that define the state/value of the new instance
	 * @return an object instance of <code>classValue</code> representing the data
	 *         in <code>valueSb</code>
	 * @throws XmlomInvalidLtException if <code>valueSb</code> represents an invalid
	 *                                 state/value of an object of class
	 *                                 <code>classValue</code>
	 */
	private static Object convertFromElementValue(final Class<?> classValue, final StringBuilder valueSb)
			throws XmlomInvalidLtException {
		Object result = null;
		if (valueSb == null || classValue == null) {
			throw new ImplementationLtRtException();
		}
		if (classValue.equals(BYTE_ARRAY_CLASS)) {
			try {
				result = HexaUtil.toByte(valueSb);
			} catch (final IllegalArgumentLtRtException e) {
				throw new XmlomInvalidLtException(e);
			}
		} else if (classValue.equals(String.class)) {
			result = valueSb.toString();
		} else if (classValue.equals(Date.class)) {
			try {
				result = new Date(Long.valueOf(valueSb.toString()));
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e);
			}
		} else if (classValue.equals(Character.class)) {
			if (valueSb.length() != 1) {
				throw new XmlomInvalidLtException("lt.XmlOmInvalidValueChar", valueSb.toString());
			}
			result = valueSb.charAt(0);
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_BYTE_CLASS)) {
			try {
				result = Byte.valueOf(valueSb.toString()).byteValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "lt.XmlOmInvalidValueByte", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_SHORT_CLASS)) {
			try {
				result = Short.valueOf(valueSb.toString()).shortValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "lt.XmlOmInvalidValueShort", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_INT_CLASS)) {
			try {
				result = Integer.valueOf(valueSb.toString()).intValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "lt.XmlOmInvalidValueInt", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_LONG_CLASS)) {
			try {
				result = Long.valueOf(valueSb.toString()).longValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "lt.XmlOmInvalidValueLong", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_FLOAT_CLASS)) {
			try {
				result = Float.valueOf(valueSb.toString()).floatValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "lt.XmlOmInvalidValueFloat", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_DOUBLE_CLASS)) {
			try {
				result = Double.valueOf(valueSb.toString()).doubleValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "lt.XmlOmInvalidValueDouble", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_BOOLEAN_CLASS)) {
			try {
				result = Boolean.valueOf(valueSb.toString()).booleanValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "lt.XmlOmInvalidValueBoolean", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_CHAR_CLASS)) {
			if (valueSb.length() != 1) {
				throw new XmlomInvalidLtException("lt.XmlOmInvalidValueChar", valueSb.toString());
			}
			result = valueSb.charAt(0);
		} else {
			try {
				final Method method = AgnosticUtil.getMethod(classValue, Constant.VALUEOF_METHOD_NAME, String.class);
				result = AgnosticUtil.invoke(classValue, method, valueSb.toString());
			} catch (final SecurityException e) {
				throw new XmlomInvalidLtException(e);
			} catch (final NoSuchMethodException e) {
				throw new XmlomInvalidLtException(e);
			} catch (final IllegalArgumentException e) {
				throw new XmlomInvalidLtException(e);
			} catch (final IllegalArgumentLtRtException e) {
				throw new XmlomInvalidLtException(e);
			} catch (final IllegalAccessException e) {
				throw new XmlomInvalidLtException(e);
			} catch (final InvocationTargetException e) {
				throw new XmlomInvalidLtException(e, "lt.XmlOmInvalidInvokeValueOf", Constant.VALUEOF_METHOD_NAME,
						classValue.getSimpleName(), valueSb.toString());
			}
		}
		return result;
	}

	private <I extends XmlObjectModelling> void readObjectData(final I object)
			throws XmlomInvalidLtException, XmlInvalidLtException, IOException {
		final DataProxyHandler dph = DATA_PROXY.getInvocationHandler(object);
		final Map<String, Object> dphData = dph.getDataMap();
		String dataName;
		boolean isElementTagOpenClose;
		List<String> comments;
		while (!this.consumer.isNextTagClose()) {
			comments = null;
			// while is comment read to array - to set on the next object read
			while (this.consumer.isNextTagComment()) {
				if (comments == null) {
					comments = new ArrayList<>();
				}
				this.consumer.consumeTag();
				this.sbTmpComment.setLength(0);
				this.consumer.getComment(this.sbTmpComment);
				comments.add(this.sbTmpComment.toString());
			}
			if (!this.consumer.isNextTagOpen()) {
				this.consumer.consumeTag();
				throw new XmlomInvalidLtException("lt.XmlOmInvalidSyntax", this.consumer);
			}
			this.consumer.consumeTag();
			isElementTagOpenClose = this.consumer.isTagOpenClose();
			dataName = this.consumer.getElementName().toString();
			if (!dph.existsDataName(dataName)) {
				throw new XmlomInvalidLtException("lt.XmlOmInvalidData", dataName, dph.getInterface().getName());
			}
			dphData.put(dataName, readObjectAux(comments));
			if (!isElementTagOpenClose) {
				this.consumer.consumeTag();// close element
			}
		}
	}

	private Object readArrayObject(final Class<?> dataClass)
			throws XmlomInvalidLtException, XmlInvalidLtException, IOException {
		Object object = null;
		boolean isElementTagOpenClose;
		if (dataClass.isArray()) {
			// ARRAY
			final Class<?> componentType = dataClass.getComponentType();
			final ArrayList<Object> arrayList = new ArrayList<>();
			while (!this.consumer.isNextTagClose()) {
				if (!this.consumer.isNextTagOpen()) {
					this.consumer.consumeTag();
					throw new XmlomInvalidLtException("lt.XmlOmInvalidSyntax", this.consumer);
				}
				this.consumer.consumeTag();
				isElementTagOpenClose = this.consumer.isTagOpenClose();
				arrayList.add(readObjectAux(null));
				if (!isElementTagOpenClose) {
					this.consumer.consumeTag();// close element
				}
			}
			object = Array.newInstance(componentType, arrayList.size());
			try {
				for (int i = 0; i < arrayList.size(); i++) {
					Array.set(object, i, arrayList.get(i));
				}
			} catch (final IllegalArgumentException e) {
				throw new XmlomInvalidLtException(e, "lt.XmlOmInvalidPrimitiveArray");
			}
		} else if (List.class.equals(dataClass)) {
			// LIST
			final List<Object> list = new ArrayList<>();
			while (!this.consumer.isNextTagClose()) {
				if (!this.consumer.isNextTagOpen()) {
					this.consumer.consumeTag();
					throw new XmlomInvalidLtException("lt.XmlOmInvalidSyntax", this.consumer);
				}
				this.consumer.consumeTag();
				isElementTagOpenClose = this.consumer.isTagOpenClose();
				list.add(readObjectAux(null));
				if (!isElementTagOpenClose) {
					this.consumer.consumeTag();// close element
				}
			}
			object = list;
		} else if (Set.class.equals(dataClass)) {
			// SET
			final Set<Object> set = new HashSet<>();
			while (!this.consumer.isNextTagClose()) {
				if (!this.consumer.isNextTagOpen()) {
					this.consumer.consumeTag();
					throw new XmlomInvalidLtException("lt.XmlOmInvalidSyntax", this.consumer);
				}
				this.consumer.consumeTag();
				isElementTagOpenClose = this.consumer.isTagOpenClose();
				set.add(readObjectAux(null));
				if (!isElementTagOpenClose) {
					this.consumer.consumeTag();// close element
				}
			}
			object = set;
		} else if (Map.class.equals(dataClass)) {
			// MAP
			final Map<Object, Object> map = new HashMap<>();
			Object key = null;
			Object value = null;
			while (!this.consumer.isNextTagClose()) {
				if (!this.consumer.isNextTagOpen()) {
					this.consumer.consumeTag();
					throw new XmlomInvalidLtException("lt.XmlOmInvalidSyntax", this.consumer);
				}
				if (this.consumer.isNextTagOpenClose()) {
					this.consumer.consumeTag();
					throw new XmlomInvalidLtException("lt.XmlOmInvalidMapElementNull", this.consumer);
				}
				this.consumer.consumeTag();// open element 'element' (only to structure)
				while (!this.consumer.isNextTagClose()) {
					if (!this.consumer.isNextTagOpen()) {
						this.consumer.consumeTag();
						throw new XmlomInvalidLtException("lt.XmlOmInvalidSyntax", this.consumer);
					}
					this.consumer.consumeTag();
					isElementTagOpenClose = this.consumer.isTagOpenClose();
					this.sbTmpElmName.setLength(0);
					this.sbTmpElmName.append(this.consumer.getElementName());
					if (StringUtil.isEquals(Producer.ELEMENT_NAME_MAP_KEY, this.sbTmpElmName)) {
						key = readObjectAux(null);
					} else if (StringUtil.isEquals(Producer.ELEMENT_NAME_MAP_VALUE, this.sbTmpElmName)) {
						value = readObjectAux(null);
					} else {
						new IOException(new XmlomInvalidLtException("lt.XmlOmIgnoredTag", this.consumer));
					}
					if (!isElementTagOpenClose) {
						this.consumer.consumeTag();// close element
					}
				}
				this.consumer.consumeTag();// close element 'element' (only to structure)
				map.put(key, value);
			}
			object = map;
		} else {
			new ImplementationLtRtException("lt.XmlOmArrayImplementBug");
		}
		return object;
	}

}

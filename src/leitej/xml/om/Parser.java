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

package leitej.xml.om;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
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
import leitej.xml.XmlTagType;

/**
 * XML - Object Modeling - Parser
 *
 * @author Julio Leite
 * @see leitej.xml.om.Producer
 */
final class Parser {

	private static final DataProxy DATA_PROXY = DataProxy.getInstance();

	private static final Class<?> BYTE_ARRAY_CLASS = Array.newInstance(byte.class, 0).getClass();

	private XmlConsumer consumer = null;
	private AbstractRawHandler rawHandler = null;
	private Map<Integer, Object> trackLoopObjects = new HashMap<>();
	private final StringBuilder sbTmpVal = new StringBuilder();
	private final StringBuilder sbTmpElmName = new StringBuilder();
	private final StringBuilder sbTmpAttb = new StringBuilder();

	/**
	 *
	 * @param isr
	 * @throws XmlomInvalidLtException If do not find the root xmlom element
	 * @throws XmlInvalidLtException   If encounter an invalid syntax
	 * @throws IOException             If an I/O error occurs
	 */
	Parser(final InputStreamReader isr) throws XmlomInvalidLtException, XmlInvalidLtException, IOException {
		this(isr, null);
	}

	/**
	 *
	 * @param isr
	 * @param rawHandler handler to Raw type, this parser just gives the raw id,
	 *                   handler deal to retrieve the data
	 * @throws XmlomInvalidLtException If do not find the root xmlom element
	 * @throws XmlInvalidLtException   If encounter an invalid syntax
	 * @throws IOException             If an I/O error occurs
	 */
	Parser(final InputStreamReader isr, final AbstractRawHandler rawHandler)
			throws XmlomInvalidLtException, XmlInvalidLtException, IOException {
		this.consumer = new XmlConsumer(isr);
		this.rawHandler = rawHandler;
		LtSystemOut.debug("new instance");
		readMetaData();
		readRootElementOpen();
	}

	/**
	 *
	 * @throws XmlInvalidLtException If encounter an invalid syntax
	 * @throws IOException           If an I/O error occurs
	 */
	private void readMetaData() throws XmlInvalidLtException, IOException {
		while (!this.consumer.isEnded()
				&& (this.consumer.peekNextTagType() == null || XmlTagType.META_DATA.equals(this.consumer.peekNextTagType()))) {
			this.consumer.nextElement();
		}
	}

	/**
	 *
	 * @throws XmlomInvalidLtException If do not find the open root xmlom element
	 * @throws XmlInvalidLtException   If encounter an invalid syntax
	 * @throws IOException             If an I/O error occurs
	 */
	private void readRootElementOpen() throws XmlomInvalidLtException, XmlInvalidLtException, IOException {
		this.consumer.nextElement();
		if (!XmlTagType.OPEN.equals(this.consumer.getTagType())
				&& !XmlTagType.OPEN_CLOSE.equals(this.consumer.getTagType())) {
			throw new XmlomInvalidLtException("Invalid XML syntax, expected root_element '#0'", this.consumer);
		}
		LtSystemOut.debug("#0", this.consumer);
	}

	/**
	 *
	 * @throws XmlomInvalidLtException If do not find the close root xmlom element
	 * @throws XmlInvalidLtException   If is not a valid tag
	 */
	private void readRootElementClose() throws XmlomInvalidLtException, XmlInvalidLtException {
		if (!XmlTagType.CLOSE.equals(this.consumer.getTagType())
				&& !XmlTagType.OPEN_CLOSE.equals(this.consumer.getTagType())) {
			throw new XmlomInvalidLtException("Invalid XML syntax, expected end root_element '#0'", this.consumer);
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
	synchronized <I extends XmlObjectModelling> I read(final Class<I> interfaceClass) throws IllegalArgumentLtRtException,
			XmlomSecurityLtException, XmlomInvalidLtException, XmlInvalidLtException, IOException {
		I result = null;
		if (this.consumer != null) {
			result = readObject(interfaceClass);
			if (result == null) {
				close();
			} else {
				this.trackLoopObjects.clear();
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
			this.trackLoopObjects = null;
			this.consumer.close();
			this.consumer = null;
			if (this.rawHandler != null) {
				this.rawHandler.omClosed();
				this.rawHandler = null;
			}

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
	private <I extends XmlObjectModelling> I readObject(final Class<I> interfaceClass)
			throws XmlomSecurityLtException, XmlomInvalidLtException, XmlInvalidLtException, IOException {
		I obj = null;
		if (!this.consumer.isEnded()) {
			List<String> comments = null;
			// while is comment read to array - to set on the next object read
			while (XmlTagType.COMMENT.equals(this.consumer.peekNextTagType())) {
				if (comments == null) {
					comments = new ArrayList<>();
				}
				final StringWriter sw = new StringWriter();
				this.consumer.setWriteNextCommentTo(sw);
				this.consumer.nextElement();
				comments.add(sw.getBuffer().toString());
			}
			Object tmpObj = null;
			// read the next object
			if (XmlTagType.OPEN.equals(this.consumer.peekNextTagType())
					|| XmlTagType.OPEN_CLOSE.equals(this.consumer.peekNextTagType())) {
				this.consumer.nextElement();
				tmpObj = readObjectAux(comments);
				if (tmpObj == null) {
					throw new ImplementationLtRtException();
				}
				try {
					obj = interfaceClass.cast(tmpObj);
				} catch (final ClassCastException e) {
					throw new XmlomInvalidLtException(e);
				}
				this.consumer.nextElement();// close tag
			} else {
				// there is no next object
				if (comments != null) {
					throw new XmlomInvalidLtException("Invalid XMLOM, expected open element for the comments '#0'", comments);
				}
				this.consumer.nextElement();
				if (this.consumer.isEnded()) {
					// already ended
					readRootElementClose();
					close();
				} else {
					// invalid xmlom
					throw new XmlomInvalidLtException("Invalid XMLOM, expected open element '#0'", this.consumer);
				}
			}
		}
		return obj;
	}

	private Object tryfindObjectById(final List<String> comments) throws XmlInvalidLtException {
		Object object = null;
		final Integer id = getElementAttributeId();
		if (id != null) {
			if (comments != null) {
				throw new XmlomInvalidLtException("Invalid XMLOM, object refered by ID can not have comment");
			}
			object = this.trackLoopObjects.get(id);
			if (object == null) {
				throw new XmlomInvalidLtException();
			}
		}
		return object;
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
		if (this.consumer.getElementAttributeValue(this.sbTmpAttb, Producer.ATTRIBUTE_CLASS_NAME)
				&& this.sbTmpAttb.length() > 0) {
			final String className = TypeVsClassname.getClassname(this.sbTmpAttb.toString());
			try {
				dataClass = AgnosticUtil.getClass(className);
			} catch (final ClassNotFoundException e) {
				throw new XmlomInvalidLtException(e, "Invalid XMLOM element attribute class '#0'", this.consumer);
			}
			// parse object
			if (!XmlTagType.OPEN_CLOSE.equals(this.consumer.getTagType())) {
				if (LeafElement.has(dataClass)) {
					if (comments != null) {
						throw new XmlomInvalidLtException("Invalid XMLOM, leaf object can not have comment");
					}
					this.sbTmpVal.setLength(0);
					this.consumer.getElementValue(this.sbTmpVal);
					object = convertFromElementValue(dataClass, this.sbTmpVal, this.rawHandler);
				} else if (ArrayElement.has(dataClass)) {
					if (comments != null) {
						throw new XmlomInvalidLtException("Invalid XMLOM, array object can not have comment");
					}
					object = readArrayObject(dataClass);
				} else {
					if (!XmlObjectModelling.class.isAssignableFrom(dataClass)) {
						throw new IllegalArgumentLtRtException(dataClass.toString());
					}
					object = DATA_PROXY.newXmlObjectModelling((Class<I>) dataClass);
					final Integer id = getElementAttributeId();
					if (id != null) {
						this.trackLoopObjects.put(id, object);
					}
					if (!XmlTagType.CLOSE.equals(this.consumer.peekNextTagType())) {
						readObjectData(XmlObjectModelling.class.cast(object));
					}
					XmlObjectModelling.class.cast(object).setComments(comments);
				}
			} else {
				object = tryfindObjectById(comments);
				if (object == null && dataClass.isPrimitive()) {
					throw new XmlomInvalidLtException();
				}
			}
		} else if (XmlTagType.OPEN_CLOSE.equals(this.consumer.getTagType())) {
			object = tryfindObjectById(comments);
		} else {
			throw new XmlomInvalidLtException("Invalid XMLOM element without attribute type '#0'", this.consumer);
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
				throw new XmlomInvalidLtException(e, "Invalid value for attribute id '#0'", this.consumer);
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
	 * @throws IOException
	 */
	private static Object convertFromElementValue(final Class<?> classValue, final StringBuilder valueSb,
			final AbstractRawHandler rawHandler) throws XmlomInvalidLtException, IOException {
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
				throw new XmlomInvalidLtException("Invalid char - '#0'", valueSb.toString());
			}
			result = valueSb.charAt(0);
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_BYTE_CLASS)) {
			try {
				result = Byte.valueOf(valueSb.toString()).byteValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "Invalid byte - '#0'", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_SHORT_CLASS)) {
			try {
				result = Short.valueOf(valueSb.toString()).shortValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "Invalid short - '#0'", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_INT_CLASS)) {
			try {
				result = Integer.valueOf(valueSb.toString()).intValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "Invalid int - '#0'", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_LONG_CLASS)) {
			try {
				result = Long.valueOf(valueSb.toString()).longValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "Invalid long - '#0'", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_FLOAT_CLASS)) {
			try {
				result = Float.valueOf(valueSb.toString()).floatValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "Invalid float - '#0'", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_DOUBLE_CLASS)) {
			try {
				result = Double.valueOf(valueSb.toString()).doubleValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "Invalid double - '#0'", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_BOOLEAN_CLASS)) {
			try {
				result = Boolean.valueOf(valueSb.toString()).booleanValue();
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e, "Invalid boolean - '#0'", valueSb.toString());
			}
		} else if (classValue.equals(AgnosticUtil.PRIMITIVE_CHAR_CLASS)) {
			if (valueSb.length() != 1) {
				throw new XmlomInvalidLtException("Invalid char - '#0'", valueSb.toString());
			}
			result = valueSb.charAt(0);
		} else if (classValue.equals(InputStream.class)) {
			if (rawHandler == null) {
				throw new XmlomInvalidLtException("this parser needs to receive a raw handler different to null");
			}
			try {
				result = rawHandler.read(Long.valueOf(valueSb.toString()));
			} catch (final NumberFormatException e) {
				throw new XmlomInvalidLtException(e);
			}
		} else {
			// Enum
			try {
				final Method method = AgnosticUtil.getMethod(classValue, Constant.VALUEOF_METHOD_NAME, String.class);
				result = AgnosticUtil.invoke(classValue, method, valueSb.toString());
			} catch (final SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalArgumentLtRtException
					| IllegalAccessException e) {
				throw new XmlomInvalidLtException(e);
			} catch (final InvocationTargetException e) {
				throw new XmlomInvalidLtException(e, "Invalid call '#0' from class '#1' with arg '#2'",
						Constant.VALUEOF_METHOD_NAME, classValue.getSimpleName(), valueSb.toString());
			}
		}
		return result;
	}

	private <I extends XmlObjectModelling> void readObjectData(final I object)
			throws XmlomInvalidLtException, XmlInvalidLtException, IOException {
		final DataProxyHandler dph = DATA_PROXY.getHandler(object);
		final Map<String, Object> dphData = dph.getDataMap();
		String dataName;
		boolean isElementTagOpenClose;
		List<String> comments;
		while (!XmlTagType.CLOSE.equals(this.consumer.peekNextTagType())) {
			comments = null;
			// while is comment read to array - to set on the next object read
			while (XmlTagType.COMMENT.equals(this.consumer.peekNextTagType())) {
				if (comments == null) {
					comments = new ArrayList<>();
				}
				final StringWriter sw = new StringWriter();
				this.consumer.setWriteNextCommentTo(sw);
				this.consumer.nextElement();
				comments.add(sw.getBuffer().toString());
			}
			if (!XmlTagType.OPEN.equals(this.consumer.peekNextTagType())
					&& !XmlTagType.OPEN_CLOSE.equals(this.consumer.peekNextTagType())) {
				this.consumer.nextElement();
				throw new XmlomInvalidLtException("Invalid XMLOM, expected open element '#0'", this.consumer);
			}
			this.consumer.nextElement();
			isElementTagOpenClose = XmlTagType.OPEN_CLOSE.equals(this.consumer.getTagType());
			dataName = this.consumer.getElementName().toString();
			if (!dph.existsDataName(dataName)) {
				throw new XmlomInvalidLtException("Invalid XMLOM, data parser, fail to set '#0' in object '#1'", dataName,
						dph.getInterface().getName());
			}
			dphData.put(dataName, readObjectAux(comments));
			if (!isElementTagOpenClose) {
				this.consumer.nextElement();// close element
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
			while (!XmlTagType.CLOSE.equals(this.consumer.peekNextTagType())) {
				if (!XmlTagType.OPEN.equals(this.consumer.peekNextTagType())
						&& !XmlTagType.OPEN_CLOSE.equals(this.consumer.peekNextTagType())) {
					this.consumer.nextElement();
					throw new XmlomInvalidLtException("Invalid XMLOM, expected open element '#0'", this.consumer);
				}
				this.consumer.nextElement();
				isElementTagOpenClose = XmlTagType.OPEN_CLOSE.equals(this.consumer.getTagType());
				arrayList.add(readObjectAux(null));
				if (!isElementTagOpenClose) {
					this.consumer.nextElement();// close element
				}
			}
			object = Array.newInstance(componentType, arrayList.size());
			try {
				for (int i = 0; i < arrayList.size(); i++) {
					Array.set(object, i, arrayList.get(i));
				}
			} catch (final IllegalArgumentException e) {
				throw new XmlomInvalidLtException(e, "A primitive array can't have a null element");
			}
		} else if (List.class.equals(dataClass)) {
			// LIST
			final List<Object> list = new ArrayList<>();
			while (!XmlTagType.CLOSE.equals(this.consumer.peekNextTagType())) {
				if (!XmlTagType.OPEN.equals(this.consumer.peekNextTagType())
						&& !XmlTagType.OPEN_CLOSE.equals(this.consumer.peekNextTagType())) {
					this.consumer.nextElement();
					throw new XmlomInvalidLtException("Invalid XMLOM, expected open element '#0'", this.consumer);
				}
				this.consumer.nextElement();
				isElementTagOpenClose = XmlTagType.OPEN_CLOSE.equals(this.consumer.getTagType());
				list.add(readObjectAux(null));
				if (!isElementTagOpenClose) {
					this.consumer.nextElement();// close element
				}
			}
			object = list;
		} else if (Set.class.equals(dataClass)) {
			// SET
			final Set<Object> set = new HashSet<>();
			while (!XmlTagType.CLOSE.equals(this.consumer.peekNextTagType())) {
				if (!XmlTagType.OPEN.equals(this.consumer.peekNextTagType())
						&& !XmlTagType.OPEN_CLOSE.equals(this.consumer.peekNextTagType())) {
					this.consumer.nextElement();
					throw new XmlomInvalidLtException("Invalid XMLOM, expected open element '#0'", this.consumer);
				}
				this.consumer.nextElement();
				isElementTagOpenClose = XmlTagType.OPEN_CLOSE.equals(this.consumer.getTagType());
				set.add(readObjectAux(null));
				if (!isElementTagOpenClose) {
					this.consumer.nextElement();// close element
				}
			}
			object = set;
		} else if (Map.class.equals(dataClass)) {
			// MAP
			final Map<Object, Object> map = new HashMap<>();
			Object key = null;
			Object value = null;
			while (!XmlTagType.CLOSE.equals(this.consumer.peekNextTagType())) {
				if (!XmlTagType.OPEN.equals(this.consumer.peekNextTagType())
						&& !XmlTagType.OPEN_CLOSE.equals(this.consumer.peekNextTagType())) {
					this.consumer.nextElement();
					throw new XmlomInvalidLtException("Invalid XMLOM, expected open element '#0'", this.consumer);
				}
				if (XmlTagType.OPEN_CLOSE.equals(this.consumer.peekNextTagType())) {
					this.consumer.nextElement();
					throw new XmlomInvalidLtException("Invalid XMLOM syntax, map element can't be null '#0'", this.consumer);
				}
				this.consumer.nextElement();// open element 'element' (only to structure)
				while (!XmlTagType.CLOSE.equals(this.consumer.peekNextTagType())) {
					if (!XmlTagType.OPEN.equals(this.consumer.peekNextTagType())
							&& !XmlTagType.OPEN_CLOSE.equals(this.consumer.peekNextTagType())) {
						this.consumer.nextElement();
						throw new XmlomInvalidLtException("Invalid XMLOM, expected open element '#0'", this.consumer);
					}
					this.consumer.nextElement();
					isElementTagOpenClose = XmlTagType.OPEN_CLOSE.equals(this.consumer.getTagType());
					this.sbTmpElmName.setLength(0);
					this.sbTmpElmName.append(this.consumer.getElementName());
					if (StringUtil.isEquals(Producer.ELEMENT_NAME_MAP_KEY, this.sbTmpElmName)) {
						key = readObjectAux(null);
					} else if (StringUtil.isEquals(Producer.ELEMENT_NAME_MAP_VALUE, this.sbTmpElmName)) {
						value = readObjectAux(null);
					} else {
						new IOException(new XmlomInvalidLtException("read map elements -> ignore tag '#0'", this.consumer));
					}
					if (!isElementTagOpenClose) {
						this.consumer.nextElement();// close element
					}
				}
				this.consumer.nextElement();// close element 'element' (only to structure)
				map.put(key, value);
				key = null;
				value = null;
			}
			object = map;
		} else {
			throw new ImplementationLtRtException(
					"Something wrong (Defined an array in 'ArrayElement.ARRAY_CLASS' element which isn't implemented!)");
		}
		return object;
	}

}

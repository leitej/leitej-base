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
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import leitej.LtSystemOut;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.ImplementationLtRtException;
import leitej.exception.XmlInvalidLtException;
import leitej.exception.XmlomInvalidLtException;
import leitej.util.AgnosticUtil;
import leitej.util.HexaUtil;
import leitej.xml.XmlProducer;

/**
 * XML - Object Modeling - Producer
 *
 * @author Julio Leite
 * @see leitej.xml.om.Parser
 */
final class Producer {

	private static final DataProxy DATA_PROXY = DataProxy.getInstance();

	private static final String ROOT_ELEMENT_NAME = Object.class.getSimpleName();
	private static final Class<XmlObjectModelling> ROOT_ELEMENT_CLASS = XmlObjectModelling.class;
	static final String ELEMENT_NAME_ARRAYCLASS = "el", ELEMENT_NAME_MAP_KEY = "key", ELEMENT_NAME_MAP_VALUE = "value",
			ATTRIBUTE_ID = "id", ATTRIBUTE_CLASS_NAME = "type";
//			ATTRIBUTE_CLASS_FACE_NAME = "face";

	private static final Class<?> BYTE_ARRAY_CLASS = Array.newInstance(byte.class, 0).getClass();

	private XmlProducer producer = null;
	private List<XmlObjectModelling> objectSet = new ArrayList<>();
	private Map<Object, Integer> trackLoopObjects = new HashMap<>();
	private Integer objectCount = 0;
	private final StringBuilder sbTmpAttb = new StringBuilder();
	private final StringBuilder sbTmpVal = new StringBuilder();
	private final StringBuilder sbTmpElmName = new StringBuilder();
	private final StringBuilder sbTmpAttbSub1 = new StringBuilder();
	private final StringBuilder sbTmpAttbSub2 = new StringBuilder();
	private boolean init = false;
	private boolean done = false;

	/**
	 * Creates a new instance of Producer.
	 *
	 * @param osr      the underlying output stream to be written
	 * @param minified when false produces a human readable XML, other wise outputs
	 *                 a clean strait line
	 */
	Producer(final OutputStreamWriter osr, final boolean minified) {
		this.producer = new XmlProducer(osr, minified);
		LtSystemOut.debug("lt.NewInstance");
	}

	synchronized <I extends XmlObjectModelling> void add(final I obj) {
		if (this.done) {
			throw new IllegalStateLtRtException("lt.XmlOmProducerAlreadyDone");
		}
		if (obj != null) {
			this.objectSet.add(obj);
		}
	}

	synchronized <I extends XmlObjectModelling> void add(final I[] objs) {
		if (this.done) {
			throw new IllegalStateLtRtException("lt.XmlOmProducerAlreadyDone");
		}
		if (objs != null) {
			for (final XmlObjectModelling o : objs) {
				this.add(o);
			}
		}
	}

	private void init() throws XmlInvalidLtException, IOException {
		if (!this.init) {
			try {
				printMetaData();
				printRootElementTagOpen();
			} finally {
				this.init = true;
			}
		}
	}

	synchronized void flush() throws XmlInvalidLtException, IOException {
		if (this.done) {
			throw new IllegalStateLtRtException("lt.XmlOmProducerAlreadyDone");
		}
		init();
		printObjectSet();
		this.producer.flush();
		this.trackLoopObjects.clear();
		this.objectCount = 0;
	}

	synchronized void doFinal() throws XmlInvalidLtException, IOException {
		if (!this.done) {
			flush();
			try {
				printRootElementTagClose();
				this.producer.flush();
			} finally {
				this.done = true;
				this.objectSet = null;
				this.trackLoopObjects = null;
			}
		}
	}

	synchronized void close() throws IOException, XmlInvalidLtException {
		if (this.producer != null) {
			doFinal();
			this.producer.close();
			this.producer = null;
		}
	}

	private Integer registId(final Object obj) {
		Integer result = null;
		if (obj != null) {
			result = this.trackLoopObjects.get(obj);
			++this.objectCount;
			if (result == null) {
				result = this.objectCount;
				this.trackLoopObjects.put(obj, result);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private <I extends XmlObjectModelling> void printObjectSet() throws IOException, XmlInvalidLtException {
		final Object[] objs = this.objectSet.toArray();
		LtSystemOut.debug("objs.length: #0", objs.length);
		Class<I> typeClass;
		for (final Object o : objs) {
			if (o != null) {
				if (!this.objectSet.remove(o)) {
					throw new XmlomInvalidLtException("lt.XmlOmNotRemovingObject");
				}
				typeClass = DATA_PROXY.getInvocationHandler((I) o).getInterface();
				this.sbTmpElmName.setLength(0);
				if (XmlObjectModelling.class.equals(typeClass)) {
					this.sbTmpElmName.append("xom");
					printObject(null, DATA_PROXY.getInvocationHandler((I) o).getByteArray(), BYTE_ARRAY_CLASS,
							this.sbTmpElmName);
				} else {
					this.sbTmpElmName.append(typeClass.getSimpleName());
					printObject(null, o, typeClass, this.sbTmpElmName);
				}
			}
		}
	}

	/**
	 * As interfaces so podem ter dados com o tipo: LeafElement Interface Collection
	 * com parameterizacao directa: LeafElement ou Interface ArrayElement
	 * multidimensional com componente: LeafElement ou Interface
	 *
	 * @param method
	 * @param obj
	 * @param typeClass
	 * @param elementName
	 * @throws XmlInvalidLtException
	 * @throws IOException
	 * @throws IllegalArgumentLtRtException if is not a valid interface
	 *
	 * @see TrustClassname#registry(Class)
	 */
	@SuppressWarnings("unchecked")
	private <I extends XmlObjectModelling> void printObject(final Method method, final Object obj,
			final Class<?> typeClass, final StringBuilder elementName) throws XmlInvalidLtException, IOException {
		this.sbTmpAttb.setLength(0);
		final StringBuilder localElementName = new StringBuilder(elementName);
		if (LeafElement.has(typeClass) || obj == null) {
			genAttribute(this.sbTmpAttb, typeClass);
			this.producer.printElement(localElementName,
					((obj == null) ? null : convertToElementValue(this.sbTmpVal, obj)), this.sbTmpAttb);
		} else {
			if (ArrayElement.has(typeClass)) {
				genAttribute(this.sbTmpAttb, typeClass);
				this.producer.printTagOpen(localElementName, this.sbTmpAttb);
				Class<?>[] typeComponentClass;
				if (typeClass.isArray()) {
					if (!LeafElement.has(typeClass.getComponentType())
							&& !XmlObjectModelling.class.isAssignableFrom(typeClass.getComponentType())
							&& !typeClass.getComponentType().isArray()) {
						throw new IllegalArgumentLtRtException(typeClass.toString());
					}
					typeComponentClass = new Class[] { typeClass.getComponentType() };
				} else if (List.class.isAssignableFrom(typeClass) || Set.class.equals(typeClass)) {
					typeComponentClass = AgnosticUtil.getReturnParameterizedTypes(method);
					if (!LeafElement.has(typeComponentClass[0])
							&& !XmlObjectModelling.class.isAssignableFrom(typeComponentClass[0])) {
						throw new IllegalArgumentLtRtException(typeClass.toString());
					}
				} else if (Map.class.equals(typeClass)) {
					typeComponentClass = AgnosticUtil.getReturnParameterizedTypes(method);
					if (!LeafElement.has(typeComponentClass[0])
							&& !XmlObjectModelling.class.isAssignableFrom(typeComponentClass[0])) {
						throw new IllegalArgumentLtRtException(typeClass.toString());
					}
					if (!LeafElement.has(typeComponentClass[1])
							&& !XmlObjectModelling.class.isAssignableFrom(typeComponentClass[1])) {
						throw new IllegalArgumentLtRtException(typeClass.toString());
					}
				} else {
					throw new IllegalArgumentLtRtException(new ImplementationLtRtException(typeClass.toString()));
				}
				printArrayElement(obj, typeComponentClass);
				this.producer.printTagClose(localElementName);
			} else {
				final Integer registId = registId(obj);
				genAttribute(this.sbTmpAttb, typeClass, registId);
				if (registId.equals(this.objectCount)) {
					if (!XmlObjectModelling.class.isInstance(obj)) {
						throw new IOException(new IllegalArgumentLtRtException(obj.getClass().getName()));
					}
					if (((I) obj).getComments() != null) {
						for (final String comment : ((I) obj).getComments()) {
							this.producer.printComment(comment);
						}
					}
					this.producer.printTagOpen(localElementName, this.sbTmpAttb);
					printMethods((I) obj);
					this.producer.printTagClose(localElementName);
				} else {
					this.producer.printElement(localElementName, null, this.sbTmpAttb);
				}
			}
		}
	}

	private static StringBuilder convertToElementValue(final StringBuilder dest, final Object value) {
		dest.setLength(0);
		if (Date.class.isInstance(value)) {
			dest.append(((Long) ((Date) value).getTime()).toString());
		} else if (BYTE_ARRAY_CLASS.isInstance(value)) {
			HexaUtil.toHex(dest, (byte[]) value);
		} else {
			dest.append(value.toString());
		}
		return dest;
	}

	@SuppressWarnings("unchecked")
	private <I extends XmlObjectModelling> void printMethods(final I o) throws IOException, XmlInvalidLtException {
		LtSystemOut.debug("lt.XmlOmProcessObject", o.getClass().getSimpleName());
		final DataProxyHandler dph = DATA_PROXY.getInvocationHandler(o);
		final Map<String, Object> dphData = dph.getDataMap();
		Object data;
		for (final String dataName : dph.getDataNames()) {
			data = dphData.get(dataName);
			if (data != null) {
				final Method mGet = dph.getMethodsGetSet(dataName)[0];
				this.sbTmpElmName.setLength(0);
				this.sbTmpElmName.append(dataName);
				if (XmlObjectModelling.class.isInstance(data)) {
					printObject(mGet, data, DATA_PROXY.getInvocationHandler((I) data).getInterface(),
							this.sbTmpElmName);
				} else {
					printObject(mGet, data, mGet.getReturnType(), this.sbTmpElmName);
				}
			}
		}
	}

	private void printArrayElement(final Object obj, final Class<?>[] typeComponentClass)
			throws XmlInvalidLtException, IOException {
		if (obj.getClass().isArray()) {
			final Object[] array = (Object[]) obj;
			for (final Object o : array) {
				this.sbTmpElmName.setLength(0);
				this.sbTmpElmName.append(ELEMENT_NAME_ARRAYCLASS);
				printObject(null, o, typeComponentClass[0], this.sbTmpElmName);
			}
		} else if (List.class.isAssignableFrom(obj.getClass())) {
			final List<?> list = (List<?>) obj;
			for (final Object o : list) {
				this.sbTmpElmName.setLength(0);
				this.sbTmpElmName.append(ELEMENT_NAME_ARRAYCLASS);
				printObject(null, o, typeComponentClass[0], this.sbTmpElmName);
			}
		} else if (Set.class.isAssignableFrom(obj.getClass())) {
			final Set<?> set = (Set<?>) obj;
			for (final Object o : set) {
				this.sbTmpElmName.setLength(0);
				this.sbTmpElmName.append(ELEMENT_NAME_ARRAYCLASS);
				printObject(null, o, typeComponentClass[0], this.sbTmpElmName);
			}
		} else if (Map.class.isAssignableFrom(obj.getClass())) {
			final Map<?, ?> map = (Map<?, ?>) obj;
			Object o = null;
			final Set<?> keys = map.keySet();
			for (final Object key : keys) {
				o = map.get(key);
				this.sbTmpElmName.setLength(0);
				this.sbTmpElmName.append(ELEMENT_NAME_ARRAYCLASS);
				this.producer.printTagOpen(this.sbTmpElmName, null);
				this.sbTmpElmName.setLength(0);
				this.sbTmpElmName.append(ELEMENT_NAME_MAP_KEY);
				printObject(null, key, typeComponentClass[0], this.sbTmpElmName);
				this.sbTmpElmName.setLength(0);
				this.sbTmpElmName.append(ELEMENT_NAME_MAP_VALUE);
				printObject(null, o, typeComponentClass[1], this.sbTmpElmName);
				this.sbTmpElmName.setLength(0);
				this.sbTmpElmName.append(ELEMENT_NAME_ARRAYCLASS);
				this.producer.printTagClose(this.sbTmpElmName);
			}
		} else {
			throw new ImplementationLtRtException("lt.XmlOmArrayImplementBug");
		}
	}

	private void printMetaData() throws IOException, XmlInvalidLtException {
		this.producer.printMetaData();
	}

	private void printRootElementTagOpen() throws IOException, XmlInvalidLtException {
		this.sbTmpAttb.setLength(0);
		this.sbTmpElmName.setLength(0);
		this.sbTmpElmName.append(ROOT_ELEMENT_NAME);
		this.producer.printTagOpen(this.sbTmpElmName, genAttribute(this.sbTmpAttb, ROOT_ELEMENT_CLASS));
	}

	private void printRootElementTagClose() throws IOException, XmlInvalidLtException {
		this.sbTmpElmName.setLength(0);
		this.sbTmpElmName.append(ROOT_ELEMENT_NAME);
		this.producer.printTagClose(this.sbTmpElmName);
	}

	private StringBuilder genAttribute(final StringBuilder dest, final Class<?> typeClass) {
		this.sbTmpAttbSub1.setLength(0);
		this.sbTmpAttbSub1.append(ATTRIBUTE_CLASS_NAME);
		this.sbTmpAttbSub2.setLength(0);
		this.sbTmpAttbSub2.append(TypeVsClassname.getType(typeClass.getName()));
		this.producer.genAttribute(dest, this.sbTmpAttbSub1, this.sbTmpAttbSub2);
		return dest;
	}

	private StringBuilder genAttribute(final StringBuilder dest, final Class<?> typeClass, final Integer id) {
		this.sbTmpAttbSub1.setLength(0);
		this.sbTmpAttbSub1.append(ATTRIBUTE_CLASS_NAME);
		this.sbTmpAttbSub2.setLength(0);
		this.sbTmpAttbSub2.append(TypeVsClassname.getType(typeClass.getName()));
		this.producer.genAttribute(dest, this.sbTmpAttbSub1, this.sbTmpAttbSub2);
		this.sbTmpAttbSub1.setLength(0);
		this.sbTmpAttbSub1.append(ATTRIBUTE_ID);
		this.sbTmpAttbSub2.setLength(0);
		this.sbTmpAttbSub2.append(id);
		this.producer.genAttribute(dest, this.sbTmpAttbSub1, this.sbTmpAttbSub2);
		return dest;
	}

}

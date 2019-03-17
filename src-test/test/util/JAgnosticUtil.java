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

package test.util;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.LtException;
import leitej.util.AgnosticUtil;
import leitej.util.data.Invoke;

public class JAgnosticUtil {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = NoSuchMethodException.class)
	public final void testGetMethod_execption() throws SecurityException, NoSuchMethodException, LtException {
		AgnosticUtil.getMethod(this, "testgetmethod_execption");
	}

	@Test
	public final void testGetMethod() throws SecurityException, NoSuchMethodException, LtException {
		AgnosticUtil.getMethod(this, "testGetMethod");
		assertTrue(true);
	}

	@Test
	public final void testInvokeInvokeItf() throws SecurityException, NoSuchMethodException, LtException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		final Method method = AgnosticUtil.getMethod(this, "tii");
		final String result = (String) AgnosticUtil.invoke(new Invoke(this, method));
		assertTrue(result.equals("hi"));
	}

	public String tii() {
		return "hi";
	}

	@Test
	public final void testInvokeObjectMethodObjectArray() throws IllegalArgumentException, SecurityException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException, LtException {
		final String result = (String) AgnosticUtil.invoke(this, AgnosticUtil.getMethod(this, "tii"));
		assertTrue(result.equals("hi"));
	}

	@Test
	public final void testIsArray() {
		assertTrue(AgnosticUtil.isArray("[Ljava.lang.String;") == true);
		assertTrue(AgnosticUtil.isArray("[Ljava.lang.String") == true);
		assertTrue(AgnosticUtil.isArray("[java.lang.String;") == true);
		assertTrue(AgnosticUtil.isArray("[[Ljava.lang.String;") == true);
		assertTrue(AgnosticUtil.isArray("[Ljava.lang.String;;") == true);
		assertTrue(AgnosticUtil.isArray("java.lang.String") == false);
		assertTrue(AgnosticUtil.isArray("[Lint;") == true);
	}

	@Test
	public final void testGetClassString_int() throws ClassNotFoundException {
		assertTrue(int.class.equals(AgnosticUtil.getClass("int")));
	}

	@Test
	public final void testGetClassString_int_array() throws ClassNotFoundException {
		assertTrue(int[].class.equals(AgnosticUtil.getClass("[I")));
	}

	@Test
	public final void testGetClassString_String() throws ClassNotFoundException {
		assertTrue(String.class.equals(AgnosticUtil.getClass("java.lang.String")));
	}

	@Test(expected = ClassNotFoundException.class)
	public final void testGetClassString_String_exception() throws ClassNotFoundException {
		assertTrue(String.class.equals(AgnosticUtil.getClass("String")));
	}

	@Test(expected = ClassNotFoundException.class)
	public final void testGetClassString_String_array_exception() throws ClassNotFoundException {
		assertTrue(String.class.equals(AgnosticUtil.getClass("[java.lang.String;")));
	}

	@Test(expected = ClassNotFoundException.class)
	public final void testGetClassString_String_array_exception2() throws ClassNotFoundException {
		assertTrue(String.class.equals(AgnosticUtil.getClass("[Ljava.lang.String")));
	}

	@Test
	public final void testGetClassString_String_Array() throws ClassNotFoundException {
		assertTrue(String[].class.equals(AgnosticUtil.getClass("[Ljava.lang.String;")));
		assertTrue(String[][].class.equals(AgnosticUtil.getClass("[[Ljava.lang.String;")));
	}

	@Test
	public final void testGetReturnType_string() throws SecurityException, NoSuchMethodException, LtException {
		assertTrue(String.class.equals(AgnosticUtil.getReturnType(AgnosticUtil.getMethod(this, "tgrts"))));
	}

	public String tgrts() {
		return null;
	}

	@Test
	public final void testGetReturnType_void() throws SecurityException, NoSuchMethodException, LtException {
		assertTrue(AgnosticUtil.getReturnType(AgnosticUtil.getMethod(this, "tgrtv")) == null);
	}

	public void tgrtv() {
	}

	@Test
	public final void testGetParameterizedClasses()
			throws IllegalArgumentLtRtException, SecurityException, NoSuchMethodException {
		Class<?>[] parameterizedClasses = AgnosticUtil
				.getParameterizedClasses(AgnosticUtil.getMethod(this, "tgrtss").getGenericReturnType());
		assertTrue(List.class.equals(parameterizedClasses[0]));
		parameterizedClasses = AgnosticUtil
				.getParameterizedClasses(AgnosticUtil.getMethod(this, "tgrtsss").getGenericReturnType());
		assertTrue(String.class.equals(parameterizedClasses[0]));
		parameterizedClasses = AgnosticUtil
				.getParameterizedClasses(AgnosticUtil.getMethod(this, "tgrtsssm").getGenericReturnType());
		assertTrue(Integer.class.equals(parameterizedClasses[0]));
		assertTrue(Map.class.equals(parameterizedClasses[1]));
		parameterizedClasses = AgnosticUtil.getReturnParameterizedTypes(AgnosticUtil.getMethod(this, "tgrtsssar"));
		assertTrue(Array.newInstance(int.class, 0, 0).getClass().equals(parameterizedClasses[0]));
		assertTrue(AgnosticUtil.getReturnParameterizedTypes(AgnosticUtil.getMethod(this, "tgrtsssra")) == null);
	}

	public List<List<String>> tgrtss() {
		return null;
	}

	public List<String> tgrtsss() {
		return null;
	}

	public List<int[][]> tgrtsssar() {
		return null;
	}

	public List<String>[] tgrtsssra() {
		return null;
	}

	public Map<Integer, Map<String, String>> tgrtsssm() {
		return null;
	}

}

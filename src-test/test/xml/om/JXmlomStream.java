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

package test.xml.om;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.Constant;
import leitej.exception.IllegalStateLtRtException;
import leitej.exception.XmlInvalidLtException;
import leitej.exception.XmlomInvalidLtException;
import leitej.util.data.Stopwatch;
import leitej.util.stream.SniffOutputStream;
import leitej.xml.om.XmlObjectModelling;
import leitej.xml.om.XmlomIOStream;
import leitej.xml.om.XmlomInputStream;
import leitej.xml.om.XmlomOutputStream;

public class JXmlomStream {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_empty() throws NullPointerException, ClassCastException, XmlInvalidLtException, IOException {
		final byte[] t3 = ("<?xml version=\"1.0\" encoding=\"UTF8\"?>\r\n"
				+ "<Object type=\"leitej.XmlObjectModelling\">\r\n" + "</Object>\r\n").getBytes();
		final ByteArrayInputStream bIn = new ByteArrayInputStream(t3);
		final XmlomInputStream xis = new XmlomInputStream(bIn, Constant.UTF8_CHARSET_NAME);
		assertTrue(xis.read(Sample.class) == null);
		xis.close();
//		XmlomOutputStream xos = new XmlomOutputStream(System.out, Constant.UTF8_CHARSET_NAME);
//		xos.write(XmlomUtil.newXmlObjectModelling(Sample.class));
//		xos.write(XmlomUtil.newXmlObjectModelling(Sample2.class));
//		xos.write(XmlomUtil.newXmlObjectModelling(Sample3.class));
//		xos.write(123);
//		xos.close();
	}

	@Test(expected = XmlomInvalidLtException.class)
	public void test_ee_primitive_null() throws IOException, ClassCastException, XmlInvalidLtException {
		final byte[] t3 = ("<?xml version=\"1.0\" encoding=\"UTF8\"?>\r\n"
				+ "<Object type=\"leitej.XmlObjectModelling\">\r\n"
				+ "	<Sample type=\"test.xml.om.Sample\" id=\"1\">\r\n" + "		<name type=\"String\"/>\r\n"
				+ "		<date type=\"Date\"/>\r\n" + "		<zbyte type=\"Byte\"/>\r\n"
				+ "		<zshort type=\"Short\"/>\r\n" + "		<zint type=\"Integer\"/>\r\n"
				+ "		<zlong type=\"Long\"/>\r\n" + "		<zfloat type=\"Float\"/>\r\n"
				+ "		<zdouble type=\"Double\"/>\r\n" + "		<zboolean type=\"Boolean\"/>\r\n"
				+ "		<zchar type=\"Character\"/>\r\n" + "		<pbyte type=\"Pbyte\">0</pbyte>\r\n"
				+ "		<pshort type=\"Pshort\">0</pshort>\r\n" + "		<pint type=\"Pint\"/>\r\n" + // illegal
																										// primitive
																										// null
				"		<plong type=\"Plong\">0</plong>\r\n" + "		<pfloat type=\"Pfloat\">0.0</pfloat>\r\n"
				+ "		<pdouble type=\"Pdouble\">0.0</pdouble>\r\n"
				+ "		<pboolean type=\"Pboolean\">false</pboolean>\r\n"
				+ "		<pchar type=\"Pchar\">\0</pchar>\r\n" + "		<filho type=\"test.xml.om.Sample\"/>\r\n"
				+ "		<ao type=\"[LString;\"/>\r\n" + "		<so type=\"Set\"/>\r\n" + "		<mo type=\"Map\"/>\r\n"
				+ "		<lo type=\"List\"/>\r\n" + "		<am type=\"[Ltest.xml.om.Sample;\"/>\r\n"
				+ "		<as type=\"[[[LString;\"/>\r\n" + "	</Sample>\r\n" + "</Object>\r\n").getBytes();
		final ByteArrayInputStream bIn = new ByteArrayInputStream(t3);
		final XmlomInputStream xis = new XmlomInputStream(bIn, Constant.UTF8_CHARSET_NAME);
		xis.read(Sample.class);
		fail();
		xis.close();
	}

	@Test(expected = XmlomInvalidLtException.class)
	public void test_ee1_primitive_null() throws IOException, ClassCastException, XmlInvalidLtException {
		final byte[] t3 = ("<?xml version=\"1.0\" encoding=\"UTF8\"?>\r\n"
				+ "<Object type=\"leitej.XmlObjectModelling\">\r\n"
				+ "	<Sample type=\"test.xml.om.Sample\" id=\"1\">\r\n" + "		<name type=\"String\"/>\r\n"
				+ "		<date type=\"Date\"/>\r\n" + "		<zbyte type=\"Byte\"/>\r\n"
				+ "		<zshort type=\"Short\"/>\r\n" + "		<zint type=\"Integer\"/>\r\n"
				+ "		<zlong type=\"Long\"/>\r\n" + "		<zfloat type=\"Float\"/>\r\n"
				+ "		<zdouble type=\"Double\"/>\r\n" + "		<zboolean type=\"Boolean\"/>\r\n"
				+ "		<zchar type=\"Character\"/>\r\n" + "		<pbyte type=\"Pbyte\">0</pbyte>\r\n"
				+ "		<pshort type=\"Pshort\"></pshort>\r\n" + // illegal primitive without value
				"		<pint type=\"Pint\">0</pint>\r\n" + "		<plong type=\"Plong\">0</plong>\r\n"
				+ "		<pfloat type=\"Pfloat\">0.0</pfloat>\r\n" + "		<pdouble type=\"Pdouble\">0.0</pdouble>\r\n"
				+ "		<pboolean type=\"Pboolean\">false</pboolean>\r\n"
				+ "		<pchar type=\"Pchar\">\0</pchar>\r\n" + "		<filho type=\"test.xml.om.Sample\"/>\r\n"
				+ "		<ao type=\"[LString;\"/>\r\n" + "		<so type=\"Set\"/>\r\n" + "		<mo type=\"Map\"/>\r\n"
				+ "		<lo type=\"List\"/>\r\n" + "		<am type=\"[Ltest.xml.om.Sample;\"/>\r\n"
				+ "		<as type=\"[[[LString;\"/>\r\n" + "	</Sample>\r\n" + "</Object>\r\n").getBytes();
		final ByteArrayInputStream bIn = new ByteArrayInputStream(t3);
		final XmlomInputStream xis = new XmlomInputStream(bIn, Constant.UTF8_CHARSET_NAME);
		xis.read(Sample.class);
		fail();
		xis.close();
	}

	@Test
	public void test_array_byte() throws IOException, XmlInvalidLtException {
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final XmlomOutputStream xos = new XmlomOutputStream(new SniffOutputStream(bOut, null),
				Constant.UTF8_CHARSET_NAME);
		final Sample2 inS2 = XmlomIOStream.newXmlObjectModelling(Sample2.class);
		xos.write(inS2);
		xos.flush();
		inS2.setAb(new byte[] {});
		xos.write(inS2);
		xos.flush();
		final byte[] ab = new byte[4 * 1024];
		(new Random()).nextBytes(ab);
		inS2.setAb(ab);
		xos.write(inS2);
		xos.close();
		final ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
		final XmlomInputStream xis = new XmlomInputStream(bIn, Constant.UTF8_CHARSET_NAME);
		assertTrue(xis.read(Sample2.class).getAb() == null);
		assertTrue(xis.read(Sample2.class).getAb().length == 0);
		final byte[] abIn = xis.read(Sample2.class).getAb();
		assertTrue(Arrays.equals(ab, abIn));
		xis.close();
	}

	@Test
	public void test() throws IOException, XmlInvalidLtException {
		int erro = 0;
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final XmlomOutputStream xos = new XmlomOutputStream(new SniffOutputStream(bOut, null),
				Constant.UTF8_CHARSET_NAME);
		assertTrue(bOut.toByteArray().length == 0);
		xos.flush();
		assertTrue(bOut.toByteArray().length != 0);
		final byte[] t1 = ("<?xml version=\"1.0\" encoding=\"UTF8\"?>" + "<Object type=\"leitej.XmlObjectModelling\">")
				.getBytes();
		assertTrue(Arrays.equals(bOut.toByteArray(), t1));
		final Sample inS = XmlomIOStream.newXmlObjectModelling(Sample.class);
		xos.write(inS);
		assertTrue(Arrays.equals(bOut.toByteArray(), t1));
		xos.flush();
		final byte[] t2 = ("<?xml version=\"1.0\" encoding=\"UTF8\"?>" + "<Object type=\"leitej.XmlObjectModelling\">"
				+ "<Sample type=\"test.xml.om.Sample\" id=\"1\">" + "</Sample>").getBytes();
		assertTrue(Arrays.equals(bOut.toByteArray(), t2));
		xos.write(123);
		assertTrue(Arrays.equals(bOut.toByteArray(), t2));
		xos.doFinal();
		final byte[] t3 = ("<?xml version=\"1.0\" encoding=\"UTF8\"?>" + "<Object type=\"leitej.XmlObjectModelling\">"
				+ "<Sample type=\"test.xml.om.Sample\" id=\"1\">" + "</Sample>" + "<xom type=\"[B\">7B</xom>"
				+ "</Object>").getBytes();
		assertTrue(Arrays.equals(bOut.toByteArray(), t3));
		try {
			xos.write(345);
		} catch (final IllegalStateLtRtException e) {
			erro = 1;
		}
		assertTrue(erro == 1);

		final ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
		final XmlomInputStream xis = new XmlomInputStream(bIn, Constant.UTF8_CHARSET_NAME);
		final Sample outS = xis.read(Sample.class);
		assertTrue(outS.getZbyte() == null && inS.getZbyte() == null);
		assertTrue(outS.getZshort() == null && inS.getZshort() == null);
		assertTrue(outS.getZint() == null && inS.getZint() == null);
		assertTrue(outS.getZlong() == null && inS.getZlong() == null);
		assertTrue(outS.getZfloat() == null && inS.getZfloat() == null);
		assertTrue(outS.getZdouble() == null && inS.getZdouble() == null);
		assertTrue(outS.getZboolean() == null && inS.getZboolean() == null);
		assertTrue(outS.getZchar() == null && inS.getZchar() == null);
		assertTrue(outS.getPbyte() == inS.getPbyte());
		assertTrue(outS.getPshort() == inS.getPshort());
		assertTrue(outS.getPint() == inS.getPint());
		assertTrue(outS.getPlong() == inS.getPlong());
		assertTrue(outS.getPfloat() == inS.getPfloat());
		assertTrue(outS.getPdouble() == inS.getPdouble());
		assertTrue(outS.isPboolean() == inS.isPboolean());
		assertTrue(outS.getPchar() == inS.getPchar());
		assertTrue(outS.getDate() == null && inS.getDate() == null);
		assertTrue(outS.getName() == null && inS.getName() == null);
		assertTrue(outS.getFilho() == null && inS.getFilho() == null);
		assertTrue(outS.getAo() == null && inS.getAo() == null);
		assertTrue(outS.getSo() == null && inS.getSo() == null);
		assertTrue(outS.getMo() == null && inS.getMo() == null);
		assertTrue(outS.getLo() == null && inS.getLo() == null);
		assertTrue(outS.getAm() == null && inS.getAm() == null);
		assertTrue(outS.getAs() == null && inS.getAs() == null);
		final byte b = (byte) (xis.read() & 0xff);
		assertTrue(b == 123);
		final Object o1 = xis.read(XmlObjectModelling.class);
		assertTrue(o1 == null);
		final Sample o2 = xis.read(Sample.class);
		assertTrue(o2 == null);
		xis.close();
		xos.close();
	}

	@Test(expected = IOException.class) // with cause: ClassCastException.class
	public void test_e1() throws UnsupportedEncodingException, XmlInvalidLtException, IOException {
		final byte[] t3 = ("<?xml version=\"1.0\" encoding=\"UTF8\"?>\r\n"
				+ "<Object type=\"leitej.XmlObjectModelling\">\r\n"
				+ "	<Sample type=\"test.xml.om.Sample\" id=\"1\">\r\n" + "		<pbyte type=\"Pbyte\">0</pbyte>\r\n"
				+ "		<pshort type=\"Pshort\">0</pshort>\r\n" + "		<pint type=\"Pint\">0</pint>\r\n"
				+ "		<plong type=\"Plong\">0</plong>\r\n" + "		<pfloat type=\"Pfloat\">0.0</pfloat>\r\n"
				+ "		<pdouble type=\"Pdouble\">0.0</pdouble>\r\n"
				+ "		<pboolean type=\"Pboolean\">false</pboolean>\r\n"
				+ "		<pchar type=\"Pchar\">\0</pchar>\r\n" + "	</Sample>\r\n" + "	<xom type=\"[B\">7b</xom>\r\n"
				+ "</Object>\r\n").getBytes();
		final ByteArrayInputStream bIn = new ByteArrayInputStream(t3);
		final XmlomInputStream xis = new XmlomInputStream(bIn, Constant.UTF8_CHARSET_NAME);
		final List<Sample> outS = xis.readAll(Sample.class);
		assertTrue(outS.size() == 2);
		xis.close();
	}

	@Test(expected = IOException.class) // with cause: ClassCastException.class
	public void test_e2() throws UnsupportedEncodingException, XmlInvalidLtException, IOException {
		final byte[] t3 = ("<?xml version=\"1.0\" encoding=\"UTF8\"?>\r\n"
				+ "<Object type=\"leitej.XmlObjectModelling\">\r\n"
				+ "	<Sample type=\"test.xml.om.Sample\" id=\"1\">\r\n" + "		<pbyte type=\"Pbyte\">0</pbyte>\r\n"
				+ "		<pshort type=\"Pshort\">0</pshort>\r\n" + "		<pint type=\"Pint\">0</pint>\r\n"
				+ "		<plong type=\"Plong\">0</plong>\r\n" + "		<pfloat type=\"Pfloat\">0.0</pfloat>\r\n"
				+ "		<pdouble type=\"Pdouble\">0.0</pdouble>\r\n"
				+ "		<pboolean type=\"Pboolean\">false</pboolean>\r\n"
				+ "		<pchar type=\"Pchar\">\0</pchar>\r\n" + "	</Sample>\r\n" + "	<xom type=\"[B\">7b</xom>\r\n"
				+ "</Object>\r\n").getBytes();
		final ByteArrayInputStream bIn = new ByteArrayInputStream(t3);
		final XmlomInputStream xis = new XmlomInputStream(bIn, Constant.UTF8_CHARSET_NAME);
		xis.read(Sample2.class);
		xis.close();
		fail();
	}

	@Test
	public void test_e3_loop() throws IOException, ClassCastException, XmlInvalidLtException {
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final Sample s = XmlomIOStream.newXmlObjectModelling(Sample.class);
		s.setFilho(s);
		final XmlomOutputStream xos = new XmlomOutputStream(new SniffOutputStream(bOut, null),
				Constant.UTF8_CHARSET_NAME);
		xos.write(s);
		xos.close();
		final ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
		final XmlomInputStream xis = new XmlomInputStream(bIn, Constant.UTF8_CHARSET_NAME);
		final Sample tmp = xis.read(Sample.class);
		xis.close();
		assertTrue(tmp.equals(tmp.getFilho()));
	}

	@Test
	public void test_same_object() throws IOException {
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final Sample s = XmlomIOStream.newXmlObjectModelling(Sample.class);
		final XmlomOutputStream xos = new XmlomOutputStream(bOut, Constant.UTF8_CHARSET_NAME);
		xos.write(s);
		xos.flush();
		xos.write(s);
		xos.flush();
		xos.close();
	}

	@Test
	public void test_data_interface() throws IOException, XmlInvalidLtException {
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final XmlomOutputStream xos = new XmlomOutputStream(new SniffOutputStream(bOut, System.out),
				Constant.UTF8_CHARSET_NAME);
		final Sample4 s = XmlomIOStream.newXmlObjectModelling(Sample4.class);
		xos.write(s);
		xos.flush();
		s.setData(new DataLeaf("ola"));
		xos.write(s);
		xos.flush();
		xos.close();
		final ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
		final XmlomInputStream xis = new XmlomInputStream(bIn, Constant.UTF8_CHARSET_NAME);
		Sample4 tmp = xis.read(Sample4.class);
		assertTrue(tmp.getData() == null);
		tmp = xis.read(Sample4.class);
		assertTrue(tmp.getData().toString().equals(s.getData().toString()));
		xis.close();
	}

	@Test
	public void test_velocity() throws IOException, XmlInvalidLtException {
		final int exaust = 10000;
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final XmlomOutputStream xos = new XmlomOutputStream(bOut, Constant.UTF8_CHARSET_NAME);
		final Stopwatch sw = Stopwatch.getInstance("test_velocity");
		sw.start();
		final Sample[] arrayS = new Sample[exaust];
		for (int i = 0; i < exaust; i++) {
			arrayS[i] = XmlomIOStream.newXmlObjectModelling(Sample.class);
		}
		sw.step();
		for (int i = 0; i < exaust; i++) {
			xos.write(arrayS[i]);
			xos.flush();
			arrayS[i].release();
		}
		sw.step("Prod");
		xos.close();
		final ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
		final XmlomInputStream xis = new XmlomInputStream(bIn, Constant.UTF8_CHARSET_NAME);
		sw.step();
		for (int i = 0; i < exaust; i++) {
			xis.read(Sample.class).release();
		}
		sw.step("Cons");
		xis.close();
		sw.stop();
		System.out.println(sw);
	}

	@Test
	public void test_velocity2() throws IOException, XmlInvalidLtException {
		final int exaust = 10000;
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final XmlomOutputStream xos = new XmlomOutputStream(bOut, Constant.UTF8_CHARSET_NAME);
		final Stopwatch sw = Stopwatch.getInstance("test_velocity2");
		sw.start();
		final Sample2[] arrayS = new Sample2[exaust];
		for (int i = 0; i < exaust; i++) {
			arrayS[i] = XmlomIOStream.newXmlObjectModelling(Sample2.class);
		}
		sw.step();
		for (int i = 0; i < exaust; i++) {
			xos.write(arrayS[i]);
			xos.flush();
			arrayS[i].release();
		}
		sw.step("Prod");
		xos.close();
		final ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
		final XmlomInputStream xis = new XmlomInputStream(bIn, Constant.UTF8_CHARSET_NAME);
		sw.step();
		for (int i = 0; i < exaust; i++) {
			xis.read(Sample2.class).release();
		}
		sw.step("Cons");
		xis.close();
		sw.stop();
		System.out.println(sw);
	}

	@Test
	public void test_velocity3() throws IOException, XmlInvalidLtException {
		final int exaust = 10000;
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final XmlomOutputStream xos = new XmlomOutputStream(bOut, Constant.UTF8_CHARSET_NAME);
		final Stopwatch sw = Stopwatch.getInstance("test_velocity3");
		sw.start();
		final Sample3[] arrayS = new Sample3[exaust];
		final Random rnd = new Random((new Date()).getTime());
		for (int i = 0; i < exaust; i++) {
			arrayS[i] = XmlomIOStream.newXmlObjectModelling(Sample3.class);
			arrayS[i].setZbyte((byte) (rnd.nextInt() & 0xff));
			arrayS[i].setZshort((short) rnd.nextInt());
			arrayS[i].setZint(rnd.nextInt());
			arrayS[i].setZlong(rnd.nextLong());
			arrayS[i].setZfloat(rnd.nextFloat());
			arrayS[i].setZdouble(rnd.nextDouble());
			arrayS[i].setZboolean(rnd.nextBoolean());
			arrayS[i].setZchar((char) (rnd.nextInt() & 0xff));
			arrayS[i].setPbyte((byte) (rnd.nextInt() & 0xff));
			arrayS[i].setPshort((short) rnd.nextInt());
			arrayS[i].setPint(rnd.nextInt());
			arrayS[i].setPlong(rnd.nextLong());
			arrayS[i].setPfloat(rnd.nextFloat());
			arrayS[i].setPdouble(rnd.nextDouble());
			arrayS[i].setPboolean(rnd.nextBoolean());
			arrayS[i].setPchar((char) (rnd.nextInt() & 0xff));
			arrayS[i].setDate(new Date(rnd.nextLong()));
			arrayS[i].setName("" + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
					+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
					+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
					+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff));
			arrayS[i].setFilho(null);
			arrayS[i].setAo(new String[] { "" + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
					+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
					+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
					+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff),
					"" + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
							+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
							+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
							+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
							+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff),
					"" + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
							+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
							+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
							+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
							+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff),
					"" + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
							+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
							+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
							+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff)
							+ (char) (rnd.nextInt() & 0xff) + (char) (rnd.nextInt() & 0xff) });
			arrayS[i].setSo(null);
			arrayS[i].setMo(null);
			arrayS[i].setLo(null);
			arrayS[i].setAm(null);
			arrayS[i].setAs(null);
		}
		sw.step();
		for (int i = 0; i < exaust; i++) {
			xos.write(arrayS[i]);
			xos.flush();
			arrayS[i].release();
		}
		sw.step("Prod");
		xos.close();
		final ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
		final XmlomInputStream xis = new XmlomInputStream(bIn, Constant.UTF8_CHARSET_NAME);
		sw.step();
		for (int i = 0; i < exaust; i++) {
			xis.read(Sample3.class).release();
		}
		sw.step("Cons");
		xis.close();
		sw.stop();
		System.out.println(sw);
	}

}

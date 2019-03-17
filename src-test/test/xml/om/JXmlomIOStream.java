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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.exception.XmlInvalidLtException;
import leitej.util.DateUtil;
import leitej.util.machine.VMMonitor;
import leitej.xml.om.XmlObjectModelling;
import leitej.xml.om.XmlomIOStream;

public class JXmlomIOStream {

	private final static int SIZE_BLOCK;
	static {
		System.out.println(Long.valueOf(Integer.MAX_VALUE).longValue());
		System.out.println(VMMonitor.heapMemoryUsage().getMax());
		if (Long.valueOf(Integer.MAX_VALUE).longValue() < VMMonitor.heapMemoryUsage().getMax()) {
			throw new IllegalStateException();
		}
		SIZE_BLOCK = Long.valueOf(VMMonitor.heapMemoryUsage().getMax()).intValue() / 10;
		System.out.println("SIZE_BLOCK " + SIZE_BLOCK);
	}

	private String fileName;
	private Sample sample;

	@Before
	public void setUp() throws Exception {
		this.fileName = "testJxmlom.lixo.xml";
	}

	@After
	public void tearDown() throws Exception {
		(new File(this.fileName)).delete();
	}

	@Test
	public final void testSendToFileUTF8_null()
			throws NullPointerException, FileNotFoundException, SecurityException, IOException, XmlInvalidLtException {
		XmlomIOStream.sendToFileUTF8(this.fileName, this.sample);
		final List<XmlObjectModelling> result = XmlomIOStream.getObjectsFromFileUTF8(XmlObjectModelling.class,
				this.fileName);
		assertTrue(this.sample == null && result.size() == 0);
		(new File(this.fileName)).delete();
	}

	@Test
	public final void testSendToFileUTF8_1()
			throws NullPointerException, FileNotFoundException, SecurityException, IOException, XmlInvalidLtException {
		this.sample = XmlomIOStream.newXmlObjectModelling(Sample.class);
		XmlomIOStream.sendToFileUTF8(this.fileName, this.sample);
		final List<Sample> result = XmlomIOStream.getObjectsFromFileUTF8(Sample.class, this.fileName);
		assertSampleEquals(result.get(0), this.sample);
		(new File(this.fileName)).delete();
	}

	private void assertSampleEquals(final Sample s1, final Sample s2) {
		assertTrue(s1.getZbyte() == null && s2.getZbyte() == null || s1.getZbyte().equals(s2.getZbyte()));
		assertTrue(s1.getZshort() == null && s2.getZshort() == null || s1.getZshort().equals(s2.getZshort()));
		assertTrue(s1.getZint() == null && s2.getZint() == null || s1.getZint().equals(s2.getZint()));
		assertTrue(s1.getZlong() == null && s2.getZlong() == null || s1.getZlong().equals(s2.getZlong()));
		assertTrue(s1.getZfloat() == null && s2.getZfloat() == null || s1.getZfloat().equals(s2.getZfloat()));
		assertTrue(s1.getZdouble() == null && s2.getZdouble() == null || s1.getZdouble().equals(s2.getZdouble()));
		assertTrue(s1.getZboolean() == null && s2.getZboolean() == null || s1.getZboolean().equals(s2.getZboolean()));
		assertTrue(s1.getZchar() == null && s2.getZchar() == null || s1.getZchar().equals(s2.getZchar()));
		assertTrue(s1.getPbyte() == s2.getPbyte());
		assertTrue(s1.getPshort() == s2.getPshort());
		assertTrue(s1.getPint() == s2.getPint());
		assertTrue(s1.getPlong() == s2.getPlong());
		assertTrue(s1.getPfloat() == s2.getPfloat());
		assertTrue(s1.getPdouble() == s2.getPdouble());
		assertTrue(s1.isPboolean() == s2.isPboolean());
		assertTrue(s1.getPchar() == s2.getPchar());
		assertTrue(s1.getDate() == null && s2.getDate() == null || s1.getDate().equals(s2.getDate()));
		assertTrue(s1.getName() == null && s2.getName() == null || s1.getName().equals(s2.getName()));
		assertTrue(
				s1.getFilho() == null && s2.getFilho() == null || s1.getFilho().getPbyte() == s2.getFilho().getPbyte());
		assertArrayEquals(s1.getAo(), s2.getAo());
		assertTrue(s1.getSo() == null && s2.getSo() == null
				|| s1.getSo() != null && s2.getSo() != null && s1.getSo().size() == s2.getSo().size());
		if (s1.getSo() != null && s1.getSo().iterator().hasNext()) {
			final List<Sample> l1 = new ArrayList<>();
			final List<Sample> l2 = new ArrayList<>();
			final Iterator<Sample> i1 = s1.getSo().iterator();
			final Iterator<Sample> i2 = s1.getSo().iterator();
			int countNull1 = 0;
			int countNull2 = 0;
			Sample tmp;
			for (int i = 0; i < s1.getSo().size(); i++) {
				tmp = i1.next();
				if (tmp == null) {
					countNull1++;
				} else {
					l1.add(tmp);
				}
				tmp = i2.next();
				if (tmp == null) {
					countNull2++;
				} else {
					l2.add(tmp);
				}
			}
			assertTrue(countNull1 == countNull2);
			// ATENCAO que se l1.size > 1 podemos ter objetos em ordens diferentes em
			// relacao ao l2
			assertSampleEquals(l1.get(0), l2.get(0));
		}
		assertTrue(s1.getMo() == null && s2.getMo() == null
				|| s1.getMo() != null && s2.getMo() != null && s1.getMo().size() == s2.getMo().size());
		assertTrue(s1.getLo() == null && s2.getLo() == null
				|| s1.getLo() != null && s2.getLo() != null && s1.getLo().size() == s2.getLo().size());
		if (s1.getLo() != null && s1.getLo().iterator().hasNext() && s1.getLo().iterator().next() != null) {
			assertSampleEquals(s1.getLo().iterator().next(), s2.getLo().iterator().next());
		}
		assertTrue(s1.getAm() == null && s2.getAm() == null
				|| s1.getAm() != null && s2.getAm() != null && s1.getAm().length == s2.getAm().length);
		if (s1.getAm() != null && s1.getAm().length > 0 && s1.getAm()[0] != null) {
			assertSampleEquals(s1.getAm()[0], s2.getAm()[0]);
		}
		assertArrayEquals(s1.getAs(), s2.getAs());
	}

	@Test
	public final void test_heap() throws InterruptedException {
		System.out.println("MAX_HEAP " + VMMonitor.heapMemoryUsage().getMax());
		System.gc();
		System.out.println(VMMonitor.heapMemoryUsage().getUsed() + " #GC# ");
		final TestHeadBlock[] tmp1 = new TestHeadBlock[6];
		for (int i = 0; i < tmp1.length; i++) {
			tmp1[i] = insertDataTestHeadBlock(XmlomIOStream.newXmlObjectModelling(TestHeadBlock.class));
		}
		System.out.println(VMMonitor.heapMemoryUsage().getUsed());
		for (int i = 0; i < 8; i++) {
			insertDataTestHeadBlock(XmlomIOStream.newXmlObjectModelling(TestHeadBlock.class));
		}
		System.gc();
		System.out.println(VMMonitor.heapMemoryUsage().getUsed() + " #GC# ");
		for (int i = 0; i < 8; i++) {
			insertDataTestHeadBlock(XmlomIOStream.newXmlObjectModelling(TestHeadBlock.class)).release();
		}
		System.out.println(VMMonitor.heapMemoryUsage().getUsed());
		for (int i = 0; i < tmp1.length; i++) {
			tmp1[i].release();
			tmp1[i] = null;
		}
		System.gc();
		System.out.println(VMMonitor.heapMemoryUsage().getUsed() + " #GC# ");
		for (int i = 0; i < 8; i++) {
			insertDataTestHeadBlock(XmlomIOStream.newXmlObjectModelling(TestHeadBlock.class)).release();
		}
		System.gc();
		System.out.println(VMMonitor.heapMemoryUsage().getUsed() + " #GC# ");
		for (int i = 0; i < 8; i++) {
			insertDataTestHeadBlock(XmlomIOStream.newXmlObjectModelling(TestHeadBlock.class));
		}
		System.gc();
		System.out.println(VMMonitor.heapMemoryUsage().getUsed() + " #GC# ");
		assertTrue(true);
	}

	public interface TestHeadBlock extends XmlObjectModelling {
		public byte[] getByte();

		public void setByte(byte[] thb);
	}

	private TestHeadBlock insertDataTestHeadBlock(final TestHeadBlock thb) {
		thb.setByte(new byte[SIZE_BLOCK]);
		return thb;
	}

	private Sample insertData(final Sample sample) {
		sample.setZbyte(Byte.valueOf("123"));
		sample.setZshort(Short.valueOf("123"));
		sample.setZint(312112312);
		sample.setZlong(12300000000L);
		sample.setZfloat(123.23231F);
		sample.setZdouble(12321312.123123123123123);
		sample.setZboolean(true);
		sample.setZchar('e');
		sample.setPbyte(Byte.valueOf("123"));
		sample.setPshort(Short.valueOf("123"));
		sample.setPint(312112312);
		sample.setPlong(12300000000L);
		sample.setPfloat(123.23231F);
		sample.setPdouble(12321312.123123123123123);
		sample.setPboolean(true);
		sample.setPchar('e');
		sample.setDate(DateUtil.now());
		sample.setName("ad&<�a\"s<>d");
		sample.setFilho(XmlomIOStream.newXmlObjectModelling(Sample.class));
		sample.setAo(new String[] { "qwe", "sdf", null });
		sample.setSo(new HashSet<Sample>());
		sample.setMo(new HashMap<String, Sample>());
		sample.setLo(new ArrayList<Sample>());
		sample.setAm((Sample[]) Array.newInstance(Sample.class, 2));
		sample.setAs(new String[][][] { { { "asd", null }, { null } }, { { null, "123" }, { "456" } } });
		return sample;
	}

	@Test
	public final void testSendToFileUTF8_1_data()
			throws NullPointerException, FileNotFoundException, SecurityException, IOException, XmlInvalidLtException {
		this.sample = XmlomIOStream.newXmlObjectModelling(Sample.class);
		insertData(this.sample);
		XmlomIOStream.sendToFileUTF8(this.fileName, this.sample);
		final List<Sample> result = XmlomIOStream.getObjectsFromFileUTF8(Sample.class, this.fileName);
		assertSampleEquals(result.get(0), this.sample);
		(new File(this.fileName)).delete();
	}

	private void insertDataList(final Sample sample) {
		insertData(sample);
		sample.getSo().add(insertData(XmlomIOStream.newXmlObjectModelling(Sample.class)));
		sample.getSo().add(null);
		sample.getMo().put(null, insertData(XmlomIOStream.newXmlObjectModelling(Sample.class)));
		sample.getMo().put("123", null);
		sample.getLo().add(insertData(XmlomIOStream.newXmlObjectModelling(Sample.class)));
		sample.getLo().add(null);
		sample.getAm()[0] = insertData(XmlomIOStream.newXmlObjectModelling(Sample.class));
	}

	@Test
	public final void testSendToFileUTF8_1_data_N()
			throws FileNotFoundException, SecurityException, NullPointerException, XmlInvalidLtException, IOException {
		this.sample = XmlomIOStream.newXmlObjectModelling(Sample.class);
		insertDataList(this.sample);
		XmlomIOStream.sendToFileUTF8(this.fileName, this.sample);
		final List<Sample> result = XmlomIOStream.getObjectsFromFileUTF8(Sample.class, this.fileName);
		assertSampleEquals(result.get(0), this.sample);
		(new File(this.fileName)).delete();
	}

	@Test
	public final void testSendToFileUTF8_N_data_N()
			throws FileNotFoundException, SecurityException, NullPointerException, XmlInvalidLtException, IOException {
		final Sample sample0 = XmlomIOStream.newXmlObjectModelling(Sample.class);
		insertDataList(sample0);
		final Sample sample1 = XmlomIOStream.newXmlObjectModelling(Sample.class);
		final Sample sample2 = XmlomIOStream.newXmlObjectModelling(Sample.class);
		insertDataList(sample2);
		XmlomIOStream.sendToFileUTF8(this.fileName, new Sample[] { sample0, sample1, sample2 });
		List<Sample> result;
		result = XmlomIOStream.getObjectsFromFileUTF8(Sample.class, this.fileName);
		assertSampleEquals(result.get(0), sample0);
		assertSampleEquals(result.get(1), sample1);
		assertSampleEquals(result.get(2), sample2);
		(new File(this.fileName)).delete();
	}

}

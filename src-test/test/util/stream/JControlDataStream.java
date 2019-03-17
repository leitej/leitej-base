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

package test.util.stream;

import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.util.DateUtil;
import leitej.util.stream.ControlDataInputStream;
import leitej.util.stream.ControlDataOutputStream;
import leitej.util.stream.FileUtil;

public class JControlDataStream {

	private static String filename = "JControlDataStream.lixo.dat";
	private static Random rnd = new Random();
	private byte[] buffOut;
	private byte[] buffIn;

	@Before
	public void setUp() throws Exception {
		FileUtil.createFile(filename);
		final int bSize = 1024;
		this.buffOut = new byte[bSize];
		rnd.nextBytes(this.buffOut);
		this.buffIn = new byte[bSize];
	}

	@After
	public void tearDown() throws Exception {
		(new File(filename)).delete();
	}

	private double testSend(final int bytePerSec, final int iterations) throws InterruptedException, IOException {
		int q = 0;
		final OutputStream os = new BufferedOutputStream(
				new ControlDataOutputStream(new FileOutputStream(new File(filename)), bytePerSec));
		final long start = DateUtil.nowTime();
		for (int i = 0; i < iterations; i++) {
			rndSleepTime();
			os.write(this.buffOut);
			os.flush();
			q += this.buffOut.length;
		}
		os.close();
		final long elapse = (DateUtil.nowTime() - (start));
		return (double) q * 1000 / elapse;
	}

	private double testReceive(final int bytePerSec, final int iterations, final int bytePerStep)
			throws InterruptedException, IOException {
		int q = 0;
		final ControlDataInputStream is = new ControlDataInputStream(new FileInputStream(new File(filename)),
				bytePerSec, bytePerStep);
		final long start = DateUtil.nowTime();
		for (int i = 0; i < iterations; i++) {
			rndSleepTime();
			is.read(this.buffIn, 0, this.buffIn.length);
			is.changeStep();
			q += this.buffIn.length;
		}
		is.close();
		final long elapse = (DateUtil.nowTime() - (start));
		return (double) q * 1000 / elapse;
	}

	private double testSendBBB(final int bytePerSec, final int iterations) throws InterruptedException, IOException {
		int q = 0;
		final OutputStream os = new BufferedOutputStream(
				new ControlDataOutputStream(new FileOutputStream(new File(filename)), bytePerSec));
		final long start = DateUtil.nowTime();
		for (int i = 0; i < iterations; i++) {
			os.write(this.buffOut[0]);
			q += 1;
		}
		os.close();
		final long elapse = (DateUtil.nowTime() - (start));
		return (double) q * 1000 / elapse;
	}

	private double testReceiveBBB(final int bytePerSec, final int iterations, final int bytePerStep)
			throws InterruptedException, IOException {
		int q = 0;
		final InputStream is = new ControlDataInputStream(new FileInputStream(new File(filename)), bytePerSec,
				bytePerStep);
		final long start = DateUtil.nowTime();
		for (int i = 0; i < iterations; i++) {
			is.read();
			q += 1;
		}
		is.close();
		final long elapse = (DateUtil.nowTime() - (start));
		return (double) q * 1000 / elapse;
	}

	private void rndSleepTime() throws InterruptedException {
		if (rnd.nextBoolean()) {
			final int r = rnd.nextInt(990);
			Thread.sleep(r);
		}
	}

	@Test
	public void test_1() throws FileNotFoundException, IOException, InterruptedException {
		final int v = 0;
		final int qps = 1024; // quantity of bytes per step
		final int it = 2;
		System.out.println();
		System.out.println("test_1");
		System.out.println("bytePerSec: " + v);
		double tmp = testSend(v, it);
		System.out.println("byte/sec: " + tmp);
		tmp = testReceive(v, it, qps);
		System.out.println("byte/sec: " + tmp);
	}

	@Test
	public void test_2() throws FileNotFoundException, IOException, InterruptedException {
		final int v = 1000;
		final int qps = 1024; // quantity of bytes per step
		final int it = 2;
		System.out.println();
		System.out.println("test_2");
		System.out.println("bytePerSec: " + v);
		double tmp = testSend(v, it);
		System.out.println("byte/sec: " + tmp);
		assertTrue((double) (v + 1) >= tmp);
		tmp = testReceive(v, it, qps);
		System.out.println("byte/sec: " + tmp);
		assertTrue((double) (v + 1) >= tmp);
	}

	@Test(expected = IOException.class)
	public void test_3() throws FileNotFoundException, IOException, InterruptedException {
		final int v = 1000;
		final int qps = 1023; // quantity of bytes per step
		final int it = 2;
		System.out.println();
		System.out.println("test_3");
		System.out.println("bytePerSec: " + v);
		double tmp = testSend(v, it);
		System.out.println("byte/sec: " + tmp);
		assertTrue((double) (v + 1) >= tmp);
		tmp = testReceive(v, it, qps);
		System.out.println("byte/sec: " + tmp);
		assertTrue((double) (v + 1) >= tmp);
	}

	@Test
	public void test_4() throws FileNotFoundException, IOException, InterruptedException {
		final int v = -1;
		final int qps = -1; // quantity of bytes per step
		final int it = 4;
		System.out.println();
		System.out.println("test_4");
		System.out.println("bytePerSec: " + v);
		double tmp = testSend(v, it);
		System.out.println("byte/sec: " + tmp);
		tmp = testReceive(v, it, qps);
		System.out.println("byte/sec: " + tmp);
	}

	@Test
	public void test_byte_by_byte_1() throws FileNotFoundException, IOException, InterruptedException {
		final int v = 1000;
		final int qps = 10000; // quantity of bytes per step
		final int it = 2000;
		System.out.println();
		System.out.println("test_byte_by_byte_1");
		System.out.println("bytePerSec: " + v);
		double tmp = testSendBBB(v, it);
		System.out.println("byte/sec: " + tmp);
		assertTrue((double) (v + 1) >= tmp);
		tmp = testReceiveBBB(v, it, qps);
		System.out.println("byte/sec: " + tmp);
		assertTrue((double) (v + 1) >= tmp);
	}

	@Test
	public void test_byte_by_byte_2() throws FileNotFoundException, IOException, InterruptedException {
		final int v = 1000;
		final int qps = 10000; // quantity of bytes per step
		final int it = 10000;
		System.out.println();
		System.out.println("test_byte_by_byte_2");
		System.out.println("bytePerSec: " + v);
		double tmp = testSendBBB(v, it);
		System.out.println("byte/sec: " + tmp);
		assertTrue((double) (v + 1) >= tmp);
		tmp = testReceiveBBB(v, it, qps);
		System.out.println("byte/sec: " + tmp);
		assertTrue((double) (v + 1) >= tmp);
	}

	@Test(expected = IOException.class)
	public void test_byte_by_byte_3() throws FileNotFoundException, IOException, InterruptedException {
		final int v = 100000;
		final int qps = 10000; // quantity of bytes per step
		final int it = 10001;
		System.out.println();
		System.out.println("test_byte_by_byte_3");
		System.out.println("bytePerSec: " + v);
		double tmp = testSendBBB(v, it);
		System.out.println("byte/sec: " + tmp);
		assertTrue((double) (v + 1) >= tmp);
		tmp = testReceiveBBB(v, it, qps);
		System.out.println("byte/sec: " + tmp);
		assertTrue((double) (v + 1) >= tmp);
	}

	@Test
	public void test_byte_by_byte_4() throws FileNotFoundException, IOException, InterruptedException {
		final int v = 0;
		final int qps = 10000; // quantity of bytes per step
		final int it = 10000;
		System.out.println();
		System.out.println("test_byte_by_byte_4");
		System.out.println("bytePerSec: " + v);
		double tmp = testSendBBB(v, it);
		System.out.println("byte/sec: " + tmp);
		tmp = testReceiveBBB(v, it, qps);
		System.out.println("byte/sec: " + tmp);
	}

}

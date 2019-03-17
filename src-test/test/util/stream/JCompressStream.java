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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import leitej.log.Logger;
import leitej.util.HexaUtil;
import leitej.util.data.BinaryConcat;
import leitej.util.stream.CompressInputStream;
import leitej.util.stream.CompressOutputStream;

public class JCompressStream {

	private static final Logger LOG = Logger.getInstance();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		// LOG.close();
	}

	@Test
	public void test() throws IOException {
		final int inputSize = 4086;
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final CompressOutputStream cos = new CompressOutputStream(bos);
		final byte[] input = new byte[inputSize];
		(new java.util.Random()).nextBytes(input); // random can't compress ;)
//		for(int i=0; i<input.length; i++){
//			input[i] = (byte) 0xff;
//		}
		cos.write(input);
		cos.flush();
		cos.close();
		final byte[] outputCompress = bos.toByteArray();
		final ByteArrayInputStream bis = new ByteArrayInputStream(outputCompress);
		final CompressInputStream cis = new CompressInputStream(bis);
		final byte[] buff = new byte[512];
		final BinaryConcat bc = new BinaryConcat(inputSize);
		int r = 0;
		while (r != -1) {
			bc.add(buff, 0, r);
			r = cis.read(buff);
		}
		cis.close();
		final byte[] output = bc.resetExcess();
		LOG.info("input size: #0", inputSize);
		LOG.info("outputCompress size: #0", outputCompress.length);
		LOG.info("output size: #0", output.length);
		LOG.info(HexaUtil.toHex(outputCompress));
		LOG.info(HexaUtil.toHex(input));
		LOG.info(HexaUtil.toHex(output));
		assertTrue(output.length == inputSize);
		assertTrue(Arrays.equals(input, output));
	}

}

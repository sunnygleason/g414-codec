package com.g414.codec.lzf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PushbackInputStream;
import java.util.Random;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.g414.codec.Chunk;

@Test
public class TestLzfJava {
	public void testLzfByteArray() {
		Random r1 = new Random(101L);
		Random r2 = new Random(101L);

		byte[] toencode = new byte[1024 * 1024];
		for (int i = 0; i < toencode.length; i++) {
			toencode[i] = makeByte(r1);
		}

		LZFCodec codec = new LZFCodec();
		byte[] encoded = codec.encode(toencode);
		byte[] decoded = codec.decode(encoded);

		for (int i = 0; i < toencode.length; i++) {
			byte expected = makeByte(r2);
			Assert.assertEquals("bad byte at position: " + i, expected,
					decoded[i]);
		}
	}

	public void testLzfStream() throws Exception {
		LZFCodec codec = new LZFCodec();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int times = 3000;

		for (int i = 0; i < times; i++) {
			byte[] theBytes = makeString(i).getBytes();
			codec.appendEncode(out, theBytes);
		}

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		PushbackInputStream pin = new PushbackInputStream(in,
				LZFConstants.MAX_CHUNK_LEN);

		int i = 0;
		for (Chunk next = codec.decodeNext(pin); next != null; next = codec
				.decodeNext(pin)) {
			String actual = new String(next.getData());
			String expected = makeString(i);

			Assert.assertEquals(expected, actual);
			i += 1;
		}

		Assert.assertEquals(times, i);
	}

	private static String makeString(int i) {
		return "this is only a test!!!!!!!!!!!!!!! abcdefgabcdefgabcdefg: " + i;
	}

	private static byte makeByte(Random r) {
		return (byte) (r.nextInt('F' - 'A') + 'A');
	}
}

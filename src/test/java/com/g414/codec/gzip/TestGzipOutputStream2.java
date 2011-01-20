package com.g414.codec.gzip;

//package com.g414.codec.gzip;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.util.Scanner;
//import java.util.zip.GZIPOutputStream;
//
//import org.testng.annotations.Test;
//
//import com.g414.codec.gzip.GzipOutputStream2.CompressionLevel;
//
//@Test
//public class TestGzipOutputStream2 {
//	public void testContent() throws Exception {
//		Scanner scanner = new Scanner(new File(
//				"src/main/java/com/g414/codec/gzip/TestCompression.java"));
//		StringBuilder b = new StringBuilder();
//		while (scanner.hasNextLine()) {
//			String line = scanner.nextLine();
//			b.append(line);
//			b.append("\n");
//		}
//
//		String content = b.toString();
//
//		System.out.println("un: " + content.length());
//
//		System.out.println("b1: " + getCompressed(content.getBytes()).length);
//		System.out.println("b2: " + getCompressed2(content.getBytes()).length);
//		System.out.println("b3: " + getCompressed3(content.getBytes()).length);
//		System.out.println("b4: " + getCompressed4(content.getBytes()).length);
//	}
//
//	private static byte[] getCompressed(byte[] input) throws Exception {
//		ByteArrayOutputStream o = new ByteArrayOutputStream();
//		GZIPOutputStream s = new GZIPOutputStream(o);
//		s.write(input);
//		s.flush();
//		s.close();
//
//		return o.toByteArray();
//	}
//
//	private static byte[] getCompressed2(byte[] input) throws Exception {
//		ByteArrayOutputStream o = new ByteArrayOutputStream();
//		GZIPOutputStream s = new GzipOutputStream2(o, 8192,
//				CompressionLevel.BEST_COMPRESSION);
//		s.write(input);
//		s.flush();
//		s.close();
//
//		return o.toByteArray();
//	}
//
//	private static byte[] getCompressed3(byte[] input) throws Exception {
//		ByteArrayOutputStream o = new ByteArrayOutputStream();
//		GZIPOutputStream s = new GzipOutputStream2(o, 8192,
//				CompressionLevel.DEFAULT_COMPRESSION);
//		s.write(input);
//		s.flush();
//		s.close();
//
//		return o.toByteArray();
//	}
//
//	private static byte[] getCompressed4(byte[] input) throws Exception {
//		ByteArrayOutputStream o = new ByteArrayOutputStream();
//		GZIPOutputStream s = new GzipOutputStream2(o, 8192,
//				CompressionLevel.BEST_SPEED);
//		s.write(input);
//		s.flush();
//		s.close();
//
//		return o.toByteArray();
//	}
// }

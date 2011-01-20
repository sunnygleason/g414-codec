//package com.g414.codec.huff;
//
//import java.io.FileInputStream;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import junit.framework.TestCase;
//
//import com.g414.codec.huff.ReadFrequencies.CharFreq;
//
//public class TestHuffman extends TestCase {
//	public void testHuffman() throws Exception {
//		InputStream input = null;
//		try {
//			System.out.println("here1");
//			input = new FileInputStream("src/sample_ST_good.txt");
//			int[] freq = ReadFrequencies.readFreq(input);
//			System.out.println("here2");
//			List<CharFreq> sorted = ReadFrequencies.sortFreq(freq);
//			System.out.println("here3");
//			System.out.println("=======================");
//			ReadFrequencies.showFreq(sorted);
//			System.out.println("=======================");
//			ReadFrequencies.doHuffman(sorted);
//			System.out.println("here4");
//			List<String> markers = new ArrayList<String>();
//			ReadFrequencies.showTree(sorted.get(0), "", "0", markers);
//			System.out.println("=======================");
//			Collections.sort(markers);
//			int total = 0;
//			for (String marker : markers) {
//				System.out.println(marker);
//				String[] vals = marker.split("\t");
//				total += Integer.parseInt(vals[2]);
//			}
//			System.out.println("=======================");
//			System.out.println("====> TOTAL HUFF: " + total + " bytes");
//		} catch (Throwable t) {
//			t.printStackTrace();
//		} finally {
//			if (input != null) {
//				input.close();
//			}
//		}
//	}
//}

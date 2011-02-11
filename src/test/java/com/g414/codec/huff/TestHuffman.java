package com.g414.codec.huff;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testng.annotations.Test;

import com.g414.codec.huff.ReadFrequencies.CharFreq;

@Test
public class TestHuffman {
	public void testHuffman() throws Exception {
		InputStream input = null;
		try {
			System.out.println("here1");
			input = new FileInputStream("samplefreq.txt");
			long[] freq = ReadFrequencies.readFreqFile(input);
			System.out.println("here2");
			List<CharFreq> sorted = ReadFrequencies.sortFreq(freq);
			System.out.println("here3");
			System.out.println("=======================");
			ReadFrequencies.showFreq(sorted);
			System.out.println("=======================");
			ReadFrequencies.doHuffman(sorted);
			System.out.println("here4");
			List<String> markers = new ArrayList<String>();
			ReadFrequencies.showTree(sorted.get(0), "", "", markers);
			System.out.println("=======================");
			Collections.sort(markers, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					String field1 = o1.split("\\|")[0];
					String field2 = o2.split("\\|")[0];

					if (field1.length() < field2.length()) {
						return -1;
					}

					if (field2.length() < field1.length()) {
						return 1;
					}

					return field1.compareTo(field2);
				}
			});
			int total = 0;
			for (String marker : markers) {
				System.out.println(marker.replaceAll("\\s", " "));
				String[] vals = marker.split("\\|");
				total += Integer.parseInt(vals[2]);
			}
			System.out.println("=======================");
			System.out.println("====> TOTAL HUFF: " + total + " bytes");
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}
}

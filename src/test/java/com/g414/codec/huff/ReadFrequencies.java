package com.g414.codec.huff;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class ReadFrequencies {
	public static long[] readFreqFile(InputStream input) {
		long[] freq = new long[256];
		Scanner scanner = null;
		long total = 0;
		try {
			scanner = new Scanner(input);
			while (scanner.hasNextLine()) {
				String[] line = scanner.nextLine().split("\\s+");
				long f = Long.parseLong(line[0]);
				int c = Integer.parseInt(line[1]);
				if (c > 128) {
					continue;
				}

				total += f;
				freq[Character.toUpperCase(c)] += f;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("===> TOTAL: " + total + " bytes");

		return freq;
	}

	public static long[] readFreq(InputStream input) {
		long[] freq = new long[256];
		Scanner scanner = null;
		int total = 0;
		try {
			scanner = new Scanner(input);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				char[] data = line.toCharArray();
				total += data.length;
				for (int i = 0; i < data.length; i++) {
					freq[data[i]] += 1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("===> TOTAL: " + total + " bytes");

		return freq;
	}

	public static List<CharFreq> sortFreq(long[] freq) {
		List<CharFreq> output = new ArrayList<CharFreq>();

		for (int i = 0; i < freq.length; i++) {
			if (freq[i] == 0) {
				continue;
			}

			CharFreq toAdd = new CharFreq();
			toAdd.b = (char) i;
			toAdd.f = freq[i];
			output.add(toAdd);
		}

		Collections.sort(output, new Comparator<CharFreq>() {
			public int compare(CharFreq o1, CharFreq o2) {
				if (o1.f == o2.f) {
					return 0;
				} else if (o1.f < o2.f) {
					return -1;
				} else {
					return 1;
				}
			}
		});

		return output;
	}

	public static void showFreq(List<CharFreq> freq) {
		for (CharFreq input : freq) {
			if (input.f != 0) {
				System.out.println(((input.b == 0) ? "~  " : "'" + input.b
						+ "'")
						+ " : " + input.f);
			}
		}
	}

	public static void showTree(CharFreq input, String indent, String marker,
			List<String> collector) {
		if (input.b != 0) {
			System.out.println((indent
					+ ((input.b == 0) ? "~" : "'" + input.b + "'") + " : "
					+ input.f + "    [" + marker + "]").replaceAll("\\s", " "));
			collector.add(marker + "|'" + input.b + "'|"
					+ ((input.f * marker.length()) / 8));
		}

		if (input.l != null) {
			showTree(input.l, indent + "    ", marker + "0", collector);
		}

		if (input.r != null) {
			showTree(input.r, indent + "    ", marker + "1", collector);
		}
	}

	public static void doHuffman(List<CharFreq> input) {
		while (input.size() > 1) {
			CharFreq z = new CharFreq();
			if (input.size() > 0) {
				z.l = input.remove(0);
				z.f = z.l.f;
			}
			if (input.size() > 0) {
				z.r = input.remove(0);
				z.f += z.r.f;
			}

			for (int j = 0; j < input.size(); j++) {
				if (z.f < input.get(j).f) {
					input.add(j, z);
					z = null;
					break;
				}
			}

			if (z != null && z.f > 0) {
				input.add(z);
			}
		}
	}

	public static class CharFreq {
		public char b;
		public long f;
		public CharFreq l;
		public CharFreq r;
	}
}

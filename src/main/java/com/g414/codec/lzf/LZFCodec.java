/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.g414.codec.lzf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.g414.codec.Chunk;
import com.g414.codec.StreamCodec;

/**
 * LZF Codec - provides byte[] oriented decode / encode methods, plus support
 * for stream-oriented codec methods.
 * 
 * Inspired by ning/compress lzf codec, created by Tatu Saloranta and Jon
 * Hartlaub.
 */
public final class LZFCodec implements StreamCodec {
	@Override
	public byte[] encode(byte[] data) {
		return encode(data, data.length);
	}

	@Override
	public byte[] decode(final byte[] sourceBuffer) {
		return decodeImpl(sourceBuffer);
	}

	@Override
	public void appendEncode(OutputStream out, byte[] buffer)
			throws IOException {
		out.write(encode(buffer));
	}

	@Override
	public Chunk decodeNext(InputStream source) throws IOException {
		final byte[] header = new byte[LZFConstants.HEADER_BYTES];

		int totalRead = 0;
		while (totalRead < LZFConstants.HEADER_BYTES) {
			int lastRead = source.read(header, totalRead, header.length
					- totalRead);
			if (lastRead == -1) {
				if (lastRead > 0) {
					throw new IOException("IOException while reading, only "
							+ totalRead + "bytes in header");
				} else {
					return null;
				}
			}

			totalRead += lastRead;
		}

		int type = header[2];
		int len = uint16(header, 3);

		final byte[] out;
		if (type == LZFConstants.BLOCK_TYPE_NON_COMPRESSED) {
			out = new byte[len];
			int outPtr = 0;

			while (outPtr < len) {
				int lastRead = source.read(out, outPtr, len - outPtr);
				if (lastRead == -1) {
					throw new IOException("IOException while reading, only "
							+ outPtr + "bytes in uncompressed data");
				}

				outPtr += lastRead;
			}
		} else {
			int d0 = source.read();
			int d1 = source.read();
			if (d0 == -1 || d1 == -1) {
				throw new IOException(
						"IOException while reading, malformed 'uncompressed' length in compressed chunk");
			}

			int uncompLen = uint16b(d0, d1);

			final byte[] in = new byte[len];
			int inPtr = 0;

			while (inPtr < len) {
				int lastRead = source.read(in, inPtr, len - inPtr);
				if (lastRead == -1) {
					throw new IOException("IOException while reading, only "
							+ inPtr + "bytes in uncompressed data");
				}

				inPtr += lastRead;
			}

			out = new byte[uncompLen];
			decompressChunk(in, 0, out, 0, uncompLen);
		}

		return new Chunk(out);
	}

	private static int calculateUncompressedSize(byte[] data) {
		int uncompressedSize = 0;
		int ptr = 0;
		int blockNr = 0;

		while (ptr < data.length) {
			if (ptr == (data.length + 1) && data[ptr] == LZFConstants.BYTE_NULL) {
				++ptr;
				break;
			}

			try {
				if (data[ptr] != LZFConstants.BYTE_Z
						|| data[ptr + 1] != LZFConstants.BYTE_V) {
					throw new RuntimeException("Corrupt input data, block #"
							+ blockNr + " (at offset " + ptr
							+ "): did not start with 'ZV' signature bytes");
				}
				int type = (int) data[ptr + 2];
				int blockLen = uint16(data, ptr + 3);
				if (type == LZFConstants.BLOCK_TYPE_NON_COMPRESSED) {
					ptr += 5;
					uncompressedSize += blockLen;
				} else if (type == LZFConstants.BLOCK_TYPE_COMPRESSED) {
					uncompressedSize += uint16(data, ptr + 5);
					ptr += 7;
				} else {
					throw new RuntimeException("Corrupt input data, block #"
							+ blockNr + " (at offset " + ptr
							+ "): unrecognized block type " + (type & 0xFF));
				}
				ptr += blockLen;
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new RuntimeException("Corrupt input data, block #"
						+ blockNr + " (at offset " + ptr
						+ "): truncated block header");
			}
			++blockNr;
		}

		if (ptr != data.length) {
			throw new RuntimeException("Corrupt input data: block #" + blockNr
					+ " extends " + (data.length - ptr)
					+ " beyond end of input");
		}
		return uncompressedSize;
	}

	private static final byte[] encode(byte[] data, int length) {
		int left = length;
		ChunkEncoder enc = new ChunkEncoder(left);
		int chunkLen = Math.min(LZFConstants.MAX_CHUNK_LEN, left);
		LZFChunk first = enc.encodeChunk(data, 0, chunkLen);
		left -= chunkLen;

		if (left < 1) {
			return first.getData();
		}

		int resultBytes = first.length();
		int inputOffset = chunkLen;
		LZFChunk last = first;

		do {
			chunkLen = Math.min(left, LZFConstants.MAX_CHUNK_LEN);
			LZFChunk chunk = enc.encodeChunk(data, inputOffset, chunkLen);
			inputOffset += chunkLen;
			left -= chunkLen;
			resultBytes += chunk.length();
			last.setNext(chunk);
			last = chunk;
		} while (left > 0);

		byte[] result = new byte[resultBytes];
		int ptr = 0;
		for (; first != null; first = first.next()) {
			ptr = first.copyTo(result, ptr);
		}
		return result;
	}

	private static void decompressChunk(byte[] in, int inPos, byte[] out,
			int outPos, int outEnd) {
		do {
			int ctrl = in[inPos++] & 255;
			if (ctrl < LZFConstants.MAX_LITERAL) {
				ctrl += inPos;
				do {
					out[outPos++] = in[inPos];
				} while (inPos++ < ctrl);
			} else {
				int len = ctrl >> 5;
				ctrl = -((ctrl & 0x1f) << 8) - 1;
				if (len == 7) {
					len += in[inPos++] & 255;
				}
				ctrl -= in[inPos++] & 255;
				len += outPos + 2;
				out[outPos] = out[outPos++ + ctrl];
				out[outPos] = out[outPos++ + ctrl];
				while (outPos < len - 8) {
					out[outPos] = out[outPos++ + ctrl];
					out[outPos] = out[outPos++ + ctrl];
					out[outPos] = out[outPos++ + ctrl];
					out[outPos] = out[outPos++ + ctrl];
					out[outPos] = out[outPos++ + ctrl];
					out[outPos] = out[outPos++ + ctrl];
					out[outPos] = out[outPos++ + ctrl];
					out[outPos] = out[outPos++ + ctrl];
				}
				while (outPos < len) {
					out[outPos] = out[outPos++ + ctrl];
				}
			}
		} while (outPos < outEnd);

		if (outPos != outEnd)
			throw new RuntimeException(
					"Corrupt data: overrun in decompress, input offset "
							+ inPos + ", output offset " + outPos);
	}

	private static byte[] decodeImpl(final byte[] sourceBuffer) {
		byte[] result = new byte[calculateUncompressedSize(sourceBuffer)];

		int inPtr = 0;
		int outPtr = 0;

		while (inPtr < (sourceBuffer.length - 1)) {
			inPtr += 2;
			int type = sourceBuffer[inPtr++];
			int len = uint16(sourceBuffer, inPtr);
			inPtr += 2;
			if (type == LZFConstants.BLOCK_TYPE_NON_COMPRESSED) {
				System.arraycopy(sourceBuffer, inPtr, result, outPtr, len);
				outPtr += len;
			} else {
				int uncompLen = uint16(sourceBuffer, inPtr);
				inPtr += 2;
				decompressChunk(sourceBuffer, inPtr, result, outPtr, outPtr
						+ uncompLen);
				outPtr += uncompLen;
			}
			inPtr += len;
		}

		return result;
	}

	private static final int uint16(byte[] data, int ptr) {
		return ((data[ptr] & 0xFF) << 8) + (data[ptr + 1] & 0xFF);
	}

	private static final int uint16b(int d0, int d1) {
		return ((d0 & 0xFF) << 8) + (d1 & 0xFF);
	}

	private static class ChunkEncoder {
		private final byte[] _encodeBuffer;
		private final int[] _hashTable;
		private final int _hashModulo;

		public ChunkEncoder(int totalLength) {
			int largestChunkLen = Math.max(totalLength,
					LZFConstants.MAX_CHUNK_LEN);

			int hashLen = calcHashLen(largestChunkLen);
			_hashTable = new int[hashLen];
			_hashModulo = hashLen - 1;
			int bufferLen = largestChunkLen + ((largestChunkLen + 31) >> 5);
			_encodeBuffer = new byte[bufferLen];
		}

		public LZFChunk encodeChunk(byte[] data, int offset, int len) {
			if (len >= LZFConstants.MIN_BLOCK_TO_COMPRESS) {
				int compLen = tryCompress(data, offset, offset + len,
						_encodeBuffer, 0);
				if (compLen < (len - 2)) {
					return LZFChunk.createCompressed(len, _encodeBuffer, 0,
							compLen);
				}
			}

			return LZFChunk.createNonCompressed(data, offset, len);
		}

		private static final int calcHashLen(int chunkSize) {
			chunkSize += chunkSize;

			if (chunkSize >= LZFConstants.MAX_HASH_SIZE) {
				return LZFConstants.MAX_HASH_SIZE;
			}

			int hashLen = LZFConstants.MIN_HASH_SIZE;
			while (hashLen < chunkSize) {
				hashLen += hashLen;
			}
			return hashLen;
		}

		private static final int first(byte[] in, int inPos) {
			return (in[inPos] << 8) + (in[inPos + 1] & 255);
		}

		private static final int next(int v, byte[] in, int inPos) {
			return (v << 8) + (in[inPos + 2] & 255);
		}

		private final int hash(int h) {
			return ((h * 57321) >> 9) & _hashModulo;
		}

		private final int tryCompress(byte[] in, int inPos, int inEnd,
				byte[] out, int outPos) {
			int literals = 0;
			outPos++;
			int hash = first(in, 0);
			inEnd -= 4;
			final int firstPos = inPos;

			while (inPos < inEnd) {
				byte p2 = in[inPos + 2];
				hash = (hash << 8) + (p2 & 255);

				int off = hash(hash);
				int ref = _hashTable[off];
				_hashTable[off] = inPos;
				if (ref < inPos && ref >= firstPos
						&& (off = inPos - ref - 1) < LZFConstants.MAX_OFF
						&& in[ref + 2] == p2
						&& in[ref + 1] == (byte) (hash >> 8)
						&& in[ref] == (byte) (hash >> 16)) {

					int maxLen = inEnd - inPos + 2;
					if (maxLen > LZFConstants.MAX_REF) {
						maxLen = LZFConstants.MAX_REF;
					}
					if (literals == 0) {
						outPos--;
					} else {
						out[outPos - literals - 1] = (byte) (literals - 1);
						literals = 0;
					}
					int len = 3;
					while (len < maxLen && in[ref + len] == in[inPos + len]) {
						len++;
					}
					len -= 2;
					if (len < 7) {
						out[outPos++] = (byte) ((off >> 8) + (len << 5));
					} else {
						out[outPos++] = (byte) ((off >> 8) + (7 << 5));
						out[outPos++] = (byte) (len - 7);
					}
					out[outPos++] = (byte) off;
					outPos++;
					inPos += len;
					hash = first(in, inPos);
					hash = next(hash, in, inPos);
					_hashTable[hash(hash)] = inPos++;
					hash = next(hash, in, inPos);
					_hashTable[hash(hash)] = inPos++;
				} else {
					out[outPos++] = in[inPos++];
					literals++;
					if (literals == LZFConstants.MAX_LITERAL) {
						out[outPos - literals - 1] = (byte) (literals - 1);
						literals = 0;
						outPos++;
					}
				}
			}
			inEnd += 4;
			while (inPos < inEnd) {
				out[outPos++] = in[inPos++];
				literals++;
				if (literals == LZFConstants.MAX_LITERAL) {
					out[outPos - literals - 1] = (byte) (literals - 1);
					literals = 0;
					outPos++;
				}
			}
			out[outPos - literals - 1] = (byte) (literals - 1);
			if (literals == 0) {
				outPos--;
			}
			return outPos;
		}
	}

	private static class LZFChunk extends Chunk {
		private LZFChunk _next;

		private LZFChunk(byte[] data) {
			super(data);
		}

		public static LZFChunk createCompressed(int origLen, byte[] encData,
				int encPtr, int encLen) {
			byte[] result = new byte[encLen + 7];
			result[0] = LZFConstants.BYTE_Z;
			result[1] = LZFConstants.BYTE_V;
			result[2] = LZFConstants.BLOCK_TYPE_COMPRESSED;
			result[3] = (byte) (encLen >> 8);
			result[4] = (byte) encLen;
			result[5] = (byte) (origLen >> 8);
			result[6] = (byte) origLen;
			System.arraycopy(encData, encPtr, result, 7, encLen);

			return new LZFChunk(result);
		}

		public static LZFChunk createNonCompressed(byte[] plainData, int ptr,
				int len) {
			byte[] result = new byte[len + 5];
			result[0] = LZFConstants.BYTE_Z;
			result[1] = LZFConstants.BYTE_V;
			result[2] = LZFConstants.BLOCK_TYPE_NON_COMPRESSED;
			result[3] = (byte) (len >> 8);
			result[4] = (byte) len;
			System.arraycopy(plainData, ptr, result, 5, len);

			return new LZFChunk(result);
		}

		public void setNext(LZFChunk next) {
			_next = next;
		}

		public LZFChunk next() {
			return _next;
		}
	}
}
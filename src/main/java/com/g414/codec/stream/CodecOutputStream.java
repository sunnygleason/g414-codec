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
package com.g414.codec.stream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.g414.codec.ByteArrayCodec;

public class CodecOutputStream extends FilterOutputStream {
	private final ByteArrayCodec codec;
	private final byte[] buffer;
	private final int bufferLength;
	private int pos;

	public CodecOutputStream(ByteArrayCodec codec, int bufferLength,
			OutputStream dest) {
		super(dest);
		this.codec = codec;
		this.bufferLength = bufferLength;
		this.buffer = new byte[bufferLength];
		this.pos = 0;
	}

	@Override
	public void flush() throws IOException {
		this.write(buffer, 0, pos);
		pos = 0;

		super.flush();
	}

	@Override
	public void close() throws IOException {
		this.flush();
		super.close();
	}

	@Override
	public void write(int theByte) throws IOException {
		final byte[] toWrite = new byte[] { (byte) theByte };

		write(toWrite);
	}

	@Override
	public void write(byte[] source) throws IOException {
		this.write(source, 0, source.length);
	}

	@Override
	public void write(byte[] source, int offset, int length) throws IOException {
		int bytesCopied = 0;

		while (bytesCopied < length) {
			int size = Math.min(length, bufferLength - pos);
			System.arraycopy(source, offset, buffer, pos, size);
			bytesCopied += size;
			pos += bytesCopied;

			if (pos == bufferLength) {
				out.write(codec.encode(buffer));
				pos = 0;
			}
		}
	}
}

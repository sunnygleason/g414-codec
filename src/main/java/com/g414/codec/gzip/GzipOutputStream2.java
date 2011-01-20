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
package com.g414.codec.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class GzipOutputStream2 extends GZIPOutputStream {
	public enum CompressionLevel {
		DEFAULT_COMPRESSION(-1), NONE(0), BEST_SPEED(1), BEST_COMPRESSION(9);

		private final int code;

		private CompressionLevel(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}

	public GzipOutputStream2(OutputStream out) throws IOException {
		super(out);
	}

	public GzipOutputStream2(OutputStream out, int bufferLen)
			throws IOException {
		super(out, bufferLen);
	}

	public GzipOutputStream2(OutputStream out, CompressionLevel level)
			throws IOException {
		super(out);
		def.setLevel(level.getCode());
	}

	public GzipOutputStream2(OutputStream out, int bufferLen,
			CompressionLevel level) throws IOException {
		super(out, bufferLen);
		def.setLevel(level.getCode());
	}
}

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

public final class LZFConstants {
	public static final byte BYTE_NULL = 0;
	public static final byte BYTE_Z = 'Z';
	public static final byte BYTE_V = 'V';
	public static final int EOF_FLAG = -1;
	public static final int HEADER_BYTES = 5;
	public static final int MAX_LITERAL = 1 << 5;
	public static final int MAX_CHUNK_LEN = 0xFFFF;
	public static final int BLOCK_TYPE_NON_COMPRESSED = 0;
	public static final int BLOCK_TYPE_COMPRESSED = 1;
	public static final int MIN_BLOCK_TO_COMPRESS = 16;
	public static final int MIN_HASH_SIZE = 256;
	public static final int MAX_HASH_SIZE = 16384;
	public static final int MAX_OFF = 1 << 13;
	public static final int MAX_REF = (1 << 8) + (1 << 3);

	private LZFConstants() {
	}
}

/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liferay.blade.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.nio.charset.Charset;

/**
 * @author Christopher Bryan Boyd
 */
public class StringPrintStream extends PrintStream {

	public static StringPrintStream newInstance() {
		return new StringPrintStream(new ByteArrayOutputStream(), Charset.defaultCharset());
	}

	@Override
	public String toString() {
		return new String(_outputStream.toByteArray(), _charset);
	}

	private StringPrintStream(ByteArrayOutputStream outputStream, Charset charset) {
		super(outputStream);

		_outputStream = outputStream;
		_charset = charset;
	}

	private Charset _charset;
	private ByteArrayOutputStream _outputStream;

}
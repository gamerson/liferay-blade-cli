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
import java.io.InputStream;
import java.io.PrintStream;

import java.nio.charset.Charset;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Christopher Bryan Boyd
 */
public class StringPrintStream extends PrintStream implements Supplier<String> {

	public static StringPrintStream fromInputStream(InputStream inputStream) {
		StringPrintStream stringPrintStream = new StringPrintStream(new ByteArrayOutputStream(), Optional.empty());

		return stringPrintStream;
	}

	public static StringPrintStream newInstance() {
		return new StringPrintStream(new ByteArrayOutputStream(), Optional.empty());
	}

	public static StringPrintStream newInstance(PrintStream printStream) {
		return new StringPrintStream(new ByteArrayOutputStream(), Optional.ofNullable(printStream));
	}

	@Override
	public void flush() {
		if (_optionalPrintStream.isPresent()) {
			PrintStream stream = _optionalPrintStream.get();

			stream.flush();
		}

		super.flush();
	}

	@Override
	public String get() {
		return new String(_outputStream.toByteArray(), Charset.defaultCharset());
	}

	@Override
	public String toString() {
		return get();
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		if (_optionalPrintStream.isPresent()) {
			PrintStream stream = _optionalPrintStream.get();

			stream.write(buf, off, len);
		}

		super.write(buf, off, len);
	}

	private StringPrintStream(ByteArrayOutputStream outputStream, Optional<PrintStream> optionalPrintStream) {
		super(outputStream);

		_outputStream = outputStream;
		_optionalPrintStream = optionalPrintStream;
	}

	private Optional<PrintStream> _optionalPrintStream = Optional.empty();
	private ByteArrayOutputStream _outputStream;

}
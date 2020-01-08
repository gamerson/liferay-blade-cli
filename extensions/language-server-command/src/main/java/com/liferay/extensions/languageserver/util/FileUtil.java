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

package com.liferay.extensions.languageserver.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Terry Jia
 * @author Seiphon Wang
 */
public class FileUtil extends com.liferay.blade.cli.util.FileUtil {

	public static String[] readLinesFromFile(File file) {
		return readLinesFromFile(file, false);
	}

	public static String[] readLinesFromFile(File file, boolean includeNewlines) {
		if (!file.exists()) {
			return null;
		}

		List<String> lines = new ArrayList<>();

		try (FileReader fileReader = new FileReader(file)) {
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			String line;

			while ((line = bufferedReader.readLine()) != null) {
				StringBuffer contents = new StringBuffer(line);

				if (includeNewlines) {
					contents.append(System.getProperty("line.separator"));
				}

				lines.add(contents.toString());
			}
		}
		catch (Exception e) {
		}

		return lines.toArray(new String[0]);
	}

	public static String[] readLinesFromString(String content) {
		return readLinesFromString(content, false);
	}

	public static String[] readLinesFromString(String content, boolean includeNewlines) {
		if (content.isEmpty()) {
			return null;
		}

		List<String> lines = new ArrayList<>();

		try (InputStreamReader inputStreamReader = new InputStreamReader(
				new ByteArrayInputStream(content.getBytes()))) {

			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String line;

			while ((line = bufferedReader.readLine()) != null) {
				StringBuffer contents = new StringBuffer(line);

				if (includeNewlines) {
					contents.append(System.getProperty("line.separator"));
				}

				lines.add(contents.toString());
			}
		}
		catch (Exception e) {
		}

		return lines.toArray(new String[0]);
	}

}
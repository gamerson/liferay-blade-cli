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

package com.liferay.extensions.languageserver.services;

import java.io.File;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Terry Jia
 */
public class FolderService extends Service {

	public FolderService(File file) {
		super(file);
	}

	@Override
	public String[] getPossibleValues() {
		File parentFile = getFile().getParentFile();

		String[] files = parentFile.list();

		return Stream.of(
			files
		).map(
			fileName -> new File(parentFile, fileName)
		).filter(
			File::isDirectory
		).map(
			File::getName
		).collect(
			Collectors.toList()
		).toArray(
			new String[0]
		);
	}

	@Override
	public void validate(String value) throws Exception {
		File file = new File(getFile().getParentFile(), value);

		if (!file.exists()) {
			throw new Exception(file + " does not exist.");
		}
	}

}
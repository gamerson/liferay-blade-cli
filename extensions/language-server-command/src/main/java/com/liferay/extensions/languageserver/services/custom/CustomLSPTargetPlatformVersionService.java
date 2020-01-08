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

package com.liferay.extensions.languageserver.services.custom;

import com.liferay.extensions.languageserver.services.StringArrayService;

import java.io.File;
import java.io.InputStream;

import java.nio.file.Files;

import java.util.Properties;

/**
 * @author Terry Jia
 */
public class CustomLSPTargetPlatformVersionService extends StringArrayService {

	public CustomLSPTargetPlatformVersionService(File file) {
		super(file, new String[] {"7.0.6", "7.1.0", "7.1.1", "7.1.2", "7.1.3", "7.2.0"});
	}

	@Override
	public void validate(String value) throws Exception {
		File bladeBlade = new File(getFile().getParentFile(), ".blade.properties");

		if (!bladeBlade.exists()) {
			return;
		}

		Properties properties = new Properties();

		try (InputStream in = Files.newInputStream(bladeBlade.toPath())) {
			properties.load(in);

			String liferayVersionDefault = properties.getProperty("liferay.version.default");

			if (!value.startsWith(liferayVersionDefault)) {
				throw new Exception(
					"Version " + value + " does not match with " + liferayVersionDefault + " in .blade.properties");
			}
		}
	}

}
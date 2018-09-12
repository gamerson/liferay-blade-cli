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

package com.liferay.blade.cli.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;

import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Simon Jiang
 */
public class JavaUtil {

	public static final String EXT_JAR = ".jar";

	public static String getJarProperty(final File systemJarFile, final String propertyName) {
		if (systemJarFile.canRead()) {
			try (ZipFile jar = new ZipFile(systemJarFile)) {
				ZipEntry manifest = jar.getEntry("META-INF/MANIFEST.MF");

				Manifest mf = new Manifest(jar.getInputStream(manifest));

				Attributes a = mf.getMainAttributes();

				return a.getValue(propertyName);
			}
			catch (IOException ioe) {
				return null;
			}
		}

		return null;
	}

	public static String getManifestProperty(File manifestFile, String propertyName) {
		try {
			String contents = BladeUtil.readStreamToString(Files.newInputStream(manifestFile.toPath()));

			if (contents != null) {
				try (InputStream input = new ByteArrayInputStream(contents.getBytes())) {
					Manifest mf = new Manifest(input);

					Attributes a = mf.getMainAttributes();

					String val = a.getValue(propertyName);

					return val;
				}
			}
		}
		catch (IOException ioe) {
		}

		return null;
	}

	public static String getManifestPropFromFolderJars(File location, String mainFolder, String property) {
		File f = new File(location, mainFolder);

		if (f.exists()) {
			File[] children = f.listFiles();

			for (File child : children) {
				String childrenName = child.getName();

				if (childrenName.endsWith(EXT_JAR)) {
					return getJarProperty(child, property);
				}
			}
		}

		return null;
	}

	public static boolean scanFolderJarsForManifestProp(
		File location, String mainFolder, String property, String propPrefix) {

		String value = getManifestPropFromFolderJars(location, mainFolder, property);

		if (value != null) {
			String trimedValue = value.trim();

			if (trimedValue.startsWith(propPrefix)) {
				return true;
			}
		}

		return false;
	}

}
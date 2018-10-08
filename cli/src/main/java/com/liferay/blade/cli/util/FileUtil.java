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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Gregory Amerson
 */
public class FileUtil {

	public static void copyDir(Path source, Path target) throws IOException {
		if (!Files.exists(target)) {
			Files.createDirectories(target);
		}

		Files.walkFileTree(source, new CopyDirVisitor(source, target, StandardCopyOption.REPLACE_EXISTING));
	}

	public static void deleteDir(Path dirPath) throws IOException {
		Files.walkFileTree(
			dirPath,
			new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult postVisitDirectory(Path dirPath, IOException ioe) throws IOException {
					Files.delete(dirPath);

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes)
					throws IOException {

					Files.delete(path);

					return FileVisitResult.CONTINUE;
				}

			});
	}

	public static boolean exists(File file) {
		if ((file != null) && file.exists()) {
			return true;
		}

		return false;
	}

	public static boolean exists(Path path) {
		if (path != null) {
			File file = path.toFile();

			if (file.exists()) {
				return true;
			}
		}

		return false;
	}

	public static File[] getDirectories(File directory) {
		return directory.listFiles(
			new FileFilter() {

				@Override
				public boolean accept(File file) {
					return file.isDirectory();
				}

			});
	}

	public static boolean notExists(File file) {
		if ((file == null) || !file.exists()) {
			return true;
		}

		return false;
	}

	public static boolean notExists(Path filePath) {
		if (filePath == null) {
			return false;
		}

		File file = filePath.toFile();

		if ((file == null) || !file.exists()) {
			return true;
		}

		return false;
	}

	public static String readContents(File file, boolean includeNewlines) {
		if (notExists(file)) {
			return null;
		}

		StringBuffer contents = new StringBuffer();

		try (FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader)) {

			String line;

			while ((line = bufferedReader.readLine()) != null) {
				contents.append(line);

				if (includeNewlines) {
					contents.append(System.getProperty("line.separator"));
				}
			}
		}
		catch (Exception e) {
		}

		return contents.toString();
	}

	public static boolean verifyPath(String verifyPath) {
		if (verifyPath == null) {
			return false;
		}

		Path verifyLocation = Paths.get(verifyPath);

		File verifyFile = verifyLocation.toFile();

		if (exists(verifyFile) && verifyFile.isDirectory()) {
			return true;
		}

		return false;
	}

}
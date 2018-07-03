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

import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Domain;
import aQute.bnd.osgi.Jar;

import com.liferay.blade.cli.util.BladeUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.Scanner;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.gradle.testkit.runner.BuildTask;

import org.junit.Assert;

/**
 * @author Christopher Bryan Boyd
 */
public class TestUtil {

	public static File checkFileDoesNotExists(Path path) {
		return _checkFileDoesNotExists(path.toString());
	}

	public static File checkFileExists(Path path) {
		return _checkFileExists(path.toAbsolutePath().toString());
	}

	public static void checkGradleBuildFiles(Path projectPath) {
		checkFileExists(projectPath);
		checkFileExists(projectPath.resolve("bnd.bnd"));
		checkFileExists(projectPath.resolve("build.gradle"));
		checkFileExists(projectPath.resolve("gradlew"));
		checkFileExists(projectPath.resolve("gradlew.bat"));
	}

	public static void checkMavenBuildFiles(Path projectPath) {
		checkFileExists(projectPath);
		checkFileExists(projectPath.resolve("bnd.bnd"));
		checkFileExists(projectPath.resolve("pom.xml"));
		checkFileExists(projectPath.resolve("mvnw"));
		checkFileExists(projectPath.resolve("mvnw.cmd"));
	}

	public static void contains(File file, String pattern) throws Exception {
		String content = new String(Files.readAllBytes(file.toPath()));

		_contains(content, pattern);
	}

	public static void contains(File file, String[] patterns) throws Exception {
		String content = new String(Files.readAllBytes(file.toPath()));

		for (String pattern : patterns) {
			_contains(content, pattern);
		}
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

	public static void lacks(File file, String regex) throws Exception {
		String content = new String(Files.readAllBytes(file.toPath()));

		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);

		Assert.assertFalse(pattern.matcher(content).matches());
	}

	public static void makeSDK(Path dir) throws IOException {
		Path portletsPath = Files.createDirectories(dir.resolve("portlets"));

		Assert.assertTrue(Files.exists(portletsPath));

		Path hooksPath = Files.createDirectories(dir.resolve("hooks"));

		Assert.assertTrue(Files.exists(hooksPath));

		Path layouttplPath = Files.createDirectories(dir.resolve("layouttpl"));

		Assert.assertTrue(Files.exists(layouttplPath));

		Path themesPath = Files.createDirectories(dir.resolve("themes"));

		Assert.assertTrue(Files.exists(themesPath));

		Path buildPropertiesPath = Files.createFile(dir.resolve("build.properties"));

		Assert.assertTrue(Files.exists(buildPropertiesPath));

		Path buildXmlPath = Files.createFile(dir.resolve("build.xml"));

		Assert.assertTrue(Files.exists(buildXmlPath));

		Path buildCommonXmlPath = Files.createFile(dir.resolve("build-common.xml"));

		Assert.assertTrue(Files.exists(buildCommonXmlPath));

		Path buildCommonPluginXmlPath = Files.createFile(dir.resolve("build-common-plugin.xml"));

		Assert.assertTrue(Files.exists(buildCommonPluginXmlPath));
	}

	public static void makeWorkspace(Path rootPath, Path workspace) throws Exception {
		String[] args = {"--base", workspace.getParent().toString(), "init", workspace.getFileName().toString()};

		runBlade(rootPath, args);

		Assert.assertTrue(BladeUtil.isWorkspace(workspace));
	}

	public static String runBlade(File home, String... args) throws Exception {
		return runBlade(home.toPath().toAbsolutePath(), args);
	}

	public static String runBlade(Path home, String... args) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		PrintStream outputPrintStream = new PrintStream(outputStream);

		ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

		PrintStream errorPrintStream = new PrintStream(errorStream);

		BladeTest blade = new BladeTest(outputPrintStream, errorPrintStream);

		if (home != null) {
			blade.setUserHomeDir(home);
		}

		blade.run(args);

		String error = errorStream.toString();

		try (Scanner scanner = new Scanner(error)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				if (line.startsWith("SLF4J:")) {
					continue;
				}

				Assert.fail("Encountered error at line: " + line + "\n" + error);
			}
		}

		String content = outputStream.toString();

		return content;
	}

	public static String runBlade(String... args) throws Exception {
		return runBlade((File)null, args);
	}

	public static void testCreateWar(Path rootPath, File workspace, String projectType, String projectName)
		throws Exception {

		String[] args = {"--base", workspace.toString(), "create", "-t", projectType, projectName};

		runBlade(rootPath, args);

		Path workspacePath = workspace.toPath().toAbsolutePath();

		Path projectPath = workspacePath.resolve(Paths.get("wars", projectName));

		checkFileExists(projectPath);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.getAbsolutePath(), "war");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(projectPath, projectName + ".war");
	}

	public static void testCreateWar(Path rootPath, Path workspace, String projectType, String projectName)
		throws Exception {

		testCreateWar(rootPath, workspace.toFile(), projectType, projectName);
	}

	public static void verifyBuild(Path projectPath, String outputFileName) throws Exception {
		verifyBuild(projectPath.toAbsolutePath().toString(), "build", outputFileName);
	}

	public static void verifyBuild(String projectPath, String outputFileName) throws Exception {
		verifyBuild(projectPath, "build", outputFileName);
	}

	public static void verifyBuild(String projectPath, String taskPath, String outputFileName) throws Exception {
		Path path = Paths.get(projectPath);

		Path buildGradlePath = path.resolve("build.gradle");

		String content = "\nbuildscript { repositories { mavenLocal() } }";

		if (Files.exists(buildGradlePath)) {
			Files.write(buildGradlePath, content.getBytes(), StandardOpenOption.APPEND);
		}

		Path settingsGradlePath = path.resolve("settings.gradle");

		if (Files.exists(settingsGradlePath)) {
			Files.write(settingsGradlePath, content.getBytes(), StandardOpenOption.APPEND);
		}

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(projectPath, taskPath);

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(projectPath, outputFileName);
	}

	public static void verifyGradleBuild(Path workspaceDir) throws Exception {
		_createBundle(workspaceDir);

		Path projectPath = workspaceDir.resolve("modules");

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspaceDir.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(projectPath.resolve("foo"), "foo-1.0.0.jar");
	}

	public static void verifyImportPackage(Path serviceJar) throws Exception {
		try (Jar jar = new Jar(serviceJar.toFile())) {
			Manifest m = jar.getManifest();

			Domain domain = Domain.domain(m);

			Parameters imports = domain.getImportPackage();

			for (String key : imports.keySet()) {
				Assert.assertFalse(key.isEmpty());
			}
		}
	}

	public static void verifyMavenBuild(Path workspaceDir) throws Exception {
		_createMavenBundle(workspaceDir);

		Path projectPath = workspaceDir.resolve(Paths.get("modules", "foo"));

		MavenRunnerUtil.executeGoals(projectPath, new String[] {"clean", "package"});

		MavenRunnerUtil.verifyBuildOutput(projectPath, "foo-1.0.0.jar");
	}

	private static File _checkFileDoesNotExists(String path) {
		Path file = Paths.get(path);

		Assert.assertFalse(Files.exists(file));

		return file.toFile();
	}

	private static File _checkFileExists(String path) {
		Path file = Paths.get(path);

		Assert.assertTrue(Files.exists(file));

		return file.toFile();
	}

	private static void _contains(String content, String regex) throws Exception {
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);

		Assert.assertTrue(pattern.matcher(content).matches());
	}

	private static void _createBundle(Path workspaceDir) throws Exception {
		Path projectPath = workspaceDir.resolve("modules");

		String[] args = {"create", "-t", "mvc-portlet", "-d", projectPath.toString(), "foo"};

		new BladeTest().run(args);

		Path file = projectPath.resolve("foo");

		Path bndFile = file.resolve("bnd.bnd");

		Assert.assertTrue(Files.exists(file));

		Assert.assertTrue(Files.exists(bndFile));
	}

	private static void _createMavenBundle(Path workspaceDir) throws Exception {
		Path projectPath = workspaceDir.resolve("modules");

		String[] args = {"create", "-t", "mvc-portlet", "-d", projectPath.toString(), "-b", "maven", "foo"};

		new BladeTest().run(args);

		Path file = projectPath.resolve("foo");

		Path bndFile = file.resolve("bnd.bnd");

		Assert.assertTrue(Files.exists(file));

		Assert.assertTrue(Files.exists(bndFile));
	}

}
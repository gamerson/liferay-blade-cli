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

package com.liferay.blade.cli.command.create;

import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Domain;
import aQute.bnd.osgi.Jar;

import com.liferay.blade.cli.GradleRunnerUtil;
import com.liferay.blade.cli.TestUtil;
import com.liferay.blade.cli.util.BladeUtil;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.gradle.testkit.runner.BuildTask;
import org.gradle.tooling.internal.consumer.ConnectorServices;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class BaseCreateTest {

	@After
	public void cleanUp() throws Exception {
		ConnectorServices.reset();
	}

	@Before
	public void setUp() throws Exception {
		File tempRootFile = temporaryFolder.getRoot();

		tempRoot = tempRootFile.toPath().toAbsolutePath();
	}

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	protected File checkFileDoesNotExists(Path path) {
		return _checkFileDoesNotExists(path.toString());
	}

	protected File checkFileExists(Path path) {
		return _checkFileExists(path.toAbsolutePath().toString());
	}

	protected void checkGradleBuildFiles(Path projectPath) {
		checkFileExists(projectPath);
		checkFileExists(projectPath.resolve("bnd.bnd"));
		checkFileExists(projectPath.resolve("build.gradle"));
		checkFileExists(projectPath.resolve("gradlew"));
		checkFileExists(projectPath.resolve("gradlew.bat"));
	}

	protected void checkMavenBuildFiles(Path projectPath) {
		checkFileExists(projectPath);
		checkFileExists(projectPath.resolve("bnd.bnd"));
		checkFileExists(projectPath.resolve("pom.xml"));
		checkFileExists(projectPath.resolve("mvnw"));
		checkFileExists(projectPath.resolve("mvnw.cmd"));
	}

	protected void contains(File file, String pattern) throws Exception {
		String content = new String(Files.readAllBytes(file.toPath()));

		_contains(content, pattern);
	}

	protected void contains(File file, String[] patterns) throws Exception {
		String content = new String(Files.readAllBytes(file.toPath()));

		for (String pattern : patterns) {
			_contains(content, pattern);
		}
	}

	protected void lacks(File file, String regex) throws Exception {
		String content = new String(Files.readAllBytes(file.toPath()));

		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);

		Assert.assertFalse(pattern.matcher(content).matches());
	}

	protected void makeWorkspace(Path workspace) throws Exception {
		String[] args = {"--base", workspace.getParent().toString(), "init", workspace.getFileName().toString()};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Assert.assertTrue(BladeUtil.isWorkspace(workspace));
	}

	protected void testCreateWar(File workspace, String projectType, String projectName) throws Exception {
		String[] args = {"--base", workspace.toString(), "create", "-t", projectType, projectName};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Path workspacePath = workspace.toPath().toAbsolutePath();

		Path projectPath = workspacePath.resolve(Paths.get("wars", projectName));

		checkFileExists(projectPath);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.getAbsolutePath(), "war");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(projectPath, projectName + ".war");
	}

	protected void testCreateWar(Path workspace, String projectType, String projectName) throws Exception {
		testCreateWar(workspace.toFile(), projectType, projectName);
	}

	protected void verifyImportPackage(Path serviceJar) throws Exception {
		try (Jar jar = new Jar(serviceJar.toFile())) {
			Manifest m = jar.getManifest();

			Domain domain = Domain.domain(m);

			Parameters imports = domain.getImportPackage();

			for (String key : imports.keySet()) {
				Assert.assertFalse(key.isEmpty());
			}
		}
	}

	protected Path tempRoot = null;

	private File _checkFileDoesNotExists(String path) {
		Path file = Paths.get(path);

		Assert.assertFalse(Files.exists(file));

		return file.toFile();
	}

	private File _checkFileExists(String path) {
		Path file = Paths.get(path);

		Assert.assertTrue(Files.exists(file));

		return file.toFile();
	}

	private void _contains(String content, String regex) throws Exception {
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);

		Assert.assertTrue(pattern.matcher(content).matches());
	}

}
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

package com.liferay.blade.cli.command.init;

import com.liferay.blade.cli.BladeTest;
import com.liferay.blade.cli.GradleRunnerUtil;
import com.liferay.blade.cli.MavenRunnerUtil;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.testkit.runner.BuildTask;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class BaseInitTest {

	@Before
	public void setUp() throws Exception {
		workspaceDir = temporaryFolder.newFolder("build", "test", "workspace").toPath();

		File tempRootFile = temporaryFolder.getRoot();

		tempRoot = tempRootFile.toPath().toAbsolutePath();
	}

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	protected void makeSDK(Path dir) throws IOException {
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

	protected void verifyGradleBuild() throws Exception {
		_createBundle();

		Path projectPath = workspaceDir.resolve("modules");

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspaceDir.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(projectPath.resolve("foo"), "foo-1.0.0.jar");
	}

	protected void verifyMavenBuild() throws Exception {
		_createMavenBundle();

		Path projectPath = workspaceDir.resolve(Paths.get("modules", "foo"));

		MavenRunnerUtil.executeGoals(projectPath, new String[] {"clean", "package"});

		MavenRunnerUtil.verifyBuildOutput(projectPath, "foo-1.0.0.jar");
	}

	protected Path tempRoot = null;
	protected Path workspaceDir = null;

	private void _createBundle() throws Exception {
		Path projectPath = workspaceDir.resolve("modules");

		String[] args = {"create", "-t", "mvc-portlet", "-d", projectPath.toString(), "foo"};

		new BladeTest().run(args);

		Path file = projectPath.resolve("foo");

		Path bndFile = file.resolve("bnd.bnd");

		Assert.assertTrue(Files.exists(file));

		Assert.assertTrue(Files.exists(bndFile));
	}

	private void _createMavenBundle() throws Exception {
		Path projectPath = workspaceDir.resolve("modules");

		String[] args = {"create", "-t", "mvc-portlet", "-d", projectPath.toString(), "-b", "maven", "foo"};

		new BladeTest().run(args);

		Path file = projectPath.resolve("foo");

		Path bndFile = file.resolve("bnd.bnd");

		Assert.assertTrue(Files.exists(file));

		Assert.assertTrue(Files.exists(bndFile));
	}

}
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

import com.liferay.blade.cli.MavenRunnerUtil;
import com.liferay.blade.cli.TestUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CreateActivatorTest extends BaseCreateTest {

	@Test
	public void testCreateActivator() throws Exception {
		String[] gradleArgs = {"create", "-d", tempRoot.toString(), "-t", "activator", "bar-activator"};

		String[] mavenArgs = {"create", "-d", tempRoot.toString(), "-b", "maven", "-t", "activator", "bar-activator"};

		Path projectPath = tempRoot.resolve("bar-activator");

		TestUtil.runBlade(temporaryFolder.getRoot(), gradleArgs);

		checkGradleBuildFiles(projectPath);

		Path barActivator = Paths.get("src", "main", "java", "bar", "activator", "BarActivator.java");

		contains(
			checkFileExists(projectPath.resolve(barActivator)),
			".*^public class BarActivator implements BundleActivator.*$");

		TestUtil.verifyBuild(projectPath, "bar.activator-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "bar.activator-1.0.0.jar")));

		FileUtils.deleteDirectory(projectPath.toFile());

		TestUtil.runBlade(temporaryFolder.getRoot(), mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve(barActivator)),
			".*^public class BarActivator implements BundleActivator.*$");

		MavenRunnerUtil.executeGoals(projectPath, new String[] {"clean", "package"});

		MavenRunnerUtil.verifyBuildOutput(projectPath, "bar-activator-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("target", "bar-activator-1.0.0.jar")));
	}

}
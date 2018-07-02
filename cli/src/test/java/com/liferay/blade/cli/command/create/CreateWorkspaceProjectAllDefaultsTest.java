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

import com.liferay.blade.cli.GradleRunnerUtil;
import com.liferay.blade.cli.TestUtil;

import java.io.File;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.testkit.runner.BuildTask;

import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CreateWorkspaceProjectAllDefaultsTest extends BaseCreateTest {

	@Test
	public void testCreateWorkspaceProjectAllDefaults() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path projectPath = workspace.resolve(Paths.get("modules", "apps"));

		String[] args = {"create", "-d", projectPath.toString(), "-t", "mvc-portlet", "foo"};

		makeWorkspace(workspace);

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(projectPath.resolve("foo"));

		checkFileExists(projectPath.resolve(Paths.get("foo", "bnd.bnd")));

		File portletFile = checkFileExists(
			projectPath.resolve(Paths.get("foo", "src", "main", "java", "foo", "portlet", "FooPortlet.java")));

		contains(portletFile, ".*^public class FooPortlet extends MVCPortlet.*$");

		File gradleBuildFile = checkFileExists(projectPath.resolve(Paths.get("foo", "build.gradle")));

		lacks(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(projectPath.resolve("foo"), "foo-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("foo", "build", "libs", "foo-1.0.0.jar")));
	}

}
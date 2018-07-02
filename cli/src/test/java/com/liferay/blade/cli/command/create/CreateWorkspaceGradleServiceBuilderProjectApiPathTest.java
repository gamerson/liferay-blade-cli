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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.testkit.runner.BuildTask;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CreateWorkspaceGradleServiceBuilderProjectApiPathTest extends BaseCreateTest {

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectApiPath() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path nestedDir = workspace.resolve(Paths.get("modules", "nested/path"));

		String[] args =
			{"create", "-d", nestedDir.toString(), "-t", "service-builder", "-p", "com.liferay.sample", "sample"};

		makeWorkspace(workspace);

		Files.createDirectories(nestedDir);

		Assert.assertTrue(Files.exists(nestedDir));

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(nestedDir.resolve(Paths.get("sample", "build.gradle")));

		checkFileDoesNotExists(nestedDir.resolve(Paths.get("sample", "settings.gradle")));

		checkFileExists(nestedDir.resolve(Paths.get("sample", "sample-api", "build.gradle")));

		checkFileExists(nestedDir.resolve(Paths.get("sample", "sample-service", "build.gradle")));

		File file = checkFileExists(nestedDir.resolve(Paths.get("sample", "sample-service", "build.gradle")));

		contains(file, ".*compileOnly project\\(\":modules:nested:path:sample:sample-api\"\\).*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "buildService");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(
			nestedDir.resolve(Paths.get("sample", "sample-api")), "com.liferay.sample.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(
			nestedDir.resolve(Paths.get("sample", "sample-service")), "com.liferay.sample.service-1.0.0.jar");

		verifyImportPackage(
			nestedDir.resolve(
				Paths.get("sample", "sample-service", "build", "libs", "com.liferay.sample.service-1.0.0.jar")));
	}

}
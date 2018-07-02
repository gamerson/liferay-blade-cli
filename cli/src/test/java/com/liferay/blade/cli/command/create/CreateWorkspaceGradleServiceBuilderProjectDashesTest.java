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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.testkit.runner.BuildTask;

import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CreateWorkspaceGradleServiceBuilderProjectDashesTest extends BaseCreateTest {

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectDashes() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path modulesDir = workspace.resolve("modules");

		String[] args =
			{"create", "-d", modulesDir.toString(), "-t", "service-builder", "-p", "com.sample", "workspace-sample"};

		makeWorkspace(workspace);

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(modulesDir.resolve(Paths.get("workspace-sample", "build.gradle")));

		checkFileDoesNotExists(modulesDir.resolve(Paths.get("workspace-sample", "settings.gradle")));

		checkFileExists(modulesDir.resolve(Paths.get("workspace-sample", "workspace-sample-api", "build.gradle")));

		checkFileExists(modulesDir.resolve(Paths.get("workspace-sample", "workspace-sample-service", "build.gradle")));

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "buildService");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(
			modulesDir.resolve(Paths.get("workspace-sample", "workspace-sample-api")), "com.sample.api-1.0.0.jar");

		GradleRunnerUtil.verifyBuildOutput(
			modulesDir.resolve(Paths.get("workspace-sample", "workspace-sample-service")),
			"com.sample.service-1.0.0.jar");

		verifyImportPackage(
			modulesDir.resolve(
				Paths.get(
					"workspace-sample", "workspace-sample-service", "build", "libs", "com.sample.service-1.0.0.jar")));
	}

}
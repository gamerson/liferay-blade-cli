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
public class CreateWorkspaceGradleFragmentTest extends BaseCreateTest {

	@Test
	public void testCreateWorkspaceGradleFragment() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path extensionsDir = workspace.resolve(Paths.get("modules", "extensions"));

		String[] args = {
			"create", "-d", extensionsDir.toString(), "-t", "fragment", "-h", "com.liferay.login.web", "-H", "1.0.0",
			"loginHook"
		};

		makeWorkspace(workspace);

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(extensionsDir.resolve("loginHook"));

		contains(
			checkFileExists(extensionsDir.resolve(Paths.get("loginHook", "bnd.bnd"))),
			new String[] {
				".*^Bundle-SymbolicName: loginhook.*$",
				".*^Fragment-Host: com.liferay.login.web;bundle-version=\"1.0.0\".*$"
			});

		checkFileExists(extensionsDir.resolve(Paths.get("loginHook", "build.gradle")));

		lacks(
			checkFileExists(extensionsDir.resolve(Paths.get("loginHook", "build.gradle"))),
			".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(extensionsDir.resolve("loginHook"), "loginhook-1.0.0.jar");

		verifyImportPackage(extensionsDir.resolve(Paths.get("loginHook", "build", "libs", "loginhook-1.0.0.jar")));
	}

}
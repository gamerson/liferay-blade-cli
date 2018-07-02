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
public class CreateWorkspaceThemeLocationTest extends BaseCreateTest {

	@Test
	public void testCreateWorkspaceThemeLocation() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		String[] args = {"--base", workspace.toString(), "create", "-t", "theme", "theme-test"};

		makeWorkspace(workspace);

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = workspace.resolve(Paths.get("wars", "theme-test"));

		checkFileExists(projectPath);

		checkFileDoesNotExists(projectPath.resolve("bnd.bnd"));

		checkFileExists(projectPath.resolve(Paths.get("src", "main", "webapp", "css", "_custom.scss")));

		File properties = checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "webapp", "WEB-INF", "liferay-plugin-package.properties")));

		contains(properties, ".*^name=theme-test.*");

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "war");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(projectPath, "theme-test.war");
	}

}
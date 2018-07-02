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
import com.liferay.blade.cli.util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CreateFragmentTest extends BaseCreateTest {

	@Test
	public void testCreateFragment() throws Exception {
		String[] gradleArgs = {
			"create", "-d", tempRoot.toString(), "-t", "fragment", "-h", "com.liferay.login.web", "-H", "1.0.0",
			"loginHook"
		};

		String[] mavenArgs = {
			"create", "-d", tempRoot.toString(), "-b", "maven", "-t", "fragment", "-h", "com.liferay.login.web", "-H",
			"1.0.0", "loginHook"
		};

		Path projectPath = tempRoot.resolve("loginHook");

		TestUtil.runBlade(temporaryFolder.getRoot(), gradleArgs);

		checkGradleBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve("bnd.bnd")),
			new String[] {
				".*^Bundle-SymbolicName: loginhook.*$",
				".*^Fragment-Host: com.liferay.login.web;bundle-version=\"1.0.0\".*$"
			});

		contains(checkFileExists(projectPath.resolve("build.gradle")), ".*^apply plugin: \"com.liferay.plugin\".*");

		TestUtil.verifyBuild(projectPath, "loginhook-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "loginhook-1.0.0.jar")));

		FileUtil.deleteDir(projectPath);

		TestUtil.runBlade(temporaryFolder.getRoot(), mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve("bnd.bnd")),
			new String[] {
				".*^Bundle-SymbolicName: loginhook.*$",
				".*^Fragment-Host: com.liferay.login.web;bundle-version=\"1.0.0\".*$"
			});

		MavenRunnerUtil.executeGoals(projectPath, new String[] {"clean", "package"});

		MavenRunnerUtil.verifyBuildOutput(projectPath, "loginHook-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("target", "loginHook-1.0.0.jar")));
	}

}
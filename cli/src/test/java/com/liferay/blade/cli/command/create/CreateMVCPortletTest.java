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
public class CreateMVCPortletTest extends BaseCreateTest {

	@Test
	public void testCreateMVCPortlet() throws Exception {
		String[] gradleArgs = {"create", "-d", tempRoot.toString(), "-t", "mvc-portlet", "foo"};

		String[] mavenArgs = {"create", "-d", tempRoot.toString(), "-b", "maven", "-t", "mvc-portlet", "foo"};

		Path projectPath = tempRoot.resolve("foo");

		TestUtil.runBlade(tempRoot, gradleArgs);

		checkGradleBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve(Paths.get("src", "main", "java", "foo", "portlet", "FooPortlet.java"))),
			".*^public class FooPortlet extends MVCPortlet.*$");

		contains(checkFileExists(projectPath.resolve("build.gradle")), ".*^apply plugin: \"com.liferay.plugin\".*");

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "view.jsp")));

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "init.jsp")));

		TestUtil.verifyBuild(projectPath, "foo-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "foo-1.0.0.jar")));

		FileUtil.deleteDir(projectPath);

		TestUtil.runBlade(tempRoot, mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve(Paths.get("src", "main", "java", "foo", "portlet", "FooPortlet.java"))),
			".*^public class FooPortlet extends MVCPortlet.*$");

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "view.jsp")));

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "init.jsp")));

		MavenRunnerUtil.executeGoals(projectPath, new String[] {"clean", "package"});

		MavenRunnerUtil.verifyBuildOutput(projectPath, "foo-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("target", "foo-1.0.0.jar")));
	}

}
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

import com.liferay.blade.cli.TestUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CreateGradleMVCPortletProjectWithPackageTest extends BaseCreateTest {

	@Test
	public void testCreateGradleMVCPortletProjectWithPackage() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "mvc-portlet", "-p", "com.liferay.test", "foo"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Path projectPath = tempRoot.resolve("foo");

		checkFileExists(projectPath);

		checkFileExists(projectPath.resolve("bnd.bnd"));

		contains(
			checkFileExists(
				projectPath.resolve(
					Paths.get("src", "main", "java", "com", "liferay", "test", "portlet", "FooPortlet.java"))),
			".*^public class FooPortlet extends MVCPortlet.*$");

		contains(checkFileExists(projectPath.resolve("build.gradle")), ".*^apply plugin: \"com.liferay.plugin\".*");

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "view.jsp")));

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "init.jsp")));

		TestUtil.verifyBuild(projectPath, "com.liferay.test-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "com.liferay.test-1.0.0.jar")));
	}

}
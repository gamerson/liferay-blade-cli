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
public class CreateGradlePortletProjectTest extends BaseCreateTest {

	@Test
	public void testCreateGradlePortletProject() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "portlet", "-c", "Foo", "gradle.test"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Path projectPath = tempRoot.resolve("gradle.test");

		checkFileExists(projectPath);

		checkFileExists(projectPath.resolve("build.gradle"));

		contains(
			checkFileExists(
				projectPath.resolve(Paths.get("src", "main", "java", "gradle", "test", "portlet", "FooPortlet.java"))),
			new String[] {
				"^package gradle.test.portlet;.*", ".*javax.portlet.display-name=Foo.*",
				".*^public class FooPortlet .*", ".*Hello from Foo!.*"
			});

		TestUtil.verifyBuild(projectPath, "gradle.test-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "gradle.test-1.0.0.jar")));
	}

}
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
public class CreateWorkspaceGradlePortletProjectTest extends BaseCreateTest {

	@Test
	public void testCreateWorkspaceGradlePortletProject() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path appsDir = workspace.resolve(Paths.get("modules", "apps"));

		String[] args = {"create", "-d", appsDir.toString(), "-t", "portlet", "-c", "Foo", "gradle.test"};

		makeWorkspace(workspace);

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(appsDir.resolve(Paths.get("gradle.test", "build.gradle")));

		checkFileDoesNotExists(appsDir.resolve(Paths.get("gradle.test", "gradlew")));

		contains(
			checkFileExists(
				appsDir.resolve(
					Paths.get("gradle.test", "src", "main", "java", "gradle", "test", "portlet", "FooPortlet.java"))),
			new String[] {
				"^package gradle.test.portlet;.*", ".*javax.portlet.display-name=Foo.*",
				".*^public class FooPortlet .*", ".*Hello from Foo!.*"
			});

		lacks(
			checkFileExists(appsDir.resolve(Paths.get("gradle.test", "build.gradle"))),
			".*^apply plugin: \"com.liferay.plugin\".*");

		TestUtil.verifyBuild(workspace.toString(), "jar", "gradle.test-1.0.0.jar");

		verifyImportPackage(appsDir.resolve(Paths.get("gradle.test", "build", "libs", "gradle.test-1.0.0.jar")));
	}

}
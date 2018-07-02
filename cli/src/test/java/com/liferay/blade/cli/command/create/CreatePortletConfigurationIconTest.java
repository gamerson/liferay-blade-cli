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

import java.io.File;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CreatePortletConfigurationIconTest extends BaseCreateTest {

	@Test
	public void testCreatePortletConfigurationIcon() throws Exception {
		String[] args =
			{"create", "-d", tempRoot.toString(), "-t", "portlet-configuration-icon", "-p", "blade.test", "icontest"};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("icontest");

		checkFileExists(projectPath);

		checkFileExists(projectPath.resolve("bnd.bnd"));

		File componentFile = checkFileExists(
			projectPath.resolve(
				Paths.get(
					"src", "main", "java", "blade", "test", "portlet", "configuration", "icon",
					"IcontestPortletConfigurationIcon.java")));

		contains(
			componentFile, ".*^public class IcontestPortletConfigurationIcon.*extends BasePortletConfigurationIcon.*$");

		File gradleBuildFile = checkFileExists(projectPath.resolve("build.gradle"));

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		TestUtil.verifyBuild(projectPath, "blade.test-1.0.0.jar");
	}

}
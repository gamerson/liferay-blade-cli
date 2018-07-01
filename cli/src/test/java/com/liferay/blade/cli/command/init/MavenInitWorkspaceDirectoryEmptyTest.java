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

package com.liferay.blade.cli.command.init;

import com.liferay.blade.cli.TestUtil;

import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class MavenInitWorkspaceDirectoryEmptyTest extends BaseInitTest {

	@Test
	public void testMavenInitWorkspaceDirectoryEmpty() throws Exception {
		String[] args = {"--base", workspaceDir.toString(), "init", "-b", "maven"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Assert.assertTrue(Files.exists(workspaceDir.resolve("pom.xml")));

		Assert.assertTrue(Files.exists(workspaceDir.resolve("modules")));

		Assert.assertFalse(Files.exists(workspaceDir.resolve("build.gradle")));

		Assert.assertFalse(Files.exists(workspaceDir.resolve("gradle.properties")));

		Assert.assertFalse(Files.exists(workspaceDir.resolve("gradle-local.properties")));

		Assert.assertFalse(Files.exists(workspaceDir.resolve("settings.gradle")));

		verifyMavenBuild();
	}

}
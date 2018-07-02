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
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class InitWithNameWorkspaceDirectoryHasFilesTest extends BaseInitTest {

	@Test
	public void testInitWithNameWorkspaceDirectoryHasFiles() throws Exception {
		String[] args = {"--base", workspaceDir.toString(), "init", "newproject"};

		Path newProjectPath = workspaceDir.resolve("newproject");

		Files.createDirectories(newProjectPath);

		Assert.assertTrue(Files.exists(newProjectPath));

		Path fooPath = Files.createFile(workspaceDir.resolve(Paths.get("newproject", "foo")));

		Assert.assertTrue(Files.exists(fooPath));

		boolean encounteredError = false;

		try {
			TestUtil.runBlade(temporaryFolder.getRoot(), args);
		}
		catch (Throwable th) {
			encounteredError = true;
		}

		Assert.assertTrue(encounteredError);

		Path buildGradlePath = workspaceDir.resolve(Paths.get("newproject", "build.gradle"));

		Assert.assertFalse(Files.exists(buildGradlePath));
	}

}
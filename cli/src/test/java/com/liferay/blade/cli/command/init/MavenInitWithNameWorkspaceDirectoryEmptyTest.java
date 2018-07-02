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

import com.liferay.blade.cli.BladeSettings;
import com.liferay.blade.cli.BladeTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class MavenInitWithNameWorkspaceDirectoryEmptyTest extends BaseInitTest {

	@Test
	public void testMavenInitWithNameWorkspaceDirectoryEmpty() throws Exception {
		String[] args = {"--base", workspaceDir.toString(), "init", "-f", "-b", "maven", "newproject"};

		Path newproject = workspaceDir.resolve("newproject");

		Files.createDirectories(newproject);

		Assert.assertTrue(Files.exists(newproject));

		BladeTest bladeTest = new BladeTest();

		bladeTest.setUserHomeDir(temporaryFolder.getRoot());

		bladeTest.run(args);

		Assert.assertTrue(Files.exists(newproject.resolve("pom.xml")));

		Assert.assertTrue(Files.exists(newproject.resolve("modules")));

		String contents = new String(Files.readAllBytes(newproject.resolve("pom.xml")));

		Assert.assertTrue(contents, contents.contains("3.2.1"));

		Path metadataFile = workspaceDir.resolve(Paths.get("newproject", ".blade", "settings.properties"));

		Assert.assertTrue(Files.exists(metadataFile));

		BladeSettings bladeSettings = bladeTest.getSettings();

		Assert.assertEquals("maven", bladeSettings.getProfileName());
	}

}
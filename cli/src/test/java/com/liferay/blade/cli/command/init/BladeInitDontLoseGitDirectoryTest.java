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
import com.liferay.blade.cli.util.BladeUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class BladeInitDontLoseGitDirectoryTest extends BaseInitTest {

	@Test
	public void testBladeInitDontLoseGitDirectory() throws Exception {
		Path testdir = tempRoot.resolve(Paths.get("build", "testBladeInitDontLoseGitDirectory"));

		Files.createDirectories(testdir);

		BladeUtil.unzip(Paths.get("test-resources", "projects", "plugins-sdk-with-git.zip"), testdir);

		Assert.assertTrue(Files.exists(testdir));

		Path projectDir = testdir.resolve("plugins-sdk-with-git");

		String[] args = {"--base", projectDir.toString(), "init", "-u"};

		TestUtil.runBlade(tempRoot, args);

		Path gitdir = projectDir.resolve(".git");

		Assert.assertTrue(Files.exists(gitdir));

		Path oldGitIgnore = projectDir.resolve(Paths.get("plugins-sdk", ".gitignore"));

		Assert.assertTrue(Files.exists(oldGitIgnore));
	}

}
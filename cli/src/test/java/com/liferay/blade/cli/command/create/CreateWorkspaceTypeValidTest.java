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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CreateWorkspaceTypeValidTest extends BaseCreateTest {

	@Test
	public void testCreateWorkspaceTypeValid() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path modulesDir = workspace.resolve("modules");

		String[] args = {"--base", modulesDir.toString(), "create", "-t", "soy-portlet", "foo"};

		makeWorkspace(workspace);

		TestUtil.runBlade(tempRoot, args);

		Path buildGradle = modulesDir.resolve(Paths.get("foo", "build.gradle"));

		checkFileExists(buildGradle);

		String content = new String(Files.readAllBytes(buildGradle));

		Assert.assertEquals(1, StringUtils.countMatches(content, '{'));

		Assert.assertEquals(1, StringUtils.countMatches(content, '}'));
	}

}
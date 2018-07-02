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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class LiferayVersionTest extends BaseCreateTest {

	@Test
	public void testLiferayVersion() throws Exception {
		String[] sevenZeroArgs = {"--base", tempRoot.toString(), "create", "-t", "npm-angular-portlet", "seven-zero"};

		TestUtil.runBlade(tempRoot, sevenZeroArgs);

		Path npmbundlerrc = tempRoot.resolve(Paths.get("seven-zero", "build.gradle"));

		String content = new String(Files.readAllBytes(npmbundlerrc));

		Assert.assertFalse(content.contains("js.loader.modules.extender.api"));

		String[] sevenOneArgs =
			{"--base", tempRoot.toString(), "create", "-t", "npm-angular-portlet", "-v", "7.1", "seven-one"};

		TestUtil.runBlade(tempRoot, sevenOneArgs);

		npmbundlerrc = tempRoot.resolve(Paths.get("seven-one", "build.gradle"));

		content = new String(Files.readAllBytes(npmbundlerrc));

		Assert.assertTrue(content.contains("js.loader.modules.extender.api"));
	}

}
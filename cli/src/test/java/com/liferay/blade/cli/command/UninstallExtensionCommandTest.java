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

package com.liferay.blade.cli.command;

import com.liferay.blade.cli.TestUtil;

import java.io.File;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Christopher Bryan Boyd
 */
public class UninstallExtensionCommandTest {

	@Test
	public void testUninstallCustomExtensionMock() throws Exception {
		String jarName = "custom.blade.extension.jar";

		String[] args = {"extension", "uninstall", jarName};

		File extensionsDir = new File(tempFolder.getRoot(), ".blade/extensions");

		extensionsDir.mkdirs();

		File testJar = new File(extensionsDir, jarName);

		Assert.assertTrue(testJar.createNewFile());

		String output = TestUtil.runBlade(tempFolder.getRoot().toPath(), args);

		Assert.assertTrue(output.contains(" successful") && output.contains(jarName));

		Assert.assertTrue(!testJar.exists());
	}

	@Rule
	public final TemporaryFolder tempFolder = new TemporaryFolder();

}
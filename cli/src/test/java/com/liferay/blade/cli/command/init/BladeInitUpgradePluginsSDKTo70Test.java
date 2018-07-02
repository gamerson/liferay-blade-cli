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

import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class BladeInitUpgradePluginsSDKTo70Test extends BaseInitTest {

	@Test
	public void testBladeInitUpgradePluginsSDKTo70() throws Exception {
		Path testdir = tempRoot.resolve(Paths.get("build", "testUpgradePluginsSDKTo70"));

		Files.createDirectories(testdir);

		BladeUtil.unzip(Paths.get("test-resources", "projects", "plugins-sdk-with-git.zip"), testdir);

		Assert.assertTrue(Files.exists(testdir));

		Path projectDir = testdir.resolve("plugins-sdk-with-git");

		String[] args = {"--base", projectDir.toString(), "init", "-u"};

		TestUtil.runBlade(tempRoot, args);

		Path buildProperties = projectDir.resolve(Paths.get("plugins-sdk", "build.properties"));

		Properties props = new Properties();

		try (final FileChannel channel = FileChannel.open(buildProperties, StandardOpenOption.READ);
			final FileLock lock = channel.lock(0L, Long.MAX_VALUE, true)) {

			props.load(Channels.newInputStream(channel));
		}

		String version = props.getProperty("lp.version");

		Assert.assertEquals("7.0.0", version);
	}

}
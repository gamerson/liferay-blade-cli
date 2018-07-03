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

import static com.liferay.blade.cli.TestUtil.makeSDK;
import static com.liferay.blade.cli.TestUtil.verifyGradleBuild;
import static com.liferay.blade.cli.TestUtil.verifyMavenBuild;

import com.googlecode.junittoolbox.ParallelRunner;

import com.liferay.blade.cli.BladeSettings;
import com.liferay.blade.cli.BladeTest;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

/**
 * @author Gregory Amerson
 */
@RunWith(ParallelRunner.class)
public class InitCommandTest {

	@Before
	public void setUp() throws Exception {
		_tempRoot = temporaryFolder.getRoot().toPath();

		_workspaceDir = temporaryFolder.newFolder("build", "test", "workspace").toPath();
	}

	@Test
	public void testBladeInitDontLoseGitDirectory() throws Exception {
		Path testdir = _tempRoot.resolve(Paths.get("build", "testBladeInitDontLoseGitDirectory"));

		Files.createDirectories(testdir);

		BladeUtil.unzip(Paths.get("test-resources", "projects", "plugins-sdk-with-git.zip"), testdir);

		Assert.assertTrue(Files.exists(testdir));

		Path projectDir = testdir.resolve("plugins-sdk-with-git");

		String[] args = {"--base", projectDir.toString(), "init", "-u"};

		TestUtil.runBlade(_tempRoot, args);

		Path gitdir = projectDir.resolve(".git");

		Assert.assertTrue(Files.exists(gitdir));

		Path oldGitIgnore = projectDir.resolve(Paths.get("plugins-sdk", ".gitignore"));

		Assert.assertTrue(Files.exists(oldGitIgnore));
	}

	@Test
	public void testBladeInitUpgradePluginsSDKTo70() throws Exception {
		Path testdir = _tempRoot.resolve(Paths.get("build", "testUpgradePluginsSDKTo70"));

		Files.createDirectories(testdir);

		BladeUtil.unzip(Paths.get("test-resources", "projects", "plugins-sdk-with-git.zip"), testdir);

		Assert.assertTrue(Files.exists(testdir));

		Path projectDir = testdir.resolve("plugins-sdk-with-git");

		String[] args = {"--base", projectDir.toString(), "init", "-u"};

		TestUtil.runBlade(_tempRoot, args);

		Path buildProperties = projectDir.resolve(Paths.get("plugins-sdk", "build.properties"));

		Properties props = new Properties();

		try (final FileChannel channel = FileChannel.open(buildProperties, StandardOpenOption.READ);
			final FileLock lock = channel.lock(0L, Long.MAX_VALUE, true)) {

			props.load(Channels.newInputStream(channel));
		}

		String version = props.getProperty("lp.version");

		Assert.assertEquals("7.0.0", version);
	}

	@Test
	public void testDefaultInitWorkspaceDirectoryEmpty() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Assert.assertTrue(Files.exists(_workspaceDir));

		Assert.assertTrue(Files.exists(_workspaceDir.resolve("build.gradle")));

		Assert.assertTrue(Files.exists(_workspaceDir.resolve("modules")));

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("com")));

		verifyGradleBuild(_workspaceDir);
	}

	@Test
	public void testDefaultInitWorkspaceDirectoryHasFiles() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init"};

		Path fooPath = _workspaceDir.resolve("foo");

		Files.createFile(fooPath);

		Assert.assertTrue(Files.exists(fooPath));

		boolean encounteredError = false;

		try {
			TestUtil.runBlade(temporaryFolder.getRoot(), args);
		}
		catch (Throwable th) {
			encounteredError = true;
		}

		Assert.assertTrue(encounteredError);

		Path gradlePath = _workspaceDir.resolve("build.gradle");

		Assert.assertFalse(Files.exists(gradlePath));
	}

	@Test
	public void testDefaultInitWorkspaceDirectoryHasFilesForce() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "-f"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Assert.assertTrue(Files.exists(_workspaceDir));

		Assert.assertTrue(Files.exists(_workspaceDir.resolve("build.gradle")));

		Assert.assertTrue(Files.exists(_workspaceDir.resolve("modules")));

		verifyGradleBuild(_workspaceDir);
	}

	@Test
	public void testInitCommandGradleOption() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "-b", "gradle", "gradleworkspace"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Path gradleWorkspace = _workspaceDir.resolve("gradleworkspace");

		Assert.assertTrue(Files.exists(gradleWorkspace));

		Assert.assertFalse(Files.exists(gradleWorkspace.resolve("pom.xml")));

		Assert.assertTrue(Files.exists(gradleWorkspace.resolve("build.gradle")));
	}

	@Test
	public void testInitInPluginsSDKDirectory() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "-u"};

		makeSDK(_workspaceDir);

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Assert.assertTrue(Files.exists(_workspaceDir.resolve("build.gradle")));

		Assert.assertTrue(Files.exists(_workspaceDir.resolve("modules")));

		Assert.assertTrue(Files.exists(_workspaceDir.resolve("themes")));

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("portlets")));

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("hooks")));

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("build.properties")));

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("build.xml")));

		Assert.assertTrue(Files.exists(_workspaceDir.resolve("plugins-sdk/build.properties")));

		Assert.assertTrue(Files.exists(_workspaceDir.resolve("plugins-sdk/build.xml")));
	}

	@Test
	public void testInitWithLiferayVersion70() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "-v", "7.0"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		String contents = new String(Files.readAllBytes(_workspaceDir.resolve("gradle.properties")));

		Assert.assertTrue(contents, contents.contains("7.0.6-ga7"));
	}

	@Test
	public void testInitWithLiferayVersion71() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "-v", "7.1"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		String contents = new String(Files.readAllBytes(_workspaceDir.resolve("gradle.properties")));

		Assert.assertTrue(contents, contents.contains("7.1.0-b3"));
	}

	@Test
	public void testInitWithLiferayVersionDefault() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		String contents = new String(Files.readAllBytes(_workspaceDir.resolve("gradle.properties")));

		Assert.assertTrue(contents, contents.contains("7.0.6-ga7"));
	}

	@Test
	public void testInitWithNameWorkspaceDirectoryEmpty() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "-f", "newproject"};

		Path newproject = _workspaceDir.resolve("newproject");

		Files.createDirectories(newproject);

		Assert.assertTrue(Files.exists(newproject));

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Assert.assertTrue(Files.exists(newproject.resolve("build.gradle")));

		Assert.assertTrue(Files.exists(newproject.resolve("modules")));

		String contents = new String(Files.readAllBytes(newproject.resolve("settings.gradle")));

		Assert.assertTrue(contents, contents.contains("1.10"));
	}

	@Test
	public void testInitWithNameWorkspaceDirectoryHasFiles() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "newproject"};

		Path newProjectPath = _workspaceDir.resolve("newproject");

		Files.createDirectories(newProjectPath);

		Assert.assertTrue(Files.exists(newProjectPath));

		Path fooPath = Files.createFile(_workspaceDir.resolve(Paths.get("newproject", "foo")));

		Assert.assertTrue(Files.exists(fooPath));

		boolean encounteredError = false;

		try {
			TestUtil.runBlade(temporaryFolder.getRoot(), args);
		}
		catch (Throwable th) {
			encounteredError = true;
		}

		Assert.assertTrue(encounteredError);

		Path buildGradlePath = _workspaceDir.resolve(Paths.get("newproject", "build.gradle"));

		Assert.assertFalse(Files.exists(buildGradlePath));
	}

	@Test
	public void testInitWithNameWorkspaceNotExists() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "newproject"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Path buildGradlePath = _workspaceDir.resolve(Paths.get("newproject", "build.gradle"));

		Assert.assertTrue(Files.exists(buildGradlePath));

		Path modulesPath = _workspaceDir.resolve(Paths.get("newproject", "modules"));

		Assert.assertTrue(Files.exists(modulesPath));
	}

	@Test
	public void testMavenInitWithNameWorkspaceDirectoryEmpty() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "-f", "-b", "maven", "newproject"};

		Path newproject = _workspaceDir.resolve("newproject");

		Files.createDirectories(newproject);

		Assert.assertTrue(Files.exists(newproject));

		BladeTest bladeTest = new BladeTest();

		bladeTest.setUserHomeDir(temporaryFolder.getRoot());

		bladeTest.run(args);

		Assert.assertTrue(Files.exists(newproject.resolve("pom.xml")));

		Assert.assertTrue(Files.exists(newproject.resolve("modules")));

		String contents = new String(Files.readAllBytes(newproject.resolve("pom.xml")));

		Assert.assertTrue(contents, contents.contains("3.2.1"));

		Path metadataFile = _workspaceDir.resolve(Paths.get("newproject", ".blade", "settings.properties"));

		Assert.assertTrue(Files.exists(metadataFile));

		BladeSettings bladeSettings = bladeTest.getSettings();

		Assert.assertEquals("maven", bladeSettings.getProfileName());
	}

	@Test
	public void testMavenInitWithNameWorkspaceDirectoryHasFiles() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "-b", "maven", "newproject"};

		Path projectDir = Files.createDirectories(_workspaceDir.resolve("newproject"));

		Assert.assertTrue(Files.exists(projectDir));

		Path fooPath = Files.createFile(_workspaceDir.resolve(Paths.get("newproject", "foo")));

		Assert.assertTrue(Files.exists(fooPath));

		boolean encounteredError = false;

		try {
			TestUtil.runBlade(temporaryFolder.getRoot(), args);
		}
		catch (Throwable th) {
			encounteredError = true;
		}

		Assert.assertTrue(encounteredError);

		Path pomPath = _workspaceDir.resolve(Paths.get("newproject", "pom.xml"));

		Assert.assertFalse(Files.exists(pomPath));
	}

	@Test
	public void testMavenInitWithNameWorkspaceNotExists() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "-b", "maven", "newproject"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Assert.assertTrue(Files.exists(_workspaceDir.resolve(Paths.get("newproject", "pom.xml"))));

		Assert.assertTrue(Files.exists(_workspaceDir.resolve(Paths.get("newproject", "modules"))));
	}

	@Test
	public void testMavenInitWorkspaceDirectoryEmpty() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "-b", "maven"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Assert.assertTrue(Files.exists(_workspaceDir.resolve("pom.xml")));

		Assert.assertTrue(Files.exists(_workspaceDir.resolve("modules")));

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("build.gradle")));

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("gradle.properties")));

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("gradle-local.properties")));

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("settings.gradle")));

		verifyMavenBuild(_workspaceDir);
	}

	@Test
	public void testMavenInitWorkspaceDirectoryHasFiles() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "-b", "maven"};

		Path fooPath = Files.createFile(_workspaceDir.resolve("foo"));

		Assert.assertTrue(Files.exists(fooPath));

		boolean encounteredError = false;

		try {
			TestUtil.runBlade(temporaryFolder.getRoot(), args);
		}
		catch (Throwable th) {
			encounteredError = true;
		}

		Assert.assertTrue(encounteredError);

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("pom.xml")));
	}

	@Test
	public void testMavenInitWorkspaceDirectoryHasFilesForce() throws Exception {
		String[] args = {"--base", _workspaceDir.toString(), "init", "-f", "-b", "maven"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Assert.assertTrue(Files.exists(_workspaceDir));

		Assert.assertTrue(Files.exists(_workspaceDir.resolve("pom.xml")));

		Assert.assertTrue(Files.exists(_workspaceDir.resolve("modules")));

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("build.gradle")));

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("gradle.properties")));

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("gradle-local.properties")));

		Assert.assertFalse(Files.exists(_workspaceDir.resolve("settings.gradle")));

		verifyMavenBuild(_workspaceDir);
	}

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private Path _tempRoot = null;
	private Path _workspaceDir = null;

}
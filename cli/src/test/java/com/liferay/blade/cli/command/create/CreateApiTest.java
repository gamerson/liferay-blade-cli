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

import com.liferay.blade.cli.MavenRunnerUtil;
import com.liferay.blade.cli.TestUtil;
import com.liferay.blade.cli.util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CreateApiTest extends BaseCreateTest {

	@Test
	public void testCreateApi() throws Exception {
		String[] gradleArgs = {"create", "-d", tempRoot.toString(), "-t", "api", "foo"};

		String[] mavenArgs = {"create", "-d", tempRoot.toString(), "-b", "maven", "-t", "api", "foo"};

		Path projectPath = tempRoot.resolve("foo");

		TestUtil.runBlade(temporaryFolder.getRoot(), gradleArgs);

		checkGradleBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve(Paths.get("src", "main", "java", "foo", "api", "Foo.java"))),
			".*^public interface Foo.*");

		contains(
			checkFileExists(projectPath.resolve(Paths.get("src", "main", "resources", "foo", "api", "packageinfo"))),
			"version 1.0.0");

		TestUtil.verifyBuild(projectPath, "foo-1.0.0.jar");

		Path jarPath = projectPath.resolve(Paths.get("build", "libs", "foo-1.0.0.jar"));

		try (JarFile jar = new JarFile(jarPath.toFile())) {
			Manifest manifest = jar.getManifest();

			Attributes mainAttributes = manifest.getMainAttributes();

			Assert.assertEquals("foo.api;version=\"1.0.0\"", mainAttributes.getValue("Export-Package"));
		}

		FileUtil.deleteDir(projectPath);

		jarPath = projectPath.resolve(Paths.get("target", "foo-1.0.0.jar"));

		TestUtil.runBlade(temporaryFolder.getRoot(), mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve(Paths.get("src", "main", "java", "foo", "api", "Foo.java"))),
			".*^public interface Foo.*");

		contains(
			checkFileExists(projectPath.resolve(Paths.get("src", "main", "resources", "foo", "api", "packageinfo"))),
			"version 1.0.0");

		MavenRunnerUtil.executeGoals(projectPath, new String[] {"clean", "package"});

		MavenRunnerUtil.verifyBuildOutput(projectPath, "foo-1.0.0.jar");

		verifyImportPackage(jarPath);

		try (JarFile jar = new JarFile(jarPath.toFile())) {
			Manifest manifest = jar.getManifest();

			Attributes mainAttributes = manifest.getMainAttributes();

			Assert.assertEquals("foo.api;version=\"1.0.0\"", mainAttributes.getValue("Export-Package"));
		}
	}

}
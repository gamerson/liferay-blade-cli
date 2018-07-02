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

import com.liferay.blade.cli.GradleRunnerUtil;
import com.liferay.blade.cli.TestUtil;

import java.io.File;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.gradle.testkit.runner.BuildTask;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CreateWorkspaceGradleServiceBuilderProjectDefaultTest extends BaseCreateTest {

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectDefault() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path modulesDir = workspace.resolve("modules");

		String[] args =
			{"create", "-d", modulesDir.toString(), "-t", "service-builder", "-p", "com.liferay.sample", "sample"};

		makeWorkspace(workspace);

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(modulesDir.resolve(Paths.get("sample", "build.gradle")));

		checkFileDoesNotExists(modulesDir.resolve(Paths.get("sample", "settings.gradle")));

		checkFileExists(modulesDir.resolve(Paths.get("sample", "sample-api", "build.gradle")));

		checkFileExists(modulesDir.resolve(Paths.get("sample", "sample-service", "build.gradle")));

		File file = checkFileExists(modulesDir.resolve(Paths.get("", "sample", "sample-service", "build.gradle")));

		contains(file, ".*compileOnly project\\(\":modules:sample:sample-api\"\\).*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "buildService");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(
			modulesDir.resolve(Paths.get("sample", "sample-api")), "com.liferay.sample.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(
			modulesDir.resolve(Paths.get("sample", "sample-service")), "com.liferay.sample.service-1.0.0.jar");

		Path serviceJar = modulesDir.resolve(
			Paths.get("sample", "sample-service", "build", "libs", "com.liferay.sample.service-1.0.0.jar"));

		verifyImportPackage(serviceJar);

		try (JarFile serviceJarFile = new JarFile(serviceJar.toFile())) {
			Manifest manifest = serviceJarFile.getManifest();

			Attributes mainAttributes = manifest.getMainAttributes();

			String springContext = mainAttributes.getValue("Liferay-Spring-Context");

			Assert.assertTrue(springContext.equals("META-INF/spring"));
		}
	}

}
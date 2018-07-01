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
public class CreateGradleServiceBuilderDefaultTest extends BaseCreateTest {

	@Test
	public void testCreateGradleServiceBuilderDefault() throws Exception {
		String[] args = {
			"create", "-d", tempRoot.toString(), "-t", "service-builder", "-p", "com.liferay.docs.guestbook",
			"guestbook"
		};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Path projectPath = tempRoot.resolve("guestbook");

		contains(
			checkFileExists(projectPath.resolve("settings.gradle")),
			"include \"guestbook-api\", \"guestbook-service\"");

		contains(
			checkFileExists(projectPath.resolve(Paths.get("guestbook-api", "bnd.bnd"))),
			new String[] {
				".*Export-Package:\\\\.*", ".*com.liferay.docs.guestbook.exception,\\\\.*",
				".*com.liferay.docs.guestbook.model,\\\\.*", ".*com.liferay.docs.guestbook.service,\\\\.*",
				".*com.liferay.docs.guestbook.service.persistence.*"
			});

		contains(
			checkFileExists(projectPath.resolve(Paths.get("guestbook-service", "bnd.bnd"))),
			".*Liferay-Service: true.*");

		File file = checkFileExists(projectPath.resolve(Paths.get("guestbook-service", "build.gradle")));

		contains(file, ".*compileOnly project\\(\":guestbook-api\"\\).*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(projectPath.toString(), "buildService");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(projectPath.toString(), "build");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(
			projectPath.resolve("guestbook-api"), "com.liferay.docs.guestbook.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(
			projectPath.resolve("guestbook-service"), "com.liferay.docs.guestbook.service-1.0.0.jar");

		Path serviceJar = projectPath.resolve(
			Paths.get("guestbook-service", "build", "libs", "com.liferay.docs.guestbook.service-1.0.0.jar"));

		verifyImportPackage(serviceJar);

		try (JarFile serviceJarFile = new JarFile(serviceJar.toFile())) {
			Manifest manifest = serviceJarFile.getManifest();

			Attributes mainAttributes = manifest.getMainAttributes();

			String springContext = mainAttributes.getValue("Liferay-Spring-Context");

			Assert.assertTrue(springContext.equals("META-INF/spring"));
		}
	}

}
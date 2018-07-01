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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.testkit.runner.BuildTask;

import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CreateGradleServiceBuilderDashesTest extends BaseCreateTest {

	@Test
	public void testCreateGradleServiceBuilderDashes() throws Exception {
		String[] args = {
			"create", "-d", tempRoot.toString(), "-t", "service-builder", "-p", "com.liferay.backend.integration",
			"backend-integration"
		};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Path projectPath = tempRoot.resolve("backend-integration");

		contains(
			checkFileExists(projectPath.resolve("settings.gradle")),
			"include \"backend-integration-api\", \"backend-integration-service\"");

		contains(
			checkFileExists(projectPath.resolve(Paths.get("backend-integration-api", "bnd.bnd"))),
			new String[] {
				".*Export-Package:\\\\.*", ".*com.liferay.backend.integration.exception,\\\\.*",
				".*com.liferay.backend.integration.model,\\\\.*", ".*com.liferay.backend.integration.service,\\\\.*",
				".*com.liferay.backend.integration.service.persistence.*"
			});

		contains(
			checkFileExists(projectPath.resolve(Paths.get("backend-integration-service", "bnd.bnd"))),
			".*Liferay-Service: true.*");

		BuildTask buildServiceTask = GradleRunnerUtil.executeGradleRunner(projectPath.toString(), "buildService");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildServiceTask);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(projectPath.toString(), "build");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(
			projectPath.resolve("backend-integration-api").toString(), "com.liferay.backend.integration.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(
			projectPath.resolve("backend-integration-service").toString(),
			"com.liferay.backend.integration.service-1.0.0.jar");

		verifyImportPackage(
			projectPath.resolve(
				Paths.get(
					"backend-integration-service", "build", "libs",
					"com.liferay.backend.integration.service-1.0.0.jar")));
	}

}
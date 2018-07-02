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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CreateGradleServiceTest extends BaseCreateTest {

	@Test
	public void testCreateGradleService() throws Exception {
		String[] args = {
			"create", "-d", tempRoot.toString(), "-t", "service", "-s",
			"com.liferay.portal.kernel.events.LifecycleAction", "-c", "FooAction", "servicepreaction"
		};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("servicepreaction");

		checkFileExists(projectPath.resolve("build.gradle"));

		Path path = projectPath.resolve(Paths.get("src", "main", "java", "servicepreaction", "FooAction.java"));

		contains(
			checkFileExists(path),
			new String[] {
				"^package servicepreaction;.*", ".*^import com.liferay.portal.kernel.events.LifecycleAction;$.*",
				".*service = LifecycleAction.class.*", ".*^public class FooAction implements LifecycleAction \\{.*"
			});

		List<String> lines = new ArrayList<>();
		String line = null;

		try (InputStream in = Files.newInputStream(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

			while ((line = reader.readLine()) != null) {
				lines.add(line);

				if (line.equals("import com.liferay.portal.kernel.events.LifecycleAction;")) {
					lines.add("import com.liferay.portal.kernel.events.LifecycleEvent;");
					lines.add("import com.liferay.portal.kernel.events.ActionException;");
				}

				if (line.equals("public class FooAction implements LifecycleAction {")) {
					StringBuilder sb = new StringBuilder();

					sb.append("@Override\n");
					sb.append(
						"public void processLifecycleEvent(LifecycleEvent lifecycleEvent)" + System.lineSeparator());
					sb.append("throws ActionException {\n");
					sb.append("System.out.println(\"login.event.pre=\" + lifecycleEvent);" + System.lineSeparator());
					sb.append("}" + System.lineSeparator());

					lines.add(sb.toString());
				}
			}
		}

		String output = String.join(System.lineSeparator(), lines);

		Files.write(path, output.getBytes());

		TestUtil.verifyBuild(projectPath, "servicepreaction-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "servicepreaction-1.0.0.jar")));
	}

}
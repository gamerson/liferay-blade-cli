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

package com.liferay.blade.test.apichanges;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.liferay.blade.api.FileMigrator;
import com.liferay.blade.api.Problem;

import java.io.File;
import java.util.List;

import org.junit.Test;

/**
 * @author Andy Wu
 */
public class MavenPomLegacyPluginsTest extends APITestBase {

	@Override
	public String getImplClassName() {
		return "MavenPomLegacyPlugins";
	}

	@Override
	public File getTestFile() {
		return new File("projects/test-maven/sub-portlet/pom.xml");
	}

	@Override
	public int getExpectedNumber() {
		return 1;
	}

	@Test
	public void testMavenProjectPomMigrator() throws Exception {
		assertNotNull(fileMigrators[0]);

		FileMigrator fmigrator = context.getService(fileMigrators[0]);

		List<Problem> problems = fmigrator.analyze(
				new File("projects/test-maven/parent/pom.xml") );
		assertEquals(1, problems.size());

		problems = fmigrator.analyze(
				new File("projects/test-maven/sub-service/pom.xml") );
		assertEquals(1, problems.size());
	}

}

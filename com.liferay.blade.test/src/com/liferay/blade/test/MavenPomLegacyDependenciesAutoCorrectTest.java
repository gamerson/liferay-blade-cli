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

package com.liferay.blade.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import aQute.lib.io.IO;

import com.liferay.blade.api.AutoMigrator;
import com.liferay.blade.api.FileMigrator;
import com.liferay.blade.api.Problem;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Andy Wu
 */
public class MavenPomLegacyDependenciesAutoCorrectTest {

	final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
	private AutoMigrator autoMigrator = null;

	@Before
	public void beforeTest() throws Exception {
		Filter filter = context.createFilter("(auto.correct=maven-pom-legacy-dependencies)");

		ServiceTracker<AutoMigrator, AutoMigrator> tracker = new ServiceTracker<AutoMigrator, AutoMigrator>(context,
				filter, null);

		tracker.open();

		ServiceReference<AutoMigrator>[] refs = tracker.getServiceReferences();

		assertNotNull(refs);

		assertEquals(1, refs.length);

		autoMigrator = context.getService(refs[0]);
	}

	@Test
	public void testAutoCorrectPomXml() throws Exception {
		assertNotNull(autoMigrator);

		FileMigrator fileMigrator = (FileMigrator) autoMigrator;

		File parentfile = new File("generated/test/maven/sub-portlet/pom.xml");

		if (parentfile.exists()) {
			assertTrue(parentfile.delete());
		}

		parentfile.getParentFile().mkdirs();

		IO.copy(new File("projects/test-maven/sub-portlet/pom.xml"), parentfile);

		List<Problem> problems = fileMigrator.analyze(parentfile);

		assertEquals(4, problems.size());

		int corrected = autoMigrator.correctProblems(parentfile, problems);

		assertEquals(4, corrected);

		problems = fileMigrator.analyze(parentfile);

		assertEquals(0, problems.size());

		parentfile = new File("generated/test/maven/sub-service/pom.xml");

		if (parentfile.exists()) {
			assertTrue(parentfile.delete());
		}

		parentfile.getParentFile().mkdirs();

		IO.copy(new File("projects/test-maven/sub-service/pom.xml"), parentfile);

		problems = fileMigrator.analyze(parentfile);

		assertEquals(1, problems.size());

		corrected = autoMigrator.correctProblems(parentfile, problems);

		assertEquals(1, corrected);

		problems = fileMigrator.analyze(parentfile);

		assertEquals(0, problems.size());
	}
}

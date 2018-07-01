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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
public class CreateServiceTemplateServiceParameterRequiredTest extends BaseCreateTest {

	@Test
	public void testCreateServiceTemplateServiceParameterRequired() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "service", "foo"};

		String output = null;

		try {
			output = TestUtil.runBlade(temporaryFolder.getRoot(), args);
		}
		catch (Throwable t) {
			output = t.getMessage();
		}

		Assert.assertNotNull(output);

		Assert.assertTrue(output, output.contains("Usage:"));

		args = new String[] {"create", "-t", "service", "-s com.test.Foo", "foo"};

		try {
			output = TestUtil.runBlade(temporaryFolder.getRoot(), args);
		}
		catch (Throwable t) {
			output = t.getMessage();
		}

		Assert.assertFalse(output, output.contains("Usage:"));
	}

}
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

package com.liferay.blade.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * @author Vernon Singleton
 */
@Parameters(
	commandDescription = "Helps to upgrade your build.",
	commandNames = "upgradeBuild"
)
public class UpgradeBuildArgs extends BaseArgs {

	public boolean isDryrun() {
		return _dryrun;
	}

	public void setDryrun(boolean dryrun) {
		_dryrun = dryrun;
	}

	@Parameter(description = "Perform a trial run with no changes made.", names = {"-n", "--dryrun"})
	private boolean _dryrun;

}
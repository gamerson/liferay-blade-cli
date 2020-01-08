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

import java.io.File;

/**
 * @author Gregory Amerson
 * @author Terry Jia
 */
@Parameters(commandDescription = "Generate a sample project", commandNames = "samples")
public class SamplesArgs extends BaseArgs {

	public String getCategory() {
		return _category;
	}

	public File getDir() {
		return _dir;
	}

	public String getLiferayVersion() {
		return _liferayVersion;
	}

	public String getSampleName() {
		return _sampleName;
	}

	public boolean isCache() {
		return _cache;
	}

	public boolean isListCategories() {
		return _listCategories;
	}

	@Parameter(description = "To cache the archive for the specified liferay version", names = {"-c", "--cache"})
	private boolean _cache;

	@Parameter(description = "category", names = {"-ca", "--category"})
	private String _category;

	@Parameter(description = "The directory where to create the new project.", names = {"-d", "--dir"})
	private File _dir;

	@Parameter(
		description = "The version of Liferay to target when downloading the sample project. Available options are 7.0, 7.1, 7.2. (default 7.1).",
		names = {"-v", "--liferay-version"}
	)
	private String _liferayVersion;

	@Parameter(
		description = "To list all of categories for the specified liferay version",
		names = {"-lc", "--list-categories"}
	)
	private boolean _listCategories;

	@Parameter(description = "name")
	private String _sampleName;

}
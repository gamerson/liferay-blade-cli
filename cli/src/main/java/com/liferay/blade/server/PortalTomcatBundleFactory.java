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

package com.liferay.blade.server;

import com.liferay.blade.cli.util.FileUtil;

import java.nio.file.Path;

import java.util.Map;

/**
 * @author Simon Jiang
 */
public class PortalTomcatBundleFactory extends AbstractPortalBundleFactory {

	@Override
	public PortalBundle create(Map<String, String> appServerProperties) {
		return new PortalTomcatBundle(appServerProperties);
	}

	@Override
	public PortalBundle create(Path location) {
		return new PortalTomcatBundle(location);
	}

	@Override
	protected boolean detectBundleDir(Path path) {
		if (FileUtil.notExists(path)) {
			return false;
		}

		Path binPath = path.resolve("bin");
		Path confPath = path.resolve("conf");
		Path libPath = path.resolve("lib");
		Path webappPath = path.resolve("webapps");

		if (FileUtil.exists(binPath) && FileUtil.exists(confPath) && FileUtil.exists(libPath) &&
			FileUtil.exists(webappPath)) {

			return true;
		}

		return false;
	}

}
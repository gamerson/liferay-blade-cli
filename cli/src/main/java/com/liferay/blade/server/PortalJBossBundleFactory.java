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
import com.liferay.blade.cli.util.JavaUtil;

import java.nio.file.Path;

import java.util.Map;

/**
 * @author Simon Jiang
 */
public class PortalJBossBundleFactory extends AbstractPortalBundleFactory {

	@Override
	public PortalBundle create(Map<String, String> appServerProperties) {
		return new PortalJBossBundle(appServerProperties);
	}

	@Override
	public PortalBundle create(Path location) {
		return new PortalJBossBundle(location);
	}

	@Override
	protected boolean detectBundleDir(Path path) {
		if (FileUtil.notExists(path)) {
			return false;
		}

		Path bundlesPath = path.resolve("bundles");
		Path modulesPath = path.resolve("modules");
		Path standalonePath = path.resolve("standalone");
		Path binPath = path.resolve("bin");

		if (FileUtil.exists(bundlesPath) && FileUtil.exists(modulesPath) && FileUtil.exists(standalonePath) &&
			FileUtil.exists(binPath)) {

			String mainFolder = "modules/org/jboss/as/server/main";

			return JavaUtil.scanFolderJarsForManifestProp(path.toFile(), mainFolder, _JBAS7_RELEASE_VERSION, "7.");
		}

		return false;
	}

	private static final String _JBAS7_RELEASE_VERSION = "JBossAS-Release-Version";

}
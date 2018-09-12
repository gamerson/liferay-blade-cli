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

import java.io.File;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Map;

/**
 * @author Simon Jiang
 */
public abstract class AbstractPortalBundleFactory implements PortalBundleFactory {

	@Override
	public Path canCreateFromPath(Map<String, Object> appServerProperties) {
		Path retval = null;

		String appServerPath = (String)appServerProperties.get("app.server.dir");
		String appServerParentPath = (String)appServerProperties.get("app.server.parent.dir");
		String appServerDeployPath = (String)appServerProperties.get("app.server.deploy.dir");
		String appServerGlobalLibPath = (String)appServerProperties.get("app.server.lib.global.dir");
		String appServerPortalPath = (String)appServerProperties.get("app.server.portal.dir");

		if (!FileUtil.verifyPath(appServerPath) || !FileUtil.verifyPath(appServerParentPath) ||
			!FileUtil.verifyPath(appServerDeployPath) || !FileUtil.verifyPath(appServerPortalPath) ||
			!FileUtil.verifyPath(appServerGlobalLibPath)) {

			return retval;
		}

		Path appServerLocation = Paths.get(appServerPath);
		Path liferayHomelocation = Paths.get(appServerParentPath);

		if (detectBundleDir(appServerLocation)) {
			retval = appServerLocation;
		}

		Path liferayHome = _detectLiferayHome(liferayHomelocation, true);

		if (liferayHome != null) {
			File[] directories = FileUtil.getDirectories(liferayHome.toFile());

			for (File directory : directories) {
				Path dirPath = Paths.get(directory.getPath());

				if (detectBundleDir(dirPath)) {
					retval = dirPath;

					break;
				}
			}
		}

		return retval;
	}

	@Override
	public Path canCreateFromPath(Path location) {
		Path retval = null;

		Path liferayHome = _detectLiferayHome(location, true);

		if (liferayHome != null) {
			File[] directories = FileUtil.getDirectories(liferayHome.toFile());

			for (File directory : directories) {
				Path dirPath = Paths.get(directory.getPath());

				if (detectBundleDir(dirPath)) {
					retval = dirPath;

					break;
				}
			}
		}

		return retval;
	}

	@Override
	public String getType() {
		return _bundleFactoryType;
	}

	public void setBundleFactoryType(String type) {
		_bundleFactoryType = type;
	}

	protected abstract boolean detectBundleDir(Path path);

	private Path _detectLiferayHome(Path path, boolean checkParents) {
		if (FileUtil.notExists(path)) {
			return null;
		}

		Path osgiPath = Paths.get(path.toString(), "osgi");
		Path licensePath = Paths.get(path.toString(), "license");
		Path dataPath = Paths.get(path.toString(), "data");

		if (FileUtil.exists(osgiPath) && FileUtil.exists(licensePath) && FileUtil.exists(dataPath)) {
			return path;
		}
		else {
			if (checkParents && (path.getParent() != null)) {
				return _detectLiferayHome(path.getParent(), checkParents);
			}
		}

		return null;
	}

	private String _bundleFactoryType;

}
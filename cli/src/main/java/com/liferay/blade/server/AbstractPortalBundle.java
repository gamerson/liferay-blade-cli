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

import java.net.URL;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Map;

/**
 * @author Simon Jiang
 */
public abstract class AbstractPortalBundle implements PortalBundle {

	public AbstractPortalBundle(Map<String, String> appServerProperties) {
		if (appServerProperties == null) {
			throw new IllegalArgumentException("bundle parameters cannot be null");
		}

		String appServerPath = appServerProperties.get("app.server.dir");
		String appServerDeployPath = appServerProperties.get("app.server.deploy.dir");
		String appServerParentPath = appServerProperties.get("app.server.parent.dir");

		bundlePath = Paths.get(appServerPath);

		liferayHome = Paths.get(appServerParentPath);

		autoDeployPath = Paths.get(appServerDeployPath);

		modulesPath = null;
	}

	public AbstractPortalBundle(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("path cannot be null");
		}

		bundlePath = path;

		liferayHome = bundlePath.getParent();

		autoDeployPath = liferayHome.resolve("deploy");

		modulesPath = liferayHome.resolve("osgi");
	}

	@Override
	public Path getAppServerDir() {
		return bundlePath;
	}

	@Override
	public Path getBundleHome() {
		return liferayHome;
	}

	@Override
	public URL getLiferayHomeUrl() throws Exception {
		return new URL("http://localhost:8080");
	}

	protected Path autoDeployPath;
	protected Path bundlePath;
	protected Path liferayHome;
	protected Path modulesPath;

}
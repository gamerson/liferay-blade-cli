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

import java.nio.file.Path;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Map;

/**
 * @author Simon Jiang
 */
public class PortalJBossBundle extends AbstractPortalBundle {

	public PortalJBossBundle(Map<String, String> appServerProperties) {
		super(appServerProperties);
	}

	public PortalJBossBundle(Path path) {
		super(path);
	}

	@Override
	public Path getApperServerLog() {
		Path standalonePath = getAppServerDir().resolve("standalone");

		Path logPath = standalonePath.resolve("log");

		return logPath.resolve("server.log");
	}

	@Override
	public Path getAppServerPortalDir() {
		Path retval = null;

		if (bundlePath != null) {
			retval = bundlePath.resolve("/standalone/deployments/ROOT.war");
		}

		return retval;
	}

	@Override
	public Path getBundleLog() {
		Path liferayHome = getBundleHome();

		SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		String logDateString = logDateFormat.format(new Date());

		Path logsPath = liferayHome.resolve("logs");

		return logsPath.resolve("liferay." + logDateString + ".log");
	}

	@Override
	public String getType() {
		return "jboss";
	}

}
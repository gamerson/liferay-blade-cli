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

import com.liferay.blade.cli.util.BladeUtil;
import com.liferay.blade.cli.util.FileUtil;
import com.liferay.blade.cli.util.JavaUtil;

import java.io.File;

import java.nio.file.Path;

import java.util.Map;
import java.util.Properties;

/**
 * @author Simon Jiang
 */
public class PortalJBossEapBundleFactory extends PortalJBossBundleFactory {

	public static String getEAPVersionNoSlotCheck(
		File location, String metaInfPath, String[] versionPrefixs, String releaseName) {

		Path rootPath = location.toPath();

		Path eapDir = rootPath.resolve(metaInfPath);

		if (FileUtil.exists(eapDir)) {
			Path manifest = eapDir.resolve("MANIFEST.MF");

			String type = JavaUtil.getManifestProperty(manifest.toFile(), "JBoss-Product-Release-Name");
			String version = JavaUtil.getManifestProperty(manifest.toFile(), "JBoss-Product-Release-Version");

			boolean matchesName = type.contains(releaseName);

			for (String prefixVersion : versionPrefixs) {
				boolean matchesVersion = version.startsWith(prefixVersion);

				if (matchesName && matchesVersion) {
					return version;
				}
			}
		}

		return null;
	}

	@Override
	public PortalBundle create(Map<String, String> appServerProperties) {
		return new PortalJBossEapBundle(appServerProperties);
	}

	@Override
	public PortalBundle create(Path location) {
		return new PortalJBossEapBundle(location);
	}

	@Override
	protected boolean detectBundleDir(Path path) {
		if (FileUtil.notExists(path)) {
			return false;
		}

		Path modulesPath = path.resolve("modules");
		Path standalonePath = path.resolve("standalone");
		Path binPath = path.resolve("bin");

		if (FileUtil.exists(modulesPath) && FileUtil.exists(standalonePath) && FileUtil.exists(binPath)) {
			String eapVersion = _getEAPVersion(
				path.toFile(), _EAP_DIR_META_INF, new String[] {"6.", "7."}, "eap", "EAP");

			if (eapVersion != null) {
				return true;
			}
			else {
				return super.detectBundleDir(path);
			}
		}

		return false;
	}

	private String _getEAPVersion(
		File location, String metaInfPath, String[] versionPrefix, String slot, String releaseName) {

		Path rootPath = location.toPath();

		Path productConf = rootPath.resolve("bin/product.conf");

		if (FileUtil.exists(productConf)) {
			Properties p = BladeUtil.loadProperties(productConf.toFile());

			if (p != null) {
				String product = (String)p.get("slot");

				if (slot.equals(product)) {
					return getEAPVersionNoSlotCheck(location, metaInfPath, versionPrefix, releaseName);
				}
			}
		}

		return null;
	}

	private static final String _EAP_DIR_META_INF = "modules/system/layers/base/org/jboss/as/product/eap/dir/META-INF";

}
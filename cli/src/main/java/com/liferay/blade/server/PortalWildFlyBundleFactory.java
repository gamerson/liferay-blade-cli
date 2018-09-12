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
import com.liferay.blade.cli.util.ListUtil;

import java.io.File;
import java.io.FileFilter;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Simon Jiang
 */
public class PortalWildFlyBundleFactory extends PortalJBossBundleFactory {

	@Override
	public PortalBundle create(Map<String, String> appServerProperties) {
		return new PortalWildFlyBundle(appServerProperties);
	}

	@Override
	public PortalBundle create(Path location) {
		return new PortalWildFlyBundle(location);
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
			String vers = getManifestPropFromJBossModulesFolder(
				new File[] {new File(path.toString(), "modules")}, "org.jboss.as.product", "wildfly-full/dir/META-INF",
				_WF_RELEASE_MANIFEST_KEY);

			if ((vers != null) && (vers.startsWith("10.") || vers.startsWith("11."))) {
				return true;
			}
			else {
				return super.detectBundleDir(path);
			}
		}

		return false;
	}

	protected String getManifestPropFromJBossModulesFolder(
		File[] moduleRoots, String moduleId, String slot, String property) {

		File[] layeredRoots = LayeredModulePathFactory.resolveLayeredModulePath(moduleRoots);

		for (File root : layeredRoots) {
			Path[] manifests = _getFilesForModule(root, moduleId, slot, _manifestFilter());

			if (ListUtil.isNotEmpty(manifests)) {
				String value = JavaUtil.getManifestProperty(manifests[0].toFile(), property);

				if (value != null) {
					return value;
				}

				return null;
			}
		}

		return null;
	}

	private static Path[] _getFiles(File modulesFolder, Path moduleRelativePath, FileFilter filter) {
		File[] layeredPaths = LayeredModulePathFactory.resolveLayeredModulePath(modulesFolder);

		for (File layeredPathFile : layeredPaths) {
			Path lay = layeredPathFile.toPath();

			Path relativeLayPath = lay.resolve(moduleRelativePath);

			File layeredPath = relativeLayPath.toFile();

			if (FileUtil.exists(layeredPath)) {
				return _getFilesFrom(layeredPath, filter);
			}
		}

		return new Path[0];
	}

	private static Path[] _getFilesForModule(File modulesFolder, String moduleName, String slot, FileFilter filter) {
		String slashed = moduleName.replaceAll("\\.", "/");

		slot = slot == null ? "main" : slot;

		return _getFiles(modulesFolder, Paths.get(slashed, slot), filter);
	}

	private static Path[] _getFilesFrom(File layeredPath, FileFilter filter) {
		ArrayList<Path> list = new ArrayList<>();
		File[] children = layeredPath.listFiles();

		for (File child : children) {
			if (filter.accept(child)) {
				list.add(child.toPath());
			}
		}

		return list.toArray(new Path[list.size()]);
	}

	private static FileFilter _manifestFilter() {
		return new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				String pathName = pathname.getName();

				String pathNameLowerCase = pathName.toLowerCase();

				if (pathname.isFile() && pathNameLowerCase.equals("manifest.mf")) {
					return true;
				}

				return false;
			}

		};
	}

	private static final String _WF_RELEASE_MANIFEST_KEY = "JBoss-Product-Release-Version";

}
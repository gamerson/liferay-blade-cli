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

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.WorkspaceConstants;
import com.liferay.blade.cli.util.BladeUtil;
import com.liferay.blade.cli.util.ServerUtil;
import com.liferay.blade.cli.util.WorkspaceUtil;
import com.liferay.blade.server.PortalBundle;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * @author David Truong
 * @author Simon Jiang
 * @param <T>
 */
public abstract class AbstractServerCommand<T extends BaseArgs> extends BaseCommand<T> {

	public AbstractServerCommand() {
	}

	@Override
	public void execute() throws Exception {
		bladeCLI = getBladeCLI();

		BaseArgs baseArgs = bladeCLI.getBladeArgs();

		File baseDir = new File(baseArgs.getBase());

		File gradleWrapperFile = BladeUtil.getGradleWrapper(baseDir);

		if (gradleWrapperFile != null) {
			Path gradleWrapperPath = gradleWrapperFile.toPath();

			Path parent = gradleWrapperPath.getParent();

			File rootDir = parent.toFile();

			Path rootDirPath = rootDir.toPath();

			if (WorkspaceUtil.isWorkspace(rootDir)) {
				Properties properties = WorkspaceUtil.getGradleProperties(baseDir);

				String liferayHomePath = properties.getProperty(WorkspaceConstants.DEFAULT_LIFERAY_HOME_DIR_PROPERTY);

				if ((liferayHomePath == null) || liferayHomePath.equals("")) {
					liferayHomePath = WorkspaceConstants.DEFAULT_LIFERAY_HOME_DIR;
				}

				Path liferayHomeDir = null;
				Path tempLiferayHome = Paths.get(liferayHomePath);

				if (tempLiferayHome.isAbsolute()) {
					liferayHomeDir = tempLiferayHome.normalize();
				}
				else {
					Path tempFile = rootDirPath.resolve(liferayHomePath);

					liferayHomeDir = tempFile.normalize();
				}

				_commandServer(liferayHomeDir);
			}
			else {
				bladeCLI.error("Please execute this command from a Liferay project");
			}
		}
		else {
			List<Properties> propertiesList = BladeUtil.getAppServerProperties(baseDir);

			String appServerParentDir = "";

			for (Properties properties : propertiesList) {
				if (appServerParentDir.equals("")) {
					String appServerParentDirTemp = properties.getProperty(BladeUtil.APP_SERVER_PARENT_DIR_PROPERTY);

					if ((appServerParentDirTemp != null) && !appServerParentDirTemp.equals("")) {
						Path baseDirPath = baseDir.toPath();

						Path rootDirRealPath = baseDirPath.toRealPath();

						appServerParentDirTemp = appServerParentDirTemp.replace(
							"${project.dir}", rootDirRealPath.toString());

						appServerParentDir = appServerParentDirTemp;
					}
				}
			}

			if (appServerParentDir.startsWith("/") || appServerParentDir.contains(":")) {
				_commandServer(Paths.get(appServerParentDir));
			}
			else if (!BladeUtil.isDirEmpty(baseDir.toPath()) && BladeUtil.isEmpty(appServerParentDir)) {
				_commandServer(baseDir.toPath());
			}
			else {
				bladeCLI.error("Please set correct server path.");

				throw new Exception("Please set correct server path");
			}
		}
	}

	public Collection<Process> getProcesses() {
		return processes;
	}

	protected abstract void doServerCommand(PortalBundle portalBundle) throws Exception;

	protected Properties getProperties() {
		BladeCLI bladeCLI = getBladeCLI();

		BaseArgs baseArgs = bladeCLI.getBladeArgs();

		File baseDir = new File(baseArgs.getBase());

		return WorkspaceUtil.getGradleProperties(baseDir);
	}

	protected BladeCLI bladeCLI;
	protected Collection<Process> processes = new HashSet<>();

	private void _commandServer(Path dir) throws Exception {
		BladeCLI bladeCLI = getBladeCLI();

		if (Files.notExists(dir) || BladeUtil.isDirEmpty(dir)) {
			bladeCLI.error(
				" bundles folder does not exist in Liferay Workspace, execute 'gradlew initBundle' in order to " +
					"create it.");

			return;
		}

		PortalBundle portalBundle = ServerUtil.getPortalBundle(dir);

		if (portalBundle != null) {
			doServerCommand(portalBundle);
		}
		else {
			bladeCLI.error("Pleae confirm correct server location.");
		}
	}

}
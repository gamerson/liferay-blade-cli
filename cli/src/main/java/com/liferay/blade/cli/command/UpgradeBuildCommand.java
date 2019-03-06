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
import com.liferay.blade.cli.BladeSettings;
import com.liferay.blade.cli.WorkspaceConstants;
import com.liferay.blade.cli.WorkspaceProvider;
import com.liferay.blade.cli.gradle.GradleWorkspaceProvider;
import com.liferay.blade.cli.gradle.parser.DependencyUpgrader;
import com.liferay.blade.cli.gradle.parser.GradleDependency;
import com.liferay.blade.cli.util.BladeUtil;
import com.liferay.project.templates.ProjectTemplatesArgs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;

/**
 * @author Vernon Singleton
 */
public class UpgradeBuildCommand extends BaseCommand<UpgradeBuildArgs> {

	public UpgradeBuildCommand() {
	}

	@Override
	public void execute() throws Exception {
		BladeCLI bladeCLI = getBladeCLI();

		UpgradeBuildArgs upgradeBuildArgs = getArgs();

		boolean dryrun = upgradeBuildArgs.isDryrun();

		if (!dryrun) {
			bladeCLI.addErrors(
				"upgradeBuild", Collections.singleton("Only dryruns are supported so far ..."));

			return;
		}

		if (!Files.exists(Paths.get("build.gradle")) && !Files.exists(Paths.get("settings.gradle"))) {
			bladeCLI.addErrors(
					"upgradeBuild", Collections.singleton("Only gradle builds are supported so far ..."));

			return;
		}
//		else {
//			bladeCLI.out("user.dir = " + System.getProperty("user.dir"));
//		}

		// Gather the dependencies which would be used for
		// all templates and a workspace

		TreeMap<String, String> upgradeBuildscriptDependencies = new TreeMap<>();
		TreeMap<String, String> upgradeDependencies = new TreeMap<>();

		Path gradlePath = Files.createTempDirectory("gradle");

		List<Path> configs = _createConfigs(bladeCLI, gradlePath);

		for (Path config : configs) {
			File configFile = config.toFile();

			long configFileLength = configFile.length();

			if (configFileLength > 0) {
				try {
					DependencyUpgrader dependencyUpgrader = new DependencyUpgrader(configFile);

					List<GradleDependency> buildscriptDependenciesList = dependencyUpgrader.getBuildscriptDependencies();

					for (GradleDependency dependency : buildscriptDependenciesList) {
						upgradeBuildscriptDependencies.put(
							dependency.getGroup() + ":" + dependency.getName(),
							dependency.getVersion()
						);
					}

					List<GradleDependency> dependenciesList = dependencyUpgrader.getDependencies();

					for (GradleDependency dependency : dependenciesList) {
						upgradeDependencies.put(
							dependency.getGroup() + ":" + dependency.getName(),
							dependency.getVersion()
						);
					}

				} catch (MultipleCompilationErrorsException exception) {
					bladeCLI.out("WARNING: error processing " + configFile + ": " + exception.getMessage());
				}
			}
		}

		Iterator iterator = null;

//		iterator = upgradeBuildscriptDependencies.keySet().iterator();
//		while (iterator.hasNext()) {
//			String key = (String) iterator.next();
//			bladeCLI.out("UpgradeBuild: upgradeBuildscript dependency key = " + key);
//		}

//		bladeCLI.out("UpgradeBuild: upgradeDependencies.size() = " + upgradeDependencies.keySet().size());

//		iterator = upgradeDependencies.keySet().iterator();
//		while (iterator.hasNext()) {
//			String key = (String) iterator.next();
//			bladeCLI.out("UpgradeBuild: upgradeDependency key = " + key);
//		}

		// Gather the dependencies from the project in the current working directory

		TreeMap<String, String> buildscriptDependencies = new TreeMap<>();
		TreeMap<String, String> dependencies = new TreeMap<>();

		configs = new LinkedList<>();

		configs.add(Paths.get("build.gradle"));
		configs.add(Paths.get("settings.gradle"));

		for (Path config : configs) {
			File configFile = config.toFile();

			long configFileLength = configFile.length();

			if (configFileLength > 0) {
				DependencyUpgrader dependencyUpgrader = new DependencyUpgrader(configFile);

				List<GradleDependency> buildscriptDependenciesList = dependencyUpgrader.getBuildscriptDependencies();

				for (GradleDependency dependency : buildscriptDependenciesList) {
					buildscriptDependencies.put(
						dependency.getGroup() + ":" + dependency.getName(),
						dependency.getVersion()
					);
				}

				List<GradleDependency> dependenciesList = dependencyUpgrader.getDependencies();

				for (GradleDependency dependency : dependenciesList) {
					dependencies.put(
						dependency.getGroup() + ":" + dependency.getName(),
						dependency.getVersion()
					);
				}
			}
		}

		// TODO maybe figure out
		//  1. what project/product we are trying to upgrade to ...
		//       e.g. what version of Liferay is being used?
		//  2. what we are upgrading from

		String userDir = System.getProperty("user.dir");

		Path workingPath =  Paths.get(userDir);

		WorkspaceProvider workspaceProvider = bladeCLI.getWorkspaceProvider(workingPath.toFile());

		if (workspaceProvider == null) {
			bladeCLI.out("UpgradeBuild: isWorkspace = false");
		}
		else {
			bladeCLI.out("UpgradeBuild: isWorkspace = " + workspaceProvider.isWorkspace(bladeCLI));

			if (workspaceProvider instanceof GradleWorkspaceProvider) {
				GradleWorkspaceProvider gradleWorkspaceProvider = (GradleWorkspaceProvider)workspaceProvider;

				bladeCLI.out("UpgradeBuild: isDependencyManagementEnabled = " +
						gradleWorkspaceProvider.isDependencyManagementEnabled(workingPath.toFile()));

				Properties gradleProperties = gradleWorkspaceProvider.getGradleProperties(workingPath.toFile());

				String bundleUrlString = gradleProperties.getProperty(WorkspaceConstants.BUNDLE_URL);

				bladeCLI.out("UpgradeBuild: " + WorkspaceConstants.BUNDLE_URL + " = " + bundleUrlString);
			}

		}

//		BladeSettings bladeSettings = bladeCLI.getBladeSettings();
//
//		bladeCLI.out("UpgradeBuild: liferayVersionDefault = " + bladeSettings.getLiferayVersionDefault());

		// Now show the dry run or do the upgrade

		Set keySet = buildscriptDependencies.keySet();

		int buildscriptDependenciesSize = keySet.size();

		bladeCLI.out("UpgradeBuild: " + buildscriptDependenciesSize + " buildscript dependency(s) getting checked " +
				"for upgrades ...");

		keySet = buildscriptDependencies.keySet();

		iterator = keySet.iterator();

		while (iterator.hasNext()) {
			String key = (String)iterator.next();

			String upgradeVersion = upgradeBuildscriptDependencies.get(key);

			if (upgradeVersion != null) {
				String version = buildscriptDependencies.get(key);

				if (upgradeVersion.equals(version)) {
					bladeCLI.out("UpgradeBuild: no need to upgrade " + key);
				} else {
					bladeCLI.out("UpgradeBuild: buildscript dependency " +
							key + " would be upgraded from " + version + " to " + upgradeVersion);
				}
			}
		}

//		bladeCLI.out("UpgradeBuild: dependencies.size() = " + dependencies.keySet().size());

		// TODO build trust in the following commented code such that it takes into account
		// TODO workspace and more templates with workspace detection, if needed, etc ...

//		iterator = dependencies.keySet().iterator();
//		while (iterator.hasNext()) {
//			String key = (String) iterator.next();

//			String upgradeVersion = upgradeDependencies.get(key);
//			if (upgradeVersion != null) {
//				String version = dependencies.get(key);
//				if (upgradeVersion.equals(version)) {
//					bladeCLI.out("UpgradeBuild: no need to upgrade " + key);
//				} else {
//					bladeCLI.out("UpgradeBuild: " +
//							key + " could be updated from " + version + " to " + upgradeVersion);
//				}
//			}
//		}

//		FileUtil.deleteDir(gradlePath);

	}

	@Override
	public Class<UpgradeBuildArgs> getArgsClass() {
		return UpgradeBuildArgs.class;
	}

	private List<Path> _createConfigs(BladeCLI bladeCLI, Path gradlePath) throws Exception {

		Collection<String> templateNames = BladeUtil.getTemplateNames(bladeCLI);

		for (String templateName : templateNames) {

			// TODO figure out if this skipping is needed

			if ("fragment".equals(templateName)) {
//				bladeCLI.out("_createConfigs: skipping " + templateName + " ...");

				continue;
			}

			if ("modules-ext".equals(templateName)) {
//				bladeCLI.out("_createConfigs: skipping " + templateName + " ...");

				continue;
			}

			if ("service".equals(templateName)) {
//				bladeCLI.out("_createConfigs: skipping " + templateName + " ...");

				continue;
			}

			if ("service-wrapper".equals(templateName)) {
//				bladeCLI.out("_createConfigs: skipping " + templateName + " ...");

				continue;
			}
//			bladeCLI.out("_createConfigs: " + templateName);

			Path templatePath = gradlePath.resolve(templateName);

			File templateDirectory = templatePath.toFile();

			templateDirectory.mkdirs();

			CreateCommand createCommand = new CreateCommand(bladeCLI);

			ProjectTemplatesArgs projectTemplatesArgs = new ProjectTemplatesArgs();

			projectTemplatesArgs.setDestinationDir(templateDirectory);
			projectTemplatesArgs.setName("foo");

			// projectTemplatesArgs.setPackageName("packey");

			projectTemplatesArgs.setTemplate(templateName);

			createCommand.execute(projectTemplatesArgs);
		}

		Path templatePath = gradlePath.resolve("workspace");

		File templateDirectory = templatePath.toFile();

		templateDirectory.mkdirs();

//		System.setProperty("user.dir", templateDirectory.toString());

		BladeCLI bladey = new BladeCLI();

		InitCommand initCommand = new InitCommand();

		initCommand.setBlade(bladeCLI);

		BaseArgs baseArgs = new BaseArgs();

		baseArgs.setBase(templateDirectory);

		InitArgs initArgs = new InitArgs();

		initArgs.setName("workspace");

		initCommand.execute(bladey, baseArgs, initArgs, "workspace", templateDirectory, templateDirectory);

		List<Path> list = Files.find(gradlePath,
				Integer.MAX_VALUE,
				(filePath, fileAttr) -> filePath.toFile().getName().matches(".*.gradle"))
				.collect(Collectors.toList());

		return list;
	}

	private void _extractTemplateJars(Path jarsDirectory) throws IOException {
		Class<?> clazz = getClass();

		try (InputStream inputStream = clazz.getResourceAsStream("/project-template-jar-versions.properties")) {
			if (inputStream == null) {
				return;
			}

			Properties properties = new Properties();

			properties.load(inputStream);

			Set<Object> keySet = properties.keySet();

			ClassLoader classLoader = clazz.getClassLoader();

			for (Object key : keySet) {
				String extension = key.toString() + "-" + properties.getProperty(key.toString()) + ".jar";

				try (InputStream extensionInputStream = classLoader.getResourceAsStream(extension)) {
					Path extensionPath = jarsDirectory.resolve(extension);

					Files.copy(extensionInputStream, extensionPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

}
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

package com.liferay.blade.upgrade.liferay70.apichanges;

import com.liferay.blade.api.AutoMigrateException;
import com.liferay.blade.api.AutoMigrator;
import com.liferay.blade.api.FileMigrator;
import com.liferay.blade.api.Problem;
import com.liferay.blade.api.SearchResult;
import com.liferay.blade.api.XMLFile;
import com.liferay.blade.upgrade.liferay70.XMLFileMigrator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.osgi.service.component.annotations.Component;

@Component(
	property = {
		"file.extensions=xml",
		"problem.title=pom file in 6.2 maven project",
		"problem.summary=the pom configuration in 6.2 maven project is not supported in 7.x",
		"problem.tickets=",
		"problem.section=#pom-file-in-62-maven-project",
		"implName=MavenProjectPomMigrator",
		"auto.correct=maven-pom-file"
	},
	service = {
		AutoMigrator.class,
		FileMigrator.class
	}
)
public class MavenProjectPomMigrator extends XMLFileMigrator
	implements AutoMigrator {

	private static final String KEY = "maven-pom-file";
	private static final String LIFERAY_MAVEN_PLUGIN_ARTIFACT_ID = "liferay-maven-plugin";

	@Override
	protected List<SearchResult> searchFile(File file, XMLFile xmlFileChecker) {
		if ("pom.xml".equals(file.getName())) {
			List<SearchResult> results = new ArrayList<>();

			Collection<SearchResult> searchResults = new ArrayList<SearchResult>();

			searchResults.addAll(xmlFileChecker.findElement("artifactId", "portal-service"));
			searchResults.addAll(xmlFileChecker.findElement("artifactId", "util-java"));
			searchResults.addAll(xmlFileChecker.findElement("artifactId", "util-bridges"));
			searchResults.addAll(xmlFileChecker.findElement("artifactId", "util-taglib"));
			searchResults.addAll(xmlFileChecker.findElement("artifactId", "util-slf4j"));
			searchResults.addAll(xmlFileChecker.findElement("artifactId", LIFERAY_MAVEN_PLUGIN_ARTIFACT_ID));

			if (!searchResults.isEmpty()) {
				SearchResult result = new SearchResult(file, 0, 0, 1, 1, true);
				result.autoCorrectContext = KEY;
				results.add(result);

				return results;
			}

			return Collections.emptyList();

		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public int correctProblems(File file, List<Problem> problems)
			throws AutoMigrateException {
		try {
			updatePomFile(file);
		} catch (Exception e) {
			throw new AutoMigrateException("auto fix pom error", e);
		}
		return 1;
	}

	private void updatePomFile(File pomFile) throws Exception {

		MavenXpp3Reader mavenreader = new MavenXpp3Reader();
		FileReader reader = new FileReader(pomFile.getAbsolutePath());
		Model model = mavenreader.read(reader);

		String[][] dependenciesConvertMap = new String[5][];

		dependenciesConvertMap[0] = new String[] { "portal-service", "com.liferay.portal.kernel", "2.6.0" };
		dependenciesConvertMap[1] = new String[] { "util-java", "com.liferay.util.java", "2.2.2" };
		dependenciesConvertMap[2] = new String[] { "util-bridges", "com.liferay.util.bridges", "2.0.0" };
		dependenciesConvertMap[3] = new String[] { "util-taglib", "com.liferay.util.taglib", "2.0.0" };
		dependenciesConvertMap[4] = new String[] { "util-slf4j", "com.liferay.util.slf4j", "1.0.0" };

		// convert dependencies
		List<Dependency> existedDependencies = model.getDependencies();

		List<Dependency> newDependencies = new ArrayList<Dependency>();

		for (Dependency existedDependency : existedDependencies) {
			String artifaceId = existedDependency.getArtifactId();

			for (String[] str : dependenciesConvertMap) {
				if (artifaceId.equals(str[0])) {
					existedDependency.setArtifactId(str[1]);
					existedDependency.setVersion(str[2]);

					continue;
				}
			}
			newDependencies.add(existedDependency);
		}

		Dependency portalKernel = new Dependency();
		portalKernel.setGroupId("com.liferay.portal");
		portalKernel.setArtifactId("com.liferay.portal.kernel");
		portalKernel.setVersion("2.6.0");

		Dependency utilJava = new Dependency();
		utilJava.setGroupId("com.liferay.portal");
		utilJava.setArtifactId("com.liferay.util.java");
		utilJava.setVersion("2.2.2");

		Dependency bndAnnotation = new Dependency();
		bndAnnotation.setGroupId("biz.aQute.bnd");
		bndAnnotation.setArtifactId("biz.aQute.bnd.annotation");
		bndAnnotation.setVersion("3.2.0");

		String packaging = model.getPackaging();

		if (packaging.equals("war")) {
			addDependency(newDependencies, portalKernel);
			addDependency(newDependencies, utilJava);
			addDependency(newDependencies, bndAnnotation);
		} else if (packaging.equals("jar")) {
			addDependency(newDependencies, portalKernel);
			addDependency(newDependencies, bndAnnotation);
		}

		model.setDependencies(newDependencies);

		List<Plugin> newPlugins = new ArrayList<Plugin>();

		// remove liferay-maven-plugin build
		Build build = model.getBuild();

		if (build != null) {
			List<Plugin> plugins = build.getPlugins();

			for (Plugin plugin : plugins) {
				if (plugin.getArtifactId().equals(LIFERAY_MAVEN_PLUGIN_ARTIFACT_ID)) {
					continue;
				}

				newPlugins.add(plugin);
			}

			if (packaging.equals("war")) {
				Plugin sbPlugin = new Plugin();
				sbPlugin.setGroupId("com.liferay");
				sbPlugin.setArtifactId("com.liferay.portal.tools.service.builder");
				sbPlugin.setVersion("1.0.142");

				Xpp3Dom sbConfiguration = new Xpp3Dom("configuration");

				addConfiguration("apiDirName", "../" + model.getArtifactId() + "-service/src/main/java",
						sbConfiguration);
				addConfiguration("autoNamespaceTables", "true", sbConfiguration);
				addConfiguration("buildNumberIncrement", "true", sbConfiguration);
				addConfiguration("hbmFileName", "src/main/resources/META-INF/portlet-hbm.xml", sbConfiguration);
				addConfiguration("implDirName", "src/main/java", sbConfiguration);
				addConfiguration("inputFileName", "src/main/webapp/WEB-INF/service.xml", sbConfiguration);
				addConfiguration("modelHintsFileName", "src/main/resources/META-INF/portlet-model-hints.xml",
						sbConfiguration);
				addConfiguration("osgiModule", "false", sbConfiguration);
				addConfiguration("pluginName", model.getArtifactId(), sbConfiguration);
				addConfiguration("propsUtil", "com.liferay.util.service.ServiceProps", sbConfiguration);
				addConfiguration("resourcesDirName", "src/main/resources", sbConfiguration);
				addConfiguration("springNamespaces", "beans", sbConfiguration);
				addConfiguration("springFileName", "src/main/resources/META-INF/portlet-spring.xml", sbConfiguration);
				addConfiguration("sqlDirName", "src/main/webapp/WEB-INF/sql", sbConfiguration);
				addConfiguration("sqlFileName", "tables.sql", sbConfiguration);

				sbPlugin.setConfiguration(sbConfiguration);

				newPlugins.add(sbPlugin);

				Plugin buildCssPlugin = new Plugin();
				buildCssPlugin.setGroupId("com.liferay");
				buildCssPlugin.setArtifactId("com.liferay.css.builder");
				buildCssPlugin.setVersion("1.0.21");

				List<PluginExecution> executions = new ArrayList<PluginExecution>();

				PluginExecution buildCssExecution = new PluginExecution();
				buildCssExecution.setId("default-build-css");
				buildCssExecution.setPhase("generate-sources");

				List<String> goals = new ArrayList<String>();
				goals.add("build-css");

				buildCssExecution.setGoals(goals);
				executions.add(buildCssExecution);
				buildCssPlugin.setExecutions(executions);

				Xpp3Dom buildCssConfiguration = new Xpp3Dom("configuration");
				addConfiguration("docrootDirName", "src/main/webapp", buildCssConfiguration);
				buildCssPlugin.setConfiguration(buildCssConfiguration);

				newPlugins.add(buildCssPlugin);
			}

			build.setPlugins(newPlugins);
		}

		try (FileOutputStream out = new FileOutputStream(pomFile)) {
			MavenXpp3Writer writer = new MavenXpp3Writer();

			writer.write(new FileOutputStream(pomFile), model);
		} catch (Exception e) {
			throw e;
		}
	}

	private void addConfiguration(String name, String value, Xpp3Dom configuration) {
		Xpp3Dom conf = new Xpp3Dom(name);
		conf.setValue(value);
		configuration.addChild(conf);
	}

	private void addDependency(List<Dependency> dependencyList, Dependency addDependency) {
		String addGroupId = addDependency.getGroupId();
		String addArtifactId = addDependency.getArtifactId();
		String addVersion = addDependency.getVersion();

		boolean existed = false;

		for (Dependency dependency : dependencyList) {
			String groupId = dependency.getGroupId();
			String artifactId = dependency.getArtifactId();
			String version = dependency.getVersion();

			if (addGroupId.equals(groupId) && addArtifactId.equals(artifactId)
					&& addVersion.equals(version)) {
				existed = true;
				break;
			}
		}

		if (!existed) {
			dependencyList.add(addDependency);
		}
	}
}

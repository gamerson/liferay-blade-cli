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

package com.liferay.blade.cli.command.create;

import static com.liferay.blade.cli.TestUtil.checkFileDoesNotExists;
import static com.liferay.blade.cli.TestUtil.checkFileExists;
import static com.liferay.blade.cli.TestUtil.checkGradleBuildFiles;
import static com.liferay.blade.cli.TestUtil.checkMavenBuildFiles;
import static com.liferay.blade.cli.TestUtil.contains;
import static com.liferay.blade.cli.TestUtil.lacks;
import static com.liferay.blade.cli.TestUtil.makeWorkspace;
import static com.liferay.blade.cli.TestUtil.testCreateWar;
import static com.liferay.blade.cli.TestUtil.verifyImportPackage;

import com.googlecode.junittoolbox.ParallelRunner;

import com.liferay.blade.cli.GradleRunnerUtil;
import com.liferay.blade.cli.MavenRunnerUtil;
import com.liferay.blade.cli.TestUtil;
import com.liferay.blade.cli.util.FileUtil;
import com.liferay.project.templates.ProjectTemplates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import org.gradle.testkit.runner.BuildTask;
import org.gradle.tooling.internal.consumer.ConnectorServices;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
@RunWith(ParallelRunner.class)
public class CreateCommandTest {

	@AfterClass
	public static void cleanUp() throws Exception {
		ConnectorServices.reset();
	}

	@Before
	public void setUp() throws Exception {
		File tempRootFile = temporaryFolder.getRoot();

		tempRoot = tempRootFile.toPath().toAbsolutePath();
	}

	@Test
	public void testCreateActivator() throws Exception {
		String[] gradleArgs = {"create", "-d", tempRoot.toString(), "-t", "activator", "bar-activator"};

		String[] mavenArgs = {"create", "-d", tempRoot.toString(), "-b", "maven", "-t", "activator", "bar-activator"};

		Path projectPath = tempRoot.resolve("bar-activator");

		TestUtil.runBlade(temporaryFolder.getRoot(), gradleArgs);

		checkGradleBuildFiles(projectPath);

		Path barActivator = Paths.get("src", "main", "java", "bar", "activator", "BarActivator.java");

		contains(
			checkFileExists(projectPath.resolve(barActivator)),
			".*^public class BarActivator implements BundleActivator.*$");

		TestUtil.verifyBuild(projectPath, "bar.activator-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "bar.activator-1.0.0.jar")));

		FileUtils.deleteDirectory(projectPath.toFile());

		TestUtil.runBlade(temporaryFolder.getRoot(), mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve(barActivator)),
			".*^public class BarActivator implements BundleActivator.*$");

		MavenRunnerUtil.executeGoals(projectPath, new String[] {"clean", "package"});

		MavenRunnerUtil.verifyBuildOutput(projectPath, "bar-activator-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("target", "bar-activator-1.0.0.jar")));
	}

	@Test
	public void testCreateApi() throws Exception {
		String[] gradleArgs = {"create", "-d", tempRoot.toString(), "-t", "api", "foo"};

		String[] mavenArgs = {"create", "-d", tempRoot.toString(), "-b", "maven", "-t", "api", "foo"};

		Path projectPath = tempRoot.resolve("foo");

		TestUtil.runBlade(temporaryFolder.getRoot(), gradleArgs);

		checkGradleBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve(Paths.get("src", "main", "java", "foo", "api", "Foo.java"))),
			".*^public interface Foo.*");

		contains(
			checkFileExists(projectPath.resolve(Paths.get("src", "main", "resources", "foo", "api", "packageinfo"))),
			"version 1.0.0");

		TestUtil.verifyBuild(projectPath, "foo-1.0.0.jar");

		Path jarPath = projectPath.resolve(Paths.get("build", "libs", "foo-1.0.0.jar"));

		try (JarFile jar = new JarFile(jarPath.toFile())) {
			Manifest manifest = jar.getManifest();

			Attributes mainAttributes = manifest.getMainAttributes();

			Assert.assertEquals("foo.api;version=\"1.0.0\"", mainAttributes.getValue("Export-Package"));
		}

		FileUtil.deleteDir(projectPath);

		jarPath = projectPath.resolve(Paths.get("target", "foo-1.0.0.jar"));

		TestUtil.runBlade(temporaryFolder.getRoot(), mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve(Paths.get("src", "main", "java", "foo", "api", "Foo.java"))),
			".*^public interface Foo.*");

		contains(
			checkFileExists(projectPath.resolve(Paths.get("src", "main", "resources", "foo", "api", "packageinfo"))),
			"version 1.0.0");

		MavenRunnerUtil.executeGoals(projectPath, new String[] {"clean", "package"});

		MavenRunnerUtil.verifyBuildOutput(projectPath, "foo-1.0.0.jar");

		verifyImportPackage(jarPath);

		try (JarFile jar = new JarFile(jarPath.toFile())) {
			Manifest manifest = jar.getManifest();

			Attributes mainAttributes = manifest.getMainAttributes();

			Assert.assertEquals("foo.api;version=\"1.0.0\"", mainAttributes.getValue("Export-Package"));
		}
	}

	@Test
	public void testCreateFragment() throws Exception {
		String[] gradleArgs = {
			"create", "-d", tempRoot.toString(), "-t", "fragment", "-h", "com.liferay.login.web", "-H", "1.0.0",
			"loginHook"
		};

		String[] mavenArgs = {
			"create", "-d", tempRoot.toString(), "-b", "maven", "-t", "fragment", "-h", "com.liferay.login.web", "-H",
			"1.0.0", "loginHook"
		};

		Path projectPath = tempRoot.resolve("loginHook");

		TestUtil.runBlade(temporaryFolder.getRoot(), gradleArgs);

		checkGradleBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve("bnd.bnd")),
			new String[] {
				".*^Bundle-SymbolicName: loginhook.*$",
				".*^Fragment-Host: com.liferay.login.web;bundle-version=\"1.0.0\".*$"
			});

		contains(checkFileExists(projectPath.resolve("build.gradle")), ".*^apply plugin: \"com.liferay.plugin\".*");

		TestUtil.verifyBuild(projectPath, "loginhook-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "loginhook-1.0.0.jar")));

		FileUtil.deleteDir(projectPath);

		TestUtil.runBlade(temporaryFolder.getRoot(), mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve("bnd.bnd")),
			new String[] {
				".*^Bundle-SymbolicName: loginhook.*$",
				".*^Fragment-Host: com.liferay.login.web;bundle-version=\"1.0.0\".*$"
			});

		MavenRunnerUtil.executeGoals(projectPath, new String[] {"clean", "package"});

		MavenRunnerUtil.verifyBuildOutput(projectPath, "loginHook-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("target", "loginHook-1.0.0.jar")));
	}

	@Test
	public void testCreateFragmentWithoutHostOptions() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "fragment", "loginHook"};

		String content = TestUtil.runBlade(tempRoot, args);

		Assert.assertTrue(content, content.contains("\"-t fragment\" options missing"));

		args = new String[]
			{"create", "-d", tempRoot.toString(), "-t", "fragment", "-h", "com.liferay.login.web", "loginHook"};

		content = TestUtil.runBlade(tempRoot, args);

		Assert.assertTrue(content, content.contains("\"-t fragment\" options missing"));

		args = new String[] {"create", "-d", tempRoot.toString(), "-t", "fragment", "-H", "1.0.0", "loginHook"};

		content = TestUtil.runBlade(tempRoot, args);

		Assert.assertTrue(content, content.contains("\"-t fragment\" options missing"));
	}

	@Test
	public void testCreateGradleMVCPortletProjectWithPackage() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "mvc-portlet", "-p", "com.liferay.test", "foo"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Path projectPath = tempRoot.resolve("foo");

		checkFileExists(projectPath);

		checkFileExists(projectPath.resolve("bnd.bnd"));

		contains(
			checkFileExists(
				projectPath.resolve(
					Paths.get("src", "main", "java", "com", "liferay", "test", "portlet", "FooPortlet.java"))),
			".*^public class FooPortlet extends MVCPortlet.*$");

		contains(checkFileExists(projectPath.resolve("build.gradle")), ".*^apply plugin: \"com.liferay.plugin\".*");

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "view.jsp")));

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "init.jsp")));

		TestUtil.verifyBuild(projectPath, "com.liferay.test-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "com.liferay.test-1.0.0.jar")));
	}

	@Test
	public void testCreateGradleMVCPortletProjectWithPortletSuffix() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "mvc-portlet", "portlet-portlet"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Path projectPath = tempRoot.resolve("portlet-portlet");

		checkFileExists(projectPath);

		checkFileExists(projectPath.resolve("bnd.bnd"));

		contains(
			checkFileExists(
				projectPath.resolve(
					Paths.get("src", "main", "java", "portlet", "portlet", "portlet", "PortletPortlet.java"))),
			".*^public class PortletPortlet extends MVCPortlet.*$");

		contains(checkFileExists(projectPath.resolve("build.gradle")), ".*^apply plugin: \"com.liferay.plugin\".*");

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "view.jsp")));

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "init.jsp")));

		TestUtil.verifyBuild(projectPath, "portlet.portlet-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "portlet.portlet-1.0.0.jar")));
	}

	@Test
	public void testCreateGradlePortletProject() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "portlet", "-c", "Foo", "gradle.test"};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Path projectPath = tempRoot.resolve("gradle.test");

		checkFileExists(projectPath);

		checkFileExists(projectPath.resolve("build.gradle"));

		contains(
			checkFileExists(
				projectPath.resolve(Paths.get("src", "main", "java", "gradle", "test", "portlet", "FooPortlet.java"))),
			new String[] {
				"^package gradle.test.portlet;.*", ".*javax.portlet.display-name=Foo.*",
				".*^public class FooPortlet .*", ".*Hello from Foo!.*"
			});

		TestUtil.verifyBuild(projectPath, "gradle.test-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "gradle.test-1.0.0.jar")));
	}

	@Test
	public void testCreateGradleService() throws Exception {
		String[] args = {
			"create", "-d", tempRoot.toString(), "-t", "service", "-s",
			"com.liferay.portal.kernel.events.LifecycleAction", "-c", "FooAction", "servicepreaction"
		};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("servicepreaction");

		checkFileExists(projectPath.resolve("build.gradle"));

		Path path = projectPath.resolve(Paths.get("src", "main", "java", "servicepreaction", "FooAction.java"));

		contains(
			checkFileExists(path),
			new String[] {
				"^package servicepreaction;.*", ".*^import com.liferay.portal.kernel.events.LifecycleAction;$.*",
				".*service = LifecycleAction.class.*", ".*^public class FooAction implements LifecycleAction \\{.*"
			});

		List<String> lines = new ArrayList<>();
		String line = null;

		try (InputStream in = Files.newInputStream(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

			while ((line = reader.readLine()) != null) {
				lines.add(line);

				if (line.equals("import com.liferay.portal.kernel.events.LifecycleAction;")) {
					lines.add("import com.liferay.portal.kernel.events.LifecycleEvent;");
					lines.add("import com.liferay.portal.kernel.events.ActionException;");
				}

				if (line.equals("public class FooAction implements LifecycleAction {")) {
					StringBuilder sb = new StringBuilder();

					sb.append("@Override\n");
					sb.append(
						"public void processLifecycleEvent(LifecycleEvent lifecycleEvent)" + System.lineSeparator());
					sb.append("throws ActionException {\n");
					sb.append("System.out.println(\"login.event.pre=\" + lifecycleEvent);" + System.lineSeparator());
					sb.append("}" + System.lineSeparator());

					lines.add(sb.toString());
				}
			}
		}

		String output = String.join(System.lineSeparator(), lines);

		Files.write(path, output.getBytes());

		TestUtil.verifyBuild(projectPath, "servicepreaction-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "servicepreaction-1.0.0.jar")));
	}

	@Test
	public void testCreateGradleServiceBuilderDashes() throws Exception {
		String[] args = {
			"create", "-d", tempRoot.toString(), "-t", "service-builder", "-p", "com.liferay.backend.integration",
			"backend-integration"
		};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Path projectPath = tempRoot.resolve("backend-integration");

		contains(
			checkFileExists(projectPath.resolve("settings.gradle")),
			"include \"backend-integration-api\", \"backend-integration-service\"");

		contains(
			checkFileExists(projectPath.resolve(Paths.get("backend-integration-api", "bnd.bnd"))),
			new String[] {
				".*Export-Package:\\\\.*", ".*com.liferay.backend.integration.exception,\\\\.*",
				".*com.liferay.backend.integration.model,\\\\.*", ".*com.liferay.backend.integration.service,\\\\.*",
				".*com.liferay.backend.integration.service.persistence.*"
			});

		contains(
			checkFileExists(projectPath.resolve(Paths.get("backend-integration-service", "bnd.bnd"))),
			".*Liferay-Service: true.*");

		BuildTask buildServiceTask = GradleRunnerUtil.executeGradleRunner(projectPath.toString(), "buildService");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildServiceTask);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(projectPath.toString(), "build");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(
			projectPath.resolve("backend-integration-api").toString(), "com.liferay.backend.integration.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(
			projectPath.resolve("backend-integration-service").toString(),
			"com.liferay.backend.integration.service-1.0.0.jar");

		verifyImportPackage(
			projectPath.resolve(
				Paths.get(
					"backend-integration-service", "build", "libs",
					"com.liferay.backend.integration.service-1.0.0.jar")));
	}

	@Test
	public void testCreateGradleServiceBuilderDefault() throws Exception {
		String[] args = {
			"create", "-d", tempRoot.toString(), "-t", "service-builder", "-p", "com.liferay.docs.guestbook",
			"guestbook"
		};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Path projectPath = tempRoot.resolve("guestbook");

		contains(
			checkFileExists(projectPath.resolve("settings.gradle")),
			"include \"guestbook-api\", \"guestbook-service\"");

		contains(
			checkFileExists(projectPath.resolve(Paths.get("guestbook-api", "bnd.bnd"))),
			new String[] {
				".*Export-Package:\\\\.*", ".*com.liferay.docs.guestbook.exception,\\\\.*",
				".*com.liferay.docs.guestbook.model,\\\\.*", ".*com.liferay.docs.guestbook.service,\\\\.*",
				".*com.liferay.docs.guestbook.service.persistence.*"
			});

		contains(
			checkFileExists(projectPath.resolve(Paths.get("guestbook-service", "bnd.bnd"))),
			".*Liferay-Service: true.*");

		File file = checkFileExists(projectPath.resolve(Paths.get("guestbook-service", "build.gradle")));

		contains(file, ".*compileOnly project\\(\":guestbook-api\"\\).*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(projectPath.toString(), "buildService");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(projectPath.toString(), "build");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(
			projectPath.resolve("guestbook-api"), "com.liferay.docs.guestbook.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(
			projectPath.resolve("guestbook-service"), "com.liferay.docs.guestbook.service-1.0.0.jar");

		Path serviceJar = projectPath.resolve(
			Paths.get("guestbook-service", "build", "libs", "com.liferay.docs.guestbook.service-1.0.0.jar"));

		verifyImportPackage(serviceJar);

		try (JarFile serviceJarFile = new JarFile(serviceJar.toFile())) {
			Manifest manifest = serviceJarFile.getManifest();

			Attributes mainAttributes = manifest.getMainAttributes();

			String springContext = mainAttributes.getValue("Liferay-Spring-Context");

			Assert.assertTrue(springContext.equals("META-INF/spring"));
		}
	}

	@Test
	public void testCreateGradleServiceBuilderDots() throws Exception {
		String[] args = {
			"create", "-d", tempRoot.toString(), "-t", "service-builder", "-p", "com.liferay.docs.guestbook",
			"com.liferay.docs.guestbook"
		};

		TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Path projectPath = tempRoot.resolve("com.liferay.docs.guestbook");

		contains(
			checkFileExists(projectPath.resolve("settings.gradle")),
			"include \"com.liferay.docs.guestbook-api\", \"com.liferay.docs.guestbook-service\"");

		contains(
			checkFileExists(projectPath.resolve(Paths.get("com.liferay.docs.guestbook-api", "bnd.bnd"))),
			new String[] {
				".*Export-Package:\\\\.*", ".*com.liferay.docs.guestbook.exception,\\\\.*",
				".*com.liferay.docs.guestbook.model,\\\\.*", ".*com.liferay.docs.guestbook.service,\\\\.*",
				".*com.liferay.docs.guestbook.service.persistence.*"
			});

		contains(
			checkFileExists(projectPath.resolve(Paths.get("com.liferay.docs.guestbook-service", "bnd.bnd"))),
			".*Liferay-Service: true.*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(projectPath.toString(), "buildService");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(projectPath.toString(), "build");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(
			projectPath.resolve("com.liferay.docs.guestbook-api"), "com.liferay.docs.guestbook.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(
			projectPath.resolve("com.liferay.docs.guestbook-service"), "com.liferay.docs.guestbook.service-1.0.0.jar");

		verifyImportPackage(
			projectPath.resolve(
				Paths.get(
					"com.liferay.docs.guestbook-service", "build", "libs",
					"com.liferay.docs.guestbook.service-1.0.0.jar")));
	}

	@Test
	public void testCreateGradleServiceWrapper() throws Exception {
		String[] args = {
			"create", "-d", tempRoot.toString(), "-t", "service-wrapper", "-s",
			"com.liferay.portal.kernel.service.UserLocalServiceWrapper", "serviceoverride"
		};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("serviceoverride");

		checkFileExists(projectPath.resolve("build.gradle"));

		contains(
			checkFileExists(
				projectPath.resolve(Paths.get("src", "main", "java", "serviceoverride", "Serviceoverride.java"))),
			new String[] {
				"^package serviceoverride;.*",
				".*^import com.liferay.portal.kernel.service.UserLocalServiceWrapper;$.*",
				".*service = ServiceWrapper.class.*",
				".*^public class Serviceoverride extends UserLocalServiceWrapper \\{.*",
				".*public Serviceoverride\\(\\) \\{.*"
			});

		TestUtil.verifyBuild(projectPath, "serviceoverride-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "serviceoverride-1.0.0.jar")));
	}

	@Test
	public void testCreateGradleSymbolicName() throws Exception {
		String[] args = {"create", "-t", "mvc-portlet", "-d", tempRoot.toString(), "-p", "foo.bar", "barfoo"};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("barfoo");

		checkFileExists(projectPath.resolve("build.gradle"));

		contains(checkFileExists(projectPath.resolve("bnd.bnd")), ".*Bundle-SymbolicName: foo.bar.*");

		TestUtil.verifyBuild(projectPath, "foo.bar-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "foo.bar-1.0.0.jar")));
	}

	@Test
	public void testCreateMissingArgument() throws Exception {
		String[] args = {"create", "foobar", "-d", tempRoot.toString()};

		String content = null;

		try {
			content = TestUtil.runBlade(temporaryFolder.getRoot(), args);
		}
		catch (Throwable t) {
			content = t.getMessage();
		}

		Assert.assertNotNull(content);

		boolean containsError = content.contains("The following option is required");

		Assert.assertTrue(containsError);
	}

	@Test
	public void testCreateMVCPortlet() throws Exception {
		String[] gradleArgs = {"create", "-d", tempRoot.toString(), "-t", "mvc-portlet", "foo"};

		String[] mavenArgs = {"create", "-d", tempRoot.toString(), "-b", "maven", "-t", "mvc-portlet", "foo"};

		Path projectPath = tempRoot.resolve("foo");

		TestUtil.runBlade(tempRoot, gradleArgs);

		checkGradleBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve(Paths.get("src", "main", "java", "foo", "portlet", "FooPortlet.java"))),
			".*^public class FooPortlet extends MVCPortlet.*$");

		contains(checkFileExists(projectPath.resolve("build.gradle")), ".*^apply plugin: \"com.liferay.plugin\".*");

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "view.jsp")));

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "init.jsp")));

		TestUtil.verifyBuild(projectPath, "foo-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "foo-1.0.0.jar")));

		FileUtil.deleteDir(projectPath);

		TestUtil.runBlade(tempRoot, mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath.resolve(Paths.get("src", "main", "java", "foo", "portlet", "FooPortlet.java"))),
			".*^public class FooPortlet extends MVCPortlet.*$");

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "view.jsp")));

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "init.jsp")));

		MavenRunnerUtil.executeGoals(projectPath, new String[] {"clean", "package"});

		MavenRunnerUtil.verifyBuildOutput(projectPath, "foo-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("target", "foo-1.0.0.jar")));
	}

	@Test
	public void testCreateNpmAngular() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "npm-angular-portlet", "npmangular"};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("npmangular");

		checkFileExists(projectPath.resolve("build.gradle"));

		File jsp = checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "view.jsp")));

		contains(jsp, ".*<aui:script require=\"npmangular@1.0.0\">.*");

		contains(jsp, ".*npmangular100.default.*");
	}

	@Test
	public void testCreateOnExistFolder() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "activator", "exist"};

		Path existFile = tempRoot.resolve(Paths.get("exist", "file.txt"));

		if (!Files.exists(existFile)) {
			Files.createDirectories(existFile.getParent());

			Files.createFile(existFile);
		}

		boolean encounteredError = false;

		try {
			TestUtil.runBlade(tempRoot, args);
		}
		catch (Throwable th) {
			encounteredError = true;
		}

		Assert.assertTrue(encounteredError);

		Path projectPath = tempRoot.resolve("exist");

		checkFileDoesNotExists(projectPath.resolve("bnd.bnd"));
	}

	@Test
	public void testCreatePortletConfigurationIcon() throws Exception {
		String[] args =
			{"create", "-d", tempRoot.toString(), "-t", "portlet-configuration-icon", "-p", "blade.test", "icontest"};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("icontest");

		checkFileExists(projectPath);

		checkFileExists(projectPath.resolve("bnd.bnd"));

		File componentFile = checkFileExists(
			projectPath.resolve(
				Paths.get(
					"src", "main", "java", "blade", "test", "portlet", "configuration", "icon",
					"IcontestPortletConfigurationIcon.java")));

		contains(
			componentFile, ".*^public class IcontestPortletConfigurationIcon.*extends BasePortletConfigurationIcon.*$");

		File gradleBuildFile = checkFileExists(projectPath.resolve("build.gradle"));

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		TestUtil.verifyBuild(projectPath, "blade.test-1.0.0.jar");
	}

	@Test
	public void testCreatePortletToolbarContributor() throws Exception {
		String[] args = {
			"create", "-d", tempRoot.toString(), "-t", "portlet-toolbar-contributor", "-p", "blade.test", "toolbartest"
		};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("toolbartest");

		checkFileExists(projectPath);

		checkFileExists(projectPath.resolve("bnd.bnd"));

		File componentFile = checkFileExists(
			projectPath.resolve(
				Paths.get(
					"src", "main", "java", "blade", "test", "portlet", "toolbar", "contributor",
					"ToolbartestPortletToolbarContributor.java")));

		contains(
			componentFile,
			".*^public class ToolbartestPortletToolbarContributor.*implements PortletToolbarContributor.*$");

		File gradleBuildFile = checkFileExists(projectPath.resolve("build.gradle"));

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		TestUtil.verifyBuild(projectPath, "blade.test-1.0.0.jar");
	}

	@Test
	public void testCreateProjectAllDefaults() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "mvc-portlet", "hello-world-portlet"};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("hello-world-portlet");

		checkFileExists(projectPath);

		checkFileExists(projectPath.resolve("bnd.bnd"));

		File portletFile = checkFileExists(
			projectPath.resolve(
				Paths.get("src", "main", "java", "hello", "world", "portlet", "portlet", "HelloWorldPortlet.java")));

		contains(portletFile, ".*^public class HelloWorldPortlet extends MVCPortlet.*$");

		File gradleBuildFile = checkFileExists(projectPath.resolve("build.gradle"));

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "view.jsp")));

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "init.jsp")));

		TestUtil.verifyBuild(projectPath, "hello.world.portlet-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "hello.world.portlet-1.0.0.jar")));
	}

	@Test
	public void testCreateProjectWithRefresh() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "mvc-portlet", "hello-world-refresh"};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("hello-world-refresh");

		checkFileExists(projectPath);

		checkFileExists(projectPath.resolve("bnd.bnd"));

		File portletFile = checkFileExists(
			projectPath.resolve(
				Paths.get(
					"src", "main", "java", "hello", "world", "refresh", "portlet", "HelloWorldRefreshPortlet.java")));

		contains(portletFile, ".*^public class HelloWorldRefreshPortlet extends MVCPortlet.*$");

		File gradleBuildFile = checkFileExists(projectPath.resolve("build.gradle"));

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "view.jsp")));

		checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "resources", "META-INF", "resources", "init.jsp")));

		TestUtil.verifyBuild(projectPath, "hello.world.refresh-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "hello.world.refresh-1.0.0.jar")));
	}

	@Test
	public void testCreateServiceTemplateServiceParameterRequired() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "service", "foo"};

		String output = null;

		try {
			output = TestUtil.runBlade(temporaryFolder.getRoot(), args);
		}
		catch (Throwable t) {
			output = t.getMessage();
		}

		Assert.assertNotNull(output);

		Assert.assertTrue(output, output.contains("Usage:"));

		args = new String[] {"create", "-t", "service", "-s com.test.Foo", "foo"};

		try {
			output = TestUtil.runBlade(temporaryFolder.getRoot(), args);
		}
		catch (Throwable t) {
			output = t.getMessage();
		}

		Assert.assertFalse(output, output.contains("Usage:"));
	}

	@Test
	public void testCreateSimulationPanelEntry() throws Exception {
		String[] args =
			{"create", "-d", tempRoot.toString(), "-t", "simulation-panel-entry", "-p", "test.simulator", "simulator"};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("simulator");

		checkFileExists(projectPath);

		checkFileExists(projectPath.resolve("bnd.bnd"));

		File componentFile = checkFileExists(
			projectPath.resolve(
				Paths.get(
					"src", "main", "java", "test", "simulator", "application", "list",
					"SimulatorSimulationPanelApp.java")));

		contains(componentFile, ".*^public class SimulatorSimulationPanelApp.*extends BaseJSPPanelApp.*$");

		File gradleBuildFile = checkFileExists(projectPath.resolve("build.gradle"));

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		TestUtil.verifyBuild(projectPath, "test.simulator-1.0.0.jar");
	}

	@Test
	public void testCreateSpringMvcPortlet() throws Exception {
		String[] args = {
			"create", "-d", tempRoot.toString(), "-t", "spring-mvc-portlet", "-p", "test.spring.portlet", "spring-test"
		};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("spring-test");

		checkFileExists(projectPath);

		checkFileExists(
			projectPath.resolve(
				Paths.get(
					"src", "main", "java", "test", "spring", "portlet", "portlet",
					"SpringTestPortletViewController.java")));

		checkFileExists(projectPath.resolve("build.gradle"));

		TestUtil.verifyBuild(projectPath, "spring-test.war");
	}

	@Test
	public void testCreateTemplateContextContributor() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "template-context-contributor", "blade-test"};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("blade-test");

		checkFileExists(projectPath);

		checkFileExists(projectPath.resolve("bnd.bnd"));

		File componentFile = checkFileExists(
			projectPath.resolve(
				Paths.get(
					"src", "main", "java", "blade", "test", "context", "contributor",
					"BladeTestTemplateContextContributor.java")));

		contains(
			componentFile,
			".*^public class BladeTestTemplateContextContributor.*implements TemplateContextContributor.*$");

		File gradleBuildFile = checkFileExists(projectPath.resolve("build.gradle"));

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		TestUtil.verifyBuild(projectPath, "blade.test-1.0.0.jar");
	}

	@Test
	public void testCreateTheme() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "theme", "theme-test"};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("theme-test");

		checkFileExists(projectPath);

		checkFileDoesNotExists(projectPath.resolve("bnd.bnd"));

		checkFileExists(projectPath.resolve(Paths.get("src", "main", "webapp", "css", "_custom.scss")));

		File properties = checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "webapp", "WEB-INF", "liferay-plugin-package.properties")));

		contains(properties, ".*^name=theme-test.*");

		Path buildFile = projectPath.resolve("build.gradle");

		try (BufferedWriter bufferWriter = Files.newBufferedWriter(buildFile, StandardOpenOption.APPEND)) {
			bufferWriter.write("\nbuildTheme { jvmArgs \"-Djava.awt.headless=true\" }");
		}

		TestUtil.verifyBuild(projectPath, "theme-test.war");
	}

	@Test
	public void testCreateThemeContributor() throws Exception {
		String[] args =
			{"create", "-d", tempRoot.toString(), "-t", "theme-contributor", "-C", "foobar", "theme-contributor-test"};

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = tempRoot.resolve("theme-contributor-test");

		checkFileExists(projectPath);

		File bnd = checkFileExists(projectPath.resolve("bnd.bnd"));

		contains(bnd, ".*Liferay-Theme-Contributor-Type: foobar.*");

		TestUtil.verifyBuild(projectPath, "theme.contributor.test-1.0.0.jar");
	}

	@Test
	public void testCreateWarHookLocation() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		makeWorkspace(tempRoot, workspace);

		testCreateWar(tempRoot, workspace, "war-hook", "war-hook-test");
	}

	@Test
	public void testCreateWarMVCPortletLocation() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		makeWorkspace(tempRoot, workspace);

		testCreateWar(tempRoot, workspace, "war-mvc-portlet", "war-portlet-test");
	}

	@Test
	public void testCreateWorkspaceGradleFragment() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path extensionsDir = workspace.resolve(Paths.get("modules", "extensions"));

		String[] args = {
			"create", "-d", extensionsDir.toString(), "-t", "fragment", "-h", "com.liferay.login.web", "-H", "1.0.0",
			"loginHook"
		};

		makeWorkspace(tempRoot, workspace);

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(extensionsDir.resolve("loginHook"));

		contains(
			checkFileExists(extensionsDir.resolve(Paths.get("loginHook", "bnd.bnd"))),
			new String[] {
				".*^Bundle-SymbolicName: loginhook.*$",
				".*^Fragment-Host: com.liferay.login.web;bundle-version=\"1.0.0\".*$"
			});

		checkFileExists(extensionsDir.resolve(Paths.get("loginHook", "build.gradle")));

		lacks(
			checkFileExists(extensionsDir.resolve(Paths.get("loginHook", "build.gradle"))),
			".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(extensionsDir.resolve("loginHook"), "loginhook-1.0.0.jar");

		verifyImportPackage(extensionsDir.resolve(Paths.get("loginHook", "build", "libs", "loginhook-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceGradlePortletProject() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path appsDir = workspace.resolve(Paths.get("modules", "apps"));

		String[] args = {"create", "-d", appsDir.toString(), "-t", "portlet", "-c", "Foo", "gradle.test"};

		makeWorkspace(tempRoot, workspace);

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(appsDir.resolve(Paths.get("gradle.test", "build.gradle")));

		checkFileDoesNotExists(appsDir.resolve(Paths.get("gradle.test", "gradlew")));

		contains(
			checkFileExists(
				appsDir.resolve(
					Paths.get("gradle.test", "src", "main", "java", "gradle", "test", "portlet", "FooPortlet.java"))),
			new String[] {
				"^package gradle.test.portlet;.*", ".*javax.portlet.display-name=Foo.*",
				".*^public class FooPortlet .*", ".*Hello from Foo!.*"
			});

		lacks(
			checkFileExists(appsDir.resolve(Paths.get("gradle.test", "build.gradle"))),
			".*^apply plugin: \"com.liferay.plugin\".*");

		TestUtil.verifyBuild(workspace.toString(), "jar", "gradle.test-1.0.0.jar");

		verifyImportPackage(appsDir.resolve(Paths.get("gradle.test", "build", "libs", "gradle.test-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectApiPath() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path nestedDir = workspace.resolve(Paths.get("modules", "nested/path"));

		String[] args =
			{"create", "-d", nestedDir.toString(), "-t", "service-builder", "-p", "com.liferay.sample", "sample"};

		makeWorkspace(tempRoot, workspace);

		Files.createDirectories(nestedDir);

		Assert.assertTrue(Files.exists(nestedDir));

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(nestedDir.resolve(Paths.get("sample", "build.gradle")));

		checkFileDoesNotExists(nestedDir.resolve(Paths.get("sample", "settings.gradle")));

		checkFileExists(nestedDir.resolve(Paths.get("sample", "sample-api", "build.gradle")));

		checkFileExists(nestedDir.resolve(Paths.get("sample", "sample-service", "build.gradle")));

		File file = checkFileExists(nestedDir.resolve(Paths.get("sample", "sample-service", "build.gradle")));

		contains(file, ".*compileOnly project\\(\":modules:nested:path:sample:sample-api\"\\).*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "buildService");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(
			nestedDir.resolve(Paths.get("sample", "sample-api")), "com.liferay.sample.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(
			nestedDir.resolve(Paths.get("sample", "sample-service")), "com.liferay.sample.service-1.0.0.jar");

		verifyImportPackage(
			nestedDir.resolve(
				Paths.get("sample", "sample-service", "build", "libs", "com.liferay.sample.service-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectDashes() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path modulesDir = workspace.resolve("modules");

		String[] args =
			{"create", "-d", modulesDir.toString(), "-t", "service-builder", "-p", "com.sample", "workspace-sample"};

		makeWorkspace(tempRoot, workspace);

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(modulesDir.resolve(Paths.get("workspace-sample", "build.gradle")));

		checkFileDoesNotExists(modulesDir.resolve(Paths.get("workspace-sample", "settings.gradle")));

		checkFileExists(modulesDir.resolve(Paths.get("workspace-sample", "workspace-sample-api", "build.gradle")));

		checkFileExists(modulesDir.resolve(Paths.get("workspace-sample", "workspace-sample-service", "build.gradle")));

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "buildService");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(
			modulesDir.resolve(Paths.get("workspace-sample", "workspace-sample-api")), "com.sample.api-1.0.0.jar");

		GradleRunnerUtil.verifyBuildOutput(
			modulesDir.resolve(Paths.get("workspace-sample", "workspace-sample-service")),
			"com.sample.service-1.0.0.jar");

		verifyImportPackage(
			modulesDir.resolve(
				Paths.get(
					"workspace-sample", "workspace-sample-service", "build", "libs", "com.sample.service-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectDefault() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path modulesDir = workspace.resolve("modules");

		String[] args =
			{"create", "-d", modulesDir.toString(), "-t", "service-builder", "-p", "com.liferay.sample", "sample"};

		makeWorkspace(tempRoot, workspace);

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(modulesDir.resolve(Paths.get("sample", "build.gradle")));

		checkFileDoesNotExists(modulesDir.resolve(Paths.get("sample", "settings.gradle")));

		checkFileExists(modulesDir.resolve(Paths.get("sample", "sample-api", "build.gradle")));

		checkFileExists(modulesDir.resolve(Paths.get("sample", "sample-service", "build.gradle")));

		File file = checkFileExists(modulesDir.resolve(Paths.get("", "sample", "sample-service", "build.gradle")));

		contains(file, ".*compileOnly project\\(\":modules:sample:sample-api\"\\).*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "buildService");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(
			modulesDir.resolve(Paths.get("sample", "sample-api")), "com.liferay.sample.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(
			modulesDir.resolve(Paths.get("sample", "sample-service")), "com.liferay.sample.service-1.0.0.jar");

		Path serviceJar = modulesDir.resolve(
			Paths.get("sample", "sample-service", "build", "libs", "com.liferay.sample.service-1.0.0.jar"));

		verifyImportPackage(serviceJar);

		try (JarFile serviceJarFile = new JarFile(serviceJar.toFile())) {
			Manifest manifest = serviceJarFile.getManifest();

			Attributes mainAttributes = manifest.getMainAttributes();

			String springContext = mainAttributes.getValue("Liferay-Spring-Context");

			Assert.assertTrue(springContext.equals("META-INF/spring"));
		}
	}

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectDots() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path modulesDir = workspace.resolve("modules");

		String[] args =
			{"create", "-d", modulesDir.toString(), "-t", "service-builder", "-p", "com.sample", "workspace.sample"};

		makeWorkspace(tempRoot, workspace);

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(modulesDir.resolve(Paths.get("workspace.sample", "build.gradle")));

		checkFileDoesNotExists(modulesDir.resolve(Paths.get("workspace.sample", "settings.gradle")));

		checkFileExists(modulesDir.resolve(Paths.get("workspace.sample", "workspace.sample-api", "build.gradle")));

		checkFileExists(modulesDir.resolve(Paths.get("workspace.sample", "workspace.sample-service", "build.gradle")));

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "buildService");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(
			modulesDir.resolve(Paths.get("workspace.sample", "workspace.sample-api")), "com.sample.api-1.0.0.jar");

		GradleRunnerUtil.verifyBuildOutput(
			modulesDir.resolve(Paths.get("workspace.sample", "workspace.sample-service")),
			"com.sample.service-1.0.0.jar");

		verifyImportPackage(
			modulesDir.resolve(
				Paths.get(
					"workspace.sample", "workspace.sample-service", "build", "libs", "com.sample.service-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceModuleLocation() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path modulesDir = workspace.resolve("modules");

		String[] args = {"--base", workspace.toString(), "create", "-t", "mvc-portlet", "foo"};

		makeWorkspace(tempRoot, workspace);

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(modulesDir.resolve("foo"));

		checkFileExists(modulesDir.resolve(Paths.get("foo", "bnd.bnd")));

		File portletFile = checkFileExists(
			modulesDir.resolve(Paths.get("foo", "src", "main", "java", "foo", "portlet", "FooPortlet.java")));

		contains(portletFile, ".*^public class FooPortlet extends MVCPortlet.*$");

		File gradleBuildFile = checkFileExists(modulesDir.resolve(Paths.get("foo", "build.gradle")));

		lacks(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(modulesDir.resolve("foo"), "foo-1.0.0.jar");

		verifyImportPackage(modulesDir.resolve(Paths.get("foo", "build", "libs", "foo-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceProjectAllDefaults() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path projectPath = workspace.resolve(Paths.get("modules", "apps"));

		String[] args = {"create", "-d", projectPath.toString(), "-t", "mvc-portlet", "foo"};

		makeWorkspace(tempRoot, workspace);

		TestUtil.runBlade(tempRoot, args);

		checkFileExists(projectPath.resolve("foo"));

		checkFileExists(projectPath.resolve(Paths.get("foo", "bnd.bnd")));

		File portletFile = checkFileExists(
			projectPath.resolve(Paths.get("foo", "src", "main", "java", "foo", "portlet", "FooPortlet.java")));

		contains(portletFile, ".*^public class FooPortlet extends MVCPortlet.*$");

		File gradleBuildFile = checkFileExists(projectPath.resolve(Paths.get("foo", "build.gradle")));

		lacks(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(projectPath.resolve("foo"), "foo-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("foo", "build", "libs", "foo-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceProjectWithRefresh() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path appsDir = workspace.resolve(Paths.get("modules", "apps"));

		String[] args = {"create", "-d", appsDir.toString(), "-t", "mvc-portlet", "foo-refresh"};

		makeWorkspace(tempRoot, workspace);

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = appsDir.resolve("foo-refresh");

		checkFileExists(projectPath);

		checkFileExists(projectPath.resolve("bnd.bnd"));

		File portletFile = checkFileExists(
			projectPath.resolve(
				Paths.get("src", "main", "java", "foo", "refresh", "portlet", "FooRefreshPortlet.java")));

		contains(portletFile, ".*^public class FooRefreshPortlet extends MVCPortlet.*$");

		File gradleBuildFile = checkFileExists(projectPath.resolve("build.gradle"));

		lacks(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(projectPath, "foo.refresh-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build", "libs", "foo.refresh-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceThemeLocation() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		String[] args = {"--base", workspace.toString(), "create", "-t", "theme", "theme-test"};

		makeWorkspace(tempRoot, workspace);

		TestUtil.runBlade(tempRoot, args);

		Path projectPath = workspace.resolve(Paths.get("wars", "theme-test"));

		checkFileExists(projectPath);

		checkFileDoesNotExists(projectPath.resolve("bnd.bnd"));

		checkFileExists(projectPath.resolve(Paths.get("src", "main", "webapp", "css", "_custom.scss")));

		File properties = checkFileExists(
			projectPath.resolve(Paths.get("src", "main", "webapp", "WEB-INF", "liferay-plugin-package.properties")));

		contains(properties, ".*^name=theme-test.*");

		BuildTask buildTask = GradleRunnerUtil.executeGradleRunner(workspace.toString(), "war");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildTask);

		GradleRunnerUtil.verifyBuildOutput(projectPath, "theme-test.war");
	}

	@Test
	public void testCreateWorkspaceTypeValid() throws Exception {
		Path workspace = tempRoot.resolve("workspace");

		Path modulesDir = workspace.resolve("modules");

		String[] args = {"--base", modulesDir.toString(), "create", "-t", "soy-portlet", "foo"};

		makeWorkspace(tempRoot, workspace);

		TestUtil.runBlade(tempRoot, args);

		Path buildGradle = modulesDir.resolve(Paths.get("foo", "build.gradle"));

		checkFileExists(buildGradle);

		String content = new String(Files.readAllBytes(buildGradle));

		Assert.assertEquals(1, StringUtils.countMatches(content, '{'));

		Assert.assertEquals(1, StringUtils.countMatches(content, '}'));
	}

	@Test
	public void testLiferayVersion() throws Exception {
		String[] sevenZeroArgs = {"--base", tempRoot.toString(), "create", "-t", "npm-angular-portlet", "seven-zero"};

		TestUtil.runBlade(tempRoot, sevenZeroArgs);

		Path npmbundlerrc = tempRoot.resolve(Paths.get("seven-zero", "build.gradle"));

		String content = new String(Files.readAllBytes(npmbundlerrc));

		Assert.assertFalse(content.contains("js.loader.modules.extender.api"));

		String[] sevenOneArgs =
			{"--base", tempRoot.toString(), "create", "-t", "npm-angular-portlet", "-v", "7.1", "seven-one"};

		TestUtil.runBlade(tempRoot, sevenOneArgs);

		npmbundlerrc = tempRoot.resolve(Paths.get("seven-one", "build.gradle"));

		content = new String(Files.readAllBytes(npmbundlerrc));

		Assert.assertTrue(content.contains("js.loader.modules.extender.api"));
	}

	@Test
	public void testListTemplates() throws Exception {
		String[] args = {"create", "-l"};

		String templateList = TestUtil.runBlade(temporaryFolder.getRoot(), args);

		Map<String, String> templates = ProjectTemplates.getTemplates();

		List<String> templateNames = new ArrayList<>(templates.keySet());

		for (String templateName : templateNames) {
			Assert.assertTrue(templateList.contains(templateName));
		}
	}

	@Test
	public void testWrongTemplateTyping() throws Exception {
		String[] args = {"create", "-d", tempRoot.toString(), "-t", "activatorXXX", "wrong-activator"};

		boolean encounteredError = false;

		try {
			TestUtil.runBlade(tempRoot, args);
		}
		catch (Throwable th) {
			encounteredError = true;
		}

		Assert.assertTrue(encounteredError);

		Path projectPath = tempRoot.resolve("wrong-activator");

		checkFileDoesNotExists(projectPath);
	}

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	protected Path tempRoot = null;

}
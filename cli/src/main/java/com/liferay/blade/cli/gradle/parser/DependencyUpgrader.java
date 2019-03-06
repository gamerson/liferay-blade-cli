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

package com.liferay.blade.cli.gradle.parser;

import com.liferay.blade.cli.util.BladeUtil;

import java.io.File;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;

/**
 * @author Vernon Singleton
 */
public class DependencyUpgrader {

	public DependencyUpgrader(File file) throws IOException, MultipleCompilationErrorsException {
		this(FileUtils.readFileToString(file, "UTF-8"));

		_file = file;
	}

	public DependencyUpgrader(String scriptContents) throws MultipleCompilationErrorsException {
		AstBuilder builder = new AstBuilder();

		_nodes = builder.buildFromString(scriptContents);
	}

	public List<GradleDependency> getBuildscriptDependencies() {
		UpgradeVisitor visitor = new UpgradeVisitor();

		walkScript(visitor);

		return visitor.getBuildscriptDependencies();
	}

	public List<GradleDependency> getDependencies() {
		UpgradeVisitor visitor = new UpgradeVisitor();

		walkScript(visitor);

		return visitor.getDependencies();
	}

	public List<String> getGradleFileContents() {
		return _gradleFileContents;
	}

	public List<GradleDependency> getUpgradeDependencies() throws IOException {

		List<GradleDependency> list = new ArrayList<>();

		return list;

	}

	public UpgradeVisitor insertDependency(GradleDependency gradleDependency) throws IOException {
		StringBuilder sb = new StringBuilder();

		sb.append("compile group: \"");
		sb.append(gradleDependency.getGroup());
		sb.append("\", name:\"");
		sb.append(gradleDependency.getName());
		sb.append("\", version:\"");
		sb.append(gradleDependency.getVersion());
		sb.append("\"");

		return insertDependency(sb.toString());
	}

	// TODO do not use until fixed for upgrading

	public UpgradeVisitor insertDependency(String dependency) throws IOException {
		UpgradeVisitor visitor = new UpgradeVisitor();

		walkScript(visitor);

		_gradleFileContents = FileUtils.readLines(_file);

		if (!dependency.startsWith("\t")) {
			dependency = "\t" + dependency;
		}

		if (visitor.getDependenceLineNum() == -1) {
			_gradleFileContents.add("");
			_gradleFileContents.add("dependencies {");
			_gradleFileContents.add(dependency);
			_gradleFileContents.add("}");
		}
		else {
			if (visitor.getColumnNum() != -1) {
				_gradleFileContents = Files.readAllLines(Paths.get(_file.toURI()), StandardCharsets.UTF_8);

				StringBuilder builder = new StringBuilder(_gradleFileContents.get(visitor.getDependenceLineNum() - 1));

				builder.insert(visitor.getColumnNum() - 2, "\n" + dependency + "\n");

				String dep = builder.toString();

				if (BladeUtil.isWindows()) {
					dep.replace("\n", "\r\n");
				}
				else {
					dep.replace("\n", "\r");
				}

				_gradleFileContents.remove(visitor.getDependenceLineNum() - 1);
				_gradleFileContents.add(visitor.getDependenceLineNum() - 1, dep);
			}
			else {
				_gradleFileContents.add(visitor.getDependenceLineNum() - 1, dependency);
			}
		}

		return visitor;
	}

	public void walkScript(GroovyCodeVisitor visitor) {
		for (ASTNode node : _nodes) {
			node.visit(visitor);
		}
	}

	private File _file;
	private List<String> _gradleFileContents;
	private List<ASTNode> _nodes;

}
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;

/**
 * @author Vernon Singleton
 */
public class UpgradeVisitor extends CodeVisitorSupport {

	public List<GradleDependency> getBuildscriptDependencies() {
		return _buildscriptDependencies;
	}

	public int getColumnNum() {
		return _columnNum;
	}

	public int getDependenceLineNum() {
		return _dependenceLineNum;
	}

	public List<GradleDependency> getDependencies() {
		return _dependencies;
	}

	@Override
	public void visitArgumentlistExpression(ArgumentListExpression ale) {
		List<Expression> expressions = ale.getExpressions();

		if ((expressions.size() == 1) && (expressions.get(0) instanceof ConstantExpression)) {
			Expression expression = expressions.get(0);

			String expressionText = expression.getText();

			String[] deps = expressionText.split(":");

			if (deps.length >= 3) {
//				System.out.println("ale: deps.length >= 3 ... deps[1] = " + deps[1]);
//				System.out.println("ale: _inDependencies = " + _inDependencies);
//				System.out.println("ale: _inBuildscriptDependencies = " + _inBuildscriptDependencies);

				if (_inDependencies && !_inBuildscriptDependencies) {
					_dependencies.add(new GradleDependency(deps[0], deps[1], deps[2]));
				}

				if (_inBuildscriptDependencies) {
					_buildscriptDependencies.add(new GradleDependency(deps[0], deps[1], deps[2]));
				}
			}
		}

		super.visitArgumentlistExpression(ale);
	}

	@Override
	public void visitClosureExpression(ClosureExpression expression) {

		super.visitClosureExpression(expression);
	}

	@Override
	public void visitMapExpression(MapExpression expression) {

		List<MapEntryExpression> mapEntryExpressions = expression.getMapEntryExpressions();
		Map<String, String> dependencyMap = new HashMap<>();

		boolean gav = false;

		for (MapEntryExpression mapEntryExpression : mapEntryExpressions) {
			Expression keyExpression = mapEntryExpression.getKeyExpression();
			Expression valueExpression  = mapEntryExpression.getValueExpression();

			String key = keyExpression.getText();
			String value = valueExpression.getText();

			if (key.equalsIgnoreCase("group")) {
				gav = true;
			}

			dependencyMap.put(key, value);
		}

		if (gav) {
			if (_inDependencies && !_inBuildscriptDependencies) {
				_dependencies.add(new GradleDependency(dependencyMap));
			}

			if (_inBuildscriptDependencies) {
				_buildscriptDependencies.add(new GradleDependency(dependencyMap));
			}
		}

		super.visitMapExpression(expression);
	}

	@Override
	public void visitMethodCallExpression(MethodCallExpression call) {

		int lineNumber = call.getLineNumber();

		if (lineNumber > _dependenciesLastLineNumber) {
			_inDependencies = false;
		}

		if (lineNumber > _buildscriptLastLineNumber) {
			_inBuildscript = false;
		}

		if (lineNumber > _buildscriptDependenciesLastLineNumber) {
			_inBuildscriptDependencies = false;
		}

		String method = call.getMethodAsString();

		if (method.equals("buildscript")) {
			int lastLineNumber = call.getLastLineNumber();

			_buildscriptLastLineNumber = lastLineNumber;

			_inBuildscript = true;
		}

		if (method.equals("dependencies")) {
			int lastLineNumber = call.getLastLineNumber();

			_dependenciesLastLineNumber = lastLineNumber;

			_inDependencies = true;
		}

		if (_inBuildscript && _inDependencies && (_buildscriptDependenciesLastLineNumber == -1)) {
			_buildscriptDependenciesLastLineNumber = call.getLastLineNumber();
			_inBuildscriptDependencies = true;
		}

		super.visitMethodCallExpression(call);

	}

	private List<GradleDependency> _buildscriptDependencies = new ArrayList<>();
	private int _buildscriptDependenciesLastLineNumber = -1;
	private int _buildscriptLastLineNumber = -1;
	private int _columnNum = -1;
	private int _dependenceLineNum = -1;
	private List<GradleDependency> _dependencies = new ArrayList<>();
	private int _dependenciesLastLineNumber = -1;
	private boolean _inBuildscript = false;
	private boolean _inBuildscriptDependencies = false;
	private boolean _inDependencies = false;

}
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

package com.liferay.blade.upgrade.liferay70.deprecatedmethods;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.Path;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import com.liferay.blade.api.FileMigrator;
import com.liferay.blade.api.JavaFile;
import com.liferay.blade.api.Problem;
import com.liferay.blade.api.SearchResult;
import com.liferay.blade.upgrade.liferay70.JavaFileMigrator;

@Component(
	property = {
		"file.extensions=java,jsp,jspf",
		"implName=DeprecatedMethodsInvocation"
	},
	service = FileMigrator.class
)
public class DeprecatedMethodsInvocation extends JavaFileMigrator {

	private static JSONObject tempMethod = null;

	@Override
	public List<Problem> analyze(File file) {
		final List<Problem> problems = new ArrayList<>();
		final List<JSONArray> deprecatedMethods = new ArrayList<>();

		JSONArray version61DeprecatedMethods = getDeprecatedMethods(
				"/com/liferay/blade/upgrade/liferay70/deprecatedmethods/deprecatedMethods61.json");
		JSONArray version62DeprecatedMethods = getDeprecatedMethods(
				"/com/liferay/blade/upgrade/liferay70/deprecatedmethods/deprecatedMethods62.json");
		JSONArray version70DeprecatedMethods = getDeprecatedMethods(
				"/com/liferay/blade/upgrade/liferay70/deprecatedmethods/deprecatedMethods70.json");
		JSONArray versionNoneDeprecatedMethods = getDeprecatedMethods(
				"/com/liferay/blade/upgrade/liferay70/deprecatedmethods/deprecatedMethodsNoneVersionFile.json");

		deprecatedMethods.add(version61DeprecatedMethods);
		deprecatedMethods.add(version62DeprecatedMethods);
		deprecatedMethods.add(version70DeprecatedMethods);
		deprecatedMethods.add(versionNoneDeprecatedMethods);

		for (JSONArray methods : deprecatedMethods) {
			for (int i = 0; i < methods.length(); i++) {
				tempMethod = methods.getJSONObject(i);

				final List<SearchResult> searchResults = searchFile(file, createFileChecker(_type, file));

				if (searchResults != null) {
					for (SearchResult searchResult : searchResults) {
						String fileExtension = new Path(file.getAbsolutePath()).getFileExtension();

						problems.add(new Problem(tempMethod.getString("javadoc"), tempMethod.getString("javadoc"),
								fileExtension, "", file, searchResult.startLine, searchResult.startOffset,
								searchResult.endOffset, tempMethod.getString("javadoc"),
								searchResult.autoCorrectContext, Problem.STATUS_NOT_RESOLVED,
								Problem.DEFAULT_MARKER_ID));
					}
				}
			}
		}

		return problems;
	}

	public JSONArray getDeprecatedMethods(String jsonFilePath) {

		try (InputStream in = getClass().getResourceAsStream(jsonFilePath)) {

			String jsonContext = IOUtils.toString(in, "UTF-8");

			return new JSONArray(jsonContext);
		}
		catch (IOException e) {
		}

		return null;
	}

	@Override
	protected List<SearchResult> searchFile(File file, JavaFile fileChecker) {
		final List<SearchResult> searchResults = new ArrayList<>();

		String[] parameters = null;

		JSONArray parameterJSONArray = tempMethod.getJSONArray("parameters");

		if (parameterJSONArray != null) {
			parameters = new String[parameterJSONArray.length()];

			for (int i = 0; i < parameterJSONArray.length(); i++) {
				parameters[i] = parameterJSONArray.getString(i);
			}
		}

		searchResults.addAll(fileChecker.findMethodInvocations(
			tempMethod.getString("className"), null, tempMethod.getString("methodName"), parameters));

		searchResults.addAll(fileChecker.findMethodInvocations(
			null, tempMethod.getString("className"), tempMethod.getString("methodName"), parameters));

		return searchResults;
	}

}

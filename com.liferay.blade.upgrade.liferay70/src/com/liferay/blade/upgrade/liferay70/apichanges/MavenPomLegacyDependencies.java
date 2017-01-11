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

import aQute.lib.io.IO;

import com.liferay.blade.api.AutoMigrateException;
import com.liferay.blade.api.AutoMigrator;
import com.liferay.blade.api.FileMigrator;
import com.liferay.blade.api.Problem;
import com.liferay.blade.api.SearchResult;
import com.liferay.blade.api.XMLFile;
import com.liferay.blade.upgrade.liferay70.XMLFileMigrator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

@Component(
	property = {
		"file.extensions=xml",
		"problem.title=maven pom legacy dependencies",
		"problem.summary=maven pom legacy dependencies",
		"problem.tickets=",
		"problem.section=#maven-pom-legacy-dependencies",
		"implName=MavenPomLegacyDependencies",
		"auto.correct=maven-pom-legacy-dependencies"
	},
	service = {
		AutoMigrator.class,
		FileMigrator.class
	}
)
public class MavenPomLegacyDependencies extends XMLFileMigrator implements AutoMigrator {

	private static final String KEY = "maven-pom-legacy-dependencies";

	private static String[][] dependenciesConvertMap;

	@Override
	protected List<SearchResult> searchFile(File file, XMLFile xmlFileChecker) {
		if ("pom.xml".equals(file.getName())) {
			List<SearchResult> results = new ArrayList<>();

			Collection<SearchResult> searchResults = new ArrayList<SearchResult>();

			String parentNodeName = "dependency";

			Map<String, String> portalService = new HashMap<String, String>();
			portalService.put("groupId", "com.liferay.portal");
			portalService.put("artifactId", "portal-service");

			Map<String, String> utilJava = new HashMap<String, String>();
			utilJava.put("groupId", "com.liferay.portal");
			utilJava.put("artifactId", "util-java");

			Map<String, String> utilBridges = new HashMap<String, String>();
			utilBridges.put("groupId", "com.liferay.portal");
			utilBridges.put("artifactId", "util-bridges");

			Map<String, String> utilTaglib = new HashMap<String, String>();
			utilTaglib.put("groupId", "com.liferay.portal");
			utilTaglib.put("artifactId", "util-taglib");

			Map<String, String> utilSlf4j = new HashMap<String, String>();
			utilSlf4j.put("groupId", "com.liferay.portal");
			utilSlf4j.put("artifactId", "util-slf4j");

			searchResults.addAll(xmlFileChecker.searchChildren(parentNodeName, portalService));
			searchResults.addAll(xmlFileChecker.searchChildren(parentNodeName, utilJava));
			searchResults.addAll(xmlFileChecker.searchChildren(parentNodeName, utilBridges));
			searchResults.addAll(xmlFileChecker.searchChildren(parentNodeName, utilTaglib));
			searchResults.addAll(xmlFileChecker.searchChildren(parentNodeName, utilSlf4j));

			for (SearchResult result : searchResults) {
				result.autoCorrectContext = KEY;
			}

			results.addAll(searchResults);

			return results;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public int correctProblems(File file, List<Problem> problems) throws AutoMigrateException {
		int corrected = 0;
		IFile xmlFile = getXmlFile(file);
		IDOMModel xmlModel = null;

		if (xmlFile != null) {
			try {
				xmlModel = (IDOMModel) StructuredModelManager.getModelManager().getModelForEdit(xmlFile);

				List<IDOMElement> elementsToCorrect = new ArrayList<>();

				for (Problem problem : problems) {
					if (KEY.equals(problem.autoCorrectContext)) {
						IndexedRegion region = xmlModel.getIndexedRegion(problem.startOffset);

						if (region instanceof IDOMElement) {
							IDOMElement element = (IDOMElement) region;
							elementsToCorrect.add(element);
						}
					}
				}

				for (IDOMElement element : elementsToCorrect) {
					xmlModel.aboutToChangeModel();

					NodeList groudIdList = element.getElementsByTagName("groupId");
					NodeList artifactIdList = element.getElementsByTagName("artifactId");
					NodeList versionList = element.getElementsByTagName("version");

					if (groudIdList != null && groudIdList.getLength() == 1 && artifactIdList != null
							&& artifactIdList.getLength() == 1 && versionList != null && versionList.getLength() == 1) {

						IDOMElement groupIdElement = (IDOMElement) groudIdList.item(0);
						IDOMElement artifactIdElement = (IDOMElement) artifactIdList.item(0);
						IDOMElement versionElement = (IDOMElement) versionList.item(0);

						String groupIdContent = groupIdElement.getTextContent();
						String artifactIdContent = artifactIdElement.getTextContent();

						if (groupIdContent != null && "com.liferay.portal".equals(groupIdContent.trim())
								&& artifactIdContent != null && artifactIdContent.trim().length() > 0) {
							String[] result = getFixedArtifactIdAndVersion(artifactIdContent.trim());

							if (result != null) {
								removeChildren(artifactIdElement);
								Text artifactIdTextContent = element.getOwnerDocument().createTextNode(result[0]);
								artifactIdElement.appendChild(artifactIdTextContent);

								removeChildren(versionElement);
								Text versionTextContent = element.getOwnerDocument().createTextNode(result[1]);
								versionElement.appendChild(versionTextContent);
							}
						}
					}

					xmlModel.changedModel();

					corrected++;
				}

				xmlModel.save();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (xmlModel != null) {
					xmlModel.releaseFromEdit();
				}
			}
		}

		if (corrected > 0 && !xmlFile.getLocation().toFile().equals(file)) {
			try {
				IO.copy(xmlFile.getContents(), file);
			} catch (IOException | CoreException e) {
				throw new AutoMigrateException("Error writing corrected file.", e);
			}
		}

		return corrected;
	}

	private String[] getFixedArtifactIdAndVersion(String artifactId) {
		if (dependenciesConvertMap == null) {
			dependenciesConvertMap = new String[5][];

			dependenciesConvertMap[0] = new String[] { "portal-service", "com.liferay.portal.kernel", "2.6.0" };
			dependenciesConvertMap[1] = new String[] { "util-java", "com.liferay.util.java", "2.0.0" };
			dependenciesConvertMap[2] = new String[] { "util-bridges", "com.liferay.util.bridges", "2.0.0" };
			dependenciesConvertMap[3] = new String[] { "util-taglib", "com.liferay.util.taglib", "2.0.0" };
			dependenciesConvertMap[4] = new String[] { "util-slf4j", "com.liferay.util.slf4j", "1.0.0" };
		}

		for (String[] str : dependenciesConvertMap) {
			if (artifactId.equals(str[0])) {
				String[] result = new String[2];
				result[0] = str[1];
				result[1] = str[2];

				return result;
			}
		}

		return null;
	}

	private void removeChildren(IDOMElement element) {
		while (element.hasChildNodes()) {
			element.removeChild(element.getFirstChild());
		}
	}
}

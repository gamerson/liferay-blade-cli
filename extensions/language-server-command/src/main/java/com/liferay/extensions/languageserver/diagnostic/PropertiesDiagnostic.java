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

package com.liferay.extensions.languageserver.diagnostic;

import com.liferay.extensions.languageserver.properties.BladeProperties;
import com.liferay.extensions.languageserver.properties.LiferayWorkspaceGradleProperties;
import com.liferay.extensions.languageserver.properties.PortalProperties;
import com.liferay.extensions.languageserver.properties.PropertiesFile;
import com.liferay.extensions.languageserver.properties.PropertyPair;
import com.liferay.extensions.languageserver.services.Service;
import com.liferay.extensions.languageserver.util.FileUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * @author Terry Jia
 */
public class PropertiesDiagnostic {

	public PropertiesDiagnostic(File file) {
		_file = file;
	}

	public List<Diagnostic> validate() {
		List<Diagnostic> diagnostics = new ArrayList<>();

		List<PropertiesFile> propertiesFiles = _getAllPropertiesFiles(_file);

		for (PropertiesFile propertiesFile : propertiesFiles) {
			if (propertiesFile.match()) {
				List<PropertyPair> properties = propertiesFile.getProperties();

				String[] lines = FileUtil.readLinesFromFile(_file);

				for (int i = 0; i < lines.length; i++) {
					String line = lines[i];

					String l = line.trim();

					if (l.equals("") || l.startsWith("#")) {
						continue;
					}

					int t = l.indexOf("=");

					String key = l;
					String value = "";

					if (t > 0) {
						key = l.substring(0, t);

						key = key.trim();

						value = l.substring(t + 1);

						value = value.trim();
					}

					boolean findKey = false;

					for (PropertyPair propertyPair : properties) {
						if (key.equals(propertyPair.getKey())) {
							findKey = true;

							break;
						}
					}

					if (!findKey) {
						int keyStart = line.indexOf(key);

						Diagnostic diagnostic = new Diagnostic();

						diagnostic.setSeverity(DiagnosticSeverity.Warning);
						diagnostic.setRange(
							new Range(new Position(i, keyStart), new Position(i, keyStart + key.length())));
						diagnostic.setMessage(key + " may not in possible keys");
						diagnostic.setSource("ex");

						diagnostics.add(diagnostic);
					}

					if (!value.equals("")) {
						for (PropertyPair propertyPair : properties) {
							if (key.equals(propertyPair.getKey())) {
								Service valueService = propertyPair.getValue();

								if (valueService != null) {
									try {
										valueService.validate(value);
									}
									catch (Exception e) {
										int valueStart = line.indexOf(value);

										Diagnostic diagnostic = new Diagnostic();

										diagnostic.setSeverity(DiagnosticSeverity.Warning);
										diagnostic.setRange(
											new Range(
												new Position(i, valueStart),
												new Position(i, valueStart + value.length())));

										String message = e.getMessage();

										if (message != null) {
											diagnostic.setMessage(message);
										}
										else {
											diagnostic.setMessage("Validate failed on value \"" + value + "\"");
										}

										diagnostic.setSource("ex");

										diagnostics.add(diagnostic);
									}

									break;
								}
							}
						}
					}
				}

				break;
			}
		}

		return diagnostics;
	}

	private List<PropertiesFile> _getAllPropertiesFiles(File file) {
		List<PropertiesFile> propertiesFiles = new ArrayList<>();

		propertiesFiles.add(new LiferayWorkspaceGradleProperties(file));
		propertiesFiles.add(new BladeProperties(file));
		propertiesFiles.add(new PortalProperties(file));

		return propertiesFiles;
	}

	private File _file;

}
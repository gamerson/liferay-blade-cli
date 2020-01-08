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

package com.liferay.extensions.languageserver.completions;

import com.liferay.extensions.languageserver.properties.BladeProperties;
import com.liferay.extensions.languageserver.properties.CoreLanguageProperties;
import com.liferay.extensions.languageserver.properties.LiferayPluginPackageProperties;
import com.liferay.extensions.languageserver.properties.LiferayWorkspaceGradleProperties;
import com.liferay.extensions.languageserver.properties.PortalProperties;
import com.liferay.extensions.languageserver.properties.PropertiesFile;
import com.liferay.extensions.languageserver.properties.PropertyPair;
import com.liferay.extensions.languageserver.services.Service;
import com.liferay.extensions.languageserver.util.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.net.URI;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringEscapeUtils;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * @author Terry Jia
 */
public class PropertiesCompletion {

	public PropertiesCompletion(CompletionParams completionParams) {
		Position position = completionParams.getPosition();

		_line = position.getLine();

		TextDocumentIdentifier textDocument = completionParams.getTextDocument();

		try {
			URI uri = new URI(textDocument.getUri());

			_file = new File(uri);

			List<PropertiesFile> propertiesFiles = _getAllPropertiesFiles(_file);

			Stream<PropertiesFile> stream = propertiesFiles.stream();

			_propertiesFile = stream.filter(
				PropertiesFile::match
			).findFirst(
			).get();
		}
		catch (Exception e) {
		}
	}

	public List<CompletionItem> getCompletions(String currentContent) {
		List<CompletionItem> completionItems = new ArrayList<>();

		String[] lines = null;

		if (currentContent != null) {
			lines = FileUtil.readLinesFromString(currentContent);
		}
		else {
			lines = FileUtil.readLinesFromFile(_file);
		}

		if (_propertiesFile != null) {
			String line = "";

			if (_line < lines.length) {
				line = lines[_line].trim();
			}

			List<PropertyPair> properties = _propertiesFile.getProperties();

			if (line.contains("=")) {
				int index = line.indexOf("=");

				String key = line.substring(0, index);

				String key2 = key.trim();

				Stream<PropertyPair> stream = properties.stream();

				completionItems = stream.filter(
					propertyPair -> Objects.equals(key2, propertyPair.getKey())
				).map(
					PropertyPair::getValue
				).map(
					Service::getPossibleValues
				).flatMap(
					Stream::of
				).map(
					possibleValue -> {
						possibleValue = StringEscapeUtils.escapeJava(possibleValue);

						CompletionItem completionItem = new CompletionItem(possibleValue);

						completionItem.setKind(CompletionItemKind.Property);

						return completionItem;
					}
				).collect(
					Collectors.toList()
				);
			}
			else {
				Properties props = new Properties();

				try {
					props.load(new FileReader(_file));
				}
				catch (IOException ioe) {
				}

				for (PropertyPair propertyPair : properties) {
					String key = propertyPair.getKey();

					if (!props.containsKey(key)) {
						String comment = propertyPair.getCommennt();

						CompletionItem completionItem = new CompletionItem(key);

						completionItem.setKind(CompletionItemKind.Property);
						completionItem.setDetail(comment);

						completionItems.add(completionItem);
					}
				}
			}
		}

		return completionItems;
	}

	private List<PropertiesFile> _getAllPropertiesFiles(File file) {
		List<PropertiesFile> propertiesFiles = new ArrayList<>();

		propertiesFiles.add(new LiferayWorkspaceGradleProperties(file));
		propertiesFiles.add(new BladeProperties(file));
		propertiesFiles.add(new PortalProperties(file));
		propertiesFiles.add(new LiferayPluginPackageProperties(file));
		propertiesFiles.add(new CoreLanguageProperties(file));

		return propertiesFiles;
	}

	private File _file;
	private int _line;
	private PropertiesFile _propertiesFile;

}
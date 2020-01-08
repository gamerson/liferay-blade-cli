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

package com.liferay.extensions.languageserver.properties;

import com.liferay.blade.cli.util.StringUtil;
import com.liferay.extensions.languageserver.LiferayLSPFile;
import com.liferay.extensions.languageserver.services.BooleanService;
import com.liferay.extensions.languageserver.services.FolderService;
import com.liferay.extensions.languageserver.services.Service;
import com.liferay.extensions.languageserver.services.StringArrayService;

import java.io.File;
import java.io.InputStream;

import java.lang.reflect.Constructor;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Terry Jia
 */
public abstract class PropertiesFile extends LiferayLSPFile {

	public PropertiesFile(File file, String storageFile) {
		super(file);

		Class<?> clazz = getClass();

		try (InputStream in = clazz.getResourceAsStream(storageFile)) {
			String source = IOUtils.toString(in, StandardCharsets.UTF_8);

			JSONObject jsonObject = new JSONObject(source);

			_checkPossibleKeys = jsonObject.getBoolean("checkPossibleKeys");

			JSONArray keyJSONArray = jsonObject.getJSONArray("keys");

			for (Object keyObject : keyJSONArray) {
				PropertyPair propertyPair = new PropertyPair();

				JSONObject keyJSONObject = (JSONObject)keyObject;

				String key = keyJSONObject.getString("key");

				boolean validateValues = keyJSONObject.getBoolean("validateValues");

				if (validateValues) {
					_checkPossibleValueKeys.add(key);
				}

				propertyPair.setKey(key);

				try {
					String comment = keyJSONObject.getString("comment");

					propertyPair.setComment(comment);
				}
				catch (Exception e) {
				}

				try {
					String value = keyJSONObject.getString("values");

					if (!StringUtil.isNullOrEmpty(value)) {
						Service valueService = null;

						if (value.equals("folder")) {
							valueService = new FolderService(getFile());
						}
						else if (value.equals("boolean") || value.equals("true") || value.equals("false")) {
							valueService = new BooleanService(getFile());
						}
						else if (value.startsWith("CustomLSP")) {
							String className = "com.liferay.extensions.languageserver.services.custom." + value;

							Class<?> serviceClass = Class.forName(className);

							Constructor constructor = serviceClass.getConstructor(File.class);

							valueService = (Service)constructor.newInstance(getFile());
						}
						else {
							String[] possibleValues = value.split(",");

							valueService = new StringArrayService(getFile(), possibleValues);
						}

						propertyPair.setValue(valueService);
					}
				}
				catch (Exception e) {
				}

				_propertyPairs.add(propertyPair);
			}
		}
		catch (Exception e) {
		}
	}

	public boolean checkPossibleKeys() {
		return _checkPossibleKeys;
	}

	public List<String> checkPossibleValueKeys() {
		return _checkPossibleValueKeys;
	}

	public List<PropertyPair> getProperties() {
		return _propertyPairs;
	}

	public abstract boolean match();

	private boolean _checkPossibleKeys = false;
	private List<String> _checkPossibleValueKeys = new ArrayList<>();
	private List<PropertyPair> _propertyPairs = new ArrayList<>();

}
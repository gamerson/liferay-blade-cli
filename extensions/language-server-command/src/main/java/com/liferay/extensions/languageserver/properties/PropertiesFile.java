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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;

/**
 * @author Terry Jia
 */
public abstract class PropertiesFile extends LiferayLSPFile {

	public PropertiesFile(File file) {
		super(file);
	}

	public List<PropertyPair> getProperties() {
		List<PropertyPair> propertyPairs = new ArrayList<>();

		Class<?> clazz = getClass();

		try (InputStream in = clazz.getResourceAsStream(getStorageFileName())) {
			PropertiesConfiguration config = new PropertiesConfiguration();

			config.setThrowExceptionOnMissing(false);

			config.load(in);

			Iterator<String> keys = config.getKeys();

			while (keys.hasNext()) {
				String key = keys.next();

				PropertyPair propertyPair = new PropertyPair();

				propertyPair.setKey(key);

				String value = config.getString(key);

				if (!StringUtil.isNullOrEmpty(value)) {
					Service valueService = null;

					if (value.equals("folder")) {
						valueService = new FolderService(getFile());
					}
					else if (value.equals("boolean")) {
						valueService = new BooleanService(getFile());
					}
					else if (value.startsWith("Custom")) {
						String className = "com.liferay.extensions.languageserver.services.custom." + value;

						Class<?> serviceClass = Class.forName(className);

						Constructor constructor = serviceClass.getConstructor(File.class);

						valueService = (Service)constructor.newInstance(getFile());
					}
					else {
						String[] possibleValues = config.getStringArray(key);

						valueService = new StringArrayService(getFile(), possibleValues);
					}

					propertyPair.setValue(valueService);
				}

				PropertiesConfigurationLayout layout = config.getLayout();

				propertyPair.setComment(layout.getComment(key));

				propertyPairs.add(propertyPair);
			}
		}
		catch (Exception e) {
		}

		return propertyPairs;
	}

	public abstract String getStorageFileName();

	public abstract boolean match();

}
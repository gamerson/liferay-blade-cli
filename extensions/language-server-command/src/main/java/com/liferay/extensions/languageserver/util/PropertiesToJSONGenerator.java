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

package com.liferay.extensions.languageserver.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.Iterator;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Terry Jia
 */
public class PropertiesToJSONGenerator {

	public static void main(String[] args) throws Exception {
		File propertiesFile = new File("");

		PropertiesConfiguration config = new PropertiesConfiguration();

		config.setThrowExceptionOnMissing(false);

		try (InputStream in = new FileInputStream(propertiesFile)) {
			config.load(in);
		}

		Iterator<String> keys = config.getKeys();

		JSONObject propertiesObject = new JSONObject();

		propertiesObject.put("checkPossibleKeys", false);

		JSONArray keyArray = new JSONArray();

		while (keys.hasNext()) {
			JSONObject object = new JSONObject();

			String key = keys.next();

			object.put("key", key);

			object.put("validateValues", false);

			String[] values = config.getStringArray(key);

			if (values != null) {
				String s = _merge(values);

				object.put("values", s);
			}

			PropertiesConfigurationLayout layout = config.getLayout();

			String comment = layout.getComment(key);

			if (comment != null) {
				comment = comment.replaceAll("#", "");

				comment = comment.replaceAll("\\n", "");

				comment = comment.trim();

				object.put("comment", comment);
			}

			keyArray.put(object);
		}

		propertiesObject.put("keys", keyArray);

		System.out.println(propertiesObject);
	}

	private static String _merge(String[] array) {
		if (array == null) {
			return null;
		}

		if (array.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder(2 * array.length - 1);

		for (int i = 0; i < array.length; i++) {
			if (i != 0) {
				sb.append(",");
			}

			sb.append(array[i]);
		}

		return sb.toString();
	}

}
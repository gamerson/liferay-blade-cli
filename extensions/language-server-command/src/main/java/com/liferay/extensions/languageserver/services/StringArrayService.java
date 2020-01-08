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

package com.liferay.extensions.languageserver.services;

import java.io.File;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * @author Terry Jia
 */
public class StringArrayService extends Service {

	public StringArrayService(File file, String[] possibleValues) {
		super(file);

		_possibleValues = possibleValues;
	}

	@Override
	public String[] getPossibleValues() {
		return _possibleValues;
	}

	@Override
	public void validate(String value) throws Exception {
		for (String possibleValue : _possibleValues) {
			possibleValue = StringEscapeUtils.unescapeJava(possibleValue);

			if (possibleValue.equals(value)) {
				return;
			}
		}

		throw new Exception("\"" + value + "\" is not in possible values.");
	}

	private String[] _possibleValues;

}
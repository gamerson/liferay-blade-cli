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

import java.util.Map;

/**
 * @author Lovett Li
 * @author Vernon Singleton
 */
public class GradleDependency {

	public GradleDependency(Map<String, String> dep) {
		setGroup(dep.get("group"));
		setName(dep.get("name"));
		setVersion(dep.get("version"));
	}

	public GradleDependency(String group, String name, String version) {
		_group = group;
		_name = name;
		_version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		GradleDependency other = (GradleDependency)obj;

		if (_configuration == null) {
			if (other._configuration != null) {
				return false;
			}
		}
		else if (!_configuration.equals(other._configuration)) {
			return false;
		}

		if (_group == null) {
			if (other._group != null) {
				return false;
			}
		}
		else if (!_group.equals(other._group)) {
			return false;
		}

		if (_name == null) {
			if (other._name != null) {
				return false;
			}
		}
		else if (!_name.equals(other._name)) {
			return false;
		}

		if (_version == null) {
			if (other._version != null) {
				return false;
			}
		}
		else if (!_version.equals(other._version)) {
			return false;
		}

		return true;
	}

	public String getConfiguration() {
		return _configuration;
	}

	public String getGroup() {
		return _group;
	}

	public String getName() {
		return _name;
	}

	public String getVersion() {
		return _version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_group == null) ? 0 : _group.hashCode());
		result = prime * result + ((_name == null) ? 0 : _name.hashCode());
		result = prime * result + ((_version == null) ? 0 : _version.hashCode());

		return result;
	}

	public void setConfiguration(String configuration) {
		_configuration = configuration;
	}

	public void setGroup(String group) {
		_group = group;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setVersion(String version) {
		_version = version;
	}

	private String _configuration;
	private String _group;
	private String _name;
	private String _version;

}
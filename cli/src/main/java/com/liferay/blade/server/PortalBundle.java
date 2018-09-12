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

package com.liferay.blade.server;

import java.net.URL;

import java.nio.file.Path;

/**
 * @author Simon Jiang
 */
public interface PortalBundle {

	public Path getApperServerLog();

	public Path getAppServerDir();

	public Path getAppServerPortalDir();

	public Path getBundleHome();

	public Path getBundleLog();

	public URL getLiferayHomeUrl() throws Exception;

	public String getType();

}
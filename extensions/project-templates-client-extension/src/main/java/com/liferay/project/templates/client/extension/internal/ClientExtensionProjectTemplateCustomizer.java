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

package com.liferay.project.templates.client.extension.internal;

import java.io.File;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;

import com.liferay.project.templates.extensions.ProjectTemplateCustomizer;
import com.liferay.project.templates.extensions.ProjectTemplatesArgs;

/**
 * @author Gregory Amerson
 */
public class ClientExtensionProjectTemplateCustomizer implements ProjectTemplateCustomizer {

	@Override
	public String getTemplateName() {
		return "client-extension";
	}

	@Override
	public void onAfterGenerateProject(
			ProjectTemplatesArgs projectTemplatesArgs, File destinationDir,
			ArchetypeGenerationResult archetypeGenerationResult)
		throws Exception {
	}

	@Override
	public void onBeforeGenerateProject(
			ProjectTemplatesArgs projectTemplatesArgs, ArchetypeGenerationRequest archetypeGenerationRequest)
		throws Exception {

		File destinationDir = projectTemplatesArgs.getDestinationDir();

		String projectName = projectTemplatesArgs.getName();

		LXCUtil.run(destinationDir.toPath(), new String[] {"generate", "-n", projectName}, false);
	}

}
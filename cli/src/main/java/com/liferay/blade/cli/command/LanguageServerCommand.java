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

package com.liferay.blade.cli.command;

import com.liferay.blade.cli.languageserver.LiferayLanguageServer;

import java.util.concurrent.Future;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * @author Terry Jia
 */
public class LanguageServerCommand extends BaseCommand<LanguageServerArgs> {

	public LanguageServerCommand() {
	}

	@Override
	public void execute() throws Exception {
		LiferayLanguageServer liferayLanguageServer = new LiferayLanguageServer();

		Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(
			liferayLanguageServer, System.in, System.out);

		Future<?> startListening = launcher.startListening();

		liferayLanguageServer.setRemoteProxy(launcher.getRemoteProxy());

		startListening.get();
	}

	@Override
	public Class<LanguageServerArgs> getArgsClass() {
		return LanguageServerArgs.class;
	}

}
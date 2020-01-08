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

package com.liferay.extensions.languageserver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.SaveOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * @author Terry Jia
 */
public class LiferayLanguageServer implements LanguageServer {

	public LiferayLanguageServer() {
		_textDocumentService = new LiferayTextDocumentService(this);
		_workspaceService = new LiferayWorkspaceService();
	}

	public void exit() {
	}

	public LanguageClient getLanguageClient() {
		return _languageClient;
	}

	public TextDocumentService getTextDocumentService() {
		return _textDocumentService;
	}

	public WorkspaceService getWorkspaceService() {
		return _workspaceService;
	}

	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		ServerCapabilities serverCapabilities = new ServerCapabilities();

		TextDocumentSyncOptions textDocumentSyncOptions = new TextDocumentSyncOptions();

		textDocumentSyncOptions.setChange(TextDocumentSyncKind.Full);
		textDocumentSyncOptions.setOpenClose(false);
		textDocumentSyncOptions.setSave(new SaveOptions(true));
		textDocumentSyncOptions.setWillSave(false);
		textDocumentSyncOptions.setWillSaveWaitUntil(false);

		serverCapabilities.setTextDocumentSync(textDocumentSyncOptions);

		InitializeResult initializeResult = new InitializeResult(serverCapabilities);

		ServerCapabilities capabilities = initializeResult.getCapabilities();

		CompletionOptions completionOptions = new CompletionOptions();

		List<String> triggerCharacters = Arrays.asList("=", ",");

		completionOptions.setTriggerCharacters(triggerCharacters);

		capabilities.setCompletionProvider(completionOptions);

		return CompletableFuture.supplyAsync(() -> initializeResult);
	}

	public void setRemoteProxy(LanguageClient languageClient) {
		_languageClient = languageClient;
	}

	public CompletableFuture<Object> shutdown() {
		return CompletableFuture.supplyAsync(() -> Boolean.TRUE);
	}

	private LanguageClient _languageClient;
	private TextDocumentService _textDocumentService;
	private WorkspaceService _workspaceService;

}
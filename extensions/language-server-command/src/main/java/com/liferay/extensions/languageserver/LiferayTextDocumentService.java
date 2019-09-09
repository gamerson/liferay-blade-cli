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

import com.liferay.extensions.languageserver.completions.PropertiesCompletion;
import com.liferay.extensions.languageserver.diagnostic.PropertiesDiagnostic;

import java.io.File;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

/**
 * @author Terry Jia
 */
public class LiferayTextDocumentService implements TextDocumentService {

	public LiferayTextDocumentService(LiferayLanguageServer liferayLanguageServer) {
		_liferayLanguageServer = liferayLanguageServer;
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
		CompletionParams completionParams) {

		PropertiesCompletion propertiesCompletion = new PropertiesCompletion(completionParams);

		List<CompletionItem> completionItems = propertiesCompletion.getCompletions();

		return CompletableFuture.supplyAsync(() -> Either.forLeft(completionItems));
	}

	@Override
	public void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {
	}

	@Override
	public void didClose(DidCloseTextDocumentParams didCloseTextDocumentParams) {
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams didOpenTextDocumentParams) {
		TextDocumentItem textDocument = didOpenTextDocumentParams.getTextDocument();

		try {
			URI uri = new URI(textDocument.getUri());

			File file = new File(uri);

			PropertiesDiagnostic propertiesDiagnostic = new PropertiesDiagnostic(file);

			List<Diagnostic> diagnostics = propertiesDiagnostic.validate();

			LanguageClient client = _liferayLanguageServer.getLanguageClient();

			client.publishDiagnostics(new PublishDiagnosticsParams(textDocument.getUri(), diagnostics));
		}
		catch (URISyntaxException urise) {
		}
	}

	@Override
	public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {
		TextDocumentIdentifier textDocument = didSaveTextDocumentParams.getTextDocument();

		try {
			URI uri = new URI(textDocument.getUri());

			File file = new File(uri);

			PropertiesDiagnostic propertiesDiagnostic = new PropertiesDiagnostic(file);

			List<Diagnostic> diagnostics = propertiesDiagnostic.validate();

			LanguageClient client = _liferayLanguageServer.getLanguageClient();

			client.publishDiagnostics(new PublishDiagnosticsParams(textDocument.getUri(), diagnostics));
		}
		catch (URISyntaxException urise) {
		}
	}

	private LiferayLanguageServer _liferayLanguageServer;

}
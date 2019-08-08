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

import java.io.File;
import java.io.InputStream;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

/**
 * @author Terry Jia
 */
public class LiferayTextDocumentService implements TextDocumentService {

	public LiferayTextDocumentService(LiferayLanguageServer liferayLanguageServer) {
	}


	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
		CompletionParams completionParams) {

		TextDocumentIdentifier textDocument = completionParams.getTextDocument();

		List<CompletionItem> completionItems = new ArrayList<>();

		try {
			URI uri = new URI(textDocument.getUri());

			File file = new File(uri);

			String fileName = file.getName();

			if (fileName.startsWith("portal") && fileName.endsWith("properties")) {
				Class<?> clazz = getClass();

				try (InputStream in = clazz.getResourceAsStream("/portal.properties");){
					Properties properties = new Properties();

					properties.load(in);

					for (Object key : properties.keySet()) {
						CompletionItem completionItem = new CompletionItem(key.toString());

						completionItem.setKind(CompletionItemKind.Property);

						completionItems.add(completionItem);
					}
				}
				catch (Exception ioe) {
				}
			}
		}
		catch (URISyntaxException urise) {
		}

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
	}

	@Override
	public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {
	}

}
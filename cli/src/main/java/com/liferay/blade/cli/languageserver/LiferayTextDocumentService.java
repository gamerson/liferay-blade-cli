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

package com.liferay.blade.cli.languageserver;

import com.liferay.blade.cli.languageserver.properties.PortalPropertiesConfiguration;
import com.liferay.blade.cli.languageserver.properties.PropKey;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfigurationLayout;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
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

				InputStream in = null;

				try {
					in = clazz.getResourceAsStream("/portal.properties");

					PropKey[] keys = _parseKeys(in);

					for (PropKey key : keys) {
						CompletionItem completionItem = new CompletionItem(key.getKey());

						completionItem.setDetail(key.getComment());

						completionItem.setKind(CompletionItemKind.Property);

						completionItems.add(completionItem);
					}
				}
				catch (IOException ioe) {
				}
				catch (ConfigurationException ce) {
				}
				finally {
					if (in != null) {
						try {
							in.close();
						}
						catch (IOException ioe) {
						}
					}
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

	private PropKey[] _parseKeys(InputStream inputStream) throws ConfigurationException, IOException {
		List<PropKey> parsed = new ArrayList<>();

		PortalPropertiesConfiguration config = new PortalPropertiesConfiguration();

		try {
			config.load(inputStream);
		}
		finally {
			inputStream.close();
		}

		Iterator<?> keys = config.getKeys();
		PropertiesConfigurationLayout layout = config.getLayout();

		while (keys.hasNext()) {
			Object o = keys.next();

			String key = o.toString();

			String comment = layout.getComment(key);

			parsed.add(new PropKey(key, (comment == null) ? null : comment.replaceAll("\n", "\n<br/>")));
		}

		PropKey[] parsedKeys = parsed.toArray(new PropKey[0]);

		Arrays.sort(
			parsedKeys,
			new Comparator<PropKey>() {

				@Override
				public int compare(PropKey o1, PropKey o2) {
					String o1key = o1.getKey();

					return o1key.compareTo(o2.getKey());
				}

			});

		return parsedKeys;
	}

}
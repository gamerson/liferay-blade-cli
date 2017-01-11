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

package com.liferay.blade.eclipse.provider;

import com.liferay.blade.api.SearchResult;
import com.liferay.blade.api.XMLFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.NodeList;

/**
 * @author Gregory Amerson
 * @author Andy Wu
 */
@Component(property = "file.extension=xml")
public class SSEXMLFile extends WorkspaceFile implements XMLFile {

	@Override
	public List<SearchResult> findElement(String tagName, String value) {
		List<SearchResult> _results = new ArrayList<>();

		IFile xmlFile = getIFile(_file);
		IDOMModel domModel = null;

		try {
			domModel = (IDOMModel) StructuredModelManager.getModelManager().getModelForRead(xmlFile);

			IDOMDocument document = domModel.getDocument();

			NodeList elements = document.getElementsByTagName(tagName);

			if (elements != null) {
				for (int i = 0; i < elements.getLength(); i++) {
					IDOMElement element = (IDOMElement) elements.item(i);

					String textContent = element.getTextContent();

					if (textContent != null && value.trim().equals(textContent.trim())) {

						int startOffset = element.getStartOffset();
						int endOffset = element.getEndOffset();
						int startLine = document.getStructuredDocument().getLineOfOffset(startOffset) + 1;
						int endLine = document.getStructuredDocument().getLineOfOffset(endOffset) + 1;

						SearchResult result = new SearchResult(_file, "startOffset:" + startOffset, startOffset,
								endOffset, startLine, endLine, true);

						_results.add(result);
					}
				}
			}

		} catch (Exception e) {
		}
		finally {
			if (domModel != null) {
				domModel.releaseFromRead();
			}
		}

		return _results;
	}

	@Override
	public Collection<SearchResult> searchChildren(String parentNodeName,
			Map<String, String> childrenNodeNameValueMap) {

		final List<SearchResult> results = new ArrayList<>();

		final IFile xmlFile = getIFile(_file);
		IDOMModel domModel = null;

		try {
			domModel = (IDOMModel) StructuredModelManager.getModelManager().getModelForRead(xmlFile);

			final IDOMDocument document = domModel.getDocument();

			final NodeList elements = document.getElementsByTagName(parentNodeName);

			if (elements == null || elements.getLength() == 0)
				return results;

			for (int i = 0; i < elements.getLength(); i++) {
				final IDOMElement element = (IDOMElement) elements.item(i);

				final Map<String, NodeList> nodeLists = new HashMap<String, NodeList>();

				final Set<String> keys = childrenNodeNameValueMap.keySet();

				boolean fullMatch = true;

				for (String key : keys) {
					final NodeList nodeList = element.getElementsByTagName(key);

					if (nodeList != null && nodeList.getLength() == 1) {
						nodeLists.put(key, nodeList);

						final IDOMElement node = (IDOMElement) nodeList.item(0);

						final String nodeContent = node.getTextContent();
						final String exceptedContent = childrenNodeNameValueMap.get(key);

						if (!exceptedContent.equals(nodeContent)) {
							fullMatch = false;
						}
					}
				}

				if (fullMatch) {
					int startOffset = element.getStartOffset();
					int endOffset = element.getEndOffset();
					int startLine = document.getStructuredDocument().getLineOfOffset(startOffset) + 1;
					int endLine = document.getStructuredDocument().getLineOfOffset(endOffset) + 1;

					SearchResult result = new SearchResult(_file, "startOffset:" + startOffset, startOffset,
							endOffset, startLine, endLine, true);

					results.add(result);
				}
			}
		}
		catch (Exception e) {
		}
		finally {
			if (domModel != null) {
				domModel.releaseFromRead();
			}
		}

		return results;
	}

}
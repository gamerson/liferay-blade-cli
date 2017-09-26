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

package com.liferay.blade.eclipse.provider.conditions;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class NotImplementsInterfaceCondition extends AbstractJDTCondition {

	public NotImplementsInterfaceCondition(String[] params) {
		super(params);

		_notImplementsInterface = getParams()[1];
	}

	@Override
	public boolean test() {

		getAst().accept(new ASTVisitor() {

			@Override
			public boolean visit(TypeDeclaration typeDeclaration) {
				ITypeBinding typeBind = typeDeclaration.resolveBinding();
				ITypeBinding[] interfaceBinds = typeBind.getInterfaces();

				for (ITypeBinding interfaceBind : interfaceBinds) {
					if (interfaceBind.getName().equals(_notImplementsInterface)) {
						_found = true;

						break;
					}
				}

				return false;
			}
		});

		return !_found;
	}

	private boolean _found = false;
	private String _notImplementsInterface;

}

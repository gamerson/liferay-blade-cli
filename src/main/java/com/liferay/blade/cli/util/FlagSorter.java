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

package com.liferay.blade.cli.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Christopher Bryan Boyd
 */
public class FlagSorter {

	public static void sort(List<String> flags) {
		Collection<String> addLast = new ArrayList<>();

		for (int x = 0; x < flags.size(); x++) {
			String s = flags.get(x);

			if (s.equals("--base") || s.equals("--working-dir")) {
				addLast.add(flags.remove(x));
				addLast.add(flags.remove(x));
			}
			else if (s.equals("--trace") || s.equals("--help")) {
				addLast.add(flags.remove(x));
			}
		}

		flags.addAll(addLast);
	}

}
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Gregory Amerson
 * @author Simon Jiang
 */
public class ListUtil {

	public static boolean contains(Collection<?> collection, Object o) {
		if ((collection == null) || (o == null)) {
			return false;
		}

		return collection.contains(o);
	}

	public static <E> List<E> fromArray(E[] array) {
		if (ArrayUtil.isEmpty(array)) {
			return new ArrayList<>();
		}

		return new ArrayList<>(Arrays.asList(array));
	}

	public static boolean isEmpty(Collection<?> collection) {
		if ((collection == null) || collection.isEmpty()) {
			return true;
		}

		return false;
	}

	public static boolean isEmpty(Object[] array) {
		if ((array == null) || (array.length == 0)) {
			return true;
		}

		return false;
	}

	public static boolean isNotEmpty(Collection<?> collection) {
		return !isEmpty(collection);
	}

	public static boolean isNotEmpty(Object[] array) {
		return !isEmpty(array);
	}

	public static boolean notContains(Collection<?> collection, Object o) {
		return !contains(collection, o);
	}

}
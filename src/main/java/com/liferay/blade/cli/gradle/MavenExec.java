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

package com.liferay.blade.cli.gradle;

import java.io.File;

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.Util;

/**
 * @author Christopher Bryan Boyd
 */
public class MavenExec {
	public MavenExec(BladeCLI blade) {
		_blade = blade;

		File mavenw = Util.getMavenWrapper(blade.getBase());

		if (mavenw != null) {
			try {
				_executable = mavenw.getCanonicalPath();
			}
			catch (Exception e) {
				blade.out("Could not find maven wrapper, using maven");

				_executable = "mvn";
			}
		}
		else {
			blade.out("Could not find maven wrapper, maven gradle");

			_executable = "mvn";
		}
	}

	public int executeMavenCommand(String cmd) throws Exception {
		Process process = Util.startProcess(_blade, "\"" + _executable + "\" " + cmd);

		return process.waitFor();
	}
	
	public Process executeMavenCommandAsync(String cmd) throws Exception {
		return Util.startProcessAsync(_blade, "\"" + _executable + "\" " + cmd);
	}

	private BladeCLI _blade;
	private String _executable;
}

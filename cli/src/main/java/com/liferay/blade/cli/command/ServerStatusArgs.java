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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * @author Simon Jiang
 */
@Parameters(commandDescription = "get server status", commandNames = "server status")
public class ServerStatusArgs extends BaseArgs {

	public int getTime() {
		return _time;
	}

	public void setBackground(int time) {
		_time = time;
	}

	@Parameter(description = "Set wait time, time unit is second., ", names = {"-t", "--time"})
	private int _time = 0;

}
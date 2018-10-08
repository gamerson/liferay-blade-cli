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

import com.liferay.blade.server.PortalBundle;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Simon Jiang
 */
public class ServerStatusCommand extends AbstractServerCommand<ServerStatusArgs> {

	public ServerStatusCommand() {
	}

	@Override
	public Class<ServerStatusArgs> getArgsClass() {
		return ServerStatusArgs.class;
	}

	public boolean getServerStatus() {
		return _serverStatus;
	}

	@Override
	protected void doServerCommand(PortalBundle portalBundle) throws Exception {
		_serverStatus = _verifyServerStatus(portalBundle.getLiferayHomeUrl());

		if (_serverStatus) {
			bladeCLI.out("Server is Started");
		}
		else {
			bladeCLI.out("Server is not Start");
		}
	}

	private boolean _verifyServerStatus(URL liferayHomeUrl) {
		try {
			URLConnection conn = liferayHomeUrl.openConnection();

			conn.setReadTimeout(_statusInterval);

			((HttpURLConnection)conn).setInstanceFollowRedirects(false);
			int code = ((HttpURLConnection)conn).getResponseCode();

			if (code == 200) {
				return true;
			}
		}
		catch (Exception e) {
			return false;
		}

		return false;
	}

	private static int _statusInterval = 250;

	private boolean _serverStatus = false;

}
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
		ServerStatusArgs serverStatusArgs = getArgs();

		int timeout = serverStatusArgs.getTime();

		if (timeout < (_defaultTimeout / 1000)) {
			_timeout = _defaultTimeout;
		}
		else {
			_timeout = timeout * 1000;
		}

		Thread t = new Thread("Liferay Blade Server Start Check Thread") {

			public void run() {
				try {
					_startedTime = System.currentTimeMillis();
					_serverStatus = _verifyServerStatus(portalBundle.getLiferayHomeUrl());

					if (_serverStatus) {
						bladeCLI.out("Server is Started");
					}
					else {
						bladeCLI.out("Server is not Start");
					}
				}
				catch (Exception e) {
					bladeCLI.out("Server is not Start");
				}
			}

		};

		t.start();
		t.join();
	}

	private boolean _verifyServerStatus(URL liferayHomeUrl) {
		boolean result = false;
		long currentTime = 0;

		while (!_stop) {
			try {
				currentTime = System.currentTimeMillis();

				if ((currentTime - _startedTime) > _timeout) {
					try {
						result = false;
					}
					catch (Exception e) {
					}

					_stop = true;

					break;
				}

				URLConnection conn = liferayHomeUrl.openConnection();

				conn.setReadTimeout(_statusInterval);

				((HttpURLConnection)conn).setInstanceFollowRedirects(false);
				int code = ((HttpURLConnection)conn).getResponseCode();

				if (!_stop && (code != 404)) {
					result = true;
					_stop = true;

					break;
				}

				Thread.sleep(_statusInterval);
			}
			catch (Exception e) {
				if (!_stop) {
					try {
						Thread.sleep(_statusInterval);
					}
					catch (InterruptedException ie) {
					}
				}
			}
		}

		return result;
	}

	private static int _statusInterval = 100;

	private long _defaultTimeout = 5 * 1000;
	private boolean _serverStatus = false;
	private long _startedTime;
	private boolean _stop = false;
	private long _timeout;

}
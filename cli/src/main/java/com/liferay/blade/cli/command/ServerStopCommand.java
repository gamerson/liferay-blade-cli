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

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.util.BladeUtil;
import com.liferay.blade.cli.util.FileUtil;
import com.liferay.blade.cli.util.ServerUtil;
import com.liferay.blade.server.PortalBundle;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.zeroturnaround.process.PidProcess;
import org.zeroturnaround.process.Processes;

/**
 * @author David Truong
 * @author Simon Jiang
 */
public class ServerStopCommand extends AbstractServerCommand<ServerStopArgs> {

	public ServerStopCommand() {
	}

	@Override
	public Class<ServerStopArgs> getArgsClass() {
		return ServerStopArgs.class;
	}

	@Override
	protected void doServerCommand(PortalBundle portalBundle) throws Exception {
		if (portalBundle != null) {
			String serverType = portalBundle.getType();

			_commandServer(portalBundle.getBundleHome(), serverType);
		}
	}

	private void _commandServer(Path dir, String serverType) throws Exception {
		BladeCLI bladeCLI = getBladeCLI();

		if (Files.notExists(dir) || BladeUtil.isDirEmpty(dir)) {
			bladeCLI.error(
				" bundles folder does not exist in Liferay Workspace, execute 'gradlew initBundle' in order to " +
					"create it.");

			return;
		}

		Optional<Path> server = ServerUtil.findServerFolder(dir, serverType);

		boolean success = false;

		if (server.isPresent()) {
			Path file = server.get();

			if (serverType.equals("tomcat")) {
				_commmandTomcat(file);

				success = true;
			}
			else if (serverType.equals("jboss") || serverType.equals("wildfly")) {
				_commmandJBossWildfly(file);

				success = true;
			}
		}

		if (!success) {
			bladeCLI.error(serverType + " not supported");
		}
	}

	private void _commmandJBossWildfly(Path dir) throws Exception {
		Map<String, String> enviroment = new HashMap<>();
		BladeCLI bladeCLI = getBladeCLI();

		Path binPath = dir.resolve("bin");

		String executable = ServerUtil.getJBossWildflyStopExecutable();

		String stopCommand = " --connect --command=shutdown";

		Process process = BladeUtil.startProcess(executable + stopCommand, binPath.toFile(), enviroment);

		process.waitFor();

		try {
			Path pidPath = binPath.resolve("jboss.pid");

			if (FileUtil.exists(pidPath)) {
				String pidString = FileUtil.readContents(pidPath.toFile(), false);

				int pid = Integer.parseInt(pidString);

				PidProcess pidProcess = Processes.newPidProcess(pid);

				pidProcess.waitFor(60, TimeUnit.SECONDS);

				pidProcess.destroyForcefully();
			}
		}
		catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			bladeCLI.error("JBoss/Wildfly failed to stop server, please check process pid");
		}
		catch (Exception e) {
			bladeCLI.error("JBoss/Wildfly failed to stop server.");
		}
	}

	private void _commmandTomcat(Path dir) throws Exception {
		Map<String, String> enviroment = new HashMap<>();

		enviroment.put("CATALINA_PID", "catalina.pid");

		String executable = ServerUtil.getTomcatExecutable();

		Path binPath = dir.resolve("bin");

		Process process = BladeUtil.startProcess(executable + " stop 60 -force", binPath.toFile(), enviroment);

		process.waitFor();
	}

}
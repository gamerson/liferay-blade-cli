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
import com.liferay.blade.cli.util.ServerUtil;
import com.liferay.blade.server.PortalBundle;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.zeroturnaround.exec.ProcessExecutor;

/**
 * @author David Truong
 * @author Simon Jiang
 */
public class ServerStartCommand extends AbstractServerCommand<ServerStartArgs> {

	public ServerStartCommand() {
	}

	@Override
	public Class<ServerStartArgs> getArgsClass() {
		return ServerStartArgs.class;
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

		Optional<Path> serverFolder = ServerUtil.findServerFolder(dir, serverType);

		boolean success = false;

		if (serverFolder.isPresent()) {
			Path file = serverFolder.get();

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
		BladeCLI bladeCLI = getBladeCLI();
		ServerStartArgs serverStartArgs = getArgs();

		Map<String, String> enviroment = new HashMap<>();

		String executable = ServerUtil.getJBossWildflyStartExecutable();

		final StringBuilder startCommand = new StringBuilder("");

		if (serverStartArgs.isDebug()) {
			startCommand.append(" --debug");

			if (!BladeUtil.isEmpty(serverStartArgs.getPort())) {
				startCommand.append(" " + serverStartArgs.getPort());
			}
		}

		Path binPath = dir.resolve("bin");

		if (!serverStartArgs.isBackground()) {
			Process process = BladeUtil.startProcess(
				executable + startCommand.toString(), binPath.toFile(), enviroment, bladeCLI.out(), bladeCLI.err());

			_processes.add(process);

			Runtime runtime = Runtime.getRuntime();

			runtime.addShutdownHook(
				new Thread() {

					@Override
					public void run() {
						try {
							process.waitFor();
						}
						catch (InterruptedException ie) {
							bladeCLI.error("Could not wait for process to end before shutting down");
						}
					}

				});
		}
		else {
			if (!serverStartArgs.isCommandLine()) {
				enviroment.put("JBOSS_PIDFILE", "jboss.pid");
				enviroment.put("LAUNCH_JBOSS_IN_BACKGROUND", "1");

				List<String> commands = new ArrayList<>();

				if (BladeUtil.isWindows()) {
					commands.add("cmd.exe");
					commands.add("/c");
				}
				else {
					commands.add("sh");
					commands.add("-c");
					commands.add(executable);
					commands.add(startCommand.toString());
				}

				ProcessExecutor processExecutor = new ProcessExecutor();

				processExecutor.environment(enviroment);
				processExecutor.directory(binPath.toFile());
				processExecutor.command(commands);
				processExecutor.redirectErrorStream(true);
				processExecutor.readOutput(true);
				processExecutor.redirectOutput(null);
				processExecutor.redirectError(null);
				processExecutor.start();
			}
			else {
				bladeCLI.error(
					"Blade does not support start Jboss or Wildfly bundle with backgroud model from command line. ");
			}
		}
	}

	private void _commmandTomcat(Path dir) throws Exception {
		BladeCLI bladeCLI = getBladeCLI();
		ServerStartArgs serverStartArgs = getArgs();

		Map<String, String> enviroment = new HashMap<>();

		enviroment.put("CATALINA_PID", "catalina.pid");

		String executable = ServerUtil.getTomcatExecutable();

		String startCommand = " run";

		if (serverStartArgs.isBackground()) {
			startCommand = " start";
		}

		if (serverStartArgs.isDebug()) {
			startCommand = " jpda " + startCommand;

			if (!BladeUtil.isEmpty(serverStartArgs.getPort())) {
				enviroment.put("JPDA_ADDRESS", "localhost:" + serverStartArgs.getPort());
			}
		}

		Path logsPath = dir.resolve("logs");

		if (!Files.exists(logsPath)) {
			Files.createDirectory(logsPath);
		}

		Path catalinaOutPath = logsPath.resolve("catalina.out");

		if (!Files.exists(catalinaOutPath)) {
			Files.createFile(catalinaOutPath);
		}

		Path binPath = dir.resolve("bin");

		final Process process = BladeUtil.startProcess(
			executable + startCommand, binPath.toFile(), enviroment, bladeCLI.out(), bladeCLI.err());

		_processes.add(process);

		Runtime runtime = Runtime.getRuntime();

		runtime.addShutdownHook(
			new Thread() {

				@Override
				public void run() {
					try {
						process.waitFor();
					}
					catch (InterruptedException ie) {
						bladeCLI.error("Could not wait for process to end before shutting down");
					}
				}

			});

		if (serverStartArgs.isBackground() && serverStartArgs.isTail()) {
			Process tailProcess = BladeUtil.startProcess("tail -f catalina.out", logsPath.toFile(), enviroment);

			_processes.add(tailProcess);

			tailProcess.waitFor();
		}
	}

	private Collection<Process> _processes = new HashSet<>();

}
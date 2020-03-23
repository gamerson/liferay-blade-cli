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

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Christopher Bryan Boyd
 */
public class ServerConfigCommand extends BaseCommand<ServerConfigArgs> {

	public ServerConfigCommand() {
	}

	@Override
	public void execute() throws Exception {
		BladeCLI bladeCLI = getBladeCLI();

		LocalServer localServer = newLocalServer(bladeCLI);

		Path liferayHomePath = localServer.getLiferayHomePath();

		if (Files.notExists(liferayHomePath) || BladeUtil.isDirEmpty(liferayHomePath)) {
			bladeCLI.error("Liferay home directory does not exist. Execute 'blade server init' to create it.");

			return;
		}

		String serverType = localServer.getServerType();

		if (!localServer.isSupported()) {
			bladeCLI.error(serverType + " not supported");

			return;
		}

		ServerConfigArgs args = getArgs();

		Optional<Path> optionalAppServerPath = localServer.getAppServerPath();

		if (optionalAppServerPath.isPresent()) {
			Path appServerPath = optionalAppServerPath.get();
			int debugPort = getArgs().getDebugPort();
			String desiredString = null;
			Path appServerPathBin = null;
			String debugPortString = null;
			Collection<String> lines = null;
			Collection<String> linesNew = new ArrayList<>();
			boolean windows = BladeUtil.isWindows();

			boolean suspend = args.isSuspend();

			boolean debug = args.isDebug();

			String suspendValue = suspend ? "y" : "n";

			if (serverType.equals("tomcat")) {
				if (debugPort == -1) {
					debugPort = 8000;
				}

				debugPortString = String.valueOf(debugPort);

				if (windows) {
					desiredString = "set \"JAVA_OPTS=%JAVA_OPTS% %JSSE_OPTS%\"";
					appServerPathBin = appServerPath.resolve("bin/catalina.bat");
				}
				else {
					desiredString = "JAVA_OPTS=\"$JAVA_OPTS $JSSE_OPTS\"";
					appServerPathBin = appServerPath.resolve("bin/catalina.sh");
				}

				lines = Files.readAllLines(appServerPathBin);

				if (debug || suspend) {
					for (String line : lines) {
						line = StringUtils.stripEnd(line, " ");

						String tomcatDebugLine = " -agentlib:jdwp=transport=dt_socket,address=";

						tomcatDebugLine += debugPortString + ",server=y,suspend=" + suspendValue + "\"";

						if (line.equals(desiredString)) {
							line = line.substring(0, line.length() - 1) + tomcatDebugLine;
						}
						else if (line.contains(desiredString)) {
							line = desiredString.substring(0, desiredString.length() - 1) + tomcatDebugLine;
						}

						linesNew.add(line);
					}
				}

				else
				{
					for (String line : lines) {
						line = StringUtils.stripEnd(line, " ");

						if (line.contains(desiredString) && !line.equals(desiredString)) {
							line = desiredString;
						}

						linesNew.add(line);
					}
				}
			}
			else if (serverType.equals("jboss") || serverType.equals("wildfly")) {
				if (debugPort == -1) {
					debugPort = 8787;
				}

				debugPortString = String.valueOf(debugPort);

				if (windows) {
					desiredString = "  \"%JAVA%\" %JAVA_OPTS% ^";
					appServerPathBin = appServerPath.resolve("bin/standalone.bat");
				}
				else {
					desiredString = "      eval \\\"$JAVA\\\" -D\\\"[Standalone]\\\" $JAVA_OPTS \\";
					appServerPathBin = appServerPath.resolve("bin/standalone.sh");
				}

				lines = Files.readAllLines(appServerPathBin);

				if (debug || suspend) {
					for (String line : lines) {
						line = StringUtils.stripEnd(line, " ");
						String desiredStringTrimmed = StringUtils.stripEnd(
							desiredString.substring(0, desiredString.length() - 1), " ");

						String wildflyLineWindows = " -Xrunjdwp:transport=dt_socket,address=";

						wildflyLineWindows += debugPortString + ",server=y,suspend=" + suspendValue + " ^";

						String wildflyLineLinux = " -Xrunjdwp:transport=dt_socket,address=";

						wildflyLineLinux += debugPortString + ",server=y,suspend=" + suspendValue + " \\";

						if (line.equals(desiredString)) {
							if (windows) {
								line = line.substring(0, line.length() - 1) + wildflyLineWindows;
							}
							else {
								line = line.substring(0, line.length() - 1) + wildflyLineLinux;
							}
						}
						else if (line.contains(desiredStringTrimmed)) {
							if (windows) {
								line = desiredStringTrimmed + wildflyLineWindows;
							}
							else {
								line = desiredStringTrimmed + wildflyLineLinux;
							}
						}

						linesNew.add(line);
					}
				}

				else
				{
					for (String line : lines) {
						line = StringUtils.stripEnd(line, " ");

						if (line.contains(desiredString) && !line.equals(desiredString)) {
							line = desiredString;
						}

						linesNew.add(line);
					}
				}
			}

			Files.write(appServerPathBin, linesNew, Charset.defaultCharset());
		}
	}

	@Override
	public Class<ServerConfigArgs> getArgsClass() {
		return ServerConfigArgs.class;
	}

	protected LocalServer newLocalServer(BladeCLI bladeCLI) {
		return new LocalServer(bladeCLI);
	}

}
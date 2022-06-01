package com.liferay.project.templates.client.extension.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.liferay.blade.cli.util.BladeUtil;
import com.liferay.blade.cli.util.FileUtil;
import com.liferay.blade.cli.util.OSDetector;

public class LXCUtil {

	public static int run(Path dir, String[] args, boolean quiet) throws Exception {
		Path lxcPath = downloadLxc();
		
		ProcessBuilder processBuilder = new ProcessBuilder();

		processBuilder.directory(dir.toFile());

		List<String> commands = new ArrayList<>();

		if (OSDetector.isWindows()) {
			commands.add("cmd.exe");
			commands.add("/c");
			commands.add(lxcPath.toString());

			for (String arg : args) {
				commands.add(arg);
			}
		}
		else {
			commands.add("sh");
			commands.add("-c");

			StringBuilder command = new StringBuilder();

			command.append("\"");
			command.append(lxcPath.toString());
			command.append("\" ");

			for (String arg : args) {
				command.append("\"");
				command.append(arg);
				command.append("\" ");
			}

			commands.add(command.toString());
		}

		processBuilder.command(commands);

		if (!quiet) {
			processBuilder.inheritIO();
		}

		if ((dir != null) && Files.exists(dir)) {
			processBuilder.directory(dir.toFile());
		}

		Process process = processBuilder.start();

		OutputStream outputStream = process.getOutputStream();

		outputStream.close();

		return process.waitFor();
	}
	
	public static Path downloadLxc() throws IOException {
		Path bladeCachePath = BladeUtil.getBladeCachePath();

		Path lxcDirPath = bladeCachePath.resolve("lxc");

		if (!Files.exists(lxcDirPath) || !_containsFiles(lxcDirPath)) {
			Files.createDirectories(lxcDirPath);

			String lxcURL = _getLxcURL();

			Path downloadPath = bladeCachePath.resolve(lxcURL.substring(lxcURL.lastIndexOf("/") + 1));

			if (!Files.exists(downloadPath)) {
				BladeUtil.downloadLink(lxcURL, lxcDirPath.toFile(), downloadPath);
			}

			FileUtil.unpack(downloadPath, lxcDirPath, 1);

			if (OSDetector.isWindows()) {
				Path nodePath;

				try (Stream<Path> paths = Files.list(lxcDirPath)) {
					nodePath = paths.findFirst(
					).get();
				}

				try (Stream<Path> nodePaths = Files.list(nodePath)) {
					nodePaths.forEach(
						x -> {
							try {
								Files.move(
									x, lxcDirPath.resolve(x.getFileName()), StandardCopyOption.REPLACE_EXISTING);
							}
							catch (IOException ioException) {
								throw new RuntimeException(ioException);
							}
						});
				}

				Files.delete(nodePath);
			}
			else {
				Files.setPosixFilePermissions(
						lxcDirPath.resolve("lxc"), PosixFilePermissions.fromString("rwxrwxr--"));
				Files.setPosixFilePermissions(
						lxcDirPath.resolve("lxc"), PosixFilePermissions.fromString("rwxrwxr--"));
			}
		}

		return lxcDirPath.resolve("lxc");
	}
	
	private static String _lxcVersion = "0.0.3";
	
	private static String _getLxcURL() {
		StringBuilder sb = new StringBuilder();

		//https://github.com/ipeychev/lxc-cli-release/releases/download/0.0.1/lxc-linux.tgz
		sb.append("https://github.com/ipeychev/lxc-cli-release/releases/download/");
		sb.append(_lxcVersion);
		sb.append("/lxc-");

		String os = "linux";

		if (OSDetector.isApple()) {
			os = "macos";
		}
		else if (OSDetector.isWindows()) {
			os = "win";
		}

		sb.append(os);
		sb.append(".tgz");

		System.out.println(sb.toString());
		return sb.toString();
	}
	
	private static boolean _containsFiles(Path path) throws IOException {
		try (Stream<Path> files = Files.list(path)) {
			if (files.count() > 0) {
				return true;
			}

			return false;
		}
	}
}

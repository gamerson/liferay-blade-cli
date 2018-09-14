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

import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Processor;
import aQute.bnd.osgi.Resource;

import aQute.lib.io.IO;

import com.liferay.blade.cli.BladeCLI;
import com.liferay.project.templates.ProjectTemplates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author Gregory Amerson
 * @author David Truong
 */
public class BladeUtil {

	public static final String APP_SERVER_PARENT_DIR_PROPERTY = "app.server.parent.dir";

	public static final String APP_SERVER_TYPE_PROPERTY = "app.server.type";

	public static boolean canConnect(String host, int port) {
		InetSocketAddress localAddress = new InetSocketAddress(0);
		InetSocketAddress remoteAddress = new InetSocketAddress(host, Integer.valueOf(port));

		return _canConnect(localAddress, remoteAddress);
	}

	public static void copy(InputStream in, File outputDir) throws Exception {
		try (Jar jar = new Jar("dot", in)) {
			Map<String, Resource> resources = jar.getResources();

			for (Entry<String, Resource> e : resources.entrySet()) {
				String path = e.getKey();

				Resource r = e.getValue();

				File dest = Processor.getFile(outputDir, path);

				if ((dest.lastModified() < r.lastModified()) || (r.lastModified() <= 0)) {
					File dp = dest.getParentFile();

					if (!dp.exists() && !dp.mkdirs()) {
						throw new Exception("Could not create directory " + dp);
					}

					IO.copy(r.openInputStream(), dest);
				}
			}
		}
	}

	public static void downloadGithubProject(String url, Path target) throws IOException {
		String zipUrl = url + "/archive/master.zip";

		downloadLink(zipUrl, target);
	}

	public static void downloadLink(String link, Path target) throws IOException {
		if (_isURLAvailable(link)) {
			LinkDownloader downloader = new LinkDownloader(link, target);

			downloader.run();
		}
		else {
			throw new RuntimeException("url '" + link + "' is not accessible.");
		}
	}

	public static File findParentFile(File dir, String[] fileNames, boolean checkParents) {
		return _findParentFile(dir, fileNames, checkParents, false, 1, 0);
	}

	public static File findParentFile(File dir, String[] fileNames, boolean checkParents, int maxDepth) {
		return _findParentFile(dir, fileNames, checkParents, false, maxDepth, 0);
	}

	public static File findParentFileRecursive(File dir, String[] fileNames, boolean checkParents) {
		return _findParentFile(dir, fileNames, checkParents, true, 1, 0);
	}

	public static File findParentFileRecursive(File dir, String[] fileNames, boolean checkParents, int maxDepth) {
		return _findParentFile(dir, fileNames, checkParents, true, maxDepth, 0);
	}

	public static List<Properties> getAppServerProperties(File dir) {
		return new ArrayList<>(getAppServerPropertiesMap(dir).values());
	}

	public static Map<File, Properties> getAppServerPropertiesMap(File dir) {
		File projectRoot = findParentFileRecursive(dir, _APP_SERVER_PROPERTIES_FILE_NAMES, true, 2);

		Map<File, Properties> properties = new HashMap<>();

		for (String fileName : _APP_SERVER_PROPERTIES_FILE_NAMES) {
			File file = new File(projectRoot, fileName);

			if (file.exists()) {
				properties.put(file, getProperties(file));
			}
		}

		return properties;
	}

	public static String getBundleVersion(Path pathToJar) throws IOException {
		return getManifestProperty(pathToJar, "Bundle-Version");
	}

	public static File getGradleWrapper(File dir) {
		File gradleRoot = findParentFile(
			dir, new String[] {_GRADLEW_UNIX_FILE_NAME, _GRADLEW_WINDOWS_FILE_NAME}, true, Integer.MAX_VALUE);

		if (gradleRoot != null) {
			if (isWindows()) {
				return new File(gradleRoot, _GRADLEW_WINDOWS_FILE_NAME);
			}
			else {
				return new File(gradleRoot, _GRADLEW_UNIX_FILE_NAME);
			}
		}

		return null;
	}

	public static String getManifestProperty(Path pathToJar, String propertyName) throws IOException {
		File file = pathToJar.toFile();

		try (JarFile jar = new JarFile(file)) {
			Manifest manifest = jar.getManifest();

			Attributes attributes = manifest.getMainAttributes();

			return attributes.getValue("Bundle-Version");
		}
	}

	public static Properties getProperties(File file) {
		try (InputStream inputStream = new FileInputStream(file)) {
			Properties properties = new Properties();

			properties.load(inputStream);

			return properties;
		}
		catch (Exception e) {
			return null;
		}
	}

	public static Optional<Path> getServerPathByType(Path path, String... types) {
		Optional<Path> serverPath;
		Iterator<Path> i = path.iterator();
		int serverPathIndex = -1;
		int x = 0;
		boolean absolutePath = path.isAbsolute();
		Optional<Path> rootPathOptional;

		if (absolutePath) {
			rootPathOptional = Optional.of(path.getRoot());
		}
		else {
			rootPathOptional = Optional.empty();
		}
		while (i.hasNext() && (serverPathIndex == -1)) {
			Path pathPart = i.next();

			String pathPartString = String.valueOf(pathPart);

			for (String type : types) {
				if (pathPartString.startsWith(type)) {
					serverPathIndex = x;

					break;
				}
			}

			x++;
		}

		if (serverPathIndex > -1) {
			Path subpath = path.subpath(0, x);

			if (absolutePath) {
				Path rootPath = rootPathOptional.get();

				serverPath = Optional.of(rootPath.resolve(subpath));
			}
			else {
				serverPath = Optional.of(subpath);
			}
		}
		else {
			serverPath = Optional.empty();
		}

		return serverPath;
	}

	public static Collection<String> getTemplateNames(BladeCLI blade) throws Exception {
		Map<String, String> templates = getTemplates(blade);

		return templates.keySet();
	}

	public static Map<String, String> getTemplates(BladeCLI bladeCLI) throws Exception {
		Path extensions = bladeCLI.getExtensionsPath();

		Collection<File> templatesFiles = new HashSet<>();

		templatesFiles.add(extensions.toFile());

		return ProjectTemplates.getTemplates(templatesFiles);
	}

	public static boolean hasGradleWrapper(File dir) {
		if (new File(dir, _GRADLEW_UNIX_FILE_NAME).exists() && new File(dir, _GRADLEW_WINDOWS_FILE_NAME).exists()) {
			return true;
		}
		else {
			File parent = dir.getParentFile();

			if ((parent != null) && parent.exists()) {
				return hasGradleWrapper(parent);
			}
		}

		return false;
	}

	public static boolean isDirEmpty(final Path directory) throws IOException {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
			Iterator<Path> iterator = directoryStream.iterator();

			return !iterator.hasNext();
		}
	}

	public static boolean isEmpty(List<?> list) {
		if ((list == null) || list.isEmpty()) {
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

	public static boolean isEmpty(String string) {
		if ((string == null) || string.isEmpty()) {
			return true;
		}

		return false;
	}

	public static boolean isNotEmpty(List<?> list) {
		return !isEmpty(list);
	}

	public static boolean isNotEmpty(Object[] array) {
		return !isEmpty(array);
	}

	public static boolean isWindows() {
		String osName = System.getProperty("os.name");

		osName = osName.toLowerCase();

		return osName.contains("windows");
	}

	public static boolean isZipValid(File file) {
		try (ZipFile zipFile = new ZipFile(file)) {
			return true;
		}
		catch (IOException ioe) {
			return false;
		}
	}

	public static String read(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()));
	}

	public static void readProcessStream(final InputStream inputStream, final PrintStream printStream) {
		Thread t = new Thread(
			new Runnable() {

				@Override
				public void run() {

					try (Scanner scanner = new Scanner(inputStream)) {
						while (scanner.hasNextLine()) {
							String line = scanner.nextLine();

							if (line != null) {
								AnsiLinePrinter.println(printStream, line);
							}
						}
					}
				}

			});

		t.start();
	}

	public static boolean searchZip(Path path, Predicate<String> test) {
		if (Files.exists(path) && !Files.isDirectory(path)) {
			try (ZipFile zipFile = new ZipFile(path.toFile())) {
				Stream<? extends ZipEntry> stream = zipFile.stream();

				Collection<ZipEntry> entryCollection = stream.collect(Collectors.toSet());

				for (ZipEntry zipEntry : entryCollection) {
					if (!zipEntry.isDirectory()) {
						String entryName = zipEntry.getName();

						if (test.test(entryName)) {
							return true;
						}
					}
				}

			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	public static void setShell(ProcessBuilder processBuilder, String cmd) {
		Map<String, String> env = processBuilder.environment();

		List<String> commands = new ArrayList<>();

		if (isWindows()) {
			commands.add("cmd.exe");
			commands.add("/c");
		}
		else {
			env.put("PATH", env.get("PATH") + ":/usr/local/bin");

			commands.add("sh");
			commands.add("-c");
		}

		commands.add(cmd);

		processBuilder.command(commands);
	}

	public static Process startProcess(String command, File workingDir) throws Exception {
		return startProcess(command, workingDir, null);
	}

	public static Process startProcess(String command, File dir, Map<String, String> environment) throws Exception {
		ProcessBuilder processBuilder = _buildProcessBuilder(command, dir, environment, true);

		Process process = processBuilder.start();

		OutputStream outputStream = process.getOutputStream();

		outputStream.close();

		return process;
	}

	public static Process startProcess(
			String command, File dir, Map<String, String> environment, PrintStream out, PrintStream err)
		throws Exception {

		ProcessBuilder processBuilder = _buildProcessBuilder(command, dir, environment, false);

		Process process = processBuilder.start();

		readProcessStream(process.getInputStream(), out);
		readProcessStream(process.getErrorStream(), err);

		OutputStream outputStream = process.getOutputStream();

		outputStream.close();

		return process;
	}

	public static Process startProcess(String command, File dir, PrintStream out, PrintStream err) throws Exception {
		return startProcess(command, dir, null, out, err);
	}

	public static void unzip(File srcFile, File destDir) throws IOException {
		unzip(srcFile, destDir, null);
	}

	public static void unzip(File srcFile, File destDir, String entryToStart) throws IOException {
		try (final ZipFile zip = new ZipFile(srcFile)) {
			final Enumeration<? extends ZipEntry> entries = zip.entries();

			boolean foundStartEntry = false;

			if (entryToStart == null) {
				foundStartEntry = true;
			}

			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement();

				String entryName = entry.getName();

				if (!foundStartEntry) {
					foundStartEntry = entryToStart.equals(entryName);
					continue;
				}

				if (entry.isDirectory() || ((entryToStart != null) && !entryName.startsWith(entryToStart))) {
					continue;
				}

				if (entryToStart != null) {
					entryName = entryName.replaceFirst(entryToStart, "");
				}

				final File f = new File(destDir, entryName);

				if (!_isSafelyRelative(f, destDir)) {
					throw new ZipException(
						"Entry " + f.getName() + " is outside of the target destination: " + destDir);
				}

				if (f.exists()) {
					IO.delete(f);

					if (f.exists()) {
						throw new IOException("Could not delete " + f.getAbsolutePath());
					}
				}

				final File dir = f.getParentFile();

				if (!dir.exists() && !dir.mkdirs()) {
					final String msg = "Could not create dir: " + dir.getPath();

					throw new IOException(msg);
				}

				try (final InputStream in = zip.getInputStream(entry);
					final FileOutputStream out = new FileOutputStream(f)) {

					final byte[] bytes = new byte[1024];

					int count = in.read(bytes);

					while (count != -1) {
						out.write(bytes, 0, count);
						count = in.read(bytes);
					}

					out.flush();
				}
			}
		}
	}

	private static ProcessBuilder _buildProcessBuilder(
		String command, File dir, Map<String, String> environment, boolean inheritIO) {

		ProcessBuilder processBuilder = new ProcessBuilder();

		Map<String, String> env = processBuilder.environment();

		if (environment != null) {
			env.putAll(environment);
		}

		if ((dir != null) && dir.exists()) {
			processBuilder.directory(dir);
		}

		setShell(processBuilder, command);

		if (inheritIO) {
			processBuilder.inheritIO();
		}

		return processBuilder;
	}

	private static boolean _canConnect(InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
		boolean connected = false;

		try (Socket socket = new Socket()) {
			socket.bind(localAddress);
			socket.connect(remoteAddress, 3000);
			socket.getInputStream();

			connected = true;
		}
		catch (IOException ioe) {
		}

		if (connected) {
			return true;
		}

		return false;
	}

	private static boolean _fileNamesMatch(String[] fileNames, Path path) {
		try (Stream<String> stream = Stream.of(fileNames)) {
			String pathString = String.valueOf(path);

			return stream.anyMatch(pathString::equals);
		}
	}

	private static File _findParentFile(
		File dir, String[] fileNames, boolean checkParents, boolean recursive, int maxDepth, int curDepth) {

		if ((dir == null) || !dir.exists() || _isRoot(dir.toPath())) {
			return null;
		}
		else if (".".equals(dir.toString()) || !dir.isAbsolute()) {
			try {
				dir = dir.getCanonicalFile();
			}
			catch (Exception e) {
				dir = dir.getAbsoluteFile();
			}
		}

		Path dirPath = dir.toPath();

		for (String fileName : fileNames) {
			File file = new File(dir, fileName);

			if (file.exists()) {
				return dir;
			}
		}

		try (Stream<Path> filePaths = Files.find(
				dirPath, recursive ? maxDepth : 1,
				(p, b) ->
					!Files.isDirectory(p) &&
					_fileNamesMatch(fileNames, p.getFileName()))) {

			Optional<Path> filePathOptional = filePaths.findFirst();

			if (filePathOptional.isPresent()) {
				Path filePath = filePathOptional.get();

				filePath = filePath.getParent();

				return filePath.toFile();
			}
		}
		catch (Throwable th) {
		}

		if (checkParents && (maxDepth > curDepth)) {
			return _findParentFile(dir.getParentFile(), fileNames, checkParents, recursive, maxDepth, curDepth++);
		}

		return null;
	}

	private static boolean _isRoot(Path path) {
		FileSystem fileSystem = FileSystems.getDefault();

		Iterable<Path> rootDirectoriesIterable = fileSystem.getRootDirectories();

		Iterator<Path> rootDirectories = rootDirectoriesIterable.iterator();

		Path root = rootDirectories.next();

		return Objects.equals(path, root);
	}

	private static boolean _isSafelyRelative(File file, File destDir) {
		Path destPath = destDir.toPath();

		destPath = destPath.toAbsolutePath();

		destPath = destPath.normalize();

		Path path = file.toPath();

		path = path.toAbsolutePath();

		path = path.normalize();

		return path.startsWith(destPath);
	}

	private static boolean _isURLAvailable(String urlString) throws IOException {
		URL url = new URL(urlString);

		HttpURLConnection.setFollowRedirects(false);

		HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

		httpURLConnection.setRequestMethod("HEAD");

		int responseCode = httpURLConnection.getResponseCode();

		if ((responseCode == HttpURLConnection.HTTP_OK) || (responseCode == HttpURLConnection.HTTP_MOVED_TEMP)) {
			return true;
		}

		return false;
	}

	private static final String[] _APP_SERVER_PROPERTIES_FILE_NAMES = {
		"app.server." + System.getProperty("user.name") + ".properties",
		"app.server." + System.getenv("COMPUTERNAME") + ".properties",
		"app.server." + System.getenv("HOST") + ".properties",
		"app.server." + System.getenv("HOSTNAME") + ".properties", "app.server.properties",
		"build." + System.getProperty("user.name") + ".properties",
		"build." + System.getenv("COMPUTERNAME") + ".properties", "build." + System.getenv("HOST") + ".properties",
		"build." + System.getenv("HOSTNAME") + ".properties", "build.properties"
	};

	private static final String _GRADLEW_UNIX_FILE_NAME = "gradlew";

	private static final String _GRADLEW_WINDOWS_FILE_NAME = "gradlew.bat";

}
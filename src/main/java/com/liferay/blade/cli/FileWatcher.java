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

package com.liferay.blade.cli;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;

import java.lang.reflect.Field;

import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Example to watch a directory (or tree) for changes to files.
 *
 * @author n/a
 */
public class FileWatcher {

	@SuppressWarnings("unchecked")
	public static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>)event;
	}

	public FileWatcher(Path baseDir, boolean recursive, Consumer<Path> consumer) throws IOException {
		this(baseDir, null, recursive, consumer);
	}

	/**
	 * Creates a WatchService and registers the given directory
	 * @param runnable
	 */
	public FileWatcher(Path baseDir, Path fileToWatch, boolean recursive, Consumer<Path> consumer) throws IOException {
		_watcher = FileSystems.getDefault().newWatchService();
		_keys = new HashMap<>();
		_recursive = recursive;

		System.out.format("Scanning %s\n", baseDir);

		if (recursive) {
			_registerAll(baseDir);
		}
		else {
			_register(baseDir);
		}

		// enable trace after initial registration

		_trace = true;

		processEvents(fileToWatch, consumer);
	}

	/**
	 * Process all events for keys queued to the watcher
	 * @param fileToWatch
	 * @param runnable
	 */
	public void processEvents(Path fileToWatch, Consumer<Path> consumer) {
		while (true) {

			// wait for key to be signalled

			WatchKey key;

			try {
				key = _watcher.take();
			}
			catch (InterruptedException ie) {
				return;
			}

			Path dir = _keys.get(key);

			if (dir == null) {
				System.err.println("WatchKey not recognized!!");
				continue;
			}

			final Set<Path> reportModified = new HashSet<>();

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled

				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry

				WatchEvent<Path> ev = cast(event);

				Path name = ev.context();

				Path child = dir.resolve(name);

				if ((child.equals(fileToWatch) || (fileToWatch == null)) &&
					((kind == ENTRY_CREATE) || (kind == ENTRY_MODIFY))) {

					reportModified.add(child);
				}

				// if directory is created, and watching recursively, then
				// register it and its sub-directories

				if (_recursive && (kind == ENTRY_CREATE)) {
					try {
						if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
							_registerAll(child);
						}
					}
					catch (IOException ioe) {

						// ignore to keep sample readbale

					}
				}
			}

			if (!reportModified.isEmpty()) {
				for (Path modified : reportModified) {
					try {
						consumer.consume(modified);
					}
					catch (Throwable t) {
						//ignore
					}
				}
			}

			// reset key and remove from set if directory no longer accessible

			boolean valid = key.reset();

			if (!valid) {
				_keys.remove(key);

				// all directories are inaccessible

				if (_keys.isEmpty()) {
					break;
				}
			}
		}
	}

	public interface Consumer<E> {

		public void consume(E reference);

	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void _register(Path dir) throws IOException {
		Modifier modifier = null;

		try {
			Class<?> c = Class.forName("com.sun.nio.file.SensitivityWatchEventModifier");

			Field f = c.getField("HIGH");

			modifier = (Modifier)f.get(c);
		}
		catch (Exception e) {
		}

		WatchKey key;

		if (modifier != null) {
			key = dir.register(_watcher, new WatchEvent.Kind[] {ENTRY_CREATE, ENTRY_MODIFY}, modifier);
		}
		else {
			key = dir.register(_watcher, ENTRY_CREATE, ENTRY_MODIFY);
		}

		if (_trace) {
			Path prev = _keys.get(key);

			if (prev == null) {
			}
			else {
				if (!dir.equals(prev)) {
				}
			}
		}

		_keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the WatchService.
	 */
	private void _registerAll(final Path start) throws IOException {

		// register directory and sub-directories

		Files.walkFileTree(
			start,
			new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					_register(dir);

					return FileVisitResult.CONTINUE;
				}

			});
	}

	private final Map<WatchKey, Path> _keys;
	private final boolean _recursive;
	private boolean _trace = false;
	private final WatchService _watcher;

}
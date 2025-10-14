package com.condation.cms.filesystem;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.condation.cms.api.utils.PathUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;

/**
 * The recursive file watcher monitors a folder (and its sub-folders).
 */
@Slf4j
public class MultiRootRecursiveWatcher {

	private final AtomicBoolean running;

	private WatchService watchService;
	private Thread watchThread;
	private final Map<Path, WatchKey> watchPathKeyMap;
	private final ReentrantLock mapLock = new ReentrantLock();

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> scheduledFuture;

	private final Map<Path, Root> roots;

	static record Root(Path path, SubmissionPublisher<FileEvent> publisher) {

	}

	public MultiRootRecursiveWatcher(List<Path> roots) {
		this.running = new AtomicBoolean(false);

		this.watchService = null;
		this.watchThread = null;
		this.watchPathKeyMap = new HashMap<>();

		this.roots = new HashMap<>();
		roots.forEach(path -> {
			this.roots.put(path, new Root(path, new SubmissionPublisher<>()));
		});
	}

	public SubmissionPublisher<FileEvent> getPublisher(Path root) {

		if (this.roots.containsKey(root)) {
			return this.roots.get(root).publisher;
		}

		return null;
	}

	/**
	 * Starts the watcher service and registers watches in all of the sub-folders of the given root folder.
	 *
	 * <p>
	 * <b>Important:</b> This method returns immediately, even though the watches might not be in place yet. For large
	 * file trees, it might take several seconds until all directories are being monitored. For normal cases (1-100
	 * folders), this should not take longer than a few milliseconds.
	 */
	public void start() throws IOException {
		watchService = FileSystems.getDefault().newWatchService();

		watchThread = Thread.ofVirtual().name("Watcher").start(() -> {
			running.set(true);
			walkTreeAndSetWatches();
			
			while (running.get()) {
				try {
					WatchKey watchKey = watchService.take();
					List<WatchEvent<?>> events = watchKey.pollEvents();

					for (WatchEvent<?> event : events) {
						WatchEvent.Kind<?> kind = event.kind();

						// TBD - might be a server overflow (Events cast)
						if (kind == OVERFLOW) {
							resetWaitSettlementTimer();
							continue;
						}

						Path path = (Path) watchKey.watchable();
						File file = path.resolve((Path) event.context()).toFile();

						final FileEvent fileEvent;
						if (event.kind().equals(ENTRY_CREATE)) {
							fileEvent = new FileEvent(file, FileEvent.Type.CREATED);
							if (file.isDirectory()) {
								walkTreeAndSetWatches(file.toPath());
							}
						} else if (event.kind().equals(ENTRY_DELETE)) {
							fileEvent = new FileEvent(file, FileEvent.Type.DELETED);
						} else if (event.kind().equals(ENTRY_MODIFY)) {
							fileEvent = new FileEvent(file, FileEvent.Type.MODIFIED);
						} else {
							fileEvent = null;
						}

						if (fileEvent != null) {
							roots.values().forEach((root) -> {
								if (PathUtil.isChild(root.path, file.toPath())) {
									root.publisher.submit(fileEvent);
								}
							});
						}
					}

					// fire events
					if (!watchKey.reset()) {
						Path path = (Path) watchKey.watchable();
						unregisterWatch(path);
					}
				} catch (InterruptedException | ClosedWatchServiceException e) {
					running.set(false);
				} catch (Exception e) {
					log.error("an error occured", e);
				}
			}
		});
	}

	public synchronized void stop() {
		if (watchThread != null) {
			try {
				roots.values().forEach(root -> root.publisher.close());
				watchService.close();
				running.set(false);
				watchThread.interrupt();
				scheduler.shutdown();
			} catch (IOException e) {
				log.error("could not close watch service", e);
			}
		}
	}

	private synchronized void resetWaitSettlementTimer() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(false);
		}

		scheduledFuture = scheduler.schedule(() -> {
			log.debug("File system actions (on watched folders) settled. Updating watches ...");
			walkTreeAndSetWatches();
		}, 10, TimeUnit.SECONDS);
	}

	private void walkTreeAndSetWatches() {
		log.debug("Registering new folders at watch service ...");

		roots.values().forEach(root -> {
			walkTreeAndSetWatches(root.path);
		});

	}

	private void walkTreeAndSetWatches(Path path) {
		try {
			Files.walkFileTree(path, new FileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					registerWatch(dir);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("could not walk file tree for path {}", path, e);
		}
	}

	private void registerWatch(Path dir) {
		mapLock.lock();
		try {
			if (!watchPathKeyMap.containsKey(dir)) {
				log.debug("- Registering " + dir);

				try {
					WatchKey watchKey = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW);
					watchPathKeyMap.put(dir, watchKey);
				} catch (IOException e) {
					log.error("could not register watch for path {}", dir, e);
				}
			}
		} finally {
			mapLock.unlock();
		}
	}

	private void unregisterWatch(Path dir) {
		mapLock.lock();
		try {
			WatchKey watchKey = watchPathKeyMap.get(dir);

			if (watchKey != null) {
				log.debug("- Cancelling " + dir);

				watchKey.cancel();
				watchPathKeyMap.remove(dir);
			}
		} finally {
			mapLock.unlock();
		}
	}

	public interface WatchListener {

		public void watchEventsOccurred();
	}

	public abstract static class AbstractFileEventSubscriber implements Flow.Subscriber<FileEvent> {

		protected Flow.Subscription subscription;

		@Override
		public void onSubscribe(Flow.Subscription subscription) {
			this.subscription = subscription;
			this.subscription.request(1);
		}

		@Override
		public void onError(Throwable throwable) {
		}

		@Override
		public void onComplete() {
		}
	}
}

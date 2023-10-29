package com.github.thmarx.cms.eventbus;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class EventBus {
	
	public final Multimap<Class<? extends Event>, EventListener> listeners;
	
	public EventBus () {
		listeners = ArrayListMultimap.create();
	}
	
	public <T extends Event> void register (Class<T> eventClass, EventListener<T> listener) {
		listeners.put(eventClass, listener);
	}
	
	public <T extends Event> void publish (final T event) {
		listeners.get(event.getClass()).forEach(listener -> {
			Thread.startVirtualThread(() -> {
				try {
					listener.consum(event);
				} catch (Exception e) {
					log.error(null, e);
				}
			});
		});
	}
}

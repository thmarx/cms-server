package com.github.thmarx.cms.eventbus;

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
	
	public <T extends Event> void register (Class<T> eventClass, EventListener listener) {
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

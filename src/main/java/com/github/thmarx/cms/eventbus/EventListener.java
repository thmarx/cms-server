package com.github.thmarx.cms.eventbus;

/**
 *
 * @author t.marx
 */
public interface EventListener<T extends Event> {
	
	public void consum (T event);
}

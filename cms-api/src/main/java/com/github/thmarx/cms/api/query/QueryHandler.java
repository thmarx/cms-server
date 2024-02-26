package com.github.thmarx.cms.api.query;

/**
 *
 * @author t.marx
 */
public interface QueryHandler<Q extends Query<T>, T> {
	
	 T handle (Q query);
}

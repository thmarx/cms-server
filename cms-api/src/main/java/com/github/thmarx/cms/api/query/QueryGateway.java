package com.github.thmarx.cms.api.query;

import com.github.thmarx.cms.api.annotations.Experimental;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author t.marx
 */
@Experimental
public class QueryGateway {
	
	public Map<Class<? extends Query>, QueryHandler> handlers = new HashMap<>();
	
	public  <T, Q extends Query<T>> void register (Class<Q> query, QueryHandler<Q, T> handler) {
		handlers.put(query, handler);
	}
	
	public <T> T execute (Query<T> query) {
		if (!handlers.containsKey(query.getClass())) {
			return null;
		}
		
		return (T) handlers.get(query.getClass()).handle(query);
	}
}

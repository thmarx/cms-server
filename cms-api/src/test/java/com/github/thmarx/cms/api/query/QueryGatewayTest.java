package com.github.thmarx.cms.api.query;

import lombok.Data;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


/**
 *
 * @author t.marx
 */
public class QueryGatewayTest {
	
	private static QueryGateway gateway = new QueryGateway();

	@BeforeAll
	public static void setup () {
		gateway.register(CustomQuery.class, new CustomQueryHandler());
	}
			
	@Test
	public void testSomeMethod() {
		String message = gateway.execute(new CustomQuery("cms"));
		System.out.println(message);
	}

	public static record CustomQuery(String name) implements Query<String> {
		
	}
	
	public static class CustomQueryHandler implements QueryHandler<CustomQuery, String> {
		@Override
		public String handle(CustomQuery query) {
			return "hello " + query.name();
		}
		
	}
}

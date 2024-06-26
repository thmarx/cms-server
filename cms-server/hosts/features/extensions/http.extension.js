import { UTF_8 } from 'system/charsets.mjs';
import { $hooks } from 'system/hooks.mjs';
import { $http } from 'system/http.mjs';
import { $routes } from 'system/routes.mjs';

// callable via /extensions/test
$http.get("/test", (request, response) => {
	response.addHeader("Content-Type", "text/html; charset=utf-8")
	response.write("ich bin einen test extension!öäü", UTF_8)
})

$hooks.registerAction("server/http/extension/add", (context) => {
	context.arguments().get("httpExtensions").add(
			"GET",
			"/test2",
			(request, response) => {
				response.addHeader("Content-Type", "text/html; charset=utf-8")
				response.write("ich bin einen test extension, registered via hook!", UTF_8)
			}
	)
	return null;
})

$routes.get("/hello-extension", (request, response) => {
	response.addHeader("Content-Type", "text/html; charset=utf-8")
	response.write("extension route", UTF_8)
})

$hooks.registerAction("server/http/route/add", (context) => {
	context.arguments().get("httpRoutes").add(
			"GET",
			"/hello-route",
			(request, response) => {
				response.addHeader("Content-Type", "text/html; charset=utf-8")
				response.write("route via hook!", UTF_8)
			}
	)
	return null;
})

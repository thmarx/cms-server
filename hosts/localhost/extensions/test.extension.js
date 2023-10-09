const UTF_8 = Java.type("java.nio.charset.StandardCharsets").UTF_8
const tryFromString = Java.type("io.undertow.util.HttpString").tryFromString

import {getString} from 'libs/module.mjs';

import {log} from 'system/logging.mjs';

log("Hallo Leute")

extensions.registerHttpExtension(
		"/test-ext",
		(exchange) => {
			exchange.getResponseHeaders().add(tryFromString("Content-Type"), "text/html; charset=utf-8");
			exchange.getResponseSender().send("ich bin einen test extension!öäü", UTF_8);
		}
)



extensions.registerTemplateSupplier(
		"myName",
		() => "Thorsten"
)

extensions.registerTemplateFunction(
		"getHello",
		(name) => "Hello " + name + "!"
)
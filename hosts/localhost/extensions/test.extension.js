const UTF_8 = Java.type("java.nio.charset.StandardCharsets").UTF_8
const tryFromString = Java.type("io.undertow.util.HttpString").tryFromString

import {getString} from 'libs/module.mjs';


extensions.registerHttpExtension(
		"/test-ext",
		(exchange) => {
			exchange.getResponseHeaders().add(tryFromString("Content-Type"), "text/html; charset=utf-8");
			exchange.getResponseSender().send("ich bin einen test extension!öäü", UTF_8);
		}
)

extensions.registerTemplateMethodExtensions(
		"myName",
		(args) => {
			return `${getString()}myName: `
		}
)

extensions.registerTemplateDirectiveExtensions(
		"repeat",
		(env, params, loopvars, body) => {
			let out = env.getOut()
			for (let i = 0; i < 4; i++) {
				if (i !== 0) {
					out.write("<hr/>")
					out.write(getString())
					out.write("<hr/>")
				}
				
				body.render(env.getOut())
			}
		}
)
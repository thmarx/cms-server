
import {getString} from 'libs/module.mjs';

console.log("umlaute: öäüß");
console.log("module: ", getString())

extensions.registerHttpExtension(
		"/test-ext",
		(exchange) => {
			exchange.getResponseSender().send("ich bin einen test extension!öäü");
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
import { getString } from 'libs/module.mjs';

import { UTF_8 } from 'system/charsets.mjs';
import { header } from 'system/http.mjs';
import { getLogger } from 'system/logging.mjs';

const logger = getLogger("extensions");
logger.info("debug log from test extension");

extensions.registerHttpExtension(
	"/test-ext",
	(exchange) => {
		exchange.getResponseHeaders().add(header("Content-Type"), "text/html; charset=utf-8");
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
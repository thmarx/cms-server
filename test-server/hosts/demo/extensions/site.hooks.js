import { $hooks } from 'system/hooks.mjs';
import { getLogger } from 'system/logging.mjs'


const LOGGER = getLogger("extensions");

$hooks.registerAction("theme/template/header", (context) => {
    LOGGER.info("site: logging from a extension");
	return "<!-- header 1 -->";
})
$hooks.registerAction("theme/template/header", (context) => {
	return "<!-- header 2 -->";
})
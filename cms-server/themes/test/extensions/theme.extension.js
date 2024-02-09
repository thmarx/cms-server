import { $shortcodes } from 'system/shortcodes.mjs';

$shortcodes.register(
	"theme_name",
	(params) => `Hello, I'm your <b>default</b> theme.`
)
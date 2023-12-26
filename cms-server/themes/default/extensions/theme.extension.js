import { $shortcodes } from 'system/shortcodes.mjs';

$shortcodes.register(
	"theme_name",
	(params) => `Hello, I'm your default theme.`
)
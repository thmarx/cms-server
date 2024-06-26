import { $hooks } from 'system/hooks.mjs';
import { $shortcodes } from 'system/shortcodes.mjs';


$shortcodes.register(
	"hello",
	(params) => `Hello ${params.get("name")}, I'm a TAG!`
)

/*
$shortcodes.register(
	"name_age",
	(params) => `Hello ${params.get("name")}, your age is ${params.get("age")}!`
)

 */

/*
$hooks.registerFilter("content/shortcodes/filter", (context) => {
	context.values().getFirst().put(
			"name_age",
			(params) => `Hello ${params.get("name")}, your age is ${params.get("age")}!`
	)
	return context.values()
})
 * 
 */
$hooks.registerAction("content/shortcodes/filter", (context) => {
	context.arguments().get("shortCodes").put(
			"name_age",
			(params) => `Hello ${params.get("name")}, your age is ${params.get("age")}!`
	)
	return null;
})


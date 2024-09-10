import { $template } from 'system/template.mjs';
import { $hooks } from 'system/hooks.mjs';

/*
$template.registerTemplateSupplier(
	"myName",
	() => "Thorsten"
)
 */
$hooks.registerAction("system/template/supplier", (context) => {
	context.arguments().get("suppliers").add(
			"myName",
			() => "My name is CondationCMS"
	)
	return null;
})

/*
$template.registerTemplateFunction(
	"getHello",
	(name) => "Hello " + name + "!"
)
 */
$hooks.registerAction("system/template/function", (context) => {
	context.arguments().get("functions").add(
			"getHello",
		(name) => "Hello " + name + "!!"
	)
	return null;
})

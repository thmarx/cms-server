import { $query } from 'system/query.mjs';
import { $hooks } from 'system/hooks.mjs';

/*
$query.registerOperation(
	"none",
	(fieldValue, value) => {
		console.log("none operator")
		return false
	}
)
 */
$hooks.registerAction("db/query/operations/add", (context) => {
	context.arguments().get("operations").add(
			"none",
			(fieldValue, value) => {
				console.log("none operator")
				return false
			}
	)
	return null;
})
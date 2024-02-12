import { $query } from 'system/query.mjs';


$query.registerOperation(
	"none",
	(fieldValue, value) => {
		console.log("none operator")
		return false
	}
)
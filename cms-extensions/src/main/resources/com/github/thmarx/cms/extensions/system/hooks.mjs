import { HookSystemFeature, $features } from 'system/features.mjs';


const hooks = $features.get(HookSystemFeature).hookSystem()

export const $hooks = {
	register : (name, fun, priority) => {
		if (priority) {
				hooks.register(name, fun, priority)
		} else {
			hooks.register(name, fun)
		}
	}
}
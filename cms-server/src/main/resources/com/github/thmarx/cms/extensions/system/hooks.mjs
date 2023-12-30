export const $hooks = {
	register : (name, fun, priority) => {
		if (priority) {
				hooks.register(name, fun, priority)
		} else {
			hooks.register(name, fun)
		}
	}
}
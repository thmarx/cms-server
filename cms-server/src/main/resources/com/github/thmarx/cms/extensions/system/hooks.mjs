export const $hooks = {
	register : (name, fun) => {
		hooks.register(name, fun)
	}
	register : (name, fun, priority) => {
		hooks.register(name, fun, priority)
	}
}
const tryFromString = Java.type("io.undertow.util.HttpString").tryFromString

export function header (name) {
	return tryFromString(name)
}
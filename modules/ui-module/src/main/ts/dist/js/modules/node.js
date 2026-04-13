/**
 * Retrieves a nested value from an object using a dot-notated path like "meta.title"
 * @param {object} sourceObj - The object to retrieve the value from
 * @param {string} path - Dot-notated string path, e.g., "meta.title"
 * @returns {*} - The value found at the given path, or undefined if not found
 */
export function getValueByPath(sourceObj, path) {
    return path.split('.').reduce((acc, part) => acc?.[part], sourceObj);
}
;
/**
 * Builds a values object from an array of form fields
 * @param {Array} fields - Array of form field objects, each with a .name property
 * @param {object} sourceObj - The source object to extract the values from
 * @returns {object} values - An object mapping field names to their corresponding values
 */
export function buildValuesFromFields(fields, sourceObj) {
    const values = {};
    for (const field of fields) {
        if (!field.name)
            continue;
        values[field.name] = getValueByPath(sourceObj, field.name);
    }
    return values;
}
;

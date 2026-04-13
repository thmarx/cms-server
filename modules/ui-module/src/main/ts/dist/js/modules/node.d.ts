export function getValueByPath(sourceObj: object, path: string): any;
/**
 * Builds a values object from an array of form fields
 * @param {Array} fields - Array of form field objects, each with a .name property
 * @param {object} sourceObj - The source object to extract the values from
 * @returns {object} values - An object mapping field names to their corresponding values
 */
export function buildValuesFromFields(fields: any[], sourceObj: object): object;

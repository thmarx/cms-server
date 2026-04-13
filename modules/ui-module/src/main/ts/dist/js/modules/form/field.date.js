import { createID, getUTCDateFromInput, utcToLocalDateInputValue } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js";
const createDateField = (options, value = '') => {
    const placeholder = options.placeholder || "";
    const id = createID();
    const key = "field." + options.name;
    const title = i18n.t(key, options.title);
    let val = '';
    if (value instanceof Date) {
        val = utcToLocalDateInputValue(value.toISOString());
    }
    else if (typeof value === 'string' && value.length > 0) {
        val = utcToLocalDateInputValue(value);
    }
    return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="date">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
			<input type="date" class="form-control" id="${id}" name="${options.name}" placeholder="${placeholder}" value="${val}">
		</div>
	`;
};
const getDateData = (context) => {
    const data = {};
    context.formElement.querySelectorAll("[data-cms-form-field-type='date'] input").forEach((el) => {
        const value = getUTCDateFromInput(el); // "2025-05-31"
        data[el.name] = {
            type: "date",
            value: value === "" ? null : value
        }; // Format: "YYYY-MM-DD"
    });
    return data;
};
export const DateField = {
    markup: createDateField,
    init: (context) => { },
    data: getDateData
};

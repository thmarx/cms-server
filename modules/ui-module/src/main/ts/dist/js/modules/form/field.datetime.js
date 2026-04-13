import { createID, getUTCDateTimeFromInput, utcToLocalDateTimeInputValue } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js";
const createDateTimeField = (options, value = '') => {
    const placeholder = options.placeholder || "";
    const id = createID();
    const key = "field." + options.name;
    const title = i18n.t(key, options.title);
    let val = '';
    if (value instanceof Date) {
        val = utcToLocalDateTimeInputValue(value.toISOString());
    }
    else if (typeof value === 'string' && value.length > 0) {
        val = utcToLocalDateTimeInputValue(value);
    }
    return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="datetime">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
			<input type="datetime-local" class="form-control" id="${id}" name="${options.name}" placeholder="${placeholder}" value="${val}">
		</div>
	`;
};
const getDateTimeData = (context) => {
    const data = {};
    context.formElement.querySelectorAll("[data-cms-form-field-type='datetime'] input").forEach((el) => {
        const value = getUTCDateTimeFromInput(el); // "2025-05-31T15:30"
        data[el.name] = {
            type: 'datetime',
            value: value === "" ? null : value
        };
    });
    return data;
};
export const DateTimeField = {
    markup: createDateTimeField,
    init: (context) => { },
    data: getDateTimeData
};

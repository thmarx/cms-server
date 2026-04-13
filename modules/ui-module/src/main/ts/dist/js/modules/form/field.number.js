import { i18n } from "@cms/modules/localization.js";
import { createID } from "@cms/modules/form/utils.js";
const createNumberField = (options, value = '') => {
    const placeholder = options.placeholder || "";
    const id = createID();
    const key = "field." + options.name;
    const min = options.options.min != null ? `min="${options.options.min}"` : "";
    const max = options.options.max != null ? `max="${options.options.max}"` : "";
    const step = options.options.step != null ? `step="${options.options.step}"` : "";
    const title = i18n.t(key, options.title);
    return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="number">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
			<input type="number" class="form-control" id="${id}" name="${options.name}" placeholder="${placeholder}" value="${value || ''}" ${min} ${max} ${step}>
		</div>
	`;
};
const getData = (context) => {
    const data = {};
    context.formElement.querySelectorAll("[data-cms-form-field-type='number'] input").forEach((el) => {
        const value = el.value;
        data[el.name] = {
            type: 'number',
            value: value !== '' ? Number(value) : null
        };
    });
    return data;
};
export const NumberField = {
    markup: createNumberField,
    init: (context) => { },
    data: getData
};

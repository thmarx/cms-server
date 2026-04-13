import { createID } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js";
const createTextAreaField = (options, value = '') => {
    const rows = options.rows || 5;
    const id = createID();
    const key = "field." + options.name;
    const title = i18n.t(key, options.title);
    return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="text">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
			<textarea class="form-control" id="${id}" name="${options.name}">${value || ''}</textarea>
		</div>
	`;
};
const getData = (context) => {
    var data = {};
    context.formElement.querySelectorAll("[data-cms-form-field-type='text'] textarea").forEach((el) => {
        let value = el.value;
        data[el.name] = {
            type: 'textarea',
            value: value
        };
    });
    return data;
};
export const TextAreaField = {
    markup: createTextAreaField,
    init: (context) => { },
    data: getData
};

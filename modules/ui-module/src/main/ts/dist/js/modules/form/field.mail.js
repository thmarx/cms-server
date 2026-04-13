import { createID } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js";
const createEmailField = (options, value = '') => {
    const placeholder = options.placeholder || "";
    const id = createID();
    const key = "field." + options.name;
    const title = i18n.t(key, options.title);
    return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="mail">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
			<input type="email" class="form-control" id="${id}" name="${options.name}" placeholder="${placeholder}" value="${value || ''}">
		</div>
	`;
};
const getData = (context) => {
    var data = {};
    context.formElement.querySelectorAll("[data-cms-form-field-type='mail'] input").forEach((el) => {
        let value = el.value;
        data[el.name] = {
            type: 'mail',
            value: value
        };
    });
    return data;
};
export const MailField = {
    markup: createEmailField,
    init: (context) => { },
    data: getData
};

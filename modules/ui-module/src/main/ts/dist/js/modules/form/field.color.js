import { createID } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js";
const createColorField = (options, value = '#000000') => {
    const id = createID();
    const key = "field." + options.name;
    const title = i18n.t(key, options.title);
    return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="color">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
			<input type="color" class="form-control form-control-color" id="${id}" name="${options.name}" value="${value || '#000000'}" title="${title}">
		</div>
	`;
};
const getColorData = (context) => {
    const data = {};
    context.formElement.querySelectorAll("[data-cms-form-field-type='color'] input").forEach((el) => {
        data[el.name] = {
            type: 'color',
            value: el.value
        };
    });
    return data;
};
export const ColorField = {
    markup: createColorField,
    init: (context) => { },
    data: getColorData
};

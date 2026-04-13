import { createID } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js";
const createRangeField = (options, value = '') => {
    const id = createID();
    const key = "field." + options.name;
    const min = options.options?.min ?? 0;
    const max = options.options?.max ?? 100;
    const step = options.options?.step ?? 1;
    const title = i18n.t(key, options.title);
    return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="range">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}: <span id="${id}-value">${value || min}</span></label>
			<input type="range" class="form-range" id="${id}" name="${options.name}" 
				min="${min}" max="${max}" step="${step}" value="${value || min}" 
				oninput="document.getElementById('${id}-value').textContent = this.value">
		</div>
	`;
};
const getData = (context) => {
    const data = {};
    context.formElement.querySelectorAll("[data-cms-form-field-type='range'] input").forEach((el) => {
        data[el.name] = {
            type: 'range',
            value: parseFloat(el.value)
        };
    });
    return data;
};
export const RangeField = {
    markup: createRangeField,
    init: (context) => { },
    data: getData
};

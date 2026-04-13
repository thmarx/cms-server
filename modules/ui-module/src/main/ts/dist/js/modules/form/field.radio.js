import { createID } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js";
const createRadioField = (options, value = '') => {
    const id = createID();
    const key = "field." + options.name;
    const name = options.name || id;
    const title = i18n.t(key, options.title);
    const choices = options.options?.choices || [];
    const radios = choices.map((choice, idx) => {
        const inputId = `${id}-${idx}`;
        const checked = value === choice.value ? 'checked' : '';
        return `
			<div class="form-check cms-form-field">
				<input class="form-check-input" type="radio" name="${name}" id="${inputId}" value="${choice.value}" ${checked}>
				<label class="form-check-label" for="${inputId}">
					${choice.label}
				</label>
			</div>
		`;
    }).join('');
    return `
		<div class="mb-3" data-cms-form-field-type="radio">
			<label class="form-label" cms-i18n-key="${key}">${title}</label>
			${radios}
		</div>
	`;
};
const getData = (context) => {
    const data = {};
    context.formElement.querySelectorAll("[data-cms-form-field-type='radio']").forEach(container => {
        const name = container.querySelector("input[type='radio']").name;
        const checked = container.querySelector("input[type='radio']:checked");
        if (checked) {
            data[name] = {
                type: 'radio',
                value: checked.value
            };
        }
    });
    return data;
};
export const RadioField = {
    markup: createRadioField,
    init: (context) => { },
    data: getData
};

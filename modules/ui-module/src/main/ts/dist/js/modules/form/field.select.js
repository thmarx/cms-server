import { createID } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js";
const createSelectField = (options, value = '') => {
    const id = createID();
    const key = "field." + options.name;
    const title = i18n.t(key, options.title);
    const optionTags = (options.options?.choices || []).map(opt => {
        const label = typeof opt === 'object' ? opt.label : opt;
        const val = typeof opt === 'object' ? opt.value : opt;
        const selected = val === value ? ' selected' : '';
        return `<option value="${val}"${selected}>${label}</option>`;
    }).join('\n');
    return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="select">
			<label for="${id}" class="form-label" cms-i18n-key="${key}">${title}</label>
			<select class="form-select" id="${id}" name="${options.name}">
				${optionTags}
			</select>
		</div>
	`;
};
const getData = (context) => {
    const data = {};
    context.formElement
        .querySelectorAll("[data-cms-form-field-type='select'] select")
        .forEach((el) => {
        let value = el.value;
        // optional: type-konvertierung, aber fallback ist immer der echte Wert
        if (value === 'true') {
            value = true;
        }
        else if (value === 'false') {
            value = false;
        }
        data[el.name] = {
            type: 'select',
            value: value
        };
    });
    return data;
};
export const SelectField = {
    markup: createSelectField,
    init: (context) => { },
    data: getData
};

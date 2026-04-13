import { createID } from "@cms/modules/form/utils.js";
import { i18n } from "@cms/modules/localization.js";
let markdownEditors = [];
const createMarkdownField = (options, value = '') => {
    const id = createID();
    const key = "field." + options.name;
    const title = i18n.t(key, options.title);
    return `
		<div class="mb-3 cms-form-field" data-cms-form-field-type="easymde">
			<label class="form-label" cms-i18n-key="${key}">${title}</label>
			<textarea id="${id}" style="display: none; height:0;" data-initial-value="${encodeURIComponent(value)}" name="${options.name}"></textarea>
		</div>
	`;
};
const getData = (context) => {
    const data = {};
    markdownEditors.forEach(({ input, editor }) => {
        data[input.name] = {
            type: "easymde",
            value: editor.value()
        };
    });
    return data;
};
const init = (context) => {
    markdownEditors = [];
    const editorInputs = document.querySelectorAll('[data-cms-form-field-type="easymde"] textarea');
    editorInputs.forEach((input) => {
        const initialValue = decodeURIComponent(input.dataset.initialValue || "");
        input.value = initialValue; // Set initial value for EasyMDE
        const editor = new window.EasyMDE({
            element: input,
            initialValue: initialValue,
            autoDownloadFontAwesome: true,
            spellChecker: false,
            forceSync: true, // keeps textarea value updated
            toolbar: ["bold", "italic", "heading", "|", "quote", "unordered-list", "ordered-list"]
        });
        setTimeout(() => editor.codemirror.refresh(), 500);
        markdownEditors.push({ input, editor });
    });
};
export const EasyMDEField = {
    markup: createMarkdownField,
    init: init,
    data: getData
};

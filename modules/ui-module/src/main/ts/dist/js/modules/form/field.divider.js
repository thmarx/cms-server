import { i18n } from "@cms/modules/localization.js";
const createDivider = (options, value) => {
    const key = "field." + options.name;
    const title = i18n.t(key, options.title || "");
    const showTitle = title && title.trim().length > 0;
    return `
		<div class="my-4 cms-form-field" data-cms-form-field-type="divider">
			<hr class="mb-1">
			${showTitle ? `<div class="text-muted small text-uppercase fw-bold mb-2" cms-i18n-key="${key}">${title}</div>` : ""}
		</div>
	`;
};
const getData = (context) => {
    return {}; // Divider liefert keine Daten zurück
};
export const Divider = {
    markup: createDivider,
    init: (context) => { },
    data: getData
};

import { openModal } from '@cms/modules/modal.js';
import { i18n } from '@cms/modules/localization.js';
const alertSelect = (options) => {
    return new Promise((resolve) => {
        const modalBody = `
            <select id="modalSelect" class="form-select">
                <option value="">${options.placeholder || i18n.t("alerts.select.placeholder", "Select a element")}</option>
                ${Object.entries(options.values || {}).map(([key, value]) => `<option value="${key}">${value}</option>`).join('')}
            </select>`;
        openModal({
            title: options.title || i18n.t("alerts.select.title", "Select element"),
            body: modalBody,
            onOk: () => {
                const selectedValue = document.getElementById('modalSelect').value;
                resolve(selectedValue);
            },
            onCancel: () => resolve(null)
        });
    });
};
const alertError = (options) => {
    openModal({
        title: options.title || i18n.t("alerts.error.title", "Error"),
        body: `<p>${options.message || i18n.t("alerts.error.message", "Some error occured")}</p>`,
    });
};
const alertConfirm = (options) => {
    return new Promise((resolve) => {
        openModal({
            title: options.title || i18n.t("alerts.confirm.title", "Are you sure?"),
            body: `<p>${options.message || i18n.t("alerts.confirm.message", "You won't be able to revert this!")}</p>`,
            onOk: () => resolve(true),
            onCancel: () => resolve(false)
        });
    });
};
const alertPrompt = (options) => {
    return new Promise((resolve) => {
        const modalBody = `
            <label for="modalInput" class="form-label">${options.label || i18n.t("alerts.prompt.label", "Input")}</label>
            <input type="text" id="modalInput" class="form-control" placeholder="${options.placeholder || i18n.t("alerts.prompt.placeholder", "Enter your input")}">`;
        openModal({
            title: options.title || i18n.t("alerts.prompt.title", "Enter value?"),
            body: modalBody,
            validate: () => {
                const value = document.getElementById('modalInput').value;
                if (options.validator) {
                    const validationResult = options.validator(value);
                    return validationResult === null || validationResult === undefined;
                }
                return true;
            },
            onOk: () => {
                const value = document.getElementById('modalInput').value;
                resolve(value);
            },
            onCancel: () => resolve(null)
        });
    });
};
export { alertSelect, alertError, alertConfirm, alertPrompt };

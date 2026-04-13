import { ACTION_LOCALIZATIONS } from '@cms/modules/localization-actions.js';
import { MODULE_LOCALIZATIONS } from '@cms/modules/localization-modules.js';
import { loadLocalizations } from '@cms/modules/rpc/rpc-i18n.js';
const DEFAULT_LOCALIZATIONS = {
    en: {
        "ui.filebrowser.filename": "Filename",
        "ui.filebrowser.title": "Filesystem",
        "menu.settings": "Settings",
        "menu.settings.logout": "Logout",
        "language.en": "English",
        "language.de": "German",
        "buttons.ok": "Ok",
        "buttons.cancle": "Cancel",
        "menu.page.settings": "Page Settings"
    },
    de: {
        "ui.filebrowser.filename": "Dateiname",
        "ui.filebrowser.title": "Dateisystem",
        "menu.settings": "Einstellungen",
        "menu.settings.logout": "Abmelden",
        "language.en": "Englisch",
        "language.de": "Deutsch",
        "buttons.ok": "Ok",
        "buttons.cancle": "Abbrechen",
        "menu.page.settings": "Seiten-Einstellungen"
    }
};
const loadLocalizationsWithDefaults = async () => {
    try {
        const response = (await loadLocalizations()).result;
        // Merge server response into defaults
        for (const lang in response) {
            DEFAULT_LOCALIZATIONS[lang] = {
                ...DEFAULT_LOCALIZATIONS[lang],
                ...ACTION_LOCALIZATIONS[lang],
                ...MODULE_LOCALIZATIONS[lang],
                ...response[lang]
            };
        }
    }
    catch (e) {
        console.warn("Could not load remote translations, falling back to defaults.", e);
    }
    return DEFAULT_LOCALIZATIONS;
};
export { loadLocalizationsWithDefaults };

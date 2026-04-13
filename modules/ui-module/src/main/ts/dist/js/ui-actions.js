import { EventBus } from '@cms/modules/event-bus.js';
import { i18n, localizeUi } from '@cms/modules/localization.js';
import { executeHookAction, executeScriptAction } from '@cms/js/manager-globals';
window.addEventListener("DOMContentLoaded", async () => {
    /*
    await i18n.init()
    await localizeUi();
    var langSelect = document.querySelector(`[data-cms-i18n-lang='${i18n.getLocale()}']`)
    if (langSelect) {
        langSelect.classList.add("active");
    }
    */
    document.querySelectorAll(".cms-lang-selector").forEach($elem => {
        $elem.addEventListener("click", async () => {
            i18n.setLocale($elem.getAttribute("data-cms-i18n-lang"));
            await localizeUi();
            document.querySelectorAll(".cms-lang-selector").forEach(el => {
                el.classList.remove("active");
            });
            $elem.classList.add("active");
            const dropdown = bootstrap.Dropdown.getInstance($elem.closest('.dropdown'))
                || new bootstrap.Dropdown($elem.closest('.dropdown').querySelector('[data-bs-toggle="dropdown"]'));
            dropdown.hide();
        });
    });
    EventBus.on("ui:localeChanged", (data) => {
    });
    const actionElements = document.querySelectorAll('[data-cms-action-definition]');
    actionElements.forEach(element => {
        try {
            element.addEventListener("click", (action) => {
                const definition = element.getAttribute('data-cms-action-definition');
                try {
                    const action = JSON.parse(definition);
                    if (action.type === "hook") {
                        executeHookAction(action);
                    }
                    else if (action.type === "script") {
                        executeScriptAction(action);
                    }
                }
                catch (e) {
                    console.error('error parsing error definition', e);
                }
            });
        }
        catch (e) {
            console.error('', e);
        }
    });
});

import { UIStateManager } from '@cms/modules/ui-state.js';
const DEFAULT_LOCALE = 'en';
export function getLocale() {
    return UIStateManager.getLocale(DEFAULT_LOCALE);
}
export function setLocale(locale) {
    UIStateManager.setLocale(locale);
}

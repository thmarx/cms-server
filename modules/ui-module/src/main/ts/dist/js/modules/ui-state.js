// state-manager.js
import { EventBus } from "@cms/modules/event-bus.js";
const TAB_ID_KEY = "cms-tab-id";
const AUTH_KEY = "cms-auth-token";
export const UIStateManager = {
    // Save generic tab-specific state
    setTabState(key, value) {
        sessionStorage.setItem(key, JSON.stringify(value));
    },
    getTabState(key, defaultValue = null) {
        const raw = sessionStorage.getItem(key);
        return raw ? JSON.parse(raw) : defaultValue;
    },
    setLocale(locale) {
        this.setTabState("cms-locale", locale);
        EventBus.emit("ui:localeChanged", {
            locale: locale
        });
    },
    getLocale() {
        return this.getTabState("cms-locale");
    },
    removeTabState(key) {
        sessionStorage.removeItem(key);
    },
    // Save shared auth state
    setAuthToken(token) {
        localStorage.setItem(AUTH_KEY, token);
    },
    getAuthToken() {
        return localStorage.getItem(AUTH_KEY);
    },
    clearAuthToken() {
        localStorage.removeItem(AUTH_KEY);
    },
};

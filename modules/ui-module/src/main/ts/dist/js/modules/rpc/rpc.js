import { i18n } from "@cms/modules/localization.js";
import { getCSRFToken } from "../utils";
const executeRemoteCall = async (options) => {
    return executeRemoteMethodCall(options.method, options.parameters);
};
const executeRemoteMethodCall = async (method, parameters) => {
    var data = {
        method: method,
        parameters: parameters
    };
    var response = await fetch(window.manager.baseUrl + "/rpc", {
        method: "POST",
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-Token': getCSRFToken()
        },
        body: JSON.stringify(data)
    });
    if (response.status === 403) {
        alert(i18n.t("ui.redirect.login", "You where logged out due to inactivity. Please log in again."));
        window.location.href = window.manager.baseUrl + "/login";
        return;
    }
    return await response.json();
};
export { executeRemoteCall, executeRemoteMethodCall };

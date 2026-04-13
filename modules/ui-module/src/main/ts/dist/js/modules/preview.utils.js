import { EventBus } from "@cms/modules/event-bus.js";
//PreviewHistory.init();
// close overlay on preview loaded
EventBus.on("preview:loaded", (data) => {
    deActivatePreviewOverlay();
});
export const activatePreviewOverlay = () => {
    const overlay = document.getElementById("previewOverlay");
    if (overlay) {
        overlay.style.display = "flex";
    }
};
export const deActivatePreviewOverlay = () => {
    const overlay = document.getElementById("previewOverlay");
    if (overlay) {
        overlay.style.display = "none";
    }
};
const getPreviewFrame = () => {
    return document.getElementById("contentPreview");
};
const getPreviewUrl = () => {
    try {
        return getPreviewFrame().contentWindow.location.href;
    }
    catch (e) {
        console.warn("Konnte iframe-URL nicht auslesen", e);
        return "";
    }
};
const reloadPreview = () => {
    activatePreviewOverlay();
    getPreviewFrame().contentDocument.location.reload(true);
};
const loadPreview = (url) => {
    activatePreviewOverlay();
    try {
        // Fallback-Host für relative URLs, damit URL-Parsing funktioniert
        const dummyBase = window.location.origin;
        const parsedUrl = new URL(url, dummyBase);
        // Wenn "preview" bereits gesetzt ist, nicht erneut hinzufügen
        if (!parsedUrl.searchParams.has("preview")) {
            parsedUrl.searchParams.append("preview", "manager");
        }
        parsedUrl.searchParams.delete("preview-token");
        //parsedUrl.searchParams.append("preview-token", window.manager.previewToken);
        parsedUrl.searchParams.delete("nocache");
        parsedUrl.searchParams.append("nocache", Date.now());
        // Setze zusammengesetzten Pfad + Query zurück in das iframe
        const result = parsedUrl.pathname + parsedUrl.search;
        document.getElementById("contentPreview").src = result;
        //PreviewHistory.navigatePreview(result);
    }
    catch (e) {
        console.error("Ungültige URL:", url, e);
    }
};
export { getPreviewUrl, reloadPreview, loadPreview, getPreviewFrame };

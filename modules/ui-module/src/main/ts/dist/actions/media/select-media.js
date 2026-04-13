import { openFileBrowser } from "@cms/modules/filebrowser.js";
import { i18n } from "@cms/modules/localization.js";
import { getPreviewUrl, reloadPreview } from "@cms/modules/preview.utils.js";
import { getContentNode, setMeta } from "@cms/modules/rpc/rpc-content.js";
import { showToast } from "@cms/modules/toast.js";
export async function runAction(params) {
    var uri = null;
    if (params.options.uri) {
        uri = params.options.uri;
    }
    else {
        const contentNode = await getContentNode({
            url: getPreviewUrl()
        });
        uri = contentNode.result.uri;
    }
    openFileBrowser({
        type: "assets",
        filter: (file) => {
            return file.media || file.directory;
        },
        onSelect: async (file) => {
            if (file && file.uri) {
                var selectedFile = file.uri; // Use the file's URI
                if (file.uri.startsWith("/")) {
                    selectedFile = file.uri.substring(1); // Remove leading slash if present
                }
                var updateData = {};
                updateData[params.options.metaElement] = {
                    type: 'media',
                    value: selectedFile
                };
                var setMetaResponse = await setMeta({
                    uri: uri,
                    meta: updateData
                });
                showToast({
                    title: i18n.t('manager.actions.media.select-media.toast.title', "Media updated"),
                    message: i18n.t('manager.actions.media.select-media.toast.message', "New media has been updated successfully."),
                    type: 'success', // optional: info | success | warning | error
                    timeout: 3000
                });
                reloadPreview();
            }
        }
    });
}

import { i18n } from '@cms/modules/localization.js';
import { alertConfirm } from '@cms/modules/alerts.js';
import { deleteSection } from '@cms/modules/rpc/rpc-content.js';
import { showToast } from '@cms/modules/toast.js';
import { reloadPreview } from '@cms/modules/preview.utils.js';
export async function runAction(params) {
    var confimred = await alertConfirm({
        title: i18n.t("filebrowser.delete.confirm.title", "Are you sure?"),
        message: i18n.t("filebrowser.delete.confirm.message", "You won't be able to revert this!"),
        confirmText: i18n.t("filebrowser.delete.confirm.yes", "Yes, delete it!"),
        cancelText: i18n.t("filebrowser.delete.confirm.no", "No, cancel!")
    });
    if (!confimred) {
        return;
    }
    const sectionUri = params.sectionUri;
    deleteSection({
        uri: sectionUri
    }).then((response) => {
        if (response.error) {
            showToast({
                title: i18n.t("manager.actions.section.delete.error.title", "Error deleting section"),
                message: i18n.t("manager.actions.section.delete.error.message", "Error deleting section"),
                type: 'error',
                timeout: 3000
            });
        }
        else {
            showToast({
                title: i18n.t("manager.actions.section.delete.success.title", "Delete section"),
                message: i18n.t("manager.actions.section.delete.success.message", "Section deleted successfully"),
                type: 'success',
                timeout: 3000
            });
            reloadPreview();
        }
    });
}

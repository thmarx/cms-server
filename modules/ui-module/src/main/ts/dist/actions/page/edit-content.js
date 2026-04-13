import { openModal } from '@cms/modules/modal.js';
import { createForm } from '@cms/modules/form/forms.js';
import { getPreviewUrl, reloadPreview } from '@cms/modules/preview.utils.js';
import { getContentNode, getContent, setContent } from '@cms/modules/rpc/rpc-content.js';
import { i18n } from '@cms/modules/localization.js';
import { showToast } from '@cms/modules/toast.js';
// hook.js
export async function runAction(params) {
    var uri = null;
    if (params.uri) {
        uri = params.uri;
    }
    else {
        const contentNode = await getContentNode({
            url: getPreviewUrl()
        });
        uri = contentNode.result.uri;
    }
    const nodeContent = await getContent({
        uri: uri
    });
    const form = createForm({
        fields: [
            {
                type: params.editor,
                name: 'content',
                title: 'Main content',
                height: '80%'
            }
        ],
        values: {
            "content": nodeContent?.result?.content
        }
    });
    openModal({
        title: 'Edit Content',
        body: 'modal body',
        form: form,
        fullscreen: true,
        onCancel: (event) => { },
        onOk: async (event) => {
            var updateData = form.getData();
            var setContentResponse = await setContent({
                uri: uri,
                content: updateData.content
            });
            showToast({
                title: i18n.t('manager.actions.page.edit-content.toast.title', "Content updated"),
                message: i18n.t('manager.actions.page.edit-content.toast.message', "The content has been updated successfully."),
                type: 'success', // optional: info | success | warning | error
                timeout: 3000
            });
            reloadPreview();
        }
    });
}

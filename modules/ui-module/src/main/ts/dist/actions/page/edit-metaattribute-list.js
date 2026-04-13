import { createForm } from '@cms/modules/form/forms.js';
import { showToast } from '@cms/modules/toast.js';
import { getPreviewUrl, reloadPreview } from '@cms/modules/preview.utils.js';
import { getValueByPath } from '@cms/modules/node.js';
import { getContentNode, getContent, setMeta } from '@cms/modules/rpc/rpc-content.js';
import { i18n } from '@cms/modules/localization.js';
import { openSidebar } from '@cms/modules/sidebar.js';
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
    const getContentResponse = await getContent({
        uri: uri
    });
    let formDefinition = {
        fields: [],
        values: {}
    };
    params.attributes.forEach(attr => {
        formDefinition.values[attr.name] = getValueByPath(getContentResponse?.result?.meta, attr.name);
        formDefinition.fields.push({
            type: attr.editor,
            name: attr.name,
            title: attr.name,
            options: attr.options ? attr.options : {}
        });
    });
    const form = createForm(formDefinition);
    openSidebar({
        title: 'Edit meta attribute',
        body: 'modal body',
        form: form,
        resizable: true,
        onCancel: (event) => { },
        onOk: async (event) => {
            var updateData = form.getData();
            var setMetaResponse = await setMeta({
                uri: uri,
                meta: updateData
            });
            showToast({
                title: i18n.t('manager.actions.page.edit-metaattribute-list.toast.title', "MetaData updated"),
                message: i18n.t('manager.actions.page.edit-metaattribute-list.toast.message', "The metadata has been updated successfully."),
                type: 'success', // optional: info | success | warning | error
                timeout: 3000
            });
            reloadPreview();
        }
    });
}

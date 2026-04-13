import { openSidebar } from '@cms/modules/sidebar.js';
import { createForm } from '@cms/modules/form/forms.js';
import { showToast } from '@cms/modules/toast.js';
import { getContentNode, setMeta, getContent } from '@cms/modules/rpc/rpc-content.js';
import { getPreviewUrl, reloadPreview } from '@cms/modules/preview.utils.js';
import { i18n } from '@cms/modules/localization.js';
import { getPageTemplates } from '@cms/modules/rpc/rpc-manager.js';
import { buildValuesFromFields } from '@cms/modules/node.js';
const DEFAULT_FIELDS = [
    {
        type: 'text',
        name: 'title',
        title: 'Title'
    },
    {
        type: 'select',
        name: 'published',
        title: 'Published',
        options: {
            choices: [
                { label: 'No', value: false },
                { label: 'Yes', value: true }
            ]
        }
    },
    {
        type: 'datetime',
        name: 'publish_date',
        title: 'Publish Date',
    },
    {
        type: 'datetime',
        name: 'unpublish_date',
        title: 'Unpublish Date',
    }
];
export async function runAction(params) {
    const contentNode = await getContentNode({
        url: getPreviewUrl()
    });
    const getContentResponse = await getContent({
        uri: contentNode.result.uri
    });
    var pageTemplates = (await getPageTemplates()).result;
    var selected = pageTemplates.filter(pageTemplate => pageTemplate.template === getContentResponse?.result?.meta?.template);
    var pageSettingsForm = [];
    if (selected.length === 1) {
        pageSettingsForm = selected[0].data?.forms?.settings ? selected[0].data.forms.settings.fields : [];
    }
    //const previewMetaForm = getMetaForm()
    const fields = [
        ...DEFAULT_FIELDS,
        ...pageSettingsForm
    ];
    const values = {
        'title': getContentResponse?.result?.meta?.title,
        'published': getContentResponse?.result?.meta?.published,
        'publish_date': getContentResponse?.result?.meta?.publish_date,
        'unpublish_date': getContentResponse?.result?.meta?.unpublish_date,
        ...buildValuesFromFields(pageSettingsForm, getContentResponse?.result?.meta)
    };
    const form = createForm({
        fields: fields,
        values: values
    });
    openSidebar({
        title: 'Page settings',
        body: 'modal body',
        form: form,
        resizable: true,
        onCancel: (event) => { },
        onOk: async (event) => {
            var updateData = form.getData();
            var setMetaResponse = await setMeta({
                uri: contentNode.result.uri,
                meta: updateData
            });
            showToast({
                title: i18n.t('manager.actions.page.edit-page-settings.toast.title', "Page settings updated"),
                message: i18n.t('manager.actions.page.edit-page-settings.toast.message', "The page settings have been updated successfully."),
                type: 'success', // optional: info | success | warning | error
                timeout: 3000
            });
            reloadPreview();
        }
    });
}

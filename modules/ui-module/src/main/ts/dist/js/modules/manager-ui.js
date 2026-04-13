import { getPreviewUrl } from '@cms/modules/preview.utils.js';
import { getContent, getContentNode } from '@cms/modules/rpc/rpc-content.js';
export function updateStateButton() {
    var previewUrl = getPreviewUrl();
    ;
    if (!previewUrl) {
        document.querySelector('#cms-btn-status').classList.add('disabled');
        document.querySelector('#cms-btn-status').setAttribute('title', 'No preview URL available');
        return;
    }
    var previewUrl = getPreviewUrl();
    getContentNode({
        url: previewUrl
    }).then((contentNode) => {
        getContent({
            uri: contentNode.result.uri
        }).then((getContentResponse) => {
            updateNodeStatus(getContentResponse);
        });
    });
}
function updateNodeStatus(getContentResponse) {
    const statusBtn = document.querySelector('#cms-btn-status');
    if (!statusBtn)
        return;
    // Alle cms-node-status-* Klassen entfernen
    Array.from(statusBtn.classList).forEach(className => {
        if (className.startsWith('cms-node-status-')) {
            statusBtn.classList.remove(className);
        }
    });
    var published = getContentResponse?.result?.status?.published;
    // Status bestimmen (Provider-fähig)
    let status;
    if (!published) {
        status = 'unpublished';
    }
    else if (!getContentResponse?.result?.status?.withinSchedule) {
        status = 'published-not-visible';
    }
    else {
        status = 'published';
    }
    statusBtn.classList.add(`cms-node-status-${status}`);
}

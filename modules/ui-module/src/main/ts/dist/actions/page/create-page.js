import { openFileBrowser } from '@cms/modules/filebrowser.js';
// hook.js
export async function runAction(params) {
    openFileBrowser({
        type: "content"
    });
}

import { executeCommand } from '@cms/modules/system-commands.js';
import { getPreviewUrl } from '@cms/modules/preview.utils.js';
// hook.js
export async function runAction(params) {
    var contentNode = await executeCommand({
        command: "getContentNode",
        parameters: {
            url: getPreviewUrl()
        }
    });
    console.log(contentNode);
}

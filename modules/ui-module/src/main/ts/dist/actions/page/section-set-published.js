import { reloadPreview } from "@cms/modules/preview.utils";
import { setMeta } from "@cms/modules/rpc/rpc-content";
export async function runAction(params) {
    var request = {
        uri: params.sectionUri,
        meta: {
            published: {
                type: "select",
                value: params.published ? true : false,
            }
        }
    };
    var setMetaResponse = await setMeta(request);
    reloadPreview();
}

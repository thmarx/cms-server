import { executeRemoteCall } from '@cms/modules/rpc/rpc.js';
const createPage = async (options) => {
    var data = {
        method: "page.create",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const deletePage = async (options) => {
    var data = {
        method: "page.delete",
        parameters: options
    };
    return await executeRemoteCall(data);
};
export { createPage, deletePage };

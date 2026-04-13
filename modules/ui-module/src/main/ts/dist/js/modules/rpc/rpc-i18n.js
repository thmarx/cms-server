import { executeRemoteCall } from '@cms/modules/rpc/rpc.js';
const loadLocalizations = async (options) => {
    var data = {
        method: "i18n.load"
    };
    return await executeRemoteCall(data);
};
export { loadLocalizations };

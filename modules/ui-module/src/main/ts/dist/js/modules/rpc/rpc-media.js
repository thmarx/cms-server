import { executeRemoteCall } from '@cms/modules/rpc/rpc.js';
const getMediaMetaData = async (options) => {
    var data = {
        method: "media.meta.get",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const setMediaMetaData = async (options) => {
    var data = {
        method: "media.meta.set",
        parameters: options
    };
    return await executeRemoteCall(data);
};
export { getMediaMetaData, setMediaMetaData };

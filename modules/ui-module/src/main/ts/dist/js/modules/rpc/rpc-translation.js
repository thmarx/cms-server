import { executeRemoteCall } from '@cms/modules/rpc/rpc.js';
const getTranslations = async (options) => {
    var data = {
        method: "translations.get",
        parameters: options
    };
    return (await executeRemoteCall(data)).result;
};
const addTranslation = async (options) => {
    var data = {
        method: "translations.add",
        parameters: options
    };
    return (await executeRemoteCall(data)).result;
};
const removeTranslation = async (options) => {
    var data = {
        method: "translations.add",
        parameters: options
    };
    return (await executeRemoteCall(data)).result;
};
export { getTranslations, addTranslation, removeTranslation };

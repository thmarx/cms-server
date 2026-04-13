import { executeRemoteCall } from '@cms/modules/rpc/rpc.js';
const getContentNode = async (options) => {
    var data = {
        method: "content.node",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const getContent = async (options) => {
    var data = {
        method: "content.get",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const setContent = async (options) => {
    var data = {
        method: "content.set",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const setMeta = async (options) => {
    var data = {
        method: "meta.set",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const setMetaBatch = async (options) => {
    var data = {
        method: "meta.set.batch",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const addSection = async (options) => {
    var data = {
        method: "content.section.add",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const deleteSection = async (options) => {
    var data = {
        method: "content.section.delete",
        parameters: options
    };
    return await executeRemoteCall(data);
};
export { getContentNode, getContent, setContent, setMeta, setMetaBatch, addSection, deleteSection };

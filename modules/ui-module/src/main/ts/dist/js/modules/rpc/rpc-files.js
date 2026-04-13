import { executeRemoteCall } from '@cms/modules/rpc/rpc.js';
const listFiles = async (options) => {
    var data = {
        method: "files.list",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const deleteFile = async (options, any) => {
    var data = {
        method: "files.delete",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const deleteFolder = async (options) => {
    var data = {
        method: "files.delete",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const createFolder = async (options) => {
    var data = {
        method: "folders.create",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const createFile = async (options) => {
    var data = {
        method: "files.create",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const renameFile = async (options) => {
    var data = {
        method: "files.rename",
        parameters: options
    };
    return await executeRemoteCall(data);
};
export { listFiles, deleteFile, createFolder, createFile, deleteFolder, renameFile };

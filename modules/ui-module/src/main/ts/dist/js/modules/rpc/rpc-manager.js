import { executeRemoteCall } from '@cms/modules/rpc/rpc.js';
const getSectionTemplates = async (options) => {
    var data = {
        method: "manager.contentTypes.sections",
        parameters: options || {}
    };
    return await executeRemoteCall(data);
};
const getPageTemplates = async (options) => {
    var data = {
        method: "manager.contentTypes.pages",
        parameters: options || {}
    };
    return await executeRemoteCall(data);
};
const getListItemTypes = async (options) => {
    var data = {
        method: "manager.contentTypes.listItemTypes",
        parameters: options || {}
    };
    return await executeRemoteCall(data);
};
const getMediaForm = async (options) => {
    var data = {
        method: "manager.media.form",
        parameters: options || {}
    };
    return await executeRemoteCall(data);
};
const createCSRFToken = async (options) => {
    var data = {
        method: "manager.token.createCSRF",
        parameters: options || {}
    };
    return await executeRemoteCall(data);
};
export var Format;
(function (Format) {
    Format[Format["WEBP"] = 0] = "WEBP";
    Format[Format["JPEG"] = 1] = "JPEG";
    Format[Format["PNG"] = 2] = "PNG";
})(Format || (Format = {}));
const getMediaFormats = async (options) => {
    var data = {
        method: "manager.media.formats",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const getTagNames = async (options) => {
    var data = {
        method: "manager.content.tags",
        parameters: options
    };
    return await executeRemoteCall(data);
};
export { getSectionTemplates, getPageTemplates, getMediaForm, getTagNames, getMediaFormats, getListItemTypes, createCSRFToken };

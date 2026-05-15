export interface CreatePageOptions {
    uri: string;
    name: string;
    contentType: string;
}
export interface CreatePageResponse {
    result: {
        uri?: string;
        error?: string;
    };
}
declare const createPage: (options: CreatePageOptions) => Promise<CreatePageResponse>;
declare const deletePage: (options: any) => Promise<any>;
export { createPage, deletePage };

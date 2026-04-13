export function executeScriptAction(action: any): Promise<void>;
export function executeHookAction(action: any): Promise<void>;export function patchManagerPath(relativePath: string, managerBasePath: string): string;
/**
 * Patches a path with the context path, if not already present.
 *
 * @param {string} path - The original path (e.g. "/assets/images/test.jpg").
 * @returns {string} - The patched path with context prefix if needed.
 */
export function patchPathWithContext(path: string): string;

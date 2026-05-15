/*-
 * #%L
 * UI Module
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import { i18n } from '@cms/modules/localization';
import { openCreateContentBrowser } from '@cms/modules/filebrowser.create.js';
import { loadPreview } from '@cms/modules/preview.utils';
import { patchPathWithContext } from '@cms/js/manager-globals';
// hook.js
export async function runAction(params) {
    openCreateContentBrowser({
        type: "content",
        template: params.template,
        contentType: params.contentType,
        uri: params.folder,
        title: i18n.t("filebrowser.createContent.title", "Create Node"),
        onCreate: (newContent) => {
            console.log("Created new content:", newContent);
            loadPreview(patchPathWithContext(newContent.uri)); // Reload the preview to reflect the new content
        }
    });
}

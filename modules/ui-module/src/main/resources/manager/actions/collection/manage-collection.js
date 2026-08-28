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
import { openCollectionItemEditor } from './edit-collection-item.js';
import { i18n } from '@cms/modules/localization.js';
import { openModal } from '@cms/modules/modal.js';
import { loadPreview } from '@cms/modules/preview.utils.js';
import { listCollectionItems } from '@cms/modules/rpc/rpc-collection.js';
const PAGE_SIZE = 10;
const MIN_SEARCH_LENGTH = 3;
const escapeHtml = (value) => String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
const renderItems = (items) => {
    if (items.length === 0) {
        return `<p class="text-muted mb-0">${i18n.t('collection.items.empty', 'No collection items found.')}</p>`;
    }
    return `<div class="list-group">${items.map(item => `
		<div class="list-group-item d-flex justify-content-between align-items-center gap-3">
			<div class="text-truncate">
				<strong>${escapeHtml(item.title)}</strong><br>
				<small class="text-muted">${escapeHtml(item.id)}</small>
			</div>
			<div class="btn-group btn-group-sm flex-shrink-0">
				<button type="button" class="btn btn-outline-primary" data-collection-edit="${escapeHtml(item.id)}">
					${i18n.t('collection.items.edit', 'Edit')}
				</button>
				${item.detailUrl ? `<button type="button" class="btn btn-outline-secondary"
					data-collection-open="${escapeHtml(item.detailUrl)}">
					${i18n.t('collection.items.open', 'Open detail page')}
				</button>` : ''}
			</div>
		</div>`).join('')}</div>`;
};
export const runAction = async (options) => {
    let currentPage = 1;
    let currentQuery = '';
    let requestVersion = 0;
    let modal;
    const body = `
		<div>
			<label class="form-label" for="cms-collection-search">
				${i18n.t('collection.items.searchLabel', 'Search by title')}
			</label>
			<input type="search" class="form-control" id="cms-collection-search"
				placeholder="${i18n.t('collection.items.searchPlaceholder', 'Search by title...')}" autocomplete="off">
			<div class="mt-3" data-collection-results></div>
			<div class="mt-3" data-collection-pagination></div>
		</div>`;
    const update = async () => {
        const version = ++requestVersion;
        const root = document.querySelector('[data-collection-results]');
        const pagination = document.querySelector('[data-collection-pagination]');
        if (!root || !pagination)
            return;
        root.innerHTML = `<div class="text-muted">${i18n.t('collection.items.loading', 'Loading collection items...')}</div>`;
        try {
            const page = await listCollectionItems({
                collection: options.collection,
                query: currentQuery,
                page: currentPage,
                size: PAGE_SIZE
            });
            if (version !== requestVersion)
                return;
            root.innerHTML = renderItems(page.items);
            pagination.innerHTML = page.totalPages > 1 ? `
				<nav aria-label="Collection pagination">
					<ul class="pagination justify-content-center mb-0">
						<li class="page-item ${page.page <= 1 ? 'disabled' : ''}">
							<button type="button" class="page-link" data-collection-page="${page.page - 1}">
								${i18n.t('pagination.previous', 'Previous')}
							</button>
						</li>
						<li class="page-item disabled"><span class="page-link">${page.page} / ${page.totalPages}</span></li>
						<li class="page-item ${page.page >= page.totalPages ? 'disabled' : ''}">
							<button type="button" class="page-link" data-collection-page="${page.page + 1}">
								${i18n.t('pagination.next', 'Next')}
							</button>
						</li>
					</ul>
				</nav>` : '';
            root.querySelectorAll('[data-collection-edit]').forEach(button => {
                button.addEventListener('click', () => openCollectionItemEditor({
                    collection: options.collection,
                    id: button.dataset.collectionEdit ?? '',
                    onSaved: update
                }));
            });
            root.querySelectorAll('[data-collection-open]').forEach(button => {
                button.addEventListener('click', () => {
                    modal.hide();
                    loadPreview(button.dataset.collectionOpen ?? '');
                });
            });
            pagination.querySelectorAll('[data-collection-page]').forEach(button => {
                button.addEventListener('click', () => {
                    currentPage = Number(button.dataset.collectionPage ?? 1);
                    update();
                });
            });
        }
        catch (error) {
            if (version !== requestVersion)
                return;
            root.innerHTML = `<div class="alert alert-danger">${escapeHtml(error?.message ?? error)}</div>`;
            pagination.innerHTML = '';
        }
    };
    modal = openModal({
        title: options.collection,
        body,
        size: 'xl',
        showFooter: false,
        onShow: (element) => {
            const input = element.querySelector('#cms-collection-search');
            let debounce;
            input?.addEventListener('input', () => {
                window.clearTimeout(debounce);
                debounce = window.setTimeout(() => {
                    const value = input.value.trim();
                    if (value.length > 0 && value.length < MIN_SEARCH_LENGTH)
                        return;
                    currentQuery = value;
                    currentPage = 1;
                    update();
                }, 300);
            });
            update();
        }
    });
};

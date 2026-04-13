import { i18n } from '@cms/modules/localization.js';
const showToast = (options) => {
    const toastId = 'toast_' + Date.now();
    // Fallbacks
    const title = options.title || i18n.t("toast.title", "Note");
    const message = options.message || '';
    const type = options.type || 'info'; // info, success, warning, error
    const timeout = typeof options.timeout === 'number' ? options.timeout : 5000;
    // Toast-Container erstellen, falls nicht vorhanden
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container position-fixed top-0 end-0 p-3';
        document.body.appendChild(container);
    }
    const colorClasses = {
        info: 'bg-info text-white',
        success: 'bg-success text-white',
        warning: 'bg-warning text-dark',
        error: 'bg-danger text-white'
    };
    const toastHtml = `
		<div id="${toastId}" class="toast align-items-center ${colorClasses[type]} border-0 mb-2" role="alert" aria-live="assertive" aria-atomic="true">
		  <div class="d-flex">
			<div class="toast-body">
			  <strong>${title}</strong><br>${message}
			</div>
			<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
		  </div>
		</div>`;
    container.insertAdjacentHTML('beforeend', toastHtml);
    const toastElement = document.getElementById(toastId);
    const toastInstance = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: timeout
    });
    toastInstance.show();
    toastElement.addEventListener('hidden.bs.toast', () => {
        toastElement.remove();
        if (typeof options.onClose === 'function') {
            options.onClose();
        }
    });
};
export { showToast };

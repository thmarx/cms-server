(async function () {
    // Basis-URLs aus Parent ableiten
    const parentManager = window.parent?.manager || {};
    const baseUrl = parentManager.baseUrl || '/manager';
    const contextPath = parentManager.contextPath || '/';
    const importMap = {
        imports: {
            "@cms/js/": `${baseUrl}/js/`,
            "@cms/libs/": `${baseUrl}/js/libs/`,
            "@cms/manager/": `${baseUrl}/js/manager/`,
            "@cms/modules/": `${baseUrl}/js/modules/`
        }
    };
    // 2. Import Map setzen
    const script = document.createElement('script');
    script.type = 'importmap';
    script.textContent = JSON.stringify(importMap);
    script.onload = () => {
        resolve();
    };
    document.head.appendChild(script);
    // 3. window.manager kopieren
    window.manager = parentManager;
    // 4. Plugins laden
    const { initIframe } = await import('@cms/js/manager-inject-init.js');
    initIframe();
})();

import frameMessenger from "../frameMessenger";
import { EDIT_ATTRIBUTES_ICON, EDIT_PAGE_ICON, SECTION_ADD_ICON, SECTION_DELETE_ICON, SECTION_SORT_ICON, SECTION_UNPUBLISHED_ICON } from "./toolbar-icons";
const addSection = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar);
    var command = {
        type: 'add-section',
        payload: {
            sectionName: toolbarDefinition.sectionName,
        }
    };
    frameMessenger.send(window.parent, command);
};
const deleteSection = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar);
    var command = {
        type: 'delete-section',
        payload: {
            sectionUri: toolbarDefinition.uri
        }
    };
    frameMessenger.send(window.parent, command);
};
const setPublishForSection = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar);
    var action = event.currentTarget.getAttribute('data-cms-action');
    var command = {
        type: 'section-set-published',
        payload: {
            sectionUri: toolbarDefinition.uri,
            published: action === "publish"
        }
    };
    frameMessenger.send(window.parent, command);
};
const orderSections = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar);
    var command = {
        type: 'edit-sections',
        payload: {
            sectionName: toolbarDefinition.sectionName
        }
    };
    frameMessenger.send(window.parent, command);
};
const editContent = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar);
    var command = {
        type: 'edit',
        payload: {
            editor: "markdown",
            element: "content"
        }
    };
    if (toolbarDefinition.uri) {
        command.payload.uri = toolbarDefinition.uri;
    }
    frameMessenger.send(window.parent, command);
};
const editAttributes = (event) => {
    var toolbar = event.target.closest('[data-cms-toolbar]');
    var toolbarDefinition = JSON.parse(toolbar.dataset.cmsToolbar);
    var command = {
        type: 'edit',
        payload: {
            editor: "form",
            element: "meta"
        }
    };
    if (toolbarDefinition.uri) {
        command.payload.uri = toolbarDefinition.uri;
    }
    var elements = [];
    toolbar.parentNode.querySelectorAll("[data-cms-editor]").forEach(($elem) => {
        var toolbar = $elem.dataset.cmsToolbar ? JSON.parse($elem.dataset.cmsToolbar) : {};
        if ($elem.dataset.cmsElement === "meta"
            && (!toolbar.id || toolbar.id === toolbarDefinition.id)) {
            elements.push({
                name: $elem.dataset.cmsMetaElement,
                editor: $elem.dataset.cmsEditor,
                options: $elem.dataset.cmsEditorOptions ? JSON.parse($elem.dataset.cmsEditorOptions) : {}
            });
        }
    });
    command.payload.metaElements = elements;
    frameMessenger.send(window.parent, command);
};
export const initToolbar = (container) => {
    var toolbarDefinition = JSON.parse(container.dataset.cmsToolbar);
    if (!toolbarDefinition.actions) {
        return;
    }
    var toolbarContainer = document.createElement('div');
    toolbarContainer.dataset.cmsToolbar = JSON.stringify(toolbarDefinition);
    toolbarContainer.style.position = 'absolute';
    toolbarContainer.style.zIndex = '9999'; // optional: damit sie über allem liegt
    if (toolbarDefinition.type === "sections") {
        toolbarContainer.classList.add("cms-ui-editable-sections");
    }
    else {
        toolbarContainer.classList.add("cms-ui-editable");
    }
    const toolbar = document.createElement('div');
    toolbar.className = 'cms-ui-toolbar';
    if (toolbarDefinition.type === "sections") {
        toolbar.classList.add("cms-ui-toolbar-tl");
    }
    else {
        toolbar.classList.add("cms-ui-toolbar-tr");
    }
    toolbar.classList.add("cms-ui-toolbar");
    toolbarDefinition.actions.forEach(action => {
        if (action === "editContent") {
            const button = document.createElement('button');
            button.setAttribute('data-cms-action', 'edit');
            button.innerHTML = EDIT_PAGE_ICON;
            button.setAttribute("title", "Edit content");
            button.addEventListener('click', editContent);
            toolbar.appendChild(button);
        }
        else if (action === "editAttributes") {
            const button = document.createElement('button');
            button.setAttribute('data-cms-action', 'editAttributes');
            button.innerHTML = EDIT_ATTRIBUTES_ICON;
            button.setAttribute("title", "Edit attributes");
            button.addEventListener('click', editAttributes);
            toolbar.appendChild(button);
        }
        else if (action === "orderSections") {
            const button = document.createElement('button');
            button.setAttribute('data-cms-action', 'editSections');
            button.innerHTML = SECTION_SORT_ICON;
            button.setAttribute("title", "Order");
            button.addEventListener('click', orderSections);
            toolbar.appendChild(button);
        }
        else if (action === "addSection") {
            const button = document.createElement('button');
            button.setAttribute('data-cms-action', 'addSection');
            button.innerHTML = SECTION_ADD_ICON;
            button.setAttribute("title", "Add");
            button.addEventListener('click', addSection);
            toolbar.appendChild(button);
        }
        else if (action === "deleteSection") {
            const button = document.createElement('button');
            button.setAttribute('data-cms-action', 'deleteSection');
            button.innerHTML = SECTION_DELETE_ICON;
            button.setAttribute("title", "Delete");
            button.addEventListener('click', deleteSection);
            toolbar.appendChild(button);
        }
    });
    if (toolbarDefinition.type === "section") {
        const button = document.createElement('button');
        button.setAttribute('data-cms-action', 'publish');
        button.setAttribute('data-cms-section-uri', toolbarDefinition.uri);
        button.classList.add('cms-unpublished');
        button.innerHTML = SECTION_UNPUBLISHED_ICON;
        button.setAttribute("title", "Publish");
        button.addEventListener('click', setPublishForSection);
        toolbar.appendChild(button);
    }
    toolbarContainer.appendChild(toolbar);
    document.body.appendChild(toolbarContainer);
    const positionToolbarContainer = () => {
        const rect = container.getBoundingClientRect();
        toolbarContainer.style.top = `${window.scrollY + rect.top - 2}px`;
        toolbarContainer.style.left = `${window.scrollX + rect.left - 2}px`;
        toolbarContainer.style.width = `${rect.width}px`;
        toolbarContainer.style.height = `${rect.height}px`;
        toolbarContainer.style.display = 'block';
    };
    container.addEventListener('mouseenter', (event) => {
        event.stopPropagation();
        console.log("mouseenter container");
        positionToolbarContainer();
        toolbar.classList.add('visible');
    });
    container.addEventListener('mouseleave', (event) => {
        event.stopPropagation();
        if (!event.relatedTarget || !toolbar.contains(event.relatedTarget)) {
            console.log("mouseleave container");
            toolbar.classList.remove('visible');
            toolbarContainer.style.display = 'none';
        }
    });
    /*
        toolbar.addEventListener('mouseleave', (event : MouseEvent) => {
            if (!event.relatedTarget || event.relatedTarget as Node !== container) {
                toolbar.classList.remove('visible');
                toolbarContainer.style.display = 'none';
            }
        });
    */
    window.addEventListener('scroll', () => {
        if (toolbarContainer.style.display === 'block')
            positionToolbarContainer();
    });
    window.addEventListener('resize', () => {
        if (toolbarContainer.style.display === 'block')
            positionToolbarContainer();
    });
};

import { $hooks } from 'system/hooks.mjs';
/*
legacy assets
theme.getAssets().addJs("/assets/form-1.js")
theme.getAssets().addCss("/assets/form-1.css")

theme.getAssets().addJs("/assets/app-1.js")
theme.getAssets().addCss("/assets/styles-1.css")
*/

$hooks.register(
    "theme/header",
    (context) => {
        return `
            <link rel="stylesheet" href="/assets/form-1.css" defer />
            <link rel="stylesheet" href="/assets/styles-1.css" defer />
            <script src="/assets/form-1.js" defer></script>
            <script src="/assets/app-1.js" defer></script>
        `;
    }
);
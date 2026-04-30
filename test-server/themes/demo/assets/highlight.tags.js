hljs.registerLanguage('tag', function (hljs) {
    return {
        name: 'Tags',
        case_insensitive: true, // Shortcodes are often case-insensitive
        contains: [
            {
                className: 'tag',
                begin: '\\[\\[', // Start of the shortcode: [[
                end: '\\]\\]',   // End of the shortcode: ]]
                contains: [
                    {
                        className: 'name',
                        begin: /[a-zA-Z][\w-]*/, // Matches tag names like tag1, my-tag
                    },
                    {
                        className: 'attribute',
                        begin: /\s+[a-zA-Z][\w-]*/, // Matches attribute names
                        starts: {
                            className: 'string',
                            begin: /=/,
                            end: /[\s\]]/, // Ends with space or closing tag
                            excludeEnd: true,
                            contains: [
                                {
                                    className: 'string',
                                    variants: [
                                        { begin: /"/, end: /"/ }, // Double-quoted string
                                        { begin: /'/, end: /'/ }, // Single-quoted string
                                        { begin: /[^\s\]]+/ },    // Unquoted string
                                    ]
                                }
                            ]
                        }
                    }
                ]
            },
            hljs.COMMENT(
                '\\[\\[!--', // Comment start [[!--
                '--\\]\\]'  // Comment end --]]
            )
        ]
    };
});
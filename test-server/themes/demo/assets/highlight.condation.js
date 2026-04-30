/*! `twig` grammar compiled for Highlight.js 11.11.1 */
(function () {
  var hljsGrammar = (function () {
    'use strict';

    function condation(hljs) {
      const regex = hljs.regex;
const FUNCTION_NAMES = [
      "block",
      "include"
    ];

    const FILTERS = [
      "date",
      "default",
      "raw",
      "upper"
    ];

    let TAG_NAMES = [
      "apply",
      "autoescape",
      "block",
      "cache",
      "deprecated",
      "do",
      "embed",
      "extends",
      "filter",
      "flush",
      "for",
      "form_theme",
      "from",
      "if",
      "import",
      "include",
      "macro",
      "sandbox",
      "set",
      "stopwatch",
      "trans",
      "trans_default_domain",
      "transchoice",
      "use",
      "verbatim",
      "with"
    ]; 

      TAG_NAMES = TAG_NAMES.concat(TAG_NAMES.map(t => `end${t}`));

      const STRING = {
        scope: 'string',
        variants: [
          {
            begin: /'/,
            end: /'/
          },
          {
            begin: /"/,
            end: /"/
          },
        ]
      };

      const NUMBER = {
        scope: "number",
        match: /\d+/
      };

      const PARAMS = {
        begin: /\(/,
        end: /\)/,
        excludeBegin: true,
        excludeEnd: true,
        contains: [
          STRING,
          NUMBER
        ]
      };


      const FUNCTIONS = {
        beginKeywords: FUNCTION_NAMES.join(" "),
        keywords: { name: FUNCTION_NAMES },
        relevance: 0,
        contains: [PARAMS]
      };

      const FILTER = {
        match: /\|(?=[A-Za-z_]+:?)/,
        beginScope: "punctuation",
        relevance: 0,
        contains: [
          {
            match: /[A-Za-z_]+:?/,
            keywords: FILTERS
          },
        ]
      };

      const tagNamed = (tagnames, { relevance }) => {
        return {
          beginScope: {
            1: 'template-tag',
            3: 'name'
          },
          relevance: relevance || 2,
          endScope: 'template-tag',
          begin: [
            /\{%/,
            /\s*/,
            regex.either(...tagnames)
          ],
          end: /%\}/,
          keywords: "in",
          contains: [
            FILTER,
            FUNCTIONS,
            STRING,
            NUMBER
          ]
        };
      };

      const CUSTOM_TAG_RE = /[a-z_]+/;
      const TAG = tagNamed(TAG_NAMES, { relevance: 2 });
      const CUSTOM_TAG = tagNamed([CUSTOM_TAG_RE], { relevance: 1 });

      const TEMPLATE_COMPONENT_RULE = {
        scope: 'template-component',
        begin: /\{\[/,
        end: /\]\}/,
        contains: [
          {
            // Matchet den Blocknamen nach {[ oder {[ end...
            className: 'name',
            match: /\b(?:end)?[a-z_][a-z0-9_]*\b/,
            relevance: 10
          },
          {
            // Parameter wie foo=123 oder bar="abc"
            begin: /\b[a-z_][a-z0-9_]*(?==)/,
            className: 'attr',
            starts: {
              begin: /=/,
              end: /(?=\s|\]\})/,
              excludeBegin: true,
              contains: [
                {
                  scope: 'string',
                  variants: [
                    { begin: /'/, end: /'/ },
                    { begin: /"/, end: /"/ }
                  ]
                },
                { scope: 'number', match: /\d+/ }
              ]
            }
          },
          // optional: Strings oder Zahlen, die direkt im Block stehen
          {
            scope: 'string',
            variants: [
              { begin: /'/, end: /'/ },
              { begin: /"/, end: /"/ }
            ]
          },
          { scope: 'number', match: /\d+/ }
        ]
      };


      return {
        name: 'Condation',
        case_insensitive: true,
        subLanguage: 'twig',
        contains: [
          hljs.COMMENT(/\{#/, /#\}/),
          TAG,
          CUSTOM_TAG,
          {
            className: 'template-variable',
            begin: /\{\{/,
            end: /\}\}/,
            contains: [
              'self',
              FILTER,
              FUNCTIONS,
              STRING,
              NUMBER
            ]
          },
          /*
          {
            className: 'template-component',
            begin: /\{\[/,
            end: /\]\}/,
            contains: [
              'self',
              FILTER,
              FUNCTIONS,
              STRING,
              NUMBER
            ]
          }*/
         /*
          {
            scope: 'template-component',
            begin: /\{\[/,
            end: /\]\}/,
            contains: [
              {
                // match "component" oder "endcomponent"
                className: 'name',
                match: /\b(?:end)?[a-z_][a-z0-9_]*\b/,
                relevance: 10
              },
              {
                // Parameter wie foo=123 oder bar="abc"
                begin: /\b[a-z_][a-z0-9_]*(?==)/,
                className: 'attr',
                starts: {
                  begin: /=/,
                  end: /(?=\s|\]\})/,
                  excludeBegin: true,
                  contains: [
                    STRING,
                    NUMBER
                  ]
                }
              },
              STRING,
              NUMBER
            ]
          }
          */
         TEMPLATE_COMPONENT_RULE
        ]
      };
    }

    return condation;

  })();

  hljs.registerLanguage('condation', hljsGrammar);
})();
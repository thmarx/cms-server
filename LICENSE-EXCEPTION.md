# Additional Permission under AGPLv3 Section 7

Copyright (c) CondationCMS Contributors

CondationCMS is licensed under the GNU Affero General Public License version 3 (AGPLv3).

This file defines an additional permission under Section 7 of the AGPLv3.

---

## Module and Extension Exception

Modules, plugins, themes, templates, and extensions that interact exclusively with CondationCMS through its public and documented extension APIs are considered **independent works** and are not subject to the copyleft requirements of the AGPLv3.

Such modules, plugins, themes, templates, and extensions may therefore be distributed under any license, including proprietary or commercial licenses.

This exception applies only if the extension:

- Uses only the public and documented extension APIs intended for third-party development.
- Remains fully separable from the CondationCMS core system.
- Can be installed, updated, or removed without modifying CondationCMS core source files.

---

## Definition of Public Extension APIs

For the purpose of this exception, “public and documented extension APIs” includes only officially documented extension points intended for third-party development, such as:

- Hooks
- Events
- Services
- Template APIs
- Plugin APIs
- Module APIs
- Extension interfaces
- Other documented extension points

Internal classes, internal services, private APIs, and undocumented implementation details are explicitly excluded.

---

## Core Modifications and Derivative Works

This exception does **not** apply to any of the following:

- Modifications to CondationCMS core source code
- Patching or altering core functionality
- Copying or reusing substantial portions of core source code
- Subclassing or replacing internal core classes not intended as public APIs
- Creating derivative works based on internal implementation details

Any such modifications remain fully subject to the AGPLv3.

---

## Installation Boundary

Extensions covered by this exception must:

- Be installable and runnable without changes to the CondationCMS core codebase
- Not require modification of core files for integration
- Interact with the system only through documented extension mechanisms

---

## No Additional Restrictions

This exception does not limit or modify any rights granted under the AGPLv3 itself. All other parts of CondationCMS remain licensed under the AGPLv3.

For the full license text, see the `LICENSE` file in this repository.

---
title: Extensions
template: start
menu: 
    order: 90
---

## Extensions
Extension are writen in good old JavaScript.

Put your extension into the _host_**/extensions** folder. 
All files with the nameing convention _name_**.extension.js** are loaded ad system startup.

### Add custom http endpoint
```javascript
import { UTF_8 } from 'system/charsets.mjs';
import { header } from 'system/http.mjs';

extensions.registerHttpExtension(
	"/test-ext",
	(exchange) => {
		exchange.getResponseHeaders().add(header("Content-Type"), "text/html; charset=utf-8");
		exchange.getResponseSender().send("I'm a test extension!", UTF_8);
	}
)
```

### Modules
To structure your extension code, you can create modules. 
But keep in mind, your modules must use the .mjs extension. 
Otherwise our js engine will not load your modules correctly.

### System modules
cms-server comes with some system modules.
All system modules are in the _systems_ package, so you can not use _system_ as folder name for custom modules.

#### http

```javascript
import { header } from 'system/http.mjs';

exchange.getResponseHeaders().add(header("Content-Type"), "text/html; charset=utf-8");
```

#### charsets

```javascript
import { UTF_8, UTF_16, ISO_88591 } from 'system/charsets.mjs';

exchange.getResponseSender().send("I'm a test extension!", UTF_8);
```

#### logging
```javascript
import { getLogger } from 'system/logging.mjs';
const logger = getLogger("extensions");
logger.debug("debug log from test extension");
```
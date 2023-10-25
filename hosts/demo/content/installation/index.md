---
title: Installation & Configuration
template: start.html
menu: 
    position: 10
    title: "Installation"
---

## Installation

Installation is very simple. Just download the current version from https://github.com/thmarx/cms/releases 
and extract into desired folder.

## Configuration

We have to kinds of configuration, global and per host configurations.

### Global config

First there is the application.properties. 
Here you can config the **port** and the **ip** the server will listen on.
Your options for **server.engine** are _undertow_ and _jetty_.
Setting the _dev_ mode to true, will disable most caches. For production you should definitly set this to false.

**Example server config**
```properties
server.port=8080
server.ip=127.0.0.1
server.engine=jetty
dev=true
```

### Per host config

The per host config is not shared between virtual hosts.

*hostname* is the hostname under witch the virtual host should be reachable.  
*tempate.engine* the template engine to be used for this virtual host. 
Your options here are currently _freemarker_, _thymeleaf_ and _pebble_.
Default template engine is _freemarker_.  
*markdown.engine* the markdown rendering engine used for this virtual host.
Options are _flexmark_ and _markd_.
Default markdown engine is _flexmark_.

**Example host config**
```yaml
hostname: localhost
template:
  engine: thymeleaf
markdownd:
  engine: flexmark
```

## Starting

### Linux
```shell
java -Dlog4j2.configurationFile=log4j2.xml -jar cms-server-1.0.jar
```
### Windows
```shell
java "-Dlog4j2.configurationFile=log4j2.xml" -jar cms-server-1.0.jar
```